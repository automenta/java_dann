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

import syncleus.dann.generate.ca.universe.DiscreteCell;
import syncleus.dann.generate.ca.universe.UniverseCell;
import syncleus.dann.math.array.EngineArray;
import syncleus.dann.math.random.RangeRandomizer;

public class BasicDiscreteCell implements DiscreteCell, Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    private final int[] data;
    private final int elementCount;

    public BasicDiscreteCell(final int theSize, final int theElementCount) {
        this.data = new int[theSize];
        this.elementCount = theElementCount;
    }

    @Override
    public double get(final int index) {
        return this.data[index];
    }

    @Override
    public void randomize() {
        for (int i = 0; i < this.data.length; i++) {
            this.data[i] = RangeRandomizer.randomInt(0, elementCount);
        }

    }

    @Override
    public void copy(final UniverseCell sourceCell) {
        if (!(sourceCell instanceof BasicDiscreteCell)) {
            throw new RuntimeException(
                    "Can only copy another BasicDiscreteCell.");
        }

        for (int i = 0; i < this.data.length; i++) {
            this.data[i] = (int) sourceCell.get(i);
        }

    }

    @Override
    public double getAvg() {
        return EngineArray.mean(this.data);
    }

    @Override
    public void set(final int i, final double d) {
        this.data[i] = (int) d;
    }

    @Override
    public int size() {
        return this.data.length;
    }

    @Override
    public void set(final int idx, final double[] d) {
        for (int i = 0; i < this.data.length; i++) {
            this.data[i] = (int) d[idx + i];
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
            result.append(this.data[i]);
        }
        result.append(']');
        return result.toString();
    }
}
