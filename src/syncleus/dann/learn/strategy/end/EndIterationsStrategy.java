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
package syncleus.dann.learn.strategy.end;

import syncleus.dann.Training;

public class EndIterationsStrategy implements EndTrainingStrategy {

    private final int maxIterations;
    private int currentIteration;
    private Training train;

    public EndIterationsStrategy(final int maxIterations) {
        this.maxIterations = maxIterations;
        this.currentIteration = 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean shouldStop() {
        return (this.currentIteration >= this.maxIterations);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void init(final Training train) {
        this.train = train;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void postIteration() {
        this.currentIteration = this.train.getIteration();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void preIteration() {
    }
}
