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
    Polygon[] polygons1;
    MultiLineString multiLineString1;
    LineString[] lineStrings1;
    MultiPoint multiPoint1;
    Point[] points1;// = new Point[1];
    Point point1;
    Coordinate coordinate1;
    Coordinate[] coordinates1;
    Coordinate[] coordinates2;
    
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
    
    public void testPoints() {
        // Test point1 area
        //System.out.println("Running points test");
        //System.out.println("Point is "+point1);
        //assertEquals(0.0d,geometryProperties1.getArea(point1),0.0d);
        assertEquals(0.0d,geometryProperties1.getArea(points1[0]),0.0d);
        // test point1 perimeter
        //assertEquals(0.0d,geometryProperties1.getPerimeter(points1[0]),0.0d);
    }
    
    public void testPolygons() {
        // Test polygon1 area
        assertEquals(8.0d,geometryProperties1.getArea(polygons1[0]),0.0d);
        // test polygon1 perimeter
        assertEquals(16.0d,geometryProperties1.getPerimeter(polygons1[0]),0.0d);
    }

    public void setUp() {
        
        GeometryFactory geometryFactory1 = new GeometryFactory();
        PrecisionModel precisionModel1 = geometryFactory1.getPrecisionModel();
        int SRID = 0;
        
        double x = 0.0d;
        double y = 0.0d;
        double z = 0.0d;
        
        // Generate a coordinate
        //coordinate1 = new Coordinate();
        coordinate1 = new Coordinate(x,y);
        //coordinate1 = new Coordinate(x,y,z);

        // Generate a point
        point1 = geometryFactory1.createPoint(coordinate1);
        points1 = new Point[1];
        points1[0] = point1;
        System.out.println("In setup");
        System.out.println("Created "+points1[0]+" which should be "+point1);
        
        // Generate polygons1[]
        coordinates1 = new Coordinate[5];
        coordinates1[0] = new Coordinate(0.0d,0.0d);
        coordinates1[1] = new Coordinate(3.0d,0.0d);
        coordinates1[2] = new Coordinate(3.0d,3.0d);
        coordinates1[3] = new Coordinate(0.0d,3.0d);
        coordinates1[4] = new Coordinate(0.0d,0.0d);
        coordinates2 = new Coordinate[5];
        coordinates2[0] = new Coordinate(1.0d,1.0d);
        coordinates2[1] = new Coordinate(1.0d,2.0d);
        coordinates2[2] = new Coordinate(2.0d,2.0d);
        coordinates2[3] = new Coordinate(2.0d,1.0d);
        coordinates2[4] = new Coordinate(1.0d,1.0d);
        LinearRing linearRing1 = new LinearRing(null,precisionModel1,SRID);
        LinearRing[] linearRings1 = new LinearRing[1];
        try {
            linearRing1 = geometryFactory1.createLinearRing(coordinates1);
            linearRings1[0] = geometryFactory1.createLinearRing(coordinates2);
        } catch (TopologyException te1) {
            System.out.println(te1+" Exiting");
            System.exit(0);
        }
        polygons1 = new Polygon[1];
        polygons1[0] = geometryFactory1.createPolygon(linearRing1,linearRings1);
        
    }
}
