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
package syncleus.dann.graph.path.search;

import syncleus.dann.graph.path.BasicPath;
import syncleus.dann.graph.path.PathGraph;
import syncleus.dann.graph.path.PathGraphSearch;
import syncleus.dann.graph.path.PathNode;

public class AStarSearch extends PathGraphSearch {

    private final CostEstimator estimator;

    public AStarSearch(final PathGraph theGraph,
                       final PathNode startingPoint, final SearchGoal theGoal,
                       final CostEstimator theEstimator) {
        super(theGraph, startingPoint, theGoal);
        this.estimator = theEstimator;
    }

    /**
     * @return the estimator
     */
    public CostEstimator getEstimator() {
        return estimator;
    }

    public double calculatePathCost(final BasicPath path) {

        double result = 0;
        PathNode lastNode = null;

        for (final PathNode node : path.getNodes()) {
            final double hc = this.estimator.estimateCost(node, getGoal());
            double stepCost = 0;

            if (lastNode != null) {
                stepCost = lastNode.getCost(node);
            }

            result += (hc + stepCost);

            lastNode = node;
        }

        return result;
    }

    @Override
    public boolean isHigherPriority(final BasicPath first,
                                    final BasicPath second) {
        final double firstCost = calculatePathCost(first);
        final double secondCost = calculatePathCost(second);
        return firstCost < secondCost;
    }

}
