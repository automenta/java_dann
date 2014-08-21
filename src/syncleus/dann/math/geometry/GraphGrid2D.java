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
package syncleus.dann.math.geometry;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;
import syncleus.dann.graph.AbstractBidirectedAdjacencyGraph;
import syncleus.dann.graph.BidirectedEdge;
import syncleus.dann.graph.ImmutableUndirectedEdge;
import syncleus.dann.graph.Weighted;
import syncleus.dann.math.geometry.GraphGrid2D.GridNode;

public class GraphGrid2D extends
        AbstractBidirectedAdjacencyGraph<GridNode, BidirectedEdge<GridNode>> {
    
    public static class GridNode extends Vector2i implements Weighted {
        private final double weight;
        private static final long serialVersionUID = 3733460419806813102L;

        public GridNode(final int x, final int y, final double weight) {
            super(x, y);
            this.weight = weight;
        }

        @Override
        public double getWeight() {
            return this.weight;
        }


    }
    
    private final GridNode[][] nodes;
    private final Set<GridNode> nodeSet = new HashSet<>();
    private final Set<BidirectedEdge<GridNode>> edges = new HashSet<>();
    private final Map<GridNode, Set<BidirectedEdge<GridNode>>> neighborEdges = new HashMap<>();
    private final Map<GridNode, Set<GridNode>> neighborNodes = new HashMap<>();
    private static final long serialVersionUID = -5202664944476006671L;

    public GraphGrid2D(final double[][] nodeWeights) {
        this.nodes = new GridNode[nodeWeights.length][nodeWeights[0].length];
        // construct nodes
        for (int y = 0; y < nodeWeights.length; y++)
            for (int x = 0; x < nodeWeights[0].length; x++) {
                this.nodes[y][x] = new GridNode(x, y, nodeWeights[y][x]);
                this.nodeSet.add(this.nodes[y][x]);
                this.neighborEdges.put(this.nodes[y][x],
                        new HashSet<>());
                this.neighborNodes.put(this.nodes[y][x],
                        new HashSet<>());
            }
        // connect nodes
        for (int y = 0; y < nodes.length; y++)
            for (int x = 0; x < this.nodes[0].length; x++) {
                // connect to the right
                if (x < this.nodes[0].length - 1) {
                    final ImmutableUndirectedEdge<GridNode> newEdge = new ImmutableUndirectedEdge<>(
                            this.nodes[y][x], this.nodes[y][x + 1]);
                    this.edges.add(newEdge);
                    this.neighborEdges.get(this.nodes[y][x]).add(newEdge);
                    this.neighborEdges.get(this.nodes[y][x + 1]).add(newEdge);
                    this.neighborNodes.get(this.nodes[y][x]).add(
                            this.nodes[y][x + 1]);
                    this.neighborNodes.get(this.nodes[y][x + 1]).add(
                            this.nodes[y][x]);
                }
                // connect to the bottom
                if (y < nodes.length - 1) {
                    final ImmutableUndirectedEdge<GridNode> newEdge = new ImmutableUndirectedEdge<>(
                            this.nodes[y][x], this.nodes[y + 1][x]);
                    this.edges.add(newEdge);
                    this.neighborEdges.get(this.nodes[y][x]).add(newEdge);
                    this.neighborEdges.get(this.nodes[y + 1][x]).add(newEdge);
                    this.neighborNodes.get(this.nodes[y][x]).add(
                            this.nodes[y + 1][x]);
                    this.neighborNodes.get(this.nodes[y + 1][x]).add(
                            this.nodes[y][x]);
                }
            }
    }

    public GridNode getNode(final int x, final int y) {
        if ((x >= this.nodes[0].length) || (y >= nodes.length))
            throw new IllegalArgumentException("coordinates are out of bounds");
        return this.nodes[y][x];
    }

    @Override
    public Set<GridNode> getNodes() {
        return Collections.unmodifiableSet(this.nodeSet);
    }

    @Override
    public Set<BidirectedEdge<GridNode>> getEdges() {
        return Collections.unmodifiableSet(this.edges);
    }

    @Override
    public Set<BidirectedEdge<GridNode>> getAdjacentEdges(final GridNode node) {
        return Collections.unmodifiableSet(this.neighborEdges.get(node));
    }

    @Override
    public Set<BidirectedEdge<GridNode>> getTraversableEdges(final GridNode node) {
        return this.getAdjacentEdges(node);
    }

    public Set<BidirectedEdge<GridNode>> getOutEdges(final GridNode node) {
        return this.getAdjacentEdges(node);
    }

    @Override
    public Set<BidirectedEdge<GridNode>> getInEdges(final GridNode node) {
        return this.getAdjacentEdges(node);
    }

    public int getIndegree(final GridNode node) {
        return this.getInEdges(node).size();
    }

    public int getOutdegree(final GridNode node) {
        return this.getOutEdges(node).size();
    }

    public boolean isStronglyConnected(final GridNode leftNode,
                                       final GridNode rightNode) {
        return this.neighborNodes.get(leftNode).contains(rightNode);
    }

    @Override
    public List<GridNode> getAdjacentNodes(final GridNode node) {
        return Collections.unmodifiableList(new ArrayList<>(
                this.neighborNodes.get(node)));
    }

    @Override
    public List<GridNode> getTraversableNodes(final GridNode node) {
        return this.getAdjacentNodes(node);
    }

    @Override
    public Stream<BidirectedEdge<GridNode>> streamTraversableEdges(final GridNode node) {
        return getTraversableEdges(node).stream();
    }

    @Override
    public Stream<BidirectedEdge<GridNode>> streamAdjacentEdges(final GridNode node) {
        return getAdjacentEdges(node).stream();
    }

    @Override
    public AbstractBidirectedAdjacencyGraph<GridNode, BidirectedEdge<GridNode>> clone() {
        return super.clone(); //To change body of generated methods, choose Tools | Templates.
    }

}
