package syncleus.dann.search.framework;

import syncleus.dann.plan.agent.impl.DynamicAction;

/**
 * A NoOp action that indicates a CutOff has occurred in a search. Used
 * primarily by DepthLimited and IterativeDeepening search routines.
 * 
 * @author Ciaran O'Reilly
 */
public class CutOffIndicatorAction extends DynamicAction {
	public static final CutOffIndicatorAction CUT_OFF = new CutOffIndicatorAction();

	//
	// START-Action
	public boolean isNoOp() {
		return true;
	}

	// END-Action
	//

	private CutOffIndicatorAction() {
		super("CutOff");
	}
}