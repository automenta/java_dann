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
package syncleus.dann.learn.bayesian.training;

import syncleus.dann.Learning;
import syncleus.dann.data.Dataset;
import syncleus.dann.learn.AbstractTraining;
import syncleus.dann.learn.bayesian.BayesianEvent;
import syncleus.dann.learn.bayesian.BayesianNetworkEncog;
import syncleus.dann.learn.bayesian.training.estimator.BayesEstimator;
import syncleus.dann.learn.bayesian.training.estimator.SimpleEstimator;
import syncleus.dann.learn.bayesian.training.search.k2.BayesSearch;
import syncleus.dann.learn.bayesian.training.search.k2.SearchK2;
import syncleus.dann.neural.flat.propagation.TrainingContinuation;

/**
 * Train a Bayesian network.
 */
public class TrainBayesian extends AbstractTraining {

    /**
    * The method by which a Bayesian network should be initialized.
    */
   public enum BayesianInit {
       /**
        * No init, do not change anything.
        */
       InitNoChange,

       /**
        * Start with no connections.
        */
       InitEmpty,

       /**
        * Init as Naive Bayes.
        */
       InitNaiveBayes
   }

    /**
     * What phase of training are we in?
     */
    private enum Phase {
        /**
         * Init phase.
         */
        Init,
        /**
         * Searching for a network structure.
         */
        Search,
        /**
         * Search complete.
         */
        SearchDone,
        /**
         * Finding probabilities.
         */
        Probability,
        /**
         * Finished training.
         */
        Finish,
        /**
         * Training terminated.
         */
        Terminated
    }

    /**
     * The phase that training is currently in.
     */
    private Phase p = Phase.Init;

    /**
     * The data used for training.
     */
    private final Dataset data;

    /**
     * The network to train.
     */
    private final BayesianNetworkEncog network;

    /**
     * The maximum parents a node should have.
     */
    private final int maximumParents;

    /**
     * The method used to search for the best network structure.
     */
    private final BayesSearch search;

    /**
     * The method used to estimate the probabilities.
     */
    private final BayesEstimator estimator;

    /**
     * The method used to setup the initial Bayesian network.
     */
    private BayesianInit initNetwork = BayesianInit.InitNaiveBayes;

    /**
     * Used to hold the query.
     */
    private String holdQuery;

    /**
     * Construct a Bayesian trainer. Use K2 to search, and the SimpleEstimator
     * to estimate probability. Init as Naive Bayes
     *
     * @param theNetwork        The network to train.
     * @param theData           The data to train.
     * @param theMaximumParents The max number of parents.
     */
    public TrainBayesian(final BayesianNetworkEncog theNetwork,
                         final Dataset theData, final int theMaximumParents) {
        this(theNetwork, theData, theMaximumParents,
                BayesianInit.InitNaiveBayes, new SearchK2(),
                new SimpleEstimator());
    }

    /**
     * Construct a Bayesian trainer.
     *
     * @param theNetwork        The network to train.
     * @param theData           The data to train with.
     * @param theMaximumParents The maximum number of parents.
     * @param theInit           How to init the new Bayes network.
     * @param theSearch         The search method.
     * @param theEstimator      The estimation mehod.
     */
    public TrainBayesian(final BayesianNetworkEncog theNetwork,
                         final Dataset theData, final int theMaximumParents,
                         final BayesianInit theInit, final BayesSearch theSearch,
                         final BayesEstimator theEstimator) {
        super(TrainingImplementationType.Iterative);
        this.network = theNetwork;
        this.data = theData;
        this.maximumParents = theMaximumParents;

        this.search = theSearch;
        this.search.init(this, theNetwork, theData);

        this.estimator = theEstimator;
        this.estimator.init(this, theNetwork, theData);

        this.initNetwork = theInit;
        setError(1.0);
    }

    /**
     * Init to Naive Bayes.
     */
    private void initNaiveBayes() {
        // clear out anything from before
        this.network.removeAllRelations();

        // locate the classification target event
        final BayesianEvent classificationTarget = this.network
                .getClassificationTargetEvent();

        this.network.getEvents().stream().filter((event) -> (event != classificationTarget)).forEach((event) -> network.createDependency(classificationTarget, event));
        this.network.finalizeStructure();

    }

    /**
     * Handle iterations for the Init phase.
     */
    private void iterationInit() {
        this.holdQuery = this.network.getClassificationStructure();

        switch (this.initNetwork) {
            case InitEmpty:
                this.network.removeAllRelations();
                this.network.finalizeStructure();
                break;
            case InitNoChange:
                break;
            case InitNaiveBayes:
                initNaiveBayes();
                break;
        }
        this.p = Phase.Search;
    }

    /**
     * Handle iterations for the Search phase.
     */
    private void iterationSearch() {
        if (!this.search.iteration()) {
            this.p = Phase.SearchDone;
        }
    }

    /**
     * Handle iterations for the Search Done phase.
     */
    private void iterationSearchDone() {
        this.network.finalizeStructure();
        this.network.reset();
        this.p = Phase.Probability;
    }

    /**
     * Handle iterations for the Probability phase.
     */
    private void iterationProbability() {
        if (!this.estimator.iteration()) {
            this.p = Phase.Finish;
        }
    }

    /**
     * Handle iterations for the Finish phase.
     */
    private void iterationFinish() {
        this.network.defineClassificationStructure(this.holdQuery);
        setError(this.network.calculateError(this.data));
        this.p = Phase.Terminated;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isTrainingDone() {
        if (super.isTrainingDone())
            return true;
        else
            return this.p == Phase.Terminated;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void iteration() {

        postIteration();

        switch (p) {
            case Init:
                iterationInit();
                break;
            case Search:
                iterationSearch();
                break;
            case SearchDone:
                iterationSearchDone();
                break;
            case Probability:
                iterationProbability();
                break;
            case Finish:
                iterationFinish();
                break;
        }

        preIteration();

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean canContinue() {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TrainingContinuation pause() {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void resume(final TrainingContinuation state) {

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Learning getMethod() {
        return this.network;
    }

    /**
     * @return the network
     */
    public BayesianNetworkEncog getNetwork() {
        return network;
    }

    /**
     * @return the maximumParents
     */
    public int getMaximumParents() {
        return maximumParents;
    }

    /**
     * @return The search method.
     */
    public BayesSearch getSearch() {
        return this.search;
    }

    /**
     * @return The init method.
     */
    public BayesianInit getInitNetwork() {
        return initNetwork;
    }

    /**
     * Set the network init method.
     *
     * @param initNetwork The init method.
     */
    public void setInitNetwork(final BayesianInit initNetwork) {
        this.initNetwork = initNetwork;
    }

}
