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
package com.syncleus.dann.graph;

import java.util.*;

public class SimpleDirectedGraph<N, E extends DirectedEdge<? extends N>, W extends BidirectedWalk<? extends N, ? extends E>> extends AbstractBidirectedGraph<N,E,W>
{
	final private Set<N> nodes;
	final private List<E> edges;
	final private Map<N, List<E>> neighborEdges = new HashMap<N, List<E>>();
	final private Map<N, Set<N>> neighborNodes = new HashMap<N, Set<N>>();

	public SimpleDirectedGraph(Set<? extends N> nodes, List<? extends E> edges)
	{
		this.nodes = new HashSet<N>(nodes);
		this.edges = new ArrayList<E>(edges);
		for(E edge : edges)
		{
			final List<? extends N> edgeNodes = edge.getNodes();
			for(int startNodeIndex = 0; startNodeIndex < edgeNodes.size(); startNodeIndex++)
			{
				if(!this.nodes.contains(edgeNodes.get(startNodeIndex)))
					throw new IllegalArgumentException("A node that is an end point in one of the edges was not in the nodes list");

				List<E> startNeighborEdges = this.neighborEdges.get(edgeNodes.get(startNodeIndex));
				if( startNeighborEdges == null )
				{
					startNeighborEdges = new ArrayList<E>();
					this.neighborEdges.put(edgeNodes.get(startNodeIndex), startNeighborEdges);
				}
				startNeighborEdges.add(edge);

				Set<N> startNeighborNodes = this.neighborNodes.get(edgeNodes.get(startNodeIndex));
				if( startNeighborNodes == null )
				{
					startNeighborNodes = new HashSet<N>();
					this.neighborNodes.put(edgeNodes.get(startNodeIndex), startNeighborNodes);
				}

				for(int endNodeIndex = 0; endNodeIndex < edgeNodes.size(); endNodeIndex++)
				{
					if(startNodeIndex == endNodeIndex)
						continue;

					startNeighborNodes.add(edgeNodes.get(endNodeIndex));
				}
			}
		}
	}

	public Set<N> getNodes()
	{
		return Collections.unmodifiableSet(this.nodes);
	}

	@Override
	public List<E> getEdges()
	{
		return Collections.unmodifiableList(new ArrayList<E>(this.edges));
	}

	public List<E> getEdges(N node)
	{
		return Collections.unmodifiableList(new ArrayList<E>(this.neighborEdges.get(node)));
	}

	public List<E> getTraversableEdges(N node)
	{
		final List<E> traversableEdges = new ArrayList<E>();
		for(E edge : edges)
			if(edge.getSourceNode() == node)
				traversableEdges.add(edge);
		return Collections.unmodifiableList(traversableEdges);
	}

	public List<E> getOutEdges(N node)
	{
		return this.getTraversableEdges(node);
	}

	public List<E> getInEdges(N node)
	{
		final List<E> inEdges = new ArrayList<E>();
		for(E edge : edges)
			if(edge.getDestinationNode() == node)
				inEdges.add(edge);
		return Collections.unmodifiableList(inEdges);
	}

	public int getIndegree(N node)
	{
		return this.getInEdges(node).size();
	}

	public int getOutdegree(N node)
	{
		return this.getOutEdges(node).size();
	}

	public boolean isConnected(N leftNode, N rightNode)
	{
		return this.neighborNodes.get(leftNode).contains(rightNode);
	}

	public List<N> getNeighbors(N node)
	{
		return Collections.unmodifiableList(new ArrayList<N>(this.neighborNodes.get(node)));
	}

	public List<N> getTraversableNeighbors(N node)
	{
		List<E> traversableEdges = this.getTraversableEdges(node);
		List<N> traversableNeighbors = new ArrayList<N>();
		for(E traversableEdge : traversableEdges)
			traversableNeighbors.add(traversableEdge.getDestinationNode());
		return Collections.unmodifiableList(traversableNeighbors);
	}
}