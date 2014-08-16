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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import syncleus.dann.graph.AbstractBidirectedAdjacencyGraph;
import syncleus.dann.math.Vector;
import syncleus.dann.neural.AbstractLocalBrain;
import syncleus.dann.neural.InputNeuron;
import syncleus.dann.neural.SimpleSynapse;
import syncleus.dann.neural.Synapse;
import syncleus.dann.neural.som.*;
import syncleus.dann.util.UnexpectedDannError;
import syncleus.dann.util.UnexpectedInterruptedException;

import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

/**
 * A SomBrain acts as the parent class for all brains that use traditional SOM
 * algorithms. It implements a standard SOM leaving only the methods handling
 * the neighborhood and learning rate as abstract. These methods can be
 * implemented in several ways to alow for different types of SOM networks.
 *
 * @author Jeffrey Phillips Freeman
 * @since 2.0
 */
public abstract class AbstractSomBrain<IN extends SomInputNeuron, ON extends SomOutputNeuron, N extends SomNeuron, S extends Synapse<N>>
        extends AbstractLocalBrain<IN, ON, N, S> implements
        SomBrain<IN, ON, N, S> {
    private int iterationsTrained;
    private Vector upperBounds;
    private Vector lowerBounds;
    private final List<IN> inputs;
    private final Map<Vector, ON> outputs = new HashMap<>();
    private static final Logger LOGGER = LogManager
            .getLogger(AbstractSomBrain.class);

    @Override
    public AbstractBidirectedAdjacencyGraph<N, S> clone() {
        return super.clone(); //To change body of generated methods, choose Tools | Templates.
    }

    private class PropagateOutput implements Callable<Double> {
        private final ON neuron;

        public PropagateOutput(final ON neuron) {
            this.neuron = neuron;
        }

        @Override
        public Double call() {
            this.neuron.tick();
            return this.neuron.getOutput();
        }
    }

    private class TrainNeuron implements Runnable {
        private final ON neuron;
        private final Vector neuronPoint;
        private final Vector bestMatchPoint;
        private final double neighborhoodRadius;
        private final double learningRate;

        public TrainNeuron(final ON neuron, final Vector neuronPoint,
                           final Vector bestMatchPoint, final double neighborhoodRadius,
                           final double learningRate) {
            this.neuron = neuron;
            this.neuronPoint = neuronPoint;
            this.bestMatchPoint = bestMatchPoint;
            this.neighborhoodRadius = neighborhoodRadius;
            this.learningRate = learningRate;
        }

        @Override
        public void run() {
            final double currentDistance = this.neuronPoint
                    .calculateRelativeTo(this.bestMatchPoint).getDistance();
            if (currentDistance < this.neighborhoodRadius) {
                final double neighborhoodAdjustment = neighborhoodFunction(currentDistance);
                this.neuron.train(this.learningRate, neighborhoodAdjustment);
            }
        }
    }

    /**
     * Called by children classes to instantiate a basic SomBrain with the given
     * number of inputs and with an output lattice of the given number of
     * dimensions.
     *
     * @param inputCount     The number of inputs
     * @param dimentionality The number of dimensions of the output lattice
     * @since 2.0
     */
    protected AbstractSomBrain(final int inputCount, final int dimentionality) {
        this(inputCount, dimentionality, null);
    }

    /**
     * Called by children classes to instantiate a basic SomBrain with the given
     * number of inputs and with an output lattice of the given number of
     * dimensions.
     *
     * @param inputCount     The number of inputs
     * @param dimentionality The number of dimensions of the output lattice
     * @param executor       ThreadPoolExecutor to use when executing parallel
     *                       functionality.
     * @since 2.0
     */
    protected AbstractSomBrain(final int inputCount, final int dimentionality,
                               final ExecutorService executor) {
        super(executor);

        if (inputCount <= 0)
            throw new IllegalArgumentException(
                    "input count must be greater than 0");
        if (dimentionality <= 0)
            throw new IllegalArgumentException(
                    "dimentionality must be greater than 0");

        this.upperBounds = new Vector(dimentionality);
        this.lowerBounds = new Vector(dimentionality);
        final List<IN> newInputs = new ArrayList<>();
        for (int inputIndex = 0; inputIndex < inputCount; inputIndex++) {
            // TODO fix typing
            final SomInputNeuron safeNewNeuron = new SimpleSomInputNeuron(this);
            final IN newNeuron = (IN) safeNewNeuron;
            newInputs.add(newNeuron);
            // TODO fix typing
            super.add((N) newNeuron);
        }
        this.inputs = Collections.unmodifiableList(newInputs);
    }

    private void updateBounds(final Vector position) {
        // make sure we have the proper dimentionality
        if (position.getDimension() != this.upperBounds.getDimension())
            throw new IllegalArgumentException("Dimentionality mismatch");

        for (int dimensionIndex = 1; dimensionIndex <= position.getDimension(); dimensionIndex++) {
            if (this.upperBounds.get(dimensionIndex) < position
                    .get(dimensionIndex))
                this.upperBounds = this.upperBounds.clone(
                        position.get(dimensionIndex), dimensionIndex);
            if (this.lowerBounds.get(dimensionIndex) > position
                    .get(dimensionIndex))
                this.lowerBounds = this.lowerBounds.clone(
                        position.get(dimensionIndex), dimensionIndex);
        }
    }

    /**
     * Creates a new point in the output lattice at the given position. This
     * will automatically have all inputs connected to it.
     *
     * @param position The position of the new output in the lattice.
     * @since 2.0
     */
    @Override
    public void createOutput(final Vector position) {
        // make sure we have the proper dimentionality
        if (position.getDimension() != this.upperBounds.getDimension())
            throw new IllegalArgumentException("Dimentionality mismatch");

        // increase the upper bounds if needed
        this.updateBounds(position);

        // create and add the new output neuron
        final SimpleSomNeuron outputNeuron = new SimpleSomNeuron(this);
        // TODO fix typing
        this.outputs.put(position, (ON) outputNeuron);
        // TODO fix typing
        this.add((N) outputNeuron);

        this.inputs.stream().map((input) -> new SimpleSynapse<>((N) input,
                (N) outputNeuron)).forEach((synapse) -> this.connect((S) synapse, true));
    }

    /**
     * Gets the positions of all the outputs in the output lattice.
     *
     * @return the positions of all the outputs in the output lattice.
     * @since 2.0
     */
    @Override
    public final Set<Vector> getPositions() {
        final Set<Vector> positions = new HashSet<>();
        this.outputs.keySet().stream().forEach((position) -> positions.add(new Vector(position)));
        return Collections.unmodifiableSet(positions);
    }

    /**
     * Gets the current output at the specified position in the output lattice
     * if the position does not have a SimpleSomNeuron associated with it then
     * it throws an exception.
     *
     * @param position position in the output lattice of the output you wish to
     *                 retrieve.
     * @return The value of the specified SimpleSomNeuron, or null if there is
     * no SimpleSomNeuron associated with the given position.
     * @throws IllegalArgumentException if position does not exist.
     * @since 2.0
     */
    @Override
    public final double getOutput(final Vector position) {
        final ON outputNeuron = this.outputs.get(position);
        if (outputNeuron == null)
            throw new IllegalArgumentException("position does not exist");

        outputNeuron.tick();
        return outputNeuron.getOutput();
    }

    /**
     * Obtains the BMU (Best Matching Unit) for the current input set. This will
     * also train against the current input.
     *
     * @return the BMU for the current input set.
     */
    @Override
    public final Vector getBestMatchingUnit() {
        return getBestMatchingUnit(true);
    }

    /**
     * Obtains the BMU (Best Matching Unit) for the current input set. This will
     * also train against the current input when specified.
     *
     * @param train true to train against the input set, false if no training
     *              occurs.
     * @return the BMU for the current input set.
     * @since 2.0
     */
    @Override
    public final Vector getBestMatchingUnit(final boolean train) {
        // make sure we have at least one output
        if (outputs.size() <= 0)
            throw new IllegalStateException("Must have at least one output");

        Vector bestMatchingUnit = null;
        double bestMatch = Double.POSITIVE_INFINITY;
        if (this.getThreadExecutor() == null) {
            for (final Entry<Vector, ON> entry : this.outputs.entrySet()) {
                final ON neuron = entry.getValue();
                neuron.tick();
                final double output = neuron.getOutput();

                if (bestMatchingUnit == null)
                    bestMatchingUnit = entry.getKey();
                else if (output < bestMatch) {
                    bestMatchingUnit = entry.getKey();
                    bestMatch = output;
                }
            }
        } else {
            // stick all the neurons in the queue to propagate
            final Map<Vector, Future<Double>> futureOutput = new HashMap<>();
            this.outputs.entrySet().stream().forEach((entry) -> {
                final PropagateOutput callable = new PropagateOutput(
                        entry.getValue());
                futureOutput.put(entry.getKey(), this.getThreadExecutor()
                        .submit(callable));
            });

            // find the best matching unit
            for (final Entry<Vector, ON> entry : this.outputs.entrySet()) {
                final double output;
                try {
                    output = futureOutput.get(entry.getKey()).get();
                } catch (final InterruptedException caught) {
                    LOGGER.warn("PropagateOutput was unexpectedly interrupted",
                            caught);
                    throw new UnexpectedInterruptedException(
                            "Unexpected interrupted. Get should block indefinitely",
                            caught);
                } catch (final ExecutionException caught) {
                    LOGGER.error(
                            "PropagateOutput was had an unexpected problem executing.",
                            caught);
                    throw new UnexpectedDannError(
                            "Unexpected execution exception. Get should block indefinitely",
                            caught);
                }

                if (bestMatchingUnit == null)
                    bestMatchingUnit = entry.getKey();
                else if (output < bestMatch) {
                    bestMatchingUnit = entry.getKey();
                    bestMatch = output;
                }
            }
        }

        if (train)
            this.train(bestMatchingUnit);

        return bestMatchingUnit;
    }

    private void train(final Vector bestMatchingUnit) {
        final double neighborhoodRadius = this.neighborhoodRadiusFunction();
        final double learningRate = this.learningRateFunction();

        if (this.getThreadExecutor() == null) {
            this.outputs.entrySet().stream().map((entry) -> new TrainNeuron(entry.getValue(),
                    entry.getKey(), bestMatchingUnit, neighborhoodRadius,
                    learningRate)).forEach(TrainNeuron::run);
        } else {
            // add all the neuron training events to the thread queue
            final ArrayList<Future> futures = new ArrayList<>();
            this.outputs.entrySet().stream().map((entry) -> new TrainNeuron(entry.getValue(),
                    entry.getKey(), bestMatchingUnit, neighborhoodRadius,
                    learningRate)).forEach((runnable) -> futures.add(this.getThreadExecutor().submit(runnable)));

            // wait until all neurons are trained
            try {
                for (final Future future : futures)
                    future.get();
            } catch (final InterruptedException caught) {
                LOGGER.warn("PropagateOutput was unexpectedly interrupted",
                        caught);
                throw new UnexpectedInterruptedException(
                        "Unexpected interrupted. Get should block indefinitely",
                        caught);
            } catch (final ExecutionException caught) {
                LOGGER.error(
                        "PropagateOutput was had an unexpected problem executing.",
                        caught);
                throw new UnexpectedDannError(
                        "Unexpected execution exception. Get should block indefinitely",
                        caught);
            }
        }

        this.iterationsTrained++;
    }

    /**
     * The number of iterations trained so far.
     *
     * @return the iterationsTrained so far.
     * @since 2.0
     */
    @Override
    public final int getIterationsTrained() {
        return this.iterationsTrained;
    }

    /**
     * The upper bounds of the positions of the output neurons.
     *
     * @return the upperBounds
     * @since 2.0
     */
    protected final Vector getUpperBounds() {
        return this.upperBounds;
    }

    /**
     * The lower bounds of the positions of the output neurons.
     *
     * @return the lowerBounds
     * @since 2.0
     */
    protected final Vector getLowerBounds() {
        return this.lowerBounds;
    }

    /**
     * Gets the number of inputs.
     *
     * @return The number of inputs.
     * @since 2.0
     */
    @Override
    public final int getInputCount() {
        return this.inputs.size();
    }

    /**
     * Sets the current input.
     *
     * @since 2.0
     */
    @Override
    public final void setInput(final int inputIndex, final double inputValue) {
        if (inputIndex >= this.getInputCount())
            throw new IllegalArgumentException("inputIndex is out of bounds");

        final InputNeuron currentInput = this.inputs.get(inputIndex);
        currentInput.setInput(inputValue);
        currentInput.tick();
    }

    /**
     * Gets the current input value at the specified index.
     *
     * @param index Index of the input to get.
     * @return The current value for the specified input.
     * @since 2.0
     */
    @Override
    public final double getInput(final int index) {
        return this.inputs.get(index).getInput();
    }

    /**
     * Obtains the weight vectors of the outputs.
     *
     * @return the weight vectors of each output in the output lattice
     * @since 2.0
     */
    @Override
    public final Map<Vector, double[]> getOutputWeightVectors() {
        // iterate through the output lattice
        final HashMap<Vector, double[]> weightVectors = new HashMap<>();
        this.outputs.entrySet().stream().forEach((output) -> {
            final double[] weightVector = new double[this.inputs.size()];
            final ON currentNeuron = output.getValue();
            final Vector currentPoint = output.getKey();
            this.getInEdges((N) currentNeuron).stream().map((source) -> {
                assert (source.getSourceNode() instanceof InputNeuron);
                return source;
            }).forEach((source) -> {
                final int sourceIndex = this.inputs.indexOf(source
                        .getSourceNode());
                weightVector[sourceIndex] = source.getWeight();
            });
            weightVectors.put(currentPoint, weightVector);
        });
        return Collections.unmodifiableMap(weightVectors);
    }

    /**
     * Determines the neighborhood function based on the neurons distance from
     * the Best Matching Unit (BMU).
     *
     * @param distanceFromBest The neuron's distance from the BMU.
     * @return the decay effecting the learning of the specified neuron due to
     * its distance from the BMU.
     * @since 2.0
     */
    protected abstract double neighborhoodFunction(double distanceFromBest);

    /**
     * Determine the current radius of the neighborhood which will be centered
     * around the Best Matching Unit (BMU).
     *
     * @return the current radius of the neighborhood.
     * @since 2.0
     */
    protected abstract double neighborhoodRadiusFunction();

    /**
     * Determines the current learning rate for the network.
     *
     * @return the current learning rate for the network.
     * @since 2.0
     */
    protected abstract double learningRateFunction();
}
