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
package syncleus.dann.graph.cycle;

import syncleus.dann.graph.Graph;

public final class Cycles {
    private static final CycleFinder EXHAUSTIVE_FINDER = new ExhaustiveDepthFirstSearchCycleFinder();
    private static final CycleDetector COLORED_DETECTOR = new ColoredDepthFirstSearchDetector();

    /**
     * This is an utility class, so it can not be instantiated.
     */
    private Cycles() {
        throw new IllegalStateException(
                "This is an utility class, it can not be instantiated");
    }

    public static int getCycleCount(final Graph graph) {
        if (graph instanceof CycleOptimizedGraph) {
            try {
                return ((CycleOptimizedGraph) graph).getCycleCount();
            } catch (final UnsupportedOperationException caught) {
                // if its not supported lets handle it as if its not optimized
            }
        }

        return EXHAUSTIVE_FINDER.cycleCount(graph);
    }

    public static boolean isPancyclic(final Graph graph) {
        if (graph instanceof CycleOptimizedGraph) {
            try {
                return ((CycleOptimizedGraph) graph).isPancyclic();
            } catch (final UnsupportedOperationException caught) {
                // if its not supported lets handle it as if its not optimized
            }
        }

        return EXHAUSTIVE_FINDER.isPancyclic(graph);
    }

    public static boolean isUnicyclic(final Graph graph) {
        if (graph instanceof CycleOptimizedGraph) {
            try {
                return ((CycleOptimizedGraph) graph).isUnicyclic();
            } catch (final UnsupportedOperationException caught) {
                // if its not supported lets handle it as if its not optimized
            }
        }

        return EXHAUSTIVE_FINDER.isUnicyclic(graph);
    }

    public static boolean isAcyclic(final Graph graph) {
        if (graph instanceof CycleOptimizedGraph) {
            try {
                return ((CycleOptimizedGraph) graph).isAcyclic();
            } catch (final UnsupportedOperationException caught) {
                // if its not supported lets handle it as if its not optimized
            }
        }

        return !COLORED_DETECTOR.hasCycle(graph);
    }

    public static int getGirth(final Graph graph) {
        if (graph instanceof CycleOptimizedGraph) {
            try {
                return ((CycleOptimizedGraph) graph).getGirth();
            } catch (final UnsupportedOperationException caught) {
                // if its not supported lets handle it as if its not optimized
            }
        }

        return EXHAUSTIVE_FINDER.girth(graph);
    }

    public static int getCircumference(final Graph graph) {
        if (graph instanceof CycleOptimizedGraph) {
            try {
                return ((CycleOptimizedGraph) graph).getCircumference();
            } catch (final UnsupportedOperationException caught) {
                // if its not supported lets handle it as if its not optimized
            }
        }

        return EXHAUSTIVE_FINDER.circumference(graph);
    }
}
