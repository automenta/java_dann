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
package syncleus.dann.util.factory.train;

import java.util.Map;
import syncleus.dann.Learning;
import syncleus.dann.Training;
import syncleus.dann.data.Dataset;
import syncleus.dann.neural.util.ContainsFlat;
import syncleus.dann.util.factory.MLTrainFactory;
import syncleus.dann.util.factory.parse.ArchitectureParse;

/**
 * A factory that creates RPROP trainers.
 */
public class RPROPFactory {

    /**
     * Create a RPROP trainer.
     *
     * @param method   The method to use.
     * @param training The training data to use.
     * @param argsStr  The arguments to use.
     * @return The newly created trainer.
     */
    public Training create(final Learning method, final Dataset training,
                          final String argsStr) {

        if (!(method instanceof ContainsFlat)) {
            throw new RuntimeException(
                    "RPROP training cannot be used on a method of type: "
                            + method.getClass().getName());
        }

        final Map<String, String> args = ArchitectureParse.parseParams(argsStr);
        final ParamsHolder holder = new ParamsHolder(args);
        final double initialUpdate = holder.getDouble(
                MLTrainFactory.PROPERTY_INITIAL_UPDATE, false,
                RPROPConst.DEFAULT_INITIAL_UPDATE);
        final double maxStep = holder.getDouble(
                MLTrainFactory.PROPERTY_MAX_STEP, false,
                RPROPConst.DEFAULT_MAX_STEP);

        return new ResilientPropagation((ContainsFlat) method, training,
                initialUpdate, maxStep);
    }
}
