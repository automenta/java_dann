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

import syncleus.dann.learn.ml.MLMethod;
import syncleus.dann.learn.ml.TrainingImplementationType;
import syncleus.dann.learn.train.BasicTraining;
import org.encog.neural.networks.training.propagation.TrainingContinuation;
import syncleus.dann.learn.ml.MLDataPair;
import syncleus.dann.learn.ml.MLDataSet;
import syncleus.dann.math.array.EngineArray;

public class TrainGaussian extends BasicTraining {

    private final GaussianFitting method;
    private final MLDataSet training;

    public TrainGaussian(final GaussianFitting theMethod,
                         final MLDataSet theTraining) {
        super(TrainingImplementationType.OnePass);
        this.method = theMethod;
        this.training = theTraining;
    }

    /**
     * @return the training
     */
    @Override
    public MLDataSet getTraining() {
        return training;
    }

    @Override
    public void iteration() {

        // calculate mu, which is the mean
        final double[] sum = new double[this.method.getInputCount()];

        for (final MLDataPair pair : this.training) {
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
        for (final MLDataPair pair : this.training) {
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
    public MLMethod getMethod() {
        return this.method;
    }

}
