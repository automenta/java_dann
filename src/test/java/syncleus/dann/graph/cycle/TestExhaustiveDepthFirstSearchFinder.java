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
 *  findCycles a license:                                                            *
 *                                                                             *
 *  Syncleus, Inc.                                                             *
 *  2604 South 12th Street                                                     *
 *  Philadelphia, PA 19148                                                     *
 *                                                                             *
 ******************************************************************************/
package syncleus.dann.graph.cycle;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;
import syncleus.dann.graph.*;

import java.util.HashSet;
import java.util.Set;

public class TestExhaustiveDepthFirstSearchFinder {
    private static final Logger LOGGER = LogManager
            .getLogger(TestExhaustiveDepthFirstSearchFinder.class);

    @Test
    public void testDirectedNoCycles() {
        final Set<Object> nodes = new HashSet<>();
        final Object topNode = "topNode";
        nodes.add(topNode);
        final Object leftNode = "leftNode";
        nodes.add(leftNode);
        final Object rightNode = "rightNode";
        nodes.add(rightNode);

        final Set<DirectedEdge<Object>> edges = new HashSet<>();
        final DirectedEdge<Object> topRightEdge = new ImmutableDirectedEdge<>(
                topNode, rightNode);
        edges.add(topRightEdge);
        final DirectedEdge<Object> rightLeftEdge = new ImmutableDirectedEdge<>(
                rightNode, leftNode);
        edges.add(rightLeftEdge);
        final DirectedEdge<Object> topLeftEdge = new ImmutableDirectedEdge<>(
                topNode, leftNode);
        edges.add(topLeftEdge);

        final BidirectedGraph<Object, DirectedEdge<Object>> graph = new ImmutableDirectedAdjacencyGraph<>(
                nodes, edges);

        final CycleFinder<Object, DirectedEdge<Object>> finder = new ExhaustiveDepthFirstSearchCycleFinder<>();
        LOGGER.info("testDirectedNoCycles cycles: ");
        finder.findCycles(graph).stream().forEach(LOGGER::info);
        Assert.assertTrue("Cycles detected when there should be none: "
                + finder.cycleCount(graph), finder.cycleCount(graph) == 0);
    }

    @Test
    public void testDirectedWithCycles() {
        final Set<Object> nodes = new HashSet<>();
        final Object tippyTopNode = "tippyTopNode";
        nodes.add(tippyTopNode);
        final Object topNode = "topNode";
        nodes.add(topNode);
        final Object leftNode = "leftNode";
        nodes.add(leftNode);
        final Object rightNode = "RightNode";
        nodes.add(rightNode);
        final Object bottomNode = "bottomNode";
        nodes.add(bottomNode);

        final Set<DirectedEdge<Object>> edges = new HashSet<>();
        final DirectedEdge<Object> bottomLeftEdge = new ImmutableDirectedEdge<>(
                bottomNode, leftNode);
        edges.add(bottomLeftEdge);
        final DirectedEdge<Object> letRightEdge = new ImmutableDirectedEdge<>(
                leftNode, rightNode);
        edges.add(letRightEdge);
        final DirectedEdge<Object> rightBottomEdge = new ImmutableDirectedEdge<>(
                rightNode, bottomNode);
        edges.add(rightBottomEdge);
        final DirectedEdge<Object> leftTopEdge = new ImmutableDirectedEdge<>(
                leftNode, topNode);
        edges.add(leftTopEdge);
        final DirectedEdge<Object> topRightEdge = new ImmutableDirectedEdge<>(
                topNode, rightNode);
        edges.add(topRightEdge);
        final DirectedEdge<Object> leftTippyTopEdge = new ImmutableDirectedEdge<>(
                leftNode, tippyTopNode);
        edges.add(leftTippyTopEdge);
        final DirectedEdge<Object> tippyTopRightEdge = new ImmutableDirectedEdge<>(
                tippyTopNode, rightNode);
        edges.add(tippyTopRightEdge);

        final BidirectedGraph<Object, DirectedEdge<Object>> graph = new ImmutableDirectedAdjacencyGraph<>(
                nodes, edges);

        final CycleFinder<Object, DirectedEdge<Object>> finder = new ExhaustiveDepthFirstSearchCycleFinder<>();
        LOGGER.info("testDirectedWithCycles cycles: ");
        finder.findCycles(graph).stream().forEach(LOGGER::info);
        Assert.assertTrue(
                "incorrect number of cycles detected. Expected 3, got: "
                        + finder.cycleCount(graph),
                finder.cycleCount(graph) == 3);
    }

    @Test
    public void testUndirectedNoCycles() {
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

        final Graph<Object, BidirectedEdge<Object>> graph = new ImmutableAdjacencyGraph<>(
                nodes, edges);

        final CycleFinder<Object, BidirectedEdge<Object>> finder = new ExhaustiveDepthFirstSearchCycleFinder<>();
        LOGGER.info("testUndirectedNoCycles cycles: ");
        finder.findCycles(graph).stream().forEach(LOGGER::info);
        Assert.assertTrue("Cycles detected when there should be none: "
                + finder.cycleCount(graph), finder.cycleCount(graph) == 0);
    }

    @Test
    public void testUndirectedWithCycles() {
        final Set<Object> nodes = new HashSet<>();
        final Object bottomNode = "bottomNode";
        nodes.add(bottomNode);
        final Object topNode = "topNode";
        nodes.add(topNode);
        final Object leftNode = "leftNode";
        nodes.add(leftNode);
        final Object rightNode = "rightNode";
        nodes.add(rightNode);

        final Set<BidirectedEdge<Object>> edges = new HashSet<>();
        final BidirectedEdge<Object> rightBottomEdge = new ImmutableUndirectedEdge<>(
                rightNode, bottomNode);
        edges.add(rightBottomEdge);
        final BidirectedEdge<Object> bottomLeftEdge = new ImmutableUndirectedEdge<>(
                bottomNode, leftNode);
        edges.add(bottomLeftEdge);
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

        final CycleFinder<Object, BidirectedEdge<Object>> finder = new ExhaustiveDepthFirstSearchCycleFinder<>();
        LOGGER.info("testUndirectedWithCycles cycles: ");
        finder.findCycles(graph).stream().forEach(LOGGER::info);
        Assert.assertTrue(
                "incorrect number of cycles detected. Expected 3, got: "
                        + finder.cycleCount(graph),
                finder.cycleCount(graph) == 3);
    }

    @Test
    public void testUndirectedWithDoubleEdgeCycles() {
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
        final BidirectedEdge<Object> centerRightEdge2 = new ImmutableUndirectedEdge<>(
                centerNode, rightNode);
        edges.add(centerRightEdge2);

        final Graph<Object, BidirectedEdge<Object>> graph = new ImmutableAdjacencyGraph<>(
                nodes, edges);

        final CycleFinder<Object, BidirectedEdge<Object>> finder = new ExhaustiveDepthFirstSearchCycleFinder<>();
        LOGGER.info("testUndirectedWithDoubleEdgeCycles cycles: ");
        finder.findCycles(graph).stream().forEach(LOGGER::info);
        Assert.assertTrue(
                "incorrect number of cycles detected. Expected 1, got: "
                        + finder.cycleCount(graph),
                finder.cycleCount(graph) == 1);
    }
}
