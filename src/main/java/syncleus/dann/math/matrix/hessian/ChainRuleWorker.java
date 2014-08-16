/*
 * Encog(tm) Core v3.2 - Java Version
 * http://www.heatonresearch.com/encog/
 * https://github.com/encog/encog-java-core

 * Copyright 2008-2013 Heaton Research, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * For more information on Heaton Research copyrights, licenses
 * and trademarks visit:
 * http://www.heatonresearch.com/copyright
 */
package syncleus.dann.math.matrix.hessian;

import org.encog.neural.flat.FlatNetwork;
import syncleus.dann.data.basic.BasicMLDataPair;
import syncleus.dann.data.DataSample;
import syncleus.dann.data.DataSet;
import syncleus.dann.math.array.EngineArray;
import syncleus.dann.neural.activation.EncogActivationFunction;

/**
 * A threaded worker that is used to calculate the first derivatives of the
 * output of the neural network. These values are ultimatly used to calculate
 * the Hessian.
 */
public class ChainRuleWorker implements Runnable {

    /**
     * The actual values from the neural network.
     */
    private final double[] actual;

    /**
     * The deltas for each layer.
     */
    private final double[] layerDelta;

    /**
     * The neuron counts, per layer.
     */
    private final int[] layerCounts;

    /**
     * The feed counts, per layer.
     */
    private final int[] layerFeedCounts;

    /**
     * The layer indexes.
     */
    private final int[] layerIndex;

    /**
     * The index to each layer's weights and thresholds.
     */
    private final int[] weightIndex;

    /**
     * The output from each layer.
     */
    private final double[] layerOutput;

    /**
     * The sums.
     */
    private final double[] layerSums;

    /**
     * The weights and thresholds.
     */
    private final double[] weights;

    /**
     * The flat network.
     */
    private final FlatNetwork flat;

    /**
     * The training data.
     */
    private final DataSet training;

    /**
     * The output neuron to calculate for.
     */
    private int outputNeuron;

    /**
     * The total first derivatives.
     */
    private final double[] totDeriv;

    /**
     * The gradients.
     */
    private final double[] gradients;

    /**
     * The error.
     */
    private double error;

    /**
     * The low range.
     */
    private final int low;

    /**
     * The high range.
     */
    private final int high;

    /**
     * The pair to use for training.
     */
    private final DataSample pair;

    /**
     * The weight count.
     */
    private final int weightCount;

    /**
     * The hessian for this worker.
     */
    private final double[][] hessian;

    /**
     * Construct the chain rule worker.
     *
     * @param theNetwork  The network to calculate a Hessian for.
     * @param theTraining The training data.
     * @param theLow      The low range.
     * @param theHigh     The high range.
     */
    public ChainRuleWorker(final FlatNetwork theNetwork,
                           final DataSet theTraining, final int theLow, final int theHigh) {

        this.weightCount = theNetwork.getWeights().length;
        this.hessian = new double[this.weightCount][this.weightCount];

        this.training = theTraining;
        this.flat = theNetwork;

        this.layerDelta = new double[flat.getLayerOutput().length];
        this.actual = new double[flat.getOutputCount()];
        this.totDeriv = new double[weightCount];
        this.gradients = new double[weightCount];

        this.weights = flat.getWeights();
        this.layerIndex = flat.getLayerIndex();
        this.layerCounts = flat.getLayerCounts();
        this.weightIndex = flat.getWeightIndex();
        this.layerOutput = flat.getLayerOutput();
        this.layerSums = flat.getLayerSums();
        this.layerFeedCounts = flat.getLayerFeedCounts();
        this.low = theLow;
        this.high = theHigh;
        this.pair = BasicMLDataPair.createPair(flat.getInputCount(),
                flat.getOutputCount());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void run() {
        this.error = 0;
        EngineArray.fill(this.hessian, 0);
        EngineArray.fill(this.totDeriv, 0);
        EngineArray.fill(this.gradients, 0);

        final double[] derivative = new double[this.weightCount];

        // Loop over every training element
        for (int i = this.low; i <= this.high; i++) {
            this.training.getRecord(i, this.pair);

            EngineArray.fill(derivative, 0);

            process(outputNeuron, derivative, pair.getInputArray(),
                    pair.getIdealArray());

        }

    }

    /**
     * Process one training set element.
     *
     * @param input The network input.
     * @param ideal The ideal values.
     */
    private void process(final int outputNeuron, final double[] derivative,
                         final double[] input, final double[] ideal) {

        this.flat.compute(input, this.actual);

        final double e = ideal[outputNeuron] - this.actual[outputNeuron];
        this.error += e * e;

        for (int i = 0; i < this.actual.length; i++) {

            if (i == outputNeuron) {
                this.layerDelta[i] = this.flat.getActivationFunctions()[0]
                        .derivative(this.layerSums[i],
                                this.layerOutput[i]);
            } else {
                this.layerDelta[i] = 0;
            }
        }

        for (int i = this.flat.getBeginTraining(); i < this.flat
                .getEndTraining(); i++) {
            processLevel(i, derivative);
        }

        // calculate gradients
        for (int j = 0; j < this.weights.length; j++) {
            this.gradients[j] += e * derivative[j];
            totDeriv[j] += derivative[j];
        }

        // update hessian
        for (int i = 0; i < this.weightCount; i++) {
            for (int j = 0; j < this.weightCount; j++) {
                this.hessian[i][j] += derivative[i] * derivative[j];
            }
        }
    }

    /**
     * Process one level.
     *
     * @param currentLevel The level.
     */
    private void processLevel(final int currentLevel, final double[] derivative) {
        final int fromLayerIndex = this.layerIndex[currentLevel + 1];
        final int toLayerIndex = this.layerIndex[currentLevel];
        final int fromLayerSize = this.layerCounts[currentLevel + 1];
        final int toLayerSize = this.layerFeedCounts[currentLevel];

        final int index = this.weightIndex[currentLevel];
        final EncogActivationFunction activation = this.flat
                .getActivationFunctions()[currentLevel + 1];

        // handle weights
        int yi = fromLayerIndex;
        for (int y = 0; y < fromLayerSize; y++) {
            final double output = this.layerOutput[yi];
            double sum = 0;
            int xi = toLayerIndex;
            int wi = index + y;
            for (int x = 0; x < toLayerSize; x++) {
                derivative[wi] += output * this.layerDelta[xi];
                sum += this.weights[wi] * this.layerDelta[xi];
                wi += fromLayerSize;
                xi++;
            }

            this.layerDelta[yi] = sum
                    * (activation.derivative(this.layerSums[yi],
                    this.layerOutput[yi]));
            yi++;
        }
    }

    /**
     * @return the outputNeuron
     */
    public int getOutputNeuron() {
        return outputNeuron;
    }

    /**
     * @param outputNeuron the outputNeuron to set
     */
    public void setOutputNeuron(final int outputNeuron) {
        this.outputNeuron = outputNeuron;
    }

    /**
     * @return The first derivatives, used to calculate the Hessian.
     */
    public double[] getDerivative() {
        return this.totDeriv;
    }

    /**
     * @return the gradients
     */
    public double[] getGradients() {
        return gradients;
    }

    /**
     * @return The SSE error.
     */
    public double getError() {
        return this.error;
    }

    /**
     * @return The flat network.
     */
    public FlatNetwork getNetwork() {
        return this.flat;
    }

    /**
     * @return the hessian
     */
    public double[][] getHessian() {
        return hessian;
    }

}
