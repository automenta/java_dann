package syncleus.dann.logic.fol.inference.otter.defaultimpl;

import java.util.Set;

import syncleus.dann.logic.fol.inference.otter.ClauseFilter;
import syncleus.dann.logic.fol.kb.data.Clause;

/**
 * @author Ciaran O'Reilly
 * 
 */
public class DefaultClauseFilter implements ClauseFilter {
	public DefaultClauseFilter() {

	}

	//
	// START-ClauseFilter
	public Set<Clause> filter(Set<Clause> clauses) {
		return clauses;
	}

	// END-ClauseFilter
	//
}
