package syncleus.dann.logic.fol.inference;

import syncleus.dann.logic.fol.kb.FOLKnowledgeBase;
import syncleus.dann.logic.fol.parsing.ast.Sentence;

/**
 * @author Ciaran O'Reilly
 * 
 */
public interface InferenceProcedure {
	/**
	 * 
	 * @param kb
	 *            the knowledge base against which the query is to be made.
	 * @param query
	 *            the query to be answered.
	 * @return an InferenceResult.
	 */
	InferenceResult ask(FOLKnowledgeBase kb, Sentence query);
}
