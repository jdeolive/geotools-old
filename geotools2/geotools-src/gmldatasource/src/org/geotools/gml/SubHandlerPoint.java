/*
 * GMLPointHandler.java
 *
 * Created on 04 March 2002, 17:49
 */

package org.geotools.gml;

import org.geotools.gml.*;
import com.vividsolutions.jts.geom.*;

/**
 * Creates an OGC simple point.
 *
 * @author Ian Turton, CCG Leeds
 * @author Rob Hranac, Vision for New York
 * @version $Id: SubHandlerPoint.java,v 1.2 2002/04/12 18:51:59 robhranac Exp $
 */
public class SubHandlerPoint extends SubHandler {


		/** The coordinate of the point. */
    Coordinate coordinate = null;


    /** Creates a new instance of GMLPointHandler. */
    public SubHandlerPoint() {}


    /**
		 * Set the coordinate for the point.
		 *
     * @param coordinate Coordinate.
     */    
    public void addCoordinate(Coordinate coordinate){

        this.coordinate = coordinate;
    }
    

    /**
		 * Determine whether or not this Point is ready to be created.
		 *
     * @param message GML element that prompted this query.
     * @return Ready for creation flag
     */    
    public boolean isComplete(String message){

        if( this.coordinate != null) { return true; }
				else                         { return false; }
    }
    

    /**
		 * Generate the point.
		 *
     * @param geometryFactory Geometry factory to be used to create the point.
     * @return Created Point.
     */    
    public Geometry create(GeometryFactory geometryFactory){

        return geometryFactory.createPoint(coordinate);
    }
    
    
}
