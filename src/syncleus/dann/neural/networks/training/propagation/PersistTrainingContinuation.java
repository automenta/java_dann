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
package syncleus.dann.neural.networks.training.propagation;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;

/**
 * Persist the training continuation.
 */
public class PersistTrainingContinuation implements EncogPersistor {

    /**
     * {@inheritDoc}
     */
    @Override
    public static int getFileVersion() {
        return 1;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public static String getPersistClassString() {
        return "TrainingContinuation";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public static Object read(final InputStream is) {
        final TrainingContinuation result = new TrainingContinuation();
        final EncogReadHelper in = new EncogReadHelper(is);
        EncogFileSection section;

        while ((section = in.readNextSection()) != null) {
            if (section.getSectionName().equals("CONT")
                    && section.getSubSectionName().equals("PARAMS")) {
                final Map<String, String> params = section.parseParams();
                for (final Map.Entry<String, String> stringStringEntry : params.entrySet()) {
                    if (stringStringEntry.getKey().equalsIgnoreCase("type")) {
                        result.setTrainingType(stringStringEntry.getValue());
                    } else {
                        final double[] list = section.parseDoubleArray(params,
                                stringStringEntry.getKey());
                        result.put(stringStringEntry.getKey(), list);
                    }
                }
            }
        }

        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public static void save(final OutputStream os, final Object obj) {
        final EncogWriteHelper out = new EncogWriteHelper(os);
        final TrainingContinuation cont = (TrainingContinuation) obj;
        out.addSection("CONT");
        out.addSubSection("PARAMS");
        out.writeProperty("type", cont.getTrainingType());
        cont.getContents().keySet().stream().forEach((key) -> {
            final double[] list = (double[]) cont.get(key);
            out.writeProperty(key, list);
        });
        out.flush();
    }

}