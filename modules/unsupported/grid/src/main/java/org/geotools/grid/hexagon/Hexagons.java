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

import java.util.HashMap;
import java.util.Map;

import com.vividsolutions.jts.geom.Envelope;

import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.feature.FeatureCollections;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.grid.GridFeatureBuilder;
import org.geotools.grid.GridElement;
import org.geotools.grid.Neighbor;
import org.geotools.grid.hexagon.Hexagon.Orientation;

import org.opengis.referencing.crs.CoordinateReferenceSystem;

/**
 * A utilities class with static methods to create and work with hexagonal
 * grid elements.
 *
 * @author mbedward
 * @since 2.7
 * @source $URL$
 * @version $Id$
 */
public class Hexagons {

    private static final double ROOT3 = Math.sqrt(3.0);

    /**
     * Calculates the area of a hexagon with the given side length.
     *
     * @param sideLen side length
     *
     * @return the area
     *
     * @throws IllegalArgumentException if {@code sideLen} is not greater than zero
     */
    public static double sideLengthToArea(double sideLen) {
        if (sideLen <= 0.0) {
            throw new IllegalArgumentException("side length must be > 0");
        }
        return sideLen * sideLen * 1.5 * ROOT3;
    }

    /**
     * Calculates the side length of a hexagon with the given area.
     *
     * @param area the area
     *
     * @return the side length
     *
     * @throws IllegalArgumentException if {@code area} is not greater than zero
     */
    public static double areaToSideLength(double area) {
        if (area <= 0.0) {
            throw new IllegalArgumentException("area must be > 0");
        }
        return Math.sqrt(area * 2.0 / 3.0 / ROOT3);
    }

    /**
     * Creates a new {@code Hexagon} object.
     *
     * @param minX the min X ordinate of the bounding rectangle
     *
     * @param minY the min Y ordinate of the bounding rectangle
     *
     * @param sideLen the side length
     *
     * @param orientation either {@code Hexagon.Orientation.FLAT} or
     *        {@code Hexagon.Orientation.ANGLED}
     *
     * @param crs the coordinate reference system (may be {@code null})
     *
     * @return a new {@code Hexagon} object
     *
     * @throws IllegalArgumentException if {@code sideLen} is {@code <=} 0 or
     *         if {@code orientation} is {@code null}
     */
    public static Hexagon create(double minX, double minY, double sideLen,
            Orientation orientation, CoordinateReferenceSystem crs) {
        return new HexagonImpl(minX, minY, sideLen, orientation, crs);
    }

    /**
     * Creates a new {@code Hexagon} positioned at the given neighbor position
     * relative to the reference element.
     *
     * @param el the reference hexagon
     *
     * @param neighbor a valid neighbour position given the reference hexagon's
     *        orientation
     *
     * @return a new {@code Hexagon} object
     *
     * @throws IllegalArgumentException if either argument is {@code null} or
     *         if {@code el} is not an instance of {@code Hexagon} or
     *         if the neighbor position is not valid for the reference hexagon's
     *         orientation
     *
     * @see #isValidNeighbor(Hexagon.Orientation, Hexagon.Neighbor)
     */
    public static Hexagon createNeighbor(GridElement el, Neighbor neighbor) {
        if (el == null || neighbor == null) {
            throw new IllegalArgumentException(
                    "el and neighbour position must both be non-null");
        }

        if (!(el instanceof Hexagon)) {
            throw new IllegalArgumentException("el must be an instance of Hexagon");
        }

        Hexagon hexagon = (Hexagon) el;

        if (!isValidNeighbor(hexagon.getOrientation(), neighbor)) {
            throw new IllegalArgumentException(
                    neighbor + " is not a valid neighbour position for orientation " +
                    hexagon.getOrientation());
        }

        ReferencedEnvelope bounds = hexagon.getBounds();
        double dx, dy;

        switch (neighbor) {
            case LEFT:
                dx = -bounds.getWidth();
                dy = 0.0;
                break;

            case LOWER:
                dx = 0.0;
                dy = -bounds.getHeight();
                break;

            case LOWER_LEFT:
                if (hexagon.getOrientation() == Hexagon.Orientation.FLAT) {
                    dx = -0.75 * bounds.getWidth();
                    dy = -0.5 * bounds.getHeight();
                } else {  // ANGLED
                    dx = -0.5 * bounds.getWidth();
                    dy = -0.75 * bounds.getHeight();
                }
                break;

            case LOWER_RIGHT:
                if (hexagon.getOrientation() == Hexagon.Orientation.FLAT) {
                    dx = 0.75 * bounds.getWidth();
                    dy = -0.5 * bounds.getHeight();
                } else {  // ANGLED
                    dx = 0.5 * bounds.getWidth();
                    dy = -0.75 * bounds.getHeight();
                }
                break;

            case RIGHT:
                dx = bounds.getWidth();
                dy = 0.0;
                break;

            case UPPER:
                dx = 0.0;
                dy = bounds.getHeight();
                break;

            case UPPER_LEFT:
                if (hexagon.getOrientation() == Hexagon.Orientation.FLAT) {
                    dx = -0.75 * bounds.getWidth();
                    dy = 0.5 * bounds.getHeight();
                } else {  // ANGLED
                    dx = -0.5 * bounds.getWidth();
                    dy = 0.75 * bounds.getHeight();
                }
                break;

            case UPPER_RIGHT:
                if (hexagon.getOrientation() == Hexagon.Orientation.FLAT) {
                    dx = 0.75 * bounds.getWidth();
                    dy = 0.5 * bounds.getHeight();
                } else {  // ANGLED
                    dx = 0.5 * bounds.getWidth();
                    dy = 0.75 * bounds.getHeight();
                }
                break;

            default:
                throw new IllegalArgumentException("Unrecognized value for neighbor");
        }

        return create(bounds.getMinX() + dx, bounds.getMinY() + dy,
                hexagon.getSideLength(), hexagon.getOrientation(),
                bounds.getCoordinateReferenceSystem());
    }

    /**
     * Tests whether a neighbor position is valid for a given orientation.
     * Since the {@code Hexagon} class is intended to work within a grid
     * (ie. a perfect tesselation) some combinations of neighbour position
     * and hexagon orientation are invalid. For example, a {@code FLAT}
     * hexagon does not have a {@code LEFT}, rather it has {@code UPPER_LEFT}
     * and {@code LOWER_LEFT}.
     *
     * @param orientation hexagon orientation
     *
     * @param neighbor neighbor position
     *
     * @return {@code true} if the combination is valid; {@code false} otherwise
     */
    public static boolean isValidNeighbor(Orientation orientation, Neighbor neighbor) {
        switch (neighbor) {
            case LEFT:
            case RIGHT:
                return orientation == Hexagon.Orientation.ANGLED;

            case LOWER:
            case UPPER:
                return orientation == Hexagon.Orientation.FLAT;

            case LOWER_LEFT:
            case LOWER_RIGHT:
            case UPPER_LEFT:
            case UPPER_RIGHT:
                return true;

            default:
                throw new IllegalArgumentException("Invalid value for neighbor");
        }
    }

    /**
     * Creates a new grid of tesselated hexagons within a bounding rectangle.
     *
     * @param bounds the bounding rectangle
     *
     * @param sideLen hexagon side length
     *
     * @param orientation hexagon orientation
     *
     * @param setter an instance of {@code GridFeatureBuilder}
     *
     * @return a new grid
     */
    public static SimpleFeatureCollection createGrid(
            ReferencedEnvelope bounds,
            double sideLen,
            Orientation orientation,
            GridFeatureBuilder setter) {
        
        final SimpleFeatureCollection fc = FeatureCollections.newCollection();
        final SimpleFeatureBuilder builder = new SimpleFeatureBuilder(setter.getType());
        String geomPropName = setter.getType().getGeometryDescriptor().getLocalName();
        
        final double ySpan = orientation == Orientation.ANGLED ? 2.0 * sideLen : Math.sqrt(3.0) * sideLen; 
        final double xSpan = orientation == Orientation.ANGLED ? Math.sqrt(3.0) * sideLen : 2.0 * sideLen; 

        Hexagon h0 = create(bounds.getMinX(), bounds.getMinY(), sideLen, 
                orientation, bounds.getCoordinateReferenceSystem());
        Hexagon h = h0;
        
        Neighbor[] nextX = new Neighbor[2];
        Neighbor[] nextY = new Neighbor[2];
        if (orientation == Orientation.ANGLED) {
            nextX[0] = nextX[1] = Neighbor.RIGHT;
            nextY[0] = Neighbor.UPPER_RIGHT;
            nextY[1] = Neighbor.UPPER_LEFT;

        } else {  // FLAT
            nextX[0] = Neighbor.LOWER_RIGHT;
            nextX[1] = Neighbor.UPPER_RIGHT;
            nextY[0] = nextY[1] = Neighbor.UPPER;
        }
        
        int xIndex = 0;
        int yIndex = 0;

        while (h.getBounds().getMinY() <= bounds.getMaxY()) {
            while (h.getBounds().getMaxX() <= bounds.getMaxX()) {
                if (((Envelope)bounds).contains(h.getBounds())) {
                    Map<String, Object> attrMap = new HashMap<String, Object>();
                    setter.setAttributes(h, attrMap);

                    builder.set(geomPropName, h.toPolygon());
                    for (String propName : attrMap.keySet()) {
                        builder.set(propName, attrMap.get(propName));
                    }

                    fc.add(builder.buildFeature(setter.getFeatureID(h)));
                }

                h = createNeighbor(h, nextX[xIndex]);
                xIndex = (xIndex + 1) % 2;
            }

            h0 = createNeighbor(h0, nextY[yIndex]);
            h = h0;
            yIndex = (yIndex + 1) % 2;
            xIndex = 0;
        }

        return fc;
    }

}
