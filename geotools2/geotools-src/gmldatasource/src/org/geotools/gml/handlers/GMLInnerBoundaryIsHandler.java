/*
 * GMLInnerBoundaryIsHandler.java
 *
 * Created on 07 March 2002, 14:37
 */

package org.geotools.gml.handlers;
import com.vividsolutions.jts.geom.*;
import com.vividsolutions.jts.algorithm.*;
/** handler for innerBoundaryIs tags
 *
 * @author ian
 * @version $Id: GMLInnerBoundaryIsHandler.java,v 1.2 2002/03/11 14:41:47 ianturton Exp $
 */
public class GMLInnerBoundaryIsHandler extends org.geotools.gml.GMLHandler {
    /** needed to perform counter clockwise test
     */    
    protected static CGAlgorithms cga = new RobustCGAlgorithms();
    LinearRing ring;
    /** Creates a new instance of GMLInnerBoundaryIsHandler */
    public GMLInnerBoundaryIsHandler() {
    }
    /** finish the polygon and return it
     * @param gf geometry factory to use to build polygon
     *
     * @return the polygon
     *
     */    
    public Geometry finish(GeometryFactory gf) {
        Coordinate[] points = ring.getCoordinates();
        LinearRing r;
        
        // the inner ring (hole) of a polygon must be counter clockwise
        if(cga.isCCW(points)){// hmm backwards - best reverse the points
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
     */    
    public void addGeometry(Geometry g) {
        ring=(LinearRing)g;
    }
    
    /** adds a coordinate
     * @param c the coordinate
     */    
    public void addCoordinate(Coordinate c) {
    }
    
}
