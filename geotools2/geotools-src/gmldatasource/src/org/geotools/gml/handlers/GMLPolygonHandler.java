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
/** handler for polygons
 *
 * @author ian
 * @version $Id: GMLPolygonHandler.java,v 1.5 2002/03/11 14:38:57 ianturton Exp $
 */
public class GMLPolygonHandler extends org.geotools.gml.GMLHandler {
    LinearRing shell;
    ArrayList holes = new ArrayList();
    /** needed to check clockwisness of linearrings
     */    
    protected static CGAlgorithms cga = new RobustCGAlgorithms();
    /** Creates a new instance of GMLPolygonHandler */
    public GMLPolygonHandler() {
    }
    
    /** build the polygon
     * @param gf geometry factory to be used
     * @return polygon
     */    
    public Geometry finish(GeometryFactory gf) {
        
        return gf.createPolygon(shell,(LinearRing[])holes.toArray(new LinearRing[0]));
        
    }
    
    /** add linearRings
     * anticlockwise for outer ring, clockwise for holes
     * @param g linearRing to be added
     */    
    public void addGeometry(Geometry g) {
        if(g.getGeometryType().equalsIgnoreCase("LinearRing")){
            LinearRing ring = (LinearRing)g;
            Coordinate[] points= ring.getCoordinates();
            if(!cga.isCCW(points)){
                
                holes.add(ring);
            }
            else{
                
                shell=ring;
            }
        }else{
            System.err.println("PolygonHandler was expecting linearRing but got "+g.getGeometryType());
        // else error?
        }
        
    }
    
    /** not used
     */    
    public void addCoordinate(Coordinate c) {
    }
    
}
