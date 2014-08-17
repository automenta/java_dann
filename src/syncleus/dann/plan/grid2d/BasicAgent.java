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
package syncleus.dann.plan.grid2d;

import syncleus.dann.math.geometry.Angle;
import syncleus.dann.plan.*;

import java.util.Set;

public class BasicAgent implements DiscreteActionSolution<Angle,BasicState> {

    private BasicState currentState;
    private AgentPolicy<Angle,BasicState> policy;
    private BasicDiscreteActionProblem world;
    private boolean first = true;

    @Override
    public BasicState getCurrentState() {
        return this.currentState;
    }

    @Override
    public void setCurrentState(final BasicState s) {
        this.currentState = s;
    }



    /**
     * @return the world
     */
    @Override
    public DiscreteActionProblem getProblem() {
        return world;
    }

    /**
     * @param world the world to set
     */
    @Override
    public boolean setProblem(final DiscreteActionProblem<Angle,BasicState> world) {
        this.world = (BasicDiscreteActionProblem)world;
        return true;
    }

    @Override
    public Angle nextAction() {
        if (first) {
            first = false;
            this.currentState.increaseVisited();
        }

        final Angle action = this.policy.determineNextAction(this);
        final Set<SuccessorState<BasicState>> states = world.getProbability()
                .determineSuccessorStates(currentState, action);
        final double d = Math.random();
        double sum = 0;
        for (final SuccessorState<BasicState> state : states) {
            sum += state.getProbability();
            if (d < sum) {
                this.currentState = state.getState();
                if (state.getState() == null) {
                    System.out.println("danger");
                }
                state.getState().increaseVisited();
                
            }
        }
        return action;
    }

    @Override
    public String toString() {
        final StringBuilder result = new StringBuilder();
        result.append("[BasicAgent: state=");
        result.append(this.currentState.toString());
        result.append(']');
        return result.toString();
    }

}
