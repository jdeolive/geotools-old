/*
 * GMLBoxHandler.java
 *
 * Created on 07 March 2002, 12:17
 */

package org.geotools.gml;


/**
 * Creates the appropriate SubHandler element for a given OGC simple geometry type.
 *
 * @author Rob Hranac, Vision for New York
 * @version $Id: SubHandlerFactory.java,v 1.3 2002/05/01 14:29:17 ianturton Exp $
 */
public class SubHandlerFactory {
    
    
    /** List of all valid OGC multi geometry types. */
    private static final java.util.Collection BASE_GEOMETRY_TYPES = 
        new java.util.Vector( java.util.Arrays.asList(
            new String[] {"MultiPoint","MultiLineString","MultiPolygon"}) 
        );
    
    
    /**
     * Empty contrsuctor.
     */
    public SubHandlerFactory() {
    }
    
    /**
     * Creates a new subhandler, based on the appropriate OGC simple geometry type.  Note that some types are aggregated into
     * a generic 'multi' type.
     *
     * @param type Type of sub-handler to return.
     */
    public SubHandler create(String type) {
        
        if ( type.equals("Point") ){
            return new SubHandlerPoint(); 
        }else if ( type.equals("LineString") ){
            return new SubHandlerLineString(); 
        }else if ( type.equals("LinearRing") ){
            return new SubHandlerLinearRing(); 
        }else if ( type.equals("Polygon") ){
            return new SubHandlerPolygon(); 
        }else if ( type.equals("Box") ){
            return new SubHandlerBox(); 
        }else if ( BASE_GEOMETRY_TYPES.contains(type) ) {
            return new SubHandlerMulti(); 
        }else{
            return null; 
        }
    }
    
    
}

