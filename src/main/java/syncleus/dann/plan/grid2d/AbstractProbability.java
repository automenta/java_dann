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

import syncleus.dann.plan.DiscreteActionProblem;
import syncleus.dann.plan.State;
import syncleus.dann.plan.ActionProbability;


public abstract class AbstractProbability<A> implements ActionProbability<A> {

    private final DiscreteActionProblem<A> world;

    public AbstractProbability(final DiscreteActionProblem<A> theWorld) {
        this.world = theWorld;
    }

    public A determineResultingAction(final State s1, final State s2) {

        if ((s1.getRow() - 1) == s2.getRow()
                && s1.getColumn() == s2.getColumn()) {
            return Grid2D.ACTION_NORTH;
        } else if ((s1.getRow() == s2.getRow() + 1)
                && s1.getColumn() == s2.getColumn()) {
            return Grid2D.ACTION_SOUTH;
        } else if (s1.getRow() == s2.getRow()
                && (s1.getColumn() + 1) == s2.getColumn()) {
            return Grid2D.ACTION_EAST;
        } else if (s1.getRow() == s2.getRow()
                && (s1.getColumn() - 1) == s2.getColumn()) {
            return Grid2D.ACTION_EAST;
        }

        return null;
    }

    public A determineActionState(final GridState currentState,
                                          final Action action) {

        A result = null;

        if (action == Grid2D.ACTION_NORTH) {
            result = this.world.getState(currentState.getRow() - 1,
                    currentState.getColumn());
        } else if (action == Grid2D.ACTION_SOUTH) {
            result = this.world.getState(currentState.getRow() + 1,
                    currentState.getColumn());
        } else if (action == Grid2D.ACTION_EAST) {
            result = this.world.getState(currentState.getRow(),
                    currentState.getColumn() + 1);
        } else if (action == Grid2D.ACTION_WEST) {
            result = this.world.getState(currentState.getRow(),
                    currentState.getColumn() - 1);
        }

        if (result == null) {
            result = currentState;
        }

        return result;
    }

 

}
