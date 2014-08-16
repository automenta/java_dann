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
package org.encog.neural.neat;

import syncleus.dann.evolve.GeneticError;
import syncleus.dann.evolve.codec.GeneticCODEC;
import syncleus.dann.evolve.genome.Genome;
import syncleus.dann.learn.Learning;
import syncleus.dann.neural.activation.EncogActivationFunction;

import java.io.Serializable;
import java.util.*;

/**
 * This CODEC is used to create phenomes (NEATNetwork) objects using a genome
 * (NEATGenome). Conversion is only one direction. You are allowed to transform
 * a NEAT genome into a NEATNetwork, but you cannot transform a NEAT phenome
 * back into a genome. The main reason is I have not found a great deal of need
 * to go the other direction. If someone ever does find a need and creates a
 * encode method, please consider contributing it to the Encog project. :)
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
public class NEATCODEC implements GeneticCODEC, Serializable {

    /**
     * The serial ID.
     */
    private static final long serialVersionUID = 1L;

    /**
     * {@inheritDoc}
     */
    @Override
    public Learning decode(final Genome genome) {
        final NEATGenome neatGenome = (NEATGenome) genome;
        final NEATPopulation pop = (NEATPopulation) neatGenome.getPopulation();
        final List<NEATNeuronGene> neuronsChromosome = neatGenome
                .getNeuronsChromosome();
        final List<NEATLinkGene> linksChromosome = neatGenome
                .getLinksChromosome();

        if (neuronsChromosome.get(0).getNeuronType() != NEATNeuronType.Bias) {
            throw new RuntimeException(
                    "The first neuron must be the bias neuron, this genome is invalid.");
        }

        final List<NEATLink> links = new ArrayList<>();
        final EncogActivationFunction[] afs = new EncogActivationFunction[neuronsChromosome
                .size()];

        for (int i = 0; i < afs.length; i++) {
            afs[i] = neuronsChromosome.get(i).getEncogActivationFunction();
        }

        final Map<Long, Integer> lookup = new HashMap<>();
        for (int i = 0; i < neuronsChromosome.size(); i++) {
            final NEATNeuronGene neuronGene = neuronsChromosome.get(i);
            lookup.put(neuronGene.getId(), i);
        }

        // loop over connections
        for (int i = 0; i < linksChromosome.size(); i++) {
            final NEATLinkGene linkGene = linksChromosome.get(i);
            if (linkGene.isEnabled()) {
                links.add(new NEATLink(lookup.get(linkGene.getFromNeuronID()),
                        lookup.get(linkGene.getToNeuronID()), linkGene
                        .getWeight()));
            }

        }

        Collections.sort(links);

        final NEATNetwork network = new NEATNetwork(neatGenome.getInputCount(),
                neatGenome.getOutputCount(), links, afs);

        network.setActivationCycles(pop.getActivationCycles());
        return network;
    }

    /**
     * This method is not currently implemented. If you have need of it, and do
     * implement a conversion from a NEAT phenotype to a genome, consider
     * contribution to the Encog project.
     *
     * @param phenotype Not used.
     * @return Not used.
     */
    @Override
    public Genome encode(final Learning phenotype) {
        throw new GeneticError("Encoding of a NEAT network is not supported.");
    }

}
