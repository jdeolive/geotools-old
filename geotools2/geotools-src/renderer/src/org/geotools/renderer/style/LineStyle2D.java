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
 * LineStyle2D.java
 *
 * Created on 7 giugno 2003, 19.08
 */
package org.geotools.renderer.style;

import java.awt.*;


/**
 * A rendered style to draw the contour of shapes  TODO: add support for Graphic rendering (that
 * is, an image painted along the contour)
 *
 * @author aaime
 */
public class LineStyle2D extends Style2D {
    protected Paint contour;
    protected Stroke stroke;
    protected Composite contourComposite;

    /**
     * Returns the stroke for the {@linkplain org.geotools.renderer.geom.Polyline polyline} to be
     * rendered, or <code>null</code> if none.
     *
     * @return the current stroke or null if none
     */
    public Stroke getStroke() {
        return this.stroke;
    }

    /**
     * Sets the stroke for the {@linkplain org.geotools.renderer.geom.Polyline polyline} to be
     * rendered
     *
     * @param stroke The stroke, or null if the contour doesn't need to be stroked
     */
    public void setStroke(Stroke stroke) {
        this.stroke = stroke;
    }

    /**
     * Returns the contour color for the {@linkplain org.geotools.renderer.geom.Polyline polyline}
     * to be rendered, or <code>null</code> if none.
     *
     * @return the current contour, or null if none
     */
    public Paint getContour() {
        return this.contour;
    }

    /**
     * Sets the contour color for the {@linkplain org.geotools.renderer.geom.Polyline polyline} to
     * be rendered
     *
     * @param contour
     */
    public void setContour(Paint contour) {
        this.contour = contour;
    }

    /**
     * Returns the contour Composite for the {@linkplain org.geotools.renderer.geom.Polyline
     * polyline} to be rendered, or <code>null</code> if the contour is to be opaque
     *
     * @return the current contour composite, or null if opaque
     */
    public Composite getContourComposite() {
        return this.contourComposite;
    }

    /**
     * Sets the contour Composite for the {@linkplain org.geotools.renderer.geom.Polyline polyline}
     * to be rendered. Set it to <code>null</code> if the contour is to be opaque
     *
     * @param contourComposite
     */
    public void setContourComposite(Composite contourComposite) {
        this.contourComposite = contourComposite;
    }
}
