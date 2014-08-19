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
import syncleus.dann.neural.spiking.util.Randomizer;
import syncleus.dann.neural.spiking.util.SquashingFunction;

/**
 * <b>SigmoidalRule</b> provides various implementations of a standard
 * sigmoidal neuron.
 *
 * TODO: Discuss renaming "DiscreteSigmoidalRule"
 *
 * @author Zach Tosi
 * @author Jeff Yoshimi
 */
public class SigmoidalRule extends AbstractSigmoidalRule {

    /**
     * Default sigmoidal.
     */
    public SigmoidalRule() {
        super();
    }

    /**
     * Construct a sigmoid update with a specified implementation.
     *
     * @param sFunction
     *            the squashing function implementation to use.
     */
    public SigmoidalRule(SquashingFunction sFunction) {
        super(sFunction);
    }

    /**
     * {@inheritDoc}
     */
    public TimeType getTimeType() {
        return TimeType.DISCRETE;
    }

    /**
     * {@inheritDoc}
     */
    public void update(SpikingNeuron neuron) {

        double val = inputType.getInput(neuron) + bias;

        // TODO: Discuss how noise is added
        if (addNoise) {
            val += noiseGenerator.getRandom();
        }

        val =
            sFunction
                .valueOf(val, getUpperBound(), getLowerBound(), getSlope());

        neuron.setBuffer(val);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void contextualIncrement(SpikingNeuron n) {
        double act = n.getActivation();
        if (act < getUpperBound()) {
            act += getIncrement();
            if (act > getUpperBound()) {
                act = getUpperBound();
            }
            n.setActivation(act);
            n.getNetwork().fireNeuronChanged(n);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void contextualDecrement(SpikingNeuron n) {
        double act = n.getActivation();
        if (act > getLowerBound()) {
            act -= getIncrement();
            if (act < getLowerBound()) {
                act = getLowerBound();
            }
            n.setActivation(act);
            n.getNetwork().fireNeuronChanged(n);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double getDerivative(final double val) {
        double up = getUpperBound();
        double lw = getLowerBound();
        double diff = up - lw;
        return sFunction.derivVal(val, up, lw, diff);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SigmoidalRule deepCopy() {
        SigmoidalRule sn = new SigmoidalRule();
        sn.setBias(getBias());
        sn.setSquashFunctionType(getSquashFunctionType());
        sn.setSlope(getSlope());
        sn.setAddNoise(getAddNoise());
        sn.noiseGenerator = new Randomizer(noiseGenerator);
        return sn;
    }

    @Override
    public String getDescription() {
        return "Sigmoidal (Discrete)";
    }

}
