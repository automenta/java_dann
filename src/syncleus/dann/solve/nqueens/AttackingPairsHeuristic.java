package syncleus.dann.solve.nqueens;

import syncleus.dann.search.framework.HeuristicFunction;

/**
 * Estimates the distance to goal by the number of attacking pairs of queens on
 * the board.
 * 
 * @author R. Lunde
 */
public class AttackingPairsHeuristic implements HeuristicFunction {

	public double h(Object state) {
		NQueensBoard board = (NQueensBoard) state;
		return board.getNumberOfAttackingPairs();
	}
}