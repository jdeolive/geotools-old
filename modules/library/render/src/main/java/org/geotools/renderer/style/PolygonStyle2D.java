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
 *    Created on 7 giugno 2003, 19.11
 */
package org.geotools.renderer.style;

// J2SE dependencies
import java.awt.Composite;
import java.awt.Paint;

import org.geotools.resources.Utilities;


/**
 * A style that contains the specification to renderer both the contour and the interior of a shape
 *
 * @author Andrea Aime
 * @source $URL$
 * @version $Id$
 */
public class PolygonStyle2D extends LineStyle2D {
    protected Paint fill;
    protected Composite fillComposite;

    /**
     * Returns the filling color for the {@linkplain org.geotools.renderer.geom.Polygon polygon} to
     * be rendered, or <code>null</code> if none.
     *
     * @return the current fill or null if none
     */
    public Paint getFill() {
        return this.fill;
    }

    /**
     * Sets filling color for the {@linkplain org.geotools.renderer.geom.Polygon polygon} to be
     * rendered. Set it to <code>null</code> if no filling is to be performed.
     *
     * @param fill
     */
    public void setFill(Paint fill) {
        this.fill = fill;
    }

    /**
     * Returns the fill Composite for the {@linkplain org.geotools.renderer.geom.Polyline polyline}
     * to be rendered, or <code>null</code> if the contour is to be opaque
     *
     * @return the current fill composite or null if none
     */
    public Composite getFillComposite() {
        return this.fillComposite;
    }

    /**
     * Sets the fill Composite for the {@linkplain org.geotools.renderer.geom.Polyline polyline} to
     * be rendered. Set it to <code>null</code> if the contour is to be opaque
     *
     * @param fillComposite
     */
    public void setFillComposite(Composite fillComposite) {
        this.fillComposite = fillComposite;
    }

    /**
     * Returns a string representation of this style.
     */
    public String toString() {
        return Utilities.getShortClassName(this) + '[' + fill + ']';
    }
}
