package aima.test.core.unit.search.uninformed;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import syncleus.dann.plan.agent.Action;
import syncleus.dann.solve.nqueens.NQueensBoard;
import syncleus.dann.solve.nqueens.NQueensFunctionFactory;
import syncleus.dann.solve.nqueens.NQueensGoalTest;
import syncleus.dann.search.framework.Problem;
import syncleus.dann.search.framework.Search;
import syncleus.dann.search.framework.SearchAgent;
import syncleus.dann.search.framework.TreeSearch;
import syncleus.dann.search.uninformed.BreadthFirstSearch;

public class BreadthFirstSearchTest {

	@Test
	public void testBreadthFirstSuccesfulSearch() throws Exception {
		Problem problem = new Problem(new NQueensBoard(8),
				NQueensFunctionFactory.getIActionsFunction(),
				NQueensFunctionFactory.getResultFunction(),
				new NQueensGoalTest());
		Search search = new BreadthFirstSearch(new TreeSearch());
		SearchAgent agent = new SearchAgent(problem, search);
		List<Action> actions = agent.getActions();
		assertCorrectPlacement(actions);
		Assert.assertEquals("1665",
				agent.getInstrumentation().getProperty("nodesExpanded"));
		Assert.assertEquals("8.0",
				agent.getInstrumentation().getProperty("pathCost"));
	}

	@Test
	public void testBreadthFirstUnSuccesfulSearch() throws Exception {
		Problem problem = new Problem(new NQueensBoard(3),
				NQueensFunctionFactory.getIActionsFunction(),
				NQueensFunctionFactory.getResultFunction(),
				new NQueensGoalTest());
		Search search = new BreadthFirstSearch(new TreeSearch());
		SearchAgent agent = new SearchAgent(problem, search);
		List<Action> actions = agent.getActions();
		Assert.assertEquals(0, actions.size());
		Assert.assertEquals("6",
				agent.getInstrumentation().getProperty("nodesExpanded"));
		Assert.assertEquals("0",
				agent.getInstrumentation().getProperty("pathCost"));
	}

	//
	// PRIVATE METHODS
	//
	private void assertCorrectPlacement(List<Action> actions) {
		Assert.assertEquals(8, actions.size());
		Assert.assertEquals(
				"Action[name==placeQueenAt, location== ( 0 , 0 ) ]", actions
						.get(0).toString());
		Assert.assertEquals(
				"Action[name==placeQueenAt, location== ( 1 , 4 ) ]", actions
						.get(1).toString());
		Assert.assertEquals(
				"Action[name==placeQueenAt, location== ( 2 , 7 ) ]", actions
						.get(2).toString());
		Assert.assertEquals(
				"Action[name==placeQueenAt, location== ( 3 , 5 ) ]", actions
						.get(3).toString());
		Assert.assertEquals(
				"Action[name==placeQueenAt, location== ( 4 , 2 ) ]", actions
						.get(4).toString());
		Assert.assertEquals(
				"Action[name==placeQueenAt, location== ( 5 , 6 ) ]", actions
						.get(5).toString());
		Assert.assertEquals(
				"Action[name==placeQueenAt, location== ( 6 , 1 ) ]", actions
						.get(6).toString());
		Assert.assertEquals(
				"Action[name==placeQueenAt, location== ( 7 , 3 ) ]", actions
						.get(7).toString());
	}
}
