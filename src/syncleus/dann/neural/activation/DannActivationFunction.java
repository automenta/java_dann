/******************************************************************************
 *                                                                             *
 *  Copyright: (c) Syncleus, Inc.                                              *
 *                                                                             *
 *  You may redistribute and modify this source code under the terms and       *
 *  conditions of the Open Source Community License - Type C version 1.0       *
 *  or any later version as published by Syncleus, Inc. at www.syncleus.com.   *
 *  There should be a copy of the license included with this file. If a copy   *
 *  of the license is not included you are granted no right to distribute or   *
 *  otherwise use this file except through a legal and valid license. You      *
 *  should also contact Syncleus, Inc. at the information below if you cannot  *
 *  find a license:                                                            *
 *                                                                             *
 *  Syncleus, Inc.                                                             *
 *  2604 South 12th Street                                                     *
 *  Philadelphia, PA 19148                                                     *
 *                                                                             *
 ******************************************************************************/
package syncleus.dann.neural.activation;

// TODO rework this so it will work with piecewise and ramping activation function, a derivative is not enough for that

/**
 * An interface containing methods for an activation function as well as its
 * derivative. This is used in propagating as well as back-propagating activity.
 *
 * @author Jeffrey Phillips Freeman
 * @since 1.0
 */
public interface DannActivationFunction extends java.io.Serializable, AbstractActivationFunction {
    /* (non-Javadoc)
     * @see syncleus.dann.neural.activation.AbstractActivationFunction#activate(double[], int, int)
     */
    @Override
    default void activate(final double[] d, final int start, final int size) {
        for (int i = start; i < start + size; i++) {
            d[i] = activate(d[i]);
        }
    }

    /**
     * The derivative of the activation function.
     *
     * @param activity The neuron's current activity.
     * @return The result of the derivative of the activation function.
     * @since 1.0
     */
    double activateDerivative(double activity);

    default boolean hasDerivative() {
        return true;
    }

    boolean isBound();

    double getUpperLimit();

    double getLowerLimit();
}
