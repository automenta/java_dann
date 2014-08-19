/*
 * Part of Simbrain--a java-based neural network kit
 * Copyright (C) 2005,2007 The Authors.  See http://www.simbrain.net/credits
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package syncleus.dann.neural.spiking.listeners;

import syncleus.dann.neural.spiking.SpikingNeuron;
import syncleus.dann.neural.spiking.NeuronUpdateRule;

/**
 * Listener interface for receiving events relating to neurons. Classes
 * interested in responding to such events are registered with a Network, which
 * broadcasts those events to registered observer classes.
 */
public interface NeuronListener {

    /**
     * Notify this listener of a Neuron changed event.
     *
     * @param networkEvent holds reference to changed neuron
     */
    void neuronChanged(NetworkEvent<SpikingNeuron> networkEvent);

    /**
     * Notify this listener of a Neuron type changed event.
     *
     * @param networkEvent holds reference to old and new Neuron
     */
    void neuronTypeChanged(NetworkEvent<NeuronUpdateRule> networkEvent);

    /**
     * Notify this listener of a text labelchanged event.
     *
     * @param networkEvent holds reference to changed Neuron
     */
    void labelChanged(NetworkEvent<SpikingNeuron> networkEvent);

    /**
     * Notify this listener of a Neuron added event.
     *
     * @param networkEvent reference to new neuron
     */
    void neuronAdded(NetworkEvent<SpikingNeuron> networkEvent);

    /**
     * Notify this listener of a Neuron moved event.
     *
     * @param networkEvent reference to neuron
     */
    void neuronMoved(NetworkEvent<SpikingNeuron> networkEvent);

    /**
     * Notify this listener of a Neuron removed event.
     *
     * @param networkEvent reference to Neuron
     */
    void neuronRemoved(NetworkEvent<SpikingNeuron> networkEvent);
}