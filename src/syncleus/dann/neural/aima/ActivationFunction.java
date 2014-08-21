package syncleus.dann.neural.aima;

/**
 * @author Ravi Mohan
 * 
 */
public interface ActivationFunction {
	double activation(double parameter);

	double deriv(double parameter);
}
