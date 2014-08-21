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

import syncleus.dann.data.Data;
import syncleus.dann.math.cluster.Centroid;

/**
 * A basic implementation of a centroid.
 */
public class VectorCentroid implements Centroid<VectorData>, Cloneable {
    /**
     * The value this centroid is based on.
     */
    public final VectorData value;

    /**
     * The number of elements.
     */
    private int size;

    /**
     * Construct the centroid.
     *
     * @param o The object to base the centroid on.
     */
    public VectorCentroid(final VectorData o) {
        this.value = o.clone();
        this.size = 1;
    }
    public VectorCentroid(final Data o) {
        this.value = new VectorData(o.getData().clone());
        this.size = 1;
    }
    
    public VectorCentroid(int dimensions) {
        this.value = new VectorData(dimensions);
        this.size = 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void add(final VectorData d) {
        final double[] a = d.getData();

        for (int i = 0; i < value.size(); i++)
            value.setData(i, ((value.getData(i) * this.size) + a[i])
                    / (this.size + 1));
        this.size++;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void remove(final VectorData d) {
        final double[] a = d.getData();

        for (int i = 0; i < value.size(); i++)
            value.setData(i, ((value.getData(i) * this.size) - a[i])
                    / (this.size - 1));
        this.size--;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double distance(final VectorData d, VectorDistance dist) {
        return dist.distance(value, d);
//        final Data diff = value.minus(d);
//        double sum = 0.;
//
//        for (int i = 0; i < diff.size(); i++)
//            sum += diff.getData(i) * diff.getData(i);
//
//        return Math.sqrt(sum);
    }

    @Override
    public VectorCentroid clone() {        
        return new VectorCentroid(value.clone());
    }

    @Override
    public String toString() {
        return value.toString();
    }
    
    
}
