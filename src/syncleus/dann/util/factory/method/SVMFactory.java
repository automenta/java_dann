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
package syncleus.dann.util.factory.method;

import java.util.List;
import syncleus.dann.Learning;
import syncleus.dann.neural.svm.KernelType;
import syncleus.dann.neural.svm.SVM;
import syncleus.dann.neural.svm.SVMType;
import syncleus.dann.util.factory.parse.ArchitectureLayer;
import syncleus.dann.util.factory.parse.ArchitectureParse;

/**
 * A factory that is used to create support vector machines (SVM).
 */
public class SVMFactory {

    /**
     * The max layer count.
     */
    public static final int MAX_LAYERS = 3;

    /**
     * Create the SVM.
     *
     * @param architecture The architecture string.
     * @param input        The input count.
     * @param output       The output count.
     * @return The newly created SVM.
     */
    public static Learning create(final String architecture, final int input,
                                  final int output) {

        final List<String> layers = ArchitectureParse.parseLayers(architecture);
        if (layers.size() != MAX_LAYERS) {
            throw new RuntimeException(
                    "SVM's must have exactly three elements, separated by ->.");
        }

        final ArchitectureLayer inputLayer = ArchitectureParse.parseLayer(
                layers.get(0), input);
        final ArchitectureLayer paramsLayer = ArchitectureParse.parseLayer(
                layers.get(1), input);
        final ArchitectureLayer outputLayer = ArchitectureParse.parseLayer(
                layers.get(2), output);

        final String name = paramsLayer.getName();
        final String kernelStr = paramsLayer.getParams().get("KERNEL");
        final String svmTypeStr = paramsLayer.getParams().get("TYPE");

        SVMType svmType = SVMType.NewSupportVectorClassification;
        KernelType kernelType = KernelType.RadialBasisFunction;

        boolean useNew = true;

        if (svmTypeStr == null) {
            useNew = true;
        } else if (svmTypeStr.equalsIgnoreCase("NEW")) {
            useNew = true;
        } else if (svmTypeStr.equalsIgnoreCase("OLD")) {
            useNew = false;
        } else {
            throw new RuntimeException("Unsupported type: " + svmTypeStr
                    + ", must be NEW or OLD.");
        }

        if (name.equalsIgnoreCase("C")) {
            if (useNew) {
                svmType = SVMType.NewSupportVectorClassification;
            } else {
                svmType = SVMType.SupportVectorClassification;
            }
        } else if (name.equalsIgnoreCase("R")) {
            if (useNew) {
                svmType = SVMType.NewSupportVectorRegression;
            } else {
                svmType = SVMType.EpsilonSupportVectorRegression;
            }
        } else {
            throw new RuntimeException("Unsupported mode: " + name
                    + ", must be C for classify or R for regression.");
        }

        if (kernelStr == null) {
            kernelType = KernelType.RadialBasisFunction;
        } else if ("linear".equalsIgnoreCase(kernelStr)) {
            kernelType = KernelType.Linear;
        } else if ("poly".equalsIgnoreCase(kernelStr)) {
            kernelType = KernelType.Poly;
        } else if ("precomputed".equalsIgnoreCase(kernelStr)) {
            kernelType = KernelType.Precomputed;
        } else if ("rbf".equalsIgnoreCase(kernelStr)) {
            kernelType = KernelType.RadialBasisFunction;
        } else if ("sigmoid".equalsIgnoreCase(kernelStr)) {
            kernelType = KernelType.Sigmoid;
        } else {
            throw new RuntimeException("Unsupported kernel: " + kernelStr
                    + ", must be linear,poly,precomputed,rbf or sigmoid.");
        }

        final int inputCount = inputLayer.getCount();
        final int outputCount = outputLayer.getCount();

        if (outputCount != 1) {
            throw new RuntimeException("SVM can only have an output size of 1.");
        }

        final SVM result = new SVM(inputCount, svmType, kernelType);

        return result;
    }
}
