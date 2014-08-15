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
package syncleus.dann.evolve.genome;

import syncleus.dann.math.array.EngineArray;

/**
 * A genome made up of continuous doubles.
 */
public class DoubleArrayGenome extends BasicGenome implements ArrayGenome {

    /**
     * The serial id.
     */
    private static final long serialVersionUID = 1L;

    /**
     * The data.
     */
    private final double[] data;

    /**
     * Construct a genome of a specific size.
     *
     * @param size The size.
     */
    public DoubleArrayGenome(final int size) {
        this.data = new double[size];
    }

    /**
     * Construct a genome based on another genome.
     *
     * @param other
     */
    public DoubleArrayGenome(final DoubleArrayGenome other) {
        this.data = other.getData().clone();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int size() {
        return this.data.length;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void copy(final ArrayGenome source, final int sourceIndex,
                     final int targetIndex) {
        final DoubleArrayGenome sourceInt = (DoubleArrayGenome) source;
        this.data[targetIndex] = sourceInt.data[sourceIndex];

    }

    /**
     * @return The data.
     */
    public double[] getData() {
        return this.data;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void copy(final Genome source) {
        final DoubleArrayGenome sourceDouble = (DoubleArrayGenome) source;
        EngineArray.arrayCopy(sourceDouble.data, this.data);
        setScore(source.getScore());
        setAdjustedScore(source.getAdjustedScore());

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void swap(final int iswap1, final int iswap2) {
        final double temp = this.data[iswap1];
        this.data[iswap1] = this.data[iswap2];
        this.data[iswap2] = temp;

    }

}
