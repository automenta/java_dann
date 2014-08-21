package syncleus.dann.plan.qlearning;

import java.util.Arrays;
import syncleus.dann.plan.DiscreteActionProblem;
import syncleus.dann.plan.DiscreteActionSolution;
import syncleus.dann.plan.State;
import syncleus.dann.plan.qlearning.elsy.QBrain;
import syncleus.dann.plan.qlearning.elsy.QPerception;

/** Adapted from: http://elsy.gdan.pl/index.php , package pl.gdan.elsy */
public class QLearningElsy<A> implements DiscreteActionSolution<A> {

    
    private QBrain brain;
    double nextReward;
    double[] sensor;
    double[] action;
    private State state;
    private DiscreteActionProblem<A> problem;


    @Override
    public boolean setProblem(DiscreteActionProblem<A> p) {


        this.problem = p;
        int numActions = p.getActions().size();
        int sensors = p.getStates().peek().getData().length;

        sensor = new double[sensors];

        brain = new QBrain(new QPerception() {

            @Override
            public boolean isUnipolar() {
                return true;
            }

            @Override
            public double getReward() {
                return nextReward;
            }

            @Override
            protected void updateInputValues() {
                for (int i = 0; i < sensor.length; i++)
                    setNextValue(sensor[i]);
            }
        }, numActions);

		/*
         * brain = new Brain(new DAPerception(sensor, 4) {
		 *
		 * @Override public boolean isUnipolar() { return true; }
		 *
		 * @Override public double getReward() { return nextReward; }
		 *
		 * }, qaction );
		 */

        brain.reset();
        
        return true;
    }

    
    double minReward = Double.MAX_VALUE;
    double maxReward = Double.MIN_VALUE;

    public int step(final double reward) {
        maxReward = Math.max(reward, maxReward);
        minReward = Math.min(reward, minReward);
        this.nextReward = (reward - minReward) / (maxReward - minReward) - 0.5;

        brain.getPerception().perceive();
        brain.count();

        Arrays.fill(action, 0.0);
        final int a = brain.getAction();
        action[a] = 1.0;

        // brain.printStats();
        // System.out.println(reward + " " + a);
        // Util.printArray(brain.getInput());
        // Util.printArray(brain.getOutput());
        // Util.printArray(action);

        return a;
    }

    public double[] getSensor() {
        return sensor;
    }

    public double[] getAction() {
        return action;
    }

    @Override
    public State getCurrentState() {
        return this.state;
    }

    @Override
    public void setCurrentState(State s) {
        this.state = s;
    }

    @Override
    public DiscreteActionProblem<A> getProblem() {
        return problem;
    }

    @Override
    public A nextAction() {
        double reward = state.getReward();
        return problem.getActions().get(step(reward));
    }

}
