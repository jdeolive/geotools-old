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
 * @version $Id: GMLHandler.java,v 1.3 2002/03/08 18:06:03 ianturton Exp $
 */
public abstract class GMLHandler {
    
    
    public abstract void addCoordinate(Coordinate c);
    public abstract void addGeometry(Geometry g);
    public abstract Geometry finish(GeometryFactory gf);
    
    public String toString(){
        String name = this.getClass().getName();
        int index = name.lastIndexOf('.');
        return name.substring(index+1);
    }
}

