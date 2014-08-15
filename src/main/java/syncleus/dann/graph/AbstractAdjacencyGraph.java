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

import org.apache.logging.log4j.Logger;
import syncleus.dann.graph.context.ContextGraphElement;
import syncleus.dann.util.UnexpectedDannError;

import java.util.*;
import java.util.stream.Stream;

import static java.util.Collections.unmodifiableList;
import static java.util.Collections.unmodifiableSet;
import static org.apache.logging.log4j.LogManager.getLogger;

/**
 * An AbstractAdjacencyGraph is a Graph implemented using adjacency lists.
 *
 * @param <N> The node type
 * @param <E> The type of edge for the given node type
 * @since 2.0
 */
public abstract class AbstractAdjacencyGraph<N, E extends Edge<N>> implements
        Graph<N, E> {
    private static final Logger LOGGER = getLogger(AbstractAdjacencyGraph.class);
    private Set<E> edges;
    private Map<N, Set<E>> adjacentEdges = new HashMap<>();
    private Map<N, List<N>> adjacentNodes = new HashMap<>();
    private final boolean contextEnabled;

    /**
     * Creates a new AbstractAdjacencyGraph with no edges and no adjacencies.
     * nodeContext and edgeContext is enabled.
     */
    protected AbstractAdjacencyGraph() {
        this(true);
    }

    /**
     * Creates a new AbstractAdjacencyGraph with no edges and no adjacencies.
     */
    protected AbstractAdjacencyGraph(final boolean contextEnabled) {
        this.edges = new HashSet<>();
        this.contextEnabled = contextEnabled;
    }

    /**
     * Creates a new AbstractAdjacencyGraph as a copy of the current Graph.
     * nodeContext is enabled.
     *
     * @param copyGraph The Graph to copy
     */
    protected AbstractAdjacencyGraph(final Graph<N, E> copyGraph) {
        this(copyGraph.getNodes(), copyGraph.getEdges(), true);
    }

    /**
     * Creates a new AbstractAdjacencyGraph as a copy of the current Graph.
     *
     * @param copyGraph The Graph to copy
     */
    protected AbstractAdjacencyGraph(final Graph<N, E> copyGraph,
                                     final boolean contextEnabled) {
        this(copyGraph.getNodes(), copyGraph.getEdges(), contextEnabled);
    }

    /**
     * Creates a new AbstractAdjacencyGraph from the given list of nodes, and
     * the given list of ourEdges. The adjacency lists are created from this
     * structure. nodeContext is enabled.
     *
     * @param nodes The set of all nodes
     * @param edges The set of all ourEdges
     */
    protected AbstractAdjacencyGraph(final Set<N> nodes, final Set<E> edges) {
        this(nodes, edges, true);
    }

    /**
     * Creates a new AbstractAdjacencyGraph from the given list of nodes, and
     * the given list of ourEdges. The adjacency lists are created from this
     * structure.
     *
     * @param attemptNodes The set of all nodes
     * @param attemptEdges The set of all ourEdges
     */
    protected AbstractAdjacencyGraph(final Set<N> attemptNodes,
                                     final Set<E> attemptEdges, final boolean contextEnabled) {
        if (attemptNodes == null)
            throw new IllegalArgumentException("attemptNodes can not be null");
        if (attemptEdges == null)
            throw new IllegalArgumentException("attemptEdges can not be null");
        // make sure all the edges only connect to contained nodes
        for (final E attemptEdge : attemptEdges)
            if (!attemptNodes.containsAll(attemptEdge.getNodes()))
                throw new IllegalArgumentException(
                        "A node that is an end point in one of the attemptEdges was not in the nodes list");

        this.contextEnabled = contextEnabled;

        attemptNodes.stream().filter((attemptNode) -> !(this.contextEnabled
                && (attemptNode instanceof ContextGraphElement)
                && !((ContextGraphElement) attemptNode).joiningGraph(this))).map((attemptNode) -> {
            this.adjacentNodes.put(attemptNode, new ArrayList<>());
            return attemptNode;
        }).forEach((attemptNode) -> this.adjacentEdges.put(attemptNode, new HashSet<>()));

        // Add the edges checking for Edge Context.
        if (this.contextEnabled) {
            this.edges = new HashSet<>(attemptEdges.size());
            for (final E attemptEdge : attemptEdges) {
                // lets see if this ContextEdge will allow itself to join the
                // graph
                if (this.contextEnabled
                        && (attemptEdge instanceof ContextGraphElement)
                        && !((ContextGraphElement) attemptEdge)
                        .joiningGraph(this))
                    continue;

                this.edges.add(attemptEdge);
                // populate adjacency maps
                for (final N currentNode : attemptEdge.getNodes()) {
                    boolean passedCurrent = false;
                    for (final N neighborNode : attemptEdge.getNodes()) {
                        if (!passedCurrent && (neighborNode == currentNode)) {
                            passedCurrent = true;
                            continue;
                        }

                        // this is a neighbor node
                        if (!this.adjacentNodes.containsKey(currentNode))
                            throw new IllegalStateException(
                                    "After edges and nodes have applied their context restrictions an edge remained connected to a node not in this graph");

                        this.adjacentNodes.get(currentNode).add(neighborNode);
                    }

                    // this is a neighbor edge
                    if (!this.adjacentEdges.containsKey(currentNode))
                        throw new IllegalStateException(
                                "After edges and nodes have applied their context restrictions an edge remained connected to a node not in this graph");
                    this.adjacentEdges.get(currentNode).add(attemptEdge);
                }
            }
        } else {
            this.edges = new HashSet<>(attemptEdges);
        }
    }

    /**
     * Gets the internal edges of the list.
     *
     * @return The set of internal edges
     */
    protected Set<E> getInternalEdges() {
        return this.edges;
    }

    /**
     * Gets the map of nodes to their associated set of edges.
     *
     * @return The internal adjacency edges to nodes
     */
    protected Map<N, Set<E>> getInternalAdjacencyEdges() {
        return this.adjacentEdges;
    }

    /**
     * Gets each node's list of adjacent nodes.
     *
     * @return The map of nodes to adjacent nodes
     */
    protected Map<N, List<N>> getInternalAdjacencyNodes() {
        return this.adjacentNodes;
    }

    @Override
    public boolean isContextEnabled() {
        return this.contextEnabled;
    }

    /**
     * Gets all nodes in the map.
     *
     * @return The unmodifiable set of nodes
     */
    @Override
    public Set<N> getNodes() {
        return unmodifiableSet(this.adjacentEdges.keySet());
    }

    /**
     * Gets all edges in the map.
     *
     * @return The unmodifiable set of edges
     */
    @Override
    public Set<E> getEdges() {
        return unmodifiableSet(this.edges);
    }

    @Override
    public Stream<E> streamEdges() {
        return edges.stream();
    }

    @Override
    public Stream<N> streamNodes() {
        return adjacentNodes.keySet().stream();
    }

    @Override
    public Stream<E> streamAdjacentEdges(final N node) {
        return getAdjacentEdges(node).stream();
    }

    @Override
    public Stream<E> streamTraversableEdges(final N node) {
        return getTraversableEdges(node).stream();
    }

    /**
     * Gets all edges adjacent to a given node.
     *
     * @param node the end point for all edges to retrieve.
     * @return The edges adjacent to that node.
     */
    @Override
    public Set<E> getAdjacentEdges(final N node) {
        if (this.adjacentEdges.containsKey(node))
            return unmodifiableSet(this.adjacentEdges.get(node));
        else
            return Collections.<E>emptySet();
    }

    public boolean contains(final N node) {
        return adjacentNodes.keySet().contains(node);
    }

    /**
     * Gets all nodes adjacent to the given node.
     *
     * @param node The whose neighbors are to be returned.
     * @return All adjacent nodes to the given node
     */
    @Override
    public List<N> getAdjacentNodes(final N node) {
        return unmodifiableList(new ArrayList<>(this.adjacentNodes
                .get(node)));
    }

    @Override
    public Stream<N> streamAdjacentNodes(final N node) {
        return getAdjacentNodes(node).stream();
    }


    /**
     * Gets the traversable nodes adjacent to the given node.
     *
     * @param node The whose traversable neighbors are to be returned.
     * @return The traversable nodes adjacent to the given node
     * @see com.syncleus.dann.graph.Edge#getTraversableNodes(Object)
     */
    @Override
    public List<N> getTraversableNodes(final N node) {
        final List<N> traversableNodes = new ArrayList<>();
        this.getAdjacentEdges(node).stream().forEach((adjacentEdge) -> traversableNodes.addAll(adjacentEdge.getTraversableNodes(node)));
        return unmodifiableList(traversableNodes);
    }

    /**
     * Gets the traversable edges from this node.
     *
     * @param node edges returned will be traversable from this node.
     * @return The traversable edges from the given node
     * @see com.syncleus.dann.graph.Edge#isTraversable(Object)
     */
    @Override
    public Set<E> getTraversableEdges(final N node) {
        final Set<E> traversableEdges = new HashSet<>();
        this.getAdjacentEdges(node).stream().filter((adjacentEdge) -> (adjacentEdge.isTraversable(node))).forEach(traversableEdges::add);
        return unmodifiableSet(traversableEdges);
    }

    /**
     * Adds the given edge to a clone of this object. Returns null if the given
     * edge could not be added.
     *
     * @param newEdge the edge to add to the cloned graph.
     * @return A clone, with the given edge added
     */
    @Override
    public AbstractAdjacencyGraph<N, E> cloneAdd(final E newEdge) {
        if (newEdge == null)
            throw new IllegalArgumentException("newEdge can not be null");
        if (!this.getNodes().containsAll(newEdge.getNodes()))
            throw new IllegalArgumentException(
                    "newEdge has a node as an end point that is not part of the graph");

        final Set<E> newEdges = new HashSet<>(this.edges);
        if (newEdges.add(newEdge)) {
            final Map<N, Set<E>> newAdjacentEdges = new HashMap<>();
            this.adjacentEdges
                    .entrySet().stream().forEach((neighborEdgeEntry) -> newAdjacentEdges.put(neighborEdgeEntry.getKey(),
                    new HashSet<>(neighborEdgeEntry.getValue())));
            final Map<N, List<N>> newAdjacentNodes = new HashMap<>();
            this.adjacentNodes
                    .entrySet().stream().forEach((neighborNodeEntry) -> newAdjacentNodes.put(neighborNodeEntry.getKey(),
                    new ArrayList<>(neighborNodeEntry.getValue())));
            newEdge.getNodes().stream().map((currentNode) -> {
                newAdjacentEdges.get(currentNode).add(newEdge);
                return currentNode;
            }).forEach((currentNode) -> {
                final List<N> currentAdjacentNodes = new ArrayList<>(
                        newEdge.getNodes());
                currentAdjacentNodes.remove(currentNode);
                currentAdjacentNodes.stream().forEach((currentAdjacentNode) -> newAdjacentNodes.get(currentNode).add(currentAdjacentNode));
            });

            final AbstractAdjacencyGraph<N, E> copy = this.clone();
            copy.edges = newEdges;
            copy.adjacentEdges = newAdjacentEdges;
            copy.adjacentNodes = newAdjacentNodes;
            return copy;
        }

        return null;
    }

    /**
     * Creates a clone of this graph with the given node added. NOTE: Returns
     * null.
     *
     * @param newNode the node to add to the cloned graph.
     * @return null
     */
    @Override
    public AbstractAdjacencyGraph<N, E> cloneAdd(final N newNode) {
        // TODO Implement this method
        return null;
    }

    @Override
    public AbstractAdjacencyGraph<N, E> cloneAdd(final Set<N> newNodes,
                                                 final Set<E> newEdges) {
        // TODO Implement this method
        return null;
    }

    @Override
    public AbstractAdjacencyGraph<N, E> cloneRemove(final E edgeToRemove) {
        // TODO Implement this method
        return null;
    }

    @Override
    public AbstractAdjacencyGraph<N, E> cloneRemove(final N nodeToRemove) {
        // TODO Implement this method
        return null;
    }

    @Override
    public AbstractAdjacencyGraph<N, E> cloneRemove(final Set<N> deleteNodes,
                                                    final Set<E> deleteEdges) {
        // TODO Implement this method
        return null;
    }

    /**
     * Clones the current object.
     *
     * @return A clone of the current object, with no changes
     */
    @Override
    public AbstractAdjacencyGraph<N, E> clone() {
        try {
            final AbstractAdjacencyGraph<N, E> cloneGraph = (AbstractAdjacencyGraph<N, E>) super
                    .clone();

            // lets instantiate some new data structures for our clone
            cloneGraph.adjacentEdges = new HashMap<>();
            cloneGraph.adjacentNodes = new HashMap<>();

            this.getNodes().stream().filter((attemptNode) -> !(this.contextEnabled
                    && (attemptNode instanceof ContextGraphElement)
                    && !((ContextGraphElement) attemptNode)
                    .joiningGraph(cloneGraph))).map((attemptNode) -> {
                cloneGraph.adjacentNodes.put(attemptNode, new ArrayList<>());
                return attemptNode;
            }).forEach((attemptNode) -> cloneGraph.adjacentEdges.put(attemptNode, new HashSet<>()));

            // Add the edges checking for Edge Context.
            if (this.contextEnabled) {
                cloneGraph.edges = new HashSet<>(this.getEdges().size());
                for (final E attemptEdge : this.getEdges()) {
                    // lets see if this ContextEdge will allow itself to join
                    // the graph
                    if (this.contextEnabled
                            && (attemptEdge instanceof ContextGraphElement)
                            && !((ContextGraphElement) attemptEdge)
                            .joiningGraph(cloneGraph))
                        continue;

                    cloneGraph.edges.add(attemptEdge);
                    // populate adjacency maps
                    for (final N currentNode : attemptEdge.getNodes()) {
                        boolean passedCurrent = false;
                        for (final N neighborNode : attemptEdge.getNodes()) {
                            if (!passedCurrent && (neighborNode == currentNode)) {
                                passedCurrent = true;
                                continue;
                            }

                            // this is a neighbor node
                            if (!cloneGraph.adjacentNodes
                                    .containsKey(currentNode))
                                throw new IllegalStateException(
                                        "After edges and nodes have applied their context restrictions an edge remained connected to a node not in this graph");

                            cloneGraph.adjacentNodes.get(currentNode).add(
                                    neighborNode);
                        }

                        // this is a neighbor edge
                        if (!cloneGraph.adjacentEdges.containsKey(currentNode))
                            throw new IllegalStateException(
                                    "After edges and nodes have applied their context restrictions an edge remained connected to a node not in this graph");
                        cloneGraph.adjacentEdges.get(currentNode).add(
                                attemptEdge);
                    }
                }
            } else {
                cloneGraph.edges = new HashSet<>(this.getEdges());
            }

            return cloneGraph;
        } catch (final CloneNotSupportedException caught) {
            LOGGER.error("Unexpectedly could not clone Graph.", caught);
            throw new UnexpectedDannError("Unexpectedly could not clone graph",
                    caught);
        }
    }

}
