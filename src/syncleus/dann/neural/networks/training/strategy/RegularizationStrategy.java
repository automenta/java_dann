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
package syncleus.dann.neural.networks.training.strategy;

import syncleus.dann.Training;
import syncleus.dann.data.VectorEncodable;
import syncleus.dann.learn.strategy.Strategy;
import syncleus.dann.math.array.EngineArray;

public class RegularizationStrategy implements Strategy {

    private final double lambda; // Weight decay
    private Training train;
    private double[] weights;
    private double[] newWeights;
    private VectorEncodable encodable;

    public RegularizationStrategy(final double lambda) {
        this.lambda = lambda;
    }

    @Override
    public void init(final Training train) {
        this.train = train;
        if (!(train.getMethod() instanceof VectorEncodable)) {
            throw new RuntimeException(
                    "Method must implement MLEncodable to be used with regularization.");
        }
        this.encodable = ((VectorEncodable) train.getMethod());
        this.weights = new double[this.encodable.encodedArrayLength()];
        this.newWeights = new double[this.encodable.encodedArrayLength()];
    }

    @Override
    public void preIteration() {
        ((VectorEncodable) train.getMethod()).encodeToArray(weights);
    }

    @Override
    public void postIteration() {

        this.encodable.encodeToArray(newWeights);

        for (int i = 0; i < newWeights.length; i++) {
            newWeights[i] -= lambda * weights[i];
        }

        this.encodable.decodeFromArray(newWeights);
        EngineArray.arrayCopy(newWeights, weights);
    }
}
