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
import syncleus.dann.neural.spiking.neuron_update_rules.interfaces.BoundedUpdateRule;

/**
 * <b>SpikingNeuron</b> is the superclass for spiking neuron types (e.g.
 * integrate and fire) with functions common to spiking neurons. For example a
 * boolean hasSpiked field is used in the gui to indicate that this neuron has
 * spiked.
 */
abstract public class SpikingNeuronUpdateRule  {


    /** The maximum number of digits to display in the tool tip. */
    private static final int MAX_DIGITS = 9;

    /** The default increment of a neuron using this rule. */
    public static final double DEFAULT_INCREMENT = 0.1;

    protected InputType inputType = InputType.WEIGHTED;

    /** Amount by which to increment or decrement neuron. */
    protected double increment = DEFAULT_INCREMENT;


    /**
     * Increment a neuron by increment.
     */
    public final void incrementActivation(SpikingNeuron n) {
        n.forceSetActivation(n.getActivation() + increment);
        n.getNetwork().fireNeuronChanged(n);
    }

    /**
     * Decrement a neuron by increment.
     */
    public final void decrementActivation(SpikingNeuron n) {
        n.forceSetActivation(n.getActivation() - increment);
        n.getNetwork().fireNeuronChanged(n);
    }

    /**
     * Increment a neuron by increment, respecting neuron specific constraints.
     * Intended to be overriden.
     */
    public void contextualIncrement(SpikingNeuron n) {
        incrementActivation(n);
    }

    /**
     * Decrement a neuron by increment, respecting neuron specific constraints.
     * Intended to be overriden.
     */
    public void contextualDecrement(SpikingNeuron n) {
        decrementActivation(n);
    }

    /**
     * Returns a random value between the upper and lower bounds of this neuron.
     * Update rules that require special randomization should override this
     * method.
     *
     * @return the random value.
     */
    public double getRandomValue() {
        if (this instanceof BoundedUpdateRule) {
            return (((BoundedUpdateRule) this).getUpperBound()
                - ((BoundedUpdateRule) this)
                .getLowerBound())
                * Math.random()
                + ((BoundedUpdateRule) this).getLowerBound();
        } else {
            return 2 * Math.random() - 1;
        }

    }

    /**
     * Set activation to 0; override for other "clearing" behavior (e.g. setting
     * other variables to 0. Called in Gui when "clear" button pressed.
     *
     * @param neuron reference to parent neuron
     */
    public void clear(final SpikingNeuron neuron) {
        neuron.forceSetActivation(0);
        setLastSpikeTime(0);   
    }

    /**
     * Returns a brief description of this update rule. Used in combo boxes in
     * the GUI.
     *
     * @return the description.
     */
    public abstract String getDescription();


    /**
     * @return the increment
     */
    public double getIncrement() {
        return increment;
    }

    /**
     * @param increment the increment to set
     */
    public void setIncrement(double increment) {
        this.increment = increment;
    }

    /**
     * @return the defaultIncrement
     */
    public static double getDefaultIncrement() {
        return DEFAULT_INCREMENT;
    }

    public InputType getInputType() {
        return inputType;
    }

    public void setInputType(InputType inputType) {
        this.inputType = inputType;
    }

    abstract public SpikingNeuronUpdateRule deepCopy();

    
    /**
     * An enum specifying how a neuron sums its inputs. The enum both specifies
     * and provides the appropriate method for the distinct ways this can
     * happen. At its core it represents the connectionist (matrix 
     * multiplication equivalent/algebraic) vs biological (convolution or other
     * function of a "spike" represented as a Dirac delta function) weighted
     * sums.
     * 
     * @author Zach Tosi
     */
    public static enum InputType {
        WEIGHTED {
            /**
             * Gets the weighted sum of the pre-synaptic neurons' activation 
             * values.
             */
            @Override
            public double getInput(SpikingNeuron n) {
                return n.getWeightedInputs();
            }

            @Override
            public String toString() {
                return "Weighted";
            }
        },
        SYNAPTIC {
            /**
             * Gets the synaptic sum of the pre-synaptic neurons' firing state
             * weighted by synapses and processed through a spike responder.
             */
            @Override
            public double getInput(SpikingNeuron n) {
                return n.getSynapticInput();
            }

            @Override
            public String toString() {
                return "Synaptic";
            }
        };
        public abstract double getInput(SpikingNeuron n);
    }

    /** Time of last spike. */
    private double lastSpikeTime;

    /** Whether a spike has occurred in the current time. */
    private boolean hasSpiked;

    
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
