/*
 * GMLFeatureCollectionHandler.java
 *
 * Created on 05 March 2002, 13:58
 */

package org.geotools.gml.handlers;
import com.vividsolutions.jts.geom.*;
import java.util.*;
/** Handler for gml:featureCollections
 *
 * @author ian
 * @version $Id: GMLFeatureCollectionHandler.java,v 1.4 2002/03/11 14:41:36 ianturton Exp $
 */
public class GMLFeatureCollectionHandler extends org.geotools.gml.GMLHandler {
    ArrayList list = new ArrayList();
    /** Creates a new instance of GMLFeatureCollectionHandler */
    public GMLFeatureCollectionHandler() {
    }

    /** finish the build and return the collection
     * @param gf factory used to build featurecollection
     * @return featurecollection as a geometryCollection
     */    
    public Geometry finish(GeometryFactory gf) {
        
        return (Geometry)gf.createGeometryCollection((Geometry[])list.toArray(new Geometry[]{}));
    }
    
    /** does nothing
     */    
    public void addCoordinate(Coordinate c) {
        // do nothing - mybe throw an exception
    }
    
    /** adds a geometry to the collection
     * @param g geometry to be added
     */    
    public void addGeometry(Geometry g) {

        list.add(g);
    }
    
}
