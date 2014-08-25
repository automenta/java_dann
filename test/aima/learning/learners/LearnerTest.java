package aima.test.core.unit.learning.learners;

import java.util.ArrayList;

import org.junit.Assert;
import org.junit.Test;

import syncleus.dann.data.feature.aima.FeatureDataset;
import aima.learning.framework.DataSetFactory;
import syncleus.dann.logic.inductive.DLTest;
import syncleus.dann.logic.inductive.DLTestFactory;
import syncleus.dann.logic.learn.CurrentBestLearner;
import syncleus.dann.logic.learn.DecisionListLearner;
import syncleus.dann.logic.learn.DecisionTreeLearner;
import syncleus.dann.logic.learn.MajorityLearner;
import aima.test.core.unit.learning.framework.MockDataSetSpecification;
import aima.test.core.unit.learning.inductive.MockDLTestFactory;

/**
 * @author Ravi Mohan
 * 
 */
public class LearnerTest {

	@Test
	public void testMajorityLearner() throws Exception {
		MajorityLearner learner = new MajorityLearner();
		FeatureDataset ds = DataSetFactory.getRestaurantDataSet();
		learner.train(ds);
		int[] result = learner.test(ds);
		Assert.assertEquals(6, result[0]);
		Assert.assertEquals(6, result[1]);
	}

	@Test
	public void testDefaultUsedWhenTrainingDataSetHasNoExamples()
			throws Exception {
		// tests RecursionBaseCase#1
		FeatureDataset ds = DataSetFactory.getRestaurantDataSet();
		DecisionTreeLearner learner = new DecisionTreeLearner();

		FeatureDataset ds2 = ds.emptyDataSet();
		Assert.assertEquals(0, ds2.size());

		learner.train(ds2);
		Assert.assertEquals("Unable To Classify",
				learner.predict(ds.get(0)));
	}

	@Test
	public void testClassificationReturnedWhenAllExamplesHaveTheSameClassification()
			throws Exception {
		// tests RecursionBaseCase#2
		FeatureDataset ds = DataSetFactory.getRestaurantDataSet();
		DecisionTreeLearner learner = new DecisionTreeLearner();

		FeatureDataset ds2 = ds.emptyDataSet();

		// all 3 examples have the same classification (willWait = yes)
		ds2.add(ds.get(0));
		ds2.add(ds.get(2));
		ds2.add(ds.get(3));

		learner.train(ds2);
		Assert.assertEquals("Yes", learner.predict(ds.get(0)));
	}

	@Test
	public void testMajorityReturnedWhenAttributesToExamineIsEmpty()
			throws Exception {
		// tests RecursionBaseCase#2
		FeatureDataset ds = DataSetFactory.getRestaurantDataSet();
		DecisionTreeLearner learner = new DecisionTreeLearner();

		FeatureDataset ds2 = ds.emptyDataSet();

		// 3 examples have classification = "yes" and one ,"no"
		ds2.add(ds.get(0));
		ds2.add(ds.get(1));// "no"
		ds2.add(ds.get(2));
		ds2.add(ds.get(3));
		ds2.setSpecification(new MockDataSetSpecification("will_wait"));

		learner.train(ds2);
		Assert.assertEquals("Yes", learner.predict(ds.get(1)));
	}

	@Test
	public void testInducedTreeClassifiesDataSetCorrectly() throws Exception {
		FeatureDataset ds = DataSetFactory.getRestaurantDataSet();
		DecisionTreeLearner learner = new DecisionTreeLearner();
		learner.train(ds);
		int[] result = learner.test(ds);
		Assert.assertEquals(12, result[0]);
		Assert.assertEquals(0, result[1]);
	}

	@Test
	public void testDecisionListLearnerReturnsNegativeDLWhenDataSetEmpty()
			throws Exception {
		// tests first base case of DL Learner
		DecisionListLearner learner = new DecisionListLearner("Yes", "No",
				new MockDLTestFactory(null));
		FeatureDataset ds = DataSetFactory.getRestaurantDataSet();
		FeatureDataset empty = ds.emptyDataSet();
		learner.train(empty);
		Assert.assertEquals("No", learner.predict(ds.get(0)));
		Assert.assertEquals("No", learner.predict(ds.get(1)));
		Assert.assertEquals("No", learner.predict(ds.get(2)));
	}

	@Test
	public void testDecisionListLearnerReturnsFailureWhenTestsEmpty()
			throws Exception {
		// tests second base case of DL Learner
		DecisionListLearner learner = new DecisionListLearner("Yes", "No",
				new MockDLTestFactory(new ArrayList<DLTest>()));
		FeatureDataset ds = DataSetFactory.getRestaurantDataSet();
		learner.train(ds);
		Assert.assertEquals(DecisionListLearner.FAILURE,
				learner.predict(ds.get(0)));
	}

	@Test
	public void testDecisionListTestRunOnRestaurantDataSet() throws Exception {
		FeatureDataset ds = DataSetFactory.getRestaurantDataSet();
		DecisionListLearner learner = new DecisionListLearner("Yes", "No",
				new DLTestFactory());
		learner.train(ds);

		int[] result = learner.test(ds);
		Assert.assertEquals(12, result[0]);
		Assert.assertEquals(0, result[1]);
	}

	@Test
	public void testCurrentBestLearnerOnRestaurantDataSet() throws Exception {
		FeatureDataset ds = DataSetFactory.getRestaurantDataSet();
		CurrentBestLearner learner = new CurrentBestLearner("Yes");
		learner.train(ds);

		int[] result = learner.test(ds);
		Assert.assertEquals(12, result[0]);
		Assert.assertEquals(0, result[1]);
	}
}
