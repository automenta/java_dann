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
package syncleus.dann.data.vector;

import syncleus.dann.data.DataCase;
import syncleus.dann.data.Dataset;
import syncleus.dann.math.array.EngineArray;
import syncleus.dann.util.ObjectCloner;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Stores data in an ArrayList. This class is memory based, so large enough
 * datasets could cause memory issues. Many other dataset types extend this
 * class.
 *
 * @author jheaton
 */
public class VectorDataset implements Serializable, Dataset<VectorData>, Cloneable {


    /**
     * An iterator to be used with the BasicMLDataSet. This iterator does not
     * support removes.
     *
     * @author jheaton
     */
    public class BasicMLIterator implements Iterator<DataCase<VectorData>> {

        /**
         * The index that the iterator is currently at.
         */
        private int currentIndex = 0;

        /**
         * {@inheritDoc}
         */
        @Override
        public final boolean hasNext() {
            return this.currentIndex < VectorDataset.this.data.size();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public final DataCase<VectorData> next() {
            if (!hasNext()) {
                return null;
            }

            return data.get(this.currentIndex++);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public final void remove() {
            throw new RuntimeException("Called remove, unsupported operation.");
        }
    }

    /**
     * The serial id.
     */
    private static final long serialVersionUID = -2279722928570071183L;

    /**
     * The data held by this object.
     */
    private final List<DataCase<VectorData>> data;

    /**
     * Default constructor.
     */
    public VectorDataset() {
         this.data = new ArrayList<>();
    }
    /**
     * Construct a data set from an already created list. Mostly used to
     * duplicate this class.
     *
     * @param theData The data to use.
     */
    public VectorDataset(final List<DataCase<VectorData>> theData) {
        this.data = theData;
    }

    /**
     * Construct a data set from an input and ideal array.
     *
     * @param input The input into the machine learning method for training.
     * @param ideal The ideal output for training.
     */
    public VectorDataset(final double[][] input, final double[][] ideal) {
        this();
        if (ideal != null) {
            for (int i = 0; i < input.length; i++) {
                final VectorData inputData = new VectorData(input[i]);
                final VectorData idealData = new VectorData(ideal[i]);
                this.add(inputData, idealData);
            }
        } else {
            for (final double[] element : input) {
                final VectorData inputData = new VectorData(element);
                this.add(inputData);
            }
        }
    }


    /**
     * Copy whatever dataset type is specified into a memory dataset.
     *
     * @param set The dataset to copy.
     */
    public VectorDataset(final Dataset<VectorData> set) {
        this();
        final int inputCount = set.getInputSize();
        final int idealCount = set.getIdealSize();

        for (final DataCase<VectorData> pair : set) {

            VectorData input = null;
            VectorData ideal = null;

            if (inputCount > 0) {
                input = new VectorData(inputCount);
                EngineArray.arrayCopy(pair.getInputArray(), input.getData());
            }

            if (idealCount > 0) {
                ideal = new VectorData(idealCount);
                EngineArray.arrayCopy(pair.getIdealArray(), ideal.getData());
            }

            add(new VectorCase(input, ideal));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void add(final VectorData theData) {
        this.data.add(new VectorCase(theData));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void add(final VectorData inputData, final VectorData idealData) {

        final DataCase pair = new VectorCase(inputData, idealData);
        this.data.add(pair);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void add(final DataCase inputData) {
        this.data.add(inputData);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object clone() {
        return ObjectCloner.deepCopy(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() {
        // nothing to close
    }

    /**
     * Get the data held by this container.
     *
     * @return the data
     */
    public List<DataCase<VectorData>> getData() {
        return this.data;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getIdealSize() {
        if (this.data.isEmpty()) {
            return 0;
        }
        final DataCase first = this.data.get(0);
        if (first.getIdeal() == null) {
            return 0;
        }

        return first.getIdeal().size();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getInputSize() {
        if (this.data.isEmpty()) {
            return 0;
        }
        final DataCase first = this.data.get(0);
        return first.getInput().size();
    }

    /**
     * {@inheritDoc}
     */
    public DataCase<VectorData> getRecord(final long index) {

        final DataCase<VectorData> source = this.data.get((int) index);
        return new VectorCase(source.getInput(), source.getIdeal());

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long getRecordCount() {
        return this.data.size();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isSupervised() {
        if (this.data.isEmpty()) {
            return false;
        }
        return this.data.get(0).isSupervised();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Iterator<DataCase<VectorData>> iterator() {
        final BasicMLIterator result = new BasicMLIterator();
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Dataset openAdditional() {
        return new VectorDataset(this.data);
    }


    /**
     * Concert the data set to a list.
     *
     * @param theSet The data set to convert.
     * @return The list.
     */
    public static List<DataCase<VectorData>> toList(final Dataset<VectorData> theSet) {
        final List<DataCase<VectorData>> list = new ArrayList<>();
        for (final DataCase<VectorData> pair : theSet) {
            list.add(pair);
        }
        return list;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int size() {
        return (int) getRecordCount();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DataCase get(final int index) {
        return this.data.get(index);
    }

}
