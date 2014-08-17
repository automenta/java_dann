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
package syncleus.dann.math;


import syncleus.dann.Classifying;
import syncleus.dann.Learning;
import syncleus.dann.RegressionLearning;
import syncleus.dann.Training;
import syncleus.dann.data.DataCase;
import syncleus.dann.data.Dataset;
import syncleus.dann.data.buffer.BufferedMLDataSet;
import syncleus.dann.data.buffer.MemoryDataLoader;
import syncleus.dann.data.buffer.codec.CSVDataCODEC;
import syncleus.dann.data.buffer.codec.DataSetCODEC;
import syncleus.dann.data.file.BasicFile;
import syncleus.dann.data.file.csv.CSVFormat;
import syncleus.dann.data.file.csv.ReadCSV;
import syncleus.dann.data.specific.CSVNeuralDataSet;
import syncleus.dann.data.vector.VectorData;
import syncleus.dann.learn.MLContext;
import syncleus.dann.learn.svm.SVM;
import syncleus.dann.learn.svm.training.SVMTrain;
import syncleus.dann.math.statistics.ErrorCalculation;
import syncleus.dann.neural.activation.ActivationSigmoid;
import syncleus.dann.neural.activation.ActivationTANH;
import syncleus.dann.neural.freeform.FreeformNetwork;
import syncleus.dann.neural.freeform.training.FreeformResilientPropagation;
import syncleus.dann.neural.networks.VectorNeuralNetwork;
import syncleus.dann.neural.networks.ContainsFlat;
import syncleus.dann.neural.networks.training.propagation.Propagation;
import syncleus.dann.neural.networks.training.propagation.resilient.ResilientPropagation;
import syncleus.dann.neural.pattern.FeedForwardPattern;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import syncleus.dann.data.Data;

/**
 * General utility class for Encog. Provides for some common Encog procedures.
 * TODO merge with EncogMath and other utility methods
 */
public final class EncogUtility {

    /**
     * Convert a CSV file to a binary training file.
     *
     * @param csvFile     The CSV file.
     * @param binFile     The binary file.
     * @param inputCount  The number of input values.
     * @param outputCount The number of output values.
     * @param headers     True, if there are headers on the3 CSV.
     */
    public static void convertCSV2Binary(final File csvFile,
                                         final File binFile, final int inputCount, final int outputCount,
                                         final boolean headers) {
        binFile.delete();
        final CSVNeuralDataSet csv = new CSVNeuralDataSet(csvFile.toString(),
                inputCount, outputCount, false);
        final BufferedMLDataSet buffer = new BufferedMLDataSet(binFile);
        buffer.beginLoad(inputCount, outputCount);
        for (final DataCase pair : csv) {
            buffer.add(pair);
        }
        buffer.endLoad();
    }

    /**
     * Load CSV to memory.
     *
     * @param filename     The CSV file to load.
     * @param input        The input count.
     * @param ideal        The ideal count.
     * @param headers      True, if headers are present.
     * @param format       The loaded dataset.
     * @param significance True, if there is a significance column.
     * @return The loaded dataset.
     */
    public static Dataset loadCSV2Memory(final String filename,
                                           final int input, final int ideal, final boolean headers,
                                           final CSVFormat format, final boolean significance) {
        final DataSetCODEC codec = new CSVDataCODEC(new File(filename), format,
                headers, input, ideal, significance);
        final MemoryDataLoader load = new MemoryDataLoader(codec);
        final Dataset dataset = load.external2Memory();
        return dataset;
    }

    /**
     * Evaluate the network and display (to the console) the output for every
     * value in the training set. Displays ideal and actual.cd 
     *
     * @param network  The network to evaluate.
     * @param training The training set to evaluate.
     */
    public static <D extends Data> void evaluate(final RegressionLearning<D> network,
                                final Dataset<D> training) {
        for (final DataCase<D> pair : training) {
            final Data output = network.compute(pair.getInput());
            System.out.println("Input="
                    + EncogUtility.formatNeuralData(pair.getInput())
                    + ", Actual=" + EncogUtility.formatNeuralData(output)
                    + ", Ideal="
                    + EncogUtility.formatNeuralData(pair.getIdeal()));

        }
    }

    /**
     * Format neural data as a list of numbers.
     *
     * @param data The neural data to format.
     * @return The formatted neural data.
     */
    public static String formatNeuralData(final Data data) {
        final StringBuilder result = new StringBuilder();
        for (int i = 0; i < data.size(); i++) {
            if (i != 0) {
                result.append(',');
            }
            result.append(Format.formatDouble(data.getData(i), 4));
        }
        return result.toString();
    }

    /**
     * Create a simple feedforward neural network.
     *
     * @param input   The number of input neurons.
     * @param hidden1 The number of hidden layer 1 neurons.
     * @param hidden2 The number of hidden layer 2 neurons.
     * @param output  The number of output neurons.
     * @param tanh    True to use hyperbolic tangent activation function, false to
     *                use the sigmoid activation function.
     * @return The neural network.
     */
    public static VectorNeuralNetwork simpleFeedForward(final int input,
                                                 final int hidden1, final int hidden2, final int output,
                                                 final boolean tanh) {
        final FeedForwardPattern pattern = new FeedForwardPattern();
        pattern.setInputNeurons(input);
        pattern.setOutputNeurons(output);
        if (tanh) {
            pattern.setActivationFunction(new ActivationTANH());
        } else {
            pattern.setActivationFunction(new ActivationSigmoid());
        }

        if (hidden1 > 0) {
            pattern.addHiddenLayer(hidden1);
        }
        if (hidden2 > 0) {
            pattern.addHiddenLayer(hidden2);
        }

        final VectorNeuralNetwork network = (VectorNeuralNetwork) pattern.generate();
        network.reset();
        return network;
    }

    /**
     * Train the neural network, using SCG training, and output status to the
     * console.
     *
     * @param network     The network to train.
     * @param trainingSet The training set.
     * @param minutes     The number of minutes to train for.
     */
    public static void trainConsole(final VectorNeuralNetwork network,
                                    final Dataset trainingSet, final int minutes) {
        final Propagation train = new ResilientPropagation(network, trainingSet);
        train.setThreadCount(0);
        EncogUtility.trainConsole(train, network, trainingSet, minutes);
    }

    /**
     * Train the network, using the specified training algorithm, and send the
     * output to the console.
     *
     * @param train       The training method to use.
     * @param network     The network to train.
     * @param trainingSet The training set.
     * @param minutes     The number of minutes to train for.
     */
    public static void trainConsole(final Training train,
                                    final VectorNeuralNetwork network, final Dataset trainingSet,
                                    final int minutes) {

        long remaining;

        System.out.println("Beginning training...");
        final long start = System.currentTimeMillis();
        do {
            train.iteration();

            final long current = System.currentTimeMillis();
            final long elapsed = (current - start) / 1000;// seconds
            remaining = minutes - elapsed / 60;

            final int iteration = train.getIteration();

            System.out.println("Iteration #" + Format.formatInteger(iteration)
                    + " Error:" + Format.formatPercent(train.getError())
                    + " elapsed time = " + Format.formatTimeSpan((int) elapsed)
                    + " time left = "
                    + Format.formatTimeSpan((int) remaining * 60));

        } while (remaining > 0);
        train.finishTraining();
    }

    /**
     * Train the method, to a specific error, send the output to the console.
     *
     * @param method  The method to train.
     * @param dataSet The training set to use.
     * @param error   The error level to train to.
     */
    public static <D extends Data> void trainToError(final Learning method,
                                    final Dataset<D> dataSet, final double error) {

        Training train;

        if (method instanceof SVM) {
            train = new SVMTrain((SVM) method, dataSet);
        }
        if (method instanceof FreeformNetwork) {
            train = new FreeformResilientPropagation((FreeformNetwork) method, dataSet);
        } else {
            train = new ResilientPropagation((ContainsFlat) method, dataSet);
        }
        EncogUtility.trainToError(train, error);
    }

    /**
     * Train to a specific error, using the specified training method, send the
     * output to the console.
     *
     * @param train The training method.
     * @param error The desired error level.
     */
    public static void trainToError(final Training train, final double error) {

        int epoch = 1;

        System.out.println("Beginning training...");

        do {
            train.iteration();

            System.out.println("Iteration #" + Format.formatInteger(epoch)
                    + " Error:" + Format.formatPercent(train.getError())
                    + " Target Error: " + Format.formatPercent(error));
            epoch++;
        } while ((train.getError() > error) && !train.isTrainingDone());
        train.finishTraining();
    }

    /**
     * Private constructor.
     */
    private EncogUtility() {

    }

    public static Dataset loadEGB2Memory(final File filename) {
        final BufferedMLDataSet buffer = new BufferedMLDataSet(filename);
        final Dataset result = buffer.loadToMemory();
        buffer.close();
        return result;
    }

    /**
     * Convert a CSV file to a binary training file.
     *
     * @param csvFile     The binary file.
     * @param binFile     The binary file.
     * @param inputCount  The number of input values.
     * @param outputCount The number of output values.
     * @param headers     True, if there are headers on the CSV.
     */
    public static void convertCSV2Binary(final String csvFile,
                                         final String binFile, final int inputCount, final int outputCount,
                                         final boolean headers) {

        (new File(binFile)).delete();
        final CSVNeuralDataSet csv = new CSVNeuralDataSet(csvFile,
                inputCount, outputCount, headers);
        final BufferedMLDataSet buffer = new BufferedMLDataSet(
                new File(binFile));
        buffer.beginLoad(inputCount, outputCount);
        for (final DataCase pair : csv) {
            buffer.add(pair);
        }
        buffer.endLoad();
    }

    public static void convertCSV2Binary(final File csvFile,
                                         final CSVFormat format, final File binFile, final int[] input,
                                         final int[] ideal, final boolean headers) {

        binFile.delete();
        final ReadCSV csv = new ReadCSV(csvFile.toString(), headers, format);

        final BufferedMLDataSet buffer = new BufferedMLDataSet(binFile);
        buffer.beginLoad(input.length, ideal.length);
        while (csv.next()) {
            final VectorData inputData = new VectorData(input.length);
            final VectorData idealData = new VectorData(ideal.length);

            // handle input data
            for (int i = 0; i < input.length; i++) {
                inputData.setData(i, csv.getDouble(input[i]));
            }

            // handle input data
            for (int i = 0; i < ideal.length; i++) {
                idealData.setData(i, csv.getDouble(ideal[i]));
            }

            // add to dataset

            buffer.add(inputData, idealData);
        }
        buffer.endLoad();
    }

    public static <D extends Data> double calculateRegressionError(final RegressionLearning method, final Dataset<D> data) {

        final ErrorCalculation errorCalculation = new ErrorCalculation();
        if (method instanceof MLContext)
            ((MLContext) method).clearContext();

        for (final DataCase pair : data) {
            final Data actual = method.compute(pair.getInput());
            errorCalculation.updateError(actual.getData(), pair.getIdeal()
                    .getData(), pair.getSignificance());
        }
        return errorCalculation.calculate();
    }

    public static <D extends Data> void saveCSV(final File targetFile, final CSVFormat format,
                               final Dataset<D> set) {

        FileWriter outFile = null;
        PrintWriter out = null;

        try {
            outFile = new FileWriter(targetFile);
            out = new PrintWriter(outFile);

            for (final DataCase data : set) {
                final StringBuilder line = new StringBuilder();

                for (int i = 0; i < data.getInput().size(); i++) {
                    final double d = data.getInput().getData(i);
                    BasicFile.appendSeparator(line, format);
                    line.append(format.format(d, EncogMath.DEFAULT_PRECISION));
                }

                for (int i = 0; i < data.getIdeal().size(); i++) {
                    final double d = data.getIdeal().getData(i);
                    BasicFile.appendSeparator(line, format);
                    line.append(format.format(d, EncogMath.DEFAULT_PRECISION));
                }

                out.println(line);
            }
        } catch (final IOException ex) {
            throw new RuntimeException(ex);
        } finally {
            if (outFile != null) {
                try {
                    outFile.close();
                } catch (final IOException e) {
                    //EncogLogging.log(e);
                }
            }
            if (out != null) {
                out.close();
            }
        }
    }

    /**
     * Calculate the classification error.
     *
     * @param method The method to check.
     * @param data   The data to check.
     * @return The error.
     */
    public static <D extends Data> double calculateClassificationError(
            final Classifying<D,Integer> method, final Dataset<D> data) {
        int total = 0;
        int correct = 0;

        for (final DataCase<D> pair : data) {
            final int ideal = (int) pair.getIdeal().getData(0);
            final int actual = method.classify(pair.getInput());
            if (actual == ideal)
                correct++;
            total++;
        }
        return (total - correct) / total;
    }

    /**
     * Save a training set to an EGB file.
     *
     * @param f
     * @param data
     */
    public static void saveEGB(final File f, final Dataset data) {
        final BufferedMLDataSet binary = new BufferedMLDataSet(f);
        binary.load(data);
        data.close();
    }
}
