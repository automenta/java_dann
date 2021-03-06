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
package syncleus.dann.data;

import syncleus.dann.math.cluster.CentroidFactory;

/**
 * A training case. Training data is stored in two ways, depending on if the data is for
 * supervised, or unsupervised training.
 * <p/>
 * For unsupervised training just an input value is provided, and the ideal
 * output values are null.
 * <p/>
 * For supervised training both input and the expected ideal outputs are
 * provided.
 * <p/>
 * This interface abstracts classes that provide a holder for both of these two
 * data items.
 *
 * @author jheaton
 */
public interface DataCase<D extends Data> extends CentroidFactory<DataCase<D>> {


    /**
     * @return True if this training pair is supervised. That is, it has both
     * input and ideal data.
     */
    boolean isSupervised();

    /**
     * @return The ideal data that the neural network should produce for the
     * specified input.
     */
    D getIdeal();

    /**
     * @return The input that the neural network
     */
    D getInput();

    
    default double[] getInputArray() {
        return getInput().getData();
    }
    default double[] getIdealArray() {
        return getIdeal().getData();
    }
    
    /**
     * Get the significance, 1.0 is neutral.
     *
     * @return The significance.
     */
    double getSignificance();

//
//    /**
//     * Set the ideal data, the desired output.
//     *
//     * @param data The ideal data.
//     */
//    void setIdealArray(double[] data);
//
//    /**
//     * Set the input.
//     *
//     * @param data The input.
//     */
//    void setInputArray(double[] data);
    
//    /**
//     * Set the significance, 1.0 is neutral.
//     *
//     * @param s The significance.
//     */
//    void setSignificance(double s);

}
