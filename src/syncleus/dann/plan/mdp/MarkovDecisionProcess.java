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

public class MarkovDecisionProcess<A,S extends State> {

    private final DiscreteActionProblem<A,S> world;
    private final State goal;
    protected ActionProbability<A> probability;

    public MarkovDecisionProcess(final DiscreteActionProblem<A,S> theWorld, ActionProbability<A> probability, State goal) {
        this.world = theWorld;
        this.goal = goal;
        this.probability = probability;
    }

    public MarkovDecisionProcess(final DiscreteActionProblem<A,S> theWorld, ActionProbability<A> probability) {
        this(theWorld, probability, theWorld.getGoals().iterator().next());
        assert(theWorld.getGoals().size() == 1);                
    }
    
    

    /**
     * @return the world
     */
    public DiscreteActionProblem<A,S> getProblem() {
        return world;
    }

    /**
     * @return the goal
     */
    public State getGoal() {
        return goal;
    }
}
