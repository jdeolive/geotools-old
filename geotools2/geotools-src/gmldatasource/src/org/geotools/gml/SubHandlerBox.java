/*
 * GMLBoxHandler.java
 *
 * Created on 07 March 2002, 12:17
 */

package org.geotools.gml;

import com.vividsolutions.jts.geom.*;

/** 
 * Creates a simple OGC box. 
 *
 * @author Ian Turton, CCG Leeds
 * @author Rob Hranac, Vision for New York
 * @version $Id: SubHandlerBox.java,v 1.1 2002/04/03 01:30:15 robhranac Exp $
 */
public class SubHandlerBox extends SubHandler {

    Envelope e = new Envelope();


    /** Creates a new instance of GMLBoxHandler */
    public SubHandlerBox() {
    }


    /** sets a corner
     * @param c the coordinate of the corner
     */    
    public void addCoordinate(Coordinate c) {
        e.expandToInclude(c);
    }


    /** sets a corner
     */    
    public boolean isComplete(String message) {
        return true;
    }


    /** builds and returns the polygon
     * @param gf geometry factory to be used to build the polygon
     *
     * @return the polygon
     *
     */    
    public Geometry create(GeometryFactory gemoetryFactory) {
        Coordinate[] c = new Coordinate[5];
        c[0]=new Coordinate(e.getMinX(),e.getMinY());
        c[1]=new Coordinate(e.getMinX(),e.getMaxY());
        c[2]=new Coordinate(e.getMaxX(),e.getMaxY());
        c[3]=new Coordinate(e.getMaxX(),e.getMinY());
        c[4]=new Coordinate(e.getMinX(),e.getMinY());
        LinearRing r = null;
        try{
            r = gemoetryFactory.createLinearRing(c);
        }catch(TopologyException e){
            System.err.println("Topology Exception in GMLBoxHandler");
            return null;
        }
        return gemoetryFactory.createPolygon(r,null);
        
    }
        
}
