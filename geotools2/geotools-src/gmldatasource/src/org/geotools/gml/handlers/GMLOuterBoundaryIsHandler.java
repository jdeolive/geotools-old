/*
 * GMLOuterBoundaryIsHandler.java
 *
 * Created on 07 March 2002, 14:35
 */

package org.geotools.gml.handlers;

import com.vividsolutions.jts.geom.*;
import com.vividsolutions.jts.algorithm.*;
/**
 *
 * @author  ian
 */
public class GMLOuterBoundaryIsHandler extends org.geotools.gml.GMLHandler {
    protected static CGAlgorithms cga = new RobustCGAlgorithms();
    LinearRing ring;
    /** Creates a new instance of GMLOuterBoundaryIsHandler */
    public GMLOuterBoundaryIsHandler() {
    }
    public Geometry finish(GeometryFactory gf) {
        Coordinate[] points = ring.getCoordinates();
        LinearRing r;
        
        // the outer ring of a polygon must be clockwise
        if(!cga.isCCW(points)){// hmm backwards - best reverse the points
            Coordinate t;
            int end = points.length - 1 ;
            for(int i=0;i<points.length/2;i++,end--){
                t=points[end];
                points[end]=points[i];
                points[i]=t;
            }
            
            try{
                r = gf.createLinearRing(points);
            }catch(TopologyException e){
                System.err.println("Caught Topology exception in GMLLinearRingHandler");
                return null;
            }
            
            return r;
        }else{
            return ring;
        }
    }
    
    public void addGeometry(Geometry g) {
        ring=(LinearRing)g;
    }
    
    public void addCoordinate(Coordinate c) {
    }
    
}
