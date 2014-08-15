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
package syncleus.dann.evolve.opp.selection;

import java.io.Serializable;
import java.util.Random;

import syncleus.dann.evolve.species.Species;
import syncleus.dann.evolve.train.EvolutionaryAlgorithm;

/**
 * Truncation selection chooses a random genome from the top genomes in the
 * population. A percent determines how large this group of top genomes is.
 * <p/>
 * http://en.wikipedia.org/wiki/Truncation_selection
 */
public class TruncationSelection implements SelectionOperator, Serializable {

    /**
     * The serial id.
     */
    private static final long serialVersionUID = 1L;

    /**
     * The trainer.
     */
    private final EvolutionaryAlgorithm trainer;

    /**
     * The percent to select from.
     */
    private final double percent;

    /**
     * Construct the truncation selector.
     *
     * @param theTrainer The trainer.
     * @param thePercent The top percent to select from.
     */
    public TruncationSelection(final EvolutionaryAlgorithm theTrainer,
                               final double thePercent) {
        this.trainer = theTrainer;
        this.percent = thePercent;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int performSelection(final Random rnd, final Species species) {
        final int top = Math.max(
                (int) (species.getMembers().size() * this.percent), 1);
        final int result = rnd.nextInt(top);
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int performAntiSelection(final Random rnd, final Species species) {
        return species.getMembers().size() - performSelection(rnd, species);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public EvolutionaryAlgorithm getTrainer() {
        return this.trainer;
    }

}
