/*
 * GMLPointHandler.java
 *
 * Created on 04 March 2002, 17:49
 */

package org.geotools.gml.handlers;
import org.geotools.gml.*;
import com.vividsolutions.jts.geom.*;
/**
 *
 * @author  ian
 */
public class GMLPointHandler extends GMLHandler {
    Coordinate c;
    /** Creates a new instance of GMLPointHandler */
    public GMLPointHandler() {
    }

    public void addCoordinate(Coordinate coord){
        System.out.println("adding coord "+coord+" to point" );
        c=coord;
    }
    
    public Geometry finish(GeometryFactory gf){
        System.out.println("building Point using "+c.toString());
        return gf.createPoint(c);
    }
    
    public void addGeometry(Geometry g) {
        // do nothing - possibly we should throw an exception?
    }
    
}
