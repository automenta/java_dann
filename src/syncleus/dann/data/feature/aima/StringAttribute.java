package syncleus.dann.data.feature.aima;

/**
 * @author Ravi Mohan
 * 
 */
public class StringAttribute implements Feature {
	private StringAttributeSpecification spec;

	private String value;

	public StringAttribute(String value, StringAttributeSpecification spec) {
		this.spec = spec;
		this.value = value;
	}

	public String valueAsString() {
		return value.trim();
	}

	public String name() {
		return spec.getAttributeName().trim();
	}
}
