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
package syncleus.dann.neural.activation;

/**
 * The bipolar sigmoid activation function is like the regular sigmoid
 * activation function, except Bipolar sigmoid activation function. TheOutput
 * range is -1 to 1 instead of the more normal 0 to 1.
 * <p/>
 * This activation is typically part of a CPPN neural network, such as
 * HyperNEAT.
 * <p/>
 * The idea for this activation function was developed by Ken Stanley, of the
 * University of Texas at Austin. http://www.cs.ucf.edu/~kstanley/
 */
public class ActivationBipolarSteepenedSigmoid implements EncogActivationFunction {

    /**
     * The serial id.
     */
    private static final long serialVersionUID = 1L;

    /**
     * {@inheritDoc}
     */
    @Override
    public void activate(final double[] d, final int start,
                         final int size) {
        for (int i = start; i < start + size; i++) {
            d[i] = (2.0 / (1.0 + Math.exp(-4.9 * d[i]))) - 1.0;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double derivative(final double b, final double a) {
        return 1;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasDerivative() {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double[] getParams() {
        return ActivationLinear.P;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setParam(final int index, final double value) {

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String[] getParamNames() {
        return ActivationLinear.N;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final IterativeDerivative clone() {
        return new ActivationBipolarSteepenedSigmoid();
    }


}
