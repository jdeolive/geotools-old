/*
 *    Geotools2 - OpenSource mapping toolkit
 *    http://geotools.org
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
 *
 */
/*
 * Created on 19-ott-2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package org.geotools.renderer.style;

import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;


/**
 * A style class used to depict a point, polygon centroid or line with a small graphic icon
 *
 * @author Andrea Aime
 * @version $Id: GraphicStyle2D.java,v 1.2 2003/11/15 14:18:08 aaime Exp $
 */
public class GraphicStyle2D extends Style2D {
    BufferedImage image;
    float rotation;
    float opacity;

    /**
     * Creates a new GraphicStyle2D object.
     *
     * @param image The image that will be used to depict the centroid/point/...
     * @param size The image size, in pixels
     * @param rotation The image rotation
     * @param opacity The image opacity
     */
    public GraphicStyle2D(BufferedImage image, float rotation, float opacity) {
        this.image = image;
        this.rotation = rotation;
        this.opacity = opacity;
    }

    /**
     * @return
     */
    public BufferedImage getImage() {
        return image;
    }

    /**
     * @return
     */
    public float getOpacity() {
        return opacity;
    }

    /**
     * @return
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
