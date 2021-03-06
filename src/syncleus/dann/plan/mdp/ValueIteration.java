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
package syncleus.dann.plan.mdp;

import syncleus.dann.plan.ActionProbability;
import syncleus.dann.plan.DiscreteActionProblem;
import syncleus.dann.plan.State;

public class ValueIteration<A,S extends State> extends MarkovDecisionProcess<A,S> {

    private final double discountFactor;

    public ValueIteration(final DiscreteActionProblem<A,S> theWorld, final ActionProbability<A> probability, final double theDiscountFactor) {
        super(theWorld, probability);        
        this.discountFactor = theDiscountFactor;
    }

    public void calculateValue(final S state) {
        double result = Double.NEGATIVE_INFINITY;
        if (!getProblem().isGoalState(state)) {
            for (final A action : getProblem().getActions()) {
                double sum = 0;
                sum = probability.determineSuccessorStates(state, action).stream().map((statePrime) -> statePrime.getProbability() * statePrime.getState().getPolicyValue()[0]).reduce(sum, (accumulator, _item) -> accumulator + _item);
                sum *= this.discountFactor;

                result = Math.max(result, sum);
            }

            state.getPolicyValue()[0] = result + state.getReward();
        } else {
            state.getPolicyValue()[0] = state.getReward();
        }
    }

    public void iteration() {
        getProblem().getStates().stream().forEach(this::calculateValue);
    }

}
