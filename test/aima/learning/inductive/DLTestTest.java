package aima.test.core.unit.learning.inductive;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import syncleus.dann.attribute.aima.AttributeSamples;
import aima.learning.framework.DataSetFactory;
import syncleus.dann.attribute.aima.Attributes;
import syncleus.dann.logic.inductive.DLTest;
import syncleus.dann.logic.inductive.DLTestFactory;

/**
 * @author Ravi Mohan
 * 
 */
public class DLTestTest {

	@Test
	public void testDecisionList() throws Exception {
		AttributeSamples ds = DataSetFactory.getRestaurantDataSet();
		List<DLTest> dlTests = new DLTestFactory()
				.createDLTestsWithAttributeCount(ds, 1);
		Assert.assertEquals(26, dlTests.size());
	}

	@Test
	public void testDLTestMatchSucceedsWithMatchedExample() throws Exception {
		AttributeSamples ds = DataSetFactory.getRestaurantDataSet();
		Attributes e = ds.get(0);
		DLTest test = new DLTest();
		test.add("type", "French");
		Assert.assertTrue(test.matches(e));
	}

	@Test
	public void testDLTestMatchFailsOnMismatchedExample() throws Exception {
		AttributeSamples ds = DataSetFactory.getRestaurantDataSet();
		Attributes e = ds.get(0);
		DLTest test = new DLTest();
		test.add("type", "Thai");
		Assert.assertFalse(test.matches(e));
	}

	@Test
	public void testDLTestMatchesEvenOnMismatchedTargetAttributeValue()
			throws Exception {
		AttributeSamples ds = DataSetFactory.getRestaurantDataSet();
		Attributes e = ds.get(0);
		DLTest test = new DLTest();
		test.add("type", "French");
		Assert.assertTrue(test.matches(e));
	}

	@Test
	public void testDLTestReturnsMatchedAndUnmatchedExamplesCorrectly()
			throws Exception {
		AttributeSamples ds = DataSetFactory.getRestaurantDataSet();
		DLTest test = new DLTest();
		test.add("type", "Burger");

		AttributeSamples matched = test.matchedExamples(ds);
		Assert.assertEquals(4, matched.size());

		AttributeSamples unmatched = test.unmatchedExamples(ds);
		Assert.assertEquals(8, unmatched.size());
	}
}
