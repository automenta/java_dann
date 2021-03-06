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
package syncleus.dann.search.pathfinding;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import syncleus.dann.graph.BidirectedGraph;
import syncleus.dann.graph.Graph;
import syncleus.dann.graph.ImmutableWeightedDirectedEdge;
import syncleus.dann.graph.MutableDirectedAdjacencyGraph;
import syncleus.dann.graph.SimpleWeightedDirectedEdge;
import syncleus.dann.graph.Weighted;
import syncleus.dann.graph.WeightedDirectedEdge;

public class JohnsonGraphTransformer<N> implements
        GraphTransformer<BidirectedGraph<N, ? extends WeightedDirectedEdge<N>>> {
    private static final Object BLANK_NODE = new Object();

    private boolean containsInfinite(final Graph<N, ?> original) {
        return original.getEdges().stream().anyMatch((edge) -> (edge instanceof Weighted
                && Double.isInfinite(((Weighted) edge).getWeight())));
    }

    @SuppressWarnings("unchecked")
    @Override
    public BidirectedGraph<N, WeightedDirectedEdge<N>> transform(
            final BidirectedGraph<N, ? extends WeightedDirectedEdge<N>> original) {
        if (original == null)
            throw new IllegalArgumentException("original can not be null");
        if (containsInfinite(original))
            throw new IllegalArgumentException(
                    "original can not contain infinite weights");
        final Set<WeightedDirectedEdge<Object>> originalEdges = new HashSet<>();
        original.getEdges().stream().forEach((originalEdge) -> originalEdges.add((WeightedDirectedEdge<Object>) originalEdge));
        final MutableDirectedAdjacencyGraph<Object, WeightedDirectedEdge<Object>> copyGraph = new MutableDirectedAdjacencyGraph<>(
                new HashSet<>(original.getNodes()), originalEdges);
        final Set<Object> originalNodes = copyGraph.getNodes();
        copyGraph.add(BLANK_NODE);
        originalNodes.stream().forEach((originalNode) -> copyGraph.add(new ImmutableWeightedDirectedEdge<>(BLANK_NODE,
                originalNode, 0.0)));
        final BellmanFordPathFinder<Object, WeightedDirectedEdge<Object>> pathFinder = new BellmanFordPathFinder<>(
                copyGraph);
        final MutableDirectedAdjacencyGraph johnsonGraph = new MutableDirectedAdjacencyGraph(
                original.getNodes(), new HashSet<>(
                original.getEdges()));
        final List<WeightedDirectedEdge<N>> edges = new ArrayList<WeightedDirectedEdge<N>>(
                johnsonGraph.getEdges());
        edges.stream().forEach((edge) -> {
            final double newWeight = edge.getWeight()
                    + getPathWeight(pathFinder.getBestPath(BLANK_NODE,
                    edge.getSourceNode(), false))
                    - getPathWeight(pathFinder.getBestPath(BLANK_NODE,
                    edge.getDestinationNode(), false));
            johnsonGraph.remove(edge);
            johnsonGraph.add(new SimpleWeightedDirectedEdge<>(edge
                    .getSourceNode(), edge.getDestinationNode(), newWeight));
        });

        return johnsonGraph;
    }

    private static double getPathWeight(
            final List<WeightedDirectedEdge<Object>> path) {
        double weight = 0.0;
        weight = path.stream().map(Weighted::getWeight).reduce(weight, (accumulator, _item) -> accumulator + _item);
        return weight;
    }
}
