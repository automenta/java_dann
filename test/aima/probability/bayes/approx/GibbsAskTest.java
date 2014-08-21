package aima.test.core.unit.probability.bayes.approx;

import org.junit.Assert;
import org.junit.Test;

import syncleus.dann.learn.probability.ProbabilityModel;
import syncleus.dann.learn.probability.RandomVariable;
import syncleus.dann.learn.probability.bayes.BayesianNetwork;
import syncleus.dann.learn.probability.bayes.approx.GibbsAsk;
import example.probability.BayesNetExampleFactory;
import example.probability.ExampleRV;
import syncleus.dann.learn.probability.proposition.AssignmentProposition;
import syncleus.dann.util.MockRandomizer;

/**
 * 
 * @author Ciaran O'Reilly
 * @author Ravi Mohan
 */
public class GibbsAskTest {
	public static final double DELTA_THRESHOLD = ProbabilityModel.DEFAULT_ROUNDING_THRESHOLD;

	@Test
	public void testGibbsAsk_basic() {
		BayesianNetwork bn = BayesNetExampleFactory
				.constructCloudySprinklerRainWetGrassNetwork();
		AssignmentProposition[] e = new AssignmentProposition[] { new AssignmentProposition(
				ExampleRV.SPRINKLER_RV, Boolean.TRUE) };
		MockRandomizer r = new MockRandomizer(new double[] { 0.5, 0.5, 0.5,
				0.5, 0.5, 0.5, 0.6, 0.5, 0.5, 0.6, 0.5, 0.5 });

		GibbsAsk ga = new GibbsAsk(r);

		double[] estimate = ga.gibbsAsk(
				new RandomVariable[] { ExampleRV.RAIN_RV }, e, bn, 3)
				.getValues();

		Assert.assertArrayEquals(new double[] { 0.3333333333333333,
				0.6666666666666666 }, estimate, DELTA_THRESHOLD);
	}
}
