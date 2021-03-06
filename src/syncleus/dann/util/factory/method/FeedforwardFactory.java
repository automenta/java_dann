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

import java.util.List;
import syncleus.dann.Learning;
import syncleus.dann.neural.VectorNeuralNetwork;
import syncleus.dann.neural.util.activation.ActivationLinear;
import syncleus.dann.neural.util.layer.BasicLayer;
import syncleus.dann.util.factory.MLActivationFactory;
import syncleus.dann.util.factory.parse.ArchitectureLayer;
import syncleus.dann.util.factory.parse.ArchitectureParse;

/**
 * A factor to create feedforward networks.
 */
public class FeedforwardFactory {

    /**
     * Error.
     */
    public static final String CANT_DEFINE_ACT = "Can't define activation function before first layer.";

    /**
     * The activation function factory to use.
     */
    private final MLActivationFactory factory = new MLActivationFactory();

    /**
     * Create a feed forward network.
     *
     * @param architecture The architecture string to use.
     * @param input        The input count.
     * @param output       The output count.
     * @return The feedforward network.
     */
    public Learning create(final String architecture, final int input,
                           final int output) {

        if (input <= 0) {
            throw new RuntimeException(
                    "Must have at least one input for feedforward.");
        }

        if (output <= 0) {
            throw new RuntimeException(
                    "Must have at least one output for feedforward.");
        }

        final VectorNeuralNetwork result = new VectorNeuralNetwork();
        final List<String> layers = ArchitectureParse.parseLayers(architecture);
        ActivationFunction af = new ActivationLinear();

        int questionPhase = 0;
        for (final String layerStr : layers) {
            int defaultCount;
            // determine default
            if (questionPhase == 0) {
                defaultCount = input;
            } else {
                defaultCount = output;
            }

            final ArchitectureLayer layer = ArchitectureParse.parseLayer(
                    layerStr, defaultCount);
            final boolean bias = layer.isBias();

            String part = layer.getName();
            if (part != null) {
                part = part.trim();
            } else {
                part = "";
            }

            final ActivationFunction lookup = this.factory.create(part);

            if (lookup != null) {
                af = lookup;
            } else {
                if (layer.isUsedDefault()) {
                    questionPhase++;
                    if (questionPhase > 2) {
                        throw new RuntimeException("Only two ?'s may be used.");
                    }
                }

                if (layer.getCount() == 0) {
                    throw new RuntimeException(
                            "Layer can't have zero neurons, Unknown architecture element: "
                                    + architecture + ", can't parse: " + part);
                }

                result.addLayer(new BasicLayer(af, bias, layer.getCount()));

            }
        }

        result.getStructure().finalizeStructure();
        result.reset();

        return result;
    }

}
