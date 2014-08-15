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
package syncleus.dann.util.factory.train;

import syncleus.dann.evolve.train.basic.TrainEA;
import syncleus.dann.learn.ml.CalculateScore;
import syncleus.dann.learn.ml.MLDataSet;
import syncleus.dann.learn.ml.MLMethod;
import syncleus.dann.learn.train.MLTrain;
import syncleus.dann.neural.neat.NEATPopulation;
import syncleus.dann.neural.neat.NEATUtil;
import syncleus.dann.neural.networks.training.TrainingSetScore;

/**
 * A factory to create genetic algorithm trainers.
 */
public class NEATGAFactory {
    /**
     * Create an NEAT GA trainer.
     *
     * @param method   The method to use.
     * @param training The training data to use.
     * @param argsStr  The arguments to use.
     * @return The newly created trainer.
     */
    public MLTrain create(final MLMethod method, final MLDataSet training,
                          final String argsStr) {

        final CalculateScore score = new TrainingSetScore(training);
        final TrainEA train = NEATUtil.constructNEATTrainer(
                (NEATPopulation) method, score);

        return train;
    }
}
