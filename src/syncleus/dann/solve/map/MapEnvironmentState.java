package syncleus.dann.solve.map;

import java.util.HashMap;

import syncleus.dann.plan.agent.Agent;
import syncleus.dann.plan.agent.EnvironmentState;
import syncleus.dann.util.datastruct.Pair;

/**
 * @author Ciaran O'Reilly
 * 
 */
public class MapEnvironmentState implements EnvironmentState {
	private java.util.Map<Agent, Pair<String, Double>> agentLocationAndTravelDistance = new HashMap<Agent, Pair<String, Double>>();

	public MapEnvironmentState() {

	}

	public String getAgentLocation(Agent a) {
		Pair<String, Double> locAndTDistance = agentLocationAndTravelDistance
				.get(a);
		if (null == locAndTDistance) {
			return null;
		}
		return locAndTDistance.getFirst();
	}

	public Double getAgentTravelDistance(Agent a) {
		Pair<String, Double> locAndTDistance = agentLocationAndTravelDistance
				.get(a);
		if (null == locAndTDistance) {
			return null;
		}
		return locAndTDistance.getSecond();
	}

	public void setAgentLocationAndTravelDistance(Agent a, String location,
			Double travelDistance) {
		agentLocationAndTravelDistance.put(a, new Pair<String, Double>(
				location, travelDistance));
	}
}
