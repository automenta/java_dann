package syncleus.dann.logic.learn;

import java.util.ArrayList;
import java.util.List;

import syncleus.dann.attribute.aima.AttributeSamples;
import syncleus.dann.attribute.aima.Attributes;
import syncleus.dann.attribute.aima.AttributeLearning;
import syncleus.dann.util.AimaUtil;

/**
 * @author Ravi Mohan
 * 
 */
public class MajorityLearner implements AttributeLearning {

	private String result;

	public void train(AttributeSamples ds) {
		List<String> targets = new ArrayList<String>();
		for (Attributes e : ds.samples) {
			targets.add(e.targetValue());
		}
		result = AimaUtil.mode(targets);
	}

	public String predict(Attributes e) {
		return result;
	}

	public int[] test(AttributeSamples ds) {
		int[] results = new int[] { 0, 0 };

		for (Attributes e : ds.samples) {
			if (e.targetValue().equals(result)) {
				results[0] = results[0] + 1;
			} else {
				results[1] = results[1] + 1;
			}
		}
		return results;
	}
}