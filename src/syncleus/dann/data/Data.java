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
 * Defines an array of real number data. This is an array of double values that could be
 * used either for input data, actual output data or ideal output data.
 * <p/>
 * TODO rename MutableData
 *
 * @author jheaton
 */
public interface Data extends Cloneable, CentroidFactory<Data>, ImplicitData {

    /**
     * Add a value to the specified index.
     *
     * @param index The index to add to.
     * @param value The value to add.
     */
    void add(int index, double value);

    /**
     * Clear any data to zero.
     */
    void clear();

    /**
     * Clone this object.
     *
     * @return A cloned version of this object.
     */
    Data clone();

    
    

    /**
     * Set all of the data as an array of doubles.
     *
     * @param data An array of doubles.
     */
    void setData(double[] data);

    /**
     * Set the specified element.
     *
     * @param index The index to set.
     * @param d     The data for the specified element.
     */
    void setData(int index, double d);



}
