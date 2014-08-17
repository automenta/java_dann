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
package syncleus.dann.plan.qlearning;

import syncleus.dann.plan.DiscreteActionProblem;
import syncleus.dann.plan.DiscreteActionSolution;
import syncleus.dann.plan.State;

public class QLearningEncog<A> implements DiscreteActionSolution<A> {

    private DiscreteActionProblem<A> world;
    /**
     * The learning rate (alpha).
     */
    private final double learningRate;

    /**
     * The discount rate (gamma).
     */
    private final double discountRate;
    private State state;
    private State prevState;
    private A action, prevAction;

    public QLearningEncog(final DiscreteActionProblem<A> theWorld, final double theLearningRate,
                     final double theDiscountRate) {
        this.world = theWorld;
        this.learningRate = theLearningRate;
        this.discountRate = theDiscountRate;
    }

    protected void learn(final State s1, final A a1, final State s2, final A a2) {
        final double q1 = s1.getPolicyValue()[world.getActions().indexOf(a1)];
        final double q2 = s2.getPolicyValue()[world.getActions().indexOf(a2)];
        final double r = s1.getReward();
        final double d = q1 + this.learningRate * (r + this.discountRate * q2 - q1);
        
        s1.getPolicyValue()[world.getActions().indexOf(a1)] = d;
    }

    @Override
    public A nextAction() {
        //TODO
        prevAction = action;
        
        learn(prevState, prevAction, state, action);
        return null;
    }

    
    @Override
    public State getCurrentState() {
        return state;
    }

    @Override
    public void setCurrentState(State s) {
        this.prevState = state;
        this.state = s;
    }


    @Override
    public DiscreteActionProblem getProblem() {
        return world;
    }

    @Override
    public boolean setProblem(DiscreteActionProblem<A> p) {
        this.world = p;
        return true;
    }

}
