package aima.test.core.unit.search.informed;

import org.junit.Assert;
import org.junit.Test;

import syncleus.dann.solve.eightpuzzle.EightPuzzleBoard;
import syncleus.dann.solve.eightpuzzle.EightPuzzleFunctionFactory;
import syncleus.dann.solve.eightpuzzle.EightPuzzleGoalTest;
import syncleus.dann.solve.eightpuzzle.ManhattanHeuristicFunction;
import syncleus.dann.solve.map.Map;
import syncleus.dann.solve.map.MapFunctionFactory;
import syncleus.dann.solve.map.MapStepCostFunction;
import syncleus.dann.solve.map.SimplifiedRoadMapOfPartOfRomania;
import syncleus.dann.solve.map.StraightLineDistanceHeuristicFunction;
import syncleus.dann.search.framework.DefaultGoalTest;
import syncleus.dann.search.framework.GraphSearch;
import syncleus.dann.search.framework.Problem;
import syncleus.dann.search.framework.Search;
import syncleus.dann.search.framework.SearchAgent;
import syncleus.dann.search.framework.TreeSearch;
import syncleus.dann.search.informed.GreedyBestFirstSearch;

public class GreedyBestFirstSearchTest {

	@Test
	public void testGreedyBestFirstSearch() {
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
			Search search = new GreedyBestFirstSearch(new GraphSearch(),
					new ManhattanHeuristicFunction());
			SearchAgent agent = new SearchAgent(problem, search);
			Assert.assertEquals(49, agent.getActions().size());
			Assert.assertEquals("197",
					agent.getInstrumentation().getProperty("nodesExpanded"));
			Assert.assertEquals("140",
					agent.getInstrumentation().getProperty("queueSize"));
			Assert.assertEquals("141",
					agent.getInstrumentation().getProperty("maxQueueSize"));
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail("Exception thrown.");
		}
	}

	@Test
	public void testAIMA3eFigure3_23() throws Exception {
		Map romaniaMap = new SimplifiedRoadMapOfPartOfRomania();
		Problem problem = new Problem(SimplifiedRoadMapOfPartOfRomania.ARAD,
				MapFunctionFactory.getActionsFunction(romaniaMap),
				MapFunctionFactory.getResultFunction(), new DefaultGoalTest(
						SimplifiedRoadMapOfPartOfRomania.BUCHAREST),
				new MapStepCostFunction(romaniaMap));

		Search search = new GreedyBestFirstSearch(new TreeSearch(),
				new StraightLineDistanceHeuristicFunction(
						SimplifiedRoadMapOfPartOfRomania.BUCHAREST, romaniaMap));
		SearchAgent agent = new SearchAgent(problem, search);
		Assert.assertEquals(
				"[Action[name==moveTo, location==Sibiu], Action[name==moveTo, location==Fagaras], Action[name==moveTo, location==Bucharest]]",
				agent.getActions().toString());
		Assert.assertEquals(3, agent.getActions().size());
		Assert.assertEquals("3",
				agent.getInstrumentation().getProperty("nodesExpanded"));
		Assert.assertEquals("6",
				agent.getInstrumentation().getProperty("queueSize"));
		Assert.assertEquals("7",
				agent.getInstrumentation().getProperty("maxQueueSize"));
	}

	@Test
	public void testAIMA3eFigure3_23_using_GraphSearch() throws Exception {
		Map romaniaMap = new SimplifiedRoadMapOfPartOfRomania();
		Problem problem = new Problem(SimplifiedRoadMapOfPartOfRomania.ARAD,
				MapFunctionFactory.getActionsFunction(romaniaMap),
				MapFunctionFactory.getResultFunction(), new DefaultGoalTest(
						SimplifiedRoadMapOfPartOfRomania.BUCHAREST),
				new MapStepCostFunction(romaniaMap));

		Search search = new GreedyBestFirstSearch(new GraphSearch(),
				new StraightLineDistanceHeuristicFunction(
						SimplifiedRoadMapOfPartOfRomania.BUCHAREST, romaniaMap));
		SearchAgent agent = new SearchAgent(problem, search);
		Assert.assertEquals(
				"[Action[name==moveTo, location==Sibiu], Action[name==moveTo, location==Fagaras], Action[name==moveTo, location==Bucharest]]",
				agent.getActions().toString());
		Assert.assertEquals(3, agent.getActions().size());
		Assert.assertEquals("3",
				agent.getInstrumentation().getProperty("nodesExpanded"));
		Assert.assertEquals("4",
				agent.getInstrumentation().getProperty("queueSize"));
		Assert.assertEquals("5",
				agent.getInstrumentation().getProperty("maxQueueSize"));
	}
}
