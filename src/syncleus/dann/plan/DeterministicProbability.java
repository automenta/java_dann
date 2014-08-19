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
package syncleus.dann.plan;

import syncleus.dann.solve.Grid2DMotionProblem;
import syncleus.dann.math.geometry.GridState;
import syncleus.dann.plan.grid2d.AbstractProbability;

import java.util.Set;
import java.util.TreeSet;

public class DeterministicProbability extends AbstractProbability {

    public DeterministicProbability(final Grid2DMotionProblem theWorld) {
        super(theWorld);
    }

    @Override
    public double calculate(final State resultState, final State previousState,
                            final Action desiredAction) {

        if (!(resultState instanceof GridState)
                || !(previousState instanceof GridState)) {
            throw new RuntimeException("Must be instance of GridState");
        }

        final GridState gridResultState = (GridState) resultState;
        final GridState gridPreviousState = (GridState) previousState;

        final Action resultingAction = determineResultingAction(
                gridPreviousState, gridResultState);
        final GridState desiredState = determineActionState(gridPreviousState,
                desiredAction);

        // are we trying to move nowhere
        if (gridResultState == gridPreviousState) {
            if (Grid2DMotionProblem.isStateBlocked(desiredState))
                return 1.0;
            else
                return 0.0;
        }

        if (resultingAction != null)
            return 1.0;
        else
            return 0.0;
    }

    @Override
    public Set<SuccessorState> determineSuccessorStates(final State state,
                                                        final Action action) {

        final Set<SuccessorState> result = new TreeSet<>();
        if (action != null) {
            final State newState = determineActionState((GridState) state,
                    action);
            result.add(new SuccessorState(newState, 1.0));
        }
        return result;
    }

}
