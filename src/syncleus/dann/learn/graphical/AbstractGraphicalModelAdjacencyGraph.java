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
package syncleus.dann.learn.graphical;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import syncleus.dann.graph.AbstractBidirectedAdjacencyGraph;
import syncleus.dann.graph.BidirectedEdge;
import syncleus.dann.graph.Graph;

public abstract class AbstractGraphicalModelAdjacencyGraph<N extends GraphicalModelNode, E extends BidirectedEdge<N>>
        extends AbstractBidirectedAdjacencyGraph<N, E> implements
        GraphicalModel<N, E> {
    protected AbstractGraphicalModelAdjacencyGraph() {
        super();
    }

    protected AbstractGraphicalModelAdjacencyGraph(final Graph<N, E> copyGraph) {
        super(copyGraph.getNodes(), copyGraph.getEdges());
    }

    protected AbstractGraphicalModelAdjacencyGraph(final Set<N> nodes,
                                                   final Set<E> edges) {
        super(nodes, edges);
    }

    @Override
    public void learnStates() {
        this.getNodes().stream().forEach(GraphicalModelNode::learnState);
    }

    @Override
    public double jointProbability() {
        // TODO implement this!
        throw new UnsupportedOperationException();
    }

    @Override
    public double conditionalProbability(final Set<N> goals,
                                         final Set<N> influences) {
        // first we need to preserve a map of all the starting states so we can
        // reset the network back to its starting
        // point when we are done
        final Map<N, Object> startingStates = new HashMap<>();
        this.getNodes().stream().filter((node) -> (!influences.contains(node))).forEach((node) -> startingStates.put(node, node.getState()));

        try {
            List<N> varyingNodes = new ArrayList<>(this.getNodes());

            // calculate numerator
            varyingNodes.removeAll(influences);
            varyingNodes.removeAll(goals);
            resetNodeStates(varyingNodes);
            double numerator = 0.0;
            do {
                numerator += this.jointProbability();
            } while (!incrementNodeStates(varyingNodes));

            // calculate denominator
            varyingNodes = new ArrayList<>(this.getNodes());
            varyingNodes.removeAll(influences);
            resetNodeStates(varyingNodes);
            double denominator = 0.0;
            do {
                denominator += this.jointProbability();
            } while (!incrementNodeStates(varyingNodes));

            // all done
            return numerator / denominator;
        } finally {
            startingStates
                    .entrySet().stream().forEach((nodeState) -> nodeState.getKey().setState(nodeState.getValue()));
        }
    }

    @SuppressWarnings("unchecked")
    protected static <N extends GraphicalModelNode> void resetNodeStates(
            final Collection<N> incNodes) {
        incNodes.stream().forEach((incNode) -> incNode.setState((incNode.getLearnedStates().toArray())[0]));
    }

    protected static <N extends GraphicalModelNode> boolean incrementNodeStates(
            final Collection<N> incNodes) {
        return incNodes.stream().noneMatch((incNode) -> (!incrementNodeState(incNode)));
    }

    @SuppressWarnings("unchecked")
    protected static <N extends GraphicalModelNode> boolean incrementNodeState(
            final N incNode) {
        final List stateTypes = Arrays.asList(incNode.getLearnedStates()
                .toArray());
        final int currentStateIndex = stateTypes.indexOf(incNode.getState());
        if ((currentStateIndex + 1) >= stateTypes.size()) {
            incNode.setState(stateTypes.get(0));
            return true;
        } else {
            incNode.setState(stateTypes.get(currentStateIndex + 1));
            return false;
        }
    }

    @Override
    public AbstractGraphicalModelAdjacencyGraph<N, E> cloneAdd(final E newEdge) {
        return (AbstractGraphicalModelAdjacencyGraph<N, E>) super
                .cloneAdd(newEdge);
    }

    @Override
    public AbstractGraphicalModelAdjacencyGraph<N, E> cloneAdd(final N newNode) {
        return (AbstractGraphicalModelAdjacencyGraph<N, E>) super
                .cloneAdd(newNode);
    }

    @Override
    public AbstractGraphicalModelAdjacencyGraph<N, E> cloneAdd(
            final Set<N> newNodes, final Set<E> newEdges) {
        return (AbstractGraphicalModelAdjacencyGraph<N, E>) super.cloneAdd(
                newNodes, newEdges);
    }

    @Override
    public AbstractGraphicalModelAdjacencyGraph<N, E> cloneRemove(
            final E edgeToRemove) {
        return (AbstractGraphicalModelAdjacencyGraph<N, E>) super
                .cloneRemove(edgeToRemove);
    }

    @Override
    public AbstractGraphicalModelAdjacencyGraph<N, E> cloneRemove(
            final N nodeToRemove) {
        return (AbstractGraphicalModelAdjacencyGraph<N, E>) super
                .cloneRemove(nodeToRemove);
    }

    @Override
    public AbstractGraphicalModelAdjacencyGraph<N, E> cloneRemove(
            final Set<N> deleteNodes, final Set<E> deleteEdges) {
        return (AbstractGraphicalModelAdjacencyGraph<N, E>) super.cloneRemove(
                deleteNodes, deleteEdges);
    }

    @Override
    public AbstractGraphicalModelAdjacencyGraph<N, E> clone() {
        return (AbstractGraphicalModelAdjacencyGraph<N, E>) super.clone();
    }

}