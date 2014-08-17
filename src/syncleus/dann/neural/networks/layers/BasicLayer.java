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
package syncleus.dann.neural.networks.layers;

import syncleus.dann.neural.activation.ActivationTANH;
import syncleus.dann.neural.activation.EncogActivationFunction;
import syncleus.dann.neural.networks.VectorNeuralNetwork;

import java.io.Serializable;
import syncleus.dann.neural.flat.FlatLayer;

/**
 * Basic functionality that most of the neural layers require. The basic layer
 * is often used by itself to implement forward or recurrent layers. Other layer
 * types are based on the basic layer as well.
 * <p/>
 * The following summarizes how basic layers calculate the output for a neural
 * network.
 * <p/>
 * Example of a simple XOR network.
 * <p/>
 * Input: BasicLayer: 2 Neurons, null biasWeights, null biasActivation
 * <p/>
 * Hidden: BasicLayer: 2 Neurons, 2 biasWeights, 1 biasActivation
 * <p/>
 * Output: BasicLayer: 1 Neuron, 1 biasWeights, 1 biasActivation
 * <p/>
 * Input1Output and Input2Output are both provided.
 * <p/>
 * Synapse 1: Input to Hidden Hidden1Activation = (Input1Output *
 * Input1->Hidden1Weight) + (Input2Output * Input2->Hidden1Weight) +
 * (HiddenBiasActivation * Hidden1BiasWeight)
 * <p/>
 * Hidden1Output = calculate(Hidden1Activation, HiddenEncogActivationFunction)
 * <p/>
 * Hidden2Activation = (Input1Output * Input1->Hidden2Weight) + (Input2Output *
 * Input2->Hidden2Weight) + (HiddenBiasActivation * Hidden2BiasWeight)
 * <p/>
 * Hidden2Output = calculate(Hidden2Activation, HiddenEncogActivationFunction)
 * <p/>
 * Synapse 2: Hidden to Output
 * <p/>
 * Output1Activation = (Hidden1Output * Hidden1->Output1Weight) + (Hidden2Output
 * * Hidden2->Output1Weight) + (OutputBiasActivation * Output1BiasWeight)
 * <p/>
 * Output1Output = calculate(Output1Activation, OutputEncogActivationFunction)
 *
 * @author jheaton
 */
public class BasicLayer extends FlatLayer implements Layer, Serializable {
    /**
     * The serial id.
     */
    private static final long serialVersionUID = -5682296868750703898L;

    /**
     * The network that this layer belongs to.
     */
    private VectorNeuralNetwork network;

    /**
     * Construct this layer with a non-default activation function, also
     * determine if a bias is desired or not.
     *
     * @param activationFunction The activation function to use.
     * @param neuronCount        How many neurons in this layer.
     * @param hasBias            True if this layer has a bias.
     */
    public BasicLayer(final EncogActivationFunction activationFunction,
                      final boolean hasBias, final int neuronCount) {

        super(activationFunction, neuronCount, hasBias ? 1.0 : 0.0);
    }

    /**
     * Construct this layer with a sigmoid activation function.
     *
     * @param neuronCount How many neurons in this layer.
     */
    public BasicLayer(final int neuronCount) {
        this(new ActivationTANH(), true, neuronCount);
    }

    /**
     * @return The network that owns this layer.
     */
    @Override
    public VectorNeuralNetwork getNetwork() {
        return this.network;
    }

    /**
     * Set the network for this layer.
     *
     * @param network The network for this layer.
     */
    @Override
    public void setNetwork(final VectorNeuralNetwork network) {
        this.network = network;
    }

    @Override
    public int getNeuronCount() {
        return this.getCount();
    }

    @Override
    public EncogActivationFunction getActivationFunction() {
        return super.getActivation();
    }
}
