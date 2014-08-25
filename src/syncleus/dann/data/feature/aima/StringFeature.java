package syncleus.dann.data.feature.aima;

/**
 * @author Ravi Mohan
 * 
 */
public class StringFeature implements Feature {
	private StringFeatureSpec spec;

	private String value;

	public StringFeature(String value, StringFeatureSpec spec) {
		this.spec = spec;
		this.value = value;
	}

	public String valueAsString() {
		return value.trim();
	}

	public String name() {
		return spec.getFeatureName().trim();
	}
}
