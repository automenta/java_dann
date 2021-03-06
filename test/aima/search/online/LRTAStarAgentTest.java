package aima.test.core.unit.search.online;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import syncleus.dann.plan.agent.Action;
import syncleus.dann.plan.agent.Agent;
import syncleus.dann.plan.agent.EnvironmentState;
import syncleus.dann.plan.agent.EnvironmentView;
import syncleus.dann.solve.map.ExtendableMap;
import syncleus.dann.solve.map.MapEnvironment;
import syncleus.dann.solve.map.MapFunctionFactory;
import syncleus.dann.solve.map.MapStepCostFunction;
import syncleus.dann.search.framework.DefaultGoalTest;
import syncleus.dann.search.framework.HeuristicFunction;
import syncleus.dann.search.online.LRTAStarAgent;
import syncleus.dann.search.online.OnlineSearchProblem;

public class LRTAStarAgentTest {
	ExtendableMap aMap;

	StringBuffer envChanges;

	HeuristicFunction hf;

	@Before
	public void setUp() {
		aMap = new ExtendableMap();
		aMap.addBidirectionalLink("A", "B", 4.0);
		aMap.addBidirectionalLink("B", "C", 4.0);
		aMap.addBidirectionalLink("C", "D", 4.0);
		aMap.addBidirectionalLink("D", "E", 4.0);
		aMap.addBidirectionalLink("E", "F", 4.0);
		hf = new HeuristicFunction() {
			public double h(Object state) {
				return 1;
			}
		};

		envChanges = new StringBuffer();
	}

	@Test
	public void testAlreadyAtGoal() {
		MapEnvironment me = new MapEnvironment(aMap);
		LRTAStarAgent agent = new LRTAStarAgent(new OnlineSearchProblem(
				MapFunctionFactory.getActionsFunction(aMap),
				new DefaultGoalTest("A"), new MapStepCostFunction(aMap)),
				MapFunctionFactory.getPerceptToStateFunction(), hf);
		me.addAgent(agent, "A");
		me.addEnvironmentView(new TestEnvironmentView());
		me.stepUntilDone();

		Assert.assertEquals("Action[name==NoOp]->", envChanges.toString());
	}

	@Test
	public void testNormalSearch() {
		MapEnvironment me = new MapEnvironment(aMap);
		LRTAStarAgent agent = new LRTAStarAgent(new OnlineSearchProblem(
				MapFunctionFactory.getActionsFunction(aMap),
				new DefaultGoalTest("F"), new MapStepCostFunction(aMap)),
				MapFunctionFactory.getPerceptToStateFunction(), hf);
		me.addAgent(agent, "A");
		me.addEnvironmentView(new TestEnvironmentView());
		me.stepUntilDone();

		Assert.assertEquals(
				"Action[name==moveTo, location==B]->Action[name==moveTo, location==A]->Action[name==moveTo, location==B]->Action[name==moveTo, location==C]->Action[name==moveTo, location==B]->Action[name==moveTo, location==C]->Action[name==moveTo, location==D]->Action[name==moveTo, location==C]->Action[name==moveTo, location==D]->Action[name==moveTo, location==E]->Action[name==moveTo, location==D]->Action[name==moveTo, location==E]->Action[name==moveTo, location==F]->Action[name==NoOp]->",
				envChanges.toString());
	}

	@Test
	public void testNoPath() {
		MapEnvironment me = new MapEnvironment(aMap);
		LRTAStarAgent agent = new LRTAStarAgent(new OnlineSearchProblem(
				MapFunctionFactory.getActionsFunction(aMap),
				new DefaultGoalTest("G"), new MapStepCostFunction(aMap)),
				MapFunctionFactory.getPerceptToStateFunction(), hf);
		me.addAgent(agent, "A");
		me.addEnvironmentView(new TestEnvironmentView());
		// Note: Will search forever if no path is possible,
		// Therefore restrict the number of steps to something
		// reasonablbe, against which to test.
		me.step(14);

		Assert.assertEquals(
				"Action[name==moveTo, location==B]->Action[name==moveTo, location==A]->Action[name==moveTo, location==B]->Action[name==moveTo, location==C]->Action[name==moveTo, location==B]->Action[name==moveTo, location==C]->Action[name==moveTo, location==D]->Action[name==moveTo, location==C]->Action[name==moveTo, location==D]->Action[name==moveTo, location==E]->Action[name==moveTo, location==D]->Action[name==moveTo, location==E]->Action[name==moveTo, location==F]->Action[name==moveTo, location==E]->",
				envChanges.toString());
	}

	private class TestEnvironmentView implements EnvironmentView {
		public void notify(String msg) {
			envChanges.append(msg).append("->");
		}

		public void agentAdded(Agent agent, EnvironmentState state) {
			// Nothing.
		}

		public void agentActed(Agent agent, Action action,
				EnvironmentState state) {
			envChanges.append(action).append("->");
		}
	}
}
