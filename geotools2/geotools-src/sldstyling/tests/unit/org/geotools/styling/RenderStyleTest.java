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


/**
 *
 * @author jamesm,iant
 */
public class RenderStyleTest extends TestCase {
    
    public RenderStyleTest(java.lang.String testName) {
        super(testName);
        
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
        EnvelopeExtent ex = new EnvelopeExtent(40, 300, 30, 350);
        
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
        
        Polygon polygon = makeSamplePolygon(geomFac,0,0);
        
        AttributeType polygonAttribute = new AttributeTypeDefault("edge", polygon.getClass());
        FeatureType polygonType = new FeatureTypeFlat(polygonAttribute).setTypeName("polygon");
        FeatureFactory polygonFac = new FeatureFactory(polygonType);
        
        Feature polygonFeature = polygonFac.create(new Object[]{polygon});
        
        Polygon polygon2 = makeSamplePolygon(geomFac,0,150);
        polygonType = new FeatureTypeFlat(polygonAttribute).setTypeName("polygontest2");
        polygonFac = new FeatureFactory(polygonType);
        Feature polygonFeature2 = polygonFac.create(new Object[]{polygon2});
        
        Polygon polygon3 = makeSamplePolygon(geomFac,220,100);
        polygonType = new FeatureTypeFlat(polygonAttribute).setTypeName("polygontest3");
        polygonFac = new FeatureFactory(polygonType);
        Feature polygonFeature3 = polygonFac.create(new Object[]{polygon3});
        
        
        Point point = makeSamplePoint(geomFac,140.0,140.0);
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
        datasource.addFeature(polygonFeature3);
        datasource.addFeature(pointFeature);
        
        FeatureCollection ft = new FeatureCollectionDefault(datasource);
        
        org.geotools.map.Map map = new DefaultMap();
        String dataFolder = System.getProperty("dataFolder");
        if(dataFolder==null){
            //then we are being run by maven
            dataFolder = System.getProperty("basedir");
            dataFolder+="/tests/unit/testData";
        }
        
        File f = new File(dataFolder,"sample.sld");
        
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
    
    private com.vividsolutions.jts.geom.Polygon makeSamplePolygon(final GeometryFactory geomFac, double xoff, double yoff) {
        Coordinate[] polygonCoordinates = new Coordinate[10];
        polygonCoordinates[0] = new Coordinate(70+xoff,70+yoff);
        polygonCoordinates[1] = new Coordinate(60+xoff,90+yoff);
        polygonCoordinates[2] = new Coordinate(60+xoff,110+yoff);
        polygonCoordinates[3] = new Coordinate(70+xoff,120+yoff);
        polygonCoordinates[4] = new Coordinate(90+xoff,110+yoff);
        polygonCoordinates[5] = new Coordinate(110+xoff,120+yoff);
        polygonCoordinates[6] = new Coordinate(130+xoff,110+yoff);
        polygonCoordinates[7] = new Coordinate(130+xoff,90+yoff);
        polygonCoordinates[8] = new Coordinate(110+xoff,70+yoff);
        polygonCoordinates[9] = new Coordinate(70+xoff,70+yoff);
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
    
    private Point makeSamplePoint(final GeometryFactory geomFac, double x, double y) {
        Coordinate c = new Coordinate(x,y);
        Point point = geomFac.createPoint(c);
        return point;
    }
}


