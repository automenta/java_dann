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

import java.io.File;
import syncleus.dann.data.buffer.codec.DataSetCODEC;

/**
 * This class is used, together with a CODEC, to move data to/from the Encog
 * binary training file format. The same Encog binary files can be used on all
 * Encog platforms. CODEC's are used to import/export with other formats, such
 * as CSV.
 */
public class BinaryDataLoader {

    /**
     * The CODEC to use.
     */
    private final DataSetCODEC codec;

    /**
     * Used to report the status.
     */
    private StatusReportable status = new NullStatusReportable();

    /**
     * Construct a loader with the specified CODEC.
     *
     * @param theCodec The codec to use.
     */
    public BinaryDataLoader(final DataSetCODEC theCodec) {
        this.codec = theCodec;
    }

    /**
     * Convert an external file format, such as CSV, to the Encog binary
     * training format.
     *
     * @param binaryFile The binary file to create.
     */
    public void external2Binary(final File binaryFile) {

        status.report(0, 0,
                "Importing to binary file: " + binaryFile.toString());

        final EncogEGBFile egb = new EncogEGBFile(binaryFile);

        egb.create(codec.getInputSize(), codec.getIdealSize());

        final double[] input = new double[this.codec.getInputSize()];
        final double[] ideal = new double[this.codec.getIdealSize()];

        this.codec.prepareRead();

        int currentRecord = 0;
        int lastUpdate = 0;
        final double[] significance = new double[1];

        while (codec.read(input, ideal, significance)) {

            egb.write(input);
            egb.write(ideal);

            currentRecord++;
            lastUpdate++;
            if (lastUpdate >= 10000) {
                lastUpdate = 0;
                this.status.report(0, currentRecord, "Importing...");
            }

            egb.write(significance[0]);
        }

        egb.close();
        this.codec.close();
        status.report(0, 0,
                "Done importing to binary file: " + binaryFile.toString());

    }

    /**
     * Convert an Encog binary file to an external form, such as CSV.
     *
     * @param binaryFile THe binary file to use.
     */
    public void binary2External(final File binaryFile) {
        status.report(0, 0, "Exporting binary file: " + binaryFile.toString());

        final EncogEGBFile egb = new EncogEGBFile(binaryFile);
        egb.open();

        this.codec.prepareWrite(egb.getNumberOfRecords(), egb.getInputCount(),
                egb.getIdealCount());

        final int inputCount = egb.getInputCount();
        final int idealCount = egb.getIdealCount();

        final double[] input = new double[inputCount];
        final double[] ideal = new double[idealCount];

        int currentRecord = 0;
        int lastUpdate = 0;

        // now load the data
        for (int i = 0; i < egb.getNumberOfRecords(); i++) {

            for (int j = 0; j < inputCount; j++) {
                input[j] = egb.read();
            }

            for (int j = 0; j < idealCount; j++) {
                ideal[j] = egb.read();
            }

            final double significance = egb.read();

            this.codec.write(input, ideal, significance);

            currentRecord++;
            lastUpdate++;
            if (lastUpdate >= 10000) {
                lastUpdate = 0;
                this.status.report(egb.getNumberOfRecords(), currentRecord,
                        "Exporting...");
            }

        }

        egb.close();
        this.codec.close();
        status.report(0, 0,
                "Done exporting binary file: " + binaryFile.toString());
    }

    /**
     * @return The object that status is reported to.
     */
    public StatusReportable getStatus() {
        return status;
    }

    /**
     * Set the object that status is reported to.
     *
     * @param theStatus The object to report status to.
     */
    public void setStatus(final StatusReportable theStatus) {
        this.status = theStatus;
    }

    /**
     * @return The CODEC that is being used.
     */
    public DataSetCODEC getCodec() {
        return codec;
    }

}
