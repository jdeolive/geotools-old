/*
 * GMLOuterBoundaryIsHandler.java
 *
 * Created on 07 March 2002, 14:35
 */

package org.geotools.gml.handlers;

import com.vividsolutions.jts.geom.*;
import com.vividsolutions.jts.algorithm.*;
/** Handler for OuterBoundaryIs elements
 *
 * @author ian
 * @version $Id: GMLOuterBoundaryIsHandler.java,v 1.2 2002/03/11 14:41:47 ianturton Exp $
 */
public class GMLOuterBoundaryIsHandler extends org.geotools.gml.GMLHandler {
    /** need to check for clockwiseness of the linearring
     */    
    protected static CGAlgorithms cga = new RobustCGAlgorithms();
    LinearRing ring;
    /** Creates a new instance of GMLOuterBoundaryIsHandler */
    public GMLOuterBoundaryIsHandler() {
    }
    /** build the LinearRing
     * @param gf geomerty factory for the build
     * @return the clockwise linearRing
     */    
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
    
    /** not used
     * @param g
     */    
    public void addGeometry(Geometry g) {
        ring=(LinearRing)g;
    }
    
    /**
     * @param c  */    
    public void addCoordinate(Coordinate c) {
    }
    
}
