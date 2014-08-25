package syncleus.dann.logic.learn;

import java.util.Hashtable;
import java.util.List;

import syncleus.dann.data.feature.aima.FeatureDataset;
import syncleus.dann.data.feature.aima.Features;
import syncleus.dann.data.feature.aima.FeatureLearning;
import syncleus.dann.util.AimaUtil;
import syncleus.dann.util.datastruct.Table;

/**
 * @author Ravi Mohan
 * 
 */
public class AdaBoostLearner implements FeatureLearning {

	private List<FeatureLearning> learners;

	private FeatureDataset dataSet;

	private double[] exampleWeights;

	private Hashtable<FeatureLearning, Double> learnerWeights;

	public AdaBoostLearner(List<FeatureLearning> learners, FeatureDataset ds) {
		this.learners = learners;
		this.dataSet = ds;

		initializeExampleWeights(ds.samples.size());
		initializeHypothesisWeights(learners.size());
	}

	public void train(FeatureDataset ds) {
		initializeExampleWeights(ds.samples.size());

		for (FeatureLearning learner : learners) {
			learner.train(ds);

			double error = calculateError(ds, learner);
			if (error < 0.0001) {
				break;
			}

			adjustExampleWeights(ds, learner, error);

			double newHypothesisWeight = learnerWeights.get(learner)
					* Math.log((1.0 - error) / error);
			learnerWeights.put(learner, newHypothesisWeight);
		}
	}

	public String predict(Features e) {
		return weightedMajority(e);
	}

	public int[] test(FeatureDataset ds) {
		int[] results = new int[] { 0, 0 };

		for (Features e : ds.samples) {
			if (e.targetValue().equals(predict(e))) {
				results[0] = results[0] + 1;
			} else {
				results[1] = results[1] + 1;
			}
		}
		return results;
	}

	//
	// PRIVATE METHODS
	//

	private String weightedMajority(Features e) {
		List<String> targetValues = dataSet.getPossibleAttributeValues(dataSet
				.getTargetAttributeName());

		Table<String, FeatureLearning, Double> table = createTargetValueLearnerTable(
				targetValues, e);
		return getTargetValueWithTheMaximumVotes(targetValues, table);
	}

	private Table<String, FeatureLearning, Double> createTargetValueLearnerTable(
			List<String> targetValues, Features e) {
		// create a table with target-attribute values as rows and learners as
		// columns and cells containing the weighted votes of each Learner for a
		// target value
		// Learner1 Learner2 Laerner3 .......
		// Yes 0.83 0.5 0
		// No 0 0 0.6

		Table<String, FeatureLearning, Double> table = new Table<String, FeatureLearning, Double>(
				targetValues, learners);
		// initialize table
		for (FeatureLearning l : learners) {
			for (String s : targetValues) {
				table.set(s, l, 0.0);
			}
		}
		for (FeatureLearning learner : learners) {
			String predictedValue = learner.predict(e);
			for (String v : targetValues) {
				if (predictedValue.equals(v)) {
					table.set(v, learner, table.get(v, learner)
							+ learnerWeights.get(learner) * 1);
				}
			}
		}
		return table;
	}

	private String getTargetValueWithTheMaximumVotes(List<String> targetValues,
			Table<String, FeatureLearning, Double> table) {
		String targetValueWithMaxScore = targetValues.get(0);
		double score = scoreOfValue(targetValueWithMaxScore, table, learners);
		for (String value : targetValues) {
			double scoreOfValue = scoreOfValue(value, table, learners);
			if (scoreOfValue > score) {
				targetValueWithMaxScore = value;
				score = scoreOfValue;
			}
		}
		return targetValueWithMaxScore;
	}

	private void initializeExampleWeights(int size) {
		if (size == 0) {
			throw new RuntimeException(
					"cannot initialize Ensemble learning with Empty Dataset");
		}
		double value = 1.0 / (1.0 * size);
		exampleWeights = new double[size];
		for (int i = 0; i < size; i++) {
			exampleWeights[i] = value;
		}
	}

	private void initializeHypothesisWeights(int size) {
		if (size == 0) {
			throw new RuntimeException(
					"cannot initialize Ensemble learning with Zero Learners");
		}

		learnerWeights = new Hashtable<FeatureLearning, Double>();
		for (FeatureLearning le : learners) {
			learnerWeights.put(le, 1.0);
		}
	}

	private double calculateError(FeatureDataset ds, FeatureLearning l) {
		double error = 0.0;
		for (int i = 0; i < ds.samples.size(); i++) {
			Features e = ds.get(i);
			if (!(l.predict(e).equals(e.targetValue()))) {
				error = error + exampleWeights[i];
			}
		}
		return error;
	}

	private void adjustExampleWeights(FeatureDataset ds, FeatureLearning l, double error) {
		double epsilon = error / (1.0 - error);
		for (int j = 0; j < ds.samples.size(); j++) {
			Features e = ds.get(j);
			if ((l.predict(e).equals(e.targetValue()))) {
				exampleWeights[j] = exampleWeights[j] * epsilon;
			}
		}
		exampleWeights = AimaUtil.normalize(exampleWeights);
	}

	private double scoreOfValue(String targetValue,
			Table<String, FeatureLearning, Double> table, List<FeatureLearning> learners) {
		double score = 0.0;
		for (FeatureLearning l : learners) {
			score += table.get(targetValue, l);
		}
		return score;
	}
}
