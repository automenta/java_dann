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
package syncleus.dann.neural.spiking.update_actions;

import syncleus.dann.neural.spiking.SpikingNeuralNetwork;
import syncleus.dann.neural.spiking.NetworkUpdateAction;

/**
 * Buffered update of loose items (neurons and synapses), i.e. items not in
 * groups. (Buffered update means order of update does not matter).
 *
 * @author jyoshimi
 */
public class BufferedUpdate implements NetworkUpdateAction {

    /** Reference to network to update. */
    private SpikingNeuralNetwork network;

    /**
     * @param network
     */
    public BufferedUpdate(SpikingNeuralNetwork network) {
        this.network = network;
    }

    @Override
    public void invoke() {
        network.bufferedUpdateAllNeurons();
        network.updateAllSynapses();
    }

    @Override
    public String getDescription() {
        return "Loose neurons (buffered) and synapses";
    }

    @Override
    public String getLongDescription() {
        return "Buffered update of loose items";
    }

}
