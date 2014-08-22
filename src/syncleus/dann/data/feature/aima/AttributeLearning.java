package syncleus.dann.data.feature.aima;

/**
 * @author Ravi Mohan
 * 
 */
public interface AttributeLearning {
	void train(AttributeSamples ds);

	/**
	 * Returns the outcome predicted for the specified example
	 * 
	 * @param e
	 *            an example
	 * 
	 * @return the outcome predicted for the specified example
	 */
	String predict(Features e);

	/**
	 * Returns the accuracy of the hypothesis on the specified set of examples
	 * 
	 * @param ds
	 *            the test data set.
	 * 
	 * @return the accuracy of the hypothesis on the specified set of examples
	 */
	int[] test(AttributeSamples ds);
}
