package aima.test.core.unit.probability.bayes.approx;

import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import syncleus.dann.learn.probability.RandomVariable;
import syncleus.dann.learn.probability.bayes.BayesianNetwork;
import syncleus.dann.learn.probability.bayes.approx.PriorSample;
import example.probability.BayesNetExampleFactory;
import example.probability.ExampleRV;
import syncleus.dann.util.MockRandomizer;

/**
 * 
 * @author Ciaran O'Reilly
 * @author Ravi Mohan
 */
public class PriorSampleTest {

	@Test
	public void testPriorSample_basic() {
		// AIMA3e pg. 530
		BayesianNetwork bn = BayesNetExampleFactory
				.constructCloudySprinklerRainWetGrassNetwork();
		MockRandomizer r = new MockRandomizer(
				new double[] { 0.5, 0.5, 0.5, 0.5 });

		PriorSample ps = new PriorSample(r);
		Map<RandomVariable, Object> event = ps.priorSample(bn);

		Assert.assertEquals(4, event.keySet().size());
		Assert.assertEquals(Boolean.TRUE, event.get(ExampleRV.CLOUDY_RV));
		Assert.assertEquals(Boolean.FALSE, event.get(ExampleRV.SPRINKLER_RV));
		Assert.assertEquals(Boolean.TRUE, event.get(ExampleRV.RAIN_RV));
		Assert.assertEquals(Boolean.TRUE, event.get(ExampleRV.WET_GRASS_RV));
	}
}
