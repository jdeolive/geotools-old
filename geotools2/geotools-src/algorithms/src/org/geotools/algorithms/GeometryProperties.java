/*
 * GeometryProperties.java
 *
 * Created on March 7, 2002, 10:48 AM
 */

package org.geotools.algorithms;

import com.vividsolutions.jts.geom.Geometry;

/**
 *
 * @author  jamesm
 */
public interface GeometryProperties {
    
    /**
     * Cacluclate and return the area of the specified geometry.<br>
     * For Polygons this is the total area inside the external ring less
     * the total of any contained by interior rings.  GeometryCollections
     * (including MultiPolygons) are ittereted through so the result is the
     * sum of all polygons anywhere within the collection.
     * Any geometry other than Polgyon or a collection returns 0;
     *
     * @param g The Geometry to calculate the area of.
     * @return The total area of the Geometry 
     */
    public double getArea(Geometry g);
    
    /**
     * Cacluclate and return the perimeter of the specified geometry.<br>
     * For Polygons this is the total length of the exterior ring and all
     * internal rings.  For LineStrings the total line length is returned.
     * GeometryCollections are ittereted through so the result is the
     * sum of all Polygon and Line geometries anywhere within the collection.
     * Any point geometries return a value of 0;
     *
     * @param g The Geometry to calculate the area of.
     * @return The total area of the Geometry 
     */
    public double getPerimeter(Geometry g);
}

