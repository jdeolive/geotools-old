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
public class GMLPolygonHandler extends org.geotools.gml.GMLHandler {
    LinearRing shell;
    ArrayList holes = new ArrayList();
    protected static CGAlgorithms cga = new RobustCGAlgorithms();
    /** Creates a new instance of GMLPolygonHandler */
    public GMLPolygonHandler() {
    }
    
    public Geometry finish(GeometryFactory gf) {
        System.out.println("Building finished poly "+shell+" with "+holes.size()+" holes");
        return gf.createPolygon(shell,(LinearRing[])holes.toArray(new LinearRing[0]));
        
    }
    
    public void addGeometry(Geometry g) {
        if(g.getGeometryType().equalsIgnoreCase("LinearRing")){
            LinearRing ring = (LinearRing)g;
            Coordinate[] points= ring.getCoordinates();
            if(cga.isCCW(points)){
                System.out.println("adding hole");
                holes.add(ring);
            }
            else{
                System.out.println("setting outer");
                shell=ring;
            }
        }else{
            System.err.println("PolygonHandler was expecting linearRing but got "+g.getGeometryType());
        // else error?
        }
        
    }
    
    public void addCoordinate(Coordinate c) {
    }
    
}
