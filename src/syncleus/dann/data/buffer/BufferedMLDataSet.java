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
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import syncleus.dann.data.DataCase;
import syncleus.dann.data.DataException;
import syncleus.dann.data.Dataset;
import syncleus.dann.data.MutableData;
import syncleus.dann.data.vector.VectorCase;
import syncleus.dann.data.vector.VectorDataset;

/**
 * This class is not memory based, so very long files can be used, without
 * running out of memory. This dataset uses a Encog binary training file as a
 * buffer.
 * <p/>
 * When used with a slower access dataset, such as CSV, XML or SQL, where
 * parsing must occur, this dataset can be used to load from the slower dataset
 * and train at much higher speeds.
 * <p/>
 * This class makes use of Java file channels for maximum file access
 * performance.
 * <p/>
 * If you are going to create a binary file, by using the add methods, you must
 * call beginLoad to cause Encog to open an output file. Once the data has been
 * loaded, call endLoad. You can also use the BinaryDataLoader class, with a
 * CODEC, to load many other popular external formats.
 * <p/>
 * The binary files produced by this class are in the Encog binary training
 * format, and can be used with any Encog platform. Encog binary files are
 * stored using "little endian" numbers.
 */
public class BufferedMLDataSet implements Dataset, Serializable {

    /**
     * The version.
     */
    private static final long serialVersionUID = 2577778772598513566L;

    /**
     * Error message for ADD.
     */
    public static final String ERROR_ADD = "Add can only be used after calling beginLoad.";

    /**
     * Error message for REMOVE.
     */
    public static final String ERROR_REMOVE = "Remove is not supported for BufferedNeuralDataSet.";

    /**
     * True, if we are in the process of loading.
     */
    private transient boolean loading;

    /**
     * The file being used.
     */
    private final File file;

    /**
     * The EGB file we are working wtih.
     */
    private transient EncogEGBFile egb;

    /**
     * Additional sets that were opened.
     */
    private final transient List<BufferedMLDataSet> additional = new ArrayList<>();

    /**
     * The owner.
     */
    private transient BufferedMLDataSet owner;

    /**
     * Construct the dataset using the specified binary file.
     *
     * @param binaryFile The file to use.
     */
    public BufferedMLDataSet(final File binaryFile) {
        this.file = binaryFile;
        this.egb = new EncogEGBFile(binaryFile);
        if (file.exists()) {
            this.egb.open();
        }
    }

    /**
     * Open the binary file for reading.
     */
    public void open() {
        this.egb.open();
    }

    /**
     * @return An iterator.
     */
    @Override
    public Iterator<DataCase> iterator() {
        return new BufferedDataSetIterator(this);
    }

    /**
     * @return The record count.
     */
    @Override
    public long getRecordCount() {
        if (this.egb == null) {
            return 0;
        } else {
            return this.egb.getNumberOfRecords();
        }
    }

    /**
     * Read an individual record.
     *
     * @param index The zero-based index. Specify 0 for the first record, 1 for
     *              the second, and so on.
     * @param pair  THe data to read.
     */
    @Override
    public void getRecord(final long index, final DataCase pair) {
        synchronized (this) {
            this.egb.setLocation((int) index);
            final double[] inputTarget = pair.getInputArray();
            this.egb.read(inputTarget);

            if (pair.getIdealArray() != null) {
                final double[] idealTarget = pair.getIdealArray();
                this.egb.read(idealTarget);
            }

            this.egb.read();
        }
    }

    /**
     * @return An additional training set.
     */
    @Override
    public BufferedMLDataSet openAdditional() {
        final BufferedMLDataSet result = new BufferedMLDataSet(this.file);
        result.setOwner(this);
        this.additional.add(result);
        return result;
    }

    /**
     * Add only input data, for an unsupervised dataset.
     *
     * @param data1 The data to be added.
     */
    @Override
    public void add(final MutableData data1) {
        if (!this.loading) {
            throw new DataException(BufferedMLDataSet.ERROR_ADD);
        }

        egb.write(data1.getData());
        egb.write(1.0);
    }

    /**
     * Add both the input and ideal data.
     *
     * @param inputData The input data.
     * @param idealData The ideal data.
     */
    @Override
    public void add(final MutableData inputData, final MutableData idealData) {

        if (!this.loading) {
            throw new DataException(BufferedMLDataSet.ERROR_ADD);
        }

        this.egb.write(inputData.getData());
        this.egb.write(idealData.getData());
        this.egb.write(1.0);
    }

    /**
     * Add a data pair of both input and ideal data.
     *
     * @param pair The pair to add.
     */
    @Override
    public void add(final DataCase pair) {
        if (!this.loading) {
            throw new DataException(BufferedMLDataSet.ERROR_ADD);
        }

        this.egb.write(pair.getInputArray());
        this.egb.write(pair.getIdealArray());
        this.egb.write(pair.getSignificance());

    }

    /**
     * Close the dataset.
     */
    @Override
    public void close() {

        final Object[] obj = this.additional.toArray();

        for (int i = 0; i < obj.length; i++) {
            final BufferedMLDataSet set = (BufferedMLDataSet) obj[i];
            set.close();
        }

        this.additional.clear();

        if (this.owner != null) {
            this.owner.removeAdditional(this);
        }

        this.egb.close();
        this.egb = null;
    }

    /**
     * @return The ideal data size.
     */
    @Override
    public int getIdealSize() {
        if (this.egb == null) {
            return 0;
        } else {
            return this.egb.getIdealCount();
        }
    }

    /**
     * @return The input data size.
     */
    @Override
    public int getInputSize() {
        if (this.egb == null) {
            return 0;
        } else {
            return this.egb.getInputCount();
        }
    }

    /**
     * @return True if this dataset is supervised.
     */
    @Override
    public boolean isSupervised() {
        if (this.egb == null) {
            return false;
        } else {
            return this.egb.getIdealCount() > 0;
        }
    }

    /**
     * @return If this dataset was created by openAdditional, the set that
     * created this object is the owner. Return the owner.
     */
    public BufferedMLDataSet getOwner() {
        return owner;
    }

    /**
     * Set the owner of this dataset.
     *
     * @param theOwner The owner.
     */
    public void setOwner(final BufferedMLDataSet theOwner) {
        this.owner = theOwner;
    }

    /**
     * Remove an additional dataset that was created.
     *
     * @param child The additional dataset to remove.
     */
    public void removeAdditional(final BufferedMLDataSet child) {
        synchronized (this) {
            this.additional.remove(child);
        }
    }

    /**
     * Begin loading to the binary file. After calling this method the add
     * methods may be called.
     *
     * @param inputSize The input size.
     * @param idealSize The ideal size.
     */
    public void beginLoad(final int inputSize, final int idealSize) {
        this.egb.create(inputSize, idealSize);
        this.loading = true;
    }

    /**
     * This method should be called once all the data has been loaded. The
     * underlying file will be closed. The binary fill will then be opened for
     * reading.
     */
    public void endLoad() {
        if (!this.loading) {
            throw new BufferedDataError("Must call beginLoad, before endLoad.");
        }

        this.egb.close();

        open();

    }

    /**
     * @return The binary file used.
     */
    public File getFile() {
        return this.file;
    }

    /**
     * @return The EGB file to use.
     */
    public EncogEGBFile getEGB() {
        return this.egb;
    }

    /**
     * Load the binary dataset to memory. Memory access is faster.
     *
     * @return A memory dataset.
     */
    public Dataset loadToMemory() {
        final VectorDataset result = new VectorDataset();

        for (final DataCase pair : this) {
            result.add(pair);
        }

        return result;
    }

    /**
     * Load the specified training set.
     *
     * @param training The training set to load.
     */
    public void load(final Dataset training) {
        beginLoad(training.getInputSize(), training.getIdealSize());
        for (final DataCase pair : training) {
            add(pair);
        }
        endLoad();
    }

    @Override
    public int size() {
        return (int) getRecordCount();
    }

    @Override
    public DataCase get(final int index) {
        final DataCase result = VectorCase.build(getInputSize(),
                getIdealSize());
        this.getRecord(index, result);
        return result;
    }
}
