/*
 * RenderStyleTest.java
 *
 * Created on 27 May 2002, 15:40
 */

package org.geotools.styling;
import org.geotools.renderer.*;
import org.geotools.data.*;
import com.vividsolutions.jts.geom.*;
import org.geotools.datasource.extents.*;
import org.geotools.feature.*;
import org.geotools.styling.*;
import org.geotools.map.*;
import java.util.*;
import java.io.*;
import junit.framework.*;
import java.awt.Frame;
import java.awt.Panel;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.*;
import org.apache.log4j.Logger;
import org.apache.log4j.BasicConfigurator;

/**
 *
 * @author jamesm
 */
public class RenderStyleTest extends TestCase {
    
    public RenderStyleTest(java.lang.String testName) {
        super(testName);
        BasicConfigurator.configure();
    }
    
    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(suite());
    }
    
    public static Test suite() {
        TestSuite suite = new TestSuite(RenderStyleTest.class);
        return suite;
    }
    
    public void testSimpleRender()throws Exception {
        //same as the datasource test, load in some features into a table
        
        // Request extent
        EnvelopeExtent ex = new EnvelopeExtent(5, 15, 5, 35);
        
        GeometryFactory geomFac = new GeometryFactory();
        LineString line = makeSampleLineString(geomFac);
        AttributeType lineAttribute = new AttributeTypeDefault("centerline", line.getClass());
        FeatureType lineType = new FeatureTypeFlat(lineAttribute).setTypeName("linefeature");
        FeatureFactory lineFac = new FeatureFactory(lineType);
        Feature lineFeature = lineFac.create(new Object[]{line});
        
        Polygon polygon = makeSamplePolygon(geomFac);
        
        AttributeType polygonAttribute = new AttributeTypeDefault("edge", polygon.getClass());
        FeatureType polygonType = new FeatureTypeFlat(polygonAttribute).setTypeName("polygon");
        FeatureFactory polygonFac = new FeatureFactory(polygonType);
        
        Feature polygonFeature = polygonFac.create(new Object[]{polygon});
        
        
        Polygon polygon2 = makeSamplePolygon2(geomFac);
        
        AttributeType polygonAttribute2 = new AttributeTypeDefault("edge2", polygon2.getClass());
        FeatureType polygonType2 = new FeatureTypeFlat(polygonAttribute2).setTypeName("polygontest");
        FeatureFactory polygonFac2 = new FeatureFactory(polygonType2);
        
        Feature polygonFeature2 = polygonFac2.create(new Object[]{polygon2});
        
        Point point = makeSamplePoint(geomFac);
        AttributeType pointAttribute = new AttributeTypeDefault("centre", point.getClass());
        FeatureType pointType = new FeatureTypeFlat(pointAttribute).setTypeName("pointfeature");
        FeatureFactory pointFac = new FeatureFactory(pointType);
        
        Feature pointFeature = pointFac.create(new Object[]{point});
        MemoryDataSource datasource = new MemoryDataSource();
        datasource.addFeature(lineFeature);
        datasource.addFeature(polygonFeature);
        datasource.addFeature(polygonFeature2);
        datasource.addFeature(pointFeature);
        
        FeatureCollection ft = new FeatureCollectionDefault(datasource);
        
        org.geotools.map.Map map = new DefaultMap();
        
        File f = new File(System.getProperty("dataFolder"),"sample.sld");
        System.out.println("testing reader using "+f.toString());
        SLDStyle style = new SLDStyle(f);
        map.addFeatureTable(ft,style);
        Java2DRenderer renderer = new org.geotools.renderer.Java2DRenderer();
        Frame frame = new Frame();
        frame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {e.getWindow().dispose(); }
        });
        Panel p = new Panel();
        frame.add(p);
        frame.setSize(300,300);
        frame.setVisible(true);
        renderer.setOutput(p.getGraphics(),p.getBounds());
        map.render(renderer,ex.getBounds());//and finaly try and draw it!
        Thread.sleep(5000);
    }
    
    private LineString makeSampleLineString(final GeometryFactory geomFac) {
        Coordinate[] linestringCoordinates = new Coordinate[7];
        linestringCoordinates[0] = new Coordinate(5.0d,5.0d);
        linestringCoordinates[1] = new Coordinate(6.0d,5.0d);
        linestringCoordinates[2] = new Coordinate(6.0d,6.0d);
        linestringCoordinates[3] = new Coordinate(7.0d,6.0d);
        linestringCoordinates[4] = new Coordinate(7.0d,7.0d);
        linestringCoordinates[5] = new Coordinate(8.0d,7.0d);
        linestringCoordinates[6] = new Coordinate(8.0d,8.0d);
        LineString line = geomFac.createLineString(linestringCoordinates);
        
        return line;
    }
    
    private com.vividsolutions.jts.geom.Polygon makeSamplePolygon(final GeometryFactory geomFac) {
        Coordinate[] polygonCoordinates = new Coordinate[10];
        polygonCoordinates[0] = new Coordinate(7,7);
        polygonCoordinates[1] = new Coordinate(6,9);
        polygonCoordinates[2] = new Coordinate(6,11);
        polygonCoordinates[3] = new Coordinate(7,12);
        polygonCoordinates[4] = new Coordinate(9,11);
        polygonCoordinates[5] = new Coordinate(11,12);
        polygonCoordinates[6] = new Coordinate(13,11);
        polygonCoordinates[7] = new Coordinate(13,9);
        polygonCoordinates[8] = new Coordinate(11,7);
        polygonCoordinates[9] = new Coordinate(7,7);
        try{
            LinearRing ring = geomFac.createLinearRing(polygonCoordinates);
            com.vividsolutions.jts.geom.Polygon polyg = geomFac.createPolygon(ring,null);
            return polyg;
        }
        catch(TopologyException te){
            fail("Error creating sample polygon for testing "+te);
        }
        return null;
    }
    private com.vividsolutions.jts.geom.Polygon makeSamplePolygon2(final GeometryFactory geomFac) {
        Coordinate[] polygonCoordinates = new Coordinate[10];
        polygonCoordinates[0] = new Coordinate(7,27);
        polygonCoordinates[1] = new Coordinate(6,29);
        polygonCoordinates[2] = new Coordinate(6,31);
        polygonCoordinates[3] = new Coordinate(7,32);
        polygonCoordinates[4] = new Coordinate(9,31);
        polygonCoordinates[5] = new Coordinate(11,32);
        polygonCoordinates[6] = new Coordinate(13,31);
        polygonCoordinates[7] = new Coordinate(13,29);
        polygonCoordinates[8] = new Coordinate(11,27);
        polygonCoordinates[9] = new Coordinate(7,27);
        try{
            LinearRing ring = geomFac.createLinearRing(polygonCoordinates);
            com.vividsolutions.jts.geom.Polygon polyg = geomFac.createPolygon(ring,null);
            return polyg;
        }
        catch(TopologyException te){
            fail("Error creating sample polygon for testing "+te);
        }
        return null;
    }
    private Point makeSamplePoint(final GeometryFactory geomFac) {
        Coordinate c = new Coordinate(14.0d,14.0d);
        Point point = geomFac.createPoint(c);
        return point;
    }
}


