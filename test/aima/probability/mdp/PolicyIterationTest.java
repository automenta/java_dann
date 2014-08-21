package aima.test.core.unit.probability.mdp;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import syncleus.dann.solve.cellworld.Cell;
import syncleus.dann.solve.cellworld.CellWorld;
import syncleus.dann.solve.cellworld.CellWorldAction;
import syncleus.dann.solve.cellworld.CellWorldFactory;
import aima.probability.example.MDPFactory;
import aima.probability.mdp.MarkovDecisionProcess;
import aima.probability.mdp.Policy;
import aima.probability.mdp.impl.ModifiedPolicyEvaluation;
import aima.probability.mdp.search.PolicyIteration;

/**
 * 
 * @author Ravi Mohan
 * @author Ciaran O'Reilly
 */
public class PolicyIterationTest {
	private CellWorld<Double> cw = null;
	private MarkovDecisionProcess<Cell<Double>, CellWorldAction> mdp = null;
	private PolicyIteration<Cell<Double>, CellWorldAction> pi = null;

	@Before
	public void setUp() {
		cw = CellWorldFactory.createCellWorldForFig17_1();
		mdp = MDPFactory.createMDPForFigure17_3(cw);
		pi = new PolicyIteration<Cell<Double>, CellWorldAction>(
				new ModifiedPolicyEvaluation<Cell<Double>, CellWorldAction>(50, 1.0));
	}

	@Test
	public void testPolicyIterationForFig17_2() {

		// AIMA3e check with Figure 17.2 (a)
		Policy<Cell<Double>, CellWorldAction> policy = pi.policyIteration(mdp);

		Assert.assertEquals(CellWorldAction.Up,
				policy.action(cw.getCellAt(1, 1)));
		Assert.assertEquals(CellWorldAction.Up,
				policy.action(cw.getCellAt(1, 2)));
		Assert.assertEquals(CellWorldAction.Right,
				policy.action(cw.getCellAt(1, 3)));

		Assert.assertEquals(CellWorldAction.Left,
				policy.action(cw.getCellAt(2, 1)));
		Assert.assertEquals(CellWorldAction.Right,
				policy.action(cw.getCellAt(2, 3)));

		Assert.assertEquals(CellWorldAction.Left,
				policy.action(cw.getCellAt(3, 1)));
		Assert.assertEquals(CellWorldAction.Up,
				policy.action(cw.getCellAt(3, 2)));
		Assert.assertEquals(CellWorldAction.Right,
				policy.action(cw.getCellAt(3, 3)));

		Assert.assertEquals(CellWorldAction.Left,
				policy.action(cw.getCellAt(4, 1)));
		Assert.assertNull(policy.action(cw.getCellAt(4, 2)));
		Assert.assertNull(policy.action(cw.getCellAt(4, 3)));
	}
}
