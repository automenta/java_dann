package aima.test.core.unit.probability.hmm.exact;

import org.junit.Before;
import org.junit.Test;

import example.probability.HMMExampleFactory;
import syncleus.dann.learn.probability.hmm.exact.HMMForwardBackwardConstantSpace;
import aima.test.core.unit.probability.temporal.CommonForwardBackwardTest;

/**
 * 
 * @author Ciaran O'Reilly
 *
 */
public class HMMForwardBackwardConstantSpaceTest extends
		CommonForwardBackwardTest {

	//
	private HMMForwardBackwardConstantSpace uw = null;

	@Before
	public void setUp() {
		uw = new HMMForwardBackwardConstantSpace(
				HMMExampleFactory.getUmbrellaWorldModel());
	}

	@Test
	public void testForwardBackward_UmbrellaWorld() {
		super.testForwardBackward_UmbrellaWorld(uw);
	}
}
