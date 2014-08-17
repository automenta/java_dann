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
package syncleus.dann.util.factory.train;

import syncleus.dann.Learning;
import syncleus.dann.Training;
import syncleus.dann.data.Dataset;
import syncleus.dann.neural.networks.VectorNeuralNetwork;
import syncleus.dann.neural.networks.ContainsFlat;

/**
 * A factory used to create SCG trainers.
 */
public class SCGFactory {

    /**
     * Create a SCG trainer.
     *
     * @param method   The method to use.
     * @param training The training data to use.
     * @param args     The arguments to use.
     * @return The newly created trainer.
     */
    public static Training create(final Learning method, final Dataset training,
                                 final String args) {

        if (!(method instanceof VectorNeuralNetwork)) {
            throw new RuntimeException(
                    "SCG training cannot be used on a method of type: "
                            + method.getClass().getName());
        }

        return new ScaledConjugateGradient((ContainsFlat) method, training);
    }
}
