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
package syncleus.dann.evolve.mutate;

import java.util.Random;
import syncleus.dann.evolve.genome.DoubleArrayGenome;
import syncleus.dann.evolve.genome.Genome;
import syncleus.dann.evolve.opp.EvolutionaryOperator;
import syncleus.dann.evolve.train.EvolutionaryAlgorithm;

/**
 * A simple mutation based on random numbers.
 */
public class MutatePerturb implements EvolutionaryOperator {

    /**
     * The amount to perturb by.
     */
    private final double perturbAmount;

    /**
     * Construct a perturb mutation.
     *
     * @param thePerturbAmount The amount to mutate by(percent).
     */
    public MutatePerturb(final double thePerturbAmount) {
        this.perturbAmount = thePerturbAmount;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void performOperation(final Random rnd, final Genome[] parents,
                                 final int parentIndex, final Genome[] offspring,
                                 final int offspringIndex) {
        final DoubleArrayGenome parent = (DoubleArrayGenome) parents[parentIndex];
        offspring[offspringIndex] = parent.getPopulation().getGenomeFactory()
                .factor();
        final DoubleArrayGenome child = (DoubleArrayGenome) offspring[offspringIndex];

        for (int i = 0; i < parent.size(); i++) {
            double value = parent.getData()[i];
            value += value
                    * (perturbAmount - (rnd.nextDouble() * perturbAmount * 2));
            child.getData()[i] = value;
        }
    }

    /**
     * @return The number of offspring produced, which is 1 for this mutation.
     */
    @Override
    public int offspringProduced() {
        return 1;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int parentsNeeded() {
        return 1;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void init(final EvolutionaryAlgorithm theOwner) {
        // not needed
    }
}
