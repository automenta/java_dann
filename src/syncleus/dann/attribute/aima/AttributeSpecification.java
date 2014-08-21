package syncleus.dann.attribute.aima;

/**
 * @author Ravi Mohan
 * 
 */
public interface AttributeSpecification {

	boolean isValid(String string);

	String getAttributeName();

	Attribute createAttribute(String rawValue);
}
