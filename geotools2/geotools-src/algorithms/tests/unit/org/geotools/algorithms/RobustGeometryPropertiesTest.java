/*
 * RobustGeometryPropertiesTest.java
 * JUnit based test
 *
 * Created on 07 March 2002, 11:41
 */

package org.geotools.algorithms;

import com.vividsolutions.jts.geom.*;
import junit.framework.*;

/**
 *
 * @author andyt
 */
public class RobustGeometryPropertiesTest extends TestCase {
    GeometryProperties geometryProperties1 = new RobustGeometryProperties();
    GeometryCollection geometryCollection;
    Geometry geometry1;
    MultiPolygon multiPolygon1;
    Polygon polygon1;
    MultiLineString multiLineString1;
    LineString lineString1;
    MultiPoint multiPoint1;
    Point point1;
    Coordinate coordinate1;
    Coordinate[] coordinates1;
    
    public RobustGeometryPropertiesTest(java.lang.String testName) {
        super(testName);
    }
    
    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(suite());
    }
    
    public static Test suite() {
        TestSuite suite = new TestSuite(RobustGeometryPropertiesTest.class);
        return suite;
    }
    
    
    public void testPoint() {
    // Test point1 area
        assertEquals(0.0d,geometryProperties1.getArea(point1),0.0d);
        // test point1 perimeter
        assertEquals(0.0d,geometryProperties1.getPerimeter(point1),0.0d);
    }
    
    /*
        //Generate a square
        coordinates1[0] = new Coordinate(((j * cellsize) + xllcorner),((i * cellsize) + yllcorner));
                coordinates1[1] = new Coordinate(((j * cellsize) + xllcorner),(((i + 1) * cellsize) + yllcorner));
                coordinates1[2] = new Coordinate((((j + 1) * cellsize) + xllcorner),(((i + 1) * cellsize) + yllcorner));
                coordinates1[3] = new Coordinate((((j + 1) * cellsize) + xllcorner),((i * cellsize) + yllcorner));
                coordinates1[4] = new Coordinate(((j * cellsize) + xllcorner),((i * cellsize) + yllcorner));
                polygon1 = new Polygon(new LinearRing(coordinates1,precisionModel1,SRID),precisionModel1,SRID);
         
         */
    
    public void setup() {
        GeometryFactory geometryFactory1 = new GeometryFactory();
        PrecisionModel precisionModel1 = geometryFactory1.getPrecisionModel();
        //int SRID;
        double x = 0.0d;
        double y = 0.0d;
        double z = 0.0d;
        // Generate a coordinate
        //coordinate1 = new Coordinate();
        coordinate1 = new Coordinate(x,y);
        //coordinate1 = new Coordinate(x,y,z);
        // Generate a point
        point1 = geometryFactory1.createPoint(coordinate1);
        
    }
}
