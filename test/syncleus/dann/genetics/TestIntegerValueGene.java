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
import syncleus.dann.evolve.IntegerValueGene;
import syncleus.dann.evolve.MutableInteger;
import syncleus.dann.evolve.ValueGene;

public class TestIntegerValueGene {
    @Test
    public void testConstructors() {
        ValueGene test = new IntegerValueGene(4765);
        Assert.assertTrue("value constructor failed", test.getValue()
                .getNumber().intValue() == 4765);
        test = new IntegerValueGene(new MutableInteger(5700));
        Assert.assertTrue("MutableInteger value constructor failed", test
                .getValue().getNumber().intValue() == 5700);
        test = new IntegerValueGene(8300);
        Assert.assertTrue("Number value constructor failed", test.getValue()
                .getNumber().intValue() == 8300);
        test = new IntegerValueGene();
        Assert.assertTrue("default constructor failed", test.getValue()
                .getNumber().intValue() == 0);
    }

    @Test
    public void testMutation() {
        final ValueGene center = new IntegerValueGene(0);
        int averageSum = 0;
        int testCount;
        for (testCount = 0; testCount < 1000; testCount++) {
            averageSum += center.mutate(100).getValue().intValue();
        }
        final int average = averageSum / testCount;
        Assert.assertTrue("average deviation is more than 100.0", average < 100);
    }
}
