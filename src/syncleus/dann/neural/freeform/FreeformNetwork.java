/*
 * Encog(tm) Core v3.2 - Java Version
 * http://www.heatonresearch.com/encog/
 * https://github.com/encog/encog-java-core

 * Copyright 2008-2013 Heaton Research, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * For more information on Heaton Research copyrights, licenses
 * and trademarks visit:
 * http://www.heatonresearch.com/copyright
 */
package syncleus.dann.neural.freeform;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import syncleus.dann.Classifying;
import syncleus.dann.RegressionLearning;
import syncleus.dann.data.Dataset;
import syncleus.dann.data.MutableData;
import syncleus.dann.data.VectorEncodable;
import syncleus.dann.data.vector.VectorData;
import syncleus.dann.learn.AbstractLearning;
import syncleus.dann.learn.ErrorLearning;
import syncleus.dann.learn.MLContext;
import syncleus.dann.learn.MLResettable;
import syncleus.dann.math.EncogMath;
import syncleus.dann.math.EncogUtility;
import syncleus.dann.math.array.EngineArray;
import syncleus.dann.math.random.ConsistentRandomizer;
import syncleus.dann.neural.VectorNeuralNetwork;
import syncleus.dann.neural.freeform.basic.BasicActivationSummationFactory;
import syncleus.dann.neural.freeform.basic.BasicFreeformConnectionFactory;
import syncleus.dann.neural.freeform.basic.BasicFreeformLayerFactory;
import syncleus.dann.neural.freeform.basic.BasicFreeformNeuronFactory;
import syncleus.dann.neural.freeform.factory.FreeformConnectionFactory;
import syncleus.dann.neural.freeform.factory.FreeformLayerFactory;
import syncleus.dann.neural.freeform.factory.FreeformNeuronFactory;
import syncleus.dann.neural.freeform.factory.InputSummationFactory;
import syncleus.dann.neural.freeform.task.ConnectionTask;
import syncleus.dann.neural.freeform.task.NeuronTask;
import syncleus.dann.neural.util.activation.ActivationTANH;
import syncleus.dann.neural.util.activation.EncogActivationFunction;
import syncleus.dann.util.ObjectCloner;

/**
 * Implements a freefrom neural network. A freeform neural network can represent
 * much more advanced structures than the flat networks that the Encog
 * BasicNetwork implements. However, while freeform networks are more advanced
 * than the BasicNetwork, they are also much slower.
 * <p/>
 * Freeform networks allow just about any neuron to be connected to another
 * neuron. You can have neuron layers if you want, but they are not required.
 */
public class FreeformNetwork<D extends MutableData> extends AbstractLearning implements MLContext, Cloneable, RegressionLearning<D>, VectorEncodable, MLResettable, Classifying<D,Integer>, ErrorLearning<D>
{

    /**
     * The serial ID.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Construct an Elmann recurrent neural network.
     *
     * @param input   The input count.
     * @param hidden1 The hidden count.
     * @param output  The output count.
     * @param af      The activation function.
     * @return The newly created network.
     */
    public static FreeformNetwork createElman(final int input,
                                              final int hidden1, final int output, final EncogActivationFunction af) {

        final FreeformNetwork network = new FreeformNetwork();
        final FreeformLayer inputLayer = network.createInputLayer(input);
        final FreeformLayer hiddenLayer1 = network.createLayer(hidden1);
        final FreeformLayer outputLayer = network.createOutputLayer(output);

        network.connectLayers(inputLayer, hiddenLayer1, af, 1.0, false);
        network.connectLayers(hiddenLayer1, outputLayer, af, 1.0, false);
        network.createContext(hiddenLayer1, hiddenLayer1);
        network.reset();

        return network;
    }

    /**
     * Create a feedforward freeform neural network.
     *
     * @param input   The input count.
     * @param hidden1 The first hidden layer count, zero if none.
     * @param hidden2 The second hidden layer count, zero if none.
     * @param output  The output count.
     * @param af      The activation function.
     * @return The newly crated network.
     */
    public static FreeformNetwork createFeedforward(final int input,
                                                    final int hidden1, final int hidden2, final int output,
                                                    final EncogActivationFunction af) {
        final FreeformNetwork network = new FreeformNetwork();
        FreeformLayer lastLayer = network.createInputLayer(input);
        FreeformLayer currentLayer;

        if (hidden1 > 0) {
            currentLayer = network.createLayer(hidden1);
            network.connectLayers(lastLayer, currentLayer, af, 1.0, false);
            lastLayer = currentLayer;
        }

        if (hidden2 > 0) {
            currentLayer = network.createLayer(hidden2);
            network.connectLayers(lastLayer, currentLayer, af, 1.0, false);
            lastLayer = currentLayer;
        }

        currentLayer = network.createOutputLayer(output);
        network.connectLayers(lastLayer, currentLayer, af, 1.0, false);

        network.reset();

        return network;
    }

    /**
     * The input layer.
     */
    private FreeformLayer inputLayer;

    /**
     * The output layer.
     */
    private FreeformLayer outputLayer;

    /**
     * The connection factory.
     */
    private final FreeformConnectionFactory connectionFactory = new BasicFreeformConnectionFactory();

    /**
     * The layer factory.
     */
    private final FreeformLayerFactory layerFactory = new BasicFreeformLayerFactory();

    /**
     * The neuron factory.
     */
    private final FreeformNeuronFactory neuronFactory = new BasicFreeformNeuronFactory();

    /**
     * The input summation factory.
     */
    private final InputSummationFactory summationFactory = new BasicActivationSummationFactory();

    /**
     * Default constructor. Typically should not be directly used.
     */
    public FreeformNetwork() {
    }

    /**
     * Craete a freeform network from a basic network.
     *
     * @param network The basic network to use.
     */
    public FreeformNetwork(final VectorNeuralNetwork network) {

        if (network.getLayerCount() < 2) {
            throw new FreeformNetworkError(
                    "The BasicNetwork must have at least two layers to be converted.");
        }

        // handle each layer
        FreeformLayer previousLayer = null;
        FreeformLayer currentLayer;

        for (int currentLayerIndex = 0; currentLayerIndex < network
                .getLayerCount(); currentLayerIndex++) {
            // create the layer
            currentLayer = this.layerFactory.factor();

            // Is this the input layer?
            if (this.inputLayer == null) {
                this.inputLayer = currentLayer;
            }

            // Add the neurons for this layer
            for (int i = 0; i < network.getLayerNeuronCount(currentLayerIndex); i++) {
                // obtain the summation object.
                InputSummation summation = null;

                if (previousLayer != null) {
                    summation = this.summationFactory.factor(network
                            .getActivation(currentLayerIndex));
                }

                // add the new neuron
                currentLayer.add(this.neuronFactory.factorRegular(summation));
            }

            // Fully connect this layer to previous
            if (previousLayer != null) {
                connectLayersFromBasic(network, currentLayerIndex - 1,
                        previousLayer, currentLayerIndex, currentLayer,
                        currentLayerIndex, false);
            }

            // Add the bias neuron
            // The bias is added after connections so it has no inputs
            if (network.isLayerBiased(currentLayerIndex)) {
                final FreeformNeuron biasNeuron = this.neuronFactory
                        .factorRegular(null);
                biasNeuron.setBias(true);
                biasNeuron.setActivation(network
                        .getLayerBiasActivation(currentLayerIndex));
                currentLayer.add(biasNeuron);
            }

            // update previous layer
            previousLayer = currentLayer;
            currentLayer = null;
        }

        // finally, set the output layer.
        this.outputLayer = previousLayer;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double calculateError(final Dataset data) {
        return EncogUtility.calculateRegressionError(this, data);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Integer classify(final D input) {
        final MutableData output = compute(input);
        return EngineArray.maxIndex(output.getData());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void clearContext() {
        performNeuronTask(neuron -> {
            if (neuron instanceof FreeformContextNeuron) {
                neuron.setActivation(0);
            }
        });
    }

    /**
     * Return a clone of this neural network. Including structure, weights and
     * bias values. This is a deep copy.
     *
     * @return A cloned copy of the neural network.
     */
    @Override
    public Object clone() {
        final VectorNeuralNetwork result = (VectorNeuralNetwork)ObjectCloner.deepCopy(this);
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MutableData compute(final D input) {

        // Allocate result
        final MutableData result = new VectorData(this.outputLayer.size());

        // Copy the input
        for (int i = 0; i < input.size(); i++) {
            this.inputLayer.setActivation(i, input.getData(i));
        }

        // Request calculation of outputs
        for (int i = 0; i < this.outputLayer.size(); i++) {
            final FreeformNeuron outputNeuron = this.outputLayer.getNeurons()
                    .get(i);
            outputNeuron.performCalculation();
            result.setData(i, outputNeuron.getActivation());
        }

        updateContext();

        return result;
    }

    /**
     * Connect two layers. These layers will be connected with a TANH activation
     * function in a non-recurrent way. A bias activation of 1.0 will be used,
     * if needed.
     *
     * @param source The source layer.
     * @param target The target layer.
     */
    public void connectLayers(final FreeformLayer source,
                              final FreeformLayer target) {
        connectLayers(source, target, new ActivationTANH(), 1.0, false);
    }

    /**
     * Connect two layers.
     *
     * @param source                     The source layer.
     * @param target                     The target layer.
     * @param theEncogActivationFunction The activation function to use.
     * @param biasActivation             The bias activation to use.
     * @param isRecurrent                True, if this is a recurrent connection.
     */
    public void connectLayers(final FreeformLayer source,
                              final FreeformLayer target,
                              final EncogActivationFunction theEncogActivationFunction,
                              final double biasActivation, final boolean isRecurrent) {

        // create bias, if requested
        if (biasActivation > EncogMath.DEFAULT_EPSILON) {
            // does the source already have a bias?
            if (source.hasBias()) {
                throw new FreeformNetworkError(
                        "The source layer already has a bias neuron, you cannot create a second.");
            }
            final FreeformNeuron biasNeuron = this.neuronFactory
                    .factorRegular(null);
            biasNeuron.setActivation(biasActivation);
            biasNeuron.setBias(true);
            source.add(biasNeuron);
        }

        target.getNeurons().stream().map((targetNeuron) -> {
            InputSummation summation = targetNeuron.getInputSummation();
            if (summation == null) {
                summation = this.summationFactory.factor(theEncogActivationFunction);
                targetNeuron.setInputSummation(summation);
            }
            return targetNeuron;
        }).forEach((targetNeuron) -> source.getNeurons().stream().map((sourceNeuron) -> {
            final FreeformConnection connection = this.connectionFactory
                    .factor(sourceNeuron, targetNeuron);
            sourceNeuron.addOutput(connection);
            return connection;
        }).forEach(targetNeuron::addInput));
    }

    /**
     * Connect two layers, assume bias activation of 1.0 and non-recurrent
     * connection.
     *
     * @param source                     The source layer.
     * @param target                     The target layer.
     * @param theEncogActivationFunction The activation function.
     */
    public void ConnectLayers(final FreeformLayer source,
                              final FreeformLayer target,
                              final EncogActivationFunction theEncogActivationFunction) {
        connectLayers(source, target, theEncogActivationFunction, 1.0, false);
    }

    /**
     * Connect layers from a BasicNetwork. Used internally only.
     *
     * @param network      The BasicNetwork.
     * @param fromLayerIdx The from layer index.
     * @param source       The from layer.
     * @param sourceIdx    The source index.
     * @param target       The target.
     * @param targetIdx    The target index.
     * @param isRecurrent  True, if this is recurrent.
     */
    private void connectLayersFromBasic(final VectorNeuralNetwork network,
                                        final int fromLayerIdx, final FreeformLayer source,
                                        final int sourceIdx, final FreeformLayer target,
                                        final int targetIdx, final boolean isRecurrent) {

        for (int targetNeuronIdx = 0; targetNeuronIdx < target.size(); targetNeuronIdx++) {
            for (int sourceNeuronIdx = 0; sourceNeuronIdx < source.size(); sourceNeuronIdx++) {
                final FreeformNeuron sourceNeuron = source.getNeurons().get(
                        sourceNeuronIdx);
                final FreeformNeuron targetNeuron = target.getNeurons().get(
                        targetNeuronIdx);

                // neurons with no input (i.e. bias neurons)
                if (targetNeuron.getInputSummation() == null) {
                    continue;
                }

                final FreeformConnection connection = this.connectionFactory
                        .factor(sourceNeuron, targetNeuron);
                sourceNeuron.addOutput(connection);
                targetNeuron.addInput(connection);
                final double weight = network.getWeight(fromLayerIdx,
                        sourceNeuronIdx, targetNeuronIdx);
                connection.setWeight(weight);
            }
        }
    }

    /**
     * Create a context connection, such as those used by Jordan/Elmann.
     *
     * @param source The source layer.
     * @param target The target layer.
     * @return The newly created context layer.
     */
    public FreeformLayer createContext(final FreeformLayer source,
                                       final FreeformLayer target) {
        final double biasActivation = 0.0;
        EncogActivationFunction activatonFunction = null;

        if (source.getNeurons().get(0).getOutputs().size() < 1) {
            throw new FreeformNetworkError(
                    "A layer cannot have a context layer connected if there are no other outbound connections from the source layer.  Please connect the source layer somewhere else first.");
        }

        activatonFunction = source.getNeurons().get(0).getInputSummation().getActivationFunction();

        // first create the context layer
        final FreeformLayer result = this.layerFactory.factor();

        for (int i = 0; i < source.size(); i++) {
            final FreeformNeuron neuron = source.getNeurons().get(i);
            if (neuron.isBias()) {
                final FreeformNeuron biasNeuron = this.neuronFactory
                        .factorRegular(null);
                biasNeuron.setBias(true);
                biasNeuron.setActivation(neuron.getActivation());
                result.add(biasNeuron);
            } else {
                final FreeformNeuron contextNeuron = this.neuronFactory
                        .factorContext(neuron);
                result.add(contextNeuron);
            }
        }

        // now connect the context layer to the target layer

        connectLayers(result, target, activatonFunction, biasActivation, false);

        return result;
    }

    /**
     * Create the input layer.
     *
     * @param neuronCount The input neuron count.
     * @return The newly created layer.
     */
    public FreeformLayer createInputLayer(final int neuronCount) {
        if (neuronCount < 1) {
            throw new FreeformNetworkError(
                    "Input layer must have at least one neuron.");
        }
        this.inputLayer = createLayer(neuronCount);
        return this.inputLayer;
    }

    /**
     * Create a hidden layer.
     *
     * @param neuronCount The neuron count.
     * @return The newly created layer.
     */
    public FreeformLayer createLayer(final int neuronCount) {
        if (neuronCount < 1) {
            throw new FreeformNetworkError(
                    "Layer must have at least one neuron.");
        }

        final FreeformLayer result = this.layerFactory.factor();

        // Add the neurons for this layer
        for (int i = 0; i < neuronCount; i++) {
            result.add(this.neuronFactory.factorRegular(null));
        }

        return result;
    }

    /**
     * Create the output layer.
     *
     * @param neuronCount The neuron count.
     * @return The newly created output layer.
     */
    public FreeformLayer createOutputLayer(final int neuronCount) {
        if (neuronCount < 1) {
            throw new FreeformNetworkError(
                    "Output layer must have at least one neuron.");
        }
        this.outputLayer = createLayer(neuronCount);
        return this.outputLayer;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void decodeFromArray(final double[] encoded) {
        int index = 0;
        final Set<FreeformNeuron> visited = new HashSet<>();
        final List<FreeformNeuron> queue = new ArrayList<>();

        this.outputLayer.getNeurons().stream().forEach(queue::add);

        while (queue.size() > 0) {
            // pop a neuron off the queue
            final FreeformNeuron neuron = queue.get(0);
            queue.remove(0);
            visited.add(neuron);

            // find anymore neurons and add them to the queue.
            if (neuron.getInputSummation() != null) {
                for (final FreeformConnection connection : neuron
                        .getInputSummation().list()) {
                    connection.setWeight(encoded[index++]);
                    final FreeformNeuron nextNeuron = connection.getSource();
                    if (!visited.contains(nextNeuron)) {
                        queue.add(nextNeuron);
                    }
                }
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int encodedArrayLength() {
        int result = 0;
        final Set<FreeformNeuron> visited = new HashSet<>();
        final List<FreeformNeuron> queue = new ArrayList<>();

        this.outputLayer.getNeurons().stream().forEach(queue::add);

        while (queue.size() > 0) {
            // pop a neuron off the queue
            final FreeformNeuron neuron = queue.get(0);
            queue.remove(0);
            visited.add(neuron);

            // find anymore neurons and add them to the queue.
            if (neuron.getInputSummation() != null) {
                for (final FreeformConnection connection : neuron
                        .getInputSummation().list()) {
                    result++;
                    final FreeformNeuron nextNeuron = connection.getSource();
                    if (!visited.contains(nextNeuron)) {
                        queue.add(nextNeuron);
                    }
                }
            }
        }

        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void encodeToArray(final double[] encoded) {
        int index = 0;
        final Set<FreeformNeuron> visited = new HashSet<>();
        final List<FreeformNeuron> queue = new ArrayList<>();

        this.outputLayer.getNeurons().stream().forEach(queue::add);

        while (queue.size() > 0) {
            // pop a neuron off the queue
            final FreeformNeuron neuron = queue.get(0);
            queue.remove(0);
            visited.add(neuron);

            // find anymore neurons and add them to the queue.
            if (neuron.getInputSummation() != null) {
                for (final FreeformConnection connection : neuron
                        .getInputSummation().list()) {
                    encoded[index++] = connection.getWeight();
                    final FreeformNeuron nextNeuron = connection.getSource();
                    if (!visited.contains(nextNeuron)) {
                        queue.add(nextNeuron);
                    }
                }
            }
        }

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getInputCount() {
        return this.inputLayer.sizeNonBias();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getOutputCount() {
        return this.outputLayer.sizeNonBias();
    }

    /**
     * @return The output layer.
     */
    public FreeformLayer getOutputLayer() {
        return this.outputLayer;
    }

    /**
     * Perform the specified connection task. This task will be performed over
     * all connections.
     *
     * @param task The connection task.
     */
    public void performConnectionTask(final ConnectionTask task) {
        final Set<FreeformNeuron> visited = new HashSet<>();

        this.outputLayer.getNeurons().stream().forEach((neuron) -> performConnectionTask(visited, neuron, task));
    }

    /**
     * Perform the specified connection task.
     *
     * @param visited      The list of visited neurons.
     * @param parentNeuron The parent neuron.
     * @param task         The task.
     */
    private void performConnectionTask(final Set<FreeformNeuron> visited,
                                       final FreeformNeuron parentNeuron, final ConnectionTask task) {
        visited.add(parentNeuron);

        // does this neuron have any inputs?
        if (parentNeuron.getInputSummation() != null) {
            parentNeuron
                    .getInputSummation().list().stream().map((connection) -> {
                task.task(connection);
                return connection;
            }).map(FreeformConnection::getSource).filter((neuron) -> (!visited.contains(neuron))).forEach((neuron) -> performConnectionTask(visited, neuron, task));
        }
    }

    /**
     * Perform the specified neuron task. This task will be executed over all
     * neurons.
     *
     * @param task
     */
    public void performNeuronTask(final NeuronTask task) {
        final Set<FreeformNeuron> visited = new HashSet<>();

        this.outputLayer.getNeurons().stream().forEach((neuron) -> performNeuronTask(visited, neuron, task));
    }

    /**
     * Perform the specified neuron task.
     *
     * @param visited      The visited list.
     * @param parentNeuron The neuron to start with.
     * @param task         The task to perform.
     */
    private void performNeuronTask(final Set<FreeformNeuron> visited,
                                   final FreeformNeuron parentNeuron, final NeuronTask task) {
        visited.add(parentNeuron);
        task.task(parentNeuron);

        // does this neuron have any inputs?
        if (parentNeuron.getInputSummation() != null) {
            parentNeuron
                    .getInputSummation().list().stream().map(FreeformConnection::getSource).filter((neuron) -> (!visited.contains(neuron))).forEach((neuron) -> performNeuronTask(visited, neuron, task));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void reset() {
        reset((int) (System.currentTimeMillis() % Integer.MAX_VALUE));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void reset(final int seed) {
        final ConsistentRandomizer randomizer = new ConsistentRandomizer(-1, 1,
                seed);

        /**
         * {@inheritDoc}
         */
        performConnectionTask(connection -> connection.setWeight(randomizer.nextDouble()));
    }

    /**
     * Allocate temp training space.
     *
     * @param neuronSize     The number of elements to allocate on each neuron.
     * @param connectionSize The number of elements to allocate on each connection.
     */
    public void tempTrainingAllocate(final int neuronSize,
                                     final int connectionSize) {
        performNeuronTask(neuron -> {
            neuron.allocateTempTraining(neuronSize);
            if (neuron.getInputSummation() != null) {
                neuron
                        .getInputSummation().list().stream().forEach((connection) -> connection.allocateTempTraining(connectionSize));
            }
        });
    }

    /**
     * Clear the temp training data.
     */
    public void tempTrainingClear() {
        performNeuronTask(neuron -> {
            neuron.clearTempTraining();
            if (neuron.getInputSummation() != null) {
                neuron
                        .getInputSummation().list().stream().forEach(TempTrainingData::clearTempTraining);
            }
        });
    }

    /**
     * Update context.
     */
    public void updateContext() {
        performNeuronTask(FreeformNeuron::updateContext);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateProperties() {
        // not needed
    }

}
