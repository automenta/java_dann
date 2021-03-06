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
package syncleus.dann.util.concurrency.job;

import java.util.ArrayList;
import java.util.List;
import syncleus.dann.Training;
import syncleus.dann.data.Dataset;
import syncleus.dann.learn.strategy.Strategy;
import syncleus.dann.learn.strategy.end.EndTrainingStrategy;
import syncleus.dann.neural.VectorNeuralNetwork;

/**
 * Base class for all concurrent training jobs.
 */
public abstract class TrainingJob {

    /**
     * The network to train.
     */
    private VectorNeuralNetwork network;

    /**
     * The training data to use.
     */
    private Dataset training;

    /**
     * The strategies to use.
     */
    private final List<Strategy> strategies = new ArrayList<>();

    /**
     * True, if binary training data should be loaded to memory.
     */
    private boolean loadToMemory;

    /**
     * The trainer being used.
     */
    private Training train;

    /**
     * Holds any errors that occur during training.
     */
    private Throwable error;

    /**
     * Construct a training job.
     *
     * @param network      The network to train.
     * @param training     The training data to use.
     * @param loadToMemory True, if binary data should be loaded to memory.
     */
    public TrainingJob(final VectorNeuralNetwork network, final Dataset training,
                       final boolean loadToMemory) {
        super();
        this.network = network;
        this.training = training;
        this.loadToMemory = loadToMemory;
    }

    /**
     * Create a trainer to use.
     */
    public abstract void createTrainer(boolean singleThreaded);

    /**
     * @return the error
     */
    public Throwable getError() {
        return this.error;
    }

    /**
     * @return the network
     */
    public VectorNeuralNetwork getNetwork() {
        return this.network;
    }

    /**
     * @return the strategies
     */
    public List<Strategy> getStrategies() {
        return this.strategies;
    }

    /**
     * @return the train
     */
    public Training getTrain() {
        return this.train;
    }

    /**
     * @return the training
     */
    public Dataset getTraining() {
        return this.training;
    }

    /**
     * @return the loadToMemory
     */
    public boolean isLoadToMemory() {
        return this.loadToMemory;
    }

    /**
     * @param error the error to set
     */
    public void setError(final Throwable error) {
        this.error = error;
    }

    /**
     * @param loadToMemory the loadToMemory to set
     */
    public void setLoadToMemory(final boolean loadToMemory) {
        this.loadToMemory = loadToMemory;
    }

    /**
     * @param network the network to set
     */
    public void setNetwork(final VectorNeuralNetwork network) {
        this.network = network;
    }

    /**
     * @param train the train to set
     */
    public void setTrain(final Training train) {
        this.train = train;
    }

    /**
     * @param training the training to set
     */
    public void setTraining(final Dataset training) {
        this.training = training;
    }

    /**
     * @return True, if training should continue.
     */
    public boolean shouldContinue() {
        return this.train.getStrategies().stream().filter((strategy) -> (strategy instanceof EndTrainingStrategy)).map((strategy) -> (EndTrainingStrategy) strategy).noneMatch((end) -> (end.shouldStop()));
    }
}
