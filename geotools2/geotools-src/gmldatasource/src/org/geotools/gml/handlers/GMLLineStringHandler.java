/*
 * GMLLineStringHandler.java
 *
 * Created on 06 March 2002, 10:31
 */

package org.geotools.gml.handlers;
import org.geotools.gml.*;
import com.vividsolutions.jts.geom.*;
import java.util.*;
/** handles a LineString  element
 *
 * @author ian
 * @version $Id: GMLLineStringHandler.java,v 1.3 2002/03/11 14:38:57 ianturton Exp $
 */
public class GMLLineStringHandler extends org.geotools.gml.GMLHandler {
    ArrayList coordList = new ArrayList();
    /** Creates a new instance of GMLLineStringHandler */
    public GMLLineStringHandler() {
    }

    /** build the linestring
     * @param gf the geomertyfactory needed to do the build
     * @return a lineString geometry
     */    
    public Geometry finish(GeometryFactory gf) {
        return gf.createLineString((Coordinate[])coordList.toArray(new Coordinate[]{}));
    }
    
    /** not used
     */    
    public void addGeometry(Geometry g) {
    }
    
    /** adds a coordinate to the string
     * @param c the coordinate to add */    
    public void addCoordinate(Coordinate c) {
        coordList.add(c);
    }
    
}
