/*
 * GMLLineStringHandler.java
 *
 * Created on 06 March 2002, 10:31
 */

package org.geotools.gml.handlers;
import org.geotools.gml.*;
import com.vividsolutions.jts.geom.*;
import java.util.*;
/**
 *
 * @author  ian
 */
public class GMLLineStringHandler implements org.geotools.gml.GMLHandler {
    ArrayList coordList = new ArrayList();
    /** Creates a new instance of GMLLineStringHandler */
    public GMLLineStringHandler() {
    }

    public Geometry finish(GeometryFactory gf) {
        return gf.createLineString((Coordinate[])coordList.toArray(new Coordinate[]{}));
    }
    
    public void addGeometry(Geometry g) {
    }
    
    public void addCoordinate(Coordinate c) {
        coordList.add(c);
    }
    
}
