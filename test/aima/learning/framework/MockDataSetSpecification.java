package aima.test.core.unit.learning.framework;

import java.util.ArrayList;
import java.util.List;

import syncleus.dann.attribute.aima.DataSetSpecification;

/**
 * @author Ravi Mohan
 * 
 */
public class MockDataSetSpecification extends DataSetSpecification {

	public MockDataSetSpecification(String targetAttributeName) {
		setTarget(targetAttributeName);
	}

	@Override
	public List<String> getAttributeNames() {
		return new ArrayList<String>();
	}
}
