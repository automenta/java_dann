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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import syncleus.dann.graph.context.AbstractContextGraphElement;
import syncleus.dann.graph.context.ContextNode;
import syncleus.dann.util.UnexpectedDannError;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public abstract class AbstractEdge<N> extends
        AbstractContextGraphElement<Graph<N, ?>> implements Edge<N> {
    private static final Logger LOGGER = LogManager
            .getLogger(AbstractEdge.class);
    private final boolean contextEnabled;
    private List<N> nodes;

    protected AbstractEdge() {
        this(true, true);
    }

    protected AbstractEdge(final boolean allowJoiningMultipleGraphs,
                           final boolean contextEnabled) {
        super(allowJoiningMultipleGraphs);
        this.contextEnabled = contextEnabled;
    }

    protected AbstractEdge(final List<N> ourNodes) {
        this(ourNodes, true, true);
    }

    protected AbstractEdge(final List<N> ourNodes,
                           final boolean allowJoiningMultipleGraphs,
                           final boolean contextEnabled) {
        super(allowJoiningMultipleGraphs);
        this.contextEnabled = contextEnabled;

        // make sure each node with context allows us to connect to it
        if (contextEnabled) {
            final List<N> nodesCopy = new ArrayList<>(ourNodes.size());
            ourNodes.stream().filter((ourNode) -> !(this.contextEnabled && (ourNode instanceof ContextNode)
                    && (!((ContextNode) ourNode).connectingEdge(this)))).forEach(nodesCopy::add);
            this.nodes = Collections.unmodifiableList(new ArrayList<>(
                    nodesCopy));
        } else
            this.nodes = Collections
                    .unmodifiableList(new ArrayList<>(ourNodes));
    }

    protected AbstractEdge(final N... ourNodes) {
        this(true, true, ourNodes);
    }

    protected AbstractEdge(final boolean allowJoiningMultipleGraphs,
                           final boolean contextEnabled, final N... ourNodes) {
        this(Arrays.asList(ourNodes), allowJoiningMultipleGraphs,
                contextEnabled);
    }

    @Override
    public boolean isContextEnabled() {
        return this.contextEnabled;
    }

    protected AbstractEdge<N> add(final N node) {
        if (node == null)
            throw new IllegalArgumentException("node can not be null");

        final List<N> newNodes = new ArrayList<>(this.nodes);
        newNodes.add(node);

        return createDeepCopy(newNodes);
    }

    protected AbstractEdge<N> add(final List<N> addNodes) {
        if (addNodes == null)
            throw new IllegalArgumentException("node can not be null");
        final List<N> newNodes = new ArrayList<>(this.nodes);
        newNodes.addAll(addNodes);

        return createDeepCopy(newNodes);
    }

    protected AbstractEdge<N> remove(final N node) {
        if (node == null)
            throw new IllegalArgumentException("node can not be null");
        if (!this.nodes.contains(node))
            throw new IllegalArgumentException("is not an endpoint");

        final List<N> newNodes = new ArrayList<>(this.nodes);
        newNodes.remove(node);

        return createDeepCopy(newNodes);
    }

    protected AbstractEdge<N> remove(final List<N> removeNodes) {
        if (removeNodes == null)
            throw new IllegalArgumentException("removeNodes can not be null");
        if (!this.nodes.containsAll(removeNodes))
            throw new IllegalArgumentException(
                    "removeNodes do not contain all valid end points");
        final List<N> newNodes = new ArrayList<>(this.nodes);
        removeNodes.stream().forEach(newNodes::remove);

        return createDeepCopy(newNodes);
    }

    /**
     * Create a deep copy of this edge, but with a new set of nodes.
     *
     * @param newNodes the set of nodes to use instead of the current ones.
     * @return a deep copy of this edge, but with a new set of nodes.
     */
    private AbstractEdge<N> createDeepCopy(final List<N> newNodes) {
        try {
            final AbstractEdge<N> clonedEdge = (AbstractEdge<N>) super.clone();
            final List<N> clonedNodes = new ArrayList<>(this.nodes.size());
            newNodes.stream().filter((newNode) -> !(this.contextEnabled
                    && (newNode instanceof ContextNode)
                    && (!((ContextNode) newNode).connectingEdge(clonedEdge)))).forEach(clonedNodes::add);
            clonedEdge.nodes = Collections.unmodifiableList(clonedNodes);
            return clonedEdge;
        } catch (final CloneNotSupportedException caught) {
            LOGGER.error("Edge was unexpectidly not cloneable", caught);
            throw new UnexpectedDannError(
                    "Edge was unexpectidly not cloneable", caught);
        }
    }

    @Override
    public boolean isTraversable(final N node) {
        return (!this.getTraversableNodes(node).isEmpty());
    }

    @Override
    public final List<N> getNodes() {
        return this.nodes;
    }

    @Override
    public String toString() {
        final StringBuilder outString = new StringBuilder(
                this.nodes.size() * 10);
        this.nodes.stream().forEach((node) -> outString.append(':').append(node));
        return outString.toString();
    }

    @Override
    public AbstractEdge<N> clone() {
        try {
            final AbstractEdge<N> clonedEdge = (AbstractEdge<N>) super.clone();
            final List<N> clonedNodes = new ArrayList<>(this.nodes.size());
            this.nodes.stream().filter((node) -> !(this.contextEnabled && (node instanceof ContextNode)
                    && (!((ContextNode) node).connectingEdge(clonedEdge)))).forEach(clonedNodes::add);
            clonedEdge.nodes = Collections.unmodifiableList(clonedNodes);
            return clonedEdge;
        } catch (final CloneNotSupportedException caught) {
            LOGGER.error("Edge was unexpectidly not cloneable", caught);
            throw new UnexpectedDannError(
                    "Edge was unexpectidly not cloneable", caught);
        }
    }

}
