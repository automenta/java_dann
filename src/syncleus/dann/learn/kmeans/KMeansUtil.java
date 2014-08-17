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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import syncleus.dann.data.DataCluster;
import syncleus.dann.data.vector.VectorCluster;
import syncleus.dann.data.vector.VectorData;
import syncleus.dann.math.VectorDistance;

/**
 * Generic KMeans clustering object.
 *
 * @param <K> The type to cluster.
 */
public class KMeansUtil {

    /**
     * The clusters.
     */
    private final ArrayList<VectorCluster> clusters;

    /**
     * The number of clusters.
     */
    private final int k;
    private final VectorDistance distanceFunc;

    /**
     * Construct the clusters. Call process to perform the cluster.
     *
     * @param theK        The number of clusters.
     * @param theElements The elements to cluster.
     */
    public KMeansUtil(final int theK, final List<VectorData> theElements, VectorDistance distance) {
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
    private void initRandomClusters(final List<VectorData> elements) {

        int clusterIndex = 0;
        int elementIndex = 0;
        int vectorSize = 1; //discovered through iterating:

        // first simply fill out the clusters, until we run out of clusters
        while ((elementIndex < elements.size()) && (clusterIndex < k)
                && (elements.size() - elementIndex > k - clusterIndex)) {
            final VectorData element = elements.get(elementIndex);

            boolean added = false;

            // if this element is identical to another, add it to this cluster
            for (int i = 0; i < clusterIndex; i++) {
                final DataCluster<VectorData> cluster = clusters.get(i);

                if (cluster.getCentroid().distance(element, distanceFunc) == 0) {
                    cluster.addPoint(element);
                    vectorSize = element.size();
                    added = true;
                    break;
                }
            }

            if (!added) {
                VectorData nextElement = elements.get(elementIndex);
                clusters.add(new VectorCluster(nextElement));
                vectorSize = element.size();
                clusterIndex++;
            }
            elementIndex++;
        }

        // create
        while (clusterIndex < k && elementIndex < elements.size()) {
            clusters.add(new VectorCluster(elements.get(elementIndex)));
            vectorSize = elements.size();
            elementIndex++;
            clusterIndex++;
        }

        // handle case where there were not enough clusters created,
        // create empty ones.
        while (clusterIndex < k) {
            clusters.add(new VectorCluster(vectorSize));
            clusterIndex++;
        }

        // otherwise, handle case where there were still unassigned elements
        // add them to the nearest clusters.
        while (elementIndex < elements.size()) {
            final VectorData element = elements.get(elementIndex);
            nearestCluster(element).addPoint(element);
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
                final VectorCluster thisCluster = clusters.get(i);
                final List<VectorData> thisElements = thisCluster.getPoints();

                for (int j = 0; j < thisElements.size(); j++) {
                    final VectorData thisElement = thisElements.get(j);

                    // don't make a cluster empty
                    if (thisCluster.getCentroid().distance(thisElement, distanceFunc) > 0) {
                        final VectorCluster nearestCluster = nearestCluster(thisElement);

                        // move to nearer cluster
                        if (thisCluster != nearestCluster) {
                            nearestCluster.addPoint(thisElement);
                            thisCluster.removePoint(j);
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
    private VectorCluster nearestCluster(final VectorData element) {
        double distance = Double.MAX_VALUE;
        VectorCluster result = null;

        for (int i = 0; i < clusters.size(); i++) {
            final double thisDistance = clusters.get(i).getCentroid()
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
    public Collection<VectorData> get(final int index) {
        return clusters.get(index).getPoints();
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
    public VectorCluster getCluster(final int i) {
        return this.clusters.get(i);
    }
}
