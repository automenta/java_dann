/*
 * Part of Simbrain--a java-based neural network kit Copyright (C) 2005,2007 The
 * Authors. See http://www.simbrain.net/credits This program is free software;
 * you can redistribute it and/or modify it under the terms of the GNU General
 * Public License as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version. This program is
 * distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU General Public License for more details. You
 * should have received a copy of the GNU General Public License along with this
 * program; if not, write to the Free Software Foundation, Inc., 59 Temple Place
 * - Suite 330, Boston, MA 02111-1307, USA.
 */
package syncleus.dann.neural.spiking.subnetworks;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

import syncleus.dann.neural.spiking.connections.AllToAll;
import syncleus.dann.neural.spiking.SpikingNeuralNetwork;
import syncleus.dann.neural.spiking.SpikingNeuron;
import syncleus.dann.neural.spiking.SpikingSynapse;
import syncleus.dann.neural.spiking.groups.NeuronGroup;
import syncleus.dann.neural.spiking.groups.Subnetwork;
import syncleus.dann.neural.spiking.groups.SynapseGroup;
import syncleus.dann.neural.spiking.neuron_update_rules.LinearRule;
import syncleus.dann.neural.spiking.neuron_update_rules.SigmoidalRule;
import syncleus.dann.neural.spiking.synapse_update_rules.StaticSynapseRule;
import syncleus.dann.neural.spiking.util.NetworkLayoutManager;
import syncleus.dann.neural.spiking.util.NetworkLayoutManager.Direction;

/**
 * A standard feed-forward network, where a succession of neuron groups and
 * synapse groups are organized into layers.
 *
 * @author Jeff Yoshimi
 */
public class FeedForward extends Subnetwork {

    /** Space to put between layers. */
    private int betweenLayerInterval = 150;

    /**
     * Construct a feed-forward network.
     *
     * @param network
     *            the parent network to which the layered network is being added
     * @param nodesPerLayer
     *            an array of integers which determines the number of layers and
     *            neurons in each layer. Integers 1...n in the array correspond
     *            to the number of nodes in layers 1...n.
     * @param initialPosition
     *            bottom corner where network will be placed.
     * @param inputNeuronTemplate
     *            the type of Neuron to use for the input layer
     */
    public FeedForward(final SpikingNeuralNetwork network, int[] nodesPerLayer,
        Point2D initialPosition, final SpikingNeuron inputNeuronTemplate) {
        super(network);
        buildNetwork(network, nodesPerLayer, initialPosition,
            inputNeuronTemplate);
    }

    /**
     * Add the layered network to the specified network, with a specified number
     * of layers and nodes in each layer.
     *
     * @param network
     *            the parent network to which the layered network is being added
     * @param nodesPerLayer
     *            an array of integers which determines the number of layers and
     *            neurons in each layer. Integers 1...n in the array correspond
     *            to the number of nodes in layers 1...n.
     * @param initialPosition
     *            upper left corner where network will be placed.
     */
    public FeedForward(final SpikingNeuralNetwork network, int[] nodesPerLayer,
        Point2D initialPosition) {
        
        super(network);
        
        LinearRule rule = new LinearRule();
        SpikingNeuron neuron = new SpikingNeuron(network, rule);
        rule.setIncrement(1); // For easier testing
        rule.setLowerBound(0);
        buildNetwork(network, nodesPerLayer, initialPosition, neuron);
    }
    

    /**
     * Create the network using the parameters.
     *
     * @param network
     *            the parent network to which the layered network is being added
     * @param nodesPerLayer
     *            an array of integers which determines the number of layers and
     *            neurons in each layer. Integers 1...n in the array correspond
     *            to the number of nodes in layers 1...n.
     * @param initialPosition
     *            bottom corner where network will be placed.
     * @param inputNeuronTemplate
     *            the type of Neuron to use for the input layer
     */
    private void buildNetwork(final SpikingNeuralNetwork network, int[] nodesPerLayer,
        Point2D initialPosition, final SpikingNeuron inputNeuronTemplate) {


        // Set up input layer
        List<SpikingNeuron> inputLayerNeurons = new ArrayList<SpikingNeuron>();
        for (int i = 0; i < nodesPerLayer[0]; i++) {
            inputLayerNeurons.add(new SpikingNeuron(network, inputNeuronTemplate));
        }
        NeuronGroup inputLayer = new NeuronGroup(network, inputLayerNeurons);
        inputLayer.setClamped(true); // Clamping makes everything easier in the
                                     // GUI. The trainer uses forceset.
        addNeuronGroup(inputLayer);
        inputLayer.setLayoutBasedOnSize(initialPosition);

        // Prepare base synapse for connecting layers
        SpikingSynapse synapse = SpikingSynapse.getTemplateSynapse(new StaticSynapseRule());
        synapse.setLowerBound(-1);
        synapse.setUpperBound(1);

        // Memory of last layer created
        NeuronGroup lastLayer = inputLayer;

        // Make hidden layers and output layer
        for (int i = 1; i < nodesPerLayer.length; i++) {
            List<SpikingNeuron> hiddenLayerNeurons = new ArrayList<SpikingNeuron>();
            for (int j = 0; j < nodesPerLayer[i]; j++) {
                SigmoidalRule rule = new SigmoidalRule();
                SpikingNeuron neuron = new SpikingNeuron(network, rule);
                rule.setLowerBound(0);
                neuron.setUpdatePriority(i);
                hiddenLayerNeurons.add(neuron);
            }

            NeuronGroup hiddenLayer = new NeuronGroup(network,
                hiddenLayerNeurons);
            hiddenLayer.setLayoutBasedOnSize();
            addNeuronGroup(hiddenLayer);
            NetworkLayoutManager.offsetNeuronGroup(lastLayer, hiddenLayer,
                Direction.NORTH, betweenLayerInterval);

            AllToAll connection = new AllToAll();
            SynapseGroup lh = connectNeuronGroups(lastLayer, hiddenLayer,
                connection);
            lh.randomizeConnectionWeights();

            // Reset last layer
            lastLayer = hiddenLayer;
        }
    }

    /**
     * @return the betweenLayerInterval
     */
    public int getBetweenLayerInterval() {
        return betweenLayerInterval;
    }

    /**
     * @param betweenLayerInterval
     *            the betweenLayerInterval to set
     */
    public void setBetweenLayerInterval(int betweenLayerInterval) {
        this.betweenLayerInterval = betweenLayerInterval;
    }

    @Override
    public void addNeuronGroup(NeuronGroup group) {
        super.addNeuronGroup(group);
    }

    /**
     * Returns the input layer.
     *
     * @return the input layer
     */
    public NeuronGroup getInputLayer() {
        return getNeuronGroup(0);
    }

    /**
     * Returns the output layer.
     *
     * @return the output layer
     */
    public NeuronGroup getOutputLayer() {
        return getNeuronGroup(getNeuronGroupCount() - 1);
    }

    /**
     * Convenience method for getting the neurons associated with the input
     * group. Also allows all feed-forward networks to implement Trainable.
     *
     * @return the input layer neurons as a list.
     */
    public List<SpikingNeuron> getInputNeurons() {
        return getInputLayer().getNeuronList();
    }

    /**
     * Convenience method for getting the neurons associated with the output
     * group. Also allows all feed-forward networks to implement Trainable.
     *
     * @return the output layer neurons as a list.
     */
    public List<SpikingNeuron> getOutputNeurons() {
        return getOutputLayer().getNeuronList();
    }

    @Override
    public String getUpdateMethodDesecription() {
        return "Layered update";
    }

}
