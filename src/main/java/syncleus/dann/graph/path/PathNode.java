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
package syncleus.dann.graph.path;

import java.util.ArrayList;
import java.util.List;

import syncleus.dann.graph.SimpleWeightedDirectedEdge;

public class PathNode {
    private final List<SimpleWeightedDirectedEdge<PathNode>> connections = new ArrayList<>();
    private final List<SimpleWeightedDirectedEdge<PathNode>> backConnections = new ArrayList<>();
    private final String label;

    public PathNode(final String label) {
        super();
        this.label = label;
    }

    public List<SimpleWeightedDirectedEdge<PathNode>> getConnections() {
        return connections;
    }

    public String getLabel() {
        return label;
    }

    public void connect(final PathNode newNode, final double cost) {
        SimpleWeightedDirectedEdge<PathNode> edge;
        this.connections.add(edge = new SimpleWeightedDirectedEdge<>(this, newNode, cost));
        newNode.getBackConnections().add(edge);
    }

    /**
     * @return the backConnections
     */
    public List<SimpleWeightedDirectedEdge<PathNode>> getBackConnections() {
        return backConnections;
    }

    @Override
    public String toString() {
        final StringBuilder result = new StringBuilder();
        result.append("[BasicNode: ");
        result.append(this.label);
        result.append(']');
        return result.toString();
    }

    public double getCost(final PathNode node) {
        for (final SimpleWeightedDirectedEdge<PathNode> edge : this.connections) {
            if (edge.getDestinationNode().equals(node)) {
                return edge.getWeight();
            }
        }

        throw new RuntimeException("Nodes are not connected");
    }

}
