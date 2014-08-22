package syncleus.dann.data.feature.aima;

import java.util.HashMap;
import java.util.Hashtable;

/**
 * @author Ravi Mohan
 * 
 */
public class Features {
//	public final Map<String, Feature> attributes;
//
//	private Feature targetFeature;
//
//        public Features() {
//            this(new HashMap(), null);
//        }
//        
//	public Features(Map<String,Feature> attributes, Feature targetFeature) {
//		this.attributes = attributes;
//		this.targetFeature = targetFeature;
//	}
//
//	public String getFeatureValueAsString(String attributeName) {
//		return attributes.get(attributeName).valueAsString();
//	}
//
        public void set(String attr, Feature value) {
            attributes.put(attr, value);
        }
        public void set(String attr, double v) {
            NumericAttribute av = new NumericAttribute(v, new NumericAttributeSpecification(attr));
            attributes.put(attr, av);
        }
        /*
        public void set(String attr, String value) {
            attributes.put(attr, new StringFeature(value));
        }
//        */
//        
//	public double getFeatureValueAsDouble(String attributeName) {
//		Feature attribute = attributes.get(attributeName);
//		if (attribute == null || !(attribute instanceof NumericFeature)) {
//			throw new RuntimeException(
//					"cannot return numerical value for non numeric attribute");
//		}
//		return ((NumericFeature) attribute).valueAsDouble();
//	}
//
//	@Override
//	public String toString() {
//            StringBuilder sb = new StringBuilder();
//            sb.append('[');
//            for (Map.Entry<String, Feature> a : attributes.entrySet()) {
//		sb.append(a.getValue()).append(", ");
//            }
//            sb.append(']');
//            return sb.toString();
//	}
//
//	public String targetValue() {
//		return getFeatureValueAsString(targetFeature.name());
//	}
//
//	@Override
//	public boolean equals(Object o) {
//		if (this == o) {
//			return true;
//		}
//		if ((o == null) || (this.getClass() != o.getClass())) {
//			return false;
//		}
//		Features other = (Features)o;
//                return attributes.equals(other.attributes);
//	}
//
//	@Override
//	public int hashCode() {
//		return attributes.hashCode();
//	}
//
//
	HashMap<String, Feature> attributes;

	private Feature targetFeature;

        public Features() {
            this(new HashMap(), null);
        }
        
	public Features(HashMap<String, Feature> attributes,
			Feature targetFeature) {
		this.attributes = attributes;
		this.targetFeature = targetFeature;
	}

	public String getFeatureValueAsString(String attributeName) {
		return attributes.get(attributeName).valueAsString();
	}

	public double getFeatureValueAsDouble(String attributeName) {
		Feature attribute = attributes.get(attributeName);
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
		return getFeatureValueAsString(targetFeature.name());
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if ((o == null) || (this.getClass() != o.getClass())) {
			return false;
		}
		Features other = (Features) o;
		return attributes.equals(other.attributes);
	}

	@Override
	public int hashCode() {
		return attributes.hashCode();
	}

	public Features numerize(
			HashMap<String, Hashtable<String, Integer>> attrValueToNumber) {
		HashMap<String, Feature> numerizedExampleData = new HashMap<String, Feature>();
		for (String key : attributes.keySet()) {
			Feature attribute = attributes.get(key);
			if (attribute instanceof StringAttribute) {
				int correspondingNumber = attrValueToNumber.get(key).get(
						attribute.valueAsString());
				NumericAttributeSpecification spec = new NumericAttributeSpecification(
						key);
				numerizedExampleData.put(key, new NumericAttribute(
						correspondingNumber, spec));
			} else {// Numeric Feature
				numerizedExampleData.put(key, attribute);
			}
		}
		return new Features(numerizedExampleData,
				numerizedExampleData.get(targetFeature.name()));
	}
//	public Features getNumerized(
//			Hashtable<String, Hashtable<String, Integer>> attrValueToNumber) {
//		Hashtable<String, Feature> numFeaturesampleData = new Hashtable<String, Feature>();
//		for (String key : attributes.keySet()) {
//			Feature attribute = attributes.get(key);
//			if (attribute instanceof StringFeature) {
//				int correspondingNumber = attrValueToNumber.get(key).get(
//						attribute.valueAsString());
//				NumericFeatureSpecification spec = new NumericFeatureSpecification(
//						key);
//				numerizedExampleData.put(key, new NumericFeature(
//						correspondingNumber, spec));
//			} else {// Numeric Feature
//				numerizedExampleData.put(key, attribute);
//			}
//		}
//		return new Features(nFeaturesampleData,
//				numerizedExampleData.get(targetFeature.name()));
//	}
}
