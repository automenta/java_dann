package syncleus.dann.data.feature.aima;

/**
 * @author Ravi Mohan
 * 
 */
public class NumberFeatureSpec implements FeatureSpec {

	// a simple attribute representing a number represented as a double .
	private String name;

	public NumberFeatureSpec(String name) {
		this.name = name;
	}

	public boolean isValid(String string) {
		try {
			Double.parseDouble(string);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	public String getFeatureName() {
		return name;
	}

	public Feature newFeature(String rawValue) {
		return new NumberFeature(Double.parseDouble(rawValue), this);
	}
}
