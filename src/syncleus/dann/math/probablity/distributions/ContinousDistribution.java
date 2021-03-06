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
package syncleus.dann.math.probablity.distributions;

import java.util.Arrays;
import java.util.Random;
import syncleus.dann.data.Data;
import syncleus.dann.data.DataCase;
import syncleus.dann.data.Dataset;
import syncleus.dann.data.vector.VectorCase;
import syncleus.dann.data.vector.VectorData;
import syncleus.dann.math.array.EngineArray;
import syncleus.dann.data.matrix.MatrixMath;
import syncleus.dann.data.matrix.RealMatrix;
import syncleus.dann.data.matrix.SimpleRealMatrix;
import syncleus.dann.data.matrix.decomposition.CholeskyDecomposition2;

/**
 * A continuous distribution represents an infinite range of choices between two
 * real numbers. A gaussian distribution is used to distribute the probability.
 */
public class ContinousDistribution<D extends Data> implements StateDistribution<D> {

    /**
     * The serial id.
     */
    private static final long serialVersionUID = 1L;

    /**
     * The dimensions.
     */
    final private int dimension;

    /**
     * The means for each dimension.
     */
    final private double[] mean;

    /**
     * The covariance matrix.
     */
    final private RealMatrix covariance;

    /**
     * The covariance left side.
     */
    private SimpleRealMatrix covarianceL = null;

    /**
     * The covariance inverse.
     */
    private SimpleRealMatrix covarianceInv = null;

    /**
     * The covariance determinant.
     */
    private double covarianceDet;

    /**
     * Random number generator.
     */
    private final static Random randomGenerator = new Random();

    /**
     * Used to perform a decomposition.
     */
    private CholeskyDecomposition2 cd;

    /**
     * Construct a continuous distribution.
     *
     * @param mean       The mean.
     * @param covariance The covariance.
     */
    public ContinousDistribution(final double[] mean,
                                 final double[][] covariance) {
        this.dimension = covariance.length;
        this.mean = EngineArray.arrayCopy(mean);
        this.covariance = new SimpleRealMatrix(covariance);
        update(covariance);
    }

    /**
     * Construct a continuous distribution with the specified number of
     * dimensions.
     *
     * @param dimension The dimensions.
     */
    public ContinousDistribution(final int dimension) {
        if (dimension <= 0) {
            throw new IllegalArgumentException();
        }

        this.dimension = dimension;
        this.mean = new double[dimension];
        this.covariance = new SimpleRealMatrix(dimension, dimension);

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ContinousDistribution clone() {
        try {
            return (ContinousDistribution) super.clone();
        } catch (final CloneNotSupportedException e) {
            throw new AssertionError(e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void fit(final Dataset<D> co) {
        final double[] weights = new double[co.size()];
        Arrays.fill(weights, 1. / co.size());

        fit(co, weights);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void fit(final Dataset<D> co, final double[] weights) {
        if ((co.size() < 1) || (co.size() != weights.length)) {
            throw new IllegalArgumentException();
        }

        // Compute mean
        final double[] mean = new double[this.dimension];
        for (int r = 0; r < this.dimension; r++) {
            int i = 0;

            for (final DataCase<D> o : co) {
                mean[r] += o.getInput().getData(r) * weights[i++];
            }
        }

        // Compute covariance
        final double[][] covariance = new double[this.dimension][this.dimension];
        int i = 0;
        for (final DataCase<D> o : co) {
            final double[] obs = o.getInput().getData();
            final double[] omm = new double[obs.length];

            for (int j = 0; j < obs.length; j++) {
                omm[j] = obs[j] - mean[j];
            }

            for (int r = 0; r < this.dimension; r++) {
                for (int c = 0; c < this.dimension; c++) {
                    covariance[r][c] += omm[r] * omm[c] * weights[i];
                }
            }

            i++;
        }

        update(covariance);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public VectorCase generate() {
        final double[] d = new double[this.dimension];

        for (int i = 0; i < this.dimension; i++) {
            d[i] = ContinousDistribution.randomGenerator.nextGaussian();
        }

        final double[] d2 = MatrixMath.multiply(this.covarianceL, d);
        return new VectorCase(new VectorData(EngineArray.add(d2,
                this.mean)));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Double probability(final DataCase<D> o) {
        final double[] v = o.getInputArray();
        final SimpleRealMatrix vmm = SimpleRealMatrix.createColumnMatrix(EngineArray.subtract(v,
                this.mean));
        final SimpleRealMatrix t = MatrixMath.multiply(this.covarianceInv, vmm);
        final double expArg = MatrixMath.multiply(MatrixMath.transpose(vmm), t)
                .get(0, 0) * -0.5;
        return Math.exp(expArg)
                / (Math.pow(2.0 * Math.PI, this.dimension / 2.0) * Math.pow(
                this.covarianceDet, 0.5));
    }

    /**
     * Update the covariance.
     *
     * @param covariance The new covariance.
     */
    public void update(final double[][] covariance) {
        this.cd = new CholeskyDecomposition2(new SimpleRealMatrix(covariance));
        this.covarianceL = this.cd.getL();
        this.covarianceInv = this.cd.inverseCholesky();
        this.covarianceDet = this.cd.getDeterminant();
    }

    /**
     * @return The mean for the dimensions of the gaussian curve.
     */
    public double[] getMean() {
        return this.mean;
    }

    /**
     * @return The covariance matrix.
     */
    public RealMatrix getCovariance() {
        return this.covariance;
    }
}
