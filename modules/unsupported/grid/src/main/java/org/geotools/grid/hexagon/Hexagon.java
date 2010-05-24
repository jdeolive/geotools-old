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
     * Neighbour positions
     */
    public static enum Neighbor {
        /** Upper neighbour; only applicable for {@code Orientation#FLAT} */
        UPPER,

        /** Upper left neighbour; applicable to both orientations */
        UPPER_LEFT,

        /** Upper right neighbour; applicable to both orientations */
        UPPER_RIGHT,

        /** Lower neighbour; only applicable for {@code Orientation#FLAT} */
        LOWER,

        /** Lower left neighbour; applicable to both orientations */
        LOWER_LEFT,

        /** Lower right neighbour; applicable to both orientations */
        LOWER_RIGHT,

        /** Left neighbour; only applicable for {@code Orientation#ANGLED} */
        LEFT,

        /** Right neighbour; only applicable for {@code Orientation#ANGLED} */
        RIGHT;
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

    /**
     * Creates a new {@code Polygon} with the vertices of this hexagon.
     * <p>
     * For a hexagon with {@code FLAT} orientation, vertex 0 is
     * at the left of the upper edge while for {@code aNGLED}
     * orientation it is the uppermost vertex. Subsequent vertices
     * are indexed in clockwise order.
     */
    public Polygon toPolygon();
}
