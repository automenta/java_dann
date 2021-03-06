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
package syncleus.dann.graph.drawing.hyperassociativemap;

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

public class SimpleUndirectedGraph
        extends
        AbstractBidirectedAdjacencyGraph<SimpleNode, BidirectedEdge<SimpleNode>> {
    private final SimpleNode[][] nodes;
    private final Set<SimpleNode> nodeSet = new HashSet<>();
    private final Set<BidirectedEdge<SimpleNode>> edges = new HashSet<>();
    private final Map<SimpleNode, Set<BidirectedEdge<SimpleNode>>> neighborEdges = new HashMap<>();
    private final Map<SimpleNode, List<SimpleNode>> neighborNodes = new HashMap<>();
    private static final long serialVersionUID = -4096835132786083007L;

    public SimpleUndirectedGraph(final int layers, final int nodesPerLayer) {
        this.nodes = new SimpleNode[layers][nodesPerLayer];

        // construct nodes
        for (int layerIndex = 0; layerIndex < layers; layerIndex++) {
            for (int nodeIndex = 0; nodeIndex < nodesPerLayer; nodeIndex++) {
                this.nodes[layerIndex][nodeIndex] = new SimpleNode(layerIndex);
                this.nodeSet.add(this.nodes[layerIndex][nodeIndex]);
                this.neighborEdges.put(this.nodes[layerIndex][nodeIndex],
                        new HashSet<>());
                this.neighborNodes.put(this.nodes[layerIndex][nodeIndex],
                        new ArrayList<>());
            }
        }
        // connect nodes
        for (int layerIndex = 0; layerIndex < (layers - 1); layerIndex++)
            for (int nodeIndex = 0; nodeIndex < nodesPerLayer; nodeIndex++) {
                for (int nodeIndex2 = 0; nodeIndex2 < nodesPerLayer; nodeIndex2++) {
                    final ImmutableUndirectedEdge<SimpleNode> newEdge = new ImmutableUndirectedEdge<>(
                            this.nodes[layerIndex][nodeIndex],
                            this.nodes[layerIndex + 1][nodeIndex2]);
                    this.edges.add(newEdge);
                    this.neighborEdges.get(this.nodes[layerIndex][nodeIndex])
                            .add(newEdge);
                    this.neighborNodes.get(this.nodes[layerIndex][nodeIndex])
                            .add(this.nodes[layerIndex + 1][nodeIndex2]);
                    this.neighborEdges.get(
                            this.nodes[layerIndex + 1][nodeIndex2])
                            .add(newEdge);
                    this.neighborNodes.get(
                            this.nodes[layerIndex + 1][nodeIndex2]).add(
                            this.nodes[layerIndex][nodeIndex]);
                }
            }
    }

    public SimpleNode[][] getNodeInLayers() {
        return this.nodes;
    }

    public SimpleNode getNode(final int layer, final int index) {
        if ((index >= this.nodes[0].length) || (layer >= nodes.length))
            throw new IllegalArgumentException("coordinates are out of bounds");
        return this.nodes[layer][index];
    }

    @Override
    public Set<SimpleNode> getNodes() {
        return Collections.unmodifiableSet(this.nodeSet);
    }

    @Override
    public Stream<BidirectedEdge<SimpleNode>> streamEdges() {
        return this.edges.stream();
    }

    @Override
    public Stream<SimpleNode> streamNodes() {
        return this.nodeSet.stream();
    }

    @Override
    public Set<BidirectedEdge<SimpleNode>> getEdges() {
        return Collections.unmodifiableSet(this.edges);
    }

    @Override
    public Set<BidirectedEdge<SimpleNode>> getAdjacentEdges(
            final SimpleNode node) {
        return Collections.unmodifiableSet(this.neighborEdges.get(node));
    }

    @Override
    public Set<BidirectedEdge<SimpleNode>> getInEdges(final SimpleNode node) {
        return this.getAdjacentEdges(node);
    }

    public int getIndegree(final SimpleNode node) {
        return this.getInEdges(node).size();
    }

    public int getOutdegree(final SimpleNode node) {
        return this.getTraversableEdges(node).size();
    }

    @Override
    public List<SimpleNode> getAdjacentNodes(final SimpleNode node) {
        return Collections.unmodifiableList(this.neighborNodes.get(node));
    }

    @Override
    public AbstractBidirectedAdjacencyGraph<SimpleNode, BidirectedEdge<SimpleNode>> clone() {
        return super.clone(); //To change body of generated methods, choose Tools | Templates.
    }
}
