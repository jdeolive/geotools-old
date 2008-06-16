/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 *
 *    (C) 2004-2008, Open Source Geospatial Foundation (OSGeo)
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

package org.geotools.styling2.raster;

import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ColorModel;

/**
 * Abstract ImageOperation
 * 
 * @author Johann Sorel (Geomatys)
 */
public abstract class AbstractOperation implements BufferedImageOp {
        
        public abstract BufferedImage filter(BufferedImage src, BufferedImage dest);

        public Rectangle2D getBounds2D(BufferedImage src) {
            return new Rectangle(0, 0, src.getWidth(),src.getHeight());
        }

        public BufferedImage createCompatibleDestImage(BufferedImage src, ColorModel destCM) {
            if (destCM == null) {
                destCM = src.getColorModel();
            }
            return new BufferedImage(destCM,
                    destCM.createCompatibleWritableRaster(
                    src.getWidth(), src.getHeight()),
                    destCM.isAlphaPremultiplied(), null);
        }

        public Point2D getPoint2D(Point2D srcPt,Point2D dstPt) {
            return (Point2D) srcPt.clone();
        }

        public RenderingHints getRenderingHints() {
            return null;
        }
}
