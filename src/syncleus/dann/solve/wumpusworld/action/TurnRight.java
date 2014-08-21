package syncleus.dann.solve.wumpusworld.action;

import syncleus.dann.plan.agent.impl.DynamicAction;
import syncleus.dann.solve.wumpusworld.AgentPosition;

/**
 * Artificial Intelligence A Modern Approach (3rd Edition): page 237.<br>
 * <br>
 * The agent can TurnRight by 90 degrees.
 * 
 * @author Federico Baron
 * @author Alessandro Daniele
 * @author Ciaran O'Reilly
 */
public class TurnRight extends DynamicAction {
	public static final String TURN_RIGHT_ACTION_NAME = "TurnRight";
	public static final String ATTRIBUTE_TO_ORIENTATION = "toOrientation";
	//
	private AgentPosition.Orientation toOrientation;

	/**
	 * Constructor.
	 * 
	 * @param currentOrientation
	 */
	public TurnRight(AgentPosition.Orientation currentOrientation) {
		super(TURN_RIGHT_ACTION_NAME);
		
		switch(currentOrientation) {
		case FACING_NORTH:
			toOrientation = AgentPosition.Orientation.FACING_EAST;
			break;
		case FACING_SOUTH:
			toOrientation = AgentPosition.Orientation.FACING_WEST;
			break;
		case FACING_EAST:
			toOrientation = AgentPosition.Orientation.FACING_SOUTH;
			break;
		case FACING_WEST:
			toOrientation = AgentPosition.Orientation.FACING_NORTH;
			break;
		}
		setAttribute(ATTRIBUTE_TO_ORIENTATION, toOrientation);
	}
	
	/**
	 * 
	 * @return the orientation the agent should be after the action occurred.
	 *         <b>Note:<b> this may not be a legal orientation within the
	 *         environment in which the action was performed and this should be
	 *         checked for.
	 */
	public AgentPosition.Orientation getToOrientation() {
		return toOrientation;
	}
}
