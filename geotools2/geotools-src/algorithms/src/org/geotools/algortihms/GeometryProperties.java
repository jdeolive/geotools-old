/*
 * GeometryProperties.java
 *
 * Created on March 5, 2002, 4:55 PM
 */

package org.geotools.algortihms;
import com.vividsolutions.jts.geom.*;

/**
 *
 * @author  jamesm
 */
public interface GeometryProperties {
    
    public double getArea(Geometry geom);
    
    public double getPerimiter(Geometry geom);

}

