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
package syncleus.dann.data.vector;

import java.io.Serializable;
import syncleus.dann.data.Data;
import syncleus.dann.data.MutableData;
import syncleus.dann.math.cluster.Centroid;

/**
 * Basic implementation of the MLData interface that stores the data in an
 * array.
 *
 * @author jheaton
 */
public class VectorData implements MutableData, Serializable, Cloneable {

    /**
     * The serial id.
     */
    private static final long serialVersionUID = -3644304891793584603L;

    /**
     * The data held by this object.
     */
    private double[] data;

//    /**
//     * Construct this object with the specified data.
//     *
//     * @param d The data to construct this object with.
//     */
//    public VectorData(final double[] d) {
//        this(d.length);
//        System.arraycopy(d, 0, this.data, 0, d.length);
//    }
    
    /** Wraps an array in VectorData without copy */
    public VectorData(final double[] d) {
        this.data = d;
    }    
    
    public VectorData(final Data d) {
        this(d.getData());
    }

    /**
     * Construct this object with blank data and a specified size.
     *
     * @param size The amount of data to store.
     */
    public VectorData(final int size) {
        this.data = new double[size];
    }

    /**
     * Construct a new BasicMLData object from an existing one. This makes a
     * copy of an array.
     *
     * @param d The object to be copied.
     */
    public VectorData(final MutableData d) {
        this(d.size());
        System.arraycopy(d.getData(), 0, this.data, 0, d.size());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void add(final int index, final double value) {
        this.data[index] += value;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void clear() {
        for (int i = 0; i < this.data.length; i++) {
            this.data[i] = 0;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public VectorData clone() {
        return new VectorData(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double[] getData() {
        return this.data;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double getData(final int index) {
        return this.data[index];
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setData(final double[] theData) {
        this.data = theData;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setData(final int index, final double d) {
        this.data[index] = d;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int size() {
        return this.data.length;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder("[");
        builder.append(this.getClass().getSimpleName());
        builder.append(':');
        for (int i = 0; i < this.data.length; i++) {
            if (i != 0) {
                builder.append(',');
            }
            builder.append(this.data[i]);
        }
        builder.append(']');
        return builder.toString();
    }


    
    
    /**
     * {@inheritDoc}
     */
    @Override
    public Centroid<VectorData> createCentroid() {
        return new VectorCentroid(this);
    }

    /**
     * Add one data element to another. This does not modify the object.
     *
     * @param o The other data element
     * @return The result.
     */
    public MutableData plus(final MutableData o) {
        if (size() != o.size())
            throw new IllegalArgumentException();

        final VectorData result = new VectorData(size());
        for (int i = 0; i < size(); i++)
            result.setData(i, getData(i) + o.getData(i));

        return result;
    }

    /**
     * Multiply one data element with another. This does not modify the object.
     *
     * @param d The other data element
     * @return The result.
     */
    public MutableData times(final double d) {
        final MutableData result = new VectorData(size());

        for (int i = 0; i < size(); i++)
            result.setData(i, getData(i) * d);

        return result;
    }

    /**
     * Subtract one data element from another. This does not modify the object.
     *
     * @param o The other data element
     * @return The result.
     */
    public MutableData minus(final MutableData o) {
        if (size() != o.size())
            throw new IllegalArgumentException();

        final MutableData result = new VectorData(size());
        for (int i = 0; i < size(); i++)
            result.setData(i, getData(i) - o.getData(i));

        return result;
    }
    
    
}
