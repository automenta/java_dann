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
import syncleus.dann.plan.DiscreteActionProblem;
import syncleus.dann.plan.DiscreteActionSolution;
import syncleus.dann.plan.State;

import java.util.*;

public abstract class BasicDiscreteActionProblem<A, S extends State> implements DiscreteActionProblem<A,S> {
    private final List<A> actions = new ArrayList<>();
    private final Queue<S> states = new ArrayDeque<>();
    private ActionProbability probability;
    private final List<DiscreteActionSolution> agents = new ArrayList<>();
    protected final Set<S> goals = new HashSet<>();
 
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

    public void setPolicyValue(final S state, final A action,
                               final double r) {
        final int index = requireActionIndex(action);
        state.getPolicyValue()[index] = r;
    }

    public double getPolicyValue(final S state, final A action) {
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

    public void removeRewardBelow(final List<S> states,
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

    public void addAgent(final DiscreteActionSolution<A,S> agent) {
        this.agents.add(agent);
        agent.setProblem(this);
    }

    public void removeAgent(final DiscreteActionSolution agent) {
        this.agents.remove(agent);
        agent.setProblem(null);
    }

    @Override
    public void addGoal(final S s) {
        this.goals.add(s);
    }

    @Override
    public void removeGoal(final S s) {
        this.goals.remove(s);

    }

    @Override
    public Set<S> getGoals() {
        return this.goals;
    }

    @Override
    public void addState(final S state) {
        this.states.add(state);
    }

    @Override
    public Queue<S> getStates() {
        return this.states;
    }

    @Override
    public boolean isGoalState(final S s) {
        return this.getGoals().stream().anyMatch((state) -> (s == state));
    }

    public void runToGoal(final DiscreteActionSolution<A,S> a) {
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

    public void createAbsorbingS(final S s, final double r) {
        addGoal(s);
        s.setReward(r);
        s.setAllPolicyValues(r);
    }
}
