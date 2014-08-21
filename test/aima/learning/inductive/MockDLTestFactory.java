package aima.test.core.unit.learning.inductive;

import java.util.List;

import syncleus.dann.attribute.aima.AttributeSamples;
import syncleus.dann.logic.inductive.DLTest;
import syncleus.dann.logic.inductive.DLTestFactory;

/**
 * @author Ravi Mohan
 * 
 */
public class MockDLTestFactory extends DLTestFactory {

	private List<DLTest> tests;

	public MockDLTestFactory(List<DLTest> tests) {
		this.tests = tests;
	}

	@Override
	public List<DLTest> createDLTestsWithAttributeCount(AttributeSamples ds, int i) {
		return tests;
	}
}
