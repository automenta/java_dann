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

import syncleus.dann.plan.ActionProbability;
import java.util.ArrayDeque;
import syncleus.dann.plan.*;
import syncleus.dann.math.geometry.GridState;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Queue;
import java.util.Set;

public abstract class BasicWorld<A extends BasicAction> implements DiscreteActionProblem<A> {
    private final List<A> actions = new ArrayList<>();
    private final Queue<State> states = new ArrayDeque<>();
    private ActionProbability probability;
    private final List<DiscreteActionSolution> agents = new ArrayList<>();
    private final Set<State> goals = new HashSet<>();

    @Override
    public List<A> getActions() {
        return this.actions;
    }

    @Override
    public void addAction(final A action) {
        this.actions.add(action);
    }

    private int getActionIndex(final A a) {
        return actions.indexOf(a);
    }

    private int requireActionIndex(final A a) {
        final int result = getActionIndex(a);
        if (result == -1) {
            throw new RuntimeException("No such action: " + a);
        }
        return result;
    }

    public void setPolicyValue(final State state, final A action,
                               final double r) {
        final int index = requireActionIndex(action);
        state.getPolicyValue()[index] = r;
    }

    public double getPolicyValue(final State state, final A action) {
        final int index = requireActionIndex(action);
        return state.getPolicyValue()[index];
    }

    /**
     * @return the probability
     */
    public ActionProbability getProbability() {
        return probability;
    }

    /**
     * @param probability the probability to set
     */
    public void setProbability(final ActionProbability probability) {
        this.probability = probability;
    }

    public static void removeRewardBelow(final List<GridState> states,
                                         final double d) {
        int i = 0;
        while (i < states.size()) {
            if (states.get(i).getReward() < d) {
                states.remove(i);
            } else {
                i++;
            }
        }
    }

    public List<DiscreteActionSolution> getAgents() {
        return this.agents;
    }

    public void addAgent(final DiscreteActionSolution agent) {
        this.agents.add(agent);
        agent.setProblem(this);
    }

    public void removeAgent(final DiscreteActionSolution agent) {
        this.agents.remove(agent);
        agent.setProblem(null);
    }

    @Override
    public void addGoal(final State s) {
        this.goals.add(s);
    }

    @Override
    public void removeGoal(final State s) {
        this.goals.remove(s);

    }

    @Override
    public Set<State> getGoals() {
        return this.goals;
    }

    @Override
    public void addState(final State state) {
        this.states.add(state);
    }

    @Override
    public Queue<State> getStates() {
        return this.states;
    }

    @Override
    public boolean isGoalState(final State s) {
        return this.getGoals().stream().anyMatch((state) -> (s == state));
    }

    public void runToGoal(final DiscreteActionSolution a) {
        boolean done = false;
        while (!done) {
            tick();
            if (isGoalState(a.getCurrentState())) {
                done = true;
            }
        }
    }

    public void tick() {
        getAgents().stream().forEach(DiscreteActionSolution::nextAction);
    }

    public void setAllRewards(final double d) {
        this.states.stream().forEach((state) -> state.setReward(d));
    }

    public void createAbsorbingState(final State s, final double r) {
        addGoal(s);
        s.setReward(r);
        s.setAllPolicyValues(r);
    }
}
