package syncleus.dann.solve.vacuum;

import syncleus.dann.plan.agent.Action;
import syncleus.dann.plan.agent.Agent;
import syncleus.dann.plan.agent.EnvironmentState;
import syncleus.dann.plan.agent.EnvironmentView;

public class VacuumEnvironmentViewActionTracker implements EnvironmentView {
	private StringBuilder actions = null;

	public VacuumEnvironmentViewActionTracker(StringBuilder envChanges) {
		this.actions = envChanges;
	}

	//
	// START-EnvironmentView
	public void notify(String msg) {
		// Do nothing by default.
	}

	public void agentAdded(Agent agent, EnvironmentState state) {
		// Do nothing by default.
	}

	public void agentActed(Agent agent, Action action, EnvironmentState state) {
		actions.append(action);
	}

	// END-EnvironmentView
	//
}
