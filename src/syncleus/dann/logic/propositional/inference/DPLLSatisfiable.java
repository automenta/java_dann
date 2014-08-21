package syncleus.dann.logic.propositional.inference;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import syncleus.dann.logic.propositional.kb.KnowledgeBase;
import syncleus.dann.logic.propositional.kb.data.Clause;
import syncleus.dann.logic.propositional.kb.data.Literal;
import syncleus.dann.logic.propositional.kb.data.Model;
import syncleus.dann.logic.propositional.parsing.ast.ComplexSentence;
import syncleus.dann.logic.propositional.parsing.ast.Connective;
import syncleus.dann.logic.propositional.parsing.ast.PropositionSymbol;
import syncleus.dann.logic.propositional.parsing.ast.Sentence;
import syncleus.dann.logic.propositional.visitors.ConvertToConjunctionOfClauses;
import syncleus.dann.logic.propositional.visitors.SymbolCollector;
import syncleus.dann.util.SetOps;
import syncleus.dann.util.AimaUtil;
import syncleus.dann.util.datastruct.Pair;

/**
 * Artificial Intelligence A Modern Approach (3rd Edition): page 261.<br>
 * <br>
 * 
 * <pre>
 * <code>
 * function DPLL-SATISFIABLE?(s) returns true or false
 *   inputs: s, a sentence in propositional logic.
 *   
 *   clauses &larr; the set of clauses in the CNF representation of s
 *   symbols &larr; a list of the proposition symbols in s
 *   return DPLL(clauses, symbols, {})
 * 
 * --------------------------------------------------------------------------------
 * 
 * function DPLL(clauses, symbols, model) returns true or false
 *   
 *   if every clause in clauses is true in model then return true
 *   if some clause in clauses is false in model then return false
 *   P, value &larr; FIND-PURE-SYMBOL(symbols, clauses, model)
 *   if P is non-null then return DPLL(clauses, symbols - P, model &cup; {P = value})
 *   P, value &larr; FIND-UNIT-CLAUSE(clauses, model)
 *   if P is non-null then return DPLL(clauses, symbols - P, model &cup; {P = value})
 *   P &larr; FIRST(symbols); rest &larr; REST(symbols)
 *   return DPLL(clauses, rest, model &cup; {P = true}) or
 *          DPLL(clauses, rest, model &cup; {P = false})
 * </code>
 * </pre>
 * 
 * Figure 7.17 The DPLL algorithm for checking satisfiability of a sentence in
 * propositional logic. The ideas behind FIND-PURE-SYMBOL and FIND-UNIT-CLAUSE
 * are described in the test; each returns a symbol (or null) and the truth
 * value to assign to that symbol. Like TT-ENTAILS?, DPLL operates over partial
 * models.
 * 
 * @author Ciaran O'Reilly
 * @author Ravi Mohan
 * @author Mike Stampone
 */
public class DPLLSatisfiable {

	/**
	 * DPLL-SATISFIABLE?(s)<br>
	 * Checks the satisfiability of a sentence in propositional logic.
	 * 
	 * @param s
	 *            a sentence in propositional logic.
	 * @return true if the sentence is satisfiable, false otherwise.
	 */
	public boolean dpllSatisfiable(Sentence s) {
		// clauses <- the set of clauses in the CNF representation of s
		Set<Clause> clauses = ConvertToConjunctionOfClauses.convert(s)
				.getClauses();
		// symbols <- a list of the proposition symbols in s
		List<PropositionSymbol> symbols = getPropositionSymbolsInSentence(s);

		// return DPLL(clauses, symbols, {})
		return dpll(clauses, symbols, new Model());
	}

	/**
	 * DPLL(clauses, symbols, model)<br>
	 * 
	 * @param clauses
	 *            the set of clauses.
	 * @param symbols
	 *            a list of unassigned symbols.
	 * @param model
	 *            contains the values for assigned symbols.
	 * @return true if the model is satisfiable under current assignments, false
	 *         otherwise.
	 */
	public boolean dpll(Set<Clause> clauses, List<PropositionSymbol> symbols,
			Model model) {
		// if every clause in clauses is true in model then return true
		if (everyClauseTrue(clauses, model)) {
			return true;
		}
		// if some clause in clauses is false in model then return false
		if (someClauseFalse(clauses, model)) {
			return false;
		}

		// P, value <- FIND-PURE-SYMBOL(symbols, clauses, model)
		Pair<PropositionSymbol, Boolean> pAndValue = findPureSymbol(symbols,
				clauses, model);
		// if P is non-null then
		if (pAndValue != null) {
			// return DPLL(clauses, symbols - P, model U {P = value})
			return dpll(clauses, minus(symbols, pAndValue.getFirst()),
					model.union(pAndValue.getFirst(), pAndValue.getSecond()));
		}

		// P, value <- FIND-UNIT-CLAUSE(clauses, model)
		pAndValue = findUnitClause(clauses, model);
		// if P is non-null then
		if (pAndValue != null) {
			// return DPLL(clauses, symbols - P, model U {P = value})
			return dpll(clauses, minus(symbols, pAndValue.getFirst()),
					model.union(pAndValue.getFirst(), pAndValue.getSecond()));
		}

		// P <- FIRST(symbols); rest <- REST(symbols)
		PropositionSymbol p = AimaUtil.first(symbols);
		List<PropositionSymbol> rest = AimaUtil.rest(symbols);
		// return DPLL(clauses, rest, model U {P = true}) or
		// ...... DPLL(clauses, rest, model U {P = false})
		return dpll(clauses, rest, model.union(p, true))
				|| dpll(clauses, rest, model.union(p, false));
	}

	//
	// SUPPORTING CODE
	//

	/**
	 * Determine if KB |= &alpha;, i.e. alpha is entailed by KB.
	 * 
	 * @param kb
	 *            a Knowledge Base in propositional logic.
	 * @param alpha
	 *            a propositional sentence.
	 * @return true, if &alpha; is entailed by KB, false otherwise.
	 */
	public boolean isEntailed(KnowledgeBase kb, Sentence alpha) {
		// AIMA3e p.g. 260: kb |= alpha, can be done by testing
		// unsatisfiability of kb & ~alpha.
		Set<Clause>            kbAndNotAlpha = new LinkedHashSet<Clause>(kb.asCNF());
		Set<PropositionSymbol> symbols       = new LinkedHashSet<PropositionSymbol>(kb.getSymbols());
		Sentence               notQuery      = new ComplexSentence(Connective.NOT, alpha);
		
		kbAndNotAlpha.addAll(ConvertToConjunctionOfClauses.convert(notQuery).getClauses());
		symbols.addAll(SymbolCollector.getSymbolsFrom(notQuery));

		return !dpll(kbAndNotAlpha, new ArrayList<PropositionSymbol>(symbols), new Model());
	}

	//
	// PROTECTED:
	// <b>NOTE:</b> You can extend this class and override these naive
	// implementations of the corresponding DPLL heuristics in order to help
	// optimize performance of the algorithm as described on p.g.s 261-262 of
	// AIMA3e (i.e. 1. component analysis, 2. variable and value ordering,
	// 3. intelligent backtracking, 4. random restarts, and 5. clever indexing).
	//

	// Note: Override this method if you wish to change the initial variable
	// ordering.
	protected List<PropositionSymbol> getPropositionSymbolsInSentence(Sentence s) {
		List<PropositionSymbol> result = new ArrayList<PropositionSymbol>(
				SymbolCollector.getSymbolsFrom(s));

		return result;
	}

	/**
	 * AIMA3e p.g. 260:<br>
	 * <quote><i>Pure symbol heuristic:</i> A <b>pure symbol</b> is a symbol
	 * that always appears with the same "sign" in all clauses. For example, in
	 * the three clauses (A | ~B), (~B | ~C), and (C | A), the symbol A is pure
	 * because only the positive literal appears, B is pure because only the
	 * negative literal appears, and C is impure. It is easy to see that if a
	 * sentence has a model, then it has a model with the pure symbols assigned
	 * so as to make their literals true, because doing so can never make a
	 * clause false. Note that, in determining the purity of a symbol, the
	 * algorithm can ignore clauses that are already known to be true in the
	 * model constructed so far. For example, if the model contains B=false,
	 * then the clause (~B | ~C) is already true, and in the remaining clauses C
	 * appears only as a positive literal; therefore C becomes pure.</quote>
	 * 
	 * @param symbols
	 *            a list of currently unassigned symbols in the model (to be
	 *            checked if pure or not).
	 * @param clauses
	 * @param model
	 * @return a proposition symbol and value pair identifying a pure symbol and
	 *         a value to be assigned to it, otherwise null if no pure symbol
	 *         can be identified.
	 */
	protected Pair<PropositionSymbol, Boolean> findPureSymbol(
			List<PropositionSymbol> symbols, Set<Clause> clauses, Model model) {
		Pair<PropositionSymbol, Boolean> result = null;

		// Collect up possible positive and negative candidate sets of pure
		// symbols
		Set<PropositionSymbol> candidatePurePositiveSymbols = new HashSet<PropositionSymbol>();
		Set<PropositionSymbol> candidatePureNegativeSymbols = new HashSet<PropositionSymbol>();
		for (Clause c : clauses) {
			// Algorithm can ignore clauses that are already known to be true
			if (Boolean.TRUE.equals(model.determineValue(c))) {
				continue;
			}
			// Collect possible candidates
			candidatePurePositiveSymbols.addAll(c.getPositiveSymbols());
			candidatePureNegativeSymbols.addAll(c.getNegativeSymbols());
		}
		// Remove all candidates that are not part of the input list of symbols
		// to be considered
		candidatePurePositiveSymbols.retainAll(symbols);
		candidatePureNegativeSymbols.retainAll(symbols);

		// Determine the overlap/intersection between the positive and negative
		// candidates
		Set<PropositionSymbol> nonPureSymbols = SetOps.intersection(
				candidatePurePositiveSymbols, candidatePureNegativeSymbols);

		// Remove the non-pure symbols
		candidatePurePositiveSymbols.removeAll(nonPureSymbols);
		candidatePureNegativeSymbols.removeAll(nonPureSymbols);

		// We have an implicit preference for positive pure symbols
		if (candidatePurePositiveSymbols.size() > 0) {
			result = new Pair<PropositionSymbol, Boolean>(
					candidatePurePositiveSymbols.iterator().next(), true);
		} // We have a negative pure symbol
		else if (candidatePureNegativeSymbols.size() > 0) {
			result = new Pair<PropositionSymbol, Boolean>(
					candidatePureNegativeSymbols.iterator().next(), false);
		}

		return result;
	}

	/**
	 * AIMA3e p.g. 260:<br>
	 * <quote><i>Unit clause heuristic:</i> A <b>unit clause</b> was defined
	 * earlier as a clause with just one literal. In the context of DPLL, it
	 * also means clauses in which all literals but one are already assigned
	 * false by the model. For example, if the model contains B = true, then (~B
	 * | ~C) simplifies to ~C, which is a unit clause. Obviously, for this
	 * clause to be true, C must be set to false. The unit clause heuristic
	 * assigns all such symbols before branching on the remainder. One important
	 * consequence of the heuristic is that any attempt to prove (by refutation)
	 * a literal that is already in the knowledge base will succeed immediately.
	 * Notice also that assigning one unit clause can create another unit clause
	 * - for example, when C is set to false, (C | A) becomes a unit clause,
	 * causing true to be assigned to A. This "cascade" of forced assignments is
	 * called <b>unit propagation</b>. It resembles the process of forward
	 * chaining with definite clauses, and indeed, if the CNF expression
	 * contains only definite clauses then DPLL essentially replicates forward
	 * chaining.</quote>
	 * 
	 * @param clauses
	 * @param model
	 * @return a proposition symbol and value pair identifying a unit clause and
	 *         a value to be assigned to it, otherwise null if no unit clause
	 *         can be identified.
	 */
	protected Pair<PropositionSymbol, Boolean> findUnitClause(
			Set<Clause> clauses, Model model) {
		Pair<PropositionSymbol, Boolean> result = null;

		for (Clause c : clauses) {
			// if clauses value is currently unknown
			// (i.e. means known literals are false)
			if (model.determineValue(c) == null) {
				Literal unassigned = null;
				// Default definition of a unit clause is a clause
				// with just one literal
				if (c.isUnitClause()) {
					unassigned = c.getLiterals().iterator().next();
				} else {
					// Also, a unit clause in the context of DPLL, also means a
					// clauseF in which all literals but one are already
					// assigned false by the model.
					// Note: at this point we already know the clause is not
					// true, so just need to determine if the clause has a
					// single unassigned literal
					for (Literal l : c.getLiterals()) {
						Boolean value = model.getValue(l.getAtomicSentence());
						if (value == null) {
							// The first unassigned literal encountered.
							if (unassigned == null) {
								unassigned = l;
							} else {
								// This means we have more than 1 unassigned
								// literal so lets skip
								unassigned = null;
								break;
							}
						}
					}
				}

				// if a value assigned it means we have a single
				// unassigned literal and all the assigned literals
				// are not true under the current model as we were
				// unable to determine a value.
				if (unassigned != null) {
					result = new Pair<PropositionSymbol, Boolean>(
							unassigned.getAtomicSentence(),
							unassigned.isPositiveLiteral());
					break;
				}
			}
		}

		return result;
	}

	protected boolean everyClauseTrue(Set<Clause> clauses, Model model) {
		return model.satisfies(clauses);
	}

	protected boolean someClauseFalse(Set<Clause> clauses, Model model) {
		for (Clause c : clauses) {
			// Only 1 needs to be false
			if (Boolean.FALSE.equals(model.determineValue(c))) {
				return true;
			}
		}
		return false;
	}

	// symbols - P
	protected List<PropositionSymbol> minus(List<PropositionSymbol> symbols,
			PropositionSymbol p) {
		List<PropositionSymbol> result = new ArrayList<PropositionSymbol>(
				symbols.size());
		for (PropositionSymbol s : symbols) {
			// symbols - P
			if (!p.equals(s)) {
				result.add(s);
			}
		}
		return result;
	}
}