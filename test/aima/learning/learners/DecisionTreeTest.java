package aima.test.core.unit.learning.learners;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import syncleus.dann.data.feature.aima.FeatureDataset;
import aima.learning.framework.DataSetFactory;
import syncleus.dann.logic.inductive.DecisionTree;
import syncleus.dann.logic.learn.DecisionTreeLearner;
import syncleus.dann.util.AimaUtil;

/**
 * @author Ravi Mohan
 * 
 */
public class DecisionTreeTest {
	private static final String YES = "Yes";

	private static final String NO = "No";

	@Test
	public void testActualDecisionTreeClassifiesRestaurantDataSetCorrectly()
			throws Exception {
		DecisionTreeLearner learner = new DecisionTreeLearner(
				createActualRestaurantDecisionTree(), "Unable to clasify");
		int[] results = learner.test(DataSetFactory.getRestaurantDataSet());
		Assert.assertEquals(12, results[0]);
		Assert.assertEquals(0, results[1]);
	}

	@Test
	public void testInducedDecisionTreeClassifiesRestaurantDataSetCorrectly()
			throws Exception {
		DecisionTreeLearner learner = new DecisionTreeLearner(
				createInducedRestaurantDecisionTree(), "Unable to clasify");
		int[] results = learner.test(DataSetFactory.getRestaurantDataSet());
		Assert.assertEquals(12, results[0]);
		Assert.assertEquals(0, results[1]);
	}

	@Test
	public void testStumpCreationForSpecifiedAttributeValuePair()
			throws Exception {
		FeatureDataset ds = DataSetFactory.getRestaurantDataSet();
		List<String> unmatchedValues = new ArrayList<String>();
		unmatchedValues.add(NO);
		DecisionTree dt = DecisionTree.getStumpFor(ds, "alternate", YES, YES,
				unmatchedValues, NO);
		Assert.assertNotNull(dt);
	}

	@Test
	public void testStumpCreationForDataSet() throws Exception {
		FeatureDataset ds = DataSetFactory.getRestaurantDataSet();
		List<DecisionTree> dt = DecisionTree.getStumpsFor(ds, YES,
				"Unable to classify");
		Assert.assertEquals(26, dt.size());
	}

	@Test
	public void testStumpPredictionForDataSet() throws Exception {
		FeatureDataset ds = DataSetFactory.getRestaurantDataSet();

		List<String> unmatchedValues = new ArrayList<String>();
		unmatchedValues.add(NO);
		DecisionTree tree = DecisionTree.getStumpFor(ds, "hungry", YES, YES,
				unmatchedValues, "Unable to Classify");
		DecisionTreeLearner learner = new DecisionTreeLearner(tree,
				"Unable to Classify");
		int[] result = learner.test(ds);
		Assert.assertEquals(5, result[0]);
		Assert.assertEquals(7, result[1]);
	}

	//
	// PRIVATE METHODS
	//
	private static DecisionTree createInducedRestaurantDecisionTree() {
		// from AIMA 2nd ED
		// Fig 18.6
		// friday saturday node
		DecisionTree frisat = new DecisionTree("fri/sat");
		frisat.addLeaf(AimaUtil.YES, AimaUtil.YES);
		frisat.addLeaf(AimaUtil.NO, AimaUtil.NO);

		// type node
		DecisionTree type = new DecisionTree("type");
		type.addLeaf("French", AimaUtil.YES);
		type.addLeaf("Italian", AimaUtil.NO);
		type.addNode("Thai", frisat);
		type.addLeaf("Burger", AimaUtil.YES);

		// hungry node
		DecisionTree hungry = new DecisionTree("hungry");
		hungry.addLeaf(AimaUtil.NO, AimaUtil.NO);
		hungry.addNode(AimaUtil.YES, type);

		// patrons node
		DecisionTree patrons = new DecisionTree("patrons");
		patrons.addLeaf("None", AimaUtil.NO);
		patrons.addLeaf("Some", AimaUtil.YES);
		patrons.addNode("Full", hungry);

		return patrons;
	}

	private static DecisionTree createActualRestaurantDecisionTree() {
		// from AIMA 2nd ED
		// Fig 18.2

		// raining node
		DecisionTree raining = new DecisionTree("raining");
		raining.addLeaf(AimaUtil.YES, AimaUtil.YES);
		raining.addLeaf(AimaUtil.NO, AimaUtil.NO);

		// bar node
		DecisionTree bar = new DecisionTree("bar");
		bar.addLeaf(AimaUtil.YES, AimaUtil.YES);
		bar.addLeaf(AimaUtil.NO, AimaUtil.NO);

		// friday saturday node
		DecisionTree frisat = new DecisionTree("fri/sat");
		frisat.addLeaf(AimaUtil.YES, AimaUtil.YES);
		frisat.addLeaf(AimaUtil.NO, AimaUtil.NO);

		// second alternate node to the right of the diagram below hungry
		DecisionTree alternate2 = new DecisionTree("alternate");
		alternate2.addNode(AimaUtil.YES, raining);
		alternate2.addLeaf(AimaUtil.NO, AimaUtil.YES);

		// reservation node
		DecisionTree reservation = new DecisionTree("reservation");
		frisat.addNode(AimaUtil.NO, bar);
		frisat.addLeaf(AimaUtil.YES, AimaUtil.YES);

		// first alternate node to the left of the diagram below waitestimate
		DecisionTree alternate1 = new DecisionTree("alternate");
		alternate1.addNode(AimaUtil.NO, reservation);
		alternate1.addNode(AimaUtil.YES, frisat);

		// hungry node
		DecisionTree hungry = new DecisionTree("hungry");
		hungry.addLeaf(AimaUtil.NO, AimaUtil.YES);
		hungry.addNode(AimaUtil.YES, alternate2);

		// wait estimate node
		DecisionTree waitEstimate = new DecisionTree("wait_estimate");
		waitEstimate.addLeaf(">60", AimaUtil.NO);
		waitEstimate.addNode("30-60", alternate1);
		waitEstimate.addNode("10-30", hungry);
		waitEstimate.addLeaf("0-10", AimaUtil.YES);

		// patrons node
		DecisionTree patrons = new DecisionTree("patrons");
		patrons.addLeaf("None", AimaUtil.NO);
		patrons.addLeaf("Some", AimaUtil.YES);
		patrons.addNode("Full", waitEstimate);

		return patrons;
	}
}
