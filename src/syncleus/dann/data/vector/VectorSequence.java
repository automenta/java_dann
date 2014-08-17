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
import syncleus.dann.data.DataException;
import syncleus.dann.data.DataSequence;
import syncleus.dann.data.Dataset;
import syncleus.dann.math.array.EngineArray;
import syncleus.dann.util.ObjectCloner;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * A basic implementation of the MLSequenceSet.
 */
public class VectorSequence implements Serializable, DataSequence<VectorData>, Cloneable {

    /**
     * An iterator to be used with the BasicMLDataSet. This iterator does not
     * support removes.
     *
     * @author jheaton
     */
    public class VectorSeqIterator implements Iterator<DataCase<VectorData>> {

        /**
         * The index that the iterator is currently at.
         */
        private int currentIndex = 0;

        /**
         * The sequence index.
         */
        private int currentSequenceIndex = 0;

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean hasNext() {

            if (this.currentSequenceIndex >= sequences.size()) {
                return false;
            }

            final Dataset<VectorData> seq = sequences.get(this.currentSequenceIndex);

            return this.currentIndex < seq.getRecordCount();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public DataCase<VectorData> next() {
            if (!hasNext()) {
                return null;
            }

            final Dataset<VectorData> target = sequences.get(this.currentSequenceIndex);

            final DataCase<VectorData> result = target.getRecord(currentIndex);
            
            this.currentIndex++;
            if (this.currentIndex >= target.getRecordCount()) {
                this.currentIndex = 0;
                this.currentSequenceIndex++;
            }

            return result;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void remove() {
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
    private List<Dataset<VectorData>> sequences = new ArrayList<>();

    private VectorDataset currentSequence;

    /**
     * Default constructor.
     */
    public VectorSequence() {
        this.currentSequence = new VectorDataset();
        sequences.add(this.currentSequence);
    }

    public VectorSequence(final VectorSequence other) {
        this.sequences = other.sequences;
        this.currentSequence = other.currentSequence;
    }

    /**
     * Construct a data set from an input and ideal array.
     *
     * @param input The input into the machine learning method for training.
     * @param ideal The ideal output for training.
     */
    public VectorSequence(final double[][] input, final double[][] ideal) {
        this.currentSequence = new VectorDataset(input, ideal);
        this.sequences.add(this.currentSequence);
    }
//
//    /**
//     * Construct a data set from an already created list. Mostly used to
//     * duplicate this class.
//     *
//     * @param theData The data to use.
//     */
//    public VectorSequence(final List<VectorCase> theData) {
//        this.currentSequence = new VectorDataset(theData);
//        this.sequences.add(this.currentSequence);
//    }

    /**
     * Copy whatever dataset type is specified into a memory dataset.
     *
     * @param set The dataset to copy.
     */
    public VectorSequence(final VectorDataset set) {
        this.currentSequence = new VectorDataset();
        this.sequences.add(this.currentSequence);

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

            this.currentSequence.add(new VectorCase(input, ideal));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void add(final VectorData theData) {
        this.currentSequence.add(theData);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void add(final VectorData inputData, final VectorData idealData) {

        final DataCase pair = new VectorCase(inputData, idealData);
        this.currentSequence.add(pair);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void add(final DataCase inputData) {
        this.currentSequence.add(inputData);
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
     * {@inheritDoc}
     */
    @Override
    public int getIdealSize() {
        if (this.sequences.get(0).getRecordCount() == 0) {
            return 0;
        }
        return this.sequences.get(0).getIdealSize();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getInputSize() {
        if (this.sequences.get(0).getRecordCount() == 0) {
            return 0;
        }
        return this.sequences.get(0).getIdealSize();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DataCase<VectorData> getRecord(final long index) {
        long recordIndex = index;
        int sequenceIndex = 0;

        while (this.sequences.get(sequenceIndex).getRecordCount() < recordIndex) {
            recordIndex -= this.sequences.get(sequenceIndex).getRecordCount();
            sequenceIndex++;
            if (sequenceIndex > this.sequences.size()) {
                throw new DataException("Record out of range: " + index);
            }
        }

        return this.sequences.get(sequenceIndex).getRecord(recordIndex);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long getRecordCount() {
        long result = 0;
        result = this.sequences.stream().map(Dataset::getRecordCount).reduce(result, (accumulator, _item) -> accumulator + _item);
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isSupervised() {
        if (this.sequences.get(0).getRecordCount() == 0) {
            return false;
        }
        return this.sequences.get(0).isSupervised();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Iterator<DataCase<VectorData>> iterator() {
        final VectorSeqIterator result = new VectorSeqIterator();
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Dataset openAdditional() {
        return new VectorSequence(this);
    }

    @Override
    public void startNewSequence() {
        if (this.currentSequence.getRecordCount() > 0) {
            this.currentSequence = new VectorDataset();
            this.sequences.add(this.currentSequence);
        }
    }

    @Override
    public int getSequenceCount() {
        return this.sequences.size();
    }

    @Override
    public Dataset<VectorData> getSequence(final int i) {
        return this.sequences.get(i);
    }

    @Override
    public List<Dataset<VectorData>> getSequences() {
        return this.sequences;
    }

    
//    @Override
//    public List<Dataset<VectorData>> getSequences() {
//        return this.sequences;
//    }

    @Override
    public int size() {
        return (int) getRecordCount();
    }

    @Override
    public DataCase get(final int index) {        
        return this.getRecord(index);        
    }

    public void add(final Dataset<VectorData> sequence) {
        for (final DataCase pair : sequence) {
            add(pair);
        }

    }

}
