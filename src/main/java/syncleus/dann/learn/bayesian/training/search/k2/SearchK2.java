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
package syncleus.dann.learn.bayesian.training.search.k2;

import java.util.ArrayList;
import java.util.List;

import syncleus.dann.learn.bayesian.BayesianEvent;
import syncleus.dann.learn.bayesian.EncogBayesianNetwork;
import syncleus.dann.learn.bayesian.query.enumerate.EnumerationQuery;
import syncleus.dann.learn.bayesian.training.TrainBayesian;
import syncleus.dann.learn.ml.MLDataPair;
import syncleus.dann.learn.ml.MLDataSet;
import syncleus.dann.math.EncogMath;

/**
 * Search for optimal Bayes structure with K2.
 */
public class SearchK2 implements BayesSearch {

    /**
     * The data to use.
     */
    private MLDataSet data;

    /**
     * The network to optimize.
     */
    private EncogBayesianNetwork network;

    /**
     * The trainer being used.
     */
    private TrainBayesian train;

    /**
     * The last calculated value for p.
     */
    private double lastCalculatedP;

    /**
     * The node ordering.
     */
    private final List<BayesianEvent> nodeOrdering = new ArrayList<>();

    /**
     * THe current index.
     */
    private int index = -1;

    /**
     * {@inheritDoc}
     */
    @Override
    public void init(final TrainBayesian theTrainer,
                     final EncogBayesianNetwork theNetwork, final MLDataSet theData) {
        this.network = theNetwork;
        this.data = theData;
        this.train = theTrainer;
        orderNodes();
        this.index = -1;
    }

    /**
     * Basically the goal here is to get the classification target, if it
     * exists, to go first. This will greatly enhance K2's effectiveness.
     */
    private void orderNodes() {
        this.nodeOrdering.clear();

        // is there a classification target?
        if (this.network.getClassificationTarget() != -1) {
            this.nodeOrdering.add(this.network.getClassificationTargetEvent());
        }

        this.network.getEvents().stream().filter((event) -> (!this.nodeOrdering.contains(event))).forEach(this.nodeOrdering::add);
    }

    /**
     * Find the value for z.
     *
     * @param event The event that we are clauclating for.
     * @param n     The value for n.
     * @param old   The old value.
     * @return The new value for z.
     */
    private BayesianEvent findZ(final BayesianEvent event, final int n,
                                final double old) {
        BayesianEvent result = null;
        double maxChildP = Double.NEGATIVE_INFINITY;
        // System.out.println("Finding parent for: " + event.toString());
        for (int i = 0; i < n; i++) {
            final BayesianEvent trialParent = this.nodeOrdering.get(i);
            final List<BayesianEvent> parents = new ArrayList<>();
            parents.addAll(event.getParents());
            parents.add(trialParent);
            // System.out.println("Calculating adding " + trialParent.toString()
            // + " to " + event.toString());
            this.lastCalculatedP = this.calculateG(network, event, parents);
            // System.out.println("lastP:" + this.lastCalculatedP);
            // System.out.println("old:" + old);
            if (this.lastCalculatedP > old && this.lastCalculatedP > maxChildP) {
                result = trialParent;
                maxChildP = this.lastCalculatedP;
                // System.out.println("Current best is: " + result.toString());
            }
        }

        this.lastCalculatedP = maxChildP;
        return result;
    }

    /**
     * Calculate the value N, which is the number of cases, from the training
     * data, where the desiredValue matches the training data. Only cases where
     * the parents match the specifed parent instance are considered.
     *
     * @param network        The network to calculate for.
     * @param event          The event we are calculating for. (variable i)
     * @param parents        The parents of the specified event we are considering.
     * @param parentInstance The parent instance we are looking for.
     * @param desiredValue   The desired value.
     * @return The value N.
     */
    public int calculateN(final EncogBayesianNetwork network,
                          final BayesianEvent event, final List<BayesianEvent> parents,
                          final int[] parentInstance, final int desiredValue) {
        int result = 0;
        final int eventIndex = network.getEventIndex(event);

        for (final MLDataPair pair : this.data) {
            final int[] d = this.network.determineClasses(pair.getInput());

            if (d[eventIndex] == desiredValue) {
                boolean reject = false;

                for (int i = 0; i < parentInstance.length; i++) {
                    final BayesianEvent parentEvent = parents.get(i);
                    final int parentIndex = network.getEventIndex(parentEvent);
                    if (parentInstance[i] != d[parentIndex]) {
                        reject = true;
                        break;
                    }
                }

                if (!reject) {
                    result++;
                }
            }
        }
        return result;
    }

    /**
     * Calculate the value N, which is the number of cases, from the training
     * data, where the desiredValue matches the training data. Only cases where
     * the parents match the specifed parent instance are considered.
     *
     * @param network        The network to calculate for.
     * @param event          The event we are calculating for. (variable i)
     * @param parents        The parents of the specified event we are considering.
     * @param parentInstance The parent instance we are looking for.
     * @return The value N.
     */
    public int calculateN(final EncogBayesianNetwork network,
                          final BayesianEvent event, final List<BayesianEvent> parents,
                          final int[] parentInstance) {
        int result = 0;

        for (final MLDataPair pair : this.data) {
            final int[] d = this.network.determineClasses(pair.getInput());

            boolean reject = false;

            for (int i = 0; i < parentInstance.length; i++) {
                final BayesianEvent parentEvent = parents.get(i);
                final int parentIndex = network.getEventIndex(parentEvent);
                if (parentInstance[i] != (d[parentIndex])) {
                    reject = true;
                    break;
                }
            }

            if (!reject) {
                result++;
            }
        }
        return result;
    }

    /**
     * Calculate G.
     *
     * @param network The network to calculate for.
     * @param event   The event to calculate for.
     * @param parents The parents.
     * @return The value for G.
     */
    public double calculateG(final EncogBayesianNetwork network,
                             final BayesianEvent event, final List<BayesianEvent> parents) {
        double result = 1.0;
        final int r = event.getChoices().size();

        final int[] args = new int[parents.size()];

        do {
            final double n = EncogMath.factorial(r - 1);
            final double d = EncogMath.factorial(calculateN(network, event,
                    parents, args) + r - 1);
            final double p1 = n / d;

            double p2 = 1;
            for (int k = 0; k < event.getChoices().size(); k++) {
                p2 *= EncogMath.factorial(calculateN(network, event, parents,
                        args, k));
            }

            result *= p1 * p2;
        } while (EnumerationQuery.roll(parents, args));

        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean iteration() {

        if (index == -1) {
            orderNodes();
        } else {
            final BayesianEvent event = this.nodeOrdering.get(index);
            double oldP = this.calculateG(network, event, event.getParents());

            while (event.getParents().size() < this.train.getMaximumParents()) {
                final BayesianEvent z = findZ(event, index, oldP);
                if (z != null) {
                    this.network.createDependency(z, event);
                    oldP = this.lastCalculatedP;
                } else {
                    break;
                }
            }
        }

        index++;
        return (index < this.data.getInputSize());
    }

}
