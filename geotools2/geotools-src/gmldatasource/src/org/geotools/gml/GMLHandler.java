/*
 * GMLHandler.java
 *
 * Created on 04 March 2002, 17:51
 */

package org.geotools.gml;
import com.vividsolutions.jts.geom.*;
/** Specifys how a GML Geometry handler should behave
 *
 * @author ian
 * @version $Id: GMLHandler.java,v 1.4 2002/03/11 14:37:34 ianturton Exp $
 */
public abstract class GMLHandler {
    
    
    /** adds a coordinate to the object being built if appropriate
     * @param c the coordinate to add
     */    
    public abstract void addCoordinate(Coordinate c);
    /** adds a geometry to the object being built if appropriate
     * @param g the geometry being added
     */    
    public abstract void addGeometry(Geometry g);
    /** builds the object and returns it
     * @param gf geometry factroy to use for the build
     * @return the geometry that repesents this object
     */    
    public abstract Geometry finish(GeometryFactory gf);
    
    /** a short description of the handler
     *
     * @return string representation
     */    
    public String toString(){
        String name = this.getClass().getName();
        int index = name.lastIndexOf('.');
        return name.substring(index+1);
    }
}

