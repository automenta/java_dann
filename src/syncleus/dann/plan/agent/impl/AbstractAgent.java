package syncleus.dann.plan.agent.impl;

import syncleus.dann.plan.agent.Action;
import syncleus.dann.plan.agent.Agent;
import syncleus.dann.plan.agent.AgentProgram;
import syncleus.dann.plan.agent.Percept;

/**
 * @author Ravi Mohan
 * @author Ciaran O'Reilly
 * @author Mike Stampone
 */
public abstract class AbstractAgent implements Agent {

	protected AgentProgram program;
	private boolean alive = true;

	public AbstractAgent() {

	}

	/**
	 * Constructs an Agent with the specified AgentProgram.
	 * 
	 * @param aProgram
	 *            the Agent's program, which maps any given percept sequences to
	 *            an action.
	 */
	public AbstractAgent(AgentProgram aProgram) {
		program = aProgram;
	}

	//
	// START-Agent
	public Action execute(Percept p) {
		if (null != program) {
			return program.execute(p);
		}
		return NoOpAction.NO_OP;
	}

	public boolean isAlive() {
		return alive;
	}

	public void setAlive(boolean alive) {
		this.alive = alive;
	}

	// END-Agent
	//
}