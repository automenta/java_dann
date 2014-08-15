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
package syncleus.dann.plan.grid;

import java.util.ArrayList;
import java.util.List;

import syncleus.dann.math.EncogMath;
import syncleus.dann.plan.Action;
import syncleus.dann.plan.State;
import syncleus.dann.plan.basic.BasicAction;
import syncleus.dann.plan.basic.BasicWorld;

public class GridWorld extends BasicWorld {

    public static final Action ACTION_NORTH = new BasicAction("NORTH");
    public static final Action ACTION_SOUTH = new BasicAction("SOUTH");
    public static final Action ACTION_EAST = new BasicAction("EAST");
    public static final Action ACTION_WEST = new BasicAction("WEST");

    private final GridState[][] state;

    public GridWorld(final int rows, final int columns) {
        addAction(ACTION_NORTH);
        addAction(ACTION_SOUTH);
        addAction(ACTION_EAST);
        addAction(ACTION_WEST);
        this.state = new GridState[rows][columns];

        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < columns; col++) {
                final GridState state = new GridState(this, row, col, false);
                addState(state);
                this.state[row][col] = state;
                this.state[row][col].setPolicyValueSize(getActions().size());
            }
        }

    }

    public static boolean isStateBlocked(final GridState state) {
        return state == null;
    }

    public int getRows() {
        return this.state.length;
    }

    public int getColumns() {
        return this.state[0].length;
    }

    public GridState getState(final int row, final int column) {
        if (row < 0 || row >= getRows()) {
            return null;
        } else if (column < 0 || column >= getColumns()) {
            return null;
        }
        return this.state[row][column];
    }

    public static Action leftOfAction(final Action action) {
        if (action == GridWorld.ACTION_NORTH) {
            return GridWorld.ACTION_WEST;
        } else if (action == GridWorld.ACTION_SOUTH) {
            return GridWorld.ACTION_EAST;
        } else if (action == GridWorld.ACTION_EAST) {
            return GridWorld.ACTION_NORTH;
        } else if (action == GridWorld.ACTION_WEST) {
            return GridWorld.ACTION_SOUTH;
        }
        return null;
    }

    public static Action rightOfAction(final Action action) {
        if (action == GridWorld.ACTION_NORTH) {
            return GridWorld.ACTION_EAST;
        } else if (action == GridWorld.ACTION_SOUTH) {
            return GridWorld.ACTION_WEST;
        } else if (action == GridWorld.ACTION_EAST) {
            return GridWorld.ACTION_SOUTH;
        } else if (action == GridWorld.ACTION_WEST) {
            return GridWorld.ACTION_NORTH;
        }
        return null;
    }

    public static Action reverseOfAction(final Action action) {
        if (action == GridWorld.ACTION_NORTH) {
            return GridWorld.ACTION_SOUTH;
        } else if (action == GridWorld.ACTION_SOUTH) {
            return GridWorld.ACTION_NORTH;
        } else if (action == GridWorld.ACTION_EAST) {
            return GridWorld.ACTION_WEST;
        } else if (action == GridWorld.ACTION_WEST) {
            return GridWorld.ACTION_EAST;
        }
        return null;
    }

    public List<GridState> getAdjacentStates(final GridState s) {
        final List<GridState> result = new ArrayList<>();
        final GridState northState = this.getState(s.getRow() - 1,
                s.getColumn());
        final GridState southState = this.getState(s.getRow() + 1,
                s.getColumn());
        final GridState eastState = this
                .getState(s.getRow(), s.getColumn() + 1);
        final GridState westState = this
                .getState(s.getRow(), s.getColumn() - 1);

        if (!isStateBlocked(northState)) {
            result.add(northState);
        }

        if (!isStateBlocked(southState)) {
            result.add(southState);
        }

        if (!isStateBlocked(eastState)) {
            result.add(eastState);
        }

        if (!isStateBlocked(westState)) {
            result.add(westState);
        }

        if (!isStateBlocked(s)) {
            result.add(s);
        }

        return result;
    }

    public static double euclideanDistance(final GridState s1,
                                           final GridState s2) {
        final double d = EncogMath.square(s1.getRow() - s2.getRow())
                + EncogMath.square(s1.getColumn() - s2.getColumn());
        return Math.sqrt(d);
    }

    public GridState findClosestStateTo(final List<GridState> states,
                                        final GridState goalState) {
        double min = Double.POSITIVE_INFINITY;
        GridState minState = null;

        for (final GridState state : states) {
            final double d = euclideanDistance(state, goalState);
            if (d < min) {
                min = d;
                minState = state;
            }
        }

        return minState;
    }

    public Action determineActionToState(final GridState currentState,
                                         final GridState targetState) {
        final int rowDiff = currentState.getRow() - targetState.getRow();
        final int colDiff = currentState.getColumn() - targetState.getColumn();

        if (rowDiff == 0 && colDiff == 0)
            return null;

        if (Math.abs(rowDiff) >= Math.abs(colDiff)) {
            if (rowDiff < 0)
                return GridWorld.ACTION_SOUTH;
            else
                return GridWorld.ACTION_NORTH;
        } else {
            if (colDiff < 0)
                return GridWorld.ACTION_EAST;
            else
                return GridWorld.ACTION_WEST;
        }
    }

    public GridState findClosestStateToGoal(final List<GridState> states) {
        double min = Double.POSITIVE_INFINITY;
        GridState minState = null;

        for (final State goalState : this.getGoals()) {
            for (final GridState state : states) {
                final double d = euclideanDistance(state, (GridState) goalState);
                if (d < min) {
                    min = d;
                    minState = state;
                }
            }
        }

        return minState;
    }

    public void setBlocked(final int row, final int column) {
        final State state = this.state[row][column];
        this.state[row][column] = null;
        this.getStates().remove(state);
    }
}
