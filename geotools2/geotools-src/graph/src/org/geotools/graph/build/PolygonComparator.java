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

import com.vividsolutions.jts.geom.Polygon;
import org.geotools.feature.Feature;


/**
 * An implentation of FeatureComparator used to compare Polygons.
 */
public class PolygonComparator extends FeatureComparator {
    /**
     * Implements the touches relationship
     * 
     * <p>
     * Touches is a bidirecitonal relationship - so the order in which the
     * features are added into the graph should not effect the resulting
     * outcome.
     * </p>
     *
     * @param f1 DOCUMENT ME!
     * @param f2 DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public int compare(Feature f1, Feature f2) {
        Polygon p1 = (Polygon) f1.getDefaultGeometry();
        Polygon p2 = (Polygon) f2.getDefaultGeometry();

        if (p1.touches(p2)) {
            return (1);
        }

        return (NO_RELATIONSHIP);
    }
}
