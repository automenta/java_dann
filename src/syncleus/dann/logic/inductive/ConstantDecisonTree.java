package syncleus.dann.logic.inductive;

import syncleus.dann.attribute.aima.Attributes;
import syncleus.dann.util.AimaUtil;

/**
 * @author Ravi Mohan
 * 
 */
public class ConstantDecisonTree extends DecisionTree {
	// represents leaf nodes like "Yes" or "No"
	private String value;

	public ConstantDecisonTree(String value) {
		this.value = value;
	}

	@Override
	public void addLeaf(String attributeValue, String decision) {
		throw new RuntimeException("cannot add Leaf to ConstantDecisonTree");
	}

	@Override
	public void addNode(String attributeValue, DecisionTree tree) {
		throw new RuntimeException("cannot add Node to ConstantDecisonTree");
	}

	@Override
	public Object predict(Attributes e) {
		return value;
	}

	@Override
	public String toString() {
		return "DECISION -> " + value;
	}

	@Override
	public String toString(int depth, StringBuffer buf) {
		buf.append(AimaUtil.ntimes("\t", depth + 1));
		buf.append("DECISION -> " + value + "\n");
		return buf.toString();
	}
}
