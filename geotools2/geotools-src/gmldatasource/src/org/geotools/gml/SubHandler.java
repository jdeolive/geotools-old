/*
 * GMLHandler.java
 *
 * Created on 04 March 2002, 17:51
 */

package org.geotools.gml;

import com.vividsolutions.jts.geom.*;


/** 
 * Specifies how a GML Geometry handler should behave.
 *
 * @author Ian Turton, CCG Leeds
 * @author Rob Hranac, Vision for New York
 * @version $Id: SubHandler.java,v 1.1 2002/04/03 01:30:15 robhranac Exp $
 */
public abstract class SubHandler {


    public static final int GEOMETRY_START = 1;

    public static final int GEOMETRY_END = 2;

    public static final int GEOMETRY_SUB = 3;


    /** 
		 * Adds a coordinate to the object being built if appropriate
		 *
     * @param c the coordinate to add
     */    
    public abstract void addCoordinate(Coordinate coordinate);


    /**
		 * Adds a geometry to the object being built if appropriate
		 *
     * @param message the geometry being added
     * @param type the geometry being added
     */    
    public void subGeometry(String message, int type) {
		}


    /**
		 * Adds a geometry to the object being built if appropriate
     */    
    public abstract boolean isComplete(String message);


    /** 
		 * Builds the object and returns it
		 * 
     * @param gf geometry factroy to use for the build
     * @return the geometry that repesents this object
     */    
    public abstract Geometry create(GeometryFactory geometryFactory);
    

    /**
		 * A short description of the handler
     *
     * @return string representation
     */    
    public String toString(){
        String name = this.getClass().getName();
        int index = name.lastIndexOf('.');
        return name.substring(index+1);
    }
}

