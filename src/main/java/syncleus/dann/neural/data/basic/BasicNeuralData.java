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
package org.encog.neural.data.basic;

import syncleus.dann.data.basic.BasicMLData;
import syncleus.dann.learn.ml.MLData;
import syncleus.dann.neural.data.NeuralData;

/**
 * This is an alias class for Encog 2.5 compatibility. This class aliases
 * BasicMLData. Newer code should use BasicMLData in place of this class.
 */
public class BasicNeuralData extends BasicMLData {

    /**
     *
     */
    private static final long serialVersionUID = 1524371205985251772L;

    /**
     * Construct from a double array.
     *
     * @param d A double array.
     */
    public BasicNeuralData(final double[] d) {
        super(d);
    }

    /**
     * Construct to a specific size.
     *
     * @param size The size to use.
     */
    public BasicNeuralData(final int size) {
        super(size);
    }

    /**
     * Construct from another object.
     *
     * @param d The other object.
     */
    public BasicNeuralData(final NeuralData d) {
        super(d);
    }

    @Override
    public MLData clone() {
        return super.clone(); //To change body of generated methods, choose Tools | Templates.
    }

}
