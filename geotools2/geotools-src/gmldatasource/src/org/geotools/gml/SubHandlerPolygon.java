/*
 * GMLPolygonHandler.java
 *
 * Created on 06 March 2002, 10:36
 */

package org.geotools.gml;

import java.util.*;
import com.vividsolutions.jts.geom.*;
import org.geotools.gml.*;

/** 
 * Creates a Polygon geometry.
 *
 * @author Ian Turton, CCG Leeds
 * @author Rob Hranac, Vision for New York
 * @version $Id: SubHandlerPolygon.java,v 1.1 2002/04/03 01:30:15 robhranac Exp $
 */
public class SubHandlerPolygon extends SubHandler {


    /** Creates a sub-handler for the current linear ring */    
    private GeometryFactory geometryFactory = new GeometryFactory();

    /** Creates a sub-handler for the current linear ring */    
    private SubHandlerLinearRing currentHandler = new SubHandlerLinearRing();

    /** Stores polygons outer boundary (shell) */    
    private LinearRing outerBoundary = null;

    /** Stores polygons inner boundaries (holes) */    
    private ArrayList innerBoundaries = new ArrayList();

		/** Remember */
		private int location = 0;

		/** Remember */
		private int INNER_BOUNDARY = 1;

		/** Remember */
		private int OUTER_BOUNDARY = 2;


    /** Checks the clockwisness of linearrings */    
    /*protected static CGAlgorithms cga = new RobustCGAlgorithms();*/


    /** Creates a new instance of GMLPolygonHandler */
    public SubHandlerPolygon() {
    }
    

    /** 
		 * Add linearRings
     * anticlockwise for outer ring, clockwise for holes
     * @param g linearRing to be added
     */    
    public void subGeometry(String message, int type) {

        if( message.equals("LinearRing") ) {
						if( type == GEOMETRY_END ) {
								if( location == INNER_BOUNDARY ) {										
										innerBoundaries.add( currentHandler.create(geometryFactory) );
								}
								else if( location == OUTER_BOUNDARY ) {
										outerBoundary = (LinearRing) currentHandler.create(geometryFactory);
								}
						}
						else if ( type == GEOMETRY_START ) {
								currentHandler = new SubHandlerLinearRing();
						}
				}
				else if( message.equals("outerBoundaryIs") ) {
						location = OUTER_BOUNDARY;
				}
				else if( message.equals("innerBoundaryIs") ) {
						location = INNER_BOUNDARY;
				}

    }
    
    /** 
		 * not used
     */    
    public void addCoordinate(Coordinate coordinate) {
				currentHandler.addCoordinate(coordinate);
    }

    
    /** 
		 * not used
     */    
    public boolean isComplete(String message) {

				if( message.equals("Polygon") ) { 
						if( outerBoundary != null ) {  
								return true; 
						}
						else { 
								return false; 
						}
				}
				else {
						this.subGeometry(message, GEOMETRY_END); 
						return false;
				}
    }

    
    /** 
		 * Return the polygon
     * @param geometryFactory geometry factory to be used
     * @return polygon
     */    
    public Geometry create(GeometryFactory geometryFactory) {
        
        return geometryFactory.createPolygon(outerBoundary,(LinearRing[])innerBoundaries.toArray(new LinearRing[0]));
		}

    
}
