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

import syncleus.dann.learn.ml.MLMethod;
import syncleus.dann.learn.bayesian.BayesianError;
import syncleus.dann.learn.bayesian.EncogBayesianNetwork;
import syncleus.dann.learn.bayesian.training.BayesianInit;
import syncleus.dann.learn.bayesian.training.TrainBayesian;
import syncleus.dann.learn.bayesian.training.estimator.BayesEstimator;
import syncleus.dann.learn.bayesian.training.estimator.EstimatorNone;
import syncleus.dann.learn.bayesian.training.estimator.SimpleEstimator;
import syncleus.dann.learn.bayesian.training.search.SearchNone;
import syncleus.dann.learn.bayesian.training.search.k2.BayesSearch;
import syncleus.dann.learn.bayesian.training.search.k2.SearchK2;
import syncleus.dann.util.factory.MLTrainFactory;
import syncleus.dann.util.factory.parse.ArchitectureParse;
import syncleus.dann.learn.train.MLTrain;
import syncleus.dann.learn.ml.MLDataSet;

import java.util.Map;

public class TrainBayesianFactory {
    /**
     * Create a K2 trainer.
     *
     * @param method   The method to use.
     * @param training The training data to use.
     * @param argsStr  The arguments to use.
     * @return The newly created trainer.
     */
    public MLTrain create(final MLMethod method, final MLDataSet training,
                          final String argsStr) {
        final Map<String, String> args = ArchitectureParse.parseParams(argsStr);
        final ParamsHolder holder = new ParamsHolder(args);

        final int maxParents = holder.getInt(
                MLTrainFactory.PROPERTY_MAX_PARENTS, false, 1);
        final String searchStr = holder.getString("SEARCH", false, "k2");
        final String estimatorStr = holder.getString("ESTIMATOR", false,
                "simple");
        final String initStr = holder.getString("INIT", false, "naive");

        BayesSearch search;
        BayesEstimator estimator;
        BayesianInit init;

        if (searchStr.equalsIgnoreCase("k2")) {
            search = new SearchK2();
        } else if (searchStr.equalsIgnoreCase("none")) {
            search = new SearchNone();
        } else {
            throw new BayesianError("Invalid search type: " + searchStr);
        }

        if (estimatorStr.equalsIgnoreCase("simple")) {
            estimator = new SimpleEstimator();
        } else if (estimatorStr.equalsIgnoreCase("none")) {
            estimator = new EstimatorNone();
        } else {
            throw new BayesianError("Invalid estimator type: " + estimatorStr);
        }

        if (initStr.equalsIgnoreCase("simple")) {
            init = BayesianInit.InitEmpty;
        } else if (initStr.equalsIgnoreCase("naive")) {
            init = BayesianInit.InitNaiveBayes;
        } else if (initStr.equalsIgnoreCase("none")) {
            init = BayesianInit.InitNoChange;
        } else {
            throw new BayesianError("Invalid init type: " + initStr);
        }

        return new TrainBayesian((EncogBayesianNetwork) method, training,
                maxParents, init, search, estimator);
    }
}
