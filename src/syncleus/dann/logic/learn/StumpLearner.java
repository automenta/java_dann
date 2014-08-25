package syncleus.dann.logic.learn;

import syncleus.dann.data.feature.aima.FeatureDataset;
import syncleus.dann.logic.inductive.DecisionTree;

/**
 * @author Ravi Mohan
 * 
 */
public class StumpLearner extends DecisionTreeLearner {

	public StumpLearner(DecisionTree sl, String unable_to_classify) {
		super(sl, unable_to_classify);
	}

	@Override
	public void train(FeatureDataset ds) {
		// System.out.println("Stump learner training");
		// do nothing the stump is not inferred from the dataset
	}
}
