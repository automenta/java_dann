package syncleus.dann.attribute.aima;

import java.util.Hashtable;

/**
 * @author Ravi Mohan
 * 
 */
public class Attributes {
	Hashtable<String, Attribute> attributes;

	private Attribute targetAttribute;

	public Attributes(Hashtable<String, Attribute> attributes,
			Attribute targetAttribute) {
		this.attributes = attributes;
		this.targetAttribute = targetAttribute;
	}

	public String getAttributeValueAsString(String attributeName) {
		return attributes.get(attributeName).valueAsString();
	}

	public double getAttributeValueAsDouble(String attributeName) {
		Attribute attribute = attributes.get(attributeName);
		if (attribute == null || !(attribute instanceof NumericAttribute)) {
			throw new RuntimeException(
					"cannot return numerical value for non numeric attribute");
		}
		return ((NumericAttribute) attribute).valueAsDouble();
	}

	@Override
	public String toString() {
		return attributes.toString();
	}

	public String targetValue() {
		return getAttributeValueAsString(targetAttribute.name());
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if ((o == null) || (this.getClass() != o.getClass())) {
			return false;
		}
		Attributes other = (Attributes) o;
		return attributes.equals(other.attributes);
	}

	@Override
	public int hashCode() {
		return attributes.hashCode();
	}

	public Attributes numerize(
			Hashtable<String, Hashtable<String, Integer>> attrValueToNumber) {
		Hashtable<String, Attribute> numerizedExampleData = new Hashtable<String, Attribute>();
		for (String key : attributes.keySet()) {
			Attribute attribute = attributes.get(key);
			if (attribute instanceof StringAttribute) {
				int correspondingNumber = attrValueToNumber.get(key).get(
						attribute.valueAsString());
				NumericAttributeSpecification spec = new NumericAttributeSpecification(
						key);
				numerizedExampleData.put(key, new NumericAttribute(
						correspondingNumber, spec));
			} else {// Numeric Attribute
				numerizedExampleData.put(key, attribute);
			}
		}
		return new Attributes(numerizedExampleData,
				numerizedExampleData.get(targetAttribute.name()));
	}
}
