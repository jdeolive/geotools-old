/*
 * GMLBoxHandler.java
 *
 * Created on 07 March 2002, 12:17
 */

package org.geotools.gml;

import com.vividsolutions.jts.geom.Coordinate;


/**
 * Creates a simple OGC box.
 *
 * @author Ian Turton, CCG Leeds
 * @author Rob Hranac, Vision for New York
 * @version $Id: SubHandlerBox.java,v 1.3 2002/05/01 14:29:17 ianturton Exp $
 */
public class SubHandlerBox extends SubHandler {
    
    com.vividsolutions.jts.geom.Envelope e = new com.vividsolutions.jts.geom.Envelope();
    
    
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
     * @param message The geometry to inspect.
     * @return Flag for a complete geometry.
     */
    public boolean isComplete(String message) {
        return true;
    }
    
    
    /** builds and returns the polygon
     * @return the polygon
     * @param geometryFactory the geometryFactory to be used to build the polygon
     */
    public com.vividsolutions.jts.geom.Geometry create(com.vividsolutions.jts.geom.GeometryFactory geometryFactory) {
        Coordinate[] c = new Coordinate[5];
        c[0] = new Coordinate(e.getMinX(), e.getMinY());
        c[1] = new Coordinate(e.getMinX(), e.getMaxY());
        c[2] = new Coordinate(e.getMaxX(), e.getMaxY());
        c[3] = new Coordinate(e.getMaxX(), e.getMinY());
        c[4] = new Coordinate(e.getMinX(), e.getMinY());
        com.vividsolutions.jts.geom.LinearRing r = null;
        try {
            r = geometryFactory.createLinearRing(c);
        } catch (com.vividsolutions.jts.geom.TopologyException e){
            System.err.println("Topology Exception in GMLBoxHandler");
            return null;
        }
        return geometryFactory.createPolygon(r, null);
        
    }
    
}
