/*
 * RobustGeometryProperties.java
 * This file is covered by the LGPL
 * Created on 05 March 2002, 17:59
 */

package org.geotools.algorithms;

import com.vividsolutions.jts.geom.*;

/** Robust implementation of the GeometryProperties interface
 * TODO: Update comments
 * TODO: Add in calculation for area of a Polygon
 *
 *
 * @author andyt
 * @version $Revision: 1.1 $ $Date: 2002/03/07 11:30:48 $
 */
public class RobustGeometryProperties implements org.geotools.algorithms.GeometryProperties {

    /**
     * Returns the area of a GeometryCollection
     * @param geometryCollection1 The GeometryCollection for which the area is
     * calulated
     * @return The total area of all geometries in the collection
     */
    protected double getArea(GeometryCollection geometryCollection1) {
        double area = 0.0d;
        double perimeter = 0.0d;
        int numberOfGeometries1 = geometryCollection1.getNumGeometries();
        // Go through geometryCollection1 and sum areas of component geometries
        for (int i = 0; i < numberOfGeometries1; i ++) {
            area += getArea(geometryCollection1.getGeometryN(i));
        }
        return area;
    }

    /**
     * Returns
     * @param geometryCollection1 The GeometryCollection for which the perimeter is
     * calulated
     * @return the perimeter of a GeometryCollection
     */
    protected double getPerimeter(GeometryCollection geometryCollection1) {
        double perimeter = 0.0d;
        int numberOfGeometries1 = geometryCollection1.getNumGeometries();
        // Go through geometryCollection1 and sum perimeters of component 
        // geometries
        for (int i = 0; i < numberOfGeometries1; i ++) {
            perimeter += getPerimeter(geometryCollection1.getGeometryN(i));
        }
        return perimeter;
    }

    /**
     * Returns the area of the geometry
     */
    public double getArea(Geometry geometry1) {
        double area = 0.0d;
        if (geometry1 instanceof GeometryCollection) {
            area += getArea((GeometryCollection) geometry1);
        } else if (geometry1 instanceof MultiPolygon) {
            area += getArea((MultiPolygon) geometry1);
        } else if (geometry1 instanceof Polygon) {
            area += getArea((Polygon) geometry1);
        } else {
            area += 0.0d;
        }
        return area;
    }

    /**
     * Returns the perimeter of the geometry
     */
    public double getPerimeter(Geometry geometry1) {
        double perimeter = 0.0d;
        if (geometry1 instanceof GeometryCollection) {
            perimeter += getPerimeter((GeometryCollection) geometry1);
        } else if (geometry1 instanceof MultiPolygon) {
            perimeter += getPerimeter((MultiPolygon) geometry1);
        } else if (geometry1 instanceof Polygon) {
            perimeter += getPerimeter((Polygon) geometry1);
        } else if (geometry1 instanceof MultiLineString) {
            perimeter += getPerimeter((MultiLineString) geometry1);
        } else if (geometry1 instanceof LineString) {
            perimeter += getPerimeter((LineString) geometry1);
        } else {
            perimeter += 0.0d;
        }
        return perimeter;
    }
    
    /**
     * Returns the area of a MultiPolygon
     * @param multiPolygon1 - the MultiPolygon for which the area is calculated
     */
    protected double getArea(MultiPolygon multiPolygon1) {
        double area = 0.0d;
        int numberOfGeometries1 = multiPolygon1.getNumGeometries();
        for (int i = 0; i < numberOfGeometries1; i ++) {
            area += getArea(multiPolygon1.getGeometryN(i));
        }
        return area;
    }
    
    /**
     * Returns the perimeter of a MultiPolygon
     * @param multiPolygon1 - the MultiPolygon for which the perimeter is 
     * calculated
     */
    protected double getperimeter(MultiPolygon multiPolygon1) {
        double perimeter = 0.0d;
        int numberOfGeometries1 = multiPolygon1.getNumGeometries();
        for (int i = 0; i < numberOfGeometries1; i ++) {
            perimeter += getPerimeter(multiPolygon1.getGeometryN(i));
        }
        return perimeter;
    }

    /**
     * Returns the area of a Polygon
     * @param polygon1 - the Polygon for which the area is calculated
     * WARNING!!!! THIS CODE IS INCOMPLETE
     */
    protected double getArea(Polygon polygon1) {
        return 0.0d;
    }

    /**
     * Returns the perimeter of a Polygon
     * @param polygon1 - the Polygon for which the perimeter is calculated
     */
    protected double getPerimeter(Polygon polygon1) {
        double perimeter = 0.0d;
        LineString lineString1 = polygon1.getExteriorRing();
        perimeter += getPerimeter(lineString1);
        int numberOfHoles = polygon1.getNumInteriorRing();
        for (int i = 0; i < numberOfHoles; i ++) {
            perimeter += getPerimeter(polygon1.getInteriorRingN(i));
        }
        return perimeter;
    }

    /**
     * Returns the perimeter of a MultiLineString
     * @param multiLineString1 - the MultiLineString for which the perimeter is
     * calculated
     */
    protected double getPerimeter(MultiLineString multiLineString1) {
        double perimeter = 0.0d;
        int numberOfGeometries1 = multiLineString1.getNumGeometries();
        for (int i = 0; i < numberOfGeometries1; i ++) {
            perimeter += getPerimeter(multiLineString1.getGeometryN(i));
        }
        return perimeter;
    }

    /**
     * Returns the perimeter of a LineString
     * @param lineString1 - the LineString for which the perimeter is calculated
     */
    protected double getPerimeter(LineString lineString1) {
        double perimeter = 0.0d;
        int numberOfPoints1 = lineString1.getNumPoints();
        Coordinate[] coordinates1 = lineString1.getCoordinates();
        for (int i = 0; i < (numberOfPoints1 - 1); i ++) {
            perimeter += coordinates1[i].distance(coordinates1[i + 1]);
        }
        return perimeter;
    }
}
