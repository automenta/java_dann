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
package syncleus.dann.learn.kmeans;

import syncleus.dann.data.basic.VectorDataset;
import syncleus.dann.data.DataCluster;
import syncleus.dann.data.Data;
import syncleus.dann.data.Dataset;
import syncleus.dann.math.cluster.Centroid;

import java.util.ArrayList;
import java.util.List;
import syncleus.dann.data.DataCase;
import syncleus.dann.math.cluster.Cluster;

/**
 * Holds a cluster of MLData items that have been clustered by the
 * KMeansClustering class.
 */
public class BasicCluster<M extends Data> extends org.apache.commons.math3.ml.clustering.Cluster<M> implements DataCluster<M> {

    /**
     * The centroid.
     */
    private Centroid<DataCase<M>> centroid;

    /**
     * The contents of the cluster.
     */
    private final List<M> data = new ArrayList<>();
    

    /**
     * Construct a cluster from another.
     *
     * @param cluster The other cluster.
     */
    public BasicCluster(final Cluster<DataCase<M>> cluster) {
        this.centroid = cluster.centroid();
        cluster.getContents().stream().forEach((pair) -> this.data.add(pair.getInput()));
    }

    /**
     * Add to the cluster.
     *
     * @param pair The pair to add.
     */
    @Override
    public final void addPoint(final M pair) {
        this.data.add(pair);
    }

    /**
     * Create a dataset from the clustered data.
     *
     * @return The dataset.
     */
    @Override
    public final Dataset createDataSet() {
        final Dataset result = new VectorDataset();

        this.data.stream().forEach(result::add);

        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final Data get(final int pos) {
        return this.data.get(pos);
    }

    /**
     * @return The centroid.
     */
    public final Centroid<?> getCentroid() {
        return this.centroid;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final List<M> getPoints() {
        return this.data;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final void remove(final Data pair) {
        this.data.remove(pair);
    }

    /**
     * Set the centroid.
     *
     * @param c The new centroid.
     */
    public final void setCentroid(final Centroid<DataCase<M>> c) {
        this.centroid = c;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final int size() {
        return this.data.size();
    }

}
