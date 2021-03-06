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
import syncleus.dann.neural.spiking.SpikingNeuralNetwork;
import syncleus.dann.neural.spiking.SpikingNeuron;
import syncleus.dann.neural.spiking.neuron_update_rules.LinearRule;
import syncleus.dann.neural.spiking.trainers.Trainable;
import syncleus.dann.neural.spiking.trainers.TrainingSet;

/**
 * Backprop network.
 *
 * @author Jeff Yoshimi
 */
public class BackpropNetwork extends FeedForward implements Trainable {

    /**
     * Training set.
     */
    private final TrainingSet trainingSet = new TrainingSet();

    /**
     * Construct a new backprop network.
     *
     * @param network reference to root network
     * @param nodesPerLayer number of layers
     * @param initialPosition initial position in network
     */
    public BackpropNetwork(SpikingNeuralNetwork network, int[] nodesPerLayer,
            Point2D initialPosition) {
        super(network, nodesPerLayer, initialPosition, new SpikingNeuron(network,
                new LinearRule()));
        setLabel("Backprop");

    }

    @Override
    public TrainingSet getTrainingSet() {
        return trainingSet;
    }

    @Override
    public void initNetwork() {
    }
}
