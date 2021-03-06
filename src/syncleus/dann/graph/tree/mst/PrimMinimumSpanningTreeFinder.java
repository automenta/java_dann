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
package syncleus.dann.graph.tree.mst;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;
import syncleus.dann.graph.BidirectedGraph;
import syncleus.dann.graph.DirectedEdge;
import syncleus.dann.graph.Edge;
import syncleus.dann.graph.Graph;
import syncleus.dann.graph.Weighted;
import syncleus.dann.graph.topological.sorter.SimpleTopologicalRanker;
import syncleus.dann.graph.topological.sorter.TopologicalSorter;

public class PrimMinimumSpanningTreeFinder<N, E extends Edge<N>> implements
        RootedMinimumSpanningTreeFinder<N, E> {
    @SuppressWarnings("unchecked")
    @Override
    public Set<E> findMinimumSpanningTree(final Graph<N, E> graph) {
        boolean isDirected = false;
        if (graph instanceof BidirectedGraph) {
            isDirected = true;
            for (final E edge : graph.getEdges())
                if (!(edge instanceof DirectedEdge))
                    isDirected = false;
        }

        final N startNode;
        if (isDirected) {
            final TopologicalSorter<N> sorter = new SimpleTopologicalRanker<>();
            final List<N> sortedNodes = sorter.sort((BidirectedGraph) graph);
            startNode = sortedNodes.get(0);
        } else {
            startNode = graph.getNodes().iterator().next();
        }

        return this.primCalculate(graph, startNode);
    }

    @Override
    public Set<E> findMinimumSpanningTree(final Graph<N, E> graph,
                                          final N startNode) {
        return primCalculate(graph, startNode);
    }

    private Set<E> primCalculate(final Graph<N, E> graph, final N startNode) {
        final Set<E> mst = new HashSet<>();
        final PrimMap<N, E> primMap = new PrimMap<>();
        graph.getNodes().stream().forEach((node) -> primMap.put(node, null));

        N currentNode = null;
        while (!primMap.isEmpty()) {
            if (currentNode == null) {
                primMap.remove(startNode);
                currentNode = startNode;
            } else {
                final Entry<N, E> currentEntry = primMap.pop();
                currentNode = currentEntry.getKey();
                mst.add(currentEntry.getValue());
            }

            final Set<E> neighborEdges = graph.getTraversableEdges(currentNode);
            for (final E neighborEdge : neighborEdges) {
                final List<N> neighborNodes = new ArrayList<>(
                        neighborEdge.getNodes());
                // remove all occurrences of currentNode, not just the first
                while (neighborNodes.remove(currentNode)) {
                }
                neighborNodes.stream().filter((neighborNode) -> (primMap.containsKey(neighborNode)
                        && primMap.isLess(neighborNode, neighborEdge))).map((neighborNode) -> {
                    primMap.put(neighborNode, neighborEdge);
                    return neighborNode;
                }).forEach((_item) -> primMap.resort());
            }
        }
        return mst;
    }

    private static class PrimMap<N, E> extends HashMap<N, E> {
        private static final long serialVersionUID = 6345120112273301259L;
        private final Queue<Map.Entry<N, E>> weightedNodes = new PriorityQueue<>(
                10, new EntryCompare());

        public void resort() {
            weightedNodes.clear();
            this.entrySet().stream().forEach(weightedNodes::add);
        }

        public double getWeight(final N node) {
            final E edge = this.get(node);
            return edgeToWeight(edge);
        }

        public boolean isLess(final N node, final E edge) {
            if (edge == null)
                throw new IllegalArgumentException("edge can not be null");
            return edgeToWeight(edge) < getWeight(node);
        }

        private double edgeToWeight(final E edge) {
            if (edge == null)
                return Double.MAX_VALUE;
            if (edge instanceof Weighted)
                return ((Weighted) edge).getWeight();
            else
                return 0;
        }

        public Map.Entry<N, E> pop() {
            final Map.Entry<N, E> poped = weightedNodes.poll();
            if (poped != null)
                this.remove(poped.getKey());
            return poped;
        }

        @Override
        public Object clone() {
            return super.clone(); //To change body of generated methods, choose Tools | Templates.
        }

        private class EntryCompare implements Comparator<Map.Entry<N, E>>,
                Serializable {
            private static final long serialVersionUID = -4356537864223227850L;

            @Override
            public int compare(final Map.Entry<N, E> first,
                               final Map.Entry<N, E> second) {
                double firstWeight = 0;
                if (first.getValue() == null)
                    firstWeight = Double.MAX_VALUE;
                else if (first.getValue() instanceof Weighted)
                    firstWeight = ((Weighted) first.getValue()).getWeight();
                double secondWeight = 0;
                if (second.getValue() == null)
                    secondWeight = Double.MAX_VALUE;
                if (second.getValue() instanceof Weighted)
                    secondWeight = ((Weighted) second.getValue()).getWeight();
                if (firstWeight < secondWeight)
                    return -1;
                else if (firstWeight > secondWeight)
                    return 1;
                else
                    return 0;
            }
        }
    }
}
