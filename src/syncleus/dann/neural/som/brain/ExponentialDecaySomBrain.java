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
import syncleus.dann.data.Data;
import syncleus.dann.graph.AbstractBidirectedAdjacencyGraph;
import syncleus.dann.neural.Synapse;

/**
 * A SomBrain which uses exponential decay over time for the neighborhood
 * radius, neighborhood function, and learning rate.
 * Note: if iterationsToConverge == 0, this runs continuously without reducing learningRate
 *
 * @author Jeffrey Phillips Freeman
 * @since 2.0
 */
public final class ExponentialDecaySomBrain<IN extends SomInputNeuron, ON extends SomOutputNeuron, N extends SomNeuron, S extends Synapse<N>>
        extends AbstractExponentialDecaySomBrain<IN, ON, N, S> {
    private static final long serialVersionUID = 4523396585666912034L;

    public ExponentialDecaySomBrain(final int inputCount,
                                    final int dimentionality, final int iterationsToConverge,
                                    final double initialLearningRate, final ExecutorService executor) {
        super(inputCount, dimentionality, iterationsToConverge,
                initialLearningRate, executor);
    }

    public ExponentialDecaySomBrain(final int inputCount,
                                    final int dimentionality, final int iterationsToConverge,
                                    final double initialLearningRate) {
        this(inputCount, dimentionality, iterationsToConverge,
                initialLearningRate, null);
    }

    @Override
    public AbstractBidirectedAdjacencyGraph<N, S> clone() {
        return super.clone(); //To change body of generated methods, choose Tools | Templates.
    }

    public void compute(double[] d) { 
        int size = getInputCount();
        for (int i = 0; i < size; i++) {
            setInput(i, d[i]);
        }        
    }
    public void compute(Data d) {
        if (d.size()<getInputCount())
            throw new RuntimeException(d + " has invalid data dimensions for " + this);
        compute(d.getData());
    }
}
