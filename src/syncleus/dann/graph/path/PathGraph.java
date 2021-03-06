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

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;
import syncleus.dann.graph.DirectedGraph;
import syncleus.dann.graph.Graph;
import syncleus.dann.graph.WeightedDirectedEdge;

/**
 * Graph with an API optimized for pathfinding/searching
 */
public class PathGraph implements DirectedGraph<PathNode, WeightedDirectedEdge<PathNode>> {

    private final Set<PathNode> nodes = new HashSet<>();
    private final PathNode root;

    public PathGraph(final PathNode rootNode) {
        this.root = rootNode;
        nodes.add(rootNode);
    }

    @Override
    public Set<PathNode> getNodes() {
        return this.nodes;
    }

    /**
     * @return the root
     */
    public PathNode getRoot() {
        return root;
    }

    public PathNode connect(final PathNode baseNode, final PathNode newNode,
                            final double cost) {
        this.nodes.add(newNode);
        baseNode.connect(newNode, cost);
        return newNode;
    }

    @Override
    public Set<WeightedDirectedEdge<PathNode>> getInEdges(final PathNode node) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Stream<PathNode> streamNodes() {
        return nodes.stream();
    }

    @Override
    public Stream<PathNode> streamAdjacentNodes(PathNode node) {
        // TODO Auto-generated method stub
        return null;
    }


    @Override
    public Stream<WeightedDirectedEdge<PathNode>> streamEdges() {
        return nodes.stream().flatMap(p -> p.getConnections().stream());
    }

    @Override
    public List<PathNode> getAdjacentNodes(final PathNode node) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public List<PathNode> getTraversableNodes(final PathNode node) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Graph<PathNode, WeightedDirectedEdge<PathNode>> cloneAdd(final WeightedDirectedEdge<PathNode> newEdge) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Graph<PathNode, WeightedDirectedEdge<PathNode>> cloneAdd(final PathNode newNode) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Graph<PathNode, WeightedDirectedEdge<PathNode>> cloneAdd(final Set<PathNode> newNodes, final Set<WeightedDirectedEdge<PathNode>> newEdges) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Graph<PathNode, WeightedDirectedEdge<PathNode>> cloneRemove(final WeightedDirectedEdge<PathNode> edgeToRemove) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Graph<PathNode, WeightedDirectedEdge<PathNode>> cloneRemove(final PathNode nodeToRemove) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Graph<PathNode, WeightedDirectedEdge<PathNode>> cloneRemove(final Set<PathNode> deleteNodes, final Set<WeightedDirectedEdge<PathNode>> deleteEdges) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Graph<PathNode, WeightedDirectedEdge<PathNode>> clone() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean isContextEnabled() {
        throw new UnsupportedOperationException("Not supported yet.");
    }


}
