package aima.test.core.unit.probability.bayes.exact;

import org.junit.Before;

import syncleus.dann.learn.probability.bayes.exact.EliminationAsk;

/**
 * 
 * @author Ciaran O'Reilly
 */
public class EliminationAskTest extends BayesianInferenceTest {

	@Before
	public void setUp() {
		bayesInference = new EliminationAsk();
	}
}
