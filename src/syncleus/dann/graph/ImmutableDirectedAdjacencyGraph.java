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
package syncleus.dann.graph;

import java.util.*;

public class ImmutableDirectedAdjacencyGraph<N, E extends DirectedEdge<N>>
        extends AbstractDirectedAdjacencyGraph<N, E> {
    private static final long serialVersionUID = -4588563094809496900L;

    public ImmutableDirectedAdjacencyGraph() {
        super();
    }

    public ImmutableDirectedAdjacencyGraph(final Graph<N, E> copyGraph) {
        super(copyGraph);
    }

    public ImmutableDirectedAdjacencyGraph(final Set<N> nodes,
                                           final Set<E> edges) {
        super(nodes, edges);
    }

    @Override
    protected Set<E> getInternalEdges() {
        return Collections.unmodifiableSet(super.getInternalEdges());
    }

    @Override
    protected Map<N, Set<E>> getInternalAdjacencyEdges() {
        final Map<N, Set<E>> newAdjacentEdges = new HashMap<>();
        super
                .getInternalAdjacencyEdges().entrySet().stream().forEach((neighborEdgeEntry) -> newAdjacentEdges.put(neighborEdgeEntry.getKey(), new HashSet<>(
                neighborEdgeEntry.getValue())));
        return newAdjacentEdges;
    }

    @Override
    protected Map<N, List<N>> getInternalAdjacencyNodes() {
        final Map<N, List<N>> newAdjacentNodes = new HashMap<>();
        super
                .getInternalAdjacencyNodes().entrySet().stream().forEach((neighborNodeEntry) -> newAdjacentNodes.put(neighborNodeEntry.getKey(), new ArrayList<>(
                neighborNodeEntry.getValue())));
        return newAdjacentNodes;
    }

    @Override
    public ImmutableDirectedAdjacencyGraph<N, E> cloneAdd(final E newEdge) {
        return (ImmutableDirectedAdjacencyGraph<N, E>) super.cloneAdd(newEdge);
    }

    @Override
    public ImmutableDirectedAdjacencyGraph<N, E> cloneAdd(final N newNode) {
        return (ImmutableDirectedAdjacencyGraph<N, E>) super.cloneAdd(newNode);
    }

    @Override
    public ImmutableDirectedAdjacencyGraph<N, E> cloneAdd(
            final Set<N> newNodes, final Set<E> newEdges) {
        return (ImmutableDirectedAdjacencyGraph<N, E>) super.cloneAdd(newNodes,
                newEdges);
    }

    @Override
    public ImmutableDirectedAdjacencyGraph<N, E> cloneRemove(
            final E edgeToRemove) {
        return (ImmutableDirectedAdjacencyGraph<N, E>) super
                .cloneRemove(edgeToRemove);
    }

    @Override
    public ImmutableDirectedAdjacencyGraph<N, E> cloneRemove(
            final N nodeToRemove) {
        return (ImmutableDirectedAdjacencyGraph<N, E>) super
                .cloneRemove(nodeToRemove);
    }

    @Override
    public ImmutableDirectedAdjacencyGraph<N, E> cloneRemove(
            final Set<N> deleteNodes, final Set<E> deleteEdges) {
        return (ImmutableDirectedAdjacencyGraph<N, E>) super.cloneRemove(
                deleteNodes, deleteEdges);
    }

    @Override
    public ImmutableDirectedAdjacencyGraph<N, E> clone() {
        return (ImmutableDirectedAdjacencyGraph<N, E>) super.clone();
    }
}
