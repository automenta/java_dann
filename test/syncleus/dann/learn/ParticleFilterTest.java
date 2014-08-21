package syncleus.dann.learn;

import aima.probability.RandomVariable;
import aima.probability.bayes.FiniteNode;
import aima.probability.bayes.approx.ParticleFiltering;
import aima.probability.bayes.impl.BayesNet;
import aima.probability.bayes.impl.DynamicBayesNet;
import aima.probability.bayes.impl.FullCPTNode;
import org.junit.Assert;
import org.junit.Test;

import aima.probability.example.DynamicBayesNetExampleFactory;
import aima.probability.example.ExampleRV;
import aima.probability.proposition.AssignmentProposition;
import aima.util.MockRandomizer;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author Ciaran O'Reilly
 * @author Ravi Mohan
 */
public class ParticleFilterTest {

	@Test
	public void test_AIMA3e_Fig15_18() {
		MockRandomizer mr = new MockRandomizer(new double[] {
				// Prior Sample:
				// 8 with Rain_t-1=true from prior distribution
				0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5,
				// 2 with Rain_t-1=false from prior distribution
				0.6, 0.6,
				// (a) Propagate 6 samples Rain_t=true
				0.7, 0.7, 0.7, 0.7, 0.7, 0.7,
				// 4 samples Rain_t=false
				0.71, 0.71, 0.31, 0.31,
				// (b) Weight should be for first 6 samples:
				// Rain_t-1=true, Rain_t=true, Umbrella_t=false = 0.1
				// Next 2 samples:
				// Rain_t-1=true, Rain_t=false, Umbrealla_t=false= 0.8
				// Final 2 samples:
				// Rain_t-1=false, Rain_t=false, Umbrella_t=false = 0.8
				// gives W[] =
				// [0.1, 0.1, 0.1, 0.1, 0.1, 0.1, 0.8, 0.8, 0.8, 0.8]
				// normalized =
				// [0.026, ...., 0.211, ....] is approx. 0.156 = true
				// the remainder is false
				// (c) Resample 2 Rain_t=true, 8 Rain_t=false
				0.15, 0.15, 0.2, 0.2, 0.2, 0.2, 0.2, 0.2, 0.2, 0.2,
				//
				// Next Sample:
				// (a) Propagate 1 samples Rain_t=true
				0.7,
				// 9 samples Rain_t=false
				0.71, 0.31, 0.31, 0.31, 0.31, 0.31, 0.31, 0.31, 0.31,
				// (c) resample 1 Rain_t=true, 9 Rain_t=false
				0.0001, 0.2, 0.2, 0.2, 0.2, 0.2, 0.2, 0.2, 0.2, 0.2 });

		int N = 10;
        FiniteNode prior_rain_tm1 = new FullCPTNode(ExampleRV.RAIN_tm1_RV, new double[]{0.5, 0.5});
        BayesNet priorNetwork = new BayesNet(prior_rain_tm1);
        FiniteNode rain_tm1 = new FullCPTNode(ExampleRV.RAIN_tm1_RV, new double[]{0.5, 0.5});
        FiniteNode rain_t = new FullCPTNode(ExampleRV.RAIN_t_RV, new double[]{0.7, 0.3, 0.3, 0.7}, rain_tm1);
        @SuppressWarnings(value = "unused")
        FiniteNode umbrealla_t = new FullCPTNode(ExampleRV.UMBREALLA_t_RV, new double[]{0.9, 0.1, 0.2, 0.8}, rain_t);
        Map<RandomVariable, RandomVariable> X_0_to_X_1 = new HashMap<RandomVariable, RandomVariable>();
        X_0_to_X_1.put(ExampleRV.RAIN_tm1_RV, ExampleRV.RAIN_t_RV);
        Set<RandomVariable> E_1 = new HashSet<RandomVariable>();
        E_1.add(ExampleRV.UMBREALLA_t_RV);
		ParticleFiltering pf = new ParticleFiltering(N, new DynamicBayesNet(priorNetwork, X_0_to_X_1, E_1, rain_tm1), mr);

		AssignmentProposition[] e = new AssignmentProposition[] { new AssignmentProposition(
				ExampleRV.UMBREALLA_t_RV, false) };

		AssignmentProposition[][] S = pf.particleFiltering(e);

		Assert.assertEquals(N, S.length);
		for (int i = 0; i < N; i++) {
			Assert.assertEquals(1, S[i].length);
			AssignmentProposition ap = S[i][0];
			Assert.assertEquals(ExampleRV.RAIN_t_RV, ap.getTermVariable());
			if (i < 2) {
				Assert.assertEquals(true, ap.getValue());
			} else {
				Assert.assertEquals(false, ap.getValue());
			}
		}

		// Generate next sample to ensure everything roles forward ok
		// in this case with prefixed probabilities only expect 1 Rain_t=true
		S = pf.particleFiltering(e);
		Assert.assertEquals(N, S.length);
		for (int i = 0; i < N; i++) {
			Assert.assertEquals(1, S[i].length);
			AssignmentProposition ap = S[i][0];
			Assert.assertEquals(ExampleRV.RAIN_t_RV, ap.getTermVariable());
			if (i < 1) {
				Assert.assertEquals(true, ap.getValue());
			} else {
				Assert.assertEquals(false, ap.getValue());
			}
		}
	}
}
