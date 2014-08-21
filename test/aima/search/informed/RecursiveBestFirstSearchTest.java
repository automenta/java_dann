package aima.test.core.unit.search.informed;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import syncleus.dann.plan.agent.Action;
import syncleus.dann.plan.agent.Agent;
import syncleus.dann.plan.agent.EnvironmentState;
import syncleus.dann.plan.agent.EnvironmentView;
import syncleus.dann.solve.map.Map;
import syncleus.dann.solve.map.MapAgent;
import syncleus.dann.solve.map.MapEnvironment;
import syncleus.dann.solve.map.SimplifiedRoadMapOfPartOfRomania;
import aima.search.framework.HeuristicFunction;
import aima.search.informed.AStarEvaluationFunction;
import aima.search.informed.RecursiveBestFirstSearch;
import aima.util.datastructure.Point2D;

/**
 * @author Ciaran O'Reilly
 * 
 */
public class RecursiveBestFirstSearchTest {

	StringBuffer envChanges;

	Map aMap;

	RecursiveBestFirstSearch recursiveBestFirstSearch;

	@Before
	public void setUp() {
		envChanges = new StringBuffer();

		aMap = new SimplifiedRoadMapOfPartOfRomania();

		HeuristicFunction heuristicFunction = new HeuristicFunction() {
			public double h(Object state) {
				Point2D pt1 = aMap.getPosition((String) state);
				Point2D pt2 = aMap
						.getPosition(SimplifiedRoadMapOfPartOfRomania.BUCHAREST);
				return pt1.distance(pt2);
			}
		};

		recursiveBestFirstSearch = new RecursiveBestFirstSearch(
				new AStarEvaluationFunction(heuristicFunction));
	}

	@Test
	public void testStartingAtGoal() {
		MapEnvironment me = new MapEnvironment(aMap);
		MapAgent ma = new MapAgent(me.getMap(), me, recursiveBestFirstSearch,
				new String[] { SimplifiedRoadMapOfPartOfRomania.BUCHAREST });

		me.addAgent(ma, SimplifiedRoadMapOfPartOfRomania.BUCHAREST);
		me.addEnvironmentView(new TestEnvironmentView());
		me.stepUntilDone();

		Assert.assertEquals(
				"CurrentLocation=In(Bucharest), Goal=In(Bucharest):Action[name==NoOp]:METRIC[pathCost]=0.0:METRIC[maxRecursiveDepth]=0:METRIC[nodesExpanded]=0:Action[name==NoOp]:",
				envChanges.toString());
	}

	@Test
	public void testAIMA3eFigure3_27() {
		MapEnvironment me = new MapEnvironment(aMap);
		MapAgent ma = new MapAgent(me.getMap(), me, recursiveBestFirstSearch,
				new String[] { SimplifiedRoadMapOfPartOfRomania.BUCHAREST });

		me.addAgent(ma, SimplifiedRoadMapOfPartOfRomania.ARAD);
		me.addEnvironmentView(new TestEnvironmentView());
		me.stepUntilDone();

		Assert.assertEquals(
				"CurrentLocation=In(Arad), Goal=In(Bucharest):Action[name==moveTo, location==Sibiu]:Action[name==moveTo, location==RimnicuVilcea]:Action[name==moveTo, location==Pitesti]:Action[name==moveTo, location==Bucharest]:METRIC[pathCost]=418.0:METRIC[maxRecursiveDepth]=4:METRIC[nodesExpanded]=6:Action[name==NoOp]:",
				envChanges.toString());
	}

	private class TestEnvironmentView implements EnvironmentView {
		public void notify(String msg) {
			envChanges.append(msg).append(":");
		}

		public void agentAdded(Agent agent, EnvironmentState state) {
			// Nothing.
		}

		public void agentActed(Agent agent, Action action,
				EnvironmentState state) {
			envChanges.append(action).append(":");
		}
	}
}
