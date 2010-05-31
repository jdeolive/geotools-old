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

package org.geotools.grid;

import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.grid.oblong.Oblongs;

/**
 * A utility class to create vector grids.
 *
 * @author mbedward
 * @since 2.7
 * @source $URL$
 * @version $Id$
 */
public class Grids {

    /**
     * Creates a vector grid of square elements. The coordinate reference system is
     * taken from the input bounds. A {@code null} coordinate reference system is
     * permitted.
     * <p>
     * If the width and/or height of the bounding envelope is not a simple multiple
     * of the requested side length, there will be some unfilled space with the
     * grid's origin being the minimum X and Y point of the envelope.
     * <p>
     * Each square in the returned grid is represented by a {@code SimpleFeature}.
     * The feature type has two properties:
     * <ul>
     * <li>element - type Polygon
     * <li>id - type Integer
     * </ul>
     *
     * @param bounds bounds of the grid
     * @param sideLen the side length of grid elements
     * @return the vector grid
     */
    public static SimpleFeatureCollection createSquareGrid(
            ReferencedEnvelope bounds, double sideLen) {
        return Oblongs.createGrid(bounds, sideLen, sideLen, new IdAttributeSetter());
    }

    /**
     * Creates a vector grid of square elements. This version creates 'densified'
     * polygons to represent grid elements by adding additional vertices to each
     * edge. This is useful if you plan to display the grid in a projection other
     * than the one that it was created with because the extra vertices will
     * approximate curves.
     *
     * The coordinate reference system is taken from the input bounds.
     * A {@code null} coordinate reference system is permitted.
     * <p>
     * If the width and/or height of the bounding envelope is not a simple multiple
     * of the requested side length, there will be some unfilled space with the
     * grid's origin being the minimum X and Y point of the envelope.
     * <p>
     * Each square in the returned grid is represented by a {@code SimpleFeature}.
     * The feature type has two properties:
     * <ul>
     * <li>element - type Polygon
     * <li>id - type Integer
     * </ul>
     * Each {@code Polygon} representing a grid element is densified by adding
     * additiona vertices to its edges. The density of vertices is controlled by
     * the value of {@code vertexSpacing} which specifies the maximum distance
     * between adjacent vertices. Vertices are added more or less uniformly.
     *
     * @param bounds bounds of the grid
     *
     * @param sideLen the side length of grid elements
     *
     * @param vertexSpacing maximum distance between adjacent vertices in a grid
     *        element; if {@code <= 0} or {@code >= sideLen / 2.0} it is ignored
     *        and the polygons will not be densified
     *
     * @return the vector grid
     */
    public static SimpleFeatureCollection createSquareGrid(
            ReferencedEnvelope bounds, double sideLen, double vertexSpacing) {
        return Oblongs.createGrid(bounds, sideLen, sideLen, vertexSpacing,
                new IdAttributeSetter());
    }

}
