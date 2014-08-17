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
package syncleus.dann.math.fitting.gaussian;

import syncleus.dann.RegressionLearning;
import syncleus.dann.data.Data;
import syncleus.dann.data.vector.VectorData;
import syncleus.dann.math.matrix.MatrixMath;
import syncleus.dann.math.matrix.RealMatrix;
import syncleus.dann.math.matrix.SimpleRealMatrix;

public class GaussianFitting implements RegressionLearning {

    private final double[] weights;
    private final int inputCount;
    private final SimpleRealMatrix sigma;
    private final SimpleRealMatrix mu;
    private SimpleRealMatrix sigmaInverse;
    private double dimFactor;
    private double normConst;

    public GaussianFitting(final int theInputCount) {
        this.mu = new SimpleRealMatrix(1, theInputCount);
        this.sigma = new SimpleRealMatrix(theInputCount, theInputCount);
        this.inputCount = theInputCount;
        this.weights = new double[theInputCount + 1];
    }

    public double[] getWeights() {
        return weights;
    }

    @Override
    public int getInputCount() {
        return this.inputCount;
    }

    @Override
    public int getOutputCount() {
        return 1;
    }

    @Override
    public Data compute(final Data input) {
        final VectorData result = new VectorData(1);

        final SimpleRealMatrix m1 = SimpleRealMatrix.createRowMatrix(input.getData());
        final SimpleRealMatrix m2 = MatrixMath.subtract(m1, this.mu);
        final SimpleRealMatrix m3 = MatrixMath.transpose(m2);
        final SimpleRealMatrix m4 = MatrixMath.multiply(sigmaInverse, m3);
        final RealMatrix m5 = MatrixMath.multiply(m4, m2);

        result.setData(0, m5.get(0, 0));

		/*
         * double d1 = x.minus(mu).transpose().times
		 * (sigmaInverse).times(x.minus(mu)).get(0,0);
		 * 
		 * double d2 = Math.exp(-0.5*d1) / normConst;
		 */

        return result;
    }

    /**
     * @return the sigma
     */
    public SimpleRealMatrix getSigma() {
        return sigma;
    }

    /**
     * @return the mu
     */
    public SimpleRealMatrix getMu() {
        return mu;
    }

    public void finalizeTraining() {
        this.sigmaInverse = this.sigma.inverse();
        this.dimFactor = Math.pow(2 * Math.PI, (this.getInputCount()) / 2.0);
        this.normConst = Math.sqrt(MatrixMath.determinant(sigma)) * dimFactor;
    }

}
