/*
 * GMLBoxHandler.java
 *
 * Created on 07 March 2002, 12:17
 */

package org.geotools.gml.handlers;

import com.vividsolutions.jts.geom.*;
/**
 *
 * @author  ian
 */
public class GMLBoxHandler extends org.geotools.gml.GMLHandler {
    Envelope e = new Envelope();
    /** Creates a new instance of GMLBoxHandler */
    public GMLBoxHandler() {
    }
    
    public Geometry finish(GeometryFactory gf) {
        Coordinate[] c = new Coordinate[5];
        c[0]=new Coordinate(e.getMinX(),e.getMinY());
        c[1]=new Coordinate(e.getMinX(),e.getMaxY());
        c[2]=new Coordinate(e.getMaxX(),e.getMaxY());
        c[3]=new Coordinate(e.getMaxX(),e.getMinY());
        c[4]=new Coordinate(e.getMinX(),e.getMinY());
        LinearRing r = null;
        try{
            r = gf.createLinearRing(c);
        }catch(TopologyException e){
            System.err.println("Topology Exception in GMLBoxHandler");
            return null;
        }
        return gf.createPolygon(r,null);
        
    }
    
    public void addGeometry(Geometry g) {
    }
    
    public void addCoordinate(Coordinate c) {
        e.expandToInclude(c);
    }
    
}
