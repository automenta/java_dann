package aima.test.core.unit.logic.fol;

import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import syncleus.dann.logic.fol.PredicateCollector;
import syncleus.dann.logic.fol.domain.DomainFactory;
import syncleus.dann.logic.fol.parsing.FOLParser;
import syncleus.dann.logic.fol.parsing.ast.Predicate;
import syncleus.dann.logic.fol.parsing.ast.Sentence;

/**
 * @author Ravi Mohan
 * 
 */
public class PredicateCollectorTest {
	PredicateCollector collector;

	FOLParser parser;

	@Before
	public void setUp() {
		collector = new PredicateCollector();
		parser = new FOLParser(DomainFactory.weaponsDomain());
	}

	@Test
	public void testSimpleSentence() {
		Sentence s = parser.parse("(Missile(x) => Weapon(x))");
		List<Predicate> predicates = collector.getPredicates(s);
		Assert.assertNotNull(predicates);
	}
}
