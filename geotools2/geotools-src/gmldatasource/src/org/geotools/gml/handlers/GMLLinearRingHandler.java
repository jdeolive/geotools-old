/*
 * GMLLinearRingHandler.java
 *
 * Created on 06 March 2002, 10:40
 */

package org.geotools.gml.handlers;
import org.geotools.gml.*;
import com.vividsolutions.jts.geom.*;
import java.util.*;
/**
 *
 * @author  ian
 */
public class GMLLinearRingHandler implements org.geotools.gml.GMLHandler {
    ArrayList coordList = new ArrayList();
    /** Creates a new instance of GMLLinearRingHandler */
    public GMLLinearRingHandler() {
    }

    public Geometry finish(GeometryFactory gf) {
        try{
            return gf.createLinearRing((Coordinate[])coordList.toArray(new Coordinate[]{}));
        }catch(TopologyException e){
            System.err.println("Caught Topology exception in GMLLinearRingHandler");
            return null;
        }
    }
    
    public void addGeometry(Geometry g) {
    }
    
    public void addCoordinate(Coordinate c) {
        coordList.add(c);
    }
    
}
