package aima.test.core.unit.learning.framework;

import java.util.ArrayList;
import java.util.List;


/**
 * @author Ravi Mohan
 * 
 */
public class MockDataSetSpecification extends syncleus.dann.data.feature.aima.AttributeSamples.Specification {

	public MockDataSetSpecification(String targetAttributeName) {
		setTarget(targetAttributeName);
	}

	@Override
	public List<String> getAttributeNames() {
		return new ArrayList<String>();
	}
}
