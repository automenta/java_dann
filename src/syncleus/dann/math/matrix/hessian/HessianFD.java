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

import syncleus.dann.data.Data;
import syncleus.dann.data.DataCase;
import syncleus.dann.data.Dataset;
import syncleus.dann.data.vector.VectorData;
import syncleus.dann.math.EncogMath;
import syncleus.dann.math.array.EngineArray;
import syncleus.dann.math.matrix.SimpleRealMatrix;
import syncleus.dann.math.statistics.ErrorCalculation;
import syncleus.dann.neural.networks.VectorNeuralNetwork;

/**
 * Calculate the Hessian matrix using the finite difference method. This is a
 * very simple method of calculating the Hessian. The algorithm does not vary
 * greatly by number layers. This makes it very useful as a tool to check the
 * accuracy of other methods of determining the Hessian.
 * <p/>
 * For more information on the Finite Difference Method see the following
 * article.
 * <p/>
 * http://en.wikipedia.org/wiki/Finite_difference_method
 */
public class HessianFD<D extends Data> extends BasicHessian<D> {

    /**
     * The initial step size for dStep.
     */
    public static final double INITIAL_STEP = 0.001;

    /**
     * The derivative step size, used for the finite difference method.
     */
    private double[] dStep;

    /**
     * The derivative coefficient, used for the finite difference method.
     */
    private double[] dCoeff;

    /**
     * The center of the point array.
     */
    private int center;

    /**
     * The number of points requested per side. This determines the accuracy of
     * the calculation.
     */
    private int pointsPerSide = 5;

    /**
     * The number of points actually used, which is (pointsPerSide*2)+1.
     */
    private int pointCount;

    /**
     * The weight count.
     */
    private int weightCount;

    /**
     * {@inheritDoc}
     */
    @Override
    public void init(final VectorNeuralNetwork theNetwork, final Dataset<D> theTraining) {

        super.init(theNetwork, theTraining);
        this.weightCount = theNetwork.getStructure().getFlat().getWeights().length;

        this.center = this.pointsPerSide + 1;
        this.pointCount = (this.pointsPerSide * 2) + 1;
        this.dCoeff = createCoefficients();
        this.dStep = new double[this.weightCount];

        for (int i = 0; i < this.weightCount; i++) {
            this.dStep[i] = HessianFD.INITIAL_STEP;
        }

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void compute() {
        this.sse = 0;

        for (int i = 0; i < network.getOutputCount(); i++) {
            internalCompute(i);
        }
    }

    private void internalCompute(final int outputNeuron) {
        double e;

        int row = 0;
        final ErrorCalculation error = new ErrorCalculation();

        final double[] derivative = new double[weightCount];

        // Loop over every training element
        for (final DataCase<D> pair : this.training) {
            EngineArray.fill(derivative, 0);
            final VectorData networkOutput = this.network.compute(pair.getInput());

            e = pair.getIdeal().getData(outputNeuron)
                    - networkOutput.getData(outputNeuron);
            error.updateError(networkOutput.getData(outputNeuron), pair
                    .getIdeal().getData(outputNeuron));

            int currentWeight = 0;

            // loop over the output weights
            final int outputFeedCount = network
                    .getLayerTotalNeuronCount(network.getLayerCount() - 2);
            for (int i = 0; i < this.network.getOutputCount(); i++) {
                for (int j = 0; j < outputFeedCount; j++) {
                    double jc;

                    if (i == outputNeuron) {
                        jc = computeDerivative(pair.getInput(), outputNeuron,
                                currentWeight, this.dStep,
                                networkOutput.getData(outputNeuron), row);
                    } else {
                        jc = 0;
                    }

                    this.gradients[currentWeight] += jc * e;
                    derivative[currentWeight] = jc;
                    currentWeight++;
                }
            }

            // Loop over every weight in the neural network
            while (currentWeight < this.network.getFlat().getWeights().length) {
                final double jc = computeDerivative(pair.getInput(),
                        outputNeuron, currentWeight, this.dStep,
                        networkOutput.getData(outputNeuron), row);
                derivative[currentWeight] = jc;
                this.gradients[currentWeight] += jc * e;
                currentWeight++;
            }

            row++;
            updateHessian(derivative);
        }

        sse += error.calculateESS();
    }

    /**
     * Computes the derivative of the output of the neural network with respect
     * to a weight.
     *
     * @param inputData     The input data to the neural network.
     * @param weight        The weight.
     * @param stepSize      The step size.
     * @param networkOutput The output from the neural network.
     * @param row           The training row currently being processed.
     * @return The derivative output.
     */
    private double computeDerivative(final D inputData,
                                     final int outputNeuron, final int weight, final double[] stepSize,
                                     final double networkOutput, final int row) {

        final double temp = this.network.getFlat().getWeights()[weight];

        final double[] points = new double[this.dCoeff.length];

        stepSize[row] = Math.max(HessianFD.INITIAL_STEP * Math.abs(temp),
                INITIAL_STEP);

        points[this.center] = networkOutput;

        for (int i = 0; i < this.dCoeff.length; i++) {
            if (i == this.center)
                continue;

            final double newWeight = temp + ((i - this.center)) * stepSize[row];

            this.network.getFlat().getWeights()[weight] = newWeight;

            final Data output = this.network.compute(inputData);
            points[i] = output.getData(outputNeuron);
        }

        double result = 0.0;
        for (int i = 0; i < this.dCoeff.length; i++) {
            result += this.dCoeff[i] * points[i];
        }

        result /= Math.pow(stepSize[row], 1);

        this.network.getFlat().getWeights()[weight] = temp;

        return result;
    }

    /**
     * Compute finite difference coefficients according to the method provided
     * here:
     * <p/>
     * http://en.wikipedia.org/wiki/Finite_difference_coefficients
     *
     * @return An array of the coefficients for FD.
     */
    public double[] createCoefficients() {

        final double[] result = new double[this.pointCount];

        final SimpleRealMatrix delts = new SimpleRealMatrix(this.pointCount, this.pointCount);
        final double[][] t = delts.getData();

        for (int j = 0; j < this.pointCount; j++) {
            final double delt = (j - this.center);
            double x = 1.0;

            for (int k = 0; k < this.pointCount; k++) {
                t[j][k] = x / EncogMath.factorial(k);
                x *= delt;
            }
        }

        final SimpleRealMatrix invMatrix = delts.inverse();
        final double f = EncogMath.factorial(this.pointCount);

        for (int k = 0; k < this.pointCount; k++) {
            result[k] = (Math.round(invMatrix.getData()[1][k] * f)) / f;
        }

        return result;
    }

    /**
     * @return The number of points per side.
     */
    public int getPointsPerSide() {
        return pointsPerSide;
    }

    /**
     * This specifies the number of points per side, default is 5. Must be
     * called before init.
     *
     * @param pointsPerSide The number of points per side.
     */
    public void setPointsPerSide(final int pointsPerSide) {
        this.pointsPerSide = pointsPerSide;
    }

}
