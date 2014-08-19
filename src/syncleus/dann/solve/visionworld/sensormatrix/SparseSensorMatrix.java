/*
 * Part of Simbrain--a java-based neural network kit
 * Copyright (C) 2006 Jeff Yoshimi <www.jeffyoshimi.net>
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package syncleus.dann.solve.visionworld.sensormatrix;

import syncleus.dann.solve.visionworld.Filter;
import syncleus.dann.solve.visionworld.Sensor;

/**
 * Sparse sensor matrix.
 */
public final class SparseSensorMatrix extends AbstractSensorMatrix {

    /** 2D object matrix of sensors. */
	private final Object [][] sensors;

    /**
     * Create a new sparse sensor matrix with the specified filter.
     *
     * @param rows number of rows, must be <code>&gt;= 1</code>
     * @param columns number of columns, must be <code>&gt;= 1</code>
     * @param receptiveFieldWidth receptive field width, must be
     *            <code>&gt;= 0</code>
     * @param receptiveFieldHeight receptive field height, must be
     *            <code>&gt;= 0</code>
     * @param defaultFilter default filter
     */
    public SparseSensorMatrix(final int rows, final int columns,
            final int receptiveFieldWidth, final int receptiveFieldHeight,
            final Filter defaultFilter) {
        super(receptiveFieldWidth, receptiveFieldHeight, defaultFilter);
        if (rows < 1) {
            throw new IllegalArgumentException("rows must be >= 1");
        }
        if (columns < 1) {
            throw new IllegalArgumentException("columns must be >= 1");
        }
        sensors = new Object[rows][columns];
    }

    /** {@inheritDoc} */
    public int rows() {
        return sensors.length;
    }

    /** {@inheritDoc} */
    public int columns() {
        return sensors[0].length;
    }

    /** {@inheritDoc} */
    public Sensor getSensor(final int row, final int column) {
        return (Sensor) sensors[row][column];
    }
}
