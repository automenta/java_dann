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

import syncleus.dann.Clustering;
import syncleus.dann.data.DataCase;
import syncleus.dann.data.DataCluster;
import syncleus.dann.data.Dataset;
import syncleus.dann.data.vector.VectorCluster;
import syncleus.dann.math.VectorDistance;

import java.util.ArrayList;
import java.util.List;
import syncleus.dann.data.Data;

/**
 * This class performs a basic K-Means clustering. This class can be used on
 * either supervised or unsupervised data. For supervised data, the ideal values
 * will be ignored.
 * <p/>
 * http://en.wikipedia.org/wiki/Kmeans
 */
public class KMeansClustering<D extends Data> implements Clustering<D> {

    /**
     * The kmeans utility.
     */
    private final KMeansUtil<D> kmeans;

    /**
     * The clusters
     */
    private DataCluster[] clusters;

    /**
     * Number of clusters.
     */
    private final int k;

    /**
     * Construct the K-Means object.
     *
     * @param theK   The number of clusters to use.
     * @param theSet The dataset to cluster.
     */
    public KMeansClustering(final int theK, final Dataset<D> theSet, VectorDistance distance) {
        final List<DataCase<D>> list = new ArrayList<>();
        for (final DataCase<D> pair : theSet) {
            list.add(pair);
        }
        this.k = theK;
        this.kmeans = new KMeansUtil(this.k, list, distance);
    }

    /**
     * Perform a single training iteration.
     */
    @Override
    public final void iteration() {
        this.kmeans.process();
        this.clusters = new DataCluster[this.k];
        for (int i = 0; i < this.k; i++) {
            this.clusters[i] = new VectorCluster(this.kmeans.getCluster(i));
        }

    }

    /**
     * The number of iterations to perform.
     *
     * @param count The count of iterations.
     */
    @Override
    public final void iteration(final int count) {
        for (int i = 0; i < count; i++) {
            iteration();
        }
    }

    /**
     * @return The clusters.
     */
    @Override
    public DataCluster[] getClusters() {
        return this.clusters;
    }

    /**
     * @return The number of clusters.
     */
    @Override
    public int numClusters() {
        return this.k;
    }

}
