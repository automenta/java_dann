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
package syncleus.dann.learn.fitness;

import syncleus.dann.Learning;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import syncleus.dann.learn.LearningScoring;

/**
 * A multi-objective fitness function.
 */
public class MultiObjectiveFitness implements LearningScoring, Serializable {

    /**
     * The serial id.
     */
    private static final long serialVersionUID = 1L;

    /**
     * The objectives.
     */
    private final List<FitnessObjective> objectives = new ArrayList<>();

    /**
     * Is the goal to minimize the score?
     */
    private boolean min;

    /**
     * Add an objective.
     *
     * @param weight          The weight of this objective, 1.0 for full, 0.5 for half, etc.
     * @param fitnessFunction The fitness function.
     */
    public void addObjective(final double weight,
                             final LearningScoring fitnessFunction) {
        if (this.objectives.isEmpty()) {
            this.min = fitnessFunction.shouldMinimize();
        } else {
            if (fitnessFunction.shouldMinimize() != this.min) {
                throw new RuntimeException(
                        "Multi-objective mismatch, some objectives are min and some are max.");
            }
        }
        this.objectives.add(new FitnessObjective(weight, fitnessFunction));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Double apply(final Learning method) {
        double result = 0;

        result = this.objectives.stream().map((obj) -> obj.getScore().apply(method) * obj.getWeight()).reduce(result, (accumulator, _item) -> accumulator + _item);

        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean shouldMinimize() {
        return this.min;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean requireSingleThreaded() {
        return this.objectives.stream().anyMatch((obj) -> (obj.getScore().requireSingleThreaded()));
    }

}
