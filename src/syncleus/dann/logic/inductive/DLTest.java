package syncleus.dann.logic.inductive;

import java.util.Hashtable;

import syncleus.dann.data.feature.aima.AttributeSamples;
import syncleus.dann.data.feature.aima.Features;

/**
 * @author Ravi Mohan
 * 
 */
public class DLTest {

	// represents a single test in the Decision List
	private Hashtable<String, String> attrValues;

	public DLTest() {
		attrValues = new Hashtable<String, String>();
	}

	public void add(String nta, String ntaValue) {
		attrValues.put(nta, ntaValue);

	}

	public boolean matches(Features e) {
		for (String key : attrValues.keySet()) {
			if (!(attrValues.get(key).equals(e.getAttributeValueAsString(key)))) {
				return false;
			}
		}
		return true;
		// return e.targetValue().equals(targetValue);
	}

	public AttributeSamples matchedExamples(AttributeSamples ds) {
		AttributeSamples matched = ds.emptyDataSet();
		for (Features e : ds.samples) {
			if (matches(e)) {
				matched.add(e);
			}
		}
		return matched;
	}

	public AttributeSamples unmatchedExamples(AttributeSamples ds) {
		AttributeSamples unmatched = ds.emptyDataSet();
		for (Features e : ds.samples) {
			if (!(matches(e))) {
				unmatched.add(e);
			}
		}
		return unmatched;
	}

	@Override
	public String toString() {
		StringBuffer buf = new StringBuffer();
		buf.append("IF  ");
		for (String key : attrValues.keySet()) {
			buf.append(key + " = ");
			buf.append(attrValues.get(key) + " ");
		}
		buf.append(" DECISION ");
		return buf.toString();
	}
}
