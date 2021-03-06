package syncleus.dann.data.feature.aima;

import java.util.Arrays;
import java.util.List;

/**
 * @author Ravi Mohan
 * 
 */
public class StringFeatureSpec implements FeatureSpec {
	String attributeName;

	List<String> attributePossibleValues;

	public StringFeatureSpec(String attributeName,
			List<String> attributePossibleValues) {
		this.attributeName = attributeName;
		this.attributePossibleValues = attributePossibleValues;
	}

	public StringFeatureSpec(String attributeName,
			String[] attributePossibleValues) {
		this(attributeName, Arrays.asList(attributePossibleValues));
	}

	public boolean isValid(String value) {
		return (attributePossibleValues.contains(value));
	}

	/**
	 * @return Returns the attributeName.
	 */
	public String getFeatureName() {
		return attributeName;
	}

	public List<String> possibleAttributeValues() {
		return attributePossibleValues;
	}

	public Feature newFeature(String rawValue) {
		return new StringFeature(rawValue, this);
	}
}
