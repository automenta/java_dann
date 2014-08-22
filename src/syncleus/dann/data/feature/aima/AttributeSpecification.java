package syncleus.dann.data.feature.aima;

/**
 * @author Ravi Mohan
 * 
 */
public interface AttributeSpecification {

	boolean isValid(String string);

	String getAttributeName();

	Feature createAttribute(String rawValue);
}
