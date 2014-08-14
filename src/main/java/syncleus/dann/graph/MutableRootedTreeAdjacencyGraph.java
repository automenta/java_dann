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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import syncleus.dann.graph.context.ContextGraphElement;

public class MutableRootedTreeAdjacencyGraph<N, E extends DirectedEdge<N>>
		extends AbstractRootedTreeAdjacencyGraph<N, E> implements
		MutableRootedTreeGraph<N, E> {
	private static final long serialVersionUID = -93813209434709L;

	public MutableRootedTreeAdjacencyGraph() {
		super();
	}

	public MutableRootedTreeAdjacencyGraph(final DirectedGraph<N, E> copyGraph) {
		super(copyGraph);
	}

	public MutableRootedTreeAdjacencyGraph(final Set<N> nodes,
			final Set<E> edges) {
		super(nodes, edges);
	}

	@Override
	public boolean add(final E newEdge) {
		if (newEdge == null)
			throw new IllegalArgumentException("newEdge can not be null");
		if (!this.getNodes().containsAll(newEdge.getNodes()))
			throw new IllegalArgumentException(
					"newEdge has a node as an end point that is not part of the graph");

		// if context is enabled lets check if it can join
		if (this.isContextEnabled() && (newEdge instanceof ContextGraphElement)
				&& !((ContextGraphElement) newEdge).joiningGraph(this))
			return false;

		if (this.getInternalEdges().add(newEdge)) {
                    newEdge.getNodes().stream().map((currentNode) -> {
                        this.getInternalAdjacencyEdges().get(currentNode).add(newEdge);
                        return currentNode;
                    }).forEach((currentNode) -> {
                        final List<N> newAdjacentNodes = new ArrayList<N>(
                                newEdge.getNodes());
                        newAdjacentNodes.remove(currentNode);
                        newAdjacentNodes.stream().forEach((newAdjacentNode) -> {
                            this.getInternalAdjacencyNodes().get(currentNode)
                                    .add(newAdjacentNode);
                        });
                    });
			return true;
		}

		return false;
	}

	@Override
	public boolean add(final N newNode) {
		if (newNode == null)
			throw new IllegalArgumentException("newNode can not be null");

		if (this.getInternalAdjacencyEdges().containsKey(newNode))
			return false;

		// if context is enabled lets check if it can join
		if (this.isContextEnabled() && (newNode instanceof ContextGraphElement)
				&& !((ContextGraphElement) newNode).joiningGraph(this))
			return false;

		this.getInternalAdjacencyEdges().put(newNode, new HashSet<E>());
		this.getInternalAdjacencyNodes().put(newNode, new ArrayList<N>());
		return true;
	}

	@Override
	public boolean remove(final E edgeToRemove) {
		if (edgeToRemove == null)
			throw new IllegalArgumentException("removeSynapse can not be null");

		if (!this.getInternalEdges().contains(edgeToRemove))
			return false;

		// if context is enabled lets check if it can join
		if (this.isContextEnabled()
				&& (edgeToRemove instanceof ContextGraphElement)
				&& !((ContextGraphElement) edgeToRemove).leavingGraph(this))
			return false;

		if (!this.getInternalEdges().remove(edgeToRemove))
			return false;

                edgeToRemove.getNodes().stream().map((removeNode) -> {
                this.getInternalAdjacencyEdges().get(removeNode)
                        .remove(edgeToRemove);
                return removeNode;
            }).forEach((removeNode) -> {
                final List<N> removeAdjacentNodes = new ArrayList<N>(
                        edgeToRemove.getNodes());
                removeAdjacentNodes.remove(removeNode);
                removeAdjacentNodes.stream().forEach((removeAdjacentNode) -> {
                    this.getInternalAdjacencyNodes().get(removeNode)
                            .remove(removeAdjacentNode);
                });
            });
		return true;
	}

	@Override
	public boolean remove(final N nodeToRemove) {
		if (nodeToRemove == null)
			throw new IllegalArgumentException("node can not be null");

		if (!this.getInternalAdjacencyEdges().containsKey(nodeToRemove))
			return false;

		// if context is enabled lets check if it can join
		if (this.isContextEnabled()
				&& (nodeToRemove instanceof ContextGraphElement)
				&& !((ContextGraphElement) nodeToRemove).leavingGraph(this))
			return false;

		final Set<E> removeEdges = this.getInternalAdjacencyEdges().get(
				nodeToRemove);

                removeEdges.stream().forEach((removeEdge) -> {
                this.remove(removeEdge);
            });

		// modify edges by removing the node to remove
		final Set<E> newEdges = new HashSet<E>();
                removeEdges.stream().map((removeEdge) -> {
                E newEdge = (E) removeEdge.disconnect(nodeToRemove);
                while ((newEdge != null)
                        && (newEdge.getNodes().contains(nodeToRemove)))
                    newEdge = (E) removeEdge.disconnect(nodeToRemove);
                return newEdge;
            }).filter((newEdge) -> (newEdge != null)).forEach((newEdge) -> {
                newEdges.add(newEdge);
            });
            newEdges.stream().forEach((newEdge) -> {
                this.add(newEdge);
            });

		// remove the node itself
		this.getInternalAdjacencyEdges().remove(nodeToRemove);
		this.getInternalAdjacencyNodes().remove(nodeToRemove);

		return true;
	}

	@Override
	public boolean clear() {
		boolean removedSomething = false;

		// first lets remove all the edges
		for (final E edge : this.getEdges()) {
			// lets just make sure we arent some how getting an we dont actually
			// own, this shouldnt be possible so its
			// an assert. This ensures that if remove() comes back false it must
			// be because the context didnt allow it.
			assert this.getInternalEdges().contains(edge);

			if (!this.remove(edge))
				throw new IllegalStateException(
						"one of the edges will not allow itself to leave this graph");

			removedSomething = true;
		}

		// now lets remove all the nodes
		for (final N node : this.getNodes()) {
			// lets just make sure we arent some how getting an we dont actually
			// own, this shouldnt be possible so its
			// an assert. This ensures that if remove() comes back false it must
			// be because the context didnt allow it.
			assert (!this.getInternalAdjacencyEdges().containsKey(node));

			if (!this.remove(node))
				throw new IllegalStateException(
						"one of the nodes will not allow itself to leave this graph");

			removedSomething = true;
		}

		return removedSomething;
	}

	@Override
	public MutableRootedTreeAdjacencyGraph<N, E> cloneAdd(final E newEdge) {
		return (MutableRootedTreeAdjacencyGraph<N, E>) super.cloneAdd(newEdge);
	}

	@Override
	public MutableRootedTreeAdjacencyGraph<N, E> cloneAdd(final N newNode) {
		return (MutableRootedTreeAdjacencyGraph<N, E>) super.cloneAdd(newNode);
	}

	@Override
	public MutableRootedTreeAdjacencyGraph<N, E> cloneAdd(
			final Set<N> newNodes, final Set<E> newEdges) {
		return (MutableRootedTreeAdjacencyGraph<N, E>) super.cloneAdd(newNodes,
				newEdges);
	}

	@Override
	public MutableRootedTreeAdjacencyGraph<N, E> cloneRemove(
			final E edgeToRemove) {
		return (MutableRootedTreeAdjacencyGraph<N, E>) super
				.cloneRemove(edgeToRemove);
	}

	@Override
	public MutableRootedTreeAdjacencyGraph<N, E> cloneRemove(
			final N nodeToRemove) {
		return (MutableRootedTreeAdjacencyGraph<N, E>) super
				.cloneRemove(nodeToRemove);
	}

	@Override
	public MutableRootedTreeAdjacencyGraph<N, E> cloneRemove(
			final Set<N> deleteNodes, final Set<E> deleteEdges) {
		return (MutableRootedTreeAdjacencyGraph<N, E>) super.cloneRemove(
				deleteNodes, deleteEdges);
	}

	@Override
	public MutableRootedTreeAdjacencyGraph<N, E> clone() {
		return (MutableRootedTreeAdjacencyGraph<N, E>) super.clone();
	}
}
