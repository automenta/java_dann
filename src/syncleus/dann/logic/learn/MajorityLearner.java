package syncleus.dann.logic.learn;

import java.util.ArrayList;
import java.util.List;

import syncleus.dann.data.feature.aima.AttributeSamples;
import syncleus.dann.data.feature.aima.Features;
import syncleus.dann.data.feature.aima.AttributeLearning;
import syncleus.dann.util.AimaUtil;

/**
 * @author Ravi Mohan
 * 
 */
public class MajorityLearner implements AttributeLearning {

	private String result;

	public void train(AttributeSamples ds) {
		List<String> targets = new ArrayList<String>();
		for (Features e : ds.samples) {
			targets.add(e.targetValue());
		}
		result = AimaUtil.mode(targets);
	}

	public String predict(Features e) {
		return result;
	}

	public int[] test(AttributeSamples ds) {
		int[] results = new int[] { 0, 0 };

		for (Features e : ds.samples) {
			if (e.targetValue().equals(result)) {
				results[0] = results[0] + 1;
			} else {
				results[1] = results[1] + 1;
			}
		}
		return results;
	}
}