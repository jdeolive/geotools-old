/*
 * GMLLinearRingHandler.java
 *
 * Created on 06 March 2002, 10:40
 */

package org.geotools.gml;

import java.util.*;
import com.vividsolutions.jts.geom.*;
import org.geotools.gml.*;

/** 
 * Creates a simple OGC LinearRing (a closed LineString).
 * 
 * @author Ian Turton, CCG Leeds
 * @author Rob Hranac, Vision for New York
 * @version $Id: SubHandlerLinearRing.java,v 1.1 2002/04/03 01:30:15 robhranac Exp $
 */
public class SubHandlerLinearRing extends SubHandler {


		/** Internal coordinate list */
    private ArrayList coordinateList = new ArrayList();


    /** Creates a new instance of GMLLinearRingHandler */
    public SubHandlerLinearRing() {
    }

    /**
		 *
     * @param coordinate
		 */    
    public void addCoordinate(Coordinate coordinate) {
        coordinateList.add(coordinate);
    }
    

    /**
		 * Determine whether or not this Point is ready to be created.
     * @return Ready for creation flag
     */    
    public boolean isComplete(String message){



				if( coordinateList.size() > 1 ) {

						Coordinate firstCoordinate = (Coordinate) coordinateList.get(0);
						Coordinate lastCoordinate = (Coordinate) coordinateList.get( coordinateList.size() - 1 );
						if( lastCoordinate.equals(lastCoordinate) ) { 
								return true;
						}
						else {
								return false;
						}
				}
				else { 
						return false; 
				}
    }


    /** 
		 * build the linearRing
		 *
     * @param geometryFactory geometry factory used for the build
     * @return geomerty of the linearring
     */    
    public Geometry create(GeometryFactory geometryFactory) {
        try{
            return geometryFactory.createLinearRing((Coordinate[])coordinateList.toArray(new Coordinate[]{}));
        } catch(TopologyException e){
            System.err.println("Caught Topology exception in GMLLinearRingHandler");
            return null;
        }
    }
    
}
