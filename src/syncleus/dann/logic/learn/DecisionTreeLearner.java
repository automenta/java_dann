package syncleus.dann.logic.learn;

import java.util.Iterator;
import java.util.List;
import syncleus.dann.data.feature.aima.FeatureDataset;
import syncleus.dann.data.feature.aima.FeatureLearning;
import syncleus.dann.data.feature.aima.Features;
import syncleus.dann.logic.inductive.ConstantDecisonTree;
import syncleus.dann.logic.inductive.DecisionTree;
import syncleus.dann.util.AimaUtil;

/**
 * @author Ravi Mohan
 * @author Mike Stampone
 */
public class DecisionTreeLearner implements FeatureLearning {
	private DecisionTree tree;

	private String defaultValue;

	public DecisionTreeLearner() {
		this.defaultValue = "Unable To Classify";

	}

	// used when you have to test a non induced tree (eg: for testing)
	public DecisionTreeLearner(DecisionTree tree, String defaultValue) {
		this.tree = tree;
		this.defaultValue = defaultValue;
	}

	//
	// START-Learner

	/**
	 * Induces the decision tree from the specified set of examples
	 * 
	 * @param ds
	 *            a set of examples for constructing the decision tree
	 */
	@Override
	public void train(FeatureDataset ds) {
		List<String> attributes = ds.getNonTargetFeatures();
		this.tree = decisionTreeLearning(ds, attributes,
				new ConstantDecisonTree(defaultValue));
	}

	@Override
	public String predict(Features e) {
		return (String) tree.predict(e);
	}

	@Override
	public int[] test(FeatureDataset ds) {
		int[] results = new int[] { 0, 0 };

		for (Features e : ds.samples) {
			if (e.targetValue().equals(tree.predict(e))) {
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
	 * Returns the decision tree of this decision tree learner
	 * 
	 * @return the decision tree of this decision tree learner
	 */
	public DecisionTree getDecisionTree() {
		return tree;
	}

	//
	// PRIVATE METHODS
	//

	private DecisionTree decisionTreeLearning(FeatureDataset ds,
			List<String> attributeNames, ConstantDecisonTree defaultTree) {
		if (ds.size() == 0) {
			return defaultTree;
		}
		if (allExamplesHaveSameClassification(ds)) {
			return new ConstantDecisonTree(ds.get(0).targetValue());
		}
		if (attributeNames.size() == 0) {
			return majorityValue(ds);
		}
		String chosenAttribute = chooseAttribute(ds, attributeNames);

		DecisionTree tree = new DecisionTree(chosenAttribute);
		ConstantDecisonTree m = majorityValue(ds);

		List<String> values = ds.getPossibleAttributeValues(chosenAttribute);
		for (String v : values) {
			FeatureDataset filtered = ds.matchingDataSet(chosenAttribute, v);
			List<String> newAttribs = AimaUtil.removeFrom(attributeNames,
					chosenAttribute);
			DecisionTree subTree = decisionTreeLearning(filtered, newAttribs, m);
			tree.addNode(v, subTree);

		}

		return tree;
	}

	private ConstantDecisonTree majorityValue(FeatureDataset ds) {
		FeatureLearning learner = new MajorityLearner();
		learner.train(ds);
		return new ConstantDecisonTree(learner.predict(ds.get(0)));
	}

	private String chooseAttribute(FeatureDataset ds, List<String> attributeNames) {
		double greatestGain = 0.0;
		String attributeWithGreatestGain = attributeNames.get(0);
		for (String attr : attributeNames) {
			double gain = ds.calculateGainFor(attr);
			if (gain > greatestGain) {
				greatestGain = gain;
				attributeWithGreatestGain = attr;
			}
		}

		return attributeWithGreatestGain;
	}

	private boolean allExamplesHaveSameClassification(FeatureDataset ds) {
		String classification = ds.get(0).targetValue();
		Iterator<Features> iter = ds.iterator();
		while (iter.hasNext()) {
			Features element = iter.next();
			if (!(element.targetValue().equals(classification))) {
				return false;
			}

		}
		return true;
	}
}
