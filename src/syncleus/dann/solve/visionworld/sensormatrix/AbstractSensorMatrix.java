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
import syncleus.dann.solve.visionworld.SensorMatrix;

/**
 * Abstract sensor matrix.
 */
abstract class AbstractSensorMatrix implements SensorMatrix {

    /** Receptive field height. */
    private final int receptiveFieldHeight;

    /** Receptive field width. */
    private final int receptiveFieldWidth;

    /** Default filter. */
    private final Filter defaultFilter;

    /**
     * Create a new abstract sensor matrix with the specified receptive field
     * width and height and specified filter.
     *
     * @param receptiveFieldWidth receptive field width, must be
     *            <code>&gt;= 0</code>
     * @param receptiveFieldHeight receptive field height, must be
     *            <code>&gt;= 0</code>
     * @param defaultFilter default filter
     */
    protected AbstractSensorMatrix(final int receptiveFieldWidth,
            final int receptiveFieldHeight, final Filter defaultFilter) {
        if (receptiveFieldWidth < 0) {
            throw new IllegalArgumentException(
                    "receptiveFieldWidth must be >= 0");
        }
        if (receptiveFieldWidth < 0) {
            throw new IllegalArgumentException(
                    "receptiveFieldHeight must be >= 0");
        }
        this.receptiveFieldHeight = receptiveFieldHeight;
        this.receptiveFieldWidth = receptiveFieldWidth;
        this.defaultFilter = defaultFilter;
    }

    /** {@inheritDoc} */
    public final int getReceptiveFieldHeight() {
        return receptiveFieldHeight;
    }

    /** {@inheritDoc} */
    public final int getReceptiveFieldWidth() {
        return receptiveFieldWidth;
    }

    /** {@inheritDoc} */
    public final Filter getDefaultFilter() {
        return defaultFilter;
    }
}
