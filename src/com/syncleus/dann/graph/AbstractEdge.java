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

import com.syncleus.dann.UnexpectedDannError;
import java.util.*;
import org.apache.log4j.Logger;

public abstract class AbstractEdge<N> implements Edge<N>
{
	private List<N> nodes;
	private final static Logger LOGGER = Logger.getLogger(AbstractEdge.class);

	protected AbstractEdge(final List<N> nodes)
	{
		this.nodes = Collections.unmodifiableList(new ArrayList<N>(nodes));
	}

	protected AbstractEdge(final N... nodes)
	{
		final List<N> newNodes = new ArrayList<N>();
		for(N node : nodes)
			newNodes.add(node);
		this.nodes = Collections.unmodifiableList(newNodes);
	}

	protected AbstractEdge<N> remove(N node)
	{
		if(node == null)
			throw new IllegalArgumentException("node can not be null", new NullPointerException());
		if(!this.getNodes().contains(node))
			throw new IllegalArgumentException("is not an endpoint");

		List<N> newNodes = new ArrayList<N>(this.nodes);
		newNodes.remove(node);

		if( newNodes.size() <= 1 )
			return null;

		AbstractEdge<N> copy = this.clone();
		copy.nodes = Collections.unmodifiableList(newNodes);
		return copy;
	}

	protected AbstractEdge<N> remove(List<N> nodes)
	{
		if(nodes == null)
			throw new IllegalArgumentException("nodes can not be null", new NullPointerException());
		if(!this.getNodes().containsAll(nodes))
			throw new IllegalArgumentException("nodes do not contain all valid end points");

		List<N> newNodes = new ArrayList<N>(this.nodes);
		for(N node : nodes)
			newNodes.remove(node);

		if( newNodes.size() <= 1 )
			return null;

		AbstractEdge<N> copy = this.clone();
		copy.nodes = Collections.unmodifiableList(newNodes);
		return copy;
	}

	public boolean isTraversable(final N node)
	{
		return (! this.getTraversableNodes(node).isEmpty());
	}

	public final List<N> getNodes()
	{
		return this.nodes;
	}

	@Override
	public String toString()
	{
		StringBuilder outString = null;
		for(N node : this.nodes)
		{
			if(outString == null)
				outString = new StringBuilder(node.toString());
			else
				outString.append(":" + node);
		}
		return outString.toString();
	}

	@Override
	public AbstractEdge<N> clone()
	{
		try
		{
			return (AbstractEdge<N>) super.clone();
		}
		catch(CloneNotSupportedException caught)
		{
			LOGGER.error("Edge was unexpectidly not cloneable", caught);
			throw new UnexpectedDannError("Edge was unexpectidly not cloneable");
		}
	}
}
