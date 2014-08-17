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

import syncleus.dann.math.cluster.CentroidFactory;
import syncleus.dann.math.cluster.Cluster;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import syncleus.dann.math.VectorDistance;

/**
 * Generic KMeans clustering object.
 *
 * @param <K> The type to cluster.
 */
public class KMeansUtil<K extends CentroidFactory<? super K>> {

    /**
     * The clusters.
     */
    private final ArrayList<Cluster<K>> clusters;

    /**
     * The number of clusters.
     */
    private final int k;
    private VectorDistance distanceFunc;

    /**
     * Construct the clusters. Call process to perform the cluster.
     *
     * @param theK        The number of clusters.
     * @param theElements The elements to cluster.
     */
    public KMeansUtil(final int theK, final List<? extends K> theElements, VectorDistance distance) {
        this.k = theK;
        clusters = new ArrayList<>(theK);
        this.distanceFunc = distance;
        
        initRandomClusters(theElements);
    }

    /**
     * Create random clusters.
     *
     * @param elements The elements to cluster.
     */
    private void initRandomClusters(final List<? extends K> elements) {

        int clusterIndex = 0;
        int elementIndex = 0;

        // first simply fill out the clusters, until we run out of clusters
        while ((elementIndex < elements.size()) && (clusterIndex < k)
                && (elements.size() - elementIndex > k - clusterIndex)) {
            final K element = elements.get(elementIndex);

            boolean added = false;

            // if this element is identical to another, add it to this cluster
            for (int i = 0; i < clusterIndex; i++) {
                final Cluster<K> cluster = clusters.get(i);

                if (cluster.centroid().distance(element, distanceFunc) == 0) {
                    cluster.add(element);
                    added = true;
                    break;
                }
            }

            if (!added) {
                clusters.add(new Cluster<>(elements.get(elementIndex)));
                clusterIndex++;
            }
            elementIndex++;
        }

        // create
        while (clusterIndex < k && elementIndex < elements.size()) {
            clusters.add(new Cluster<>(elements.get(elementIndex)));
            elementIndex++;
            clusterIndex++;
        }

        // handle case where there were not enough clusters created,
        // create empty ones.
        while (clusterIndex < k) {
            clusters.add(new Cluster<>());
            clusterIndex++;
        }

        // otherwise, handle case where there were still unassigned elements
        // add them to the nearest clusters.
        while (elementIndex < elements.size()) {
            final K element = elements.get(elementIndex);
            nearestCluster(element).add(element);
            elementIndex++;
        }

    }

    /**
     * Perform the cluster.
     */
    public void process() {

        boolean done;
        do {
            done = true;

            for (int i = 0; i < k; i++) {
                final Cluster<K> thisCluster = clusters.get(i);
                final List<K> thisElements = thisCluster.getContents();

                for (int j = 0; j < thisElements.size(); j++) {
                    final K thisElement = thisElements.get(j);

                    // don't make a cluster empty
                    if (thisCluster.centroid().distance(thisElement, distanceFunc) > 0) {
                        final Cluster<K> nearestCluster = nearestCluster(thisElement);

                        // move to nearer cluster
                        if (thisCluster != nearestCluster) {
                            nearestCluster.add(thisElement);
                            thisCluster.remove(j);
                            done = false;
                        }
                    }
                }
            }
        } while (!done);
    }

    /**
     * Find the nearest cluster to the element.
     *
     * @param element The element.
     * @return The nearest cluster.
     */
    private Cluster<K> nearestCluster(final K element) {
        double distance = Double.MAX_VALUE;
        Cluster<K> result = null;

        for (int i = 0; i < clusters.size(); i++) {
            final double thisDistance = clusters.get(i).centroid()
                    .distance(element, this.distanceFunc);

            if (distance > thisDistance) {
                distance = thisDistance;
                result = clusters.get(i);
            }
        }

        return result;
    }

    /**
     * Get a cluster by index.
     *
     * @param index The index to get.
     * @return The cluster.
     */
    public Collection<K> get(final int index) {
        return clusters.get(index).getContents();
    }

    /**
     * @return The number of clusters.
     */
    public int size() {
        return clusters.size();
    }

    /**
     * Get a cluster by index.
     *
     * @param i The index to get.
     * @return The cluster.
     */
    public Cluster<K> getCluster(final int i) {
        return this.clusters.get(i);
    }
}
