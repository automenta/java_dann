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
package syncleus.dann.learn.markov.alog;

import syncleus.dann.data.DataCase;
import syncleus.dann.data.DataSequence;
import syncleus.dann.data.Dataset;
import syncleus.dann.data.vector.VectorDataset;
import syncleus.dann.data.vector.VectorSequence;
import syncleus.dann.learn.markov.HiddenMarkovModel;

/**
 * This class is used to generate random sequences based on a Hidden Markov
 * Model. These sequences represent the random probabilities that the HMM
 * models.
 */
public class MarkovGenerator {
    private final HiddenMarkovModel hmm;
    private int currentState;

    public MarkovGenerator(final HiddenMarkovModel hmm) {
        this.hmm = hmm;
        newSequence();
    }

    public DataSequence generateSequences(final int observationCount,
                                           final int observationLength) {
        final VectorSequence result = new VectorSequence();

        for (int i = 0; i < observationCount; i++) {
            result.startNewSequence();
            result.add(observationSequence(observationLength));
        }

        return result;
    }

    public int getCurrentState() {
        return this.currentState;
    }

    public void newSequence() {
        final double rand = Math.random();
        double current = 0.0;

        for (int i = 0; i < (this.hmm.getStateCount() - 1); i++) {
            current += this.hmm.getPi(i);

            if (current > rand) {
                this.currentState = i;
                return;
            }
        }

        this.currentState = this.hmm.getStateCount() - 1;
    }

    public DataCase observation() {
        final DataCase o = this.hmm.getStateDistribution(this.currentState)
                .generate();
        double rand = Math.random();

        for (int j = 0; j < (this.hmm.getStateCount() - 1); j++) {
            if ((rand -= this.hmm
                    .getTransitionProbability(this.currentState, j)) < 0) {
                this.currentState = j;
                return o;
            }
        }

        this.currentState = this.hmm.getStateCount() - 1;
        return o;
    }

    public Dataset observationSequence(int length) {
        final Dataset sequence = new VectorDataset();
        while (length-- > 0) {
            sequence.add(observation());
        }
        newSequence();

        return sequence;
    }
}
