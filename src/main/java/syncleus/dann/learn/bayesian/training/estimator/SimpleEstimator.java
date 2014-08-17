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
package syncleus.dann.learn.bayesian.training.estimator;

import java.util.Iterator;
import syncleus.dann.learn.bayesian.BayesianEvent;
import syncleus.dann.learn.bayesian.EncogBayesianNetwork;
import syncleus.dann.learn.bayesian.training.TrainBayesian;
import syncleus.dann.data.DataCase;
import syncleus.dann.data.Dataset;

/**
 * A simple probability estimator.
 */
public class SimpleEstimator implements BayesEstimator {

    private Dataset data;
    private EncogBayesianNetwork network;
    private int index;

    /**
     * {@inheritDoc}
     */
    @Override
    public void init(final TrainBayesian theTrainer,
                     final EncogBayesianNetwork theNetwork, final Dataset theData) {
        this.network = theNetwork;
        this.data = theData;
        this.index = 0;
    }

    /**
     * Calculate the probability.
     *
     * @param event  The event.
     * @param result The result.
     * @param args   The arguments.
     * @return The probability.
     */
    public double calculateProbability(final BayesianEvent event,
                                       final int result, final int[] args) {
        final int eventIndex = this.network.getEvents().indexOf(event);
        int x = 0;
        int y = 0;

        // calculate overall probability
        Iterator<DataCase> di = data.iterator();
        while (di.hasNext()) {
            DataCase pair = di.next();
            
            final int[] d = this.network.determineClasses(pair.getInput());

            if (args.length == 0) {
                x++;
                if (d[eventIndex] == result) {
                    y++;
                }
            } else if (d[eventIndex] == result) {
                x++;

                int i = 0;
                boolean givenMatch = true;
                for (final BayesianEvent givenEvent : event.getParents()) {
                    final int givenIndex = this.network
                            .getEventIndex(givenEvent);
                    if (args[i] != d[givenIndex]) {
                        givenMatch = false;
                        break;
                    }
                    i++;
                }

                if (givenMatch) {
                    y++;
                }
            }
        }

        final double num = y + 1;
        final double den = x + event.getChoices().size();

        return num / den;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean iteration() {
        final BayesianEvent event = this.network.getEvents().get(this.index);
        event.getTable().getLines().stream().forEach((line) -> line.setProbability(calculateProbability(event, line.getResult(),
                line.getArguments())));
        index++;

        return index < this.network.getEvents().size();
    }
}
