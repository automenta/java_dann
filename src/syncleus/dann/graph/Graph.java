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

import syncleus.dann.graph.context.ContextReporter;

import java.io.Serializable;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

// TODO consider making all nodes extend from a connectable interface so you can embed other graphs as nodes if they too are connectable.

/**
 * Represents a graph as a collection of nodes connected by edges. A graph does
 * not need to contain any nodes or edges however if there is at least one edge
 * then there must be at least one node. There can, however, be one or more
 * nodes with no edges present. Each edge must have 2 or more nodes it connects,
 * however they do not need to be different nodes. The implementation defines if
 * and how a graph can be traversed across nodes and edges.
 *
 * @param <N> The node type
 * @param <E> The type of edge for the given node type
 * @author Jeffrey Phillips Freeman
 * @since 2.0
 */
public interface Graph<N, E extends Edge<N>> extends ImplicitGraph<N, E>, Serializable, Cloneable,
        ContextReporter {


    /**
     * Get a set of all nodes in the graph.
     *
     * @return An unmodifiable set of all nodes in the graph.
     * @since 2.0
     */
    default Set<N> getNodes() {
        return streamNodes().collect(Collectors.toSet());
    }

    /**
     * Get a set of all edges in the graph. Two edges in the set, and in the
     * graph, may have the same end points unless equals in the edges used by
     * this graph restrict that possibility.
     *
     * @return An unmodifiable set of a all edges in the graph.
     * @since 2.0
     */
    // Set<E> getEdges();
    default Set<E> getEdges() {
        return streamEdges().collect(Collectors.toSet());
    }

    /**
     * Get a list of all nodes adjacent to the specified node. All edges
     * connected to this node has its other end points added to the list
     * returned. The specified node itself will appear in the list once for
     * every loop. If there are multiple edges connecting node with a particular
     * end point it will appear multiple times in the list, once for each hop to
     * the end point.
     *
     * @param node The whose neighbors are to be returned.
     * @return A list of all nodes adjacent to the specified node, empty set if
     * the node has no edges.
     * @since 2.0
     */
    default List<N> getAdjacentNodes(N node) {
        return streamAdjacentNodes(node).collect(Collectors.toList());
    }


    /**
     * Get a list of all reachable nodes adjacent to node. All edges connected
     * to node and is traversable from node will have its destination node(s)
     * added to the returned list. node itself will appear in the list once for
     * every loop. If there are multiple edges connecting node with a particular
     * end point then the end point will appear multiple times in the list, once
     * for each hop to the end point.
     *
     * @param node The whose traversable neighbors are to be returned.
     * @return A list of all nodes adjacent to the specified node and
     * traversable from the spevified node, empty set if the node has no
     * edges.
     * @since 2.0
     */
    List<N> getTraversableNodes(N node);

    /**
     * Get a set of all edges which you can traverse from node. Of course node
     * will always be an end point for each edge returned. Throws an
     * IllegalArgumentException if node is not in the graph.
     *
     * @param node edges returned will be traversable from this node.
     * @return An unmodifiable set of all edges that can be traversed from node.
     * @since 2.0
     */
    default Set<E> getTraversableEdges(final N node) {
        return streamTraversableEdges(node).collect(Collectors.toSet());
    }

    default public Stream<E> streamTraversableEdges(final N node) {
        return streamEdges().filter(E -> E.isTraversable(node));
    }

    /**
     * Adds the specified edge to a clone of this class.
     *
     * @param newEdge the edge to add to the cloned graph.
     * @return a clone of this graph with the specified edge added to it. null
     * if the edge already exists.
     * @since 2.0
     */
    Graph<N, E> cloneAdd(E newEdge);

    /**
     * Adds the specified node to a clone of this class.
     *
     * @param newNode the node to add to the cloned graph.
     * @return a clone of this graph with the specified node added to it.
     * @since 2.0
     */
    Graph<N, E> cloneAdd(N newNode);

    /**
     * Adds the specified nodes and edges to a clone of this class.
     *
     * @param newNodes the nodes to add to the cloned graph.
     * @param newEdges the edges to add to the cloned graph.
     * @return a clone of this graph with the specified nodes and edges added to
     * it.
     * @since 2.0
     */
    Graph<N, E> cloneAdd(Set<N> newNodes, Set<E> newEdges);

    /**
     * Removed the specified edge from a clone of this class.
     *
     * @param edgeToRemove the edge to remove from the cloned graph.
     * @return a clone of this graph with the specified edge removed to it.
     * @since 2.0
     */
    Graph<N, E> cloneRemove(E edgeToRemove);

    /**
     * Removed the specified edge to a clone of this class.
     *
     * @param nodeToRemove the edge to remove from the cloned graph.
     * @return a clone of this graph with the specified edge removed from it.
     * @since 2.0
     */
    Graph<N, E> cloneRemove(N nodeToRemove);

    /**
     * Removed the specified nodes and edges from a clone of this class.
     *
     * @param deleteNodes the nodes to remove from the cloned graph.
     * @param deleteEdges the edges to remove from the cloned graph.
     * @return a clone of this graph with the specified nodes and edges removed
     * from it.
     * @since 2.0
     */
    Graph<N, E> cloneRemove(Set<N> deleteNodes, Set<E> deleteEdges);

    Graph<N, E> clone();
}