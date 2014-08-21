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
package syncleus.dann.neural.som;

import syncleus.dann.Classifying;
import syncleus.dann.data.Data;
import syncleus.dann.data.DataCase;
import syncleus.dann.data.Dataset;
import syncleus.dann.learn.AbstractLearning;
import syncleus.dann.learn.ErrorLearning;
import syncleus.dann.learn.MLResettable;
import syncleus.dann.math.array.EngineArray;
import syncleus.dann.data.matrix.SimpleRealMatrix;
import syncleus.dann.neural.som.encog.basic.BestMatchingUnit;

/**
 * A self organizing map neural network.
 */
public class SOMEncog<D extends Data> extends AbstractLearning implements Classifying<D,Integer>, MLResettable,  ErrorLearning<D> {

    /**
     * Serial id.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Do not allow patterns to go below this very small number.
     */
    public static final double VERYSMALL = 1.E-30;

    /**
     * The weights of the output neurons base on the input from the input
     * neurons.
     */
    private SimpleRealMatrix weights;

    /**
     * Default constructor.
     */
    public SOMEncog() {

    }

    /**
     * The constructor.
     *
     * @param inputCount  Number of input neurons
     * @param outputCount Number of output neurons
     */
    public SOMEncog(final int inputCount, final int outputCount) {
        this.weights = new SimpleRealMatrix(outputCount, inputCount);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double calculateError(final Dataset<D> data) {

        final BestMatchingUnit bmu = new BestMatchingUnit(this);

        bmu.reset();

        // Determine the BMU for each training element.
        for (final DataCase pair : data) {
            final Data input = pair.getInput();
            bmu.calculateBMU(input);
        }

        // update the error
        return bmu.getWorstDistance() / 100.0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Integer classify(final D input) {
        if (input.size() > getInputCount()) {
            throw new RuntimeException(
                    "Can't classify SOM with input size of " + getInputCount()
                            + " with input data of count " + input.size());
        }

        final double[][] m = this.weights.getData();
        final double[] inputData = input.getData();
        double minDist = Double.POSITIVE_INFINITY;
        int result = -1;

        for (int i = 0; i < getOutputCount(); i++) {
            final double dist = EngineArray.euclideanDistance(inputData, m[i]);
            if (dist < minDist) {
                minDist = dist;
                result = i;
            }
        }

        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getInputCount() {
        return this.weights.getCols();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getOutputCount() {
        return this.weights.getRows();
    }

    /**
     * @return the weights
     */
    public SimpleRealMatrix getWeights() {
        return this.weights;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void reset() {
        this.weights.randomize(-1, 1);

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void reset(final int seed) {
        reset();
    }

    /**
     * @param weights the weights to set
     */
    public void setWeights(final SimpleRealMatrix weights) {
        this.weights = weights;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateProperties() {
        // unneeded
    }

    /**
     * An alias for the classify method, kept for compatibility with earlier
     * versions of Encog.
     *
     * @param input The input pattern.
     * @return The winning neuron.
     */
    public int winner(final D input) {
        return classify(input);
    }

}
