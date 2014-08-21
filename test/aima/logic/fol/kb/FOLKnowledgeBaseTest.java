package aima.test.core.unit.logic.fol.kb;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import syncleus.dann.logic.fol.StandardizeApartIndexicalFactory;
import syncleus.dann.logic.fol.domain.DomainFactory;
import syncleus.dann.logic.fol.kb.FOLKnowledgeBase;
import syncleus.dann.logic.fol.kb.data.Clause;
import syncleus.dann.logic.fol.kb.data.Literal;
import syncleus.dann.logic.fol.parsing.ast.Predicate;
import syncleus.dann.logic.fol.parsing.ast.Term;
import syncleus.dann.logic.fol.parsing.ast.Variable;

/**
 * @author Ravi Mohan
 * @author Ciaran O'Reilly
 * 
 */
public class FOLKnowledgeBaseTest {

	private FOLKnowledgeBase weaponsKB, kingsKB;

	@Before
	public void setUp() {
		StandardizeApartIndexicalFactory.flush();

		weaponsKB = new FOLKnowledgeBase(DomainFactory.weaponsDomain());

		kingsKB = new FOLKnowledgeBase(DomainFactory.kingsDomain());
	}

	@Test
	public void testAddRuleAndFact() {
		weaponsKB.tell("(Missile(x) => Weapon(x))");
		Assert.assertEquals(1, weaponsKB.getNumberRules());
		weaponsKB.tell("American(West)");
		Assert.assertEquals(1, weaponsKB.getNumberRules());
		Assert.assertEquals(1, weaponsKB.getNumberFacts());
	}

	@Test
	public void testAddComplexRule() {
		weaponsKB
				.tell("( (((American(x) AND Weapon(y)) AND Sells(x,y,z)) AND Hostile(z)) => Criminal(x))");
		Assert.assertEquals(1, weaponsKB.getNumberRules());
		weaponsKB.tell("American(West)");
		Assert.assertEquals(1, weaponsKB.getNumberRules());

		List<Term> terms = new ArrayList<Term>();
		terms.add(new Variable("v0"));

		Clause dcRule = weaponsKB.getAllDefiniteClauseImplications().get(0);
		Assert.assertNotNull(dcRule);
		Assert.assertEquals(true, dcRule.isImplicationDefiniteClause());
		Assert.assertEquals(new Literal(new Predicate("Criminal", terms)),
				dcRule.getPositiveLiterals().get(0));
	}

	@Test
	public void testFactNotAddedWhenAlreadyPresent() {
		kingsKB.tell("((King(x) AND Greedy(x)) => Evil(x))");
		kingsKB.tell("King(John)");
		kingsKB.tell("King(Richard)");
		kingsKB.tell("Greedy(John)");

		Assert.assertEquals(1, kingsKB.getNumberRules());
		Assert.assertEquals(3, kingsKB.getNumberFacts());

		kingsKB.tell("King(John)");
		kingsKB.tell("King(Richard)");
		kingsKB.tell("Greedy(John)");

		Assert.assertEquals(1, kingsKB.getNumberRules());
		Assert.assertEquals(3, kingsKB.getNumberFacts());

		kingsKB.tell("(((King(John))))");
		kingsKB.tell("(((King(Richard))))");
		kingsKB.tell("(((Greedy(John))))");

		Assert.assertEquals(1, kingsKB.getNumberRules());
		Assert.assertEquals(3, kingsKB.getNumberFacts());
	}
}