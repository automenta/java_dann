/*
 * Part of Simbrain--a java-based neural network kit
 * Copyright (C) 2005,2007 The Authors.  See http://www.simbrain.net/credits
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
package syncleus.dann.learn.layouts;

import java.awt.geom.Point2D;
import java.util.List;

import syncleus.dann.neural.spiking.SpikingNeuron;

/**
 * Lay neurons out in a line.
 *
 * @author jyoshimi
 */
public class LineLayout implements Layout {

    /** Orientation of the line. */
    public enum LineOrientation {
        VERTICAL {
            public String toString() {
                return "Vertical";
            }
        },
        HORIZONTAL {
            public String toString() {
                return "Horizontal";
            }
        }
    };

    /** The default orientation of the line. */
    public static final LineOrientation DEFAULT_LINE_ORIENTATION = LineOrientation.HORIZONTAL;

    /** The default spacing between neurons when laid out. */
    public static final double DEFAULT_SPACING = 40;

    /** Current line orientation. */
    private LineOrientation orientation = DEFAULT_LINE_ORIENTATION;

    /** Spacing between neurons. */
    private double spacing = DEFAULT_SPACING;

    /** Initial x position of line of neurons. */
    private double initialX;

    /** Initial y position of line of neurons. */
    private double initialY;

    /**
     * Create a layout.
     *
     * @param spacing spacing between neurons
     * @param orientation of the neurons
     */
    public LineLayout(final double spacing, final LineOrientation orientation) {
        this.spacing = spacing;
        this.orientation = orientation;
    }

    /**
     * Create a line layout with all values specified.
     *
     * @param x initial x position
     * @param y initial y position
     * @param spacing spacing between neurons
     * @param orientation vertical or horizontal
     */
    public LineLayout(final double x, final double y, final double spacing,
            final LineOrientation orientation) {
        initialX = x;
        initialY = y;
        this.spacing = spacing;
        this.orientation = orientation;
    }

    /**
     * Default Constructor.
     */
    public LineLayout() {
    }

    @Override
    public void layoutNeurons(final List<SpikingNeuron> neurons) {
        if (orientation == LineOrientation.VERTICAL) {
            double ypos = initialY;
            for (SpikingNeuron neuron : neurons) {
                neuron.setX(initialX);
                neuron.setY(ypos);
                ypos += spacing;
            }
        } else if (orientation == LineOrientation.HORIZONTAL) {
            double xpos = initialX;
            for (SpikingNeuron neuron : neurons) {
                neuron.setX(xpos);
                neuron.setY(initialY);
                xpos += spacing;
            }
        }
    }

    @Override
    public void setInitialLocation(final Point2D initialPoint) {
        initialX = initialPoint.getX();
        initialY = initialPoint.getY();
    }

    @Override
    public String getDescription() {
        return "Line";
    }

    /**
     * @return the orientation
     */
    public LineOrientation getOrientation() {
        return orientation;
    }

    /**
     * @param orientation the orientation to set
     */
    public void setOrientation(final LineOrientation orientation) {
        this.orientation = orientation;
        // System.out.println("LineLayout orientation: " + this.orientation);
    }

    /**
     * @return the spacing
     */
    public double getSpacing() {
        return spacing;
    }

    /**
     * @param spacing the spacing to set
     */
    public void setSpacing(final double spacing) {
        this.spacing = spacing;
    }

    @Override
    public String toString() {
        return "Line Layout";
    }
}
