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

import syncleus.dann.data.DataCase;
import syncleus.dann.data.Dataset;
import syncleus.dann.Learning;
import syncleus.dann.data.Data;
import syncleus.dann.learn.AbstractTraining;
import syncleus.dann.math.array.EngineArray;
import syncleus.dann.neural.networks.training.propagation.TrainingContinuation;

public class TrainGaussian<D extends Data> extends AbstractTraining {

    private final GaussianFitting method;
    private final Dataset<D> training;

    public TrainGaussian(final GaussianFitting theMethod, final Dataset<D> theTraining) {
        super(TrainingImplementationType.OnePass);
        this.method = theMethod;
        this.training = theTraining;
    }

    /**
     * @return the training
     */
    @Override
    public Dataset getTraining() {
        return training;
    }

    @Override
    public void iteration() {

        // calculate mu, which is the mean
        final double[] sum = new double[this.method.getInputCount()];

        for (final DataCase pair : this.training) {
            for (int i = 0; i < this.training.getInputSize(); i++) {
                sum[i] += pair.getInput().getData(i);
            }
        }

        final double m = this.training.getRecordCount();

        for (int i = 0; i < this.training.getInputSize(); i++) {
            this.method.getMu().set(0, i, sum[i] / m);
        }

        // calculate sigma
        final double[][] sigma = this.method.getSigma().getData();
        EngineArray.fill(sigma, 0);

        final int inputCount = this.method.getInputCount();
        for (final DataCase pair : this.training) {
            for (int i = 0; i < inputCount; i++) {
                for (int j = 0; j < inputCount; j++) {
                    sigma[i][j] += (pair.getInput().getData(i) - this.method.getMu().get(0, i))
                            * (pair.getInput().getData(j) - this.method.getMu().get(0, j));
                }
            }
        }

        for (int i = 0; i < inputCount; i++) {
            for (int j = 0; j < inputCount; j++) {
                sigma[i][j] /= m;
            }
        }

        this.method.finalizeTraining();
    }

    @Override
    public boolean canContinue() {
        return false;
    }

    @Override
    public TrainingContinuation pause() {
        return null;
    }

    @Override
    public void resume(final TrainingContinuation state) {
        throw new RuntimeException("Not supported");
    }

    @Override
    public Learning getMethod() {
        return this.method;
    }

}
