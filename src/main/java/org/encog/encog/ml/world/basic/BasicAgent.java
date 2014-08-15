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
package org.encog.ml.world.basic;

import org.encog.ml.world.*;

import java.util.Set;

public class BasicAgent implements WorldAgent {

    private State currentState;
    private AgentPolicy policy;
    private World world;
    private boolean first = true;

    @Override
    public State getCurrentState() {
        return this.currentState;
    }

    @Override
    public void setCurrentState(final State s) {
        this.currentState = s;
    }

    @Override
    public AgentPolicy getPolicy() {
        return this.policy;
    }

    @Override
    public void setAgentPolicy(final AgentPolicy p) {
        this.policy = p;
    }

    /**
     * @return the world
     */
    @Override
    public World getWorld() {
        return world;
    }

    /**
     * @param world the world to set
     */
    @Override
    public void setWorld(final World world) {
        this.world = world;
    }

    @Override
    public void tick() {
        if (first) {
            first = false;
            this.currentState.increaseVisited();
        }

        final Action action = this.policy.determineNextAction(this);
        final Set<SuccessorState> states = world.getProbability()
                .determineSuccessorStates(currentState, action);
        final double d = Math.random();
        double sum = 0;
        for (final SuccessorState state : states) {
            sum += state.getProbability();
            if (d < sum) {
                this.currentState = state.getState();
                if (state.getState() == null) {
                    System.out.println("danger");
                }
                state.getState().increaseVisited();
                return;
            }
        }
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
