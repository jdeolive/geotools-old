/*
 *    Geotools - OpenSource mapping toolkit
 *    (C) 2002, Centre for Computational Geography
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; 
 *    version 2.1 of the License.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *
 *    You should have received a copy of the GNU Lesser General Public
 *    License along with this library; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *    
 */

package org.geotools.gml;

import com.vividsolutions.jts.geom.Coordinate;


/**
 * Creates a simple OGC box.
 *
 * @version $Id: SubHandlerBox.java,v 1.4 2002/06/05 11:17:42 loxnard Exp $
 * @author Ian Turton, CCG
 * @author Rob Hranac, Vision for New York
 */
public class SubHandlerBox extends SubHandler {
    
    com.vividsolutions.jts.geom.Envelope e = new com.vividsolutions.jts.geom.Envelope();
    
    
    /** Creates a new instance of GMLBoxHandler. */
    public SubHandlerBox() {
    }
    
    
    /**
     * Sets a corner.
     * @param c the coordinate of the corner.
     */
    public void addCoordinate(Coordinate c) {
        e.expandToInclude(c);
    }
    
    
    /**
     * Sets a corner.
     * @param message The geometry to inspect.
     * @return Flag for a complete geometry.
     */
    public boolean isComplete(String message) {
        return true;
    }
    
    
    /**
     * Builds and returns the polygon.
     * @return the polygon.
     * @param geometryFactory the geometryFactory to be used to build the
     * polygon.
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
