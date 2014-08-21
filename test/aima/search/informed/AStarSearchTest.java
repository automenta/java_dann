package aima.test.core.unit.search.informed;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import syncleus.dann.plan.agent.Action;
import syncleus.dann.solve.eightpuzzle.EightPuzzleBoard;
import syncleus.dann.solve.eightpuzzle.EightPuzzleFunctionFactory;
import syncleus.dann.solve.eightpuzzle.EightPuzzleGoalTest;
import syncleus.dann.solve.eightpuzzle.ManhattanHeuristicFunction;
import syncleus.dann.solve.map.ExtendableMap;
import syncleus.dann.solve.map.Map;
import syncleus.dann.solve.map.MapFunctionFactory;
import syncleus.dann.solve.map.MapStepCostFunction;
import syncleus.dann.solve.map.SimplifiedRoadMapOfPartOfRomania;
import syncleus.dann.solve.map.StraightLineDistanceHeuristicFunction;
import aima.search.framework.DefaultGoalTest;
import aima.search.framework.GraphSearch;
import aima.search.framework.HeuristicFunction;
import aima.search.framework.Problem;
import aima.search.framework.QueueSearch;
import aima.search.framework.Search;
import aima.search.framework.SearchAgent;
import aima.search.framework.TreeSearch;
import aima.search.informed.AStarSearch;

public class AStarSearchTest {

	@Test
	public void testAStarSearch() {
		// added to narrow down bug report filed by L.N.Sudarshan of
		// Thoughtworks and Xin Lu of UCI
		try {
			// EightPuzzleBoard extreme = new EightPuzzleBoard(new int[]
			// {2,0,5,6,4,8,3,7,1});
			// EightPuzzleBoard extreme = new EightPuzzleBoard(new int[]
			// {0,8,7,6,5,4,3,2,1});
			EightPuzzleBoard board = new EightPuzzleBoard(new int[] { 7, 1, 8,
					0, 4, 6, 2, 3, 5 });

			Problem problem = new Problem(board,
					EightPuzzleFunctionFactory.getActionsFunction(),
					EightPuzzleFunctionFactory.getResultFunction(),
					new EightPuzzleGoalTest());
			Search search = new AStarSearch(new GraphSearch(),
					new ManhattanHeuristicFunction());
			SearchAgent agent = new SearchAgent(problem, search);
			Assert.assertEquals(23, agent.getActions().size());
			Assert.assertEquals("926",
					agent.getInstrumentation().getProperty("nodesExpanded"));
			Assert.assertEquals("534",
					agent.getInstrumentation().getProperty("queueSize"));
			Assert.assertEquals("535",
					agent.getInstrumentation().getProperty("maxQueueSize"));
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail("Exception thrown");
		}
	}

	@Test
	public void testAIMA3eFigure3_15() throws Exception {
		Map romaniaMap = new SimplifiedRoadMapOfPartOfRomania();
		Problem problem = new Problem(SimplifiedRoadMapOfPartOfRomania.SIBIU,
				MapFunctionFactory.getActionsFunction(romaniaMap),
				MapFunctionFactory.getResultFunction(), new DefaultGoalTest(
						SimplifiedRoadMapOfPartOfRomania.BUCHAREST),
				new MapStepCostFunction(romaniaMap));

		Search search = new AStarSearch(new GraphSearch(),
				new StraightLineDistanceHeuristicFunction(
						SimplifiedRoadMapOfPartOfRomania.BUCHAREST, romaniaMap));
		SearchAgent agent = new SearchAgent(problem, search);

		List<Action> actions = agent.getActions();

		Assert.assertEquals(
				"[Action[name==moveTo, location==RimnicuVilcea], Action[name==moveTo, location==Pitesti], Action[name==moveTo, location==Bucharest]]",
				actions.toString());
		Assert.assertEquals("278.0",
				search.getMetrics().get(QueueSearch.METRIC_PATH_COST));
	}

	@Test
	public void testAIMA3eFigure3_24() throws Exception {
		Map romaniaMap = new SimplifiedRoadMapOfPartOfRomania();
		Problem problem = new Problem(SimplifiedRoadMapOfPartOfRomania.ARAD,
				MapFunctionFactory.getActionsFunction(romaniaMap),
				MapFunctionFactory.getResultFunction(), new DefaultGoalTest(
						SimplifiedRoadMapOfPartOfRomania.BUCHAREST),
				new MapStepCostFunction(romaniaMap));

		Search search = new AStarSearch(new TreeSearch(),
				new StraightLineDistanceHeuristicFunction(
						SimplifiedRoadMapOfPartOfRomania.BUCHAREST, romaniaMap));
		SearchAgent agent = new SearchAgent(problem, search);
		Assert.assertEquals(
				"[Action[name==moveTo, location==Sibiu], Action[name==moveTo, location==RimnicuVilcea], Action[name==moveTo, location==Pitesti], Action[name==moveTo, location==Bucharest]]",
				agent.getActions().toString());
		Assert.assertEquals(4, agent.getActions().size());
		Assert.assertEquals("5",
				agent.getInstrumentation().getProperty("nodesExpanded"));
		Assert.assertEquals("10",
				agent.getInstrumentation().getProperty("queueSize"));
		Assert.assertEquals("11",
				agent.getInstrumentation().getProperty("maxQueueSize"));
	}

	@Test
	public void testAIMA3eFigure3_24_using_GraphSearch() throws Exception {
		Map romaniaMap = new SimplifiedRoadMapOfPartOfRomania();
		Problem problem = new Problem(SimplifiedRoadMapOfPartOfRomania.ARAD,
				MapFunctionFactory.getActionsFunction(romaniaMap),
				MapFunctionFactory.getResultFunction(), new DefaultGoalTest(
						SimplifiedRoadMapOfPartOfRomania.BUCHAREST),
				new MapStepCostFunction(romaniaMap));

		Search search = new AStarSearch(new GraphSearch(),
				new StraightLineDistanceHeuristicFunction(
						SimplifiedRoadMapOfPartOfRomania.BUCHAREST, romaniaMap));
		SearchAgent agent = new SearchAgent(problem, search);
		Assert.assertEquals(
				"[Action[name==moveTo, location==Sibiu], Action[name==moveTo, location==RimnicuVilcea], Action[name==moveTo, location==Pitesti], Action[name==moveTo, location==Bucharest]]",
				agent.getActions().toString());
		Assert.assertEquals(4, agent.getActions().size());
		Assert.assertEquals("5",
				agent.getInstrumentation().getProperty("nodesExpanded"));
		Assert.assertEquals("4",
				agent.getInstrumentation().getProperty("queueSize"));
		Assert.assertEquals("6",
				agent.getInstrumentation().getProperty("maxQueueSize"));
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

		HeuristicFunction hf = new HeuristicFunction() {
			public double h(Object state) {
				return 0; // Don't have one for this test
			}
		};
		Search search = new AStarSearch(new GraphSearch(), hf);
		SearchAgent agent = new SearchAgent(problem, search);

		List<Action> actions = agent.getActions();

		Assert.assertEquals(
				"[Action[name==moveTo, location==b], Action[name==moveTo, location==d], Action[name==moveTo, location==goal]]",
				actions.toString());
		Assert.assertEquals("5.5",
				search.getMetrics().get(QueueSearch.METRIC_PATH_COST));
	}
}
