/*
 * GMLPolygonHandler.java
 *
 * Created on 06 March 2002, 10:36
 */

package org.geotools.gml.handlers;
import org.geotools.gml.*;
import com.vividsolutions.jts.geom.*;
import com.vividsolutions.jts.algorithm.*;
import java.util.*;
/**
 *
 * @author  ian
 */
public class GMLPolygonHandler implements org.geotools.gml.GMLHandler {
    LinearRing shell;
    ArrayList holes = new ArrayList();
    protected static CGAlgorithms cga = new RobustCGAlgorithms();
    /** Creates a new instance of GMLPolygonHandler */
    public GMLPolygonHandler() {
    }
    
    public Geometry finish(GeometryFactory gf) {
        return gf.createPolygon(shell,(LinearRing[])holes.toArray(new LinearRing[0]));
        
    }
    
    public void addGeometry(Geometry g) {
        if(g.getGeometryType().equalsIgnoreCase("LinerRing")){
            LinearRing ring = (LinearRing)g;
            Coordinate[] points= ring.getCoordinates();
            if(cga.isCCW(points)){
                holes.add(ring);
            }
            else{
                shell=ring;
            }
        } // else error?
        
    }
    
    public void addCoordinate(Coordinate c) {
    }
    
}
