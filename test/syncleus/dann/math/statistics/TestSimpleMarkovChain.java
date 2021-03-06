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
package syncleus.dann.math.statistics;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;
import syncleus.dann.learn.markov.chain.MarkovChain;
import syncleus.dann.learn.markov.chain.MarkovChainEvidence;
import syncleus.dann.learn.markov.chain.SimpleMarkovChain;
import syncleus.dann.learn.markov.chain.SimpleMarkovChainEvidence;

public class TestSimpleMarkovChain {
    private static enum WeatherState {
        RAINY, SUNNY
    }

    private static final Logger LOGGER = LogManager
            .getLogger(TestSimpleMarkovChain.class);
    private final static Random RANDOM = new Random(1);

    @Test
    public void testExplicitChainFirstOrder() {
        final Map<WeatherState, Map<WeatherState, Double>> transitionProbabilities = new EnumMap<>(WeatherState.class);
        final Map<WeatherState, Double> sunnyTransitions = new EnumMap<>(WeatherState.class);
        final Map<WeatherState, Double> rainyTransitions = new EnumMap<>(WeatherState.class);

		/*
         * final Map<WeatherState, Double> initialTransitions = new
		 * HashMap<WeatherState, Double>();
		 * initialTransitions.put(WeatherState.SUNNY, 0.83333333333);
		 * initialTransitions.put(WeatherState.RAINY, 0.16666666666);
		 * transitionProbabilities.put(null, initialTransitions);
		 */

        sunnyTransitions.put(WeatherState.SUNNY, 0.9);
        sunnyTransitions.put(WeatherState.RAINY, 0.1);
        transitionProbabilities.put(WeatherState.SUNNY, sunnyTransitions);

        rainyTransitions.put(WeatherState.SUNNY, 0.5);
        rainyTransitions.put(WeatherState.RAINY, 0.5);
        transitionProbabilities.put(WeatherState.RAINY, rainyTransitions);

        final Set<WeatherState> states = new HashSet<>();
        states.add(WeatherState.SUNNY);
        states.add(WeatherState.RAINY);

        final MarkovChain<WeatherState> simpleChain = new SimpleMarkovChain<>(
                transitionProbabilities, states);
        simpleChain.transition(WeatherState.SUNNY);

        LOGGER.info("transition columns: "
                + simpleChain.getTransitionProbabilityColumns());
        LOGGER.info("transition rows: "
                + simpleChain.getTransitionProbabilityRows());
        LOGGER.info("transition matrix: "
                + simpleChain.getTransitionProbabilityMatrix());
        LOGGER.info("steady state: "
                + simpleChain.getSteadyStateProbability(WeatherState.SUNNY)
                + " , "
                + simpleChain.getSteadyStateProbability(WeatherState.RAINY));

        Assert.assertEquals("Sunny steady state incorrect", 0.83333333333,
                Math.abs(simpleChain
                        .getSteadyStateProbability(WeatherState.SUNNY)), 0.001);
        Assert.assertEquals("Rainy steady state incorrect", 0.16666666666,
                Math.abs(simpleChain
                        .getSteadyStateProbability(WeatherState.RAINY)), 0.001);
        Assert.assertEquals("Sunny 1 step incorrect", 0.9,
                Math.abs(simpleChain.getProbability(WeatherState.SUNNY, 1)),
                0.001);
        Assert.assertEquals("Rainy 1 step incorrect", 0.1,
                Math.abs(simpleChain.getProbability(WeatherState.RAINY, 1)),
                0.001);
        Assert.assertEquals("Sunny 2 step incorrect", 0.86,
                Math.abs(simpleChain.getProbability(WeatherState.SUNNY, 2)),
                0.001);
        Assert.assertEquals("Rainy 2 step incorrect", 0.14,
                Math.abs(simpleChain.getProbability(WeatherState.RAINY, 2)),
                0.001);
    }

    @Test
    public void testExplicitChainSecondOrder() {
        final Map<List<WeatherState>, Map<WeatherState, Double>> transitionProbabilities = new HashMap<>();

		/*
         * final List<WeatherState> initialState = new
		 * ArrayList<WeatherState>(); final Map<WeatherState, Double>
		 * initialTransitions = new HashMap<WeatherState, Double>();
		 * initialTransitions.put(WeatherState.SUNNY, 0.83333333333);
		 * initialTransitions.put(WeatherState.RAINY, 0.16666666666);
		 * transitionProbabilities.put(initialState, initialTransitions);
		 */

        final List<WeatherState> sunnyState = new ArrayList<>();
        sunnyState.add(WeatherState.SUNNY);
        final Map<WeatherState, Double> sunnyTransitions = new EnumMap<>(WeatherState.class);
        sunnyTransitions.put(WeatherState.SUNNY, 0.9);
        sunnyTransitions.put(WeatherState.RAINY, 0.1);
        transitionProbabilities.put(sunnyState, sunnyTransitions);

        final List<WeatherState> rainyState = new ArrayList<>();
        rainyState.add(WeatherState.RAINY);
        final Map<WeatherState, Double> rainyTransitions = new EnumMap<>(WeatherState.class);
        rainyTransitions.put(WeatherState.SUNNY, 0.5);
        rainyTransitions.put(WeatherState.RAINY, 0.5);
        transitionProbabilities.put(rainyState, rainyTransitions);

        final List<WeatherState> sunnySunnyState = new ArrayList<>();
        sunnySunnyState.add(WeatherState.SUNNY);
        sunnySunnyState.add(WeatherState.SUNNY);
        final Map<WeatherState, Double> sunnySunnyTransitions = new EnumMap<>(WeatherState.class);
        sunnySunnyTransitions.put(WeatherState.SUNNY, 0.9);
        sunnySunnyTransitions.put(WeatherState.RAINY, 0.1);
        transitionProbabilities.put(sunnySunnyState, sunnySunnyTransitions);

        final List<WeatherState> sunnyRainyState = new ArrayList<>();
        sunnyRainyState.add(WeatherState.SUNNY);
        sunnyRainyState.add(WeatherState.RAINY);
        final Map<WeatherState, Double> sunnyRainyTransitions = new EnumMap<>(WeatherState.class);
        sunnyRainyTransitions.put(WeatherState.SUNNY, 0.5);
        sunnyRainyTransitions.put(WeatherState.RAINY, 0.5);
        transitionProbabilities.put(sunnyRainyState, sunnyRainyTransitions);

        final List<WeatherState> rainySunnyState = new ArrayList<>();
        rainySunnyState.add(WeatherState.RAINY);
        rainySunnyState.add(WeatherState.SUNNY);
        final Map<WeatherState, Double> rainySunnyTransitions = new EnumMap<>(WeatherState.class);
        rainySunnyTransitions.put(WeatherState.SUNNY, 0.9);
        rainySunnyTransitions.put(WeatherState.RAINY, 0.1);
        transitionProbabilities.put(rainySunnyState, rainySunnyTransitions);

        final List<WeatherState> rainyRainyState = new ArrayList<>();
        rainyRainyState.add(WeatherState.RAINY);
        rainyRainyState.add(WeatherState.RAINY);
        final Map<WeatherState, Double> rainyRainyTransitions = new EnumMap<>(WeatherState.class);
        rainyRainyTransitions.put(WeatherState.SUNNY, 0.5);
        rainyRainyTransitions.put(WeatherState.RAINY, 0.5);
        transitionProbabilities.put(rainyRainyState, rainyRainyTransitions);

        final Set<WeatherState> states = new HashSet<>();
        states.add(WeatherState.SUNNY);
        states.add(WeatherState.RAINY);

        final MarkovChain<WeatherState> simpleChain = new SimpleMarkovChain<>(
                transitionProbabilities, 2, states);
        simpleChain.transition(WeatherState.SUNNY);

        LOGGER.info("transition columns: "
                + simpleChain.getTransitionProbabilityColumns());
        LOGGER.info("transition rows: "
                + simpleChain.getTransitionProbabilityRows());
        LOGGER.info("transition matrix: "
                + simpleChain.getTransitionProbabilityMatrix());
        LOGGER.info("steady state: "
                + simpleChain.getSteadyStateProbability(WeatherState.SUNNY)
                + " , "
                + simpleChain.getSteadyStateProbability(WeatherState.RAINY));

        Assert.assertEquals("Sunny steady state incorrect", 0.83333333333,
                Math.abs(simpleChain
                        .getSteadyStateProbability(WeatherState.SUNNY)), 0.001);
        Assert.assertEquals("Rainy steady state incorrect", 0.16666666666,
                Math.abs(simpleChain
                        .getSteadyStateProbability(WeatherState.RAINY)), 0.001);
        Assert.assertEquals("Sunny 1 step incorrect", 0.9,
                Math.abs(simpleChain.getProbability(WeatherState.SUNNY, 1)),
                0.001);
        Assert.assertEquals("Rainy 1 step incorrect", 0.1,
                Math.abs(simpleChain.getProbability(WeatherState.RAINY, 1)),
                0.001);
        Assert.assertEquals("Sunny 2 step incorrect", 0.86,
                Math.abs(simpleChain.getProbability(WeatherState.SUNNY, 2)),
                0.001);
        Assert.assertEquals("Rainy 2 step incorrect", 0.14,
                Math.abs(simpleChain.getProbability(WeatherState.RAINY, 2)),
                0.001);
    }

    @Test
    public void testSampledChainFirstOrder() {
        final MarkovChainEvidence<WeatherState> chainEvidence = new SimpleMarkovChainEvidence<>(
                true, 1);
        // determine initial state
        WeatherState lastState;
        if (RANDOM.nextBoolean())
            lastState = WeatherState.SUNNY;
        else
            lastState = WeatherState.RAINY;
        chainEvidence.learnStep(lastState);

        // learn 1000 times
        for (int chainStep = 0; chainStep < 1000; chainStep++) {
            chainEvidence.newChain();
            for (int step = 0; step < 1000; step++) {
                if (lastState == WeatherState.SUNNY) {
                    if (RANDOM.nextDouble() > 0.9) {
                        lastState = WeatherState.RAINY;
                    }
                } else if (lastState == WeatherState.RAINY) {
                    if (RANDOM.nextBoolean()) {
                        lastState = WeatherState.SUNNY;
                    }
                }

                chainEvidence.learnStep(lastState);
            }
        }

        final MarkovChain<WeatherState> simpleChain = chainEvidence
                .getMarkovChain();

        LOGGER.info("transition matrix: "
                + simpleChain.getTransitionProbabilityMatrix());

        simpleChain.transition(WeatherState.SUNNY);

        LOGGER.info("transition columns: "
                + simpleChain.getTransitionProbabilityColumns());
        LOGGER.info("transition rows: "
                + simpleChain.getTransitionProbabilityRows());
        LOGGER.info("steady state: "
                + simpleChain.getSteadyStateProbability(WeatherState.SUNNY)
                + " , "
                + simpleChain.getSteadyStateProbability(WeatherState.RAINY));

        Assert.assertEquals("Sunny steady state incorrect", 0.83333333333,
                Math.abs(simpleChain
                        .getSteadyStateProbability(WeatherState.SUNNY)), 0.025);
        Assert.assertEquals("Rainy steady state incorrect", 0.16666666666,
                Math.abs(simpleChain
                        .getSteadyStateProbability(WeatherState.RAINY)), 0.025);
        Assert.assertEquals("Sunny 1 step incorrect", 0.9,
                Math.abs(simpleChain.getProbability(WeatherState.SUNNY, 1)),
                0.025);
        Assert.assertEquals("Rainy 1 step incorrect", 0.1,
                Math.abs(simpleChain.getProbability(WeatherState.RAINY, 1)),
                0.025);
        Assert.assertEquals("Sunny 2 step incorrect", 0.86,
                Math.abs(simpleChain.getProbability(WeatherState.SUNNY, 2)),
                0.025);
        Assert.assertEquals("Rainy 2 step incorrect", 0.14,
                Math.abs(simpleChain.getProbability(WeatherState.RAINY, 2)),
                0.025);
    }

    @Test
    public void testSampledChainSecondOrder() {
        final MarkovChainEvidence<WeatherState> chainEvidence = new SimpleMarkovChainEvidence<>(
                true, 2);
        // determine initial state
        WeatherState lastState;
        if (RANDOM.nextBoolean())
            lastState = WeatherState.SUNNY;
        else
            lastState = WeatherState.RAINY;
        chainEvidence.learnStep(lastState);

        // learn 1000 times
        for (int chainStep = 0; chainStep < 1000; chainStep++) {
            chainEvidence.newChain();
            for (int step = 0; step < 1000; step++) {
                if (lastState == WeatherState.SUNNY) {
                    if (RANDOM.nextDouble() > 0.9) {
                        lastState = WeatherState.RAINY;
                    }
                } else if (lastState == WeatherState.RAINY) {
                    if (RANDOM.nextBoolean()) {
                        lastState = WeatherState.SUNNY;
                    }
                }

                chainEvidence.learnStep(lastState);
            }
        }

        final MarkovChain<WeatherState> simpleChain = chainEvidence
                .getMarkovChain();

        LOGGER.info("transition matrix: "
                + simpleChain.getTransitionProbabilityMatrix());

        simpleChain.transition(WeatherState.SUNNY);

        LOGGER.info("transition columns: "
                + simpleChain.getTransitionProbabilityColumns());
        LOGGER.info("transition rows: "
                + simpleChain.getTransitionProbabilityRows());
        LOGGER.info("steady state: "
                + simpleChain.getSteadyStateProbability(WeatherState.SUNNY)
                + " , "
                + simpleChain.getSteadyStateProbability(WeatherState.RAINY));

        Assert.assertEquals("Sunny steady state incorrect", 0.83333333333,
                Math.abs(simpleChain
                        .getSteadyStateProbability(WeatherState.SUNNY)), 0.025);
        Assert.assertEquals("Rainy steady state incorrect", 0.16666666666,
                Math.abs(simpleChain
                        .getSteadyStateProbability(WeatherState.RAINY)), 0.025);
        Assert.assertEquals("Sunny 1 step incorrect", 0.9,
                Math.abs(simpleChain.getProbability(WeatherState.SUNNY, 1)),
                0.025);
        Assert.assertEquals("Rainy 1 step incorrect", 0.1,
                Math.abs(simpleChain.getProbability(WeatherState.RAINY, 1)),
                0.025);
        Assert.assertEquals("Sunny 2 step incorrect", 0.86,
                Math.abs(simpleChain.getProbability(WeatherState.SUNNY, 2)),
                0.025);
        Assert.assertEquals("Rainy 2 step incorrect", 0.14,
                Math.abs(simpleChain.getProbability(WeatherState.RAINY, 2)),
                0.025);
    }
}
