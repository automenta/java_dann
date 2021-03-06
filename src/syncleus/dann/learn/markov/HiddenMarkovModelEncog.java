/*
 * Encog(tm) Core v3.2 - Java Version
 * http://www.heatonresearch.com/encog/
 * https://github.com/encog/encog-java-core

 * Copyright 2008-2013 Heaton Research, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * For more information on Heaton Research copyrights, licenses
 * and trademarks visit:
 * http://www.heatonresearch.com/copyright
 */
package syncleus.dann.learn.markov;

import java.io.Serializable;
import java.util.Iterator;
import syncleus.dann.StateSequenceLearning;
import syncleus.dann.data.DataCase;
import syncleus.dann.data.Dataset;
import syncleus.dann.learn.AbstractLearning;
import syncleus.dann.learn.markov.alog.ForwardBackwardCalculator;
import syncleus.dann.learn.markov.alog.ForwardBackwardScaledCalculator;
import syncleus.dann.learn.markov.alog.ViterbiCalculator;
import syncleus.dann.math.probablity.distributions.ContinousDistribution;
import syncleus.dann.math.probablity.distributions.DiscreteDistribution;
import syncleus.dann.math.probablity.distributions.StateDistribution;

/**
 * A Hidden Markov Model (HMM) is a Machine Learning Method that allows for
 * predictions to be made about the hidden states and observations of a given
 * system over time. A HMM can be thought of as a simple dynamic Bayesian
 * network. The HMM is dynamic as it deals with changes that unfold over time.
 * <p/>
 * The Hidden Markov Model is made up of a number of states and observations. A
 * simple example might be the state of the economy. There are three hidden
 * states, such as bull market, bear market and level. We do not know which
 * state we are currently in. However, there are observations that can be made
 * such as interest rate and the level of the S&P500. The HMM learns what state
 * we are in by seeing how the observations change over time.
 * <p/>
 * The HMM is only in one state at a given time. There is a percent probability
 * that the HMM will move from one state to any of the other states. These
 * probabilities are arranged in a grid, and are called the state transition
 * probabilities.
 * <p/>
 * Observations can be discrete or continuous. These observations allow the HMM
 * to predict state transitions.
 * <p/>
 * The HMM can handle single-value or multivariate observations.
 * <p/>
 * http://www.heatonresearch.com/wiki/Hidden_Markov_Model
 * <p/>
 * Rabiner, Juang, An introduction to Hidden Markov Models, IEEE ASSP Mag.,pp
 * 4-16, June 1986.
 * <p/>
 * Baum, L. E.; Petrie, T. (1966).
 * "Statistical Inference for Probabilistic Functions of Finite State Markov Chains"
 * The Annals of Mathematical Statistics 37 (6): 1554-1563.
 */
public class HiddenMarkovModelEncog extends AbstractLearning implements StateSequenceLearning,
        Serializable, Cloneable {
    /**
     * The serial id.
     */
    private static final long serialVersionUID = 1L;

    public static final String TAG_STATES = "sates";

    public static final String TAG_ITEMS = "items";

    public static final String TAG_PI = "pi";

    public static final String TAG_TRANSITION = "transition";

    public static final String TAG_DIST_TYPE = "type";

    public static final String TAG_MEAN = "mean";

    public static final String TAG_COVARIANCE = "covariance";

    public static final String TAG_PROBABILITIES = "probabilities";

    /**
     * The initial probabilities for each state.
     */
    private double pi[];

    /**
     * The transitional probabilities between the states.
     */
    private double transitionProbability[][];

    /**
     * The mapping of observation probabilities to the states.
     */
    private final StateDistribution[] stateDistributions;

    /**
     * The counts for each item in a discrete HMM.
     */
    private final int[] items;

    /**
     * Construct a discrete HMM with the specified number of states.
     *
     * @param states The number of states.
     */
    public HiddenMarkovModelEncog(final int states) {
        this.items = null;
        this.pi = new double[states];
        this.transitionProbability = new double[states][states];
        this.stateDistributions = new StateDistribution[states];

        for (int i = 0; i < states; i++) {
            this.pi[i] = 1. / states;

            this.stateDistributions[i] = new ContinousDistribution(
                    getStateCount());

            for (int j = 0; j < states; j++) {
                this.transitionProbability[i][j] = 1. / states;
            }
        }
    }

    public HiddenMarkovModelEncog(final int theStates, final int theItems) {
        this(theStates, new int[]{theItems});

    }

    public HiddenMarkovModelEncog(final int theStates, final int[] theItems) {
        this.items = theItems;
        this.pi = new double[theStates];
        this.transitionProbability = new double[theStates][theStates];
        this.stateDistributions = new StateDistribution[theStates];

        for (int i = 0; i < theStates; i++) {
            this.pi[i] = 1. / theStates;
            this.stateDistributions[i] = new DiscreteDistribution(this.items);

            for (int j = 0; j < theStates; j++) {
                this.transitionProbability[i][j] = 1.0 / theStates;
            }
        }
    }

    @Override
    public HiddenMarkovModelEncog clone() throws CloneNotSupportedException {
        final HiddenMarkovModelEncog hmm = cloneStructure();

        hmm.pi = this.pi.clone();
        hmm.transitionProbability = this.transitionProbability.clone();

        for (int i = 0; i < this.transitionProbability.length; i++) {
            hmm.transitionProbability[i] = this.transitionProbability[i]
                    .clone();
        }

        for (int i = 0; i < hmm.stateDistributions.length; i++) {
            hmm.stateDistributions[i] = this.stateDistributions[i].clone();
        }

        return hmm;
    }

    public HiddenMarkovModelEncog cloneStructure() {
        HiddenMarkovModelEncog hmm;

        if (isDiscrete()) {
            hmm = new HiddenMarkovModelEncog(getStateCount(), this.items);
        } else {
            hmm = new HiddenMarkovModelEncog(getStateCount());
        }

        return hmm;
    }

    public StateDistribution createNewDistribution() {
        if (isContinuous()) {
            return new ContinousDistribution(getStateCount());
        } else {
            return new DiscreteDistribution(this.items);
        }
    }

    public double getPi(final int i) {
        return this.pi[i];
    }

    public int getStateCount() {
        return this.pi.length;
    }

    public StateDistribution getStateDistribution(final int i) {
        return this.stateDistributions[i];
    }

    @Override
    public int[] getStatesForSequence(final Dataset seq) {
        return (new ViterbiCalculator(seq, this)).stateSequence();
    }

    public double getTransitionProbability(final int i, final int j) {
        return this.transitionProbability[i][j];
    }

    public boolean isContinuous() {
        return this.items == null;
    }

    public boolean isDiscrete() {
        return !isContinuous();
    }

    public double lnProbability(final Dataset seq) {
        return (new ForwardBackwardScaledCalculator(seq, this)).lnProbability();
    }

    @Override
    public double probability(final Dataset seq) {
        return (new ForwardBackwardCalculator(seq, this)).probability();
    }

    @Override
    public double probability(final Dataset seq, final int[] states) {
        if ((seq.size() != states.length) || (seq.size() < 1)) {
            throw new IllegalArgumentException();
        }

        double probability = getPi(states[0]);

        final Iterator<DataCase> oseqIterator = seq.iterator();

        for (int i = 0; i < (states.length - 1); i++) {
            probability *= getStateDistribution(states[i]).probability(
                    oseqIterator.next())
                    * getTransitionProbability(states[i], states[i + 1]);
        }

        return probability
                * getStateDistribution(states[states.length - 1]).probability(
                seq.get(states.length - 1));
    }

    public void setPi(final int i, final double value) {
        this.pi[i] = value;
    }

    public void setStateDistribution(final int i, final StateDistribution dist) {
        this.stateDistributions[i] = dist;
    }

    public void setTransitionProbability(final int i, final int j,
                                         final double value) {
        this.transitionProbability[i][j] = value;
    }

    @Override
    public void updateProperties() {

    }

    public int[] getItems() {
        return this.items;
    }

    public double[] getPi() {
        return this.pi;
    }

    public double[][] getTransitionProbability() {
        return this.transitionProbability;
    }

    public void setTransitionProbability(final double[][] data) {
        if (data.length != this.transitionProbability.length
                || data[0].length != this.transitionProbability[0].length) {
            throw new RuntimeException(
                    "Dimensions of transationalProbability must match number of states.");
        }
        this.transitionProbability = data;
    }

    public void setPi(final double[] data) {
        if (data.length != this.pi.length) {
            throw new RuntimeException(
                    "The length of pi, must match the number of states.");
        }
        this.pi = data;

    }
}
