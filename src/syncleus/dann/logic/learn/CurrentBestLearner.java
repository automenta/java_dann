package syncleus.dann.logic.learn;

import java.util.ArrayList;
import java.util.List;

import syncleus.dann.attribute.aima.AttributeSamples;
import syncleus.dann.attribute.aima.Attributes;
import syncleus.dann.attribute.aima.AttributeLearning;
import syncleus.dann.logic.knowledge.CurrentBestLearning;
import syncleus.dann.logic.knowledge.FOLDataSetDomain;
import syncleus.dann.logic.knowledge.FOLExample;
import syncleus.dann.logic.knowledge.Hypothesis;
import syncleus.dann.logic.fol.inference.FOLOTTERLikeTheoremProver;
import syncleus.dann.logic.fol.inference.InferenceResult;
import syncleus.dann.logic.fol.kb.FOLKnowledgeBase;

/**
 * @author Ciaran O'Reilly
 * 
 */
public class CurrentBestLearner implements AttributeLearning {
	private String trueGoalValue = null;
	private FOLDataSetDomain folDSDomain = null;
	private FOLKnowledgeBase kb = null;
	private Hypothesis currentBestHypothesis = null;

	//
	// PUBLIC METHODS
	//
	public CurrentBestLearner(String trueGoalValue) {
		this.trueGoalValue = trueGoalValue;
	}

	//
	// START-Learner
	public void train(AttributeSamples ds) {
		folDSDomain = new FOLDataSetDomain(ds.specification, trueGoalValue);
		List<FOLExample> folExamples = new ArrayList<FOLExample>();
		int egNo = 1;
		for (Attributes e : ds.samples) {
			folExamples.add(new FOLExample(folDSDomain, e, egNo));
			egNo++;
		}

		// Setup a KB to be used for learning
		kb = new FOLKnowledgeBase(folDSDomain, new FOLOTTERLikeTheoremProver(
				1000, false));

		CurrentBestLearning cbl = new CurrentBestLearning(folDSDomain, kb);

		currentBestHypothesis = cbl.currentBestLearning(folExamples);
	}

	public String predict(Attributes e) {
		String prediction = "~" + e.targetValue();
		if (null != currentBestHypothesis) {
			FOLExample etp = new FOLExample(folDSDomain, e, 0);
			kb.clear();
			kb.tell(etp.getDescription());
			kb.tell(currentBestHypothesis.getHypothesis());
			InferenceResult ir = kb.ask(etp.getClassification());
			if (ir.isTrue()) {
				if (trueGoalValue.equals(e.targetValue())) {
					prediction = e.targetValue();
				}
			} else if (ir.isPossiblyFalse() || ir.isUnknownDueToTimeout()) {
				if (!trueGoalValue.equals(e.targetValue())) {
					prediction = e.targetValue();
				}
			}
		}

		return prediction;
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
	// END-Learner
	//
}
