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
import junit.framework.TestCase;
import syncleus.dann.Training;
import syncleus.dann.data.vector.VectorDataset;
import syncleus.dann.neural.freeform.FreeformNetwork;
import syncleus.dann.neural.freeform.training.FreeformResilientPropagation;
import syncleus.dann.neural.util.activation.ActivationSigmoid;
import syncleus.dann.neural.util.layer.BasicLayer;

public class TestFreeform extends TestCase {
	
        public static VectorNeuralNetwork newFreeformNetwork() {
            // create a neural network, without using a factory
            VectorNeuralNetwork basicNetwork = new VectorNeuralNetwork();
            basicNetwork.addLayer(new BasicLayer(null, true, 2));
            basicNetwork.addLayer(new BasicLayer(new ActivationSigmoid(), true, 3));
            basicNetwork.addLayer(new BasicLayer(new ActivationSigmoid(), false, 1));
            basicNetwork.getStructure().finalizeStructure();
            basicNetwork.reset();
            
            return basicNetwork;

        }
    
	public void testCreation() {
                VectorNeuralNetwork basicNetwork = newFreeformNetwork();

		FreeformNetwork freeformNetwork = new FreeformNetwork(basicNetwork);
		Assert.assertEquals(basicNetwork.getInputCount(),
				freeformNetwork.getInputCount());
		Assert.assertEquals(basicNetwork.getOutputCount(),
				freeformNetwork.getOutputCount());
		Assert.assertEquals(basicNetwork.encodedArrayLength(),
				freeformNetwork.encodedArrayLength());
	}
	
	public void testEncode() {
		
		// train (and test) a network
		VectorDataset trainingData = new VectorDataset(XOR.XOR_INPUT,XOR.XOR_IDEAL);		
		FreeformNetwork trainedNetwork = NetworkUtil.createXORFreeformNetworkUntrained();
		Training bprop = new FreeformResilientPropagation(trainedNetwork, trainingData);
		NetworkUtil.testTraining(trainingData,bprop,0.01);
		
		trainedNetwork = (FreeformNetwork) bprop.getMethod();
		
		// allocate space to encode to
		double[] encoded = new double[trainedNetwork.encodedArrayLength()];
		
		// encode the network
		trainedNetwork.encodeToArray(encoded);
		
		// create untrained network
		FreeformNetwork untrainedNetwork = NetworkUtil.createXORFreeformNetworkUntrained();
		
		// copy the trained network to the untrained
		untrainedNetwork.decodeFromArray(encoded);
		
		// compare error levels
		Assert.assertEquals(trainedNetwork.calculateError(trainingData), trainedNetwork.calculateError(trainingData), 0.01);
	}
}
