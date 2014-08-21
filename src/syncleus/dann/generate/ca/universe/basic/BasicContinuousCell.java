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
package syncleus.dann.generate.ca.universe.basic;

import java.io.Serializable;
import syncleus.dann.generate.ca.universe.ContinuousCell;
import syncleus.dann.generate.ca.universe.UniverseCell;
import syncleus.dann.math.Format;
import syncleus.dann.math.array.EngineArray;
import syncleus.dann.math.random.RangeRandomizer;

public class BasicContinuousCell implements ContinuousCell, Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    private final double[] data;
    private final double min;
    private final double max;

    public BasicContinuousCell(final int size, final double theMin,
                               final double theMax) {
        this.data = new double[size];
        this.max = theMax;
        this.min = theMin;
    }

    @Override
    public void randomize() {
        for (int i = 0; i < this.data.length; i++) {
            this.data[i] = RangeRandomizer.randomize(min, max);
        }

    }

    @Override
    public void copy(final UniverseCell sourceCell) {
        if (!(sourceCell instanceof BasicContinuousCell)) {
            throw new RuntimeException(
                    "Can only copy another BasicContinuousCell.");
        }

        for (int i = 0; i < this.data.length; i++) {
            this.data[i] = sourceCell.get(i);
        }
    }

    @Override
    public double getAvg() {
        return EngineArray.mean(this.data);
    }

    @Override
    public double get(final int i) {
        return this.data[i];
    }

    @Override
    public void set(final int i, final double d) {
        this.data[i] = d;
    }

    @Override
    public int size() {
        return this.data.length;
    }

    @Override
    public void add(final UniverseCell otherCell) {
        for (int i = 0; i < this.data.length; i++) {
            this.data[i] += otherCell.get(i);
        }
    }

    @Override
    public void multiply(final UniverseCell otherCell) {
        for (int i = 0; i < this.data.length; i++) {
            this.data[i] *= otherCell.get(i);
        }
    }

    @Override
    public void set(final int idx, final double[] d) {
        System.arraycopy(d, idx + 0, this.data, 0, this.data.length);
    }

    @Override
    public void clamp(final double low, final double high) {
        for (int i = 0; i < this.data.length; i++) {
            if (this.data[i] < low)
                this.data[i] = low;
            if (this.data[i] > high)
                this.data[i] = high;
        }

    }

    @Override
    public String toString() {
        final StringBuilder result = new StringBuilder();
        result.append("'[");
        result.append(this.getClass().getSimpleName());
        result.append(':');
        for (int i = 0; i < this.size(); i++) {
            if (i > 0) {
                result.append(',');
            }
            result.append(i);
            result.append('=');
            result.append(Format.formatDouble(this.data[i], 4));
        }
        result.append(']');
        return result.toString();
    }
}
