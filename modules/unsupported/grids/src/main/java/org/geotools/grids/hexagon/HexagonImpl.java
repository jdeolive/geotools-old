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

package org.geotools.grids.hexagon;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Polygon;

import org.geotools.geometry.jts.JTSFactoryFinder;

/**
 * Default implementation of {@code Hexagon}.
 *
 * @author mbedward
 * @since 2.7
 * @source $URL: $
 * @version $Id: $
 */
public class HexagonImpl implements Hexagon {
    private static final double ROOT3 = Math.sqrt(3.0);

    private static final GeometryFactory geomFactory = JTSFactoryFinder.getGeometryFactory(null);

    private final double sideLen;
    private final double area;
    private final double minX;
    private final double minY;
    private final Orientation orientation;
    private Coordinate[] vertices;

    /**
     * Creates a new hexagon.
     *
     * @param sideLen the side length
     *
     * @param minX the min X ordinate of the bounding rectangle
     *
     * @param minY the min Y ordinate of the bounding rectangle
     *
     * @param orientation either {@code Hexagon.Orientation.FLAT} or
     *        {@code Hexagon.Orientation.ANGLED}
     */
    public HexagonImpl(double sideLen, double minX, double minY, Orientation orientation) {
        if (sideLen <= 0.0) {
            throw new IllegalArgumentException("side length must be > 0");
        }

        if (orientation == null) {
            throw new IllegalArgumentException("orientation must be a non-null value");
        }
        
        this.sideLen = sideLen;
        this.minX = minX;
        this.minY = minY;
        this.orientation = orientation;

        this.area = Hexagons.sideLenToArea(sideLen);
        calculateVertices();
    }

    /**
     * {@inheritDoc}
     */
    public double getSideLength() {
        return sideLen;
    }

    /**
     * {@inheritDoc}
     */
    public double getArea() {
        return area;
    }

    /**
     * {@inheritDoc}
     */
    public Orientation getOrientation() {
        return orientation;
    }

    /**
     * {@inheritDoc}
     *
     * @return an array of copies of the vertex {@code Coordinates}
     */
    public Coordinate[] getVertices() {
        Coordinate[] copy = new Coordinate[6];
        for (int i = 0; i < 6; i++) {
            copy[i] = new Coordinate(vertices[i]);
        }
        return copy;
    }

    /**
     * {@inheritDoc}
     */
    public Envelope getBounds() {
        if (orientation == Orientation.FLAT) {
            return new Envelope(minX, minX + 2.0 * sideLen, minY, minY + ROOT3 * sideLen);
        } else {  // ANGLED
            return new Envelope(minX, minX + ROOT3 * sideLen, minY, minY + 2.0 * sideLen);
        }
    }

    /**
     * {@inheritDoc}
     */
    public Coordinate getCenter() {
        if (orientation == Orientation.FLAT) {
            return new Coordinate(minX + sideLen, minY + ROOT3 * 0.5 * sideLen);
        } else {  // ANGLED
            return new Coordinate(minX + ROOT3 * 0.5 * sideLen, minY + sideLen);
        }
    }

    /**
     * {@inheritDoc}
     */
    public Polygon toPolygon() {
        Coordinate[] ring = new Coordinate[7];
        for (int i = 0; i < 6; i++) {
            ring[i] = vertices[i];
        }
        ring[6] = vertices[0];

        return geomFactory.createPolygon(geomFactory.createLinearRing(ring), null);
    }

    /**
     * Calculates vertex coordinates.
     */
    private void calculateVertices() {
        if (orientation == null) {
            throw new IllegalStateException(
                    "Orientation must be set before calculating vertices");
        }

        vertices = new Coordinate[6];

        final double span = ROOT3 * sideLen;
        if (orientation == Orientation.FLAT) {
            vertices[0] = new Coordinate(minX + 0.5 * sideLen, minY + span);
            vertices[1] = new Coordinate(minX + 1.5 * sideLen, minY + span);
            vertices[2] = new Coordinate(minX + 2.0 * sideLen, minY + span/2.0);
            vertices[3] = new Coordinate(minX + 1.5 * sideLen, minY);
            vertices[4] = new Coordinate(minX + 0.5 * sideLen, minY);
            vertices[5] = new Coordinate(minX, minY + span/2.0);

        } else { // Orientation.ANGLED
            vertices[0] = new Coordinate(minX + 0.5 * span, minY + 2.0 * sideLen);
            vertices[1] = new Coordinate(minX + span, minY + 1.5 * sideLen);
            vertices[2] = new Coordinate(minX + span, minY + 0.5 * sideLen);
            vertices[3] = new Coordinate(minX + 0.5 * span, minY);
            vertices[4] = new Coordinate(minX, minY + 0.5 * sideLen);
            vertices[5] = new Coordinate(minX, minY + 1.5 * sideLen);
        }
    }

}
