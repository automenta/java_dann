package syncleus.dann;

import java.util.Queue;
import java.util.Set;
import syncleus.dann.plan.State;

/**
 * Abstract definition of a problem in terms of a set of goals (with assigned probabilities)
 * and a historical queue (list) of states that have been reached towards a solution.
 */
public interface Problem<S extends State> {

    void addGoal(S s);
    void removeGoal(S s);

    void addState(S state);

    Set<S> getGoals();

    default double getGoalPriority(S s) { return 1.0; }
    
    Queue<S> getStates();

    boolean isGoalState(S s);

    
}
