/*
 * Geotools 2 - OpenSource mapping toolkit
 * (C) 2003, Geotools Project Managment Committee (PMC)
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
 *    You should have received a copy of the GNU Lesser General Public
 *    License along with this library; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 *
 * Contacts:
 *     UNITED KINGDOM: James Macgill
 *             mailto:j.macgill@geog.leeds.ac.uk
 */
package org.geotools.renderer.style;

// J2SE dependencies
import java.awt.Paint;
import java.awt.Stroke;
import java.awt.Graphics2D;


/**
 * Base class for all style targeting {@link Graphics2D}.
 *
 * @version $Id: Style2D.java,v 1.1 2003/05/28 10:23:51 desruisseaux Exp $
 */
public abstract class Style2D extends Style {
    /**
     * Construct a default style.
     */
    public Style2D() {
    }

    /**
     * Returns the stroke for the {@linkplain org.geotools.renderer.geom.Polyline polyline}
     * to be rendered, or <code>null</code> if none.
     */
    public abstract Stroke getStroke();

    /**
     * Returns the contour color for the {@linkplain org.geotools.renderer.geom.Polyline polyline}
     * to be rendered, or <code>null</code> if none.
     */
    public abstract Paint getContour();

    /**
     * Returns the filling color for the {@linkplain org.geotools.renderer.geom.Polygon polygon}
     * to be rendered, or <code>null</code> if none.
     */
    public abstract Paint getFill();
}
