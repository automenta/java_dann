package aima.test.core.unit.learning.neural;

import org.junit.Assert;
import org.junit.Test;

import syncleus.dann.data.feature.aima.FeatureDataset;
import aima.learning.framework.DataSetFactory;
import syncleus.dann.neural.aima.BackPropLearning;
import syncleus.dann.neural.aima.FeedForwardNeuralNetwork;
import syncleus.dann.neural.aima.IrisDataSetNumerizer;
import syncleus.dann.neural.aima.IrisNNDataSet;
import syncleus.dann.neural.aima.NNConfig;
import aima.learning.neural.NNDataSet;
import syncleus.dann.neural.aima.Numerizer;
import syncleus.dann.neural.aima.Perceptron;
import aima.util.math.Matrix;
import aima.util.math.Vector;

public class BackPropagationTest {

	@Test
	public void testFeedForwardAndBAckLoopWorks() {
		// example 11.14 of Neural Network Design by Hagan, Demuth and Beale
		Matrix hiddenLayerWeightMatrix = new Matrix(2, 1);
		hiddenLayerWeightMatrix.set(0, 0, -0.27);
		hiddenLayerWeightMatrix.set(1, 0, -0.41);

		Vector hiddenLayerBiasVector = new Vector(2);
		hiddenLayerBiasVector.setValue(0, -0.48);
		hiddenLayerBiasVector.setValue(1, -0.13);

		Vector input = new Vector(1);
		input.setValue(0, 1);

		Matrix outputLayerWeightMatrix = new Matrix(1, 2);
		outputLayerWeightMatrix.set(0, 0, 0.09);
		outputLayerWeightMatrix.set(0, 1, -0.17);

		Vector outputLayerBiasVector = new Vector(1);
		outputLayerBiasVector.setValue(0, 0.48);

		Vector error = new Vector(1);
		error.setValue(0, 1.261);

		double learningRate = 0.1;
		double momentumFactor = 0.0;
		FeedForwardNeuralNetwork ffnn = new FeedForwardNeuralNetwork(
				hiddenLayerWeightMatrix, hiddenLayerBiasVector,
				outputLayerWeightMatrix, outputLayerBiasVector);
		ffnn.setTrainingScheme(new BackPropLearning(learningRate,
				momentumFactor));
		ffnn.processInput(input);
		ffnn.processError(error);

		Matrix finalHiddenLayerWeights = ffnn.getHiddenLayerWeights();
		Assert.assertEquals(-0.265, finalHiddenLayerWeights.get(0, 0), 0.001);
		Assert.assertEquals(-0.419, finalHiddenLayerWeights.get(1, 0), 0.001);

		Vector hiddenLayerBias = ffnn.getHiddenLayerBias();
		Assert.assertEquals(-0.475, hiddenLayerBias.getValue(0), 0.001);
		Assert.assertEquals(-0.1399, hiddenLayerBias.getValue(1), 0.001);

		Matrix finalOutputLayerWeights = ffnn.getOutputLayerWeights();
		Assert.assertEquals(0.171, finalOutputLayerWeights.get(0, 0), 0.001);
		Assert.assertEquals(-0.0772, finalOutputLayerWeights.get(0, 1), 0.001);

		Vector outputLayerBias = ffnn.getOutputLayerBias();
		Assert.assertEquals(0.7322, outputLayerBias.getValue(0), 0.001);
	}

	@Test
	public void testFeedForwardAndBAckLoopWorksWithMomentum() {
		// example 11.14 of Neural Network Design by Hagan, Demuth and Beale
		Matrix hiddenLayerWeightMatrix = new Matrix(2, 1);
		hiddenLayerWeightMatrix.set(0, 0, -0.27);
		hiddenLayerWeightMatrix.set(1, 0, -0.41);

		Vector hiddenLayerBiasVector = new Vector(2);
		hiddenLayerBiasVector.setValue(0, -0.48);
		hiddenLayerBiasVector.setValue(1, -0.13);

		Vector input = new Vector(1);
		input.setValue(0, 1);

		Matrix outputLayerWeightMatrix = new Matrix(1, 2);
		outputLayerWeightMatrix.set(0, 0, 0.09);
		outputLayerWeightMatrix.set(0, 1, -0.17);

		Vector outputLayerBiasVector = new Vector(1);
		outputLayerBiasVector.setValue(0, 0.48);

		Vector error = new Vector(1);
		error.setValue(0, 1.261);

		double learningRate = 0.1;
		double momentumFactor = 0.5;
		FeedForwardNeuralNetwork ffnn = new FeedForwardNeuralNetwork(
				hiddenLayerWeightMatrix, hiddenLayerBiasVector,
				outputLayerWeightMatrix, outputLayerBiasVector);

		ffnn.setTrainingScheme(new BackPropLearning(learningRate,
				momentumFactor));
		ffnn.processInput(input);
		ffnn.processError(error);

		Matrix finalHiddenLayerWeights = ffnn.getHiddenLayerWeights();
		Assert.assertEquals(-0.2675, finalHiddenLayerWeights.get(0, 0), 0.001);
		Assert.assertEquals(-0.4149, finalHiddenLayerWeights.get(1, 0), 0.001);

		Vector hiddenLayerBias = ffnn.getHiddenLayerBias();
		Assert.assertEquals(-0.4775, hiddenLayerBias.getValue(0), 0.001);
		Assert.assertEquals(-0.1349, hiddenLayerBias.getValue(1), 0.001);

		Matrix finalOutputLayerWeights = ffnn.getOutputLayerWeights();
		Assert.assertEquals(0.1304, finalOutputLayerWeights.get(0, 0), 0.001);
		Assert.assertEquals(-0.1235, finalOutputLayerWeights.get(0, 1), 0.001);

		Vector outputLayerBias = ffnn.getOutputLayerBias();
		Assert.assertEquals(0.6061, outputLayerBias.getValue(0), 0.001);
	}

	@Test
	public void testDataSetPopulation() throws Exception {
		FeatureDataset irisDataSet = DataSetFactory.getIrisDataSet();
		Numerizer numerizer = new IrisDataSetNumerizer();
		NNDataSet innds = new IrisNNDataSet();

		innds.createExamplesFromDataSet(irisDataSet, numerizer);

		NNConfig config = new NNConfig();
		config.setConfig(FeedForwardNeuralNetwork.NUMBER_OF_INPUTS, 4);
		config.setConfig(FeedForwardNeuralNetwork.NUMBER_OF_OUTPUTS, 3);
		config.setConfig(FeedForwardNeuralNetwork.NUMBER_OF_HIDDEN_NEURONS, 6);
		config.setConfig(FeedForwardNeuralNetwork.LOWER_LIMIT_WEIGHTS, -2.0);
		config.setConfig(FeedForwardNeuralNetwork.UPPER_LIMIT_WEIGHTS, 2.0);

		FeedForwardNeuralNetwork ffnn = new FeedForwardNeuralNetwork(config);
		ffnn.setTrainingScheme(new BackPropLearning(0.1, 0.9));

		ffnn.trainOn(innds, 10);

		innds.refreshDataset();
		ffnn.testOnDataSet(innds);
	}

	@Test
	public void testPerceptron() throws Exception {
		FeatureDataset irisDataSet = DataSetFactory.getIrisDataSet();
		Numerizer numerizer = new IrisDataSetNumerizer();
		NNDataSet innds = new IrisNNDataSet();

		innds.createExamplesFromDataSet(irisDataSet, numerizer);

		Perceptron perc = new Perceptron(3, 4);

		perc.trainOn(innds, 10);

		innds.refreshDataset();
		perc.testOnDataSet(innds);
	}
}
