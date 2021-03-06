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
package syncleus.dann.neural.spiking.connections;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import syncleus.dann.neural.spiking.SpikingNeuron;
import syncleus.dann.neural.spiking.SpikingSynapse;
import syncleus.dann.neural.spiking.SynapseUpdateRule;
import syncleus.dann.neural.spiking.util.Polarity;
import syncleus.dann.neural.spiking.util.PolarizedRandomizer;

/**
 * TODO.
 *
 * @author Zach Tosi
 */
public class ConnectionUtilities {

    /** The default excitatory strength. */
    public static final double DEFAULT_EXCITATORY_STRENGTH = 1;

    /** The default inhibitory strength. */
    public static final double DEFAULT_INHIBITORY_STRENGTH = -1;

    /**
     * Randomizes a collection of synapses based on excitatory and inhibitory
     * (polarized appropriately) randomizers, which cannot be the same
     * randomizer. This method will always attempt to maintain the ratio of
     * excitatory synapses specified. However if some of the source neurons are
     * themselves polarized, this may not always be possible. In such a case,
     * this method will get as close as possible to the desired ratio. This,
     * however is not recommended.
     *
     * If the source neurons to these synapses are themselves, by and large,
     * polarized, this method can be, but should <b>NOT</b> be used.
     *
     * Null values for either PolarizedRandomizer is permitted. Synapses are
     * assigned default strengths based on their polarity depending on which
     * randomizers are null.
     *
     * @param exciteRand
     * @param inhibRand
     * @param excitatoryRatio
     * @param synapses
     * @throws IllegalArgumentException
     */
    public static void randomizeAndPolarizeSynapses(
        Collection<SpikingSynapse> synapses, PolarizedRandomizer exciteRand,
        PolarizedRandomizer inhibRand, double excitatoryRatio)
        throws IllegalArgumentException {
        if (exciteRand.equals(inhibRand)) {
            throw new IllegalArgumentException("Randomization has failed."
                + " The excitatory and inhibitory randomizers cannot be"
                + " the same object.");
        } else if (excitatoryRatio > 1 || excitatoryRatio < 0) {
            throw new IllegalArgumentException("Randomization had failed."
                + " The ratio of excitatory synapses "
                + " cannot be greater than 1 or less than 0.");
        } else {
            checkPolarityMatches(exciteRand, Polarity.EXCITATORY);
            checkPolarityMatches(inhibRand, Polarity.INHIBITORY);
            int exciteCount = (int) (excitatoryRatio * synapses.size());
            int inhibCount = synapses.size() - exciteCount;
            int remaining = synapses.size();
            boolean excitatory = false;
            for (SpikingSynapse s : synapses) {
                excitatory = shouldBeExcitatory(excitatoryRatio, exciteCount,
                    inhibCount, s);
                // Set the strength based on the polarity.
                if (excitatory) {
                    s.setStrength(exciteRand != null ? exciteRand.getRandom()
                        : DEFAULT_EXCITATORY_STRENGTH);
                    exciteCount--;
                    // Change the excitatoryRatio to maintain balance
                    excitatoryRatio = exciteCount / (double) remaining;
                } else {
                    s.setStrength(inhibRand != null ? inhibRand.getRandom()
                        : DEFAULT_INHIBITORY_STRENGTH);
                    inhibCount--;
                    // Change the excitatoryRatio to maintain balance.
                    excitatoryRatio = (remaining - inhibCount)
                        / (double) remaining;
                }
                remaining--;
            }
        }
    }

    /**
     * Randomize and polarize synapses using default excitatory and inhibitory
     * polarizers (uniform 0 to 1).  See {@link #randomizeAndPolarizeSynapses(Collection, PolarizedRandomizer,
     * PolarizedRandomizer, double)}
     *
     * @param synapses the synapses to modify
     * @param excitatoryRatio the ration of excitatory to inhibitory synapses.
     */
    public static void randomizeAndPolarizeSynapses(
            Collection<SpikingSynapse> synapses, double excitatoryRatio) {
        PolarizedRandomizer exciteRand = new PolarizedRandomizer(
                Polarity.EXCITATORY);
        PolarizedRandomizer inhibRand = new PolarizedRandomizer(
                Polarity.INHIBITORY);
        randomizeAndPolarizeSynapses(synapses, exciteRand, inhibRand,
                excitatoryRatio);
    }

    /**
     *
     * @param synapses
     * @param exciteRand
     * @param inhibRand
     */
    public static void randomizeSynapses(Collection<SpikingSynapse> synapses,
        PolarizedRandomizer exciteRand, PolarizedRandomizer inhibRand) {
        if (exciteRand.equals(inhibRand)) {
            throw new IllegalArgumentException("Randomization has failed."
                + " The excitatory and inhibitory randomizers cannot be"
                + " the same object.");
        } else {
            checkPolarityMatches(exciteRand, Polarity.EXCITATORY);
            checkPolarityMatches(inhibRand, Polarity.INHIBITORY);
            boolean excitatory = false;
            for (SpikingSynapse s : synapses) {
                excitatory = s.getStrength() > 0;
                // Set the strength based on the polarity.
                if (excitatory) {
                    s.setStrength(exciteRand != null ? exciteRand.getRandom()
                        : DEFAULT_EXCITATORY_STRENGTH);
                } else {
                    s.setStrength(inhibRand != null ? inhibRand.getRandom()
                        : DEFAULT_INHIBITORY_STRENGTH);
                }
            }
        }
    }

    /**
     * Randomizes the excitatory synapses in the given list of synapses using
     * the given excitatory randomizer.
     *
     * @param synapses
     * @param exciteRand
     */
    public static void randomizeExcitatorySynapses(
        Collection<SpikingSynapse> synapses, PolarizedRandomizer exciteRand) {
        checkPolarityMatches(exciteRand, Polarity.EXCITATORY);
        for (SpikingSynapse s : synapses) {
            if (Polarity.EXCITATORY.equals(s.getSource().getPolarity())
                || s.getStrength() > 0) {
                s.setStrength(exciteRand != null ? exciteRand.getRandom()
                    : DEFAULT_EXCITATORY_STRENGTH);
            }
        }
    }

    /**
     * Randomizes the given synapses using the given excitatory randomizer
     * without checking first to make sure that the given synapses or their
     * source neurons are not inhibitory. Used for speed when the polarity of
     * the synapses in the list is known ahead of time.
     *
     * @param synapses
     * @param exciteRand
     */
    public static void randomizeExcitatorySynapsesUnsafe(
        Collection<SpikingSynapse> synapses, PolarizedRandomizer exciteRand) {
        checkPolarityMatches(exciteRand, Polarity.EXCITATORY);
        for (SpikingSynapse s : synapses) {
            s.setStrength(exciteRand != null ? exciteRand.getRandom()
                : DEFAULT_EXCITATORY_STRENGTH);
        }
    }

    /**
     * Randomizes the inhibitory synapses in the given list of synapses using
     * the given inhibitory randomizer.
     *
     * @param synapses
     * @param inhibRand
     */
    public static void randomizeInhibitorySynapses(
        Collection<SpikingSynapse> synapses, PolarizedRandomizer inhibRand) {
        checkPolarityMatches(inhibRand, Polarity.INHIBITORY);
        for (SpikingSynapse s : synapses) {
            if (Polarity.INHIBITORY.equals(s.getSource().getPolarity())
                || s.getStrength() < 0) {
                s.setStrength(inhibRand != null ? inhibRand.getRandom()
                    : DEFAULT_INHIBITORY_STRENGTH);
            }
        }
    }

    /**
     * Randomizes the given synapses using the given inhibitory randomizer
     * without checking first to make sure that the given synapses or their
     * source neurons are not excitatory. Used for speed when the polarity of
     * the synapses in the list is known ahead of time.
     *
     * @param synapses
     * @param inhibRand
     */
    public static void randomizeInhibitorySynapsesUnsafe(
        Collection<SpikingSynapse> synapses, PolarizedRandomizer inhibRand) {
        checkPolarityMatches(inhibRand, Polarity.INHIBITORY);
        for (SpikingSynapse s : synapses) {
            s.setStrength(inhibRand != null ? inhibRand.getRandom()
                : DEFAULT_INHIBITORY_STRENGTH);
        }
    }

    /**
     *
     * @param synapses
     * @return
     */
    public static ArrayList<SpikingSynapse> getExcitatorySynapses(
        Collection<SpikingSynapse> synapses) {
        ArrayList<SpikingSynapse> exSyns = new ArrayList<SpikingSynapse>(synapses.size() / 2);
        for (SpikingSynapse s : synapses) {
            if (s.getStrength() > 0 || Polarity.EXCITATORY.equals(s.getSource()
                .getPolarity())) {
                exSyns.add(s);
            }
        }
        return exSyns;
    }

    /**
     *
     * @param synapses
     * @return
     */
    public static ArrayList<SpikingSynapse> getInhibitorySynapses(
        Collection<SpikingSynapse> synapses) {
        ArrayList<SpikingSynapse> inSyns = new ArrayList<SpikingSynapse>(synapses.size() / 2);
        for (SpikingSynapse s : synapses) {
            if (s.getStrength() < 0 || Polarity.INHIBITORY.equals(s.getSource()
                .getPolarity())) {
                inSyns.add(s);
            }
        }
        return inSyns;
    }

    /**
     * Changes all the synapses in a given collection such that
     * <b>excitatoryRatio</b> of them are excitatory and <b>1 -
     * excitatoryRatio</b> of them are inhibitory, assigning default strengths
     * respectively to each.
     *
     * This method will attempt to maintain the requested excitatoryRatio even
     * if some or all of the source neurons are themselves polarized. In such
     * cases the polarity of the Neurons efferent synapses will not be
     * overridden. Though it may not be possible to obtain the desired
     * excitatoryRatio in this case, this method will get as close as
     * possible.
     *
     * @param synapses the synapses to polarize
     * @param excitatoryRatio the ration of excitatory synapses (1 for all
     *            exctitatory)
     */
    public static void polarizeSynapses(Collection<SpikingSynapse> synapses,
        double excitatoryRatio) {
        if (excitatoryRatio > 1 || excitatoryRatio < 0) {
            throw new IllegalArgumentException("Randomization had failed."
                + " The ratio of excitatory synapses "
                + " cannot be greater than 1 or less than 0.");
        } else {
            int exciteCount = (int) (excitatoryRatio * synapses.size());
            int inhibCount = synapses.size() - exciteCount;
            int remaining = synapses.size();
            boolean excitatory = false;
            for (SpikingSynapse s : synapses) {
                excitatory = shouldBeExcitatory(excitatoryRatio, exciteCount,
                    inhibCount, s);
                // Set the strength based on the polarity.
                if (excitatory) {
                    s.setStrength(DEFAULT_EXCITATORY_STRENGTH);
                    exciteCount--;
                    // Change the excitatoryRatio to maintain balance
                    excitatoryRatio = exciteCount / (double) remaining;
                } else {
                    s.setStrength(DEFAULT_INHIBITORY_STRENGTH);
                    inhibCount--;
                    // Change the excitatoryRatio to maintain balance.
                    excitatoryRatio = (remaining - inhibCount)
                        / (double) remaining;
                }
                remaining--;
            }
        }
    }

    /**
     * Makes the synapses in the given collection conform to the parameters of
     * the given template synapses, which are essentially information ferries.
     * Throws an exception if the template synapses do not match the appropriate
     * polarities implied by their names.
     *
     * @param synapses
     * @param exTemplateSynapse
     * @param inTemplateSynapse
     */
    public static void conformToTemplates(Collection<SpikingSynapse> synapses,
        SpikingSynapse exTemplateSynapse, SpikingSynapse inTemplateSynapse) {
        if (exTemplateSynapse.getStrength() <= 0) {
            throw new IllegalArgumentException("Excitatory template synapse" +
                " must be excitatory (having strength > 0).");
        }
        if (inTemplateSynapse.getStrength() >= 0) {
            throw new IllegalArgumentException("Inhibitory template synapse" +
                " must be inhibitory (having strength < 0).");
        }
        for (SpikingSynapse s : synapses) {
            if (s.getStrength() < 0) {
                s.setStrength(inTemplateSynapse.getStrength());
                s.setLearningRule(inTemplateSynapse.getLearningRule()
                    .deepCopy());
                s.setUpperBound(inTemplateSynapse.getUpperBound());
                s.setLowerBound(inTemplateSynapse.getLowerBound());
                s.setDelay(inTemplateSynapse.getDelay());
                s.setEnabled(inTemplateSynapse.isEnabled());
                s.setFrozen(inTemplateSynapse.isFrozen());
                s.setIncrement(inTemplateSynapse.getIncrement());
                if (inTemplateSynapse.getSpikeResponder() != null) {
                    s.setSpikeResponder(inTemplateSynapse.getSpikeResponder()
                        .deepCopy());
                }
            }
            if (s.getStrength() > 0) {
                s.setStrength(exTemplateSynapse.getStrength());
                s.setLearningRule(exTemplateSynapse.getLearningRule()
                    .deepCopy());
                s.setUpperBound(exTemplateSynapse.getUpperBound());
                s.setLowerBound(exTemplateSynapse.getLowerBound());
                s.setDelay(exTemplateSynapse.getDelay());
                s.setEnabled(exTemplateSynapse.isEnabled());
                s.setFrozen(exTemplateSynapse.isFrozen());
                s.setIncrement(exTemplateSynapse.getIncrement());
                if (exTemplateSynapse.getSpikeResponder() != null) {
                    s.setSpikeResponder(exTemplateSynapse.getSpikeResponder()
                        .deepCopy());
                }
            }
        }
    }

    /**
     *
     * @param inQuestion
     * @param expectedPolarity
     * @throws IllegalArgumentException
     */
    private static void checkPolarityMatches(PolarizedRandomizer inQuestion,
        Polarity expectedPolarity) throws IllegalArgumentException {
        // TODO: Use "optional" instead when upgrade to Java 8
        if (inQuestion == null)
            return;
        if (!expectedPolarity.equals(inQuestion.getPolarity())) {
            throw new IllegalArgumentException("Randomizer's polarity does"
                + " not match its implied polarity");
        }
    }

    /**
     *
     * @param excitatoryRatio
     * @param exciteCount
     * @param inhibCount
     * @param s
     * @return
     */
    private static boolean shouldBeExcitatory(double excitatoryRatio,
        int exciteCount, int inhibCount, SpikingSynapse s) {
        boolean excitatory = false;
        if (s.getSource().isPolarized()) {
            if (Polarity.EXCITATORY == s.getSource().getPolarity()) {
                excitatory = true;
            } else {
                excitatory = false;
            }
        } else {
            if (exciteCount <= 0 || inhibCount <= 0) {
                if (exciteCount <= 0) {
                    excitatory = false;
                }
                if (inhibCount <= 0) {
                    excitatory = true;
                }
            } else {
                double exciteOrInhib = Math.random();
                if (exciteOrInhib < excitatoryRatio) {
                    excitatory = true;
                } else {
                    excitatory = false;
                }
            }
        }
        return excitatory;
    }

    /**
     * Tests whether or not these connections are recurrent, that is, whether or
     * not the neurons in the source list are the same as those in the target
     * list.
     *
     * @return true or false: whether or not these connections are recurrent.
     */
    public static boolean testRecurrence(List<SpikingNeuron> sourceNeurons,
        List<SpikingNeuron> targetNeurons) {
        if (sourceNeurons.size() != targetNeurons.size()) {
            return false;
        } else {
            for (int i = 0; i < sourceNeurons.size(); i++) {
                if (sourceNeurons.get(i) != targetNeurons.get(i)) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     *
     * @param exciteRule
     * @param inhibRule
     * @param synapses
     */
    public static void applyLearningRules(SynapseUpdateRule exciteRule,
        SynapseUpdateRule inhibRule, List<SpikingSynapse> synapses) {
        for (SpikingSynapse s : synapses) {
            if (s.getStrength() < 0) {
                s.setLearningRule(inhibRule);
            } else {
                s.setLearningRule(exciteRule);
            }
        }
    }

}
