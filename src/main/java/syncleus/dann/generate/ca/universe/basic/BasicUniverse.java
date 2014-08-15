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

import syncleus.dann.generate.ca.universe.Universe;
import syncleus.dann.generate.ca.universe.UniverseCell;
import syncleus.dann.generate.ca.universe.UniverseCellFactory;
import syncleus.dann.learn.ml.BasicML;

import java.io.Serializable;

public class BasicUniverse extends BasicML implements Universe, Serializable, Cloneable {
    /**
     *
     */
    private static final long serialVersionUID = 1L;
    public static final String ELEMENT_COUNT = "elementCount";
    private final UniverseCell[][] data;
    private final UniverseCellFactory cellFactory;

    public BasicUniverse(final int height, final int width,
                         final UniverseCellFactory theCellFactory) {
        this.data = new UniverseCell[height][width];
        this.cellFactory = theCellFactory;
        for (int row = 0; row < getRows(); row++) {
            for (int col = 0; col < getColumns(); col++) {
                this.data[row][col] = this.cellFactory.factor();
            }
        }
    }

    @Override
    public Object clone() {
        final BasicUniverse result = new BasicUniverse(getRows(), getColumns(),
                this.cellFactory);
        result.copy(this);
        return result;
    }

    @Override
    public void copy(final Universe source) {
        if (!(source instanceof BasicUniverse)) {
            throw new RuntimeException(
                    "Can only copy another BasicUniverse");
        }
        for (int row = 0; row < getRows(); row++) {
            for (int col = 0; col < getColumns(); col++) {
                this.data[row][col].copy(source.get(row, col));
            }
        }
    }

    @Override
    public double compare(final Universe otherWorld) {
        if (!(otherWorld instanceof BasicUniverse)) {
            throw new RuntimeException(
                    "Can only compare another BasicUniverse");
        }

        int result = 0;
        int total = 0;
        for (int row = 0; row < otherWorld.getRows(); row++) {
            for (int col = 0; col < otherWorld.getColumns(); col++) {
                final int d1 = Math.abs((int) (255 * get(row, col).getAvg()));
                final int d2 = Math.abs((int) (255 * otherWorld.get(row, col)
                        .getAvg()));
                if (Math.abs(d1 - d2) > 10) {
                    result++;
                }
                total++;
            }
        }

        return result / total;
    }

    @Override
    public int getColumns() {
        return this.data[0].length;
    }

    @Override
    public int getRows() {
        return this.data.length;
    }

    @Override
    public void randomize() {
        for (int row = 0; row < getRows(); row++) {
            for (int col = 0; col < getColumns(); col++) {
                for (int i = 0; i < 3; i++) {
                    this.data[row][col].randomize();
                }
            }
        }
    }

    @Override
    public UniverseCell get(final int row, final int col) {
        return this.data[row][col];
    }

    @Override
    public boolean isValid(final int row, final int col) {
        return row >= 0 && col >= 0 && row < getRows() && col < getColumns();
    }

    @Override
    public UniverseCellFactory getCellFactory() {
        return cellFactory;
    }

    @Override
    public void updateProperties() {
        // TODO Auto-generated method stub

    }

    @Override
    public double calculatePercentInvalid() {
        int result = 0;
        int total = 0;
        for (int row = 0; row < getRows(); row++) {
            for (int col = 0; col < getColumns(); col++) {
                final UniverseCell cell = get(row, col);
                for (int i = 0; i < cell.size(); i++) {
                    if (cell.get(i) < -1 || cell.get(i) > 1) {
                        result++;
                    }
                    total++;
                }
            }
        }

        return result / total;
    }
}
