/*
 * GMLHandler.java
 *
 * Created on 04 March 2002, 17:51
 */

package org.geotools.gml;

import com.vividsolutions.jts.geom.*;


/** 
 * Specifies how a generic OGC simple geometry handler should behave.
 *
 * @author Ian Turton, CCG Leeds
 * @author Rob Hranac, Vision for New York
 * @version $Id: SubHandler.java,v 1.2 2002/04/12 18:51:59 robhranac Exp $
 */
public abstract class SubHandler {


		/** Indicates start of a geometry */
    public static final int GEOMETRY_START = 1;
		/** Indicates end of a geometry */
    public static final int GEOMETRY_END = 2;
		/** Indicates a sub geometry message */
    public static final int GEOMETRY_SUB = 3;


    /** 
		 * Adds a coordinate to the object being built if appropriate.
		 *
     * @param coordinate Coordinate to add
     */    
    public abstract void addCoordinate(Coordinate coordinate);


    /**
		 * Tells the handler that it just saw a subhandler.
		 *
     * @param message The sub geometry message (i.e. isInnerBoundary).
     * @param type The type of sub message (start, end, etc.)
     */    
    public void subGeometry(String message, int type) {
		}


    /**
		 * Determines whether or not the geometry is ready to return.
		 *
     * @param message The geometry to inspect.
		 * @return Flag for a complete geometry.
     */    
    public abstract boolean isComplete(String message);


    /** 
		 * Creates a new JTS geometry.
		 * 
     * @param geometryFactory The JTS geometry factory to use for geometry creation.
     * @return An OGC simple geometry type for return.
     */    
    public abstract Geometry create(GeometryFactory geometryFactory);
    

    /**
		 * Describes the handler.
     *
     * @return String representation of the current handler.
     */    
    public String toString(){
        String name = this.getClass().getName();
        int index = name.lastIndexOf('.');
        return name.substring(index+1);
    }
}

