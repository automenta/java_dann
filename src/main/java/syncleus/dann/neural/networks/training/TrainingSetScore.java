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
package syncleus.dann.neural.networks.training;

import syncleus.dann.data.buffer.BufferedMLDataSet;
import syncleus.dann.data.Dataset;
import syncleus.dann.Learning;
import syncleus.dann.RegressionLearning;
import syncleus.dann.learn.LearningScoring;
import syncleus.dann.math.EncogUtility;

/**
 * Calculate a score based on a training set. This class allows simulated
 * annealing or genetic algorithms just as you would any other training set
 * based training method. The method must support regression (MLRegression).
 */
public class TrainingSetScore implements LearningScoring {

    /**
     * The training set.
     */
    private final Dataset training;

    /**
     * Construct a training set score calculation.
     *
     * @param training The training data to use.
     */
    public TrainingSetScore(final Dataset training) {
        this.training = training;
    }

    /**
     * Calculate the score for the network.
     *
     * @param method The network to calculate for.
     * @return The score.
     */
    @Override
    public Double apply(final Learning method) {
        return EncogUtility.calculateRegressionError((RegressionLearning) method, this.training);
    }

    /**
     * A training set based score should always seek to lower the error, as a
     * result, this method always returns true.
     *
     * @return Returns true.
     */
    @Override
    public boolean shouldMinimize() {
        return true;
    }

    @Override
    public boolean requireSingleThreaded() {
        return this.training instanceof BufferedMLDataSet;
    }

}
