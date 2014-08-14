/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package syncleus.dann.control.qlearning;

import java.util.Arrays;

/**
 *
 * @author me
 */
public class QLearner {

	private Action[] qaction;
	private QBrain brain;
	double nextReward;
	double[] sensor;
	double[] action;

	public QLearner(final int sensors, final Action[] actions) {

		this.qaction = actions;

		for (int i = 0; i < actions.length; i++) {
			final int I = i;
			qaction[i] = new Action() {
				@Override
				public int execute() {
					return I;
				}
			};
		}

		sensor = new double[sensors];

		brain = new QBrain(new Perception() {

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
		}, qaction);

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

}
