/*
 * GMLHandler.java
 *
 * Created on 04 March 2002, 17:51
 */

package org.geotools.gml;
import com.vividsolutions.jts.geom.*;
/** Specifys how a GML Geometry handler should behave
 *
 * @author ian
 * @version $Id: GMLHandler.java,v 1.2 2002/03/06 17:55:13 ianturton Exp $
 */
public interface GMLHandler {

    
    void addCoordinate(Coordinate c);
    void addGeometry(Geometry g);
    Geometry finish(GeometryFactory gf);
}

