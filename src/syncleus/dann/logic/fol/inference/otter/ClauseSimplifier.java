package syncleus.dann.logic.fol.inference.otter;

import syncleus.dann.logic.fol.kb.data.Clause;

/**
 * @author Ciaran O'Reilly
 * 
 */
public interface ClauseSimplifier {
	Clause simplify(Clause c);
}
