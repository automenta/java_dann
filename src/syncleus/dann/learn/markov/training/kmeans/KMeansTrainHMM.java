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
package syncleus.dann.learn.markov.training.kmeans;

import syncleus.dann.Learning;
import syncleus.dann.Training;
import syncleus.dann.data.DataCase;
import syncleus.dann.data.DataSequence;
import syncleus.dann.data.Dataset;
import syncleus.dann.data.vector.VectorDataset;
import syncleus.dann.learn.markov.HiddenMarkovModel;
import syncleus.dann.learn.markov.alog.ViterbiCalculator;
import syncleus.dann.math.probablity.distributions.StateDistribution;
import syncleus.dann.learn.kmeans.KMeansUtil;
import syncleus.dann.learn.strategy.Strategy;
import syncleus.dann.math.VectorDistance;
import syncleus.dann.neural.networks.training.propagation.TrainingContinuation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import syncleus.dann.data.Data;
import syncleus.dann.learn.AbstractTraining.TrainingImplementationType;

/**
 * Train a Hidden Markov Model (HMM) with the KMeans algorithm. Makes use of
 * KMeans clustering to estimate the transitional and observational
 * probabilities for the HMM.
 * <p/>
 * Unlike Baum Welch training, this method does not require a prior estimate of
 * the HMM model, it starts from scratch.
 * <p/>
 * Faber, Clustering and the Continuous k-Means Algorithm, Los Alamos Science,
 * no. 22, 1994.
 */
public class KMeansTrainHMM<D extends Data> implements Training {
    private final Clusters clusters;
    private final int states;
    private final DataSequence<D> sequnces;
    private boolean done;
    private final HiddenMarkovModel modelHMM;
    private int iteration;
    private HiddenMarkovModel method;
    private final DataSequence training;

    public KMeansTrainHMM(final HiddenMarkovModel method, final DataSequence sequences) {
        this(method, sequences, new VectorDistance.EuclideanVectorDistance());
    }
            
    public KMeansTrainHMM(final HiddenMarkovModel method, final DataSequence sequences, VectorDistance distanceFunc) {
        this.method = method;
        this.modelHMM = method;
        this.sequnces = sequences;
        this.states = method.getStateCount();
        this.training = sequences;
        this.clusters = new Clusters(this.states, sequences, distanceFunc);
        this.done = false;
    }

    @Override
    public void addStrategy(final Strategy strategy) {
    }

    @Override
    public boolean canContinue() {
        return false;
    }

    @Override
    public void finishTraining() {

    }

    @Override
    public double getError() {
        return this.done ? 0 : 100;
    }

    @Override
    public TrainingImplementationType getImplementationType() {
        return TrainingImplementationType.Iterative;
    }

    @Override
    public int getIteration() {
        return this.iteration;
    }

    @Override
    public Learning getMethod() {
        return this.method;
    }

    @Override
    public List<Strategy> getStrategies() {
        return null;
    }

    @Override
    public Dataset getTraining() {
        return this.training;
    }

    @Override
    public boolean isTrainingDone() {
        return this.done;
    }

    @Override
    public void iteration() {
        final HiddenMarkovModel hmm = this.modelHMM.cloneStructure();

        learnPi(hmm);
        learnTransition(hmm);
        learnOpdf(hmm);

        this.done = optimizeCluster(hmm);

        this.method = hmm;
    }

    @Override
    public void iteration(final int count) {
        // this.iteration = count;

    }

    private void learnOpdf(final HiddenMarkovModel hmm) {
        for (int i = 0; i < hmm.getStateCount(); i++) {
            final Collection<DataCase> clusterObservations = this.clusters
                    .cluster(i);

            if (clusterObservations.size() < 1) {
                final StateDistribution o = this.modelHMM
                        .createNewDistribution();
                hmm.setStateDistribution(i, o);
            } else {
                final Dataset temp = new VectorDataset();
                clusterObservations.stream().forEach(temp::add);
                hmm.getStateDistribution(i).fit(temp);
            }
        }
    }

    private void learnPi(final HiddenMarkovModel hmm) {
        final double[] pi = new double[this.states];

        for (int i = 0; i < this.states; i++) {
            pi[i] = 0.;
        }

        this.sequnces.getSequences().stream().forEach((sequence) -> pi[this.clusters.cluster(sequence.get(0))]++);

        for (int i = 0; i < this.states; i++) {
            hmm.setPi(i, pi[i] / this.sequnces.size());
        }
    }

    private void learnTransition(final HiddenMarkovModel hmm) {
        for (int i = 0; i < hmm.getStateCount(); i++) {
            for (int j = 0; j < hmm.getStateCount(); j++) {
                hmm.setTransitionProbability(i, j, 0.);
            }
        }

        this.sequnces.getSequences().stream().filter((obsSeq) -> !(obsSeq.size() < 2)).forEach((obsSeq) -> {
            int first_state;
            int second_state = this.clusters.cluster(obsSeq.get(0));
            for (int i = 1; i < obsSeq.size(); i++) {
                first_state = second_state;
                second_state = this.clusters.cluster(obsSeq.get(i));

                hmm.setTransitionProbability(
                        first_state,
                        second_state,
                        hmm.getTransitionProbability(first_state, second_state) + 1.);
            }
        });

		/* Normalize Aij array */
        for (int i = 0; i < hmm.getStateCount(); i++) {
            double sum = 0;

            for (int j = 0; j < hmm.getStateCount(); j++) {
                sum += hmm.getTransitionProbability(i, j);
            }

            if (sum == 0.) {
                for (int j = 0; j < hmm.getStateCount(); j++) {
                    hmm.setTransitionProbability(i, j, 1. / hmm.getStateCount());
                }
            } else {
                for (int j = 0; j < hmm.getStateCount(); j++) {
                    hmm.setTransitionProbability(i, j,
                            hmm.getTransitionProbability(i, j) / sum);
                }
            }
        }
    }

    private boolean optimizeCluster(final HiddenMarkovModel hmm) {
        boolean modif = false;

        for (final Dataset obsSeq : this.sequnces.getSequences()) {
            final ViterbiCalculator vc = new ViterbiCalculator(obsSeq, hmm);
            final int states[] = vc.stateSequence();

            for (int i = 0; i < states.length; i++) {
                final DataCase o = obsSeq.get(i);

                if (this.clusters.cluster(o) != states[i]) {
                    modif = true;
                    this.clusters.remove(o, this.clusters.cluster(o));
                    this.clusters.put(o, states[i]);
                }
            }
        }

        return !modif;
    }

    @Override
    public TrainingContinuation pause() {
        return null;
    }


    @Override
    public void setError(final double error) {

    }

    @Override
    public void setIteration(final int iteration) {
        this.iteration = iteration;
    }

    @Override
    public void resume(TrainingContinuation state) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    static class Clusters<D extends Data> {
        private final HashMap<DataCase<D>, Integer> clustersHash;
        private final ArrayList<Collection<DataCase<D>>> clusters;

        public Clusters(final int k, final Dataset<D> observations, VectorDistance distanceFunc) {

            this.clustersHash = new HashMap<>();
            this.clusters = new ArrayList<>();

            final List<DataCase<D>> list = new ArrayList<>();
            for (final DataCase<D> pair : observations) {
                list.add(pair);
            }
            final KMeansUtil<DataCase<D>> kmc = new KMeansUtil<>(k, list, distanceFunc);
            kmc.process();

            for (int i = 0; i < k; i++) {
                final Collection<DataCase<D>> cluster = kmc.get(i);
                this.clusters.add(cluster);

                for (final DataCase element : cluster) {
                    this.clustersHash.put(element, i);
                }
            }
        }

        public Collection<DataCase<D>> cluster(final int clusterNb) {
            return this.clusters.get(clusterNb);
        }

        public int cluster(final DataCase<D> o) {
            return this.clustersHash.get(o);
        }

        public boolean isInCluster(final DataCase<D> o, final int x) {
            return cluster(o) == x;
        }

        public void put(final DataCase<D> o, final int clusterNb) {
            this.clustersHash.put(o, clusterNb);
            this.clusters.get(clusterNb).add(o);
        }

        public void remove(final DataCase<D> o, final int clusterNb) {
            this.clustersHash.put(o, -1);
            this.clusters.get(clusterNb).remove(o);
        }
    }
    
}