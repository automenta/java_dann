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
package syncleus.dann.solve.visionworld.filter;

import java.awt.image.BufferedImage;

import syncleus.dann.solve.visionworld.Filter;

/**
 * Uniform filter.
 */
public final class UniformFilter implements Filter {

    /** Display name. */
    private static final String DISPLAY_NAME = "Uniform filter";

    /** Value. */
    private final double value;

    /**
     * Create a new uniform filter with the specified value.
     *
     * @param value value for this uniform filter
     */
    public UniformFilter(final double value) {
        this.value = value;
    }

    /**
     * Return the value for this uniform filter.
     *
     * @return the value for this uniform filter
     */
    public double getValue() {
        return value;
    }

    /** {@inheritDoc} */
    public double filter(final BufferedImage image) {
        return value;
    }

    public String getDescription() {
        return DISPLAY_NAME + ", value=" + value;
    }
}
