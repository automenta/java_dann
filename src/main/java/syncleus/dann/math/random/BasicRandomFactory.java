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
package syncleus.dann.math.random;

import java.io.Serializable;
import java.util.Random;

/**
 * Basic random number generator factory. Simply returns the Random class.
 */
public class BasicRandomFactory implements RandomFactory, Serializable {

	/**
	 * Serial ID.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * A random generator to generate random seeds.
	 */
	private final Random seedProducer;

	/**
	 * Construct a random generator factory. No assigned seed.
	 */
	public BasicRandomFactory() {
		this.seedProducer = new Random();
	}

	/**
	 * Construct a random generator factory with the specified seed.
	 * 
	 * @param theSeed
	 *            The seed.
	 */
	public BasicRandomFactory(final long theSeed) {
		this.seedProducer = new Random(theSeed);
	}

	/**
	 * @return Factor a new random generator.
	 */
	@Override
	public Random factor() {
		synchronized (this) {
			final long seed = this.seedProducer.nextLong();
			return new Random(seed);
		}
	}

	/**
	 * @return Factor a new random generator factor.
	 */
	@Override
	public RandomFactory factorFactory() {
		return new BasicRandomFactory(this.seedProducer.nextLong());
	}

}
