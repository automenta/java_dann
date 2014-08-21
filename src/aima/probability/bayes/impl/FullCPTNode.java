package aima.probability.bayes.impl;

import aima.probability.RandomVariable;
import aima.probability.bayes.ConditionalProbabilityDistribution;
import aima.probability.bayes.ConditionalProbabilityTable;
import aima.probability.bayes.FiniteNode;
import aima.probability.bayes.Node;
import java.util.Set;

/**
 * Default implementation of the FiniteNode interface that uses a fully
 * specified Conditional Probability Table to represent the Node's conditional
 * distribution.
 * 
 * @author Ciaran O'Reilly
 * 
 */
public class FullCPTNode extends AbstractNode implements FiniteNode {
	protected ConditionalProbabilityTable cpt = null;

        protected FullCPTNode(RandomVariable var, Set<Node> parents, Set<Node> children, ConditionalProbabilityTable cpt) {
            super(var, parents, children);
            this.cpt = cpt;
        }
     
	public FullCPTNode(RandomVariable var, double[] distribution) {
		this(var, distribution, (Node[]) null);
	}

	public FullCPTNode(RandomVariable var, double[] values, Node... parents) {
		super(var, parents);

		RandomVariable[] conditionedOn = new RandomVariable[getParents().size()];
		int i = 0;
		for (Node p : getParents()) {
			conditionedOn[i++] = p.getRandomVariable();
		}

		cpt = new CPT(var, values, conditionedOn);
	}

        @Override
        public FullCPTNode clone() {
            //return new FullCPTNode(variable.clone(), getParents().clone(), getChildren().clone(), cpt.clone());
            return new FullCPTNode(variable.clone(), getParents(), getChildren(), cpt);
        }

        
        
        
        
	//
	// START-Node
	@Override
	public ConditionalProbabilityDistribution getCPD() {
		return getCPT();
	}

	// END-Node
	//

	//
	// START-FiniteNode

	@Override
	public ConditionalProbabilityTable getCPT() {
		return cpt;
	}

	// END-FiniteNode
	//
}
