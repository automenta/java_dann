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
package syncleus.dann;

import syncleus.dann.data.DataSet;
import syncleus.dann.learn.Learning;


/**
 * A state sequence ML method, for example a Hidden Markov Model.
 */
public interface StateSequenceLearning extends Learning {

    /**
     * Get the sates for the given sequence.
     *
     * @param oseq The sequence.
     * @return The states.
     */
    int[] getStatesForSequence(DataSet oseq);

    /**
     * Determine the probability of the specified sequence.
     *
     * @param oseq The sequence.
     * @return The probability.
     */
    double probability(DataSet oseq);

    /**
     * Determine the probability for the specified sequence and states.
     *
     * @param seq    The sequence.
     * @param states The states.
     * @return The probability.
     */
    double probability(DataSet seq, int[] states);
}