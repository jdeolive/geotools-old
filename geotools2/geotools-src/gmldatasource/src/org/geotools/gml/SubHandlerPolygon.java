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
 * @version $Id: SubHandlerPolygon.java,v 1.2 2002/04/12 18:51:59 robhranac Exp $
 */
public class SubHandlerPolygon extends SubHandler {


    /** Factory for creating the Polygon geometry. */    
    private GeometryFactory geometryFactory = new GeometryFactory();

    /** Handler for the LinearRings that comprise the Polygon. */    
    private SubHandlerLinearRing currentHandler = new SubHandlerLinearRing();

    /** Stores Polygon's outer boundary (shell) */    
    private LinearRing outerBoundary = null;

    /** Stores Polygon's inner boundaries (holes) */    
    private ArrayList innerBoundaries = new ArrayList();

		/** Remember the current location in the parsing stream (inner or outer boundary) */
		private int location = 0;
		/** Indicates that we are inside the inner boundary of the Polygon. */
		private int INNER_BOUNDARY = 1;
		/** Indicates that we are inside the outer boundary of the Polygon. */
		private int OUTER_BOUNDARY = 2;


    /** Creates a new instance of GMLPolygonHandler */
    public SubHandlerPolygon() {}
    

    /** 
		 * Catch inner and outer LinearRings messages and handle them appropriately.
		 *
     * @param message Name of sub geometry located.
     * @param type Type of sub geometry located.
     */    
    public void subGeometry(String message, int type) {

				// if we have found a linear ring, either
				//  add it to the list of inner boundaries if we are reading them and at the end of the LinearRing
				//  add it to the outer boundary if we are reading it and at the end of the LinearRing
				//  create a new linear ring, if we are at the start of a new linear ring
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

				//  or, if we are getting notice of an inner/outer boundary marker, set current location appropriately
				else if( message.equals("outerBoundaryIs") ) {
						location = OUTER_BOUNDARY;
				}
				else if( message.equals("innerBoundaryIs") ) {
						location = INNER_BOUNDARY;
				}

    }
    
    /** 
		 * Add a coordinate to the current LinearRing.
		 * 
     * @param coordinate Name of sub geometry located.
     */    
    public void addCoordinate(Coordinate coordinate) {
				currentHandler.addCoordinate(coordinate);
    }

    
    /** 
		 * Determines whether or not the geometry is ready to be returned.
		 *
     * @param message Name of GML element that prompted this check.
     * @return Flag indicating whether or not the geometry is ready to be returned.
     */    
    public boolean isComplete(String message) {

				// the conditions checked here are that the endGeometry message that prompted this check is a Polygon
				//  and that this Polygon has an outer boundary; if true,
				//  then return the all go signal
				if( message.equals("Polygon") ) { 
						if( outerBoundary != null ) {  
								return true; 
						}
						else { 
								return false; 
						}
				}
				
				// otherwise, send this message to the subGeometry method for further processing
				else {
						this.subGeometry(message, GEOMETRY_END); 
						return false;
				}
    }

    
    /** 
		 * Return the completed OGC Polygon.
		 * 
     * @param geometryFactory Geometry factory to be used in Polygon creation.
     * @return Completed OGC Polygon.
     */    
    public Geometry create(GeometryFactory geometryFactory) {
        
        return geometryFactory.createPolygon(outerBoundary,(LinearRing[])innerBoundaries.toArray(new LinearRing[0]));
		}

    
}
