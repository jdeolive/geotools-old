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
 * @author jamesm,iant
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
        EnvelopeExtent ex = new EnvelopeExtent(40, 200, 30, 350);
        
        GeometryFactory geomFac = new GeometryFactory();
        LineString line = makeSampleLineString(geomFac,0,0);
        AttributeType lineAttribute = new AttributeTypeDefault("centerline", line.getClass());
        FeatureType lineType = new FeatureTypeFlat(lineAttribute).setTypeName("linefeature");
        FeatureFactory lineFac = new FeatureFactory(lineType);
        Feature lineFeature = lineFac.create(new Object[]{line});
        
        LineString line2 = makeSampleLineString(geomFac,100,0);
        lineType = new FeatureTypeFlat(lineAttribute).setTypeName("linefeature2");
        lineFac = new FeatureFactory(lineType);
        Feature lineFeature2 = lineFac.create(new Object[]{line2});
        
        LineString line3 = makeSampleLineString(geomFac,150,0);
        lineType = new FeatureTypeFlat(lineAttribute).setTypeName("linefeature3");
        lineFac = new FeatureFactory(lineType);
        Feature lineFeature3 = lineFac.create(new Object[]{line3});
        
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
        datasource.addFeature(lineFeature2);
        datasource.addFeature(lineFeature3);
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
        Frame frame = new Frame("rendering test");
        frame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {e.getWindow().dispose(); }
        });
        Panel p = new Panel();
        frame.add(p);
        frame.setSize(300,300);
        frame.setVisible(true);
        renderer.setOutput(p.getGraphics(),p.getBounds());
        renderer.setInteractive(false);
        map.render(renderer,ex.getBounds());//and finaly try and draw it!
        Thread.sleep(5000);
    }
    
    private LineString makeSampleLineString(final GeometryFactory geomFac, double xoff, double yoff) {
        Coordinate[] linestringCoordinates = new Coordinate[8];
        linestringCoordinates[0] = new Coordinate(50.0d+xoff,50.0d+yoff);
        linestringCoordinates[1] = new Coordinate(60.0d+xoff,50.0d+yoff);
        linestringCoordinates[2] = new Coordinate(60.0d+xoff,60.0d+yoff);
        linestringCoordinates[3] = new Coordinate(70.0d+xoff,60.0d+yoff);
        linestringCoordinates[4] = new Coordinate(70.0d+xoff,70.0d+yoff);
        linestringCoordinates[5] = new Coordinate(80.0d+xoff,70.0d+yoff);
        linestringCoordinates[6] = new Coordinate(80.0d+xoff,80.0d+yoff);
        linestringCoordinates[7] = new Coordinate(130.0d+xoff,300.0d+yoff);
        LineString line = geomFac.createLineString(linestringCoordinates);
        
        return line;
    }
    
    private com.vividsolutions.jts.geom.Polygon makeSamplePolygon(final GeometryFactory geomFac) {
        Coordinate[] polygonCoordinates = new Coordinate[10];
        polygonCoordinates[0] = new Coordinate(70,70);
        polygonCoordinates[1] = new Coordinate(60,90);
        polygonCoordinates[2] = new Coordinate(60,110);
        polygonCoordinates[3] = new Coordinate(70,120);
        polygonCoordinates[4] = new Coordinate(90,110);
        polygonCoordinates[5] = new Coordinate(110,120);
        polygonCoordinates[6] = new Coordinate(130,110);
        polygonCoordinates[7] = new Coordinate(130,90);
        polygonCoordinates[8] = new Coordinate(110,70);
        polygonCoordinates[9] = new Coordinate(70,70);
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
        polygonCoordinates[0] = new Coordinate(70,270);
        polygonCoordinates[1] = new Coordinate(60,290);
        polygonCoordinates[2] = new Coordinate(60,310);
        polygonCoordinates[3] = new Coordinate(70,320);
        polygonCoordinates[4] = new Coordinate(90,310);
        polygonCoordinates[5] = new Coordinate(110,320);
        polygonCoordinates[6] = new Coordinate(130,310);
        polygonCoordinates[7] = new Coordinate(130,290);
        polygonCoordinates[8] = new Coordinate(110,270);
        polygonCoordinates[9] = new Coordinate(70,270);
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
        Coordinate c = new Coordinate(140.0d,140.0d);
        Point point = geomFac.createPoint(c);
        return point;
    }
}


