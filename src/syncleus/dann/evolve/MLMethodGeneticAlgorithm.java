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
package syncleus.dann.evolve;

import syncleus.dann.Learning;
import syncleus.dann.data.VectorEncodable;
import syncleus.dann.evolve.crossover.Splice;
import syncleus.dann.evolve.genome.Genome;
import syncleus.dann.evolve.mutate.MutatePerturb;
import syncleus.dann.evolve.population.BasicPopulation;
import syncleus.dann.evolve.population.Population;
import syncleus.dann.evolve.sort.GenomeComparator;
import syncleus.dann.evolve.sort.MaximizeScoreComp;
import syncleus.dann.evolve.sort.MinimizeScoreComp;
import syncleus.dann.evolve.species.Species;
import syncleus.dann.evolve.train.basic.TrainEA;
import syncleus.dann.learn.MethodFactory;

/**
 * Implements a genetic algorithm that allows an MLMethod that is encodable
 * (MLEncodable) to be trained. It works well with both BasicNetwork and
 * FreeformNetwork class, as well as any MLEncodable class.
 * <p/>
 * There are essentially two ways you can make use of this class.
 * <p/>
 * Either way, you will need a score object. The score object tells the genetic
 * algorithm how well suited a neural network is.
 * <p/>
 * If you would like to use genetic algorithms with a training set you should
 * make use TrainingSetScore class. This score object uses a training set to
 * score your neural network.
 * <p/>
 * If you would like to be more abstract, and not use a training set, you can
 * create your own implementation of the CalculateScore method. This class can
 * then score the networks any way that you like.
 */
public class MLMethodGeneticAlgorithm extends BasicTraining /*implements MultiThreadable*/ {

    /**
     * Very simple class that implements a genetic algorithm.
     *
     * @author jheaton
     */
    public static class MLMethodGeneticAlgorithmHelper extends TrainEA {
        /**
         * The serial id.
         */
        private static final long serialVersionUID = 1L;

        /**
         * Construct the helper.
         *
         * @param thePopulation    The population.
         * @param theScoreFunction The score function.
         */
        public MLMethodGeneticAlgorithmHelper(final Population thePopulation,
                                              final CalculateScore theScoreFunction) {
            super(thePopulation, theScoreFunction);
        }
    }

    /**
     * Simple helper class that implements the required methods to implement a
     * genetic algorithm.
     */
    private MLMethodGeneticAlgorithmHelper genetic;

    /**
     * Construct a method genetic algorithm.
     *
     * @param phenotypeFactory The phenotype factory.
     * @param calculateScore   The score calculation object.
     * @param populationSize   The population size.
     */
    public MLMethodGeneticAlgorithm(final MethodFactory phenotypeFactory,
                                    final CalculateScore calculateScore, final int populationSize) {
        super(TrainingImplementationType.Iterative);

        // Create the population
        final Population population = new BasicPopulation(populationSize, null);
        final Species defaultSpecies = population.createSpecies();

        for (int i = 0; i < population.getPopulationSize(); i++) {
            final VectorEncodable chromosomeNetwork = (VectorEncodable) phenotypeFactory
                    .factor();
            final MLMethodGenome genome = new MLMethodGenome(chromosomeNetwork);
            defaultSpecies.add(genome);
        }
        defaultSpecies.setLeader(defaultSpecies.getMembers().get(0));

        population.setGenomeFactory(new MLMethodGenomeFactory(phenotypeFactory,
                population));

        // create the trainer
        this.genetic = new MLMethodGeneticAlgorithmHelper(population,
                calculateScore);
        this.genetic.setCODEC(new MLEncodableCODEC());

        GenomeComparator comp = null;
        if (calculateScore.shouldMinimize()) {
            comp = new MinimizeScoreComp();
        } else {
            comp = new MaximizeScoreComp();
        }
        this.genetic.setBestComparator(comp);
        this.genetic.setSelectionComparator(comp);

        // create the operators
        final int s = Math
                .max(defaultSpecies.getMembers().get(0).size() / 5, 1);
        getGenetic().setPopulation(population);

        this.genetic.addOperation(0.9, new Splice(s));
        this.genetic.addOperation(0.1, new MutatePerturb(1.0));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean canContinue() {
        return false;
    }

    /**
     * @return The genetic algorithm implementation.
     */
    public MLMethodGeneticAlgorithmHelper getGenetic() {
        return this.genetic;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Learning getMethod() {
        final Genome best = this.genetic.getBestGenome();
        return this.genetic.getCODEC().decode(best);
    }

	/*@Override
    public int getThreadCount() {
		return this.genetic.getThreadCount();
	}*/

    /**
     * Perform one training iteration.
     */
    @Override
    public void iteration() {

        //EncogLogging.log(//EncogLogging.LEVEL_INFO,
        //"Performing Genetic iteration.");
        preIteration();
        setError(getGenetic().getError());
        getGenetic().iteration();
        setError(getGenetic().getError());
        postIteration();
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
     * Set the genetic helper class.
     *
     * @param genetic The genetic helper class.
     */
    public void setGenetic(final MLMethodGeneticAlgorithmHelper genetic) {
        this.genetic = genetic;
    }

/*	@Override
    public void setThreadCount(final int numThreads) {
		this.genetic.setThreadCount(numThreads);

	}*/
}
