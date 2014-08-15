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
package syncleus.dann.learn.train;

import java.util.ArrayList;
import java.util.List;

import syncleus.dann.learn.ml.MLDataSet;
import syncleus.dann.learn.ml.TrainingImplementationType;
import syncleus.dann.learn.train.strategy.Strategy;
import syncleus.dann.learn.train.strategy.end.EndTrainingStrategy;

/**
 * An abstract class that implements basic training for most training
 * algorithms. Specifically training strategies can be added to enhance the
 * training.
 *
 * @author jheaton
 */
public abstract class BasicTraining implements MLTrain {

    /**
     * The training strategies to use.
     */
    private final List<Strategy> strategies = new ArrayList<>();

    /**
     * The training data.
     */
    private MLDataSet training;

    /**
     * The current error rate.
     */
    private double error;

    /**
     * The current iteration.
     */
    private int iteration;

    private TrainingImplementationType implementationType;

    /**
     * Used for serialization.
     */
    public BasicTraining() {

    }

    public BasicTraining(final TrainingImplementationType implementationType) {
        this.implementationType = implementationType;
    }

    /**
     * Training strategies can be added to improve the training results. There
     * are a number to choose from, and several can be used at once.
     *
     * @param strategy The strategy to add.
     */
    @Override
    public void addStrategy(final Strategy strategy) {
        strategy.init(this);
        this.strategies.add(strategy);
    }

    /**
     * Should be called after training has completed and the iteration method
     * will not be called any further.
     */
    @Override
    public void finishTraining() {

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double getError() {
        return this.error;
    }

    /**
     * @return the iteration
     */
    @Override
    public int getIteration() {
        return this.iteration;
    }

    /**
     * @return The strategies to use.
     */
    @Override
    public List<Strategy> getStrategies() {
        return this.strategies;
    }

    /**
     * @return The training data to use.
     */
    @Override
    public MLDataSet getTraining() {
        return this.training;
    }

    /**
     * @return True if training can progress no further.
     */
    @Override
    public boolean isTrainingDone() {
        return this.strategies.stream().filter((strategy) -> (strategy instanceof EndTrainingStrategy)).map((strategy) -> (EndTrainingStrategy) strategy).anyMatch((end) -> (end.shouldStop()));

    }

    /**
     * Perform the specified number of training iterations. This is a basic
     * implementation that just calls iteration the specified number of times.
     * However, some training methods, particularly with the GPU, benefit
     * greatly by calling with higher numbers than 1.
     *
     * @param count The number of training iterations.
     */
    @Override
    public void iteration(final int count) {
        for (int i = 0; i < count; i++) {
            iteration();
        }
    }

    /**
     * Call the strategies after an iteration.
     */
    public void postIteration() {
        this.strategies.stream().forEach(Strategy::postIteration);
    }

    /**
     * Call the strategies before an iteration.
     */
    public void preIteration() {

        this.iteration++;

        this.strategies.stream().forEach(Strategy::preIteration);
    }

    /**
     * @param error Set the current error rate. This is usually used by training
     *              strategies.
     */
    @Override
    public void setError(final double error) {
        this.error = error;
    }

    /**
     * @param iteration the iteration to set
     */
    @Override
    public void setIteration(final int iteration) {
        this.iteration = iteration;
    }

    /**
     * Set the training object that this strategy is working with.
     *
     * @param training The training object.
     */
    public void setTraining(final MLDataSet training) {
        this.training = training;
    }

    @Override
    public TrainingImplementationType getImplementationType() {
        return this.implementationType;
    }

}
