/*
 * GeometryProperties.java
 *
 * Created on March 7, 2002, 10:48 AM
 */

package org.geotools.algorithms;

import com.vividsolutions.jts.geom.*;

/**
 *
 * @author  jamesm
 */
public interface GeometryProperties {
    public double getArea(Geometry g);
    public double getPerimeter(Geometry g);
}

