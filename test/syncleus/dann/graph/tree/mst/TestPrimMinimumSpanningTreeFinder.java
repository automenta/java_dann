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

import java.util.HashSet;
import java.util.Set;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;
import syncleus.dann.graph.BidirectedEdge;
import syncleus.dann.graph.Graph;
import syncleus.dann.graph.ImmutableAdjacencyGraph;
import syncleus.dann.graph.ImmutableTreeAdjacencyGraph;
import syncleus.dann.graph.ImmutableUndirectedEdge;
import syncleus.dann.graph.TreeGraph;
import syncleus.dann.graph.cycle.ColoredDepthFirstSearchDetector;
import syncleus.dann.graph.cycle.CycleDetector;
import syncleus.dann.graph.topological.Topography;

public class TestPrimMinimumSpanningTreeFinder {
    private static final Logger LOGGER = LogManager
            .getLogger(TestPrimMinimumSpanningTreeFinder.class);

    @Test
    public void testUndirected() {
        final Set<Object> nodes = new HashSet<>();
        final Object centerNode = "centerNode";
        nodes.add(centerNode);
        final Object topNode = "topNode";
        nodes.add(topNode);
        final Object leftNode = "leftNode";
        nodes.add(leftNode);
        final Object rightNode = "rightNode";
        nodes.add(rightNode);

        final Set<BidirectedEdge<Object>> edges = new HashSet<>();
        final BidirectedEdge<Object> centerTopEdge = new ImmutableUndirectedEdge<>(
                centerNode, topNode);
        edges.add(centerTopEdge);
        final BidirectedEdge<Object> centerLeftEdge = new ImmutableUndirectedEdge<>(
                centerNode, leftNode);
        edges.add(centerLeftEdge);
        final BidirectedEdge<Object> centerRightEdge = new ImmutableUndirectedEdge<>(
                centerNode, rightNode);
        edges.add(centerRightEdge);
        final BidirectedEdge<Object> topRightEdge = new ImmutableUndirectedEdge<>(
                topNode, rightNode);
        edges.add(topRightEdge);
        final BidirectedEdge<Object> rightLeftEdge = new ImmutableUndirectedEdge<>(
                rightNode, leftNode);
        edges.add(rightLeftEdge);
        final BidirectedEdge<Object> leftTopEdge = new ImmutableUndirectedEdge<>(
                leftNode, topNode);
        edges.add(leftTopEdge);

        final Graph<Object, BidirectedEdge<Object>> graph = new ImmutableAdjacencyGraph<>(
                nodes, edges);

        final RootedMinimumSpanningTreeFinder<Object, BidirectedEdge<Object>> finder = new PrimMinimumSpanningTreeFinder<>();
        final Set<BidirectedEdge<Object>> mstEdges = finder
                .findMinimumSpanningTree(graph);
        final TreeGraph<Object, BidirectedEdge<Object>> mst = new ImmutableTreeAdjacencyGraph<>(
                graph.getNodes(), mstEdges);

        LOGGER.info("mst edges:");
        mst.getEdges().stream().forEach(LOGGER::info);

        final CycleDetector detector = new ColoredDepthFirstSearchDetector();
        LOGGER.info("mst is cyclic: " + detector.hasCycle(mst));
        LOGGER.info("mst is connected: " + Topography.isStronglyConnected(mst));
        LOGGER.info("mst is contains all nodes: "
                + mst.getNodes().containsAll(graph.getNodes()));

        Assert.assertTrue("mst was not acyclic", !detector.hasCycle(mst));
        Assert.assertTrue("mst was not connected",
                Topography.isStronglyConnected(mst));
        Assert.assertTrue(
                "mst did not contain all the nodes of the paret graph", mst
                        .getNodes().containsAll(graph.getNodes()));
    }
}
