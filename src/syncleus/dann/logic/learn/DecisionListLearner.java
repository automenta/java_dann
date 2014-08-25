package syncleus.dann.logic.learn;

import java.util.List;

import syncleus.dann.data.feature.aima.FeatureDataset;
import syncleus.dann.data.feature.aima.Features;
import syncleus.dann.data.feature.aima.FeatureLearning;
import syncleus.dann.logic.inductive.DLTest;
import syncleus.dann.logic.inductive.DLTestFactory;
import syncleus.dann.logic.inductive.DecisionList;

/**
 * @author Ravi Mohan
 * @author Mike Stampone
 */
public class DecisionListLearner implements FeatureLearning {
	public static final String FAILURE = "Failure";

	private DecisionList decisionList;

	private String positive, negative;

	private DLTestFactory testFactory;

	public DecisionListLearner(String positive, String negative,
			DLTestFactory testFactory) {
		this.positive = positive;
		this.negative = negative;
		this.testFactory = testFactory;
	}

	//
	// START-Learner

	/**
	 * Induces the decision list from the specified set of examples
	 * 
	 * @param ds
	 *            a set of examples for constructing the decision list
	 */
	@Override
	public void train(FeatureDataset ds) {
		this.decisionList = decisionListLearning(ds);
	}

	@Override
	public String predict(Features e) {
		if (decisionList == null) {
			throw new RuntimeException(
					"learner has not been trained with dataset yet!");
		}
		return decisionList.predict(e);
	}

	@Override
	public int[] test(FeatureDataset ds) {
		int[] results = new int[] { 0, 0 };

		for (Features e : ds.samples) {
			if (e.targetValue().equals(decisionList.predict(e))) {
				results[0] = results[0] + 1;
			} else {
				results[1] = results[1] + 1;
			}
		}
		return results;
	}

	// END-Learner
	//

	/**
	 * Returns the decision list of this decision list learner
	 * 
	 * @return the decision list of this decision list learner
	 */
	public DecisionList getDecisionList() {
		return decisionList;
	}

	//
	// PRIVATE METHODS
	//
	private DecisionList decisionListLearning(FeatureDataset ds) {
		if (ds.size() == 0) {
			return new DecisionList(positive, negative);
		}
		List<DLTest> possibleTests = testFactory
				.createDLTestsWithAttributeCount(ds, 1);
		DLTest test = getValidTest(possibleTests, ds);
		if (test == null) {
			return new DecisionList(null, FAILURE);
		}
		// at this point there is a test that classifies some subset of examples
		// with the same target value
		FeatureDataset matched = test.matchedExamples(ds);
		DecisionList list = new DecisionList(positive, negative);
		list.add(test, matched.get(0).targetValue());
		return list.mergeWith(decisionListLearning(test.unmatchedExamples(ds)));
	}

	private DLTest getValidTest(List<DLTest> possibleTests, FeatureDataset ds) {
		for (DLTest test : possibleTests) {
			FeatureDataset matched = test.matchedExamples(ds);
			if (!(matched.size() == 0)) {
				if (allExamplesHaveSameTargetValue(matched)) {
					return test;
				}
			}

		}
		return null;
	}

	private boolean allExamplesHaveSameTargetValue(FeatureDataset matched) {
		// assumes at least i example in dataset
		String targetValue = matched.get(0).targetValue();
		for (Features e : matched.samples) {
			if (!(e.targetValue().equals(targetValue))) {
				return false;
			}
		}
		return true;
	}
}
