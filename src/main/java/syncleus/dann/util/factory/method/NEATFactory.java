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
package syncleus.dann.util.factory.method;

import syncleus.dann.learn.ml.MLMethod;
import syncleus.dann.util.factory.MLActivationFactory;
import syncleus.dann.util.factory.MLMethodFactory;
import syncleus.dann.util.factory.parse.ArchitectureParse;
import org.encog.neural.neat.NEATPopulation;

import java.util.Map;

/**
 * A factor to create feedforward networks.
 */
public class NEATFactory {

    /**
     * The activation function factory to use.
     */
    private final MLActivationFactory factory = new MLActivationFactory();

    /**
     * Create a NEAT population.
     *
     * @param architecture The architecture string to use.
     * @param input        The input count.
     * @param output       The output count.
     * @return The population.
     */
    public MLMethod create(final String architecture, final int input,
                           final int output) {

        if (input <= 0) {
            throw new RuntimeException("Must have at least one input for NEAT.");
        }

        if (output <= 0) {
            throw new RuntimeException("Must have at least one output for NEAT.");
        }

        final Map<String, String> args = ArchitectureParse
                .parseParams(architecture);
        final ParamsHolder holder = new ParamsHolder(args);

        final int populationSize = holder.getInt(
                MLMethodFactory.PROPERTY_POPULATION_SIZE, false, 1000);

        final int cycles = holder.getInt(MLMethodFactory.PROPERTY_CYCLES,
                false, NEATPopulation.DEFAULT_CYCLES);

        final ActivationFunction af = this.factory.create(holder.getString(
                MLMethodFactory.PROPERTY_AF, false,
                MLActivationFactory.AF_SSIGMOID));

        final NEATPopulation pop = new NEATPopulation(input, output,
                populationSize);
        pop.reset();
        pop.setActivationCycles(cycles);
        pop.setNEATActivationFunction(af);

        return pop;
    }

}