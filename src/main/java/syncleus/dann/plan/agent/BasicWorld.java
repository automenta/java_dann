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
package syncleus.dann.plan.agent;

import syncleus.dann.plan.*;
import syncleus.dann.math.geometry.GridState;

import java.util.ArrayList;
import java.util.List;

public abstract class BasicWorld implements World {
    private final List<State> states = new ArrayList<>();
    private final List<Action> actions = new ArrayList<>();
    private ActionProbability probability;
    private final List<WorldAgent> agents = new ArrayList<>();
    private final List<State> goals = new ArrayList<>();

    @Override
    public List<Action> getActions() {
        return this.actions;
    }

    @Override
    public void addAction(final Action action) {
        this.actions.add(action);
    }

    private int getActionIndex(final Action a) {
        return actions.indexOf(a);
    }

    private int requireActionIndex(final Action a) {
        final int result = getActionIndex(a);
        if (result == -1) {
            throw new RuntimeException("No such action: " + a);
        }
        return result;
    }

    @Override
    public void setPolicyValue(final State state, final Action action,
                               final double r) {
        final int index = requireActionIndex(action);
        state.getPolicyValue()[index] = r;

    }

    @Override
    public double getPolicyValue(final State state, final Action action) {
        final int index = requireActionIndex(action);
        return state.getPolicyValue()[index];
    }

    /**
     * @return the probability
     */
    @Override
    public ActionProbability getProbability() {
        return probability;
    }

    /**
     * @param probability the probability to set
     */
    @Override
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

    @Override
    public List<WorldAgent> getAgents() {
        return this.agents;
    }

    @Override
    public void addAgent(final WorldAgent agent) {
        this.agents.add(agent);
        agent.setWorld(this);
    }

    @Override
    public void removeAgent(final WorldAgent agent) {
        this.agents.remove(agent);
        agent.setWorld(null);
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
    public List<State> getGoals() {
        return this.goals;
    }

    @Override
    public void addState(final State state) {
        this.states.add(state);
    }

    @Override
    public List<State> getStates() {
        return this.states;
    }

    @Override
    public boolean isGoalState(final State s) {
        return this.getGoals().stream().anyMatch((state) -> (s == state));
    }

    @Override
    public void runToGoal(final WorldAgent a) {
        boolean done = false;
        while (!done) {
            tick();
            if (isGoalState(a.getCurrentState())) {
                done = true;
            }
        }
    }

    @Override
    public void tick() {
        getAgents().stream().forEach(WorldAgent::tick);
    }

    @Override
    public void setAllRewards(final double d) {
        this.states.stream().forEach((state) -> state.setReward(d));
    }

    public void createAbsorbingState(final State s, final double r) {
        addGoal(s);
        s.setReward(r);
        s.setAllPolicyValues(r);
    }
}
