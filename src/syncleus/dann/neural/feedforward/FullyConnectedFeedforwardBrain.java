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
package syncleus.dann.neural.feedforward;

import java.util.concurrent.ExecutorService;
import syncleus.dann.graph.AbstractBidirectedAdjacencyGraph;
import syncleus.dann.neural.Synapse;
import syncleus.dann.neural.feedforward.graph.AbstractFullyConnectedFeedforwardBrain;
import syncleus.dann.neural.feedforward.graph.BackpropNeuron;
import syncleus.dann.neural.feedforward.graph.InputBackpropNeuron;
import syncleus.dann.neural.feedforward.graph.OutputBackpropNeuron;
import syncleus.dann.neural.feedforward.graph.SimpleBackpropNeuron;
import syncleus.dann.neural.feedforward.graph.SimpleInputBackpropNeuron;
import syncleus.dann.neural.feedforward.graph.SimpleOutputBackpropNeuron;
import syncleus.dann.neural.util.activation.DannActivationFunction;

public final class FullyConnectedFeedforwardBrain<IN extends InputBackpropNeuron, ON extends OutputBackpropNeuron, N extends BackpropNeuron, S extends Synapse<N>>
        extends AbstractFullyConnectedFeedforwardBrain<IN, ON, N, S> {
    private static final long serialVersionUID = 3666884827880527998L;
    private final double learningRate;
    private final DannActivationFunction activationFunction;

    /**
     * Uses the given threadExecutor for executing tasks.
     *
     * @param threadExecutor executor to use for executing tasks.
     * @since 2.0
     */
    public FullyConnectedFeedforwardBrain(final int[] neuronsPerLayer,
                                          final double learningRate,
                                          final DannActivationFunction activationFunction,
                                          final ExecutorService threadExecutor) {
        super(threadExecutor);
        this.learningRate = learningRate;
        this.activationFunction = activationFunction;

        this.initalizeNetwork(neuronsPerLayer);
    }

    /**
     * Default constructor initializes a default threadExecutor based on the
     * number of processors.
     *
     * @since 2.0
     */
    public FullyConnectedFeedforwardBrain(final int[] neuronsPerLayer,
                                          final double learningRate,
                                          final DannActivationFunction activationFunction) {
        super();
        this.learningRate = learningRate;
        this.activationFunction = activationFunction;

        this.initalizeNetwork(neuronsPerLayer);
    }

    @Override
    protected N createNeuron(final int layer, final int index) {
        final BackpropNeuron neuron;

        if (layer == 0) {
            neuron = new SimpleInputBackpropNeuron(this);
        } else if (layer >= (getLayerCount() - 1)) {
            neuron = new SimpleOutputBackpropNeuron(this, activationFunction,
                    learningRate);
        } else {
            neuron = new SimpleBackpropNeuron(this, activationFunction,
                    learningRate);
        }

        // TODO fix typing
        return (N) neuron;
    }

    @Override
    public AbstractBidirectedAdjacencyGraph<N, S> clone() {
        return super.clone(); //To change body of generated methods, choose Tools | Templates.
    }

}
