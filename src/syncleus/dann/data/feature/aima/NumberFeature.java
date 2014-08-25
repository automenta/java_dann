package syncleus.dann.data.feature.aima;

/**
 * @author Ravi Mohan
 * 
 */
public class NumberFeature implements Feature {
	double value;

	private NumberFeatureSpec spec;

	public NumberFeature(double rawValue, NumberFeatureSpec spec) {
		this.value = rawValue;
		this.spec = spec;
	}

	public String valueAsString() {
		return Double.toString(value);
	}

	public String name() {
		return spec.getFeatureName().trim();
	}

	public double valueAsDouble() {
		return value;
	}

    @Override
    public String toString() {
        return name() + "=" + valueAsString();
    }
        
        
}
