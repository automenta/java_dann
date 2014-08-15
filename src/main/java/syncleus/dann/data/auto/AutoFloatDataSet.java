package syncleus.dann.data.auto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import syncleus.dann.data.basic.BasicMLDataPair;
import syncleus.dann.data.file.csv.CSVFormat;
import syncleus.dann.data.file.csv.ReadCSV;
import syncleus.dann.learn.ml.MLData;
import syncleus.dann.learn.ml.MLDataPair;
import syncleus.dann.learn.ml.MLDataSet;

public class AutoFloatDataSet implements Serializable, MLDataSet {

    private final int sourceInputCount;
    private final int sourceIdealCount;
    private final int inputWindowSize;
    private final int outputWindowSize;
    private final List<AutoFloatColumn> columns = new ArrayList<>();
    private float normalizedMax = 1;
    private float normalizedMin = -1;
    private boolean normalizationEnabled = false;

    public class AutoFloatIterator implements Iterator<MLDataPair> {

        /**
         * The index that the iterator is currently at.
         */
        private int currentIndex = 0;

        /**
         * {@inheritDoc}
         */
        @Override
        public final boolean hasNext() {
            return this.currentIndex < AutoFloatDataSet.this.size();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public final MLDataPair next() {
            if (!hasNext()) {
                return null;
            }

            return AutoFloatDataSet.this.get(this.currentIndex++);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public final void remove() {
            throw new RuntimeException("Called remove, unsupported operation.");
        }

    }

    public AutoFloatDataSet(final int theInputCount, final int theIdealCount,
                            final int theInputWindowSize, final int theOutputWindowSize) {
        this.sourceInputCount = theInputCount;
        this.sourceIdealCount = theIdealCount;
        this.inputWindowSize = theInputWindowSize;
        this.outputWindowSize = theOutputWindowSize;
    }

    @Override
    public Iterator<MLDataPair> iterator() {
        return new AutoFloatIterator();
    }

    @Override
    public int getIdealSize() {
        return this.sourceIdealCount * this.outputWindowSize;
    }

    @Override
    public int getInputSize() {
        return this.sourceInputCount * this.inputWindowSize;
    }

    @Override
    public boolean isSupervised() {
        return getIdealSize() > 0;
    }

    @Override
    public long getRecordCount() {
        if (this.columns.isEmpty()) {
            return 0;
        } else {
            final int totalRows = this.columns.get(0).getData().length;
            final int windowSize = this.inputWindowSize + this.outputWindowSize;
            return (totalRows - windowSize) + 1;
        }
    }

    @Override
    public void getRecord(final long index, final MLDataPair pair) {

        int columnID = 0;

        // copy the input
        int inputIndex = 0;
        for (int i = 0; i < this.sourceInputCount; i++) {
            final AutoFloatColumn column = this.columns.get(columnID++);
            for (int j = 0; j < this.inputWindowSize; j++) {
                if (this.normalizationEnabled) {
                    pair.getInputArray()[inputIndex++] = column.getNormalized(
                            (int) index + j, this.normalizedMin,
                            this.normalizedMax);
                } else {
                    pair.getInputArray()[inputIndex++] = column.getData()[(int) index
                            + j];
                }
            }
        }

        // copy the output
        int idealIndex = 0;
        for (int i = 0; i < this.sourceIdealCount; i++) {
            final AutoFloatColumn column = this.columns.get(columnID++);
            for (int j = 0; j < this.outputWindowSize; j++) {
                if (this.normalizationEnabled) {
                    pair.getIdealArray()[idealIndex++] = column.getNormalized(
                            (int) (this.inputWindowSize + index + j),
                            this.normalizedMin, this.normalizedMax);
                } else {
                    pair.getIdealArray()[idealIndex++] = column.getData()[(int) (this.inputWindowSize
                            + index + j)];
                }
            }
        }

    }

    @Override
    public MLDataSet openAdditional() {
        return this;
    }

    @Override
    public void add(final MLData data1) {
        throw new RuntimeException("Add's not supported by this dataset.");

    }

    @Override
    public void add(final MLData inputData, final MLData idealData) {
        throw new RuntimeException("Add's not supported by this dataset.");

    }

    @Override
    public void add(final MLDataPair inputData) {
        throw new RuntimeException("Add's not supported by this dataset.");

    }

    @Override
    public void close() {

    }

    @Override
    public int size() {
        return (int) getRecordCount();
    }

    @Override
    public MLDataPair get(final int index) {
        if (index >= size()) {
            return null;
        }

        final MLDataPair result = BasicMLDataPair.createPair(getInputSize(),
                this.getIdealSize());
        getRecord(index, result);
        return result;
    }

    public void addColumn(final float[] data) {
        final AutoFloatColumn column = new AutoFloatColumn(data);
        this.columns.add(column);

    }

    public void loadCSV(final String filename, final boolean headers,
                        final CSVFormat format, final int[] input, final int[] ideal) {
        // first, just size it up
        ReadCSV csv = new ReadCSV(filename, headers, format);
        int lineCount = 0;
        while (csv.next()) {
            lineCount++;
        }
        csv.close();

        // allocate space to hold it
        final float[][] data = new float[input.length + ideal.length][lineCount];

        // now read the data in
        csv = new ReadCSV(filename, headers, format);
        int rowIndex = 0;
        while (csv.next()) {
            int columnIndex = 0;

            for (int i = 0; i < input.length; i++) {
                data[columnIndex++][rowIndex] = (float) csv.getDouble(input[i]);
            }
            for (int i = 0; i < ideal.length; i++) {
                data[columnIndex++][rowIndex] = (float) csv.getDouble(ideal[i]);
            }

            rowIndex++;
        }
        csv.close();

        // now add the columns
        for (int i = 0; i < data.length; i++) {
            addColumn(data[i]);
        }
    }

    /**
     * @return the normalizedMax
     */
    public float getNormalizedMax() {
        return normalizedMax;
    }

    /**
     * @param normalizedMax the normalizedMax to set
     */
    public void setNormalizedMax(final float normalizedMax) {
        this.normalizedMax = normalizedMax;
        this.normalizationEnabled = true;
    }

    /**
     * @return the normalizedMin
     */
    public float getNormalizedMin() {
        return normalizedMin;
    }

    /**
     * @param normalizedMin the normalizedMin to set
     */
    public void setNormalizedMin(final float normalizedMin) {
        this.normalizedMin = normalizedMin;
        this.normalizationEnabled = true;
    }

    /**
     * @return the normalizationEnabled
     */
    public boolean isNormalizationEnabled() {
        return normalizationEnabled;
    }

    /**
     * @param normalizationEnabled the normalizationEnabled to set
     */
    public void setNormalizationEnabled(final boolean normalizationEnabled) {
        this.normalizationEnabled = normalizationEnabled;
    }

}
