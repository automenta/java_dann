package syncleus.dann.logic.learn;

import java.util.Hashtable;
import java.util.List;

import syncleus.dann.attribute.aima.AttributeSamples;
import syncleus.dann.attribute.aima.Attributes;
import syncleus.dann.attribute.aima.AttributeLearning;
import syncleus.dann.util.AimaUtil;
import syncleus.dann.util.datastruct.Table;

/**
 * @author Ravi Mohan
 * 
 */
public class AdaBoostLearner implements AttributeLearning {

	private List<AttributeLearning> learners;

	private AttributeSamples dataSet;

	private double[] exampleWeights;

	private Hashtable<AttributeLearning, Double> learnerWeights;

	public AdaBoostLearner(List<AttributeLearning> learners, AttributeSamples ds) {
		this.learners = learners;
		this.dataSet = ds;

		initializeExampleWeights(ds.samples.size());
		initializeHypothesisWeights(learners.size());
	}

	public void train(AttributeSamples ds) {
		initializeExampleWeights(ds.samples.size());

		for (AttributeLearning learner : learners) {
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

	public String predict(Attributes e) {
		return weightedMajority(e);
	}

	public int[] test(AttributeSamples ds) {
		int[] results = new int[] { 0, 0 };

		for (Attributes e : ds.samples) {
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

	private String weightedMajority(Attributes e) {
		List<String> targetValues = dataSet.getPossibleAttributeValues(dataSet
				.getTargetAttributeName());

		Table<String, AttributeLearning, Double> table = createTargetValueLearnerTable(
				targetValues, e);
		return getTargetValueWithTheMaximumVotes(targetValues, table);
	}

	private Table<String, AttributeLearning, Double> createTargetValueLearnerTable(
			List<String> targetValues, Attributes e) {
		// create a table with target-attribute values as rows and learners as
		// columns and cells containing the weighted votes of each Learner for a
		// target value
		// Learner1 Learner2 Laerner3 .......
		// Yes 0.83 0.5 0
		// No 0 0 0.6

		Table<String, AttributeLearning, Double> table = new Table<String, AttributeLearning, Double>(
				targetValues, learners);
		// initialize table
		for (AttributeLearning l : learners) {
			for (String s : targetValues) {
				table.set(s, l, 0.0);
			}
		}
		for (AttributeLearning learner : learners) {
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
			Table<String, AttributeLearning, Double> table) {
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

		learnerWeights = new Hashtable<AttributeLearning, Double>();
		for (AttributeLearning le : learners) {
			learnerWeights.put(le, 1.0);
		}
	}

	private double calculateError(AttributeSamples ds, AttributeLearning l) {
		double error = 0.0;
		for (int i = 0; i < ds.samples.size(); i++) {
			Attributes e = ds.get(i);
			if (!(l.predict(e).equals(e.targetValue()))) {
				error = error + exampleWeights[i];
			}
		}
		return error;
	}

	private void adjustExampleWeights(AttributeSamples ds, AttributeLearning l, double error) {
		double epsilon = error / (1.0 - error);
		for (int j = 0; j < ds.samples.size(); j++) {
			Attributes e = ds.get(j);
			if ((l.predict(e).equals(e.targetValue()))) {
				exampleWeights[j] = exampleWeights[j] * epsilon;
			}
		}
		exampleWeights = AimaUtil.normalize(exampleWeights);
	}

	private double scoreOfValue(String targetValue,
			Table<String, AttributeLearning, Double> table, List<AttributeLearning> learners) {
		double score = 0.0;
		for (AttributeLearning l : learners) {
			score += table.get(targetValue, l);
		}
		return score;
	}
}
