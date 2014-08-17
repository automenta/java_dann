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
package syncleus.dann.math.counting;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class TestCounters {
    private static final Logger LOGGER = LogManager
            .getLogger(TestCounters.class);
    private static final String SUPER_SET = "1234";
    private static final int COMBINATION_COUNT = 15;

    @Test
    public void testStringCombinations() {
        LOGGER.info("Generating combinations for: " + SUPER_SET);
        final char[] lettersArray = SUPER_SET.toCharArray();
        final List<Character> letters = new ArrayList<>();
        for (final char letter : lettersArray)
            letters.add(letter);
        final Set<List<Character>> combinations = Counters
                .everyCombination(letters);
        combinations.stream().map((combination) -> {
            final StringBuilder combinationString = new StringBuilder(
                    combination.size());
            combination.stream().forEach(combinationString::append);
            return combinationString;
        }).forEach((combinationString) -> LOGGER.info("Combination Generated: " + combinationString));

        Assert.assertTrue(
                "Wrong number of combinations: " + combinations.size(),
                combinations.size() == COMBINATION_COUNT);
    }
}
