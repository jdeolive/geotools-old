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
package org.geotools.gml;

import com.vividsolutions.jts.geom.*;
import java.util.*;


/**
 * Creates a simple OGC LineString element.
 *
 * @author Ian Turton, CCG
 * @author Rob Hranac, Vision for New York
 * @version $Id: SubHandlerLineString.java,v 1.6 2003/08/14 18:36:47 cholmesny Exp $
 */
public class SubHandlerLineString extends SubHandler {
    /** List of coordinates for LineString. */
    private ArrayList coordinateList = new ArrayList();

    /**
     * Empty constructor.
     */
    public SubHandlerLineString() {
    }

    /**
     * Adds a coordinate to the LineString.
     *
     * @param coordinate Coordinate to add to LineString.
     */
    public void addCoordinate(Coordinate coordinate) {
        coordinateList.add(coordinate);
    }

    /**
     * Determine whether or not this LineString is ready to be created.
     *
     * @param message The geometry type.
     *
     * @return Ready for creation flag.
     */
    public boolean isComplete(String message) {
        if (coordinateList.size() > 1) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Create the LineString.
     *
     * @param geometryFactory The geometry factory needed to do the build.
     *
     * @return JTS LineString geometry.
     */
    public Geometry create(GeometryFactory geometryFactory) {
        return geometryFactory.createLineString((Coordinate[]) coordinateList
            .toArray(new Coordinate[] {  }));
    }
}
