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

import syncleus.dann.Learning;
import syncleus.dann.Training;
import syncleus.dann.data.Dataset;
import syncleus.dann.data.VectorEncodable;
import syncleus.dann.evolve.MLMethodGeneticAlgorithm;
import syncleus.dann.learn.MLResettable;
import syncleus.dann.util.factory.MLTrainFactory;
import syncleus.dann.util.factory.parse.ArchitectureParse;

import java.util.Map;

/**
 * A factory to create genetic algorithm trainers.
 */
public class GeneticFactory {
    /**
     * Create an annealing trainer.
     *
     * @param method   The method to use.
     * @param training The training data to use.
     * @param argsStr  The arguments to use.
     * @return The newly created trainer.
     */
    public Training create(final Learning method, final Dataset training,
                          final String argsStr) {

        if (!(method instanceof VectorEncodable)) {
            throw new RuntimeException(
                    "Invalid method type, requires an encodable MLMethod");
        }

        final LearningScoring score = new TrainingSetScore(training);

        final Map<String, String> args = ArchitectureParse.parseParams(argsStr);
        final ParamsHolder holder = new ParamsHolder(args);
        final int populationSize = holder.getInt(
                MLTrainFactory.PROPERTY_POPULATION_SIZE, false, 5000);

        final Training train = new MLMethodGeneticAlgorithm(() -> {
            final Learning result = ObjectCloner.deepCopy(method);
            ((MLResettable) result).reset();
            return result;
        }, score, populationSize);

        return train;
    }
}
