package syncleus.dann.plan.agent.impl;

import syncleus.dann.plan.agent.Action;
import syncleus.dann.plan.agent.Agent;
import syncleus.dann.plan.agent.EnvironmentState;
import syncleus.dann.plan.agent.EnvironmentView;

/**
 * Simple environment view which uses the standard output stream to inform about
 * relevant events.
 * 
 * @author Ruediger Lunde
 */
public class SimpleEnvironmentView implements EnvironmentView {
	@Override
	public void agentActed(Agent agent, Action action,
			EnvironmentState resultingState) {
		System.out.println("Agent acted: " + action.toString());
	}

	@Override
	public void agentAdded(Agent agent, EnvironmentState resultingState) {
		System.out.println("Agent added.");
	}

	@Override
	public void notify(String msg) {
		System.out.println("Message: " + msg);
	}
}
