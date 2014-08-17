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
package syncleus.dann.generate.ca.visualize.basic;

import syncleus.dann.generate.ca.universe.DiscreteCell;
import syncleus.dann.generate.ca.universe.Universe;
import syncleus.dann.generate.ca.universe.UniverseCell;
import syncleus.dann.generate.ca.visualize.CAVisualizer;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;

public class BasicCAVisualizer implements CAVisualizer {
    private final Universe universe;
    private int currentZoom;
    private int zoom = 1;
    private int width;
    private int height;
    private int[] pixels;
    private BufferedImage currentImage;
    private WritableRaster raster;

    public BasicCAVisualizer(final Universe theUniverse) {
        this.universe = theUniverse;
    }

    private void fillCell(final int row, final int col, final UniverseCell cell) {

        for (int y = 0; y < this.currentZoom; y++) {
            int idx = (((row * currentZoom) + y) * (width * currentZoom) * 3)
                    + ((col * currentZoom) * 3);
            for (int x = 0; x < this.currentZoom; x++) {
                if (cell instanceof DiscreteCell) {
                    if (cell.get(0) > 0) {
                        pixels[idx++] = 255;
                        pixels[idx++] = 255;
                        pixels[idx++] = 255;
                    } else {
                        pixels[idx++] = 0;
                        pixels[idx++] = 0;
                        pixels[idx++] = 0;
                    }
                } else {
                    for (int i = 0; i < 3; i++) {
                        final double d = (cell.get(i) + 1.0) / 2.0;
                        pixels[idx++] = Math.min((int) (d * 255.0), 255);
                    }
                }
            }
        }
    }

    @Override
    public Image visualize() {
        this.currentZoom = this.zoom;
        this.width = universe.getColumns();
        this.height = universe.getRows();

        final int imageSize = width * height * currentZoom * currentZoom * 3;

        if (this.pixels == null || this.pixels.length != imageSize) {

            this.currentImage = new BufferedImage(width * currentZoom, height
                    * currentZoom, BufferedImage.TYPE_INT_RGB);
            this.raster = this.currentImage.getRaster();
            this.pixels = new int[imageSize];
        }

        for (int row = 0; row < height; row++) {
            for (int col = 0; col < width; col++) {
                final UniverseCell cell = universe.get(row, col);
                fillCell(row, col, cell);
            }
        }

        raster.setPixels(0, 0, width * this.currentZoom, height
                * this.currentZoom, pixels);

        return this.currentImage;

    }

    @Override
    public int getZoom() {
        return this.zoom;
    }

    @Override
    public void setZoom(final int z) {
        this.zoom = z;
    }
}
