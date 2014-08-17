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

import java.util.ArrayList;
import syncleus.dann.data.DataCluster;
import syncleus.dann.math.cluster.Centroid;

import java.util.List;

/**
 * Holds a cluster of MLData items that have been clustered by the
 * KMeansClustering class.
 */
public class VectorCluster extends org.apache.commons.math3.ml.clustering.Cluster<VectorData> implements DataCluster<VectorData> {

    /**
     * The centroid.
     */
    public VectorCentroid centroid;

    /**
     * The contents of the cluster.
     */
    public final List<VectorData> data;
    

    /**
     * Construct a cluster from another.
     *
     * @param cluster The other cluster.
     */
    public VectorCluster(final VectorCluster cluster) {
        this.centroid = cluster.centroid.clone();
        this.data = new ArrayList(cluster.data.size());
        for (VectorData v : cluster.data) {
            data.add(v.clone());
        }
    }

    public VectorCluster(final VectorDataset s) {
        this.centroid = s.createCentroid();
        data = new ArrayList(1);
        s.getData().stream().forEach((pair) -> addPoint(pair.getInput()));
    }
    
    public VectorCluster(final VectorData v) {
        this.centroid = new VectorCentroid(v);
        data = new ArrayList(1);
        addPoint(v);
    }    
    
    public VectorCluster(int dimensions) {
        this.centroid = new VectorCentroid(dimensions);
        data = new ArrayList();
    }        

    /**
     * Add to the cluster.
     *
     * @param point The pair to add.
     */
    @Override
    public final void addPoint(final VectorData point) {
        this.data.add(point);
        centroid.add(point);
    }

    /**
     * Create a dataset from the clustered data.
     *
     * @return The dataset.
     */
    @Override
    public final VectorDataset createDataSet() {
        final VectorDataset result = new VectorDataset();

        this.data.stream().forEach(result::add);

        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final VectorData get(final int pos) {
        return this.data.get(pos);
    }

    /**
     * @return The centroid.
     */
    @Override
    public final Centroid<VectorData> getCentroid() {
        return this.centroid;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final List<VectorData> getPoints() {
        return this.data;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final void removePoint(final VectorData point) {
        this.data.remove(point);
        centroid.remove(point);
    }
    
    @Override
    public final void removePoint(int point) {
        VectorData removed = this.data.remove(point);
        if (removed!=null)
            centroid.remove(removed);
    }

    /**
     * Set the centroid.
     *
     * @param c The new centroid.
     */
    public final void setCentroid(final VectorCentroid c) {
        this.centroid = c;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final int size() {
        return this.data.size();
    }

    @Override
    public String toString() {
        return "[centroid=" + centroid.toString() + ", points={" + data.toString() + "}]";
    }

    
}
