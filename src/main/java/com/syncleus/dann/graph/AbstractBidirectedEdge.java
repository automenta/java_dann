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


import java.util.ArrayList;
import java.util.List;

public abstract class AbstractBidirectedEdge<N> extends AbstractEdge<N> implements BidirectedEdge<N>
{
	private final N leftNode;
	private final N rightNode;
	private final EndState leftEndState;
	private final EndState rightEndState;

	protected AbstractBidirectedEdge()
	{
		this.leftNode = null;
		this.rightNode = null;
		this.leftEndState = null;
		this.rightEndState = null;
	}

	protected AbstractBidirectedEdge(final boolean allowJoiningMultipleGraphs, final boolean contextEnabled)
	{
		super(allowJoiningMultipleGraphs, contextEnabled);

		this.leftNode = null;
		this.rightNode = null;
		this.leftEndState = null;
		this.rightEndState = null;
	}

	protected AbstractBidirectedEdge(final N newLeftNode, final EndState newLeftEndState, final N newRightNode, final EndState newRightEndState)
	{
		super(packNodes(newLeftNode, newRightNode));

		this.leftNode = newLeftNode;
		this.rightNode = newRightNode;
		this.leftEndState = newLeftEndState;
		this.rightEndState = newRightEndState;
	}

	protected AbstractBidirectedEdge(final N newLeftNode, final EndState newLeftEndState, final N newRightNode, final EndState newRightEndState, final boolean allowJoiningMultipleGraphs, final boolean contextEnabled)
	{
		super(packNodes(newLeftNode, newRightNode), allowJoiningMultipleGraphs, contextEnabled);

		this.leftNode = newLeftNode;
		this.rightNode = newRightNode;
		this.leftEndState = newLeftEndState;
		this.rightEndState = newRightEndState;
	}

	private static <N> List<N> packNodes(final N leftNode, final N rightNode)
	{
		final List<N> pack = new ArrayList<N>();
		pack.add(leftNode);
		pack.add(rightNode);
		return pack;
	}

	@Override
	public final N getLeftNode()
	{
		return this.leftNode;
	}

	@Override
	public final N getRightNode()
	{
		return this.rightNode;
	}

	@Override
	public final EndState getLeftEndState()
	{
		return this.leftEndState;
	}

	@Override
	public final EndState getRightEndState()
	{
		return this.rightEndState;
	}

	@Override
	public boolean isIntroverted()
	{
		return (this.rightEndState == BidirectedEdge.EndState.INWARD) && (this.leftEndState == BidirectedEdge.EndState.INWARD);
	}

	@Override
	public boolean isExtroverted()
	{
		return (this.rightEndState == BidirectedEdge.EndState.OUTWARD) && (this.leftEndState == BidirectedEdge.EndState.OUTWARD);
	}

	@Override
	public boolean isDirected()
	{
		if( (this.rightEndState == EndState.INWARD) && (this.leftEndState == EndState.OUTWARD) )
			return true;
		else if( (this.rightEndState == EndState.OUTWARD) && (this.leftEndState == EndState.INWARD) )
			return true;
		return false;
	}

	@Override
	public boolean isHalfEdge()
	{
		if( (this.rightEndState == EndState.NONE) && (this.leftEndState != EndState.NONE) )
			return true;
		else if( (this.rightEndState != EndState.NONE) && (this.leftEndState == EndState.NONE) )
			return true;
		return false;
	}

	@Override
	public boolean isLooseEdge()
	{
		return (this.rightEndState == BidirectedEdge.EndState.NONE) && (this.leftEndState == BidirectedEdge.EndState.NONE);
	}

	@Override
	public boolean isOrdinaryEdge()
	{
		return (!this.isHalfEdge()) && (!this.isLooseEdge());
	}

	@Override
	public boolean isLoop()
	{
		return this.leftEndState.equals(this.rightEndState);
	}

	@Override
	public String toString()
	{
		return this.leftNode.toString()
				+ endStateToString(this.leftEndState, true)
				+ '-'
				+ endStateToString(this.rightEndState, false)
				+ this.rightNode;
	}

	private static String endStateToString(final EndState state, final boolean isLeft)
	{
		switch(state)
		{
		case INWARD:
			return (isLeft ? ">" : "<");
		case OUTWARD:
			return (isLeft ? "<" : ">");
		default:
			return "";
		}
	}

	@Override
	public AbstractBidirectedEdge<N> clone()
	{
		return (AbstractBidirectedEdge<N>) super.clone();
	}

}
