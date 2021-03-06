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
package syncleus.dann.learn.markov.chain;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;
import syncleus.dann.data.matrix.RealMatrix;
import syncleus.dann.data.matrix.SimpleRealMatrix;

public class SimpleMarkovChain<S> extends AbstractMarkovChain<S> {
    private final RealMatrix transitionProbabilityMatrix;
    private final ArrayList<List<S>> rowMapping;
    private final ArrayList<S> columnMapping;
    private final Set<S> states;
    private final int order;
    private final ArrayDeque<S> history;
    private static final double MAXIMUM_ROW_ERROR = 0.00001;
    private static final Random RANDOM = new Random();

 
    
    public SimpleMarkovChain(
            final Map<List<S>, Map<S, Double>> transitionProbabilities,
            final int order, final Set<S> states) {
        // because we add new states first, and then remove old ones, if there
        // are too many, we need +1
        this.history = new ArrayDeque<>(order + 1);
        this.order = order;
        this.states = Collections.unmodifiableSet(states);
        this.rowMapping = new ArrayList<>();

        final int columns = (this.states.size() > transitionProbabilities
                .size() ? this.states.size() : transitionProbabilities.size());
        final int rows = columns;

        final double[][] matrixValues = new double[rows][columns];

        this.columnMapping = new ArrayList<>(this.states);

        final Set<List<S>> rowHeadersLeft = new HashSet<>(
                transitionProbabilities.keySet());

        // if there is an empty set, representing an undefined starting point,
        // then it is always the first row
        if (rowHeadersLeft.remove(Collections.<S>emptyList())) {
            this.rowMapping.add(Collections.<S>emptyList());
        }

        this.columnMapping.stream().map(Collections::singletonList).map((columnAsHeader) -> {
            assert rowHeadersLeft.contains(columnAsHeader);
            return columnAsHeader;
        }).map((columnAsHeader) -> {
            rowHeadersLeft.remove(columnAsHeader);
            return columnAsHeader;
        }).forEach(this.rowMapping::add);

        if (rowMapping.contains(Collections.<S>emptyList())) {
            this.columnMapping.add(0, null);
        }

        // Now add the remaining rowHeaders
        this.rowMapping.addAll(rowHeadersLeft);
        rowHeadersLeft.clear();

        // iterate through all the new rows
        int row = 0;
        for (final List<S> rowHeader : rowMapping) {
            final Map<S, Double> rowTransition = Collections
                    .unmodifiableMap(new LinkedHashMap<>(
                            transitionProbabilities.get(rowHeader)));

            double rowSum = 0.0;
            for (final Entry<S, Double> stateTransition : rowTransition
                    .entrySet()) {
                final int column = this.columnMapping.indexOf(stateTransition
                        .getKey());
                matrixValues[row][column] = stateTransition.getValue();
                rowSum += matrixValues[row][column];
            }

            if (Math.abs(rowSum - 1.0) > MAXIMUM_ROW_ERROR)
                throw new IllegalArgumentException(
                        "One of the rows does not sum to 1");

            row++;
        }

        this.transitionProbabilityMatrix = new SimpleRealMatrix(matrixValues);
    }

    public SimpleMarkovChain(
            final Map<S, Map<S, Double>> transitionProbabilities,
            final Set<S> states) {
        this(packTransitions(transitionProbabilities), 1, states);
    }

    private static <S> Map<List<S>, Map<S, Double>> packTransitions(
            final Map<S, Map<S, Double>> transitions) {
        final Map<List<S>, Map<S, Double>> pack = new LinkedHashMap<>(
                transitions.size());
        transitions
                .entrySet().stream().forEach((transitionEntry) -> {
            if (transitionEntry.getKey() == null)
                pack.put(Collections.<S>emptyList(),
                        transitionEntry.getValue());
            else
                pack.put(Collections.singletonList(transitionEntry.getKey()),
                        transitionEntry.getValue());
        });
        return pack;
    }

    @Override
    public List<S> getTransitionProbabilityColumns() {
        return Collections.unmodifiableList(this.columnMapping);
    }

    @Override
    public List<List<S>> getTransitionProbabilityRows() {
        return Collections.unmodifiableList(this.rowMapping);
    }

    @Override
    public RealMatrix getTransitionProbabilityMatrix() {
        return this.transitionProbabilityMatrix;
    }

    @Override
    public int getOrder() {
        return this.order;
    }

    @Override
    public Set<S> getStates() {
        return this.states;
    }

    @Override
    public void transition(final S nextState) {
        this.history.add(nextState);
        while (this.history.size() > this.order)
            this.history.poll();
    }

    @Override
    public S generateTransition(final boolean step) {
        final List<S> currentState = this.getStateHistory();
        final int row = this.rowMapping.indexOf(currentState);

        final double randomValue = RANDOM.nextDouble();
        double probabilityOffset = 0.0;
        S nextStep = this.columnMapping.get(this.columnMapping.size() - 1);
        for (int columnIndex = 0; columnIndex < this.transitionProbabilityMatrix
                .getWidth(); columnIndex++) {
            final double transitionProbability = this.transitionProbabilityMatrix
                    .get(row, columnIndex);
            if (randomValue < transitionProbability + probabilityOffset) {
                nextStep = this.columnMapping.get(columnIndex);
                break;
            }
            probabilityOffset += transitionProbability;
        }

        if (step)
            this.transition(nextStep);
        return nextStep;
    }

    @Override
    public List<S> getStateHistory() {
        return Collections.unmodifiableList(new ArrayList<>(this.history));
    }

    @Override
    public void reset() {
        this.history.clear();
    }

    @Override
    public Map<S, Double> getProbability(final int steps) {
        final List<S> currentState = this.getStateHistory();
        if ((currentState == null) || (currentState.size() <= 0))
            throw new IllegalStateException(
                    "probability can not be calculated without at least one transition");
        RealMatrix futureMatrix = this.transitionProbabilityMatrix;
        for (int currentStep = 0; currentStep < steps - 1; currentStep++)
            futureMatrix = futureMatrix
                    .multiply(this.transitionProbabilityMatrix);
        final int row = this.rowMapping.indexOf(currentState);
        final Map<S, Double> probability = new LinkedHashMap<>();
        for (int columnIndex = 0; columnIndex < this.columnMapping.size(); columnIndex++) {
            final double transitionProbability = futureMatrix.get(row,
                    columnIndex);
            final S transitionState = this.columnMapping.get(columnIndex);
            probability.put(transitionState, transitionProbability);
        }
        return Collections.unmodifiableMap(probability);
    }

    @Override
    public Map<S, Double> getSteadyStateProbability() {
        final RealMatrix steadyStateMatrix = this.transitionProbabilityMatrix
                .subtract(
                        SimpleRealMatrix.identity(
                                this.transitionProbabilityMatrix.getHeight(),
                                this.transitionProbabilityMatrix.getWidth()))
                .flip();
        final double[][] simultaneousValues = new double[steadyStateMatrix
                .getHeight() + 1][steadyStateMatrix.getWidth()];
        for (int rowIndex = 0; rowIndex < simultaneousValues.length; rowIndex++)
            for (int columnIndex = 0; columnIndex < simultaneousValues[0].length; columnIndex++) {
                if (rowIndex >= simultaneousValues.length - 1)
                    simultaneousValues[rowIndex][columnIndex] = 1.0;
                else
                    simultaneousValues[rowIndex][columnIndex] = steadyStateMatrix
                            .get(rowIndex, columnIndex);
            }
        final RealMatrix simultaneousMatrix = new SimpleRealMatrix(
                simultaneousValues);

        final double[][] solutionValues = new double[simultaneousValues.length][1];
        solutionValues[simultaneousValues.length - 1][0] = 1.0;
        final RealMatrix solutionMatrix = new SimpleRealMatrix(solutionValues);

        final RealMatrix simultaneousSolved = simultaneousMatrix
                .solve(solutionMatrix);

        final Map<S, Double> stateProbabilities = new LinkedHashMap<>();
        for (int stateIndex = 0; stateIndex < this.columnMapping.size(); stateIndex++) {
            final S currentState = this.columnMapping.get(stateIndex);
            final double currentProbability = simultaneousSolved.get(
                    stateIndex, 0);
            stateProbabilities.put(currentState, currentProbability);
        }

        return Collections.unmodifiableMap(stateProbabilities);
    }
}
