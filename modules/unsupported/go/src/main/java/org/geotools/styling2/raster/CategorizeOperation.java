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

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import org.opengis.filter.expression.Expression;

/**
 * Categorize function operation
 * 
 * @author Johann Sorel
 */
public class CategorizeOperation extends AbstractOperation {
        private final Expression exp;

        public CategorizeOperation(Expression exp) {
            this.exp = exp;
        }


        private void mixColor(int[] inPixels) {
            
            for (int i = 0; i < inPixels.length; i++) {
                int argb = inPixels[i];
                int a = argb & 0xFF000000;
                int r = (argb >> 16) & 0xFF;
                int g = (argb >> 8) & 0xFF;
                int b = (argb) & 0xFF;

                Color pxColor = new Color(r, g, b);
                
                Color catColor = exp.evaluate(new int[]{r,g,b}, Color.class);
                inPixels[i] = catColor.getRGB();
                
//                r = (int) (r * (1.0f - mixValue) + mix_r * mixValue);
//                g = (int) (g * (1.0f - mixValue) + mix_g * mixValue);
//                b = (int) (b * (1.0f - mixValue) + mix_b * mixValue);
//                inPixels[i] = a << 24 | r << 16 | g << 8 | b;
            }
        }

        public int[] getPixels(BufferedImage img, int x, int y, int w, int h, int[] pixels) {
        if (w == 0 || h == 0) {
            return new int[0];
        }

        if (pixels == null) {
            pixels = new int[w * h];
        } else if (pixels.length < w * h) {
            throw new IllegalArgumentException(
                    "pixels array must have a length >= w*h");
        }
        int imageType = img.getType();
        if (imageType == BufferedImage.TYPE_INT_ARGB ||
                imageType == BufferedImage.TYPE_INT_RGB) {
            Raster raster = img.getRaster();
            return (int[]) raster.getDataElements(x, y, w, h, pixels);
        }
        return img.getRGB(x, y, w, h, pixels, 0, w);
    }

        public void setPixels(BufferedImage img, int x, int y, int w, int h, int[] pixels) {
        if (pixels == null || w == 0 || h == 0) {
            return;
        } else if (pixels.length < w * h) {
            throw new IllegalArgumentException(
                    "pixels array must have a length >= w*h");
        }
        int imageType = img.getType();
        if (imageType == BufferedImage.TYPE_INT_ARGB ||
                imageType == BufferedImage.TYPE_INT_RGB) {
            WritableRaster raster = img.getRaster();
            raster.setDataElements(x, y, w, h, pixels);
        } else {
            img.setRGB(x, y, w, h, pixels, 0, w);
        }
}
        
        @Override
        public BufferedImage filter(BufferedImage src, BufferedImage dst) {
            if (dst == null) {
                dst = createCompatibleDestImage(src, null);
            }
            int width = src.getWidth();
            int height = src.getHeight();
            int[] pixels = new int[width * height];
            getPixels(src, 0, 0, width, height, pixels);
            mixColor(pixels);
            setPixels(dst, 0, 0, width, height, pixels);
            return dst;

        }
    }
