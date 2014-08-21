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

import java.io.Serializable;
import syncleus.dann.neural.util.activation.EncogActivationFunction;

/**
 * Implements a NEAT neuron gene.
 * <p/>
 * NeuroEvolution of Augmenting Topologies (NEAT) is a genetic algorithm for the
 * generation of evolving artificial neural networks. It was developed by Ken
 * Stanley while at The University of Texas at Austin.
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
public class NEATNeuronGene extends NEATBaseGene implements Serializable {

    /**
     * Serial id.
     */
    private static final long serialVersionUID = 1L;

    /**
     * The neuron type.
     */
    private NEATNeuronType neuronType;

    /**
     * The activation function.
     */
    private EncogActivationFunction activationFunction;

    /**
     * The default constructor.
     */
    public NEATNeuronGene() {

    }

    /**
     * Construct a neuron gene.
     *
     * @param type                       The neuron type.
     * @param theEncogActivationFunction The activation function.
     * @param id                         The neuron id.
     * @param innovationID               The innovation id.
     */
    public NEATNeuronGene(final NEATNeuronType type,
                          final EncogActivationFunction theEncogActivationFunction, final long id,
                          final long innovationID) {
        this.neuronType = type;
        this.setInnovationId(innovationID);
        setId(id);
        this.activationFunction = theEncogActivationFunction;
    }

    /**
     * Construct this gene by comping another.
     *
     * @param other The other gene to copy.
     */
    public NEATNeuronGene(final NEATNeuronGene other) {
        copy(other);
    }

    /**
     * Copy another gene to this one.
     *
     * @param gene The other gene.
     */
    public void copy(final NEATNeuronGene gene) {
        final NEATNeuronGene other = gene;
        setId(other.getId());
        this.neuronType = other.neuronType;
        this.activationFunction = other.activationFunction;
        this.setInnovationId(other.getInnovationId());
    }

    /**
     * @return The type for this neuron.
     */
    public NEATNeuronType getNeuronType() {
        return this.neuronType;
    }

    /**
     * Set the neuron type.
     *
     * @param neuronType The neuron type.
     */
    public void setNeuronType(final NEATNeuronType neuronType) {
        this.neuronType = neuronType;
    }

    /**
     * @return the activationFunction
     */
    public EncogActivationFunction getEncogActivationFunction() {
        return activationFunction;
    }

    /**
     * @param activationFunction the activationFunction to set
     */
    public void setEncogActivationFunction(
            final EncogActivationFunction activationFunction) {
        this.activationFunction = activationFunction;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        final StringBuilder result = new StringBuilder();
        result.append("[NEATNeuronGene: id=");
        result.append(this.getId());
        result.append(", type=");
        result.append(this.getNeuronType());
        result.append(']');
        return result.toString();
    }
}
