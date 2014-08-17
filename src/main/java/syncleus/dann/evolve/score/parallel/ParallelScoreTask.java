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
package syncleus.dann.evolve.score.parallel;

import syncleus.dann.evolve.exception.EARuntimeError;
import syncleus.dann.evolve.genome.Genome;
import syncleus.dann.evolve.score.AdjustScore;
import syncleus.dann.evolve.train.basic.BasicEA;
import syncleus.dann.learn.CalculateScore;
import syncleus.dann.Learning;

import java.util.List;

/**
 * An individual threadable task for the parallel score calculation.
 */
public class ParallelScoreTask implements Runnable {

    /**
     * The genome to calculate the score for.
     */
    private final Genome genome;

    /**
     * The score function.
     */
    private final CalculateScore scoreFunction;

    /**
     * The score adjusters.
     */
    private final List<AdjustScore> adjusters;

    /**
     * The owners.
     */
    private final ParallelScore owner;

    /**
     * Construct the parallel task.
     *
     * @param genome   The genome.
     * @param theOwner The owner.
     */
    public ParallelScoreTask(final Genome genome, final ParallelScore theOwner) {
        super();
        this.owner = theOwner;
        this.genome = genome;
        this.scoreFunction = theOwner.getScoreFunction();
        this.adjusters = theOwner.getAdjusters();
    }

    /**
     * Perform the task.
     */
    @Override
    public void run() {
        final Learning phenotype = this.owner.getCodec().decode(this.genome);
        if (phenotype != null) {
            double score;
            try {
                score = this.scoreFunction.calculateScore(phenotype);
            } catch (final EARuntimeError e) {
                score = Double.NaN;
            }
            genome.setScore(score);
            genome.setAdjustedScore(score);
            BasicEA.calculateScoreAdjustment(genome, adjusters);
        } else {

        }
    }

}
