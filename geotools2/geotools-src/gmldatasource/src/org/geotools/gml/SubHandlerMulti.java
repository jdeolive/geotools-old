/*
 *    Geotools - OpenSource mapping toolkit
 *    (C) 2002, Centre for Computational Geography
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
 *    You should have received a copy of the GNU Lesser General Public
 *    License along with this library; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *    
 */

package org.geotools.gml;

import java.util.*;
import com.vividsolutions.jts.geom.*;

/** 
 * Creates a MultiPoint, MultiLineString, or MultiPolygon geometry as required
 * by the internal functions.
 *
 * @version $Id: SubHandlerMulti.java,v 1.4 2002/07/12 17:11:24 loxnard Exp $
 * @author Ian Turton, CCG
 * @author Rob Hranac, Vision for New York
  */
public class SubHandlerMulti extends SubHandler {


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
                * Remembers the list of all possible sub (base) types for this
                *  multi type.
                */    
                private static final Collection BASE_GEOMETRY_TYPES = new Vector(java.util.Arrays.asList(new String[] {"Point", "LineString", "Polygon"}));


    /** Empty constructor. */
    public SubHandlerMulti() {}
    

    /** 
     * Handles all internal (sub) geometries.
     *
     * @param message The sub geometry type found.
     * @param type Whether or not it is at a start or end.
     */    
    public void subGeometry(String message, int type) {
        
        // if the internal type is not yet set, set it
        if (!internalTypeSet) {
            if (BASE_GEOMETRY_TYPES.contains(message)) {
                internalType = message;
                internalTypeSet = true;
            }
        }
        
        // if the internal type is already set, then either:
        // create a new handler, if at start of geometry, or
        // return the completed geometry, if at the end of it
        if (message.equals(internalType)) {
            if (type == GEOMETRY_START) {
                currentHandler = handlerFactory.create(internalType);
            }
            else if (type == GEOMETRY_END) {
                geometries.add(currentHandler.create(geometryFactory));
            }
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
     */    
    public boolean isComplete(String message) {
        
        if (message.equals("Multi" + internalType)) {
            return true;
        }
        else {
            return false;
        }
    }

    
    /** 
     * Returns a completed multi type.
     *
     * @param geometryFactory The factory this method should use to create
     * the multi type. 
     * @return Appropriate multi geometry type.
     */    
    public Geometry create(GeometryFactory geometryFactory) {
        
        if (internalType.equals("Point")) {
            return geometryFactory.createMultiPoint(geometryFactory.toPointArray(geometries));
        }
        else if (internalType.equals("LineString")) {
            return geometryFactory.createMultiLineString(geometryFactory.toLineStringArray(geometries));
        }
        else if (internalType.equals("Polygon")) {
            return geometryFactory.createMultiPolygon(geometryFactory.toPolygonArray(geometries));
        }
        else {
            return null;
        }
    }
    
    
}
