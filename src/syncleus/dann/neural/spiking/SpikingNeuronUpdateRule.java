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
package syncleus.dann.neural.spiking;

import syncleus.dann.neural.spiking.SpikingNeuralNetwork.TimeType;

/**
 * <b>SpikingNeuron</b> is the superclass for spiking neuron types (e.g.
 * integrate and fire) with functions common to spiking neurons. For example a
 * boolean hasSpiked field is used in the gui to indicate that this neuron has
 * spiked.
 */
public abstract class SpikingNeuronUpdateRule extends NeuronUpdateRule {

    {
        inputType = InputType.SYNAPTIC;
    }

    /** Time of last spike. */
    private double lastSpikeTime;

    /** Whether a spike has occurred in the current time. */
    private boolean hasSpiked;

    @Override
    public void clear(SpikingNeuron neuron) {
        super.clear(neuron);
        setLastSpikeTime(0);
    }

    /**
     * {@inheritDoc}
     */
    public TimeType getTimeType() {
        return TimeType.CONTINUOUS;
    }

    /**
     * {@inheritDoc}
     */
    public abstract void update(SpikingNeuron neuron);

    /**
     * @param hasSpiked the hasSpiked to set
     * @param neuron the neuron which has (or has not) spiked.
     */
    public void setHasSpiked(final boolean hasSpiked, final SpikingNeuron neuron) {
        if (hasSpiked == true) {
            lastSpikeTime = neuron.getNetwork().getTime();
        }
        this.hasSpiked = hasSpiked;
    }

    /**
     * Whether the neuron has spiked in this instant or not.
     *
     * @return true if the neuron spiked.
     */
    public boolean hasSpiked() {
        return hasSpiked;
    }

    /**
     * @return the lastSpikeTime
     */
    public double getLastSpikeTime() {
        return lastSpikeTime;
    }

    /**
     * @param lastSpikeTime the lastSpikeTime to set
     */
    public void setLastSpikeTime(double lastSpikeTime) {
        this.lastSpikeTime = lastSpikeTime;
    }

}
