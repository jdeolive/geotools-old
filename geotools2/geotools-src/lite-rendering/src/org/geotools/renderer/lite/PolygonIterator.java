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
package org.geotools.renderer.lite;

import com.vividsolutions.jts.geom.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.PathIterator;


/**
 * A path iterator for the LiteShape class, specialized to iterate over Polygon
 * objects.
 *
 * @author Andrea Aime
 * @version $Id: PolygonIterator.java,v 1.4 2003/07/12 10:56:42 aaime Exp $
 */
class PolygonIterator implements PathIterator {
    private AffineTransform at;
    private LineString[] rings;
    private int currentRing = 0;
    private int currentCoord = 0;
    private Coordinate[] coords = null;
    private Coordinate oldCoord = null;
    private boolean done = false;
    private boolean generalize = true;
    private double maxDistance = 1.0;
    private double xScale;
    private double yScale;

    /**
     * Creates a new instance of JTSPolygonIterator
     *
     * @param p The polygon whose boundary is to be iterated
     * @param at The affine trasform applied on points during iteration
     */
    public PolygonIterator(com.vividsolutions.jts.geom.Polygon p,
        AffineTransform at) {
        int numInteriorRings = p.getNumInteriorRing();
        rings = new LineString[numInteriorRings + 1];
        rings[0] = p.getExteriorRing();

        for (int i = 0; i < numInteriorRings; i++) {
            rings[i + 1] = p.getInteriorRingN(i);
        }

        if (at == null) {
            at = new AffineTransform();
        }

        this.at = at;
        xScale = Math.sqrt((at.getScaleX() * at.getScaleX()) +
                (at.getShearX() * at.getShearX()));
        yScale = Math.sqrt((at.getScaleY() * at.getScaleY()) +
                (at.getShearY() * at.getShearY()));

        coords = rings[0].getCoordinates();
    }

    public PolygonIterator(com.vividsolutions.jts.geom.Polygon p,
        AffineTransform at, boolean generalize) {
        this(p, at);
        this.generalize = generalize;
    }

    public PolygonIterator(com.vividsolutions.jts.geom.Polygon p,
        AffineTransform at, boolean generalize, double maxDistance) {
        this(p, at, generalize);
        this.maxDistance = maxDistance;
    }

    public void setMaxDistance(double distance) {
        maxDistance = distance;
    }

    public double getMaxDistance(double distance) {
        return maxDistance;
    }

    /**
     * Returns the coordinates and type of the current path segment in the
     * iteration. The return value is the path-segment type: SEG_MOVETO,
     * SEG_LINETO, SEG_QUADTO, SEG_CUBICTO, or SEG_CLOSE. A double array of
     * length 6 must be passed in and can be used to store the coordinates of
     * the point(s). Each point is stored as a pair of double x,y coordinates.
     * SEG_MOVETO and SEG_LINETO types returns one point, SEG_QUADTO returns
     * two points, SEG_CUBICTO returns 3 points and SEG_CLOSE does not return
     * any points.
     *
     * @param coords an array that holds the data returned from this method
     *
     * @return the path-segment type of the current path segment.
     *
     * @see #SEG_MOVETO
     * @see #SEG_LINETO
     * @see #SEG_QUADTO
     * @see #SEG_CUBICTO
     * @see #SEG_CLOSE
     */
    public int currentSegment(double[] coords) {
        if (currentCoord == 0) {
            coords[0] = this.coords[0].x;
            coords[1] = this.coords[0].y;
            at.transform(coords, 0, coords, 0, 1);

            return SEG_MOVETO;
        } else if (currentCoord == this.coords.length) {
            return SEG_CLOSE;
        } else {
            coords[0] = this.coords[currentCoord].x;
            coords[1] = this.coords[currentCoord].y;
            at.transform(coords, 0, coords, 0, 1);

            return SEG_LINETO;
        }
    }

    /**
     * Returns the coordinates and type of the current path segment in the
     * iteration. The return value is the path-segment type: SEG_MOVETO,
     * SEG_LINETO, SEG_QUADTO, SEG_CUBICTO, or SEG_CLOSE. A float array of
     * length 6 must be passed in and can be used to store the coordinates of
     * the point(s). Each point is stored as a pair of float x,y coordinates.
     * SEG_MOVETO and SEG_LINETO types returns one point, SEG_QUADTO returns
     * two points, SEG_CUBICTO returns 3 points and SEG_CLOSE does not return
     * any points.
     *
     * @param coords an array that holds the data returned from this method
     *
     * @return the path-segment type of the current path segment.
     *
     * @see #SEG_MOVETO
     * @see #SEG_LINETO
     * @see #SEG_QUADTO
     * @see #SEG_CUBICTO
     * @see #SEG_CLOSE
     */
    public int currentSegment(float[] coords) {
        double[] dcoords = new double[2];
        int result = currentSegment(dcoords);
        coords[0] = (float) dcoords[0];
        coords[1] = (float) dcoords[1];

        return result;
    }

    /**
     * Return the winding rule for determining the interior of the path.
     *
     * @return <code>WIND_EVEN_ODD</code> by default.
     */
    public int getWindingRule() {
        return WIND_EVEN_ODD;
    }

    /**
     * Tests if the iteration is complete.
     *
     * @return <code>true</code> if all the segments have been read;
     *         <code>false</code> otherwise.
     */
    public boolean isDone() {
        return done;
    }

    /**
     * Moves the iterator to the next segment of the path forwards along the
     * primary direction of traversal as long as there are more points in that
     * direction.
     */
    public void next() {
        if (currentCoord == coords.length) {
            if (currentRing < (rings.length - 1)) {
                currentCoord = 0;
                currentRing++;
                coords = rings[currentRing].getCoordinates();
            } else {
                done = true;
            }
        } else {
            if (generalize) {
                if (oldCoord == null) {
                    currentCoord++;
                    oldCoord = coords[currentCoord];
                } else {
                    double distx = 0;
                    double disty = 0;

                    do {
                        currentCoord++;

                        if (currentCoord < coords.length) {
                            distx = Math.abs(coords[currentCoord].x -
                                    oldCoord.x);
                            disty = Math.abs(coords[currentCoord].y -
                                    oldCoord.y);
                        }
                    } while (((distx * xScale) < maxDistance) &&
                            ((disty * yScale) < maxDistance) &&
                            (currentCoord < coords.length));

                    if (currentCoord < coords.length) {
                        oldCoord = coords[currentCoord];
                    } else {
                        oldCoord = null;
                    }
                }
            } else {
                currentCoord++;
            }
        }
    }
}
