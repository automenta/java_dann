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
package syncleus.dann.neural.freeform.basic;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import syncleus.dann.neural.freeform.FreeformConnection;
import syncleus.dann.neural.freeform.InputSummation;
import syncleus.dann.neural.util.activation.EncogActivationFunction;

/**
 * Provides a basic implementation of an input summation. The inputs are summed
 * and applied to the activation function.
 */
public class BasicActivationSummation implements InputSummation, Serializable {

    /**
     * Serial id.
     */
    private static final long serialVersionUID = 1L;

    /**
     * The activation function to use.
     */
    private EncogActivationFunction activationFunction;

    /**
     * The inputs.
     */
    private final List<FreeformConnection> inputs = new ArrayList<>();

    /**
     * THe pre-activation summation.
     */
    private double sum;

    /**
     * Construct the activation summation.
     *
     * @param theEncogActivationFunction The activation function.
     */
    public BasicActivationSummation(
            final EncogActivationFunction theEncogActivationFunction) {
        this.activationFunction = theEncogActivationFunction;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void add(final FreeformConnection connection) {
        this.inputs.add(connection);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double calculate() {
        final double[] sumArray = new double[1];
        this.sum = 0;

        this.inputs.stream().map((connection) -> {
            connection.getSource().performCalculation();
            return connection;
        }).forEach((connection) -> this.sum += connection.getWeight()
                * connection.getSource().getActivation());

        // perform the activation function
        sumArray[0] = this.sum;
        this.activationFunction.activate(sumArray, 0, sumArray.length);

        return sumArray[0];
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public EncogActivationFunction getActivationFunction() {
        return this.activationFunction;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double getSum() {
        return this.sum;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<FreeformConnection> list() {
        return this.inputs;
    }

    /**
     * Set the activation function.
     *
     * @param activationFunction The activation function.
     */
    public void setEncogActivationFunction(
            final EncogActivationFunction activationFunction) {
        this.activationFunction = activationFunction;
    }

}
