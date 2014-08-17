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
package org.encog.neural.cpn.training;

import syncleus.dann.data.DataCase;
import syncleus.dann.data.Data;
import syncleus.dann.data.Dataset;
import syncleus.dann.learn.TrainingImplementationType;
import syncleus.dann.Learning;
import syncleus.dann.learn.BasicTraining;
import syncleus.dann.math.array.EngineArray;
import syncleus.dann.math.statistics.ErrorCalculation;

/**
 * Used for Instar training of a CPN neural network. A CPN network is a hybrid
 * supervised/unsupervised network. The Outstar training handles the supervised
 * portion of the training.
 */
public class TrainOutstar extends BasicTraining implements LearningRate {

    /**
     * The learning rate.
     */
    private double learningRate;

    /**
     * The network being trained.
     */
    private final CPN network;

    /**
     * The training data. Supervised training, so both input and ideal must be
     * provided.
     */
    private final Dataset training;

    /**
     * If the weights have not been initialized, then they must be initialized
     * before training begins. This will be done on the first iteration.
     */
    private boolean mustInit = true;

    /**
     * Construct the outstar trainer.
     *
     * @param theNetwork      The network to train.
     * @param theTraining     The training data, must provide ideal outputs.
     * @param theLearningRate The learning rate.
     */
    public TrainOutstar(final CPN theNetwork, final Dataset theTraining,
                        final double theLearningRate) {
        super(TrainingImplementationType.Iterative);
        this.network = theNetwork;
        this.training = theTraining;
        this.learningRate = theLearningRate;
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
    public double getLearningRate() {
        return this.learningRate;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Learning getMethod() {
        return this.network;
    }

    /**
     * Approximate the weights based on the input values.
     */
    private void initWeight() {
        for (int i = 0; i < this.network.getOutstarCount(); i++) {
            int j = 0;
            for (final DataCase pair : this.training) {
                this.network.getWeightsInstarToOutstar().set(j++, i,
                        pair.getIdeal().getData(i));
            }
        }
        this.mustInit = false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void iteration() {

        if (this.mustInit) {
            initWeight();
        }

        final ErrorCalculation error = new ErrorCalculation();

        for (final DataCase pair : this.training) {
            final Data out = this.network.computeInstar(pair.getInput());

            final int j = EngineArray.indexOfLargest(out.getData());
            for (int i = 0; i < this.network.getOutstarCount(); i++) {
                final double delta = this.learningRate
                        * (pair.getIdeal().getData(i) - this.network
                        .getWeightsInstarToOutstar().getNumber(j, i));
                this.network.getWeightsInstarToOutstar().add(j, i, delta);
            }

            final Data out2 = this.network.computeOutstar(out);
            error.updateError(out2.getData(), pair.getIdeal().getData(),
                    pair.getSignificance());
        }

        setError(error.calculate());
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
    public void setLearningRate(final double rate) {
        this.learningRate = rate;
    }

}
