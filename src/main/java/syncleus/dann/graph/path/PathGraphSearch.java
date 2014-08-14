/*
 * Encog(tm) Core v3.2 - Java Version
 * http://www.heatonresearch.com/encog/
 * https://github.com/encog/encog-java-core

 * Copyright 2008-2013 Heaton Research, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * For more information on Heaton Research copyrights, licenses
 * and trademarks visit:
 * http://www.heatonresearch.com/copyright
 */
package syncleus.dann.graph.path;

import java.util.HashSet;
import java.util.Set;

import syncleus.dann.graph.path.search.FrontierHolder;
import syncleus.dann.graph.path.search.GraphSearch;
import syncleus.dann.graph.path.search.SearchGoal;

public abstract class PathGraphSearch implements GraphSearch {

	private final PathGraph graph;
	private final SearchGoal goal;
	private final FrontierHolder frontier = new FrontierHolder(this);
	private final Set<PathNode> explored = new HashSet<PathNode>();
	private BasicPath solution;

	public PathGraphSearch(final PathGraph theGraph,
			final PathNode startingPoint, final SearchGoal theGoal) {
		this.graph = theGraph;
		this.goal = theGoal;
		frontier.add(new BasicPath(startingPoint));
	}

	@Override
	public PathGraph getGraph() {
		return graph;
	}

	@Override
	public SearchGoal getGoal() {
		return goal;
	}

	@Override
	public void iteration() {
		if (solution == null) {

			if (this.frontier.size() == 0) {
				throw new RuntimeException("Frontier is empty, cannot find solution.");
			}

			final BasicPath path = this.frontier.pop();

			if (this.goal.isGoalMet(path)) {
				this.solution = path;
				return;
			}

			final PathNode state = path.getDestinationNode();
			this.explored.add(state);

                        state.getConnections().stream().filter((connection) -> (!this.explored.contains(connection.getDestinationNode())
                            && !this.frontier.containsDestination(connection
                                    .getDestinationNode()))).map((connection) -> new BasicPath(path,
                                                                        connection.getDestinationNode())).forEach((path2) -> {
                                                            this.frontier.add(path2);
                    });
		}
	}

	/**
	 * @return the solution
	 */
	@Override
	public BasicPath getSolution() {
		return solution;
	}

}
