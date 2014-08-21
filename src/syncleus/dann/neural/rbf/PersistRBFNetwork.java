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
package syncleus.dann.neural.rbf;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;
import syncleus.dann.data.file.csv.CSVFormat;
import syncleus.dann.math.rbf.RadialBasisFunction;
import syncleus.dann.neural.VectorNeuralNetwork;
import syncleus.dann.neural.util.activation.EncogActivationFunction;

/**
 * Persist a RBF network.
 */
public class PersistRBFNetwork implements EncogPersistor {

    /**
     * {@inheritDoc}
     */
    @Override
    public static final int getFileVersion() {
        return 1;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public static final String getPersistClassString() {
        return "RBFNetwork";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object read(final InputStream is) {
        final RBFNetwork result = new RBFNetwork();
        final FlatNetworkRBF flat = (FlatNetworkRBF) result.getFlat();

        final EncogReadHelper in = new EncogReadHelper(is);
        EncogFileSection section;

        while ((section = in.readNextSection()) != null) {
            if (section.getSectionName().equals("RBF-NETWORK")
                    && section.getSubSectionName().equals("PARAMS")) {
                final Map<String, String> params = section.parseParams();
                result.getProperties().putAll(params);
            }
            if (section.getSectionName().equals("RBF-NETWORK")
                    && section.getSubSectionName().equals("NETWORK")) {
                final Map<String, String> params = section.parseParams();

                flat.setBeginTraining(EncogFileSection.parseInt(params,
                        VectorNeuralNetwork.TAG_BEGIN_TRAINING));
                flat.setConnectionLimit(EncogFileSection.parseDouble(params,
                        VectorNeuralNetwork.TAG_CONNECTION_LIMIT));
                flat.setContextTargetOffset(EncogFileSection.parseIntArray(
                        params, VectorNeuralNetwork.TAG_CONTEXT_TARGET_OFFSET));
                flat.setContextTargetSize(EncogFileSection.parseIntArray(
                        params, VectorNeuralNetwork.TAG_CONTEXT_TARGET_SIZE));
                flat.setEndTraining(EncogFileSection.parseInt(params,
                        VectorNeuralNetwork.TAG_END_TRAINING));
                flat.setHasContext(EncogFileSection.parseBoolean(params,
                        VectorNeuralNetwork.TAG_HAS_CONTEXT));
                flat.setInputCount(EncogFileSection.parseInt(params,
                        PersistConst.INPUT_COUNT));
                flat.setLayerCounts(EncogFileSection.parseIntArray(params,
                        VectorNeuralNetwork.TAG_LAYER_COUNTS));
                flat.setLayerFeedCounts(EncogFileSection.parseIntArray(params,
                        VectorNeuralNetwork.TAG_LAYER_FEED_COUNTS));
                flat.setLayerContextCount(EncogFileSection.parseIntArray(
                        params, VectorNeuralNetwork.TAG_LAYER_CONTEXT_COUNT));
                flat.setLayerIndex(EncogFileSection.parseIntArray(params,
                        VectorNeuralNetwork.TAG_LAYER_INDEX));
                flat.setLayerOutput(section.parseDoubleArray(params,
                        PersistConst.OUTPUT));
                flat.setLayerSums(new double[flat.getLayerOutput().length]);
                flat.setOutputCount(EncogFileSection.parseInt(params,
                        PersistConst.OUTPUT_COUNT));
                flat.setWeightIndex(EncogFileSection.parseIntArray(params,
                        VectorNeuralNetwork.TAG_WEIGHT_INDEX));
                flat.setWeights(section.parseDoubleArray(params,
                        PersistConst.WEIGHTS));
                flat.setBiasActivation(section.parseDoubleArray(params,
                        VectorNeuralNetwork.TAG_BIAS_ACTIVATION));
            } else if (section.getSectionName().equals("RBF-NETWORK")
                    && section.getSubSectionName().equals("ACTIVATION")) {
                int index = 0;

                flat.setEncogActivationFunctions(new EncogActivationFunction[flat
                        .getLayerCounts().length]);

                for (final String line : section.getLines()) {
                    EncogActivationFunction af = null;
                    final List<String> cols = EncogFileSection
                            .splitColumns(line);
                    final String name = "org.encog.engine.network.activation."
                            + cols.get(0);
                    try {
                        final Class<?> clazz = Class.forName(name);
                        af = (EncogActivationFunction) clazz.newInstance();
                    } catch (final ClassNotFoundException | IllegalAccessException | InstantiationException e) {
                        throw new PersistError(e);
                    }

                    for (int i = 0; i < af.getParamNames().length; i++) {
                        af.setParam(i,
                                CSVFormat.EG_FORMAT.parse(cols.get(i + 1)));
                    }

                    flat.getEncogActivationFunctions()[index++] = af;
                }

            } else if (section.getSectionName().equals("RBF-NETWORK")
                    && section.getSubSectionName().equals("RBF")) {
                int index = 0;

                final int hiddenCount = flat.getLayerCounts()[1];
                final int inputCount = flat.getLayerCounts()[2];

                flat.setRBF(new RadialBasisFunction[hiddenCount]);

                for (final String line : section.getLines()) {
                    RadialBasisFunction rbf = null;
                    final List<String> cols = EncogFileSection
                            .splitColumns(line);
                    final String name = "org.encog.mathutil.rbf." + cols.get(0);
                    try {
                        final Class<?> clazz = Class.forName(name);
                        rbf = (RadialBasisFunction) clazz.newInstance();
                    } catch (final ClassNotFoundException | IllegalAccessException | InstantiationException e) {
                        throw new PersistError(e);
                    }

                    rbf.setWidth(CSVFormat.EG_FORMAT.parse(cols.get(1)));
                    rbf.setPeak(CSVFormat.EG_FORMAT.parse(cols.get(2)));
                    rbf.setCenters(new double[inputCount]);

                    for (int i = 0; i < inputCount; i++) {
                        rbf.getCenters()[i] = CSVFormat.EG_FORMAT.parse(cols
                                .get(i + 3));
                    }

                    flat.getRBF()[index++] = rbf;
                }

            }
        }

        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void save(final OutputStream os, final Object obj) {
        final EncogWriteHelper out = new EncogWriteHelper(os);
        final RBFNetwork net = (RBFNetwork) obj;
        final FlatNetworkRBF flat = (FlatNetworkRBF) net.getFlat();
        out.addSection("RBF-NETWORK");
        out.addSubSection("PARAMS");
        out.addProperties(net.getProperties());
        out.addSubSection("NETWORK");
        out.writeProperty(VectorNeuralNetwork.TAG_BEGIN_TRAINING,
                flat.getBeginTraining());
        out.writeProperty(VectorNeuralNetwork.TAG_CONNECTION_LIMIT,
                flat.getConnectionLimit());
        out.writeProperty(VectorNeuralNetwork.TAG_CONTEXT_TARGET_OFFSET,
                flat.getContextTargetOffset());
        out.writeProperty(VectorNeuralNetwork.TAG_CONTEXT_TARGET_SIZE,
                flat.getContextTargetSize());
        out.writeProperty(VectorNeuralNetwork.TAG_END_TRAINING, flat.getEndTraining());
        out.writeProperty(VectorNeuralNetwork.TAG_HAS_CONTEXT, flat.getHasContext());
        out.writeProperty(PersistConst.INPUT_COUNT, flat.getInputCount());
        out.writeProperty(VectorNeuralNetwork.TAG_LAYER_COUNTS, flat.getLayerCounts());
        out.writeProperty(VectorNeuralNetwork.TAG_LAYER_FEED_COUNTS,
                flat.getLayerFeedCounts());
        out.writeProperty(VectorNeuralNetwork.TAG_LAYER_CONTEXT_COUNT,
                flat.getLayerContextCount());
        out.writeProperty(VectorNeuralNetwork.TAG_LAYER_INDEX, flat.getLayerIndex());
        out.writeProperty(PersistConst.OUTPUT, flat.getLayerOutput());
        out.writeProperty(PersistConst.OUTPUT_COUNT, flat.getOutputCount());
        out.writeProperty(VectorNeuralNetwork.TAG_WEIGHT_INDEX, flat.getWeightIndex());
        out.writeProperty(PersistConst.WEIGHTS, flat.getWeights());
        out.writeProperty(VectorNeuralNetwork.TAG_BIAS_ACTIVATION,
                flat.getBiasActivation());
        out.addSubSection("ACTIVATION");
        for (final EncogActivationFunction af : flat.getEncogActivationFunctions()) {
            out.addColumn(af.getClass().getSimpleName());
            for (int i = 0; i < af.getParams().length; i++) {
                out.addColumn(af.getParams()[i]);
            }
            out.writeLine();
        }
        out.addSubSection("RBF");
        for (final RadialBasisFunction rbf : flat.getRBF()) {
            out.addColumn(rbf.getClass().getSimpleName());
            out.addColumn(rbf.getWidth());
            out.addColumn(rbf.getPeak());
            for (int i = 0; i < rbf.getCenters().length; i++) {
                out.addColumn(rbf.getCenters()[i]);
            }
            out.writeLine();
        }

        out.flush();
    }

}
