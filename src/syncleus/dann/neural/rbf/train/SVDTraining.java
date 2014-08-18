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
package syncleus.dann.neural.rbf.train;

import syncleus.dann.data.Dataset;
import syncleus.dann.math.ObjectPair;
import syncleus.dann.math.rbf.RadialBasisFunction;

/**
 * Train a RBF neural network using a SVD.
 * <p/>
 * Contributed to Encog By M.Fletcher and M.Dean University of Cambridge, Dept.
 * of Physics, UK
 */
public class SVDTraining extends BasicTraining {

    /**
     * The network that is to be trained.
     */
    private final RBFNetwork network;

    /**
     * Construct the training object.
     *
     * @param network  The network to train. Must have a single output neuron.
     * @param training The training data to use. Must be indexable.
     */
    public SVDTraining(final RBFNetwork network, final Dataset training) {
        super(TrainingImplementationType.OnePass);
        if (network.getOutputCount() != 1) {
            throw new TrainingError(
                    "SVD requires an output layer with a single neuron.");
        }

        setTraining(training);
        this.network = network;
    }

    @Override
    public boolean canContinue() {
        return false;
    }

    public static void flatToMatrix(final double[] flat, final int start,
                                    final double[][] matrix) {
        final int rows = matrix.length;
        final int cols = matrix[0].length;

        int index = start;

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                matrix[r][c] = flat[index++];
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public RBFNetwork getMethod() {
        return this.network;
    }

    /**
     * Perform one iteration.
     */
    @Override
    public void iteration() {
        final int length = this.network.getRBF().length;

        final RadialBasisFunction[] funcs = new RadialBasisFunction[length];

        // Iteration over neurons and determine the necessaries
        System.arraycopy(this.network.getRBF(), 0, funcs, 0, length);

        final ObjectPair<double[][], double[][]> data = TrainingSetUtil
                .trainingToArray(getTraining());

        final double[][] matrix = new double[length][this.network
                .getOutputCount()];

        flatToMatrix(this.network.getFlat().getWeights(), 0, matrix);
        setError(SVD.svdfit(data.getA(), data.getB(), matrix, funcs));
        matrixToFlat(matrix, this.network.getFlat().getWeights(), 0);
    }

    /**
     * Convert the matrix to flat.
     *
     * @param matrix The matrix.
     * @param flat   Flat array.
     * @param start  WHere to start.
     */
    public static void matrixToFlat(final double[][] matrix, final double[] flat,
                                    final int start) {
        final int rows = matrix.length;
        final int cols = matrix[0].length;

        int index = start;

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                flat[index++] = matrix[r][c];
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TrainingContinuation pause() {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void resume(final TrainingContinuation state) {

    }

}
