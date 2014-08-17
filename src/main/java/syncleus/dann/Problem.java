package syncleus.dann;

import java.util.Queue;
import java.util.Set;
import syncleus.dann.plan.State;

/**
 * Abstract definition of a problem in terms of a set of goals (with assigned probabilities)
 * and a historical queue (list) of states that have been reached towards a solution.
 */
public interface Problem {

    void addGoal(State s);
    void removeGoal(State s);

    void addState(State state);

    Set<State> getGoals();

    default double getGoalPriority(State s) { return 1.0; }
    
    Queue<State> getStates();

    boolean isGoalState(State s);

    
}
