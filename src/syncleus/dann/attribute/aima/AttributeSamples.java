package syncleus.dann.attribute.aima;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import syncleus.dann.util.AimaUtil;

/**
 * @author Ravi Mohan
 * 
 */
public class AttributeSamples {
	protected AttributeSamples() {

	}

	public List<Attributes> samples;

	public Specification specification;

	public AttributeSamples(Specification spec) {
		samples = new LinkedList<Attributes>();
		this.specification = spec;
	}

	public void add(Attributes e) {
		samples.add(e);
	}

	public int size() {
		return samples.size();
	}

	public Attributes get(int number) {
		return samples.get(number);
	}

	public AttributeSamples remove(Attributes e) {
		AttributeSamples ds = new AttributeSamples(specification);
		for (Attributes eg : samples) {
			if (!(e.equals(eg))) {
				ds.add(eg);
			}
		}
		return ds;
	}

	public double getInformationFor() {
		String attributeName = specification.getTarget();
		Hashtable<String, Integer> counts = new Hashtable<String, Integer>();
		for (Attributes e : samples) {

			String val = e.getAttributeValueAsString(attributeName);
			if (counts.containsKey(val)) {
				counts.put(val, counts.get(val) + 1);
			} else {
				counts.put(val, 1);
			}
		}

		double[] data = new double[counts.keySet().size()];
		Iterator<Integer> iter = counts.values().iterator();
		for (int i = 0; i < data.length; i++) {
			data[i] = iter.next();
		}
		data = AimaUtil.normalize(data);

		return AimaUtil.information(data);
	}

	public Hashtable<String, AttributeSamples> splitByAttribute(String attributeName) {
		Hashtable<String, AttributeSamples> results = new Hashtable<String, AttributeSamples>();
		for (Attributes e : samples) {
			String val = e.getAttributeValueAsString(attributeName);
			if (results.containsKey(val)) {
				results.get(val).add(e);
			} else {
				AttributeSamples ds = new AttributeSamples(specification);
				ds.add(e);
				results.put(val, ds);
			}
		}
		return results;
	}

	public double calculateGainFor(String parameterName) {
		Hashtable<String, AttributeSamples> hash = splitByAttribute(parameterName);
		double totalSize = samples.size();
		double remainder = 0.0;
		for (String parameterValue : hash.keySet()) {
			double reducedDataSetSize = hash.get(parameterValue).samples
					.size();
			remainder += (reducedDataSetSize / totalSize)
					* hash.get(parameterValue).getInformationFor();
		}
		return getInformationFor() - remainder;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if ((o == null) || (this.getClass() != o.getClass())) {
			return false;
		}
		AttributeSamples other = (AttributeSamples) o;
		return samples.equals(other.samples);
	}

	@Override
	public int hashCode() {
		return 0;
	}

	public Iterator<Attributes> iterator() {
		return samples.iterator();
	}

	public AttributeSamples copy() {
		AttributeSamples ds = new AttributeSamples(specification);
		for (Attributes e : samples) {
			ds.add(e);
		}
		return ds;
	}

	public List<String> getAttributeNames() {
		return specification.getAttributeNames();
	}

	public String getTargetAttributeName() {
		return specification.getTarget();
	}

	public AttributeSamples emptyDataSet() {
		return new AttributeSamples(specification);
	}

	/**
	 * @param specification
	 *            The specification to set. USE SPARINGLY for testing etc ..
	 *            makes no semantic sense
	 */
	public void setSpecification(Specification specification) {
		this.specification = specification;
	}

	public List<String> getPossibleAttributeValues(String attributeName) {
		return specification.getPossibleAttributeValues(attributeName);
	}

	public AttributeSamples matchingDataSet(String attributeName, String attributeValue) {
		AttributeSamples ds = new AttributeSamples(specification);
		for (Attributes e : samples) {
			if (e.getAttributeValueAsString(attributeName).equals(
					attributeValue)) {
				ds.add(e);
			}
		}
		return ds;
	}

	public List<String> getNonTargetAttributes() {
		return AimaUtil.removeFrom(getAttributeNames(), getTargetAttributeName());
	}

    /**
     * @author Ravi Mohan
     *
     */
    public static class Specification {

        List<AttributeSpecification> attributeSpecifications;
        private String targetAttribute;

        public Specification() {
            super();
            this.attributeSpecifications = new ArrayList<AttributeSpecification>();
        }

        public boolean isValid(List<String> uncheckedAttributes) {
            if (attributeSpecifications.size() != uncheckedAttributes.size()) {
                throw new RuntimeException("size mismatch specsize = " + attributeSpecifications.size() + " attrbutes size = " + uncheckedAttributes.size());
            }
            Iterator<AttributeSpecification> attributeSpecIter = attributeSpecifications.iterator();
            Iterator<String> valueIter = uncheckedAttributes.iterator();
            while (valueIter.hasNext() && attributeSpecIter.hasNext()) {
                if (!(attributeSpecIter.next().isValid(valueIter.next()))) {
                    return false;
                }
            }
            return true;
        }

        /**
         * @return Returns the targetAttribute.
         */
        public String getTarget() {
            return targetAttribute;
        }

        public List<String> getPossibleAttributeValues(String attributeName) {
            for (AttributeSpecification as : attributeSpecifications) {
                if (as.getAttributeName().equals(attributeName)) {
                    return ((StringAttributeSpecification) as).possibleAttributeValues();
                }
            }
            throw new RuntimeException("No such attribute" + attributeName);
        }

        public List<String> getAttributeNames() {
            List<String> names = new ArrayList<String>();
            for (AttributeSpecification as : attributeSpecifications) {
                names.add(as.getAttributeName());
            }
            return names;
        }

        public void defineStringAttribute(String name, String[] attributeValues) {
            attributeSpecifications.add(new StringAttributeSpecification(name, attributeValues));
            setTarget(name); // target defaults to last column added
        }

        /**
         * @param target
         *            The targetAttribute to set.
         */
        public void setTarget(String target) {
            this.targetAttribute = target;
        }

        public AttributeSpecification getAttributeSpecFor(String name) {
            for (AttributeSpecification spec : attributeSpecifications) {
                if (spec.getAttributeName().equals(name)) {
                    return spec;
                }
            }
            throw new RuntimeException("no attribute spec for  " + name);
        }

        public void defineNumericAttribute(String name) {
            attributeSpecifications.add(new NumericAttributeSpecification(name));
        }

        public List<String> getNamesOfStringAttributes() {
            List<String> names = new ArrayList<String>();
            for (AttributeSpecification spec : attributeSpecifications) {
                if (spec instanceof StringAttributeSpecification) {
                    names.add(spec.getAttributeName());
                }
            }
            return names;
        }
    }
}


//package aima.learning.framework;
//
//import java.io.BufferedReader;
//import java.io.InputStreamReader;
//import java.util.Arrays;
//import java.util.Hashtable;
//import java.util.Iterator;
//import java.util.List;
//
//import aima.learning.data.DataResource;
//import aima.util.Util;
//
///**
// * @author Ravi Mohan
// * 
// */
//public class DataSetFactory {
//
//	public DataSet fromFile(String filename, DataSetSpecification spec,
//			String separator) throws Exception {
//		// assumed file in data directory and ends in .csv
//		DataSet ds = new DataSet(spec);
//		BufferedReader reader = new BufferedReader(new InputStreamReader(
//				DataResource.class.getResourceAsStream(filename + ".csv")));
//		String line;
//		while ((line = reader.readLine()) != null) {
//			ds.add(exampleFromString(line, spec, separator));
//		}
//
//		return ds;
//
//	}
//
//	public static Example exampleFromString(String data,
//			DataSetSpecification dataSetSpec, String separator) {
//		Hashtable<String, Attribute> attributes = new Hashtable<String, Attribute>();
//		List<String> attributeValues = Arrays.asList(data.split(separator));
//		if (dataSetSpec.isValid(attributeValues)) {
//			List<String> names = dataSetSpec.getAttributeNames();
//			Iterator<String> nameiter = names.iterator();
//			Iterator<String> valueiter = attributeValues.iterator();
//			while (nameiter.hasNext() && valueiter.hasNext()) {
//				String name = nameiter.next();
//				AttributeSpecification attributeSpec = dataSetSpec
//						.getAttributeSpecFor(name);
//				Attribute attribute = attributeSpec.createAttribute(valueiter
//						.next());
//				attributes.put(name, attribute);
//			}
//			String targetAttributeName = dataSetSpec.getTarget();
//			return new Example(attributes, attributes.get(targetAttributeName));
//		} else {
//			throw new RuntimeException("Unable to construct Example from "
//					+ data);
//		}
//	}
//
//	public static DataSet getRestaurantDataSet() throws Exception {
//		DataSetSpecification spec = createRestaurantDataSetSpec();
//		return new DataSetFactory().fromFile("restaurant", spec, "\\s+");
//	}
//
//	public static DataSetSpecification createRestaurantDataSetSpec() {
//		DataSetSpecification dss = new DataSetSpecification();
//		dss.defineStringAttribute("alternate", Util.yesno());
//		dss.defineStringAttribute("bar", Util.yesno());
//		dss.defineStringAttribute("fri/sat", Util.yesno());
//		dss.defineStringAttribute("hungry", Util.yesno());
//		dss.defineStringAttribute("patrons", new String[] { "None", "Some",
//				"Full" });
//		dss.defineStringAttribute("price", new String[] { "$", "$$", "$$$" });
//		dss.defineStringAttribute("raining", Util.yesno());
//		dss.defineStringAttribute("reservation", Util.yesno());
//		dss.defineStringAttribute("type", new String[] { "French", "Italian",
//				"Thai", "Burger" });
//		dss.defineStringAttribute("wait_estimate", new String[] { "0-10",
//				"10-30", "30-60", ">60" });
//		dss.defineStringAttribute("will_wait", Util.yesno());
//		// last attribute is the target attribute unless the target is
//		// explicitly reset with dss.setTarget(name)
//
//		return dss;
//	}
//
//	public static DataSet getIrisDataSet() throws Exception {
//		DataSetSpecification spec = createIrisDataSetSpec();
//		return new DataSetFactory().fromFile("iris", spec, ",");
//	}
//
//	public static DataSetSpecification createIrisDataSetSpec() {
//		DataSetSpecification dss = new DataSetSpecification();
//		dss.defineNumericAttribute("sepal_length");
//		dss.defineNumericAttribute("sepal_width");
//		dss.defineNumericAttribute("petal_length");
//		dss.defineNumericAttribute("petal_width");
//		dss.defineStringAttribute("plant_category", new String[] { "setosa",
//				"versicolor", "virginica" });
//		return dss;
//	}
//}
