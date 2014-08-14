/******************************************************************************
 *                                                                             *
 *  Copyright: (c) Syncleus, Inc.                                              *
 *                                                                             *
 *  You may redistribute and modify this source code under the terms and       *
 *  conditions of the Open Source Community License - Type C version 1.0       *
 *  or any later version as published by Syncleus, Inc. at www.syncleus.com.   *
 *  There should be a copy of the license included with this file. If a copy   *
 *  of the license is not included you are granted no right to distribute or   *
 *  otherwise use this file except through a legal and valid license. You      *
 *  should also contact Syncleus, Inc. at the information below if you cannot  *
 *  find a license:                                                            *
 *                                                                             *
 *  Syncleus, Inc.                                                             *
 *  2604 South 12th Street                                                     *
 *  Philadelphia, PA 19148                                                     *
 *                                                                             *
 ******************************************************************************/
package syncleus.dann.genetics;

import org.junit.Assert;
import org.junit.Test;

import syncleus.dann.evolve.MutableInteger;

public class TestMutableInteger {
	@Test
	public void testConstructors() {
		MutableInteger test = new MutableInteger(123);
		Assert.assertTrue("value constructor failed", test.getNumber() == 123);
		test = new MutableInteger("456");
		Assert.assertTrue("string value constructor failed",
				test.getNumber() == 456);
		test = new MutableInteger(789);
		Assert.assertTrue("Number value constructor failed",
				test.getNumber() == 789);
	}

	@Test
	public void testMax() {
		final MutableInteger highValue = new MutableInteger(Integer.MAX_VALUE);

		for (int testCount = 0; testCount < 1000; testCount++) {
			final MutableInteger mutated = highValue.mutate(100.0);

			Assert.assertTrue(
					"mutation caused number to roll over: " + mutated,
					mutated.intValue() >= -1);
		}
	}

	@Test
	public void testMin() {
		final MutableInteger lowValue = new MutableInteger(Integer.MIN_VALUE);

		for (int testCount = 0; testCount < 1000; testCount++) {
			final MutableInteger mutated = lowValue.mutate(100.0);

			Assert.assertTrue(
					"mutation caused number to roll over: " + mutated,
					mutated.intValue() <= -1);
		}
	}

	@Test
	public void testDeviation() {
		final MutableInteger center = new MutableInteger(0);
		double averageSum = 0;
		double testCount;
		for (testCount = 0.0; testCount < 10000; testCount++) {
			averageSum += center.mutate(1.0).intValue();
		}
		final double average = averageSum / testCount;
		Assert.assertTrue("average deviation is more than 1.0", average < 1.0);
	}
}
