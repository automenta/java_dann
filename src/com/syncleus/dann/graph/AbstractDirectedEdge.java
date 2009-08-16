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

public abstract class AbstractDirectedEdge<N extends DirectedNode> extends AbstractBidirectedEdge<N> implements DirectedEdge<N>
{
	private N sourceNode;
	private N destinationNode;

	protected AbstractDirectedEdge(N sourceNode, N destinationNode)
	{
		super(new NodePair<N>(sourceNode, destinationNode), EndState.Inward, EndState.Outward);
		this.sourceNode = sourceNode;
		this.destinationNode = destinationNode;
	}

	public N getSourceNode()
	{
		return this.sourceNode;
	}

	public N getDestinationNode()
	{
		return this.destinationNode;
	}

	@Override
	public boolean isIntroverted()
	{
		return false;
	}

	@Override
	public boolean isExtraverted()
	{
		return false;
	}

	@Override
	public boolean isDirected()
	{
		return true;
	}

	@Override
	public boolean isHalfEdge()
	{
		return false;
	}

	@Override
	public boolean isLooseEdge()
	{
		return false;
	}

	@Override
	public boolean isOrdinaryEdge()
	{
		return true;
	}
}
