/*
 * StylingTest.java
 * JUnit based test
 *
 * Created on April 12, 2002, 1:18 PM
 */

package org.geotools.rendering;
import org.geotools.renderer.*;
import org.geotools.data.*;
import com.vividsolutions.jts.geom.*;
import org.geotools.feature.*;
import org.geotools.styling.*;
import org.geotools.map.*;
import org.geotools.filter.*;
import java.util.*;
import junit.framework.*;
import java.awt.Frame;
import java.awt.Panel;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

//Logging system
import java.util.logging.Logger;
import java.util.logging.Level;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.*;
import java.io.*;
import javax.imageio.*;

import javax.swing.*;


/**
 *
 * @author jamesm
 */
public class Rendering2DTest extends TestCase {
    /**
     * The logger for the rendering module.
     */
    private static final Logger LOGGER = Logger.getLogger(
                                                 "org.geotools.rendering");
    private static final org.geotools.filter.FilterFactory filterFactory = org.geotools.filter.FilterFactory.createFilterFactory();
    public Rendering2DTest(java.lang.String testName) {
        super(testName);
    }
    
    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(suite());
    }
    
    public static Test suite() {
        TestSuite suite = new TestSuite(Rendering2DTest.class);
        return suite;
    }
    
    public void testSimpleRender()throws Exception {
        //same as the datasource test, load in some features into a table
        System.err.println("starting rendering2DTest");
        // Request extent
        Envelope ex = new Envelope(5, 15, 5, 15);
        
        AttributeType[] types = new AttributeType[1];
        
        GeometryFactory geomFac = new GeometryFactory();
        LineString line = makeSampleLineString(geomFac);
        types[0] = AttributeTypeFactory.newAttributeType("centerline", line.getClass());
        FeatureType lineType = FeatureTypeFactory.newFeatureType(types,"linefeature");
        Feature lineFeature = lineType.create(new Object[]{line});
        
        com.vividsolutions.jts.geom.Polygon polygon = makeSamplePolygon(geomFac);
        
        types[0] = AttributeTypeFactory.newAttributeType("edge", polygon.getClass());
        FeatureType polygonType = FeatureTypeFactory.newFeatureType(types,"polygonfeature");
        
        Feature polygonFeature = polygonType.create(new Object[]{polygon});
        
        Point point = makeSamplePoint(geomFac);
        types[0] = AttributeTypeFactory.newAttributeType("centre", point.getClass());
        FeatureType pointType =FeatureTypeFactory.newFeatureType(types,"pointfeature");
        
        Feature pointFeature = pointType.create(new Object[]{point});
        
        FeatureCollection ft = FeatureCollections.newCollection();
        ft.add(lineFeature);
        ft.add(polygonFeature);
        ft.add(pointFeature);
        
        org.geotools.map.Map map = new DefaultMap();
        StyleFactory sFac = StyleFactory.createStyleFactory();
        //The following is complex, and should be built from
        //an SLD document and not by hand
        PointSymbolizer pointsym = sFac.createPointSymbolizer();
        pointsym.setGraphic(sFac.getDefaultGraphic());
        
        LineSymbolizer linesym = sFac.createLineSymbolizer();
        Stroke myStroke = sFac.getDefaultStroke();
        myStroke.setColor(filterFactory.createLiteralExpression("#0000ff"));
        myStroke.setWidth(filterFactory.createLiteralExpression(new Integer(5)));
        LOGGER.info("got new Stroke " + myStroke);
        linesym.setStroke(myStroke);
        
        PolygonSymbolizer polysym = sFac.createPolygonSymbolizer();
        Fill myFill = sFac.getDefaultFill();
        myFill.setColor(filterFactory.createLiteralExpression("#ff0000"));
        polysym.setFill(myFill);
        polysym.setStroke(sFac.getDefaultStroke());
        Rule rule = sFac.createRule();
        rule.setSymbolizers(new Symbolizer[]{polysym});
        FeatureTypeStyle fts = sFac.createFeatureTypeStyle(new Rule[]{rule});
        fts.setFeatureTypeName("polygonfeature");
        
        Rule rule2 = sFac.createRule();
        rule2.setSymbolizers(new Symbolizer[]{linesym});
        FeatureTypeStyle fts2 = sFac.createFeatureTypeStyle();
        fts2.setRules(new Rule[]{rule2});
        fts2.setFeatureTypeName("linefeature");
        
        Rule rule3 = sFac.createRule();
        rule3.setSymbolizers(new Symbolizer[]{pointsym});
        FeatureTypeStyle fts3 = sFac.createFeatureTypeStyle();
        fts3.setRules(new Rule[]{rule3});
        fts3.setFeatureTypeName("pointfeature");
        Style style = sFac.createStyle();
        style.setFeatureTypeStyles(new FeatureTypeStyle[]{fts,fts2,fts3});
        
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
        map.render(renderer,ex);//and finaly try and draw it!
        int w = 300, h = 600;
        BufferedImage image = new BufferedImage(w,h,BufferedImage.TYPE_INT_RGB);
        Graphics g = image.getGraphics();
        g.setColor(Color.white);
        g.fillRect(0,0,w,h);
        renderer.setOutput(g,new java.awt.Rectangle(0,0,w,h));
        map.render(renderer,ex);//and finaly try and draw it!
//        String dataFolder = System.getProperty("dataFolder");
//        if(dataFolder==null){
//            //then we are being run by maven
//            dataFolder = System.getProperty("basedir");
//            dataFolder+="/tests/unit/testData";
//        }
        java.net.URL base = getClass().getResource("testData/");
        File file = new File(base.getPath(), "RendererStyle.jpg"); 
        FileOutputStream out = new FileOutputStream(file);
        ImageIO.write(image, "JPEG", out); 
        
        //Thread.sleep(5000);
        frame.dispose();
        
    }
    
     public void testPixelToWorld()throws Exception {
        //same as the datasource test, load in some features into a table
        //System.err.println("starting rendering2DTest");
        // Request extent
        Envelope ex = new Envelope(0, 10, 0, 10);
        
        
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
       
        Coordinate c = renderer.pixelToWorld(150,150,ex);
        LOGGER.info("X Coordinate is " + c.x + " expected is 5 +/- 1.0" );
        LOGGER.info("Y Coordinate is " + c.y + " expected is 5 +/- 1.0" );
        assertEquals(5d, c.x, 1.0);
        assertEquals(5d, c.y, 1.0);
       
        
        frame.dispose();
    }
    private Point makeSamplePoint(final GeometryFactory geomFac) {
        Coordinate c = new Coordinate(14.0d,14.0d);
        Point point = geomFac.createPoint(c);
        return point;
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
}
