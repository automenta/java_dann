package syncleus.dann.logic.inductive;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import syncleus.dann.data.feature.aima.FeatureDataset;
import syncleus.dann.data.feature.aima.Features;
import syncleus.dann.util.AimaUtil;

/**
 * @author Ravi Mohan
 * 
 */
public class DecisionTree {
	private String attributeName;

	// each node modelled as a hash of attribute_value/decisiontree
	private Hashtable<String, DecisionTree> nodes;

	protected DecisionTree() {

	}

	public DecisionTree(String attributeName) {
		this.attributeName = attributeName;
		nodes = new Hashtable<String, DecisionTree>();

	}

	public void addLeaf(String attributeValue, String decision) {
		nodes.put(attributeValue, new ConstantDecisonTree(decision));
	}

	public void addNode(String attributeValue, DecisionTree tree) {
		nodes.put(attributeValue, tree);
	}

	public Object predict(Features e) {
		String attrValue = e.getFeatureValueAsString(attributeName);
		if (nodes.containsKey(attrValue)) {
			return nodes.get(attrValue).predict(e);
		} else {
			throw new RuntimeException("no node exists for attribute value "
					+ attrValue);
		}
	}

	public static DecisionTree getStumpFor(FeatureDataset ds, String attributeName,
			String attributeValue, String returnValueIfMatched,
			List<String> unmatchedValues, String returnValueIfUnmatched) {
		DecisionTree dt = new DecisionTree(attributeName);
		dt.addLeaf(attributeValue, returnValueIfMatched);
		for (String unmatchedValue : unmatchedValues) {
			dt.addLeaf(unmatchedValue, returnValueIfUnmatched);
		}
		return dt;
	}

	public static List<DecisionTree> getStumpsFor(FeatureDataset ds,
			String returnValueIfMatched, String returnValueIfUnmatched) {
		List<String> attributes = ds.getNonTargetFeatures();
		List<DecisionTree> trees = new ArrayList<DecisionTree>();
		for (String attribute : attributes) {
			List<String> values = ds.getPossibleAttributeValues(attribute);
			for (String value : values) {
				List<String> unmatchedValues = AimaUtil.removeFrom(
						ds.getPossibleAttributeValues(attribute), value);

				DecisionTree tree = getStumpFor(ds, attribute, value,
						returnValueIfMatched, unmatchedValues,
						returnValueIfUnmatched);
				trees.add(tree);

			}
		}
		return trees;
	}

	/**
	 * @return Returns the attributeName.
	 */
	public String getAttributeName() {
		return attributeName;
	}

	@Override
	public String toString() {
		return toString(1, new StringBuffer());
	}

	public String toString(int depth, StringBuffer buf) {

		if (attributeName != null) {
			buf.append(AimaUtil.ntimes("\t", depth));
			buf.append(AimaUtil.ntimes("***", 1));
			buf.append(attributeName + " \n");
			for (String attributeValue : nodes.keySet()) {
				buf.append(AimaUtil.ntimes("\t", depth + 1));
				buf.append("+" + attributeValue);
				buf.append("\n");
				DecisionTree child = nodes.get(attributeValue);
				buf.append(child.toString(depth + 1, new StringBuffer()));
			}
		}

		return buf.toString();
	}
}
