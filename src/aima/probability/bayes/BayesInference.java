package aima.probability.bayes;

import aima.probability.CategoricalDistribution;
import aima.probability.RandomVariable;
import aima.probability.proposition.AssignmentProposition;

/**
 * General interface to be implemented by Bayesian Inference algorithms.
 * 
 * @author Ciaran O'Reilly
 */
public interface BayesInference {
	/**
	 * @param X
	 *            the query variables.
	 * @param observedEvidence
	 *            observed values for variables E.
	 * @param bn
	 *            a Bayes net with variables {X} &cup; E &cup; Y /* Y = hidden
	 *            variables
	 * @return a distribution over the query variables.
	 */
	CategoricalDistribution ask(final RandomVariable[] X,
			final AssignmentProposition[] observedEvidence,
			final BayesianNetwork bn);
}
