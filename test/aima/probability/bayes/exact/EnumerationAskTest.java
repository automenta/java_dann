package aima.test.core.unit.probability.bayes.exact;

import org.junit.Before;

import syncleus.dann.learn.probability.bayes.exact.EnumerationAsk;

/**
 * 
 * @author Ciaran O'Reilly
 * @author Ravi Mohan
 */
public class EnumerationAskTest extends BayesianInferenceTest {

	@Before
	public void setUp() {
		bayesInference = new EnumerationAsk();
	}
}
