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
 * Creates a MultiPoint geometry.
 *
 * @author Ian Turton, CCG Leeds
 * @author Rob Hranac, Vision for New York
 * @version $Id: SubHandlerMulti.java,v 1.1 2002/04/03 01:30:15 robhranac Exp $
 */
public class SubHandlerMulti extends SubHandler {


    /** Creates a sub-handler for the current linear ring */    
    private GeometryFactory geometryFactory = new GeometryFactory();

    /** Creates a sub-handler for the current linear ring */    
    private SubHandlerFactory handlerFactory = new SubHandlerFactory();

    /** Creates a sub-handler for the current linear ring */    
    private SubHandler currentHandler;

    /** Stores polygons inner boundaries (holes) */    
    private List geometries = new Vector();

    /** Stores polygons inner boundaries (holes) */    
    private String internalType;

    /** Stores polygons inner boundaries (holes) */    
    private boolean internalTypeSet = false;

    /** Stores polygons inner boundaries (holes) */    
		private static final Collection BASE_GEOMETRY_TYPES = new Vector( java.util.Arrays.asList(new String[] {"Point","LineString","Polygon"}) );


    /** Creates a new instance of GMLPolygonHandler */
    public SubHandlerMulti() {
    }
    

    /** 
		 * Add linearRings anticlockwise for outer ring, clockwise for holes
     * @param message linearRing to be added
     * @param type linearRing to be added
     */    
    public void subGeometry(String message, int type) {

				if( !internalTypeSet ) {
						if( BASE_GEOMETRY_TYPES.contains(message) ) {
								internalType = message;
								internalTypeSet = true;
						}						
				}
				
        if( message.equals(internalType) ) {
						if( type == GEOMETRY_START ) {
								currentHandler = handlerFactory.create(internalType);
						}
						else if( type == GEOMETRY_END ) {
								geometries.add( currentHandler.create(geometryFactory) );
						}
				}
    }
    

    /** 
		 * not used
     * @param coordinate linearRing to be added
     */    
    public void addCoordinate(Coordinate coordinate) {
				currentHandler.addCoordinate(coordinate);
    }

    
    /** 
		 * not used
     */    
    public boolean isComplete(String message) {

				if( message.equals("Multi" + internalType) ) { 
						return true; 
				}
				else {
						return false;
				}
    }

    
    /** 
		 * Return the polygon
     * @param geometryFactory geometry factory to be used
     * @return polygon
     */    
    public Geometry create(GeometryFactory geometryFactory) {
        
				if( internalType.equals("Point") )           { return geometryFactory.createMultiPoint( geometryFactory.toPointArray(geometries) ); }
				else if( internalType.equals("LineString") ) { return geometryFactory.createMultiLineString( geometryFactory.toLineStringArray(geometries) ); }
				else if( internalType.equals("Polygon") )    { return geometryFactory.createMultiPolygon( geometryFactory.toPolygonArray(geometries) ); }
				else                                         { return null; }
		}

    
}
