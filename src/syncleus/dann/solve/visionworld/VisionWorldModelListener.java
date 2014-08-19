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
package syncleus.dann.solve.visionworld;

import java.util.EventListener;

/**
 * Event listener that receives notification of changes to a VisionWorldModel.
 */
public interface VisionWorldModelListener extends EventListener {

    /**
     * Notify this listener the pixel matrix changed in a vision world model.
     *
     * @param event vision world model event
     */
    void pixelMatrixChanged(VisionWorldModelEvent event);

    /**
     * Notify this listener the sensor matrix changed in a vision world model.
     *
     * @param event vision world model event
     */
    void sensorMatrixChanged(VisionWorldModelEvent event);
}
