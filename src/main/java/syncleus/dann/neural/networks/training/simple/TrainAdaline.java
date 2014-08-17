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
package org.encog.neural.networks.training.simple;

import syncleus.dann.data.DataCase;
import syncleus.dann.data.Data;
import syncleus.dann.data.Dataset;
import syncleus.dann.learn.TrainingImplementationType;
import syncleus.dann.Learning;
import syncleus.dann.learn.BasicTraining;
import syncleus.dann.math.statistics.ErrorCalculation;
import syncleus.dann.neural.networks.BasicNetwork;

/**
 * Train an ADALINE neural network.
 */
public class TrainAdaline extends BasicTraining implements LearningRate {

    /**
     * The network to train.
     */
    private final BasicNetwork network;

    /**
     * The training data to use.
     */
    private final Dataset training;

    /**
     * The learning rate.
     */
    private double learningRate;

    /**
     * Construct an ADALINE trainer.
     *
     * @param network      The network to train.
     * @param training     The training data.
     * @param learningRate The learning rate.
     */
    public TrainAdaline(final BasicNetwork network, final Dataset training,
                        final double learningRate) {
        super(TrainingImplementationType.Iterative);
        if (network.getLayerCount() > 2) {
            throw new RuntimeException(
                    "An ADALINE network only has two layers.");
        }
        this.network = network;

        this.training = training;
        this.learningRate = learningRate;
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
     * {@inheritDoc}
     */
    @Override
    public void iteration() {

        final ErrorCalculation errorCalculation = new ErrorCalculation();

        for (final DataCase pair : this.training) {
            // calculate the error
            final Data output = this.network.compute(pair.getInput());

            for (int currentAdaline = 0; currentAdaline < output.size(); currentAdaline++) {
                final double diff = pair.getIdeal().getData(currentAdaline)
                        - output.getData(currentAdaline);

                // weights
                for (int i = 0; i <= this.network.getInputCount(); i++) {
                    final double input;

                    if (i == this.network.getInputCount()) {
                        input = 1.0;
                    } else {
                        input = pair.getInput().getData(i);
                    }

                    this.network.addWeight(0, i, currentAdaline,
                            this.learningRate * diff * input);
                }
            }

            errorCalculation.updateError(output.getData(), pair.getIdeal()
                    .getData(), pair.getSignificance());
        }

        // set the global error
        setError(errorCalculation.calculate());
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
     * Set the learning rate.
     *
     * @param rate The new learning rate.
     */
    @Override
    public void setLearningRate(final double rate) {
        this.learningRate = rate;
    }

}
