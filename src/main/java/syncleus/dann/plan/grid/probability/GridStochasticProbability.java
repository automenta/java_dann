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
package syncleus.dann.plan.grid.probability;

import syncleus.dann.plan.Action;
import syncleus.dann.plan.State;
import syncleus.dann.plan.SuccessorState;
import syncleus.dann.plan.WorldError;
import syncleus.dann.plan.grid.GridState;
import syncleus.dann.plan.grid.GridWorld;
import syncleus.dann.math.EncogMath;

import java.util.Set;
import java.util.TreeSet;

public class GridStochasticProbability extends GridAbstractProbability {

    private double probabilitySuccess;
    private double probabilitySame;
    private double probabilityLeft;
    private double probabilityRight;
    private double probabilityReverse;

    public GridStochasticProbability(final GridWorld theWorld,
                                     final double theProbabilitySuccess,
                                     final double theProbabilitySame, final double theProbabilityLeft,
                                     final double theProbabilityRight, final double theProbabilityReverse) {
        super(theWorld);
        this.probabilitySuccess = theProbabilitySuccess;
        this.probabilitySame = theProbabilitySame;
        this.probabilityLeft = theProbabilityLeft;
        this.probabilityRight = theProbabilityRight;
        this.probabilityReverse = theProbabilityReverse;
    }

    public GridStochasticProbability(final GridWorld theWorld) {
        this(theWorld, 0.8, 0.0, 0.1, 0.1, 0.0);
    }

    /**
     * @return the probabilitySuccess
     */
    public double getProbabilitySuccess() {
        return probabilitySuccess;
    }

    /**
     * @param probabilitySuccess the probabilitySuccess to set
     */
    public void setProbabilitySuccess(final double probabilitySuccess) {
        this.probabilitySuccess = probabilitySuccess;
    }

    /**
     * @return the probabilitySame
     */
    public double getProbabilitySame() {
        return probabilitySame;
    }

    /**
     * @param probabilitySame the probabilitySame to set
     */
    public void setProbabilitySame(final double probabilitySame) {
        this.probabilitySame = probabilitySame;
    }

    /**
     * @return the probabilityLeft
     */
    public double getProbabilityLeft() {
        return probabilityLeft;
    }

    /**
     * @param probabilityLeft the probabilityLeft to set
     */
    public void setProbabilityLeft(final double probabilityLeft) {
        this.probabilityLeft = probabilityLeft;
    }

    /**
     * @return the probabilityRight
     */
    public double getProbabilityRight() {
        return probabilityRight;
    }

    /**
     * @param probabilityRight the probabilityRight to set
     */
    public void setProbabilityRight(final double probabilityRight) {
        this.probabilityRight = probabilityRight;
    }

    /**
     * @return the probabilityReverse
     */
    public double getProbabilityReverse() {
        return probabilityReverse;
    }

    /**
     * @param probabilityReverse the probabilityReverse to set
     */
    public void setProbabilityReverse(final double probabilityReverse) {
        this.probabilityReverse = probabilityReverse;
    }

    @Override
    public double calculate(final State resultState, final State previousState,
                            final Action desiredAction) {
        if (!(resultState instanceof GridState)
                || !(previousState instanceof GridState)) {
            throw new WorldError("Must be instance of GridState");
        }

        final GridState gridResultState = (GridState) resultState;
        final GridState gridPreviousState = (GridState) previousState;

        final Action resultingAction = determineResultingAction(
                gridPreviousState, gridResultState);
        final GridState desiredState = determineActionState(gridPreviousState,
                desiredAction);

        // are we trying to move nowhere
        if (gridResultState == gridPreviousState) {
            if (GridWorld.isStateBlocked(desiredState))
                return this.probabilitySuccess;
            else
                return 0.0;
        }

        if (resultingAction == desiredAction) {
            return this.probabilitySuccess;
        } else if (resultingAction == GridWorld.rightOfAction(desiredAction)) {
            return this.probabilityRight;
        } else if (resultingAction == GridWorld.leftOfAction(desiredAction)) {
            return this.probabilityLeft;
        } else if (resultingAction == GridWorld.reverseOfAction(desiredAction)) {
            return this.probabilityReverse;
        } else {
            return 0.0;
        }
    }

    @Override
    public Set<SuccessorState> determineSuccessorStates(final State state,
                                                        final Action action) {

        final Set<SuccessorState> result = new TreeSet<>();

        if (action != null) {
            // probability of successful action
            if (this.probabilitySuccess > EncogMath.DEFAULT_EPSILON) {
                final State newState = determineActionState((GridState) state,
                        action);
                if (newState != null)
                    result.add(new SuccessorState(newState,
                            this.probabilitySuccess));
            }

            // probability of left
            if (this.probabilityLeft > EncogMath.DEFAULT_EPSILON) {
                final State newState = determineActionState((GridState) state,
                        GridWorld.leftOfAction(action));
                if (newState != null)
                    result.add(new SuccessorState(newState,
                            this.probabilityLeft));
            }

            // probability of right
            if (this.probabilityRight > EncogMath.DEFAULT_EPSILON) {
                final State newState = determineActionState((GridState) state,
                        GridWorld.rightOfAction(action));
                if (newState != null)
                    result.add(new SuccessorState(newState,
                            this.probabilityRight));
            }

            // probability of reverse
            if (this.probabilityReverse > EncogMath.DEFAULT_EPSILON) {
                final State newState = determineActionState((GridState) state,
                        GridWorld.reverseOfAction(action));
                if (newState != null)
                    result.add(new SuccessorState(newState,
                            this.probabilityReverse));
            }
        }

        return result;
    }
}
