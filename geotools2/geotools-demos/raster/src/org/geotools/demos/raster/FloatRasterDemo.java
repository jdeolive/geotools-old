/*
 *    Geotools2 - OpenSource mapping toolkit
 *    (C) 2002, Geotools Project Managment Committee (PMC)
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    version 2.1 of the License.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
package org.geotools.demos.raster;

// J2SE dependencies
import java.awt.Color;
import java.awt.geom.Rectangle2D;
import java.awt.image.DataBuffer;
import java.awt.image.WritableRaster;

// JAI dependencies
import javax.media.jai.RasterFactory;

// Geotools dependencies
import org.geotools.pt.Envelope;
import org.geotools.gc.GridCoverage;
import org.geotools.cs.GeographicCoordinateSystem;
import org.geotools.gui.swing.FrameFactory;


/**
 * A simple demo computing a {@link WritableRaster raster} and displaying it.
 * The raster uses <code>float</code> data type with arbitrary sample values.
 * At the difference of {@link DoubleImageDemo}, this demo consider the image
 * as one and only one tile. Consequently, sample values are set directly in
 * the raster (no need to deal for multi-tiles).
 */
public class FloatRasterDemo {
    /**
     * Run the demo.
     */
    public static void main(String[] args) {
        final int width  = 500;
        final int height = 500;
        WritableRaster raster = RasterFactory.createBandedRaster(DataBuffer.TYPE_FLOAT,
                                                                 width, height, 1, null);
        for (int y=0; y<height; y++) {
            for (int x=0; x<width; x++) {
                raster.setSample(x, y, 0, x+y);
            }
        }
        Envelope envelope = new Envelope(new Rectangle2D.Float(0, 0, 30, 30));
        GridCoverage gc = new GridCoverage("My grayscale coverage", raster, envelope);
        FrameFactory.show(gc);
        /*
         * The above example created a grayscale image. The example below create a new grid coverage
         * for the same data, but using a specified color map. Note that the constructor used allows
         * more details to be specified, for example units. Setting some of those arguments to null
         * (as in this example) lets GridCoverage computes automatically a default value.
         */
        Color[] colors = new Color[] {Color.BLUE, Color.CYAN, Color.WHITE, Color.YELLOW, Color.RED};
        gc = new GridCoverage("My colored coverage", raster, GeographicCoordinateSystem.WGS84,
                              envelope, null, null, null, new Color[][] {colors}, null);
        
        FrameFactory.show(gc);
    }
}
