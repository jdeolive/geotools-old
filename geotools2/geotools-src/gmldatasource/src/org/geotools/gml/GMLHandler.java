/*
 * GMLHandler.java
 *
 * Created on 04 March 2002, 17:51
 */

package org.geotools.gml;
import com.vividsolutions.jts.geom.*;
/**
 *
 * @author  ian
 */
public interface GMLHandler {

    
    void addCoordinate(Coordinate c);
    void addGeometry(Geometry g);
    Geometry finish(GeometryFactory gf);
}

