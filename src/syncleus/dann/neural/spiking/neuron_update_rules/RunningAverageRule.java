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
package syncleus.dann.neural.spiking.neuron_update_rules;

import syncleus.dann.neural.spiking.SpikingNeuralNetwork.TimeType;
import syncleus.dann.neural.spiking.SpikingNeuron;
import syncleus.dann.neural.spiking.NeuronUpdateRule;

/**
 * <b>RunningAverageNeuron</b> keeps a running average of current and past
 * activity.
 *
 * TODO: Currently explodes. Fix and improve. See
 * http://en.wikipedia.org/wiki/Moving_average
 */
public class RunningAverageRule extends NeuronUpdateRule {

    /** Rate constant variable. */
    private double rateConstant = .5;

    /** Last activation. */
    private double val = 0;

    /**
     * {@inheritDoc}
     */
    public TimeType getTimeType() {
        return TimeType.DISCRETE;
    }

    /**
     * {@inheritDoc}
     */
    public void init(SpikingNeuron neuron) {
        // No implementation
    }

    /**
     * {@inheritDoc}
     */
    public RunningAverageRule deepCopy() {
        RunningAverageRule cn = new RunningAverageRule();
        cn.setRateConstant(getRateConstant());
        return cn;
    }

    /**
     * {@inheritDoc}
     */
    public void update(SpikingNeuron neuron) {
        // "val" on right is activation at last time step
        val = rateConstant * inputType.getInput(neuron) + (1 - rateConstant)
            * val;
        neuron.setBuffer(val);
    }

    /**
     * @return Rate constant.
     */
    public double getRateConstant() {
        return rateConstant;
    }

    /**
     * @param rateConstant Parameter to be set.
     */
    public void setRateConstant(final double rateConstant) {
        this.rateConstant = rateConstant;
    }

    @Override
    public String getDescription() {
        return "Running average";
    }

}
