package aima.test.core.unit.search.uninformed;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import syncleus.dann.plan.agent.Action;
import syncleus.dann.solve.map.ExtendableMap;
import syncleus.dann.solve.map.Map;
import syncleus.dann.solve.map.MapFunctionFactory;
import syncleus.dann.solve.map.MapStepCostFunction;
import syncleus.dann.solve.map.SimplifiedRoadMapOfPartOfRomania;
import syncleus.dann.solve.nqueens.NQueensBoard;
import syncleus.dann.solve.nqueens.NQueensFunctionFactory;
import syncleus.dann.solve.nqueens.NQueensGoalTest;
import aima.search.framework.DefaultGoalTest;
import aima.search.framework.Problem;
import aima.search.framework.QueueSearch;
import aima.search.framework.Search;
import aima.search.framework.SearchAgent;
import aima.search.uninformed.UniformCostSearch;

/**
 * @author Ciaran O'Reilly
 * 
 */
public class UniformCostSearchTest {

	@Test
	public void testUniformCostSuccesfulSearch() throws Exception {
		Problem problem = new Problem(new NQueensBoard(8),
				NQueensFunctionFactory.getIActionsFunction(),
				NQueensFunctionFactory.getResultFunction(),
				new NQueensGoalTest());
		Search search = new UniformCostSearch();
		SearchAgent agent = new SearchAgent(problem, search);

		List<Action> actions = agent.getActions();

		Assert.assertEquals(8, actions.size());

		Assert.assertEquals("1965",
				agent.getInstrumentation().getProperty("nodesExpanded"));

		Assert.assertEquals("8.0",
				agent.getInstrumentation().getProperty("pathCost"));
	}

	@Test
	public void testUniformCostUnSuccesfulSearch() throws Exception {
		Problem problem = new Problem(new NQueensBoard(3),
				NQueensFunctionFactory.getIActionsFunction(),
				NQueensFunctionFactory.getResultFunction(),
				new NQueensGoalTest());
		Search search = new UniformCostSearch();
		SearchAgent agent = new SearchAgent(problem, search);

		List<Action> actions = agent.getActions();

		Assert.assertEquals(0, actions.size());

		Assert.assertEquals("6",
				agent.getInstrumentation().getProperty("nodesExpanded"));

		// Will be 0 as did not reach goal state.
		Assert.assertEquals("0",
				agent.getInstrumentation().getProperty("pathCost"));
	}

	@Test
	public void testAIMA3eFigure3_15() throws Exception {
		Map romaniaMap = new SimplifiedRoadMapOfPartOfRomania();
		Problem problem = new Problem(SimplifiedRoadMapOfPartOfRomania.SIBIU,
				MapFunctionFactory.getActionsFunction(romaniaMap),
				MapFunctionFactory.getResultFunction(), new DefaultGoalTest(
						SimplifiedRoadMapOfPartOfRomania.BUCHAREST),
				new MapStepCostFunction(romaniaMap));

		Search search = new UniformCostSearch();
		SearchAgent agent = new SearchAgent(problem, search);

		List<Action> actions = agent.getActions();

		Assert.assertEquals(
				"[Action[name==moveTo, location==RimnicuVilcea], Action[name==moveTo, location==Pitesti], Action[name==moveTo, location==Bucharest]]",
				actions.toString());
		Assert.assertEquals("278.0",
				search.getMetrics().get(QueueSearch.METRIC_PATH_COST));
	}

	@Test
	public void testCheckFrontierPathCost() throws Exception {
		ExtendableMap map = new ExtendableMap();
		map.addBidirectionalLink("start", "b", 2.5);
		map.addBidirectionalLink("start", "c", 1.0);
		map.addBidirectionalLink("b", "d", 2.0);
		map.addBidirectionalLink("c", "d", 4.0);
		map.addBidirectionalLink("c", "e", 1.0);
		map.addBidirectionalLink("d", "goal", 1.0);
		map.addBidirectionalLink("e", "goal", 5.0);
		Problem problem = new Problem("start",
				MapFunctionFactory.getActionsFunction(map),
				MapFunctionFactory.getResultFunction(), new DefaultGoalTest(
						"goal"), new MapStepCostFunction(map));

		Search search = new UniformCostSearch();
		SearchAgent agent = new SearchAgent(problem, search);

		List<Action> actions = agent.getActions();

		Assert.assertEquals(
				"[Action[name==moveTo, location==b], Action[name==moveTo, location==d], Action[name==moveTo, location==goal]]",
				actions.toString());
		Assert.assertEquals("5.5",
				search.getMetrics().get(QueueSearch.METRIC_PATH_COST));
	}
}
