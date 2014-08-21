package syncleus.dann.solve.nqueens;

import syncleus.dann.search.framework.GoalTest;

/**
 * @author R. Lunde
 */
public class NQueensGoalTest implements GoalTest {

	public boolean isGoalState(Object state) {
		NQueensBoard board = (NQueensBoard) state;
		return board.getNumberOfQueensOnBoard() == board.getSize()
				&& board.getNumberOfAttackingPairs() == 0;
	}
}