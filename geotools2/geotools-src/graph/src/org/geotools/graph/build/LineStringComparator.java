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
package org.geotools.graph.build;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.LineString;
import org.geotools.feature.Feature;


/**
 * An implentation of FeatureComparator used to compare Line Strings.
 */
public class LineStringComparator extends FeatureComparator {
    /** DOCUMENT ME! */
    public static final int TIP_TO_TAIL = 0;

    /** DOCUMENT ME! */
    public static final int TAIL_TO_TIP = 1;

    /** DOCUMENT ME! */
    public static final int TIP_TO_TIP = 2;

    /** DOCUMENT ME! */
    public static final int TAIL_TO_TAIL = 3;

    /**
     * Creates a new LineStringComparator object.
     */
    public LineStringComparator() {
    }

    /**
     * DOCUMENT ME!
     *
     * @param f1 DOCUMENT ME!
     * @param f2 DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public int compare(Feature f1, Feature f2) {
        LineString ls1 = (LineString) f1.getDefaultGeometry();
        LineString ls2 = (LineString) f2.getDefaultGeometry();

        Coordinate f1first = ls1.getCoordinateN(0);
        Coordinate f1last = ls1.getCoordinateN(ls1.getNumPoints() - 1);
        Coordinate f2first = ls2.getCoordinateN(0);
        Coordinate f2last = ls2.getCoordinateN(ls2.getNumPoints() - 1);

        if (f1last.equals2D(f2first)) {
            return (TIP_TO_TAIL);
        }

        if (f1first.equals2D(f2last)) {
            return (TAIL_TO_TIP);
        }

        if (f1last.equals2D(f2last)) {
            return (TIP_TO_TIP);
        }

        if (f1first.equals2D(f2first)) {
            return (TAIL_TO_TAIL);
        }

        return (-1); //no relationship
    }
}
