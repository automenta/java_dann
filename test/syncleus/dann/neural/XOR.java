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

import junit.framework.TestCase;
import syncleus.dann.RegressionLearning;
import syncleus.dann.data.Data;
import syncleus.dann.data.DataCase;
import syncleus.dann.data.vector.VectorCase;
import syncleus.dann.data.vector.VectorData;
import syncleus.dann.data.vector.VectorDataset;
import syncleus.dann.math.EncogUtility;
import syncleus.dann.math.random.RangeRandomizer;
import syncleus.dann.neural.freeform.FreeformNetwork;
import syncleus.dann.neural.util.layer.BasicLayer;
import syncleus.dann.neural.util.structure.NetworkCODEC;



public class XOR {

		public static double XOR_INPUT[][] = { { 0.0, 0.0 }, { 1.0, 0.0 }, { 0.0, 1.0 },
				{ 1.0, 1.0 } };

		public static double XOR_IDEAL[][] = { { 0.0 }, { 1.0 }, { 1.0 }, { 0.0 } };
		
		public static double XOR_IDEAL2[][] = { { 1.0, 0.0 }, { 0.0,1.0 }, { 1.0,0.0 }, { 0.0,1.0 } };
		
		public static boolean verifyXOR(RegressionLearning network,double tolerance) 
		{
			for(int trainingSet=0;trainingSet<XOR.XOR_IDEAL.length;trainingSet++)
			{
				Data actual = network.compute(new VectorData(XOR.XOR_INPUT[trainingSet]));
				
				for(int i=0;i<XOR.XOR_IDEAL[0].length;i++)
				{
					double diff = Math.abs(actual.getData(i)-XOR.XOR_IDEAL[trainingSet][i]);
					if( diff>tolerance )
						return false;
				}
				
			}
			
			return true;
		}
		
		public static VectorDataset createXORDataSet()
		{
			return new VectorDataset(XOR.XOR_INPUT,XOR.XOR_IDEAL);
		}
		
		public static void testXORDataSet(VectorDataset set)
		{
			int row = 0;
			for(DataCase<VectorData> item: set)
			{
				for(int i=0;i<XOR.XOR_INPUT[0].length;i++)
				{
					TestCase.assertEquals(item.getInput().getData(i), 
							XOR.XOR_INPUT[row][i]);
				}
				
				for(int i=0;i<XOR.XOR_IDEAL[0].length;i++)
				{
					TestCase.assertEquals(item.getIdeal().getData(i), 
							XOR.XOR_IDEAL[row][i]);
				}
				
				row++;
			}
		}
		
		public static VectorNeuralNetwork createTrainedXOR()
		{
			double[] TRAINED_XOR_WEIGHTS = { 25.427193285452972,-26.92000502099534,20.76598054603445,-12.921266548020219,-0.9223427050161919,-1.0588373209475093,-3.80109620509867,3.1764938777876837,80.98981535707951,-75.5552829139118,37.089976176012634,74.85166823997326,75.20561368661059,-37.18307123471437,-21.044949631177417,43.81815044327334,9.648991753485689 };
			VectorNeuralNetwork network = EncogUtility.simpleFeedForward(2, 4, 0, 1, false);
			NetworkCODEC.arrayToNetwork(TRAINED_XOR_WEIGHTS, network);
			return network;
		}
		
		public static VectorNeuralNetwork createUnTrainedXOR()
		{
			double[] TRAINED_XOR_WEIGHTS = { -0.427193285452972,0.92000502099534,-0.76598054603445,-0.921266548020219,-0.9223427050161919,-0.0588373209475093,-0.80109620509867,3.1764938777876837,0.98981535707951,-0.5552829139118,0.089976176012634,0.85166823997326,0.20561368661059,0.18307123471437,0.044949631177417,0.81815044327334,0.648991753485689 };
			VectorNeuralNetwork network = EncogUtility.simpleFeedForward(2, 4, 0, 1, false);
			NetworkCODEC.arrayToNetwork(TRAINED_XOR_WEIGHTS, network);
			return network;
		}
		
		public static VectorNeuralNetwork createThreeLayerNet()
		{
			VectorNeuralNetwork network = new VectorNeuralNetwork();
			network.addLayer(new BasicLayer(2));
			network.addLayer(new BasicLayer(3));
			network.addLayer(new BasicLayer(1));
			network.getStructure().finalizeStructure();
			network.reset();
			return network;
		}

		public static VectorDataset createNoisyXORDataSet(int count) {
			VectorDataset result = new VectorDataset();
			for(int i=0;i<count;i++) {
				for(int j=0;j<4;j++) {
					VectorData inputData = new VectorData(XOR_INPUT[j]);
					VectorData idealData = new VectorData(XOR_IDEAL[j]);
					VectorCase pair = new VectorCase(inputData,idealData);
					inputData.setData(0, inputData.getData(0)+RangeRandomizer.randomize(-0.1, 0.1));
					inputData.setData(1, inputData.getData(1)+RangeRandomizer.randomize(-0.1, 0.1));
					result.add(pair);
				}
			}
			return result;
		}

		public static FreeformNetwork createTrainedFreeformXOR() {
			VectorNeuralNetwork network = createTrainedXOR();
			return new FreeformNetwork(network);
		}
}
