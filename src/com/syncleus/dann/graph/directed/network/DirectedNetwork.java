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
package com.syncleus.dann.graph.directed.network;

import com.syncleus.dann.graph.directed.DirectedGraph;
import java.util.List;
import java.util.Set;

public interface DirectedNetwork<G extends DirectedNetwork<? extends G, ? extends N, ? extends E, ? extends W>, N extends WeightedDirectedNode<? extends E>, E extends WeightedDirectedEdge<? extends N>, W extends WeightedDirectedWalk<? extends N, ? extends E>> extends DirectedGraph<G, N, E, W>, BidirectedNetwork<G, N, E, W>
{
	Set<N> getNodes();
	List<E> getEdges();
}
