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
 * @version $Id: SubHandlerLinearRing.java,v 1.3 2002/05/03 16:38:31 ianturton Exp $
 */
public class SubHandlerLinearRing extends SubHandler {


		/** Internal coordinate list */
    private ArrayList coordinateList = new ArrayList();


    /** Creates a new instance of GMLLinearRingHandler */
    public SubHandlerLinearRing() {
    }

    /**
		 * Adds a coordinate to the LinearRing.
		 *
     * @param coordinate The coordinate to add to the LinearRing.
		 */    
    public void addCoordinate(Coordinate coordinate) {
        coordinateList.add(coordinate);
    }
    

    /**
		 * Determine whether or not this LinearRing is ready to be created.
		 *
     * @param message The current geometry type in the GML stream.
     * @return Ready for creation flag
     */    
    public boolean isComplete(String message){

				// makes sure that this LinearRing has more than one coordinate and its first and last are identical
				if( coordinateList.size() > 1 ) {
						Coordinate firstCoordinate = (Coordinate) coordinateList.get(0);
						Coordinate lastCoordinate = (Coordinate) coordinateList.get( coordinateList.size() - 1 );
						if( lastCoordinate.equals2D(firstCoordinate) ) { 
								return true;
						} else {
								return false;
						}
				} else { 
						return false; 
				}
    }


    /** 
		 * Create the LinearRing.
		 *
     * @param geometryFactory The geometry factory used for the build.
     * @return LinearRing geometry created.
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
