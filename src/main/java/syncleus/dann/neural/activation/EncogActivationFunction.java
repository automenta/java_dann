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

import java.io.Serializable;

/**
 * This interface allows various activation functions to be used with the neural
 * network. Activation functions are applied to the output from each layer of a
 * neural network. Activation functions scale the output into the desired range.
 * <p/>
 * Methods are provided both to process the activation function, as well as the
 * derivative of the function. Some training algorithms, particularly back
 * propagation, require that it be possible to take the derivative of the
 * activation function.
 * <p/>
 * Not all activation functions support derivatives. If you implement an
 * activation function that is not derivable then an exception should be thrown
 * inside of the derivativeFunction method implementation.
 * <p/>
 * Non-derivable activation functions are perfectly valid, they simply cannot be
 * used with every training algorithm.
 */
public interface EncogActivationFunction extends AbstractActivationFunction, Serializable, Cloneable, IterativeDerivative {


    double activate(double... activity);
    /* {
        activate(activity,0,1);
		return activity[0];
	}*/


    @Override
    @Deprecated
    default double activate(final double activity) {
        return activate(activity);
    }


    /**
     * @return The params for this activation function.
     */
    double[] getParams();

    /**
     * Set one of the params for this activation function.
     *
     * @param index The index of the param to set.
     * @param value The value to set.
     */
    void setParam(int index, double value);

    /**
     * @return The names of the parameters.
     */
    String[] getParamNames();

    /**
     * @return A cloned copy of this activation function.
     */
    public IterativeDerivative clone();


}
