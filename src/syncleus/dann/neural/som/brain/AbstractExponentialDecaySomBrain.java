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
package syncleus.dann.neural.som.brain;

import java.util.concurrent.ExecutorService;
import syncleus.dann.graph.AbstractBidirectedAdjacencyGraph;
import syncleus.dann.neural.Synapse;

/**
 * 
 * Note: if iterationsToConverge == 0, this runs continuously without reducing learningRate
 * @author me
 * @param <IN>
 * @param <ON>
 * @param <N>
 * @param <S> 
 */
public abstract class AbstractExponentialDecaySomBrain<IN extends SomInputNeuron, ON extends SomOutputNeuron, N extends SomNeuron, S extends Synapse<N>>
        extends AbstractSomBrain<IN, ON, N, S> {
    private static final long serialVersionUID = 12374098245721L;
    private final int iterationsToConverge;
    private final double initialLearningRate;

    protected AbstractExponentialDecaySomBrain(final int inputCount,
                                               final int dimentionality, final int iterationsToConverge,
                                               final double initialLearningRate, final ExecutorService executor) {
        super(inputCount, dimentionality, executor);
        this.iterationsToConverge = iterationsToConverge;
        this.initialLearningRate = initialLearningRate;
    }

    protected AbstractExponentialDecaySomBrain(final int inputCount,
                                               final int dimentionality, final int iterationsToConverge,
                                               final double initialLearningRate) {
        this(inputCount, dimentionality, iterationsToConverge,
                initialLearningRate, null);
    }

    private double getIntialRadius() {
        double maxCrossSection = 0.0;
        for (int dimensionIndex = 1; dimensionIndex <= this.getUpperBounds()
                .getDimension(); dimensionIndex++) {
            final double crossSection = this.getUpperBounds().get(
                    dimensionIndex)
                    - this.getLowerBounds().get(dimensionIndex);
            if (crossSection > maxCrossSection)
                maxCrossSection = crossSection;
        }

        return maxCrossSection / 2.0;
    }

    public double getTimeConstant() {
        return (this.iterationsToConverge) / Math.log(this.getIntialRadius());
    }

    /**
     * Determines the neighborhood function based on the neurons distance from
     * the BMU.
     *
     * @param distanceFromBest The neuron's distance from the BMU.
     * @return the decay effecting the learning of the specified neuron due to
     * its distance from the BMU.
     * @since 2.0
     */
    @Override
    protected double neighborhoodFunction(final double distanceFromBest) {
        return Math.exp(-1.0 * (Math.pow(distanceFromBest, 2.0))
                / (2.0 * Math.pow(this.neighborhoodRadiusFunction(), 2.0)));
    }

    /**
     * Determine the current radius of the neighborhood which will be centered
     * around the Best Matching Unit (BMU).
     *
     * @return the current radius of the neighborhood.
     * @since 2.0
     */
    @Override
    protected double neighborhoodRadiusFunction() {
        double tc = getTimeConstant();
        if (tc == 0)
            return getIntialRadius();
        
        return this.getIntialRadius()
                * Math.exp(-1.0 * this.getIterationsTrained()
                / this.getTimeConstant());
    }

    /**
     * Determines the current learning rate for the network.
     *
     * @return the current learning rate for the network.
     * @since 2.0
     */
    @Override
    protected double learningRateFunction() {
        double tc = getTimeConstant();
        if (tc == 0)
            return initialLearningRate;
        
        return this.initialLearningRate
                * Math.exp(-1.0 * this.getIterationsTrained()
                / tc);
    }

    @Override
    public AbstractBidirectedAdjacencyGraph<N, S> clone() {
        return super.clone(); //To change body of generated methods, choose Tools | Templates.
    }
}
