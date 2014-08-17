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

import syncleus.dann.learn.CalculateScore;
import syncleus.dann.data.Dataset;
import syncleus.dann.data.VectorEncodable;
import syncleus.dann.Learning;
import syncleus.dann.Training;
import syncleus.dann.neural.networks.BasicNetwork;
import syncleus.dann.util.factory.MLTrainFactory;
import syncleus.dann.util.factory.parse.ArchitectureParse;

import java.util.Map;

/**
 * A factory to create simulated annealing trainers.
 */
public class AnnealFactory {
    /**
     * Create an annealing trainer.
     *
     * @param method   The method to use.
     * @param training The training data to use.
     * @param argsStr  The arguments to use.
     * @return The newly created trainer.
     */
    public static Training create(final Learning method, final Dataset training,
                                 final String argsStr) {

        if (!(method instanceof BasicNetwork)) {
            throw new TrainingError(
                    "Invalid method type, requires BasicNetwork");
        }

        final CalculateScore score = new TrainingSetScore(training);

        final Map<String, String> args = ArchitectureParse.parseParams(argsStr);
        final ParamsHolder holder = new ParamsHolder(args);
        final double startTemp = holder.getDouble(
                MLTrainFactory.PROPERTY_TEMPERATURE_START, false, 10);
        final double stopTemp = holder.getDouble(
                MLTrainFactory.PROPERTY_TEMPERATURE_STOP, false, 2);

        final int cycles = holder.getInt(MLTrainFactory.CYCLES, false, 100);

        final Training train = new NeuralSimulatedAnnealing(
                (VectorEncodable) method, score, startTemp, stopTemp, cycles);

        return train;
    }
}
