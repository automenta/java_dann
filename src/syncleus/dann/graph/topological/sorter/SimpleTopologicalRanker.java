/******************************************************************************
 *                                                                             *
 *  Copyright: (c) Syncleus, Inc.                                              *
 *                                                                             *
 *  You may redistribute and modify this source code under the terms and       *
 *  conditions of the Open Source Community License - Type C version 1.0       *
 *  or any later version as published by Syncleus, Inc. at www.syncleus.com.   *
 *  There should be a copy of the license included with this file. If a copy   *
 *  of the license is not included you are granted no right to distribute or   *
 *  otherwise use this file except through a legal and valid license. You      *
 *  should also contact Syncleus, Inc. at the information below if you cannot  *
 *  find a license:                                                            *
 *                                                                             *
 *  Syncleus, Inc.                                                             *
 *  2604 South 12th Street                                                     *
 *  Philadelphia, PA 19148                                                     *
 *                                                                             *
 ******************************************************************************/
package syncleus.dann.graph.topological.sorter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import syncleus.dann.graph.BidirectedGraph;
import syncleus.dann.graph.DirectedEdge;

public class SimpleTopologicalRanker<N> implements TopologicalRanker<N> {
    @Override
    public List<Set<N>> rank(
            final BidirectedGraph<? extends N, ? extends DirectedEdge<? extends N>> graph) {
        // initialize data structures
        final Set<N> remainingNodes = new HashSet<>(graph.getNodes());
        final Set<DirectedEdge<? extends N>> remainingEdges = new HashSet<>(
                graph.getEdges());
        final Map<N, Set<DirectedEdge<? extends N>>> remainingNeighborEdges = new HashMap<>();

        // construct the remainingNeighborEdges with the entire graphs adjacency
        for (final DirectedEdge<? extends N> edge : remainingEdges) {
            for (final N edgeNode : edge.getNodes()) {
                if (!remainingNodes.contains(edgeNode))
                    throw new IllegalArgumentException(
                            "A node that is an end point in one of the edges was not in the nodes list");
                java.util.Set<syncleus.dann.graph.DirectedEdge<? extends N>> startNeighborEdges = remainingNeighborEdges
                        .get(edgeNode);
                if (startNeighborEdges == null) {
                    startNeighborEdges = new java.util.HashSet<>();
                    remainingNeighborEdges.put(edgeNode, startNeighborEdges);
                }
                startNeighborEdges.add(edge);
            }
        }

        // pull all nodes of 0 degree then delete as long as nodes are left
        final List<Set<N>> topologicalNodes = new ArrayList<>();
        while (!remainingNodes.isEmpty()) {
            // find all nodes current with a in degree of 0
            final Set<N> currentRootNodes = new HashSet<>();
            remainingNodes.stream().filter((node) -> (getIndegree(remainingEdges, node) == 0)).forEach(currentRootNodes::add);

            // if no nodes were found yet some are still remaining then this
            // cant be sorted
            if (currentRootNodes.isEmpty())
                return null;

            currentRootNodes.stream().map((node) -> {
                final Set<DirectedEdge<? extends N>> neighbors = remainingNeighborEdges
                        .get(node);
                neighbors.stream().map((neighbor) -> {
                    final List<N> adjacentNodes = new ArrayList<>(
                            neighbor.getNodes());
                    adjacentNodes.remove(node);
                    final N adjacentNode = adjacentNodes.get(0);
                    final Set<DirectedEdge<? extends N>> deleteFromEdges = remainingNeighborEdges
                            .get(adjacentNode);
                    deleteFromEdges.remove(neighbor);
                    return neighbor;
                }).forEach(remainingEdges::remove);
                return node;
            }).forEach(remainingNodes::remove);

            // lets add the current root nodes and continue
            topologicalNodes.add(currentRootNodes);
        }

        return topologicalNodes;
    }

    @Override
    public List<N> sort(
            final BidirectedGraph<? extends N, ? extends DirectedEdge<? extends N>> graph) {
        final List<Set<N>> rankedNodes = this.rank(graph);

        // convert ranked nodes into sorted nodes
        final List<N> sortedNodes = new ArrayList<>(graph.getNodes().size());
        rankedNodes.stream().forEach(sortedNodes::addAll);

        return sortedNodes;
    }

    private int getIndegree(final Set<DirectedEdge<? extends N>> edges,
                            final N node) {
        int inDegree = 0;
        inDegree = edges.stream().filter((edge) -> (edge.getDestinationNode() == node)).map((_item) -> 1).reduce(inDegree, Integer::sum);
        return inDegree;
    }
}
