package syncleus.dann.data.feature.aima;

/**
 * @author Ravi Mohan
 * 
 */
public interface FeatureSpec {

	boolean isValid(String string);

	String getFeatureName();

	Feature newFeature(String rawValue);
}
