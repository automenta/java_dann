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
package syncleus.dann.math.fitting.linear;

import syncleus.dann.Learning;
import syncleus.dann.data.MutableData;
import syncleus.dann.data.DataCase;
import syncleus.dann.data.Dataset;
import syncleus.dann.learn.AbstractTraining;
import syncleus.dann.math.EncogUtility;
import syncleus.dann.neural.networks.training.propagation.TrainingContinuation;

public class TrainLinearRegression<D extends MutableData> extends AbstractTraining {

    private final LinearRegression method;
    private final Dataset<D> training;

    public TrainLinearRegression(final LinearRegression theMethod,
                                 final Dataset<D> theTraining) {
        super(
                theMethod.getInputCount() == 1 ? TrainingImplementationType.OnePass
                        : TrainingImplementationType.Iterative);
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
        final int m = (int) this.training.getRecordCount();
        double sumX = 0;
        double sumY = 0;
        double sumXY = 0;
        double sumX2 = 0;

        for (final DataCase pair : this.training) {
            sumX += pair.getInputArray()[0];
            sumY += pair.getIdealArray()[0];
            sumX2 += Math.pow(pair.getInputArray()[0], 2);
            sumXY += pair.getInputArray()[0] * pair.getIdealArray()[0];
        }

        this.method.getWeights()[1] = ((m * sumXY) - (sumX * sumY))
                / ((m * sumX2) - Math.pow(sumX, 2));
        this.method.getWeights()[0] = ((1.0 / m) * sumY)
                - ((this.method.getWeights()[1] / m) * sumX);

        this.setError(EncogUtility.calculateRegressionError(this.method,
                this.training));
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
