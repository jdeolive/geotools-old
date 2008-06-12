/*
 *    GeoTools - The Open Source Java GIS Tookit
 *    http://geotools.org
 *
 *    (C) 2005-2008, Open Source Geospatial Foundation (OSGeo)
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
package org.geotools.display.event;

// J2SE dependencies
import java.awt.geom.Point2D;

// OpenGIS dependencies
import org.opengis.geometry.DirectPosition;


/**
 * Common interface for events corresponding in some geographic location. They are typically mouse
 * events with {@linkplain org.geotools.display.canvas.ReferencedCanvas#getDisplayToObjectiveTransform
 * display to objective transform} capabilities.
 *
 * @since 2.3
 * @source $URL$
 * @version $Id$
 * @author Martin Desruisseaux (IRD)
 */
public interface ReferencedEvent {
    /**
     * Returns the mouse's position in terms of
     * {@linkplain org.geotools.display.canvas.ReferencedCanvas#getDisplayCRS display CRS}.
     * This method is similar to {@link java.awt.event.MouseEvent#getPoint()} except that
     * the mouse location is corrected for deformations caused by some artifacts like the
     * {@linkplain org.geotools.gui.swing.ZoomPane#setMagnifierVisible magnifying glass}.
     */
    DirectPosition getDisplayPosition();

    /**
     * Returns the mouse's position in terms of
     * {@linkplain org.geotools.display.canvas.ReferencedCanvas#getObjectiveCRS objective CRS}.
     */
    DirectPosition getObjectivePosition();

    /**
     * Returns the {@linkplain #getDisplayPosition display position} as a two-dimensional point.
     * If the display position has more than two dimensions, only the two first ones are returned.
     */
    Point2D getDisplayPoint2D();

    /**
     * Returns the {@linkplain #getObjectivePosition objective position} as a two-dimensional point.
     * If the objective position has more than two dimensions, only the two first ones are returned.
     */
    Point2D getObjectivePoint2D();
}
