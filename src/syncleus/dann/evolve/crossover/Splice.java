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
package syncleus.dann.evolve.crossover;

import java.util.Random;
import syncleus.dann.evolve.genome.ArrayGenome;
import syncleus.dann.evolve.genome.Genome;
import syncleus.dann.evolve.opp.EvolutionaryOperator;
import syncleus.dann.evolve.train.EvolutionaryAlgorithm;

/**
 * A simple cross over where genes are simply "spliced". Genes are allowed to
 * repeat.
 */
public class Splice implements EvolutionaryOperator {

    /**
     * The cut length.
     */
    private final int cutLength;

    /**
     * The owner.
     */
    private EvolutionaryAlgorithm owner;

    /**
     * Create a slice crossover with the specified cut length.
     *
     * @param theCutLength The cut length.
     */
    public Splice(final int theCutLength) {
        this.cutLength = theCutLength;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void performOperation(final Random rnd, final Genome[] parents,
                                 final int parentIndex, final Genome[] offspring,
                                 final int offspringIndex) {

        final ArrayGenome mother = (ArrayGenome) parents[parentIndex];
        final ArrayGenome father = (ArrayGenome) parents[parentIndex + 1];
        final ArrayGenome offspring1 = (ArrayGenome) this.owner.getPopulation()
                .getGenomeFactory().factor();
        final ArrayGenome offspring2 = (ArrayGenome) this.owner.getPopulation()
                .getGenomeFactory().factor();

        offspring[offspringIndex] = offspring1;
        offspring[offspringIndex + 1] = offspring2;

        final int geneLength = mother.size();

        // the chromosome must be cut at two positions, determine them
        final int cutpoint1 = (rnd.nextInt(geneLength - this.cutLength));
        final int cutpoint2 = cutpoint1 + this.cutLength;

        // handle cut section
        for (int i = 0; i < geneLength; i++) {
            if (!((i < cutpoint1) || (i > cutpoint2))) {
                offspring1.copy(father, i, i);
                offspring2.copy(mother, i, i);
            }
        }

        // handle outer sections
        for (int i = 0; i < geneLength; i++) {
            if ((i < cutpoint1) || (i > cutpoint2)) {
                offspring1.copy(mother, i, i);
                offspring2.copy(father, i, i);
            }
        }
    }

    /**
     * @return The number of offspring produced, which is 2 for splice
     * crossover.
     */
    @Override
    public int offspringProduced() {
        return 2;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int parentsNeeded() {
        return 2;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void init(final EvolutionaryAlgorithm theOwner) {
        this.owner = theOwner;

    }
}
