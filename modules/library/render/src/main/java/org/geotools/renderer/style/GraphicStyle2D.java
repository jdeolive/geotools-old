/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2003-2006, Geotools Project Managment Committee (PMC)
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *
 *    Created on 19-ott-2003
 */
package org.geotools.renderer.style;

import java.awt.image.BufferedImage;


/**
 * A style class used to depict a point, polygon centroid or line with a small graphic icon
 *
 * @author Andrea Aime
 * @source $URL$
 * @version $Id$
 */
public class GraphicStyle2D extends Style2D {
    BufferedImage image;
    float rotation;
    float opacity;

    /**
     * Creates a new GraphicStyle2D object.
     *
     * @param image The image that will be used to depict the centroid/point/...
     * @param rotation The image rotation
     * @param opacity The image opacity
     */
    public GraphicStyle2D(BufferedImage image, float rotation, float opacity) {
        this.image = image;
        this.rotation = rotation;
        this.opacity = opacity;
    }

    /**
     */
    public BufferedImage getImage() {
        return image;
    }

    /**
     */
    public float getOpacity() {
        return opacity;
    }

    /**
     */
    public float getRotation() {
        return rotation;
    }

    /**
     * @param image
     */
    public void setImage(BufferedImage image) {
        this.image = image;
    }

    /**
     * @param f
     */
    public void setOpacity(float f) {
        opacity = f;
    }

    /**
     * @param f
     */
    public void setRotation(float f) {
        rotation = f;
    }
}
