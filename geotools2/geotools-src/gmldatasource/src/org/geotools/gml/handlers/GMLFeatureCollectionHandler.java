/*
 * GMLFeatureCollectionHandler.java
 *
 * Created on 05 March 2002, 13:58
 */

package org.geotools.gml.handlers;
import com.vividsolutions.jts.geom.*;
import java.util.*;
/**
 *
 * @author  ian
 */
public class GMLFeatureCollectionHandler extends org.geotools.gml.GMLHandler {
    ArrayList list = new ArrayList();
    /** Creates a new instance of GMLFeatureCollectionHandler */
    public GMLFeatureCollectionHandler() {
    }

    public Geometry finish(GeometryFactory gf) {
        
        return (Geometry)gf.createGeometryCollection((Geometry[])list.toArray(new Geometry[]{}));
    }
    
    public void addCoordinate(Coordinate c) {
        // do nothing - mybe throw an exception
    }
    
    public void addGeometry(Geometry g) {

        list.add(g);
    }
    
}
