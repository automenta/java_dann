package syncleus.dann.solve.map;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import syncleus.dann.plan.agent.Action;
import syncleus.dann.plan.agent.EnvironmentViewNotifier;
import syncleus.dann.plan.agent.Percept;
import syncleus.dann.plan.agent.State;
import syncleus.dann.plan.agent.impl.DynamicPercept;
import syncleus.dann.plan.agent.impl.DynamicState;
import syncleus.dann.search.framework.Problem;
import syncleus.dann.search.framework.Search;
import syncleus.dann.search.framework.SimpleProblemSolvingAgent;

/**
 * @author Ciaran O'Reilly
 * 
 */
public class MapAgent extends SimpleProblemSolvingAgent {
	private Map map = null;

	private EnvironmentViewNotifier notifier = null;

	private DynamicState state = new DynamicState();

	private Search search = null;

	private String[] goalTests = null;

	private int goalTestPos = 0;

	public MapAgent(Map map, EnvironmentViewNotifier notifier, Search search) {
		this.map = map;
		this.notifier = notifier;
		this.search = search;
	}

	public MapAgent(Map map, EnvironmentViewNotifier notifier, Search search,
			int maxGoalsToFormulate) {
		super(maxGoalsToFormulate);
		this.map = map;
		this.notifier = notifier;
		this.search = search;
	}

	public MapAgent(Map map, EnvironmentViewNotifier notifier, Search search,
			String[] goalTests) {
		super(goalTests.length);
		this.map = map;
		this.notifier = notifier;
		this.search = search;
		this.goalTests = new String[goalTests.length];
		System.arraycopy(goalTests, 0, this.goalTests, 0, goalTests.length);
	}

	//
	// PROTECTED METHODS
	//
	@Override
	protected State updateState(Percept p) {
		DynamicPercept dp = (DynamicPercept) p;

		state.setAttribute(DynAttributeNames.AGENT_LOCATION,
				dp.getAttribute(DynAttributeNames.PERCEPT_IN));

		return state;
	}

	@Override
	protected Object formulateGoal() {
		Object goal = null;
		if (null == goalTests) {
			goal = map.randomlyGenerateDestination();
		} else {
			goal = goalTests[goalTestPos];
			goalTestPos++;
		}
		notifier.notifyViews("CurrentLocation=In("
				+ state.getAttribute(DynAttributeNames.AGENT_LOCATION)
				+ "), Goal=In(" + goal + ")");

		return goal;
	}

	@Override
	protected Problem formulateProblem(Object goal) {
		return new BidirectionalMapProblem(map,
				(String) state.getAttribute(DynAttributeNames.AGENT_LOCATION),
				(String) goal);
	}

	@Override
	protected List<Action> search(Problem problem) {
		List<Action> actions = new ArrayList<Action>();
		try {
			List<Action> sactions = search.search(problem);
			for (Action action : sactions) {
				actions.add(action);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return actions;
	}

	@Override
	protected void notifyViewOfMetrics() {
		Set<String> keys = search.getMetrics().keySet();
		for (String key : keys) {
			notifier.notifyViews("METRIC[" + key + "]="
					+ search.getMetrics().get(key));
		}
	}
}
