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
package syncleus.dann.neural;

import junit.framework.Assert;
import syncleus.dann.Training;
import syncleus.dann.data.vector.VectorDataset;
import syncleus.dann.learn.ErrorLearning;
import syncleus.dann.math.random.ConsistentRandomizer;
import syncleus.dann.math.random.NguyenWidrowRandomizer;
import syncleus.dann.neural.freeform.FreeformLayer;
import syncleus.dann.neural.freeform.FreeformNetwork;
import syncleus.dann.neural.util.activation.ActivationSigmoid;
import syncleus.dann.neural.util.layer.BasicLayer;

public class NetworkUtil {

    public static VectorNeuralNetwork createXORNetworkUntrained() {
		// random matrix data.  However, it provides a constant starting point 
        // for the unit tests.		
        VectorNeuralNetwork network = new VectorNeuralNetwork();
        network.addLayer(new BasicLayer(null, true, 2));
        network.addLayer(new BasicLayer(new ActivationSigmoid(), true, 4));
        network.addLayer(new BasicLayer(new ActivationSigmoid(), false, 1));
        network.getStructure().finalizeStructure();

        (new ConsistentRandomizer(-1, 1)).randomize(network);

        return network;
    }

    public static VectorNeuralNetwork createXORNetworknNguyenWidrowUntrained() {
        // random matrix data.  However, it provides a constant starting point 
        // for the unit tests.

        VectorNeuralNetwork network = new VectorNeuralNetwork();
        network.addLayer(new BasicLayer(null, true, 2));
        network.addLayer(new BasicLayer(new ActivationSigmoid(), true, 3));
        network.addLayer(new BasicLayer(new ActivationSigmoid(), false, 3));
        network.addLayer(new BasicLayer(null, false, 1));
        network.getStructure().finalizeStructure();
        (new NguyenWidrowRandomizer()).randomize(network);

        return network;
    }

    public static void testTraining(VectorDataset dataSet, Training train, double requiredImprove) {
        train.iteration();
        double error1 = train.getError();

        for (int i = 0; i < 10; i++) {
            train.iteration();
        }

        double error2 = train.getError();

        if (train.getMethod() instanceof ErrorLearning) {
            double error3 = ((ErrorLearning) train.getMethod()).calculateError(dataSet);
            double improve = (error1 - error3) / error1;
            Assert.assertTrue("Improve rate too low for " + train.getClass().getSimpleName()
                    + ",Improve=" + improve + ",Needed=" + requiredImprove, improve >= requiredImprove);
        }

        double improve = (error1 - error2) / error1;
        Assert.assertTrue("Improve rate too low for " + train.getClass().getSimpleName()
                + ",Improve=" + improve + ",Needed=" + requiredImprove, improve >= requiredImprove);
    }

    public static FreeformNetwork createXORFreeformNetworkUntrained() {
        FreeformNetwork network = new FreeformNetwork();
        FreeformLayer inputLayer = network.createInputLayer(2);
        FreeformLayer hiddenLayer1 = network.createLayer(3);
        FreeformLayer outputLayer = network.createOutputLayer(1);

        network.connectLayers(inputLayer, hiddenLayer1, new ActivationSigmoid(), 1.0, false);
        network.connectLayers(hiddenLayer1, outputLayer, new ActivationSigmoid(), 1.0, false);

        network.reset(1000);
        return network;
    }
}
