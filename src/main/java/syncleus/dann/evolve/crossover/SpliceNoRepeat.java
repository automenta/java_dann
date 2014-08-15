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

import syncleus.dann.evolve.genome.Genome;
import syncleus.dann.evolve.opp.EvolutionaryOperator;
import syncleus.dann.evolve.train.EvolutionaryAlgorithm;
import syncleus.dann.evolve.GeneticError;
import syncleus.dann.evolve.genome.IntegerArrayGenome;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

/**
 * A simple cross over where genes are simply "spliced". Genes are not allowed
 * to repeat. This method only works with IntegerArrayGenome.
 */
public class SpliceNoRepeat implements EvolutionaryOperator {

    /**
     * The owner.
     */
    private EvolutionaryAlgorithm owner;

    /**
     * Get a list of the genes that have not been taken before. This is useful
     * if you do not wish the same gene to appear more than once in a genome.
     *
     * @param source The pool of genes to select from.
     * @param taken  An array of the taken genes.
     * @return Those genes in source that are not taken.
     */
    private static int getNotTaken(final IntegerArrayGenome source,
                                   final Set<Integer> taken) {

        for (final int trial : source.getData()) {
            if (!taken.contains(trial)) {
                taken.add(trial);
                return trial;
            }
        }

        throw new GeneticError("Ran out of integers to select.");
    }

    /**
     * The cut length.
     */
    private final int cutLength;

    /**
     * Construct a splice crossover.
     *
     * @param theCutLength The cut length.
     */
    public SpliceNoRepeat(final int theCutLength) {
        this.cutLength = theCutLength;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void performOperation(final Random rnd, final Genome[] parents,
                                 final int parentIndex, final Genome[] offspring,
                                 final int offspringIndex) {

        final IntegerArrayGenome mother = (IntegerArrayGenome) parents[parentIndex];
        final IntegerArrayGenome father = (IntegerArrayGenome) parents[parentIndex + 1];
        final IntegerArrayGenome offspring1 = (IntegerArrayGenome) this.owner
                .getPopulation().getGenomeFactory().factor();
        final IntegerArrayGenome offspring2 = (IntegerArrayGenome) this.owner
                .getPopulation().getGenomeFactory().factor();

        offspring[offspringIndex] = offspring1;
        offspring[offspringIndex + 1] = offspring2;

        final int geneLength = mother.size();

        // the chromosome must be cut at two positions, determine them
        final int cutpoint1 = (rnd.nextInt(geneLength - this.cutLength));
        final int cutpoint2 = cutpoint1 + this.cutLength;

        // keep track of which genes have been taken in each of the two
        // offspring, defaults to false.
        final Set<Integer> taken1 = new HashSet<>();
        final Set<Integer> taken2 = new HashSet<>();

        // handle cut section
        for (int i = 0; i < geneLength; i++) {
            if (!((i < cutpoint1) || (i > cutpoint2))) {
                offspring1.copy(father, i, i);
                offspring2.copy(mother, i, i);
                taken1.add(father.getData()[i]);
                taken2.add(mother.getData()[i]);
            }
        }

        // handle outer sections
        for (int i = 0; i < geneLength; i++) {
            if ((i < cutpoint1) || (i > cutpoint2)) {

                offspring1.getData()[i] = SpliceNoRepeat.getNotTaken(mother,
                        taken1);
                offspring2.getData()[i] = SpliceNoRepeat.getNotTaken(father,
                        taken2);

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
