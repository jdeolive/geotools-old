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
import java.util.logging.Logger;


/**
 * Creates a MultiPoint, MultiLineString, or MultiPolygon geometry as required
 * by the internal functions.
 *
 * @author Ian Turton, CCG
 * @author Rob Hranac, Vision for New York
 * @version $Id: SubHandlerMulti.java,v 1.6 2003/08/14 18:36:47 cholmesny Exp $
 */
public class SubHandlerMulti extends SubHandler {
    /** The logger for the GML module. */
    private static final Logger LOGGER = Logger.getLogger("org.geotools.gml");

    /**
     * Remembers the list of all possible sub (base) types for this multi type.
     */
    private static final Collection BASE_GEOMETRY_TYPES = new Vector(java.util.Arrays
            .asList(new String[] { "Point", "LineString", "Polygon" }));

    /** Geometry factory to return the multi type. */
    private GeometryFactory geometryFactory = new GeometryFactory();

    /** Handler factory to return the sub type. */
    private SubHandlerFactory handlerFactory = new SubHandlerFactory();

    /** Creates a SubHandler for the current sub type. */
    private SubHandler currentHandler;

    /** Stores list of all sub types. */
    private List geometries = new Vector();

    /** Remembers the current sub type (ie. Line, Polygon, Point). */
    private String internalType;

    /** Remembers whether or not the internal type is set already. */
    private boolean internalTypeSet = false;

    /**
     * Empty constructor.
     */
    public SubHandlerMulti() {
    }

    /**
     * Handles all internal (sub) geometries.
     *
     * @param message The sub geometry type found.
     * @param type Whether or not it is at a start or end.
     */
    public void subGeometry(String message, int type) {
        LOGGER.fine("subGeometry message = " + message + " type = " + type);

        // if the internal type is not yet set, set it
        if (!internalTypeSet) {
            if (BASE_GEOMETRY_TYPES.contains(message)) {
                internalType = message;
                internalTypeSet = true;
                LOGGER.fine("Internal type set to " + message);
            }
        }

        // if the internal type is already set, then either:
        // create a new handler, if at start of geometry, or
        // return the completed geometry, if at the end of it
        if (message.equals(internalType)) {
            if (type == GEOMETRY_START) {
                currentHandler = handlerFactory.create(internalType);
            } else if (type == GEOMETRY_END) {
                geometries.add(currentHandler.create(geometryFactory));
            } else if (type == GEOMETRY_SUB) {
                currentHandler.subGeometry(message, type);
            }
        } else {
            currentHandler.subGeometry(message, type);
            LOGGER.fine(internalType + " != " + message);
        }
    }

    /**
     * Adds a coordinate to the current internal (sub) geometry.
     *
     * @param coordinate The coordinate.
     */
    public void addCoordinate(Coordinate coordinate) {
        currentHandler.addCoordinate(coordinate);
    }

    /**
     * Determines whether or not it is time to return this geometry.
     *
     * @param message The geometry element that prompted this check.
     *
     * @return DOCUMENT ME!
     */
    public boolean isComplete(String message) {
        if (message.equals("Multi" + internalType)) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Returns a completed multi type.
     *
     * @param geometryFactory The factory this method should use to create the
     *        multi type.
     *
     * @return Appropriate multi geometry type.
     */
    public Geometry create(GeometryFactory geometryFactory) {
        if (internalType.equals("Point")) {
            MultiPoint mp = geometryFactory.createMultiPoint(geometryFactory
                    .toPointArray(geometries));
            LOGGER.fine("created " + mp);

            return mp;
        } else if (internalType.equals("LineString")) {
            MultiLineString ml = geometryFactory.createMultiLineString(geometryFactory
                    .toLineStringArray(geometries));
            LOGGER.fine("created " + ml);

            return ml;
        } else if (internalType.equals("Polygon")) {
            MultiPolygon mp = geometryFactory.createMultiPolygon(geometryFactory
                    .toPolygonArray(geometries));
            LOGGER.fine("created " + mp);

            return mp;
        } else {
            return null;
        }
    }
}
