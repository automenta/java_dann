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
package syncleus.dann.evolve.train.basic;

import java.util.ArrayList;
import java.util.List;
import syncleus.dann.Learning;
import syncleus.dann.Training;
import syncleus.dann.data.Dataset;
import syncleus.dann.evolve.exception.EAError;
import syncleus.dann.evolve.population.Population;
import syncleus.dann.learn.AbstractTraining.TrainingImplementationType;
import syncleus.dann.learn.ScoreLearning;
import syncleus.dann.learn.strategy.Strategy;
import syncleus.dann.neural.flat.propagation.TrainingContinuation;
import syncleus.dann.neural.util.TrainingSetScore;

/**
 * Provides a MLTrain compatible class that can be used to train genomes.
 */
public class TrainEA extends BasicEA implements Training {

    /**
     * The serial ID.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Create a trainer for a score function.
     *
     * @param thePopulation    The population.
     * @param theScoreFunction The score function.
     */
    public TrainEA(final Population thePopulation,
                   final ScoreLearning theScoreFunction) {
        super(thePopulation, theScoreFunction);
    }

    /**
     * Create a trainer for training data.
     *
     * @param thePopulation The population.
     * @param trainingData  The training data.
     */
    public TrainEA(final Population thePopulation, final Dataset trainingData) {
        super(thePopulation, new TrainingSetScore(trainingData));
    }

    /**
     * Not used.
     *
     * @param error Not used.
     */
    @Override
    public void setError(final double error) {
    }

    /**
     * @return True if training can progress no further.
     */
    @Override
    public boolean isTrainingDone() {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TrainingImplementationType getImplementationType() {
        return TrainingImplementationType.Iterative;
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
     * Not supported, will throw an error.
     *
     * @param strategy Not used.
     */
    @Override
    public void addStrategy(final Strategy strategy) {
        throw new EAError("Strategies are not supported by this training method.");
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
    public void finishTraining() {
        super.finishTraining();
        this.getPopulation().setBestGenome(this.getBestGenome());
    }

    /**
     * @return A network created for the best genome.
     */
    @Override
    public Learning getMethod() {
        return this.getPopulation();
    }

    /**
     * Returns null, does not use a training set, rather uses a score function.
     *
     * @return null, not used.
     */
    @Override
    public Dataset getTraining() {
        return null;
    }

    /**
     * Returns an empty list, strategies are not supported.
     *
     * @return The strategies in use(none).
     */
    @Override
    public List<Strategy> getStrategies() {
        return new ArrayList<>();
    }
}
