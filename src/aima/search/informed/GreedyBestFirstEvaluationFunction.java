package aima.search.informed;

import aima.search.framework.EvaluationFunction;
import aima.search.framework.HeuristicFunction;
import aima.search.framework.Node;

/**
 * Artificial Intelligence A Modern Approach (3rd Edition): page 92.<br>
 * <br>
 * Greedy best-first search tries to expand the node that is closest to the
 * goal, on the grounds that this is likely to lead to a solution quickly. Thus,
 * it evaluates nodes by using just the heuristic function; that is, f(n) = h(n)
 * 
 * @author Ciaran O'Reilly
 * 
 */
public class GreedyBestFirstEvaluationFunction implements EvaluationFunction {

	private HeuristicFunction hf = null;

	public GreedyBestFirstEvaluationFunction(HeuristicFunction hf) {
		this.hf = hf;
	}

	public double f(Node n) {
		// f(n) = h(n)
		return hf.h(n.getState());
	}
}
