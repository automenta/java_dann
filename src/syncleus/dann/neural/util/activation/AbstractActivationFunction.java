package syncleus.dann.neural.util.activation;

import syncleus.dann.Function;

public interface AbstractActivationFunction extends Function<Double,Double> {

    /**
     * The activation function.
     *
     * @param activity the neuron's current activity.
     * @return The result of the activation function. Usually a bound value
     * between 1 and -1 or 1 and 0. However this bound range is not
     * required.
     * @since 1.0
     */
    double activate(double activity);
    
    @Override
    default Double apply(Double i) {
        return activate(i);
    }

    /**
     * Implements the activation function. The array is modified according to
     * the activation function being used. See the class description for more
     * specific information on this type of activation function.
     *
     * @param d     The input array to the activation function.
     * @param start The starting index.
     * @param size  The number of values to calculate.
     */
    void activate(double[] d, int start, int size);

}