/* Copyright (c) 2001, 2003 TOPP - www.openplans.org.  All rights reserved.
 * This code is licensed under the GPL 2.0 license, availible at the root
 * application directory.
 */
package org.geotools.graph;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;


public class GeometryUtil {
    private static GeometryFactory geomFactory;

    public static boolean isEqual(Coordinate[] c1, Coordinate[] c2) {
        return (isEqual(c1, c2, false));
    }

    public static boolean isEqual(Coordinate[] c1, Coordinate[] c2,
        boolean reverse) {
        if (c1.length != c2.length) {
            return (false);
        }

        if (!reverse) {
            for (int i = 0; i < c1.length; i++) {
                if (!c1[i].equals(c2[i])) {
                    return (false);
                }
            }

            return (true);
        }

        for (int i = 0; i < c1.length; i++) {
            if (!c1[i].equals(c2[c1.length - i - 1])) {
                return (false);
            }
        }

        return (true);
    }

    public static LineString joinLinestrings(LineString l1, LineString l2) {
        Coordinate[] merged = new Coordinate[(l1.getNumPoints()
            + l2.getNumPoints()) - 1];

        //linestrings could join in one of 4 ways:
        // tip to tail
        // tail to tip
        // tip to tip
        // tail to tail
        if (l1.getCoordinateN(l1.getNumPoints() - 1).equals(l2.getCoordinateN(0))) {
            //tip to tail
            for (int i = 0; i < l1.getNumPoints(); i++) {
                merged[i] = l1.getCoordinateN(i);
            }

            for (int i = 0; i < (l2.getNumPoints() - 1); i++) {
                merged[i + l1.getNumPoints()] = l2.getCoordinateN(i + 1);
            }
        } else if (l2.getCoordinateN(l2.getNumPoints() - 1).equals(l1
                    .getCoordinateN(0))) {
            //tail to tip
            for (int i = 0; i < l2.getNumPoints(); i++) {
                merged[i] = l2.getCoordinateN(i);
            }

            for (int i = 0; i < (l1.getNumPoints() - 1); i++) {
                merged[i + l2.getNumPoints()] = l1.getCoordinateN(i + 1);
            }
        } else if (l1.getCoordinateN(l1.getNumPoints() - 1).equals(l2
                    .getCoordinateN(l2.getNumPoints() - 1))) {
            //tip to tip  
            for (int i = 0; i < l1.getNumPoints(); i++) {
                merged[i] = l1.getCoordinateN(i);
            }

            for (int i = 0; i < (l2.getNumPoints() - 1); i++) {
                merged[i + l1.getNumPoints()] = l2.getCoordinateN(l2
                        .getNumPoints() - 2 - i);
            }
        } else if (l1.getCoordinateN(0).equals(l2.getCoordinateN(0))) {
            //tail to tail
            for (int i = 0; i < l2.getNumPoints(); i++) {
                merged[i] = l2.getCoordinateN(l2.getNumPoints() - 1 - i);
            }

            for (int i = 0; i < (l1.getNumPoints() - 1); i++) {
                merged[i + l2.getNumPoints()] = l1.getCoordinateN(i + 1);
            }
        } else {
            return (null);
        }

        return (gf().createLineString(merged));
    }

    public static Geometry reverseGeometry(Geometry geometry) {
        if (geometry instanceof Point) {
            return (geometry);
        }

        if (geometry instanceof LineString) {
            return (gf().createLineString(reverseCoordinates(
                    geometry.getCoordinates())));
        }

        return (null);
    }

    public static Envelope expand(Envelope e, double value) {
        return (new Envelope(e.getMinX() + value, e.getMaxX() + value,
            e.getMinY() + value, e.getMaxY() + value));
    }

    public static Coordinate[] reverseCoordinates(Coordinate[] c) {
        int n = c.length;
        Coordinate[] reversed = new Coordinate[n];

        for (int i = 0; i < n; i++)
            reversed[i] = c[n - i - 1];

        return (reversed);
    }

    private static GeometryFactory gf() {
        if (geomFactory == null) {
            geomFactory = new GeometryFactory();
        }

        return (geomFactory);
    }
}
