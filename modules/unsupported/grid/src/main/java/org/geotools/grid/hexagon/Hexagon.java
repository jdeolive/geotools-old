/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 *
 *    (C) 2010, Open Source Geospatial Foundation (OSGeo)
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

package org.geotools.grid.hexagon;

import com.vividsolutions.jts.geom.Polygon;
import org.geotools.grid.GridElement;

/**
 * Defines methods and enum constants to work with hexagons.
 *
 * @author mbedward
 * @since 2.7
 * @source $URL$
 * @version $Id$
 */
public interface Hexagon extends GridElement {

    /**
     * Constants to describe the orientation of a hexagon.
     * The names of the constants refer to the appearance of the "top"
     * of the hexagon:
     * <ul>
     * <li>
     * An {@code ANGLED} hexagon has a "pointy" top with a single vertex
     * touching the upper edge of its bounding rectangle.
     * </li>
     * <li>
     * A {@code FLAT} hexagon has an edge that runs along the upper edge of its
     * bounding rectangle.
     * </li>
     * </ul>
     */
    public static enum Orientation {
        /**
         * The hexagon has a "pointy" top with a single vertex
         * touching the upper edge of its bounding rectangle
         */
        ANGLED,

        /**
         * The hexagon has an edge that runs along the upper edge of its
         * bounding rectangle.
         */
        FLAT;
    }

    /**
     * Gets the side length of this hexagon.
     *
     * @return side length
     */
    public double getSideLength();

    /**
     * Gets the orientation of this hexagon.
     *
     * @return either {@linkplain Orientation#ANGLED} or {@linkplain Orientation#FLAT}
     */
    public Orientation getOrientation();

}
