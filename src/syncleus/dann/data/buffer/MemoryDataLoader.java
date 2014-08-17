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
package syncleus.dann.data.buffer;

import syncleus.dann.data.Data;
import syncleus.dann.data.DataCase;
import syncleus.dann.data.Dataset;
import syncleus.dann.data.buffer.codec.DataSetCODEC;
import syncleus.dann.data.vector.VectorCase;
import syncleus.dann.data.vector.VectorData;
import syncleus.dann.data.vector.VectorDataset;

/**
 * This class is used, together with a CODEC, load training data from some
 * external file into an Encog memory-based training set.
 */
public class MemoryDataLoader {
    /**
     * The CODEC to use.
     */
    private final DataSetCODEC codec;

    /**
     * Used to report the status.
     */
    private StatusReportable status;

    /**
     * The dataset to load into.
     */
    private VectorDataset result;

    /**
     * Construct a loader with the specified CODEC.
     *
     * @param theCodec The codec to use.
     */
    public MemoryDataLoader(final DataSetCODEC theCodec) {
        this.codec = theCodec;
        this.status = new NullStatusReportable();
    }

    /**
     * Convert an external file format, such as CSV, to an Encog memory training
     * set.
     *
     * @return The binary file to create.
     */
    public final Dataset external2Memory() {
        this.status.report(0, 0, "Importing to memory");

        if (this.result == null) {
            this.result = new VectorDataset();
        }

        final double[] input = new double[this.codec.getInputSize()];
        final double[] ideal = new double[this.codec.getIdealSize()];
        final double[] significance = new double[1];

        this.codec.prepareRead();

        int currentRecord = 0;
        int lastUpdate = 0;

        while (this.codec.read(input, ideal, significance)) {
            Data a = null;
            MLData b = null;

            a = new VectorData(input);

            if (this.codec.getIdealSize() > 0) {
                b = new VectorData(ideal);
            }

            final DataCase pair = new VectorCase(a, b);
            pair.setSignificance(significance[0]);
            this.result.add(pair);

            currentRecord++;
            lastUpdate++;
            if (lastUpdate >= 10000) {
                lastUpdate = 0;
                this.status.report(0, currentRecord, "Importing...");
            }
        }

        this.codec.close();
        this.status.report(0, 0, "Done importing to memory");
        return this.result;
    }

    /**
     * @return The CODEC that is being used.
     */
    public DataSetCODEC getCodec() {
        return this.codec;
    }

    /**
     * @return The resuling dataset.
     */
    public VectorDataset getResult() {
        return this.result;
    }

    /**
     * @return The object that status is reported to.
     */
    public StatusReportable getStatus() {
        return this.status;
    }

    /**
     * Set the resulting dataset.
     *
     * @param theResult The resulting dataset.
     */
    public void setResult(final VectorDataset theResult) {
        this.result = theResult;
    }

    /**
     * Set the object that status will be reported to.
     *
     * @param theStatus The object to report status to.
     */
    public void setStatus(final StatusReportable theStatus) {
        this.status = theStatus;
    }

}
