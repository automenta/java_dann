package aima.test.core.unit.learning.learners;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import syncleus.dann.attribute.aima.DataSet;
import aima.learning.framework.DataSetFactory;
import syncleus.dann.attribute.aima.Learner;
import syncleus.dann.logic.inductive.DecisionTree;
import syncleus.dann.logic.learn.AdaBoostLearner;
import syncleus.dann.logic.learn.StumpLearner;

/**
 * @author Ravi Mohan
 * 
 */
public class EnsembleLearningTest {

	private static final String YES = "Yes";

	@Test
	public void testAdaBoostEnablesCollectionOfStumpsToClassifyDataSetAccurately()
			throws Exception {
		DataSet ds = DataSetFactory.getRestaurantDataSet();
		List<DecisionTree> stumps = DecisionTree.getStumpsFor(ds, YES, "No");
		List<Learner> learners = new ArrayList<Learner>();
		for (Object stump : stumps) {
			DecisionTree sl = (DecisionTree) stump;
			StumpLearner stumpLearner = new StumpLearner(sl, "No");
			learners.add(stumpLearner);
		}
		AdaBoostLearner learner = new AdaBoostLearner(learners, ds);
		learner.train(ds);
		int[] result = learner.test(ds);
		Assert.assertEquals(12, result[0]);
		Assert.assertEquals(0, result[1]);
	}
}
