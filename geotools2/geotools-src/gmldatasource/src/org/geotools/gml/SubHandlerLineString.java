/*
 * GMLLineStringHandler.java
 *
 * Created on 06 March 2002, 10:31
 */

package org.geotools.gml;

import java.util.*;

import com.vividsolutions.jts.geom.*;


/** 
 * Creates a simple OGC LineString element.
 *
 * @author Ian Turton, CCG Leeds
 * @author Rob Hranac, Vision for New York
 * @version $Id: SubHandlerLineString.java,v 1.2 2002/04/12 18:51:59 robhranac Exp $
 */
public class SubHandlerLineString extends SubHandler {


		/** List of coordinates for LineString. */
    private ArrayList coordinateList = new ArrayList();


    /** Empty constructor. */
    public SubHandlerLineString() {}


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
     * @return Ready for creation flag
     */    
    public boolean isComplete(String message){

        if( coordinateList.size() > 1) { return true; }
				else                           { return false; }
    }
    

    /** 
		 * Create the LineString.
		 *
     * @param geometryFactory The geomerty factory needed to do the build.
     * @return JTS LineString geometry
     */    
    public Geometry create(GeometryFactory geometryFactory) {
        return geometryFactory.createLineString( (Coordinate[]) coordinateList.toArray(new Coordinate[]{}) );
    }
}
