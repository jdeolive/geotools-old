/*
 * GMLPointHandler.java
 *
 * Created on 04 March 2002, 17:49
 */

package org.geotools.gml.handlers;
import org.geotools.gml.*;
import com.vividsolutions.jts.geom.*;
/** handler for gml:point elements
 *
 * @author ian
 * @version $Id: GMLPointHandler.java,v 1.4 2002/03/11 14:41:36 ianturton Exp $
 */
public class GMLPointHandler extends GMLHandler {
    Coordinate c;
    /** Creates a new instance of GMLPointHandler */
    public GMLPointHandler() {
    }

    /** set the coordinate for the point
     * @param coord the coordinate
     */    
    public void addCoordinate(Coordinate coord){

        c=coord;
    }
    
    /** build the point
     * @param gf geometry factroy to be used
     * @return the point
     */    
    public Geometry finish(GeometryFactory gf){

        return gf.createPoint(c);
    }
    
    public void addGeometry(Geometry g) {
        // do nothing - possibly we should throw an exception?
    }
    
}
