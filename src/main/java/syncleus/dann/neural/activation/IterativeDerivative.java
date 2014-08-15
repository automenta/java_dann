package syncleus.dann.neural.activation;

public interface IterativeDerivative {

    /**
     * Calculate the derivative. For performance reasons two numbers are
     * provided. First, the value "b" is simply the number that we would like to
     * calculate the derivative of.
     * <p/>
     * Second, the value "a", which is the value returned by the activation
     * function, when presented with "b".
     * <p/>
     * We use two values because some of the most common activation functions
     * make use of the result of the activation function. It is bad for
     * performance to calculate this value twice. Yet, not all derivatives are
     * calculated this way. By providing both the value before the activation
     * function is applied ("b"), and after the activation function is
     * applied("a"), the class can be constructed to use whichever value will be
     * the most efficient.
     *
     * @param b The number to calculate the derivative of, the number "before"
     *          the activation function was applied.
     * @param a The number "after" an activation function has been applied.
     * @return The derivative.
     */
    double derivative(double b, double a);

    /**
     * @return Return true if this function has a derivative.
     */
    boolean hasDerivative();
}