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
package syncleus.dann.util.factory.train;

import syncleus.dann.Learning;
import syncleus.dann.Training;
import syncleus.dann.data.Dataset;
import syncleus.dann.evolve.gp.opp.ConstMutation;
import syncleus.dann.evolve.gp.opp.SubtreeCrossover;
import syncleus.dann.evolve.gp.opp.SubtreeMutation;
import syncleus.dann.evolve.score.adjust.ComplexityAdjustedScore;
import syncleus.dann.evolve.train.basic.TrainEA;

public class EPLGAFactory {
    /**
     * Create an EPL GA trainer.
     *
     * @param method   The method to use.
     * @param training The training data to use.
     * @param argsStr  The arguments to use.
     * @return The newly created trainer.
     */
    public static Training create(final Learning method, final Dataset training,
                                 final String argsStr) {

        final PrgPopulation pop = (PrgPopulation) method;

        final LearningScoring score = new TrainingSetScore(training);
        final TrainEA train = new TrainEA(pop, score);
        train.getRules().addRewriteRule(new RewriteConstants());
        train.getRules().addRewriteRule(new RewriteAlgebraic());
        train.setCODEC(new PrgCODEC());
        train.addOperation(0.8, new SubtreeCrossover());
        train.addOperation(0.1, new SubtreeMutation(pop.getContext(), 4));
        train.addOperation(0.1, new ConstMutation(pop.getContext(), 0.5, 1.0));
        train.addScoreAdjuster(new ComplexityAdjustedScore());
        train.setSpeciation(new PrgSpeciation());
        return train;
    }
}
