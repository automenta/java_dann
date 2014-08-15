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
package syncleus.dann.neural.backprop.brain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import syncleus.dann.graph.AbstractBidirectedAdjacencyGraph;
import syncleus.dann.neural.AbstractLocalBrain;
import syncleus.dann.neural.Neuron;
import syncleus.dann.neural.NeuronGroup;
import syncleus.dann.neural.Synapse;
import syncleus.dann.neural.backprop.BackpropNeuron;
import syncleus.dann.neural.backprop.InputBackpropNeuron;
import syncleus.dann.neural.backprop.OutputBackpropNeuron;

public abstract class AbstractFeedforwardBrain<IN extends InputBackpropNeuron, ON extends OutputBackpropNeuron, N extends BackpropNeuron, S extends Synapse<N>>
        extends AbstractLocalBrain<IN, ON, N, S> implements
        FeedforwardBackpropBrain<IN, ON, N, S> {
    private boolean initialized = false;
    private final List<NeuronGroup<N>> neuronLayers = new ArrayList<>();
    private int layerCount;
    private static final Logger LOGGER = LogManager
            .getLogger(AbstractFeedforwardBrain.class);

    /**
     * Uses the given threadExecutor for executing tasks.
     *
     * @param threadExecutor executor to use for executing tasks.
     * @since 2.0
     */
    protected AbstractFeedforwardBrain(final ExecutorService threadExecutor) {
        super(threadExecutor);
    }

    /**
     * Default constructor initializes a default threadExecutor based on the
     * number of processors.
     *
     * @since 2.0
     */
    protected AbstractFeedforwardBrain() {
        super();
    }

    protected void initalizeNetwork(final int[] neuronsPerLayer) {
        if (neuronsPerLayer.length < 2)
            throw new IllegalArgumentException(
                    "neuronsPerLayer must have atleast 2 elements");

        this.layerCount = neuronsPerLayer.length;

        // create each layer
        int currentLayerCount = 0;
        for (final int neuronCount : neuronsPerLayer) {
            final NeuronGroup<N> currentGroup = new NeuronGroup<>();
            for (int neuronIndex = 0; neuronIndex < neuronCount; neuronIndex++) {
                final N currentNeuron = this.createNeuron(currentLayerCount,
                        neuronIndex);

                currentGroup.add(currentNeuron);
                this.add(currentNeuron);
            }

            this.neuronLayers.add(currentGroup);

            currentLayerCount++;
        }

        this.initialized = true;
    }

    /**
     * Gets the neuronLayers for children to use for connection.
     *
     * @return the neuronLayers for children to use for connection.
     * @since 2.0
     */
    protected final List<NeuronGroup<N>> getEditableLayers() {
        return this.neuronLayers;
    }

    @Override
    public final List<Set<N>> getLayers() {
        final List<Set<N>> layerList = new ArrayList<>();
        this.neuronLayers.stream().map((layerGroup) -> {
            final Set<N> layer = new HashSet<>();
            layerGroup.getChildrenNeuronsRecursivly()
                    .forEach(layer::add);
            return layer;
        }).forEach((layer) -> layerList.add(Collections.unmodifiableSet(layer)));
        return Collections.unmodifiableList(layerList);
    }

    /**
     * @return the layerCount
     */
    @Override
    public final int getLayerCount() {
        return this.layerCount;
    }

    @Override
    public final void propagate() {
        if (!this.initialized)
            throw new IllegalStateException(
                    "An implementation of AbstractFeedforwardBrain did not initialize network");
        this.neuronLayers.stream().map(NeuronGroup::getChildrenNeuronsRecursivly).forEach((layerNeurons) -> layerNeurons.parallel().forEach(Neuron::tick));
    }

    @Override
    public final void backPropagate() {
        if (!this.initialized)
            throw new IllegalStateException(
                    "An implementation of AbstractFeedforwardBrain did not initialize network");

        // step backwards through all the layers, except the first.
        for (int layerIndex = (this.neuronLayers.size() - 1); layerIndex >= 0; layerIndex--) {
            final NeuronGroup<N> layer = this.neuronLayers.get(layerIndex);
            layer.getChildrenNeuronsRecursivly().parallel()
                    .forEach(BackpropNeuron::backPropagate);

            //
            // if( this.getThreadExecutor() == null )
            // {
            // for(final BackpropNeuron neuron : layerNeurons)
            // neuron.backPropagate();
            // }
            // else
            // {
            // //begin processing all neurons in one layer simultaniously
            // final ArrayList<Future> futures = new ArrayList<Future>();
            // for(final BackpropNeuron neuron : layerNeurons)
            // futures.add(this.getThreadExecutor().submit(new
            // BackPropagate(neuron)));
            //
            // //wait until all neurons have backPropogated
            // try
            // {
            // for(final Future future : futures)
            // future.get();
            // }
            // catch(final InterruptedException caught)
            // {
            // LOGGER.warn("BackPropagate was unexpectidy interupted", caught);
            // throw new
            // UnexpectedInterruptedException("Unexpected interuption. Get should block indefinately",
            // caught);
            // }
            // catch(final ExecutionException caught)
            // {
            // LOGGER.error("BackPropagate had an unexcepted problem executing.",
            // caught);
            // throw new
            // UnexpectedDannError("Unexpected execution exception. Get should block indefinately",
            // caught);
            // }
            // }
        }
    }

    /**
     * Since a specific ActivationFunction or learning rate is needed then this
     * should be overridden in a child class.
     *
     * @param layer the current layer index for which we are creating the neuron.
     * @param index The index of the new neuron within the layer.
     * @return The new SimpleBackpropNeuron to be added to the current layer.
     * @since 2.0
     */
    protected abstract N createNeuron(int layer, int index);

    @Override
    public AbstractBidirectedAdjacencyGraph<N, S> clone() {
        return super.clone(); //To change body of generated methods, choose Tools | Templates.
    }
}
