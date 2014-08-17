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

import syncleus.dann.data.DataCase;
import syncleus.dann.math.VectorDistance;
import syncleus.dann.math.cluster.Centroid;

/**
 * A centroid for BasicMLDataPair.
 */
public class VectorCaseCentroid implements Centroid<DataCase<VectorData>>, Cloneable {
    /**
     * The value the centroid is based on.
     */
    private final VectorData value;

    /**
     * Construct the centroid.
     *
     * @param o The pair to base the centroid on.
     */
    public VectorCaseCentroid(final VectorCase o) {
        this.value = (VectorData) o.getInput().clone();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void remove(final DataCase<VectorData> d) {
        final double[] a = d.getInputArray();

        for (int i = 0; i < value.size(); i++)
            value.setData(i, ((value.getData(i) * value.size()) - a[i])
                    / (value.size() - 1));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double distance(final DataCase<VectorData> d, VectorDistance dist) {
        return dist.distance(value, d.getInput());
        
//        final Data diff = value.minus(d.getInput());
//        double sum = 0.;
//
//        for (int i = 0; i < diff.size(); i++)
//            sum += diff.getData(i) * diff.getData(i);
//
//        return Math.sqrt(sum);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void add(final DataCase<VectorData> d) {
        final double[] a = d.getInputArray();

        for (int i = 0; i < value.size(); i++)
            value.setData(i, ((value.getData(i) * value.size()) + a[i])
                    / (value.size() + 1));
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone(); //To change body of generated methods, choose Tools | Templates.
    }
}
