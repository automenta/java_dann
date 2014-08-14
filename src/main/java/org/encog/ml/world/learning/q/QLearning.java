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
package org.encog.ml.world.learning.q;

import org.encog.ml.world.Action;
import org.encog.ml.world.State;
import org.encog.ml.world.World;

public class QLearning {

	private final World world;
	/**
	 * The learning rate (alpha).
	 */
	private final double learningRate;

	/**
	 * The discount rate (gamma).
	 */
	private final double discountRate;

	public QLearning(final World theWorld, final double theLearningRate,
			final double theDiscountRate) {
		this.world = theWorld;
		this.learningRate = theLearningRate;
		this.discountRate = theDiscountRate;
	}

	public void learn(final State s1, final Action a1, final State s2,
			final Action a2) {
		final double q1 = this.world.getPolicyValue(s1, a1);
		final double q2 = this.world.getPolicyValue(s2, a2);
		final double r = s1.getReward();
		final double d = q1 + this.learningRate
				* (r + this.discountRate * q2 - q1);
		this.world.setPolicyValue(s1, a1, d);
	}
}
