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
package syncleus.dann.neural.neat.training;

import syncleus.dann.evolve.genome.BasicGenome;
import syncleus.dann.evolve.genome.Genome;
import syncleus.dann.math.random.RangeRandomizer;
import syncleus.dann.neural.activation.EncogActivationFunction;

import java.io.Serializable;
import java.util.*;

/**
 * Implements a NEAT genome. This is a "blueprint" for creating a neural
 * network.
 * <p/>
 * -----------------------------------------------------------------------------
 * http://www.cs.ucf.edu/~kstanley/ Encog's NEAT implementation was drawn from
 * the following three Journal Articles. For more complete BibTeX sources, see
 * NEATNetwork.java.
 * <p/>
 * Evolving Neural Networks Through Augmenting Topologies
 * <p/>
 * Generating Large-Scale Neural Networks Through Discovering Geometric
 * Regularities
 * <p/>
 * Automatic feature selection in neuroevolution
 */
public class NEATGenome extends BasicGenome implements Cloneable, Serializable {

    /**
     * Serial id.
     */
    private static final long serialVersionUID = 1L;

    /**
     * The number of inputs.
     */
    private int inputCount;

    /**
     * The list that holds the links.
     */
    private final List<NEATLinkGene> linksList = new ArrayList<>();

    /**
     * THe network depth.
     */
    private int networkDepth;

    /**
     * The list that holds the neurons.
     */
    private final List<NEATNeuronGene> neuronsList = new ArrayList<>();

    /**
     * The number of outputs.
     */
    private int outputCount;

    /**
     * Construct a genome by copying another.
     *
     * @param other The other genome.
     */
    public NEATGenome(final NEATGenome other) {
        this.networkDepth = other.networkDepth;
        this.setPopulation(other.getPopulation());
        setScore(other.getScore());
        setAdjustedScore(other.getAdjustedScore());
        this.inputCount = other.inputCount;
        this.outputCount = other.outputCount;
        this.setSpecies(other.getSpecies());

        other.getNeuronsChromosome().stream().map(NEATNeuronGene::new).forEach(this.neuronsList::add);
        other.getLinksChromosome().stream().map((oldGene) -> new NEATLinkGene(
                oldGene.getFromNeuronID(), oldGene.getToNeuronID(),
                oldGene.isEnabled(), oldGene.getInnovationId(),
                oldGene.getWeight())).forEach(this.linksList::add);

    }

    /**
     * Create a NEAT gnome. Neuron genes will be added by reference, links will
     * be copied.
     *
     * @param neurons     The neurons to create.
     * @param links       The links to create.
     * @param inputCount  The input count.
     * @param outputCount The output count.
     */
    public NEATGenome(final List<NEATNeuronGene> neurons,
                      final List<NEATLinkGene> links, final int inputCount,
                      final int outputCount) {
        setAdjustedScore(0);
        this.inputCount = inputCount;
        this.outputCount = outputCount;

        links.stream().forEach((gene) -> this.linksList.add(new NEATLinkGene(gene)));

        this.neuronsList.addAll(neurons);
    }

    /**
     * Create a new genome with the specified connection density. This
     * constructor is typically used to create the initial population.
     *
     * @param rnd               Random number generator.
     * @param pop               The population.
     * @param inputCount        The input count.
     * @param outputCount       The output count.
     * @param connectionDensity The connection density.
     */
    public NEATGenome(final Random rnd, final NEATPopulation pop,
                      final int inputCount, final int outputCount,
                      final double connectionDensity) {
        setAdjustedScore(0);
        this.inputCount = inputCount;
        this.outputCount = outputCount;

        // get the activation function
        final EncogActivationFunction af = pop.getEncogActivationFunctions().pickFirst();

        // first bias
        int innovationID = 0;
        final NEATNeuronGene biasGene = new NEATNeuronGene(NEATNeuronType.Bias,
                af, inputCount, innovationID++);
        this.neuronsList.add(biasGene);

        // then inputs

        for (int i = 0; i < inputCount; i++) {
            final NEATNeuronGene gene = new NEATNeuronGene(
                    NEATNeuronType.Input, af, i, innovationID++);
            this.neuronsList.add(gene);
        }

        // then outputs

        for (int i = 0; i < outputCount; i++) {
            final NEATNeuronGene gene = new NEATNeuronGene(
                    NEATNeuronType.Output, af, i + inputCount + 1,
                    innovationID++);
            this.neuronsList.add(gene);
        }

        // and now links
        for (int i = 0; i < inputCount + 1; i++) {
            for (int j = 0; j < outputCount; j++) {
                // make sure we have at least one connection
                if (this.linksList.size() < 1
                        || rnd.nextDouble() < connectionDensity) {
                    final long fromID = this.neuronsList.get(i).getId();
                    final long toID = this.neuronsList.get(inputCount + j + 1)
                            .getId();
                    final double w = RangeRandomizer.randomize(rnd,
                            -pop.getWeightRange(), pop.getWeightRange());
                    final NEATLinkGene gene = new NEATLinkGene(fromID, toID,
                            true, innovationID++, w);
                    this.linksList.add(gene);
                }
            }
        }

    }

    /**
     * Empty constructor for persistence.
     */
    public NEATGenome() {

    }

    /**
     * @return The number of input neurons.
     */
    public int getInputCount() {
        return this.inputCount;
    }

    /**
     * @return The network depth.
     */
    public int getNetworkDepth() {
        return this.networkDepth;
    }

    /**
     * @return The number of genes in the links chromosome.
     */
    public int getNumGenes() {
        return this.linksList.size();
    }

    /**
     * @return The output count.
     */
    public int getOutputCount() {
        return this.outputCount;
    }

    /**
     * @param networkDepth the networkDepth to set
     */
    public void setNetworkDepth(final int networkDepth) {
        this.networkDepth = networkDepth;
    }

    /**
     * Sort the genes.
     */
    public void sortGenes() {
        Collections.sort(this.linksList);
    }

    /**
     * @return the linksChromosome
     */
    public List<NEATLinkGene> getLinksChromosome() {
        return this.linksList;
    }

    /**
     * @return the neuronsChromosome
     */
    public List<NEATNeuronGene> getNeuronsChromosome() {
        return this.neuronsList;
    }

    /**
     * @param inputCount the inputCount to set
     */
    public void setInputCount(final int inputCount) {
        this.inputCount = inputCount;
    }

    /**
     * @param outputCount the outputCount to set
     */
    public void setOutputCount(final int outputCount) {
        this.outputCount = outputCount;
    }

    /**
     * Validate the structure of this genome.
     */
    public void validate() {

        // make sure that the bias neuron is where it should be
        final NEATNeuronGene g = this.neuronsList.get(0);
        if (g.getNeuronType() != NEATNeuronType.Bias) {
            throw new RuntimeException("NEAT Neuron Gene 0 should be a bias gene.");
        }

        // make sure all input neurons are at the beginning
        for (int i = 1; i <= this.inputCount; i++) {
            final NEATNeuronGene gene = this.neuronsList.get(i);
            if (gene.getNeuronType() != NEATNeuronType.Input) {
                throw new RuntimeException("NEAT Neuron Gene " + i
                        + " should be an input gene.");
            }
        }

        // make sure that there are no double links
        final Map<String, NEATLinkGene> map = new HashMap<>();
        for (final NEATLinkGene nlg : this.linksList) {
            final String key = nlg.getFromNeuronID() + "->"
                    + nlg.getToNeuronID();
            if (map.containsKey(key)) {
                throw new RuntimeException("Double link found: " + key);
            }
            map.put(key, nlg);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void copy(final Genome source) {
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int size() {
        return this.linksList.size();
    }

    /**
     * Find the neuron with the specified nodeID.
     *
     * @param nodeID The nodeID to look for.
     * @return The neuron, if found, otherwise null.
     */
    public NEATNeuronGene findNeuron(final long nodeID) {
        for (final NEATNeuronGene gene : this.neuronsList) {
            if (gene.getId() == nodeID)
                return gene;
        }
        return null;
    }

    @Override
    public String toString() {
        final StringBuilder result = new StringBuilder();
        result.append('[');
        result.append(this.getClass().getSimpleName());
        result.append(",score=");
        result.append(Format.formatDouble(this.getScore(), 2));
        result.append(",adjusted score=");
        result.append(Format.formatDouble(this.getAdjustedScore(), 2));
        result.append(",birth generation=");
        result.append(this.getBirthGeneration());
        result.append(",neurons=");
        result.append(this.neuronsList.size());
        result.append(",links=");
        result.append(this.linksList.size());
        result.append(']');
        return result.toString();
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone(); //To change body of generated methods, choose Tools | Templates.
    }
}
