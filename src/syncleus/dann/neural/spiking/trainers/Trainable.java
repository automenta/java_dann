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
package syncleus.dann.neural.spiking.trainers;

import java.util.List;

import syncleus.dann.neural.spiking.SpikingNeuron;

/**
 * Interface that must be implemented by any object that can be trained by a
 * Trainer.
 *
 * @author Jeff Yoshimi
 * @author Zach Tosi
 *
 */
public interface Trainable {

    /**
     * Returns the list of input neurons.
     *
     * @return the list of input neurons.
     */
    List<SpikingNeuron> getInputNeurons();

    /**
     * Returns the list of output neurons.
     *
     * @return list of output neurons
     */
    List<SpikingNeuron> getOutputNeurons();

    /**
     * Returns the the training set, which contains input and target data.
     *
     * @return the training set.
     */
    TrainingSet getTrainingSet();

    /**
     * Initialize the network. E.g. used before a training run for SRN's to
     * clear activations.
     */
    void initNetwork();

}
