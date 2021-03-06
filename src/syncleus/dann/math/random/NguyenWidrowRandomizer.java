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
package syncleus.dann.math.random;

import syncleus.dann.Learning;
import syncleus.dann.data.matrix.SimpleRealMatrix;
import syncleus.dann.neural.VectorNeuralNetwork;
import syncleus.dann.neural.util.activation.EncogActivationFunction;

/**
 * Implementation of <i>Nguyen-Widrow</i> weight initialization. This is the
 * default weight initialization used by Encog, as it generally provides the
 * most train-able neural network.
 */
public class NguyenWidrowRandomizer extends BasicRandomizer {

    public static String MSG = "This type of randomization is not supported by Nguyen-Widrow";

    @Override
    public void randomize(final Learning method) {
        if (!(method instanceof VectorNeuralNetwork)) {
            throw new RuntimeException("Nguyen-Widrow only supports BasicNetwork.");
        }

        final VectorNeuralNetwork network = (VectorNeuralNetwork) method;

        for (int fromLayer = 0; fromLayer < network.getLayerCount() - 1; fromLayer++) {
            randomizeSynapse(network, fromLayer);
        }

    }

    private static double calculateRange(final EncogActivationFunction af, final double r) {
        final double[] d = {r};
        af.activate(d, 0, 1);
        return d[0];
    }

    private void randomizeSynapse(final VectorNeuralNetwork network,
                                  final int fromLayer) {
        final int toLayer = fromLayer + 1;
        final int toCount = network.getLayerNeuronCount(toLayer);
        final int fromCount = network.getLayerNeuronCount(fromLayer);
        final int fromCountTotalCount = network
                .getLayerTotalNeuronCount(fromLayer);
        final EncogActivationFunction af = network.getActivation(toLayer);
        final double low = calculateRange(af, Double.MIN_VALUE);
        final double high = calculateRange(af, Double.MAX_VALUE);

        final double b = 0.7d * Math.pow(toCount, (1d / fromCount))
                / (high - low);

        for (int toNeuron = 0; toNeuron < toCount; toNeuron++) {
            if (fromCount != fromCountTotalCount) {
                final double w = nextDouble(-b, b);
                network.setWeight(fromLayer, fromCount, toNeuron, w);
            }
            for (int fromNeuron = 0; fromNeuron < fromCount; fromNeuron++) {
                final double w = nextDouble(0, b);
                network.setWeight(fromLayer, fromNeuron, toNeuron, w);
            }
        }
    }

    @Override
    public double randomize(final double d) {
        throw new RuntimeException(MSG);
    }

    @Override
    public void randomize(final double[] d) {
        throw new RuntimeException(MSG);
    }

    @Override
    public void randomize(final double[][] d) {
        throw new RuntimeException(MSG);
    }

    @Override
    public void randomize(final SimpleRealMatrix m) {
        throw new RuntimeException(MSG);
    }

    @Override
    public void randomize(final double[] d, final int begin, final int size) {
        throw new RuntimeException(MSG);
    }
}
