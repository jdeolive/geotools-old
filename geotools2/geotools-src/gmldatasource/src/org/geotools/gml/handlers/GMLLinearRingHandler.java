/*
 * GMLLinearRingHandler.java
 *
 * Created on 06 March 2002, 10:40
 */

package org.geotools.gml.handlers;
import org.geotools.gml.*;
import com.vividsolutions.jts.geom.*;
import java.util.*;
/** handles a LinearRing - a linestring which is closed.
 * @author ian
 * @version $Id: GMLLinearRingHandler.java,v 1.3 2002/03/11 14:41:47 ianturton Exp $
 */
public class GMLLinearRingHandler extends org.geotools.gml.GMLHandler {
    ArrayList coordList = new ArrayList();
    /** Creates a new instance of GMLLinearRingHandler */
    public GMLLinearRingHandler() {
    }

    /** build the linearRing
     * @param gf geometry factory used for the build
     * @return geomerty of the linearring
     */    
    public Geometry finish(GeometryFactory gf) {
        try{
            return gf.createLinearRing((Coordinate[])coordList.toArray(new Coordinate[]{}));
        }catch(TopologyException e){
            System.err.println("Caught Topology exception in GMLLinearRingHandler");
            return null;
        }
    }
    
    /** not used
     *
     */    
    public void addGeometry(Geometry g) {
    }
    
    /**
     * @param c  */    
    public void addCoordinate(Coordinate c) {
        coordList.add(c);
    }
    
}
