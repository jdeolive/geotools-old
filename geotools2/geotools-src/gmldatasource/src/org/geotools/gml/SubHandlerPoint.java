/*
 * GMLPointHandler.java
 *
 * Created on 04 March 2002, 17:49
 */

package org.geotools.gml;

import org.geotools.gml.*;
import com.vividsolutions.jts.geom.*;

/**
 * Creates OGC simple point.
 *
 * @author Ian Turton, CCG Leeds
 * @author Rob Hranac, Vision for New York
 * @version $Id: SubHandlerPoint.java,v 1.1 2002/04/03 01:30:15 robhranac Exp $
 */
public class SubHandlerPoint extends SubHandler {


    Coordinate coordinate = null;


    /** Creates a new instance of GMLPointHandler */
    public SubHandlerPoint() {
    }


    /**
		 * set the coordinate for the point
		 *
     * @param coordinate the coordinate
     */    
    public void addCoordinate(Coordinate coordinate){

        this.coordinate = coordinate;
    }
    

    /**
		 * Determine whether or not this Point is ready to be created.
		 *
     * @return Ready for creation flag
     */    
    public boolean isComplete(String message){

        if( this.coordinate != null) { return true; }
				else                         { return false; }
    }
    

    /**
		 * build the point
		 *
     * @param geometryFactory geometry factroy to be used
     * @return the point
     */    
    public Geometry create(GeometryFactory geometryFactory){

        return geometryFactory.createPoint(coordinate);
    }
    
    
}
