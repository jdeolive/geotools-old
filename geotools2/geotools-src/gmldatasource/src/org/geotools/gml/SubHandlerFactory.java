/*
 * GMLBoxHandler.java
 *
 * Created on 07 March 2002, 12:17
 */

package org.geotools.gml;

import java.util.*;

import com.vividsolutions.jts.geom.*;


/** 
 * Creates the appropriate SubHandler element for a given OGC simple geometry type.
 *
 * @author Rob Hranac, Vision for New York
 * @version $Id: SubHandlerFactory.java,v 1.1 2002/04/03 01:30:15 robhranac Exp $
 */
public class SubHandlerFactory {


		private static final Collection BASE_GEOMETRY_TYPES = new Vector( java.util.Arrays.asList(new String[] {"MultiPoint","MultiLineString","MultiPolygon"}) );

    /** 
		 * Adds a coordinate to the object being built if appropriate
		 *
     */    
    public SubHandlerFactory() {
		}


    /**
		 * Adds a geometry to the object being built if appropriate
		 *
     * @param type Type of sub-handler to return.
     */    
    public SubHandler create(String type) {

				if( type.equals("Point") )                    { return new SubHandlerPoint(); }
				else if( type.equals("LineString") )          { return new SubHandlerLineString(); }
				else if( type.equals("LinearRing") )          { return new SubHandlerLinearRing(); }
			  else if( type.equals("Polygon") )             { return new SubHandlerPolygon(); }
			  else if( type.equals("Box") )                 { return new SubHandlerBox(); }
			  else if( BASE_GEOMETRY_TYPES.contains(type) ) { return new SubHandlerMulti(); }
				/*else if( type.equals("MultiLineString") ) { return new SubHandlerMultiLineString(); }
			  else if( type.equals("MultiPolygon") )    { return new SubHandlerMultiPolygon(); } */
				else                                          { return null; }
		}


}

