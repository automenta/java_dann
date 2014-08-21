package syncleus.dann.logic.learn;

import java.util.ArrayList;
import java.util.List;

import syncleus.dann.attribute.aima.DataSet;
import syncleus.dann.attribute.aima.Example;
import syncleus.dann.attribute.aima.Learner;
import aima.util.AimaUtil;

/**
 * @author Ravi Mohan
 * 
 */
public class MajorityLearner implements Learner {

	private String result;

	public void train(DataSet ds) {
		List<String> targets = new ArrayList<String>();
		for (Example e : ds.examples) {
			targets.add(e.targetValue());
		}
		result = AimaUtil.mode(targets);
	}

	public String predict(Example e) {
		return result;
	}

	public int[] test(DataSet ds) {
		int[] results = new int[] { 0, 0 };

		for (Example e : ds.examples) {
			if (e.targetValue().equals(result)) {
				results[0] = results[0] + 1;
			} else {
				results[1] = results[1] + 1;
			}
		}
		return results;
	}
}