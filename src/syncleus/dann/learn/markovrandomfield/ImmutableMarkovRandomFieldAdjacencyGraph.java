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
package syncleus.dann.learn.markovrandomfield;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import syncleus.dann.graph.UndirectedEdge;
import syncleus.dann.learn.graphical.GraphicalModelNode;

public class ImmutableMarkovRandomFieldAdjacencyGraph<N extends GraphicalModelNode, E extends UndirectedEdge<N>>
        extends AbstractMarkovRandomFieldAdjacencyGraph<N, E> {
    private static final long serialVersionUID = -2944480631125238463L;

    public ImmutableMarkovRandomFieldAdjacencyGraph() {
        super();
    }

    // TODO we cant copy because right now each bayesian node is hard linked to
    // a parent graph, this should be fixed
    /*
     * public ImmutableMarkovRandomFieldAdjacencyGraph(final Graph<N, E>
	 * copyGraph) { super(copyGraph.getNodes(), copyGraph.getEdges()); }
	 */

    public ImmutableMarkovRandomFieldAdjacencyGraph(final Set<N> nodes,
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
    public ImmutableMarkovRandomFieldAdjacencyGraph<N, E> cloneAdd(
            final E newEdge) {
        return (ImmutableMarkovRandomFieldAdjacencyGraph<N, E>) super
                .cloneAdd(newEdge);
    }

    @Override
    public ImmutableMarkovRandomFieldAdjacencyGraph<N, E> cloneAdd(
            final N newNode) {
        return (ImmutableMarkovRandomFieldAdjacencyGraph<N, E>) super
                .cloneAdd(newNode);
    }

    @Override
    public ImmutableMarkovRandomFieldAdjacencyGraph<N, E> cloneAdd(
            final Set<N> newNodes, final Set<E> newEdges) {
        return (ImmutableMarkovRandomFieldAdjacencyGraph<N, E>) super.cloneAdd(
                newNodes, newEdges);
    }

    @Override
    public ImmutableMarkovRandomFieldAdjacencyGraph<N, E> cloneRemove(
            final E edgeToRemove) {
        return (ImmutableMarkovRandomFieldAdjacencyGraph<N, E>) super
                .cloneRemove(edgeToRemove);
    }

    @Override
    public ImmutableMarkovRandomFieldAdjacencyGraph<N, E> cloneRemove(
            final N nodeToRemove) {
        return (ImmutableMarkovRandomFieldAdjacencyGraph<N, E>) super
                .cloneRemove(nodeToRemove);
    }

    @Override
    public ImmutableMarkovRandomFieldAdjacencyGraph<N, E> cloneRemove(
            final Set<N> deleteNodes, final Set<E> deleteEdges) {
        return (ImmutableMarkovRandomFieldAdjacencyGraph<N, E>) super
                .cloneRemove(deleteNodes, deleteEdges);
    }

    @Override
    public ImmutableMarkovRandomFieldAdjacencyGraph<N, E> clone() {
        return (ImmutableMarkovRandomFieldAdjacencyGraph<N, E>) super.clone();
    }
}
