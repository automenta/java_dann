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

import java.util.List;
import syncleus.dann.math.cluster.Centroid;

/**
 * Defines a cluster. Usually used with the MLClustering method to break input
 * into clusters.
 */
public interface DataCluster<D extends Data>  {

    /**
     * Add data to this cluster.
     *
     * @param pair The data to add.
     */
    void addPoint(final D pair);

    /**
     * Create a machine learning dataset from the data.
     *
     * @return A dataset.
     */
    Dataset<D> createDataSet();

    /**
     * Get the specified data item by index.
     *
     * @param pos The index of the data item to get.
     * @return The data item.
     */
    D get(final int pos);

    /**
     * @return The data in this cluster.
     */
    List<D> getPoints();

    /**
     * Remove the specified item.
     *
     * @param data The item to remove.
     */
    void removePoint(final D data);
    
    void removePoint(final int pointIndex);

    /**
     * @return The number of items.
     */
    int size();
    
    Centroid<D> getCentroid();
}
