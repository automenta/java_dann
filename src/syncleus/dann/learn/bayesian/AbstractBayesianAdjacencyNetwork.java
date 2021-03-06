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
package syncleus.dann.learn.bayesian;

import java.util.Set;
import syncleus.dann.graph.DirectedEdge;
import syncleus.dann.graph.Graph;
import syncleus.dann.learn.graphical.AbstractGraphicalModelAdjacencyGraph;
import syncleus.dann.learn.graphical.GraphicalModelNode;

public abstract class AbstractBayesianAdjacencyNetwork<N extends GraphicalModelNode, E extends DirectedEdge<N>>
        extends AbstractGraphicalModelAdjacencyGraph<N, E> {
    protected AbstractBayesianAdjacencyNetwork() {
        super();
    }

    protected AbstractBayesianAdjacencyNetwork(final Graph<N, E> copyGraph) {
        super(copyGraph.getNodes(), copyGraph.getEdges());
    }

    protected AbstractBayesianAdjacencyNetwork(final Set<N> nodes,
                                               final Set<E> edges) {
        super(nodes, edges);
    }

    @Override
    public double jointProbability() {
        double probabilityProduct = 1.0;
        probabilityProduct = this.getNodes().stream().map((node) -> node.stateProbability()).reduce(probabilityProduct, (accumulator, _item) -> accumulator * _item);
        return probabilityProduct;
    }

    @Override
    public AbstractGraphicalModelAdjacencyGraph<N, E> clone() {
        return super.clone(); //To change body of generated methods, choose Tools | Templates.
    }
}
