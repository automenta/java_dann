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
package syncleus.dann.neural.backprop;

import syncleus.dann.neural.Brain;
import syncleus.dann.neural.Synapse;
import syncleus.dann.neural.activation.DannActivationFunction;

public abstract class AbstractOutputBackpropNeuron extends
		AbstractBackpropNeuron implements OutputBackpropNeuron {
	private static final long serialVersionUID = -4643866124019076672L;
	/**
	 * holds the value for the current training set.
	 *
	 * @since 1.0
	 */
	private double desired;

	/**
	 * Creates a new instance of OutputBackpropNeuron.
	 *
	 * @since 1.0
	 */
	protected AbstractOutputBackpropNeuron(final Brain brain) {
		super(brain);
	}

	/**
	 * Creates a new instance of OutputBackpropNeuron using the specified
	 * activation function.
	 *
	 * @param activationFunction
	 *            The activation function to use.
	 * @since 1.0
	 */
	protected AbstractOutputBackpropNeuron(final Brain brain,
			final DannActivationFunction activationFunction) {
		super(brain, activationFunction);
	}

	/**
	 * Creates a new instance of this class using the specified learning rate.
	 *
	 * @param learningRate
	 *            The learning rate for this neuron.
	 * @since 1.0
	 */
	protected AbstractOutputBackpropNeuron(final Brain brain,
			final double learningRate) {
		super(brain, learningRate);
	}

	/**
	 * Creates a new instance of this class with the specified activation
	 * function and learning rate.
	 *
	 * @param activationFunction
	 *            The activation used by this neuron.
	 * @param learningRate
	 *            The learning rate for this neuron.
	 * @since 1.0
	 */
	protected AbstractOutputBackpropNeuron(final Brain brain,
			final DannActivationFunction activationFunction,
			final double learningRate) {
		super(brain, activationFunction, learningRate);
	}

	/**
	 * This method sets the expected output for this neuron to learn from.
	 *
	 * @param trainingToSet
	 *            sets the current desired output.
	 * @since 1.0
	 */
	@Override
	public void setDesired(final double trainingToSet) {
		this.desired = trainingToSet;
	}

	@Override
	protected void calculateDeltaTrain() {
		double newDeltaTrain = 0.0;
		for (final Synapse currentSynapse : getBrain()
				.getTraversableEdges(this)) {
			newDeltaTrain += currentSynapse.getWeight()
					* getDeltaTrainDestinations().get(currentSynapse);
		}
		newDeltaTrain += desired - getOutput();
		newDeltaTrain *= activateDerivitive();
		setDeltaTrain(newDeltaTrain);
	}

	@Override
	public double getOutput() {
		return super.getOutput();
	}
}