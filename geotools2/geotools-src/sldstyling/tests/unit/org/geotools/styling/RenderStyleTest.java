/*
 * RenderStyleTest.java
 *
 * Created on 27 May 2002, 15:40
 */

package org.geotools.styling;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Polygon;
import java.util.Date;
import org.geotools.feature.Feature;
import org.geotools.feature.FeatureTypeFactory;
//import org.geotools.renderer.lite.LiteRenderer;

//import org.geotools.renderer.*;


/**
 *
 * @author jamesm,iant
 */
public class RenderStyleTest extends junit.framework.TestCase {
    
    public RenderStyleTest(java.lang.String testName) {
        super(testName);
        
    }
    
    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(suite());
    }
    
    public static junit.framework.Test suite() {
        junit.framework.TestSuite suite = new junit.framework.TestSuite(RenderStyleTest.class);
        return suite;
    }
    
    public void testSimpleRender()throws Exception {
        //same as the datasource test, load in some features into a table
        
        // Request extent
        com.vividsolutions.jts.geom.Envelope ex = new com.vividsolutions.jts.geom.Envelope(30, 350, 30, 350);
        
        org.geotools.feature.AttributeType[] types = new org.geotools.feature.AttributeType[1];
        
        GeometryFactory geomFac = new GeometryFactory();
        LineString line = makeSampleLineString(geomFac,0,0);
        types[0] = org.geotools.feature.AttributeTypeFactory.newAttributeType("centerline", line.getClass());
        org.geotools.feature.FeatureType lineType = FeatureTypeFactory.newFeatureType(types,"linefeature");
        Feature lineFeature = lineType.create(new Object[]{line});
        
        LineString line2 = makeSampleLineString(geomFac,100,0);
        lineType = FeatureTypeFactory.newFeatureType(types,"linefeature2");
        Feature lineFeature2 = lineType.create(new Object[]{line2});
        
        LineString line3 = makeSampleLineString(geomFac,150,0);
        lineType = FeatureTypeFactory.newFeatureType(types,"linefeature3");
        Feature lineFeature3 = lineType.create(new Object[]{line3});
        
        Polygon polygon = makeSamplePolygon(geomFac,0,0);
        
        types[0] = org.geotools.feature.AttributeTypeFactory.newAttributeType("edge", polygon.getClass());
        org.geotools.feature.FeatureType polygonType = FeatureTypeFactory.newFeatureType(types,"polygon");
        Feature polygonFeature = polygonType.create(new Object[]{polygon});
        
        Polygon polygon2 = makeSamplePolygon(geomFac,0,150);
        polygonType = FeatureTypeFactory.newFeatureType(types,"polygontest2");
        Feature polygonFeature2 = polygonType.create(new Object[]{polygon2});
        
        Polygon polygon3 = makeSamplePolygon(geomFac,220,100);
        polygonType = FeatureTypeFactory.newFeatureType(types,"polygontest3");
        Feature polygonFeature3 = polygonType.create(new Object[]{polygon3});
        
        
        com.vividsolutions.jts.geom.Point point = makeSamplePoint(geomFac,140.0,140.0);
        types[0] = org.geotools.feature.AttributeTypeFactory.newAttributeType("centre", point.getClass());
        org.geotools.feature.FeatureType pointType = FeatureTypeFactory.newFeatureType(types,"pointfeature");
        
        Feature pointFeature = pointType.create(new Object[]{point});
        
        org.geotools.feature.FeatureCollection ft = org.geotools.feature.FeatureCollections.newCollection();
        ft.add(lineFeature);
        ft.add(lineFeature2);
        ft.add(lineFeature3);
        ft.add(polygonFeature);
        ft.add(polygonFeature2);
        ft.add(polygonFeature3);
        ft.add(pointFeature);
        
        org.geotools.map.Map map = new org.geotools.map.DefaultMap();
        //        String dataFolder = System.getProperty("dataFolder");
        //        if(dataFolder==null){
        //            //then we are being run by maven
        //            dataFolder = System.getProperty("basedir");
        //            if(dataFolder == null) dataFolder = ".";
        //            dataFolder+="/tests/unit/testData";
        //        }
        java.net.URL base = getClass().getResource("testData/");
        java.io.File f = new java.io.File(base.getPath(),"sample.sld");
        
        System.out.println("testing reader using "+f.toString());
        StyleFactory factory = StyleFactory.createStyleFactory();
        SLDStyle stylereader = new SLDStyle(factory,f);
        Style[] style = stylereader.readXML();
        for(int i = 0; i< style.length; i++){
            map.addFeatureTable(ft,style[i]);
            org.geotools.renderer.lite.LiteRenderer renderer = new org.geotools.renderer.lite.LiteRenderer();
//            org.geotools.renderer.Java2DRenderer renderer = new org.geotools.renderer.Java2DRenderer();
            java.awt.Frame frame = new java.awt.Frame("rendering test");
            frame.addWindowListener(new java.awt.event.WindowAdapter() {
                public void windowClosing(java.awt.event.WindowEvent e) {e.getWindow().dispose(); }
            });
            java.awt.Panel p = new java.awt.Panel();
            
            frame.add(p);
            
            frame.setSize(300,300);
            p.setSize(300,300); // make the panel square ?
            frame.setLocation(300*i,0);
            frame.setVisible(true);
            renderer.setOutput(p.getGraphics(),p.getBounds());
            renderer.setInteractive(false);
            Date start = new Date();
            map.render(renderer,ex);//and finaly try and draw it!
            Date end = new Date();
            System.out.println("Time to render to screen: " +(end.getTime() - start.getTime()));
            int[] wa={400,400,600},ha={400,600,400};
            for(int j=0;j<wa.length;j++){
                int w = wa[j];
                int h= ha[j];
                
                java.awt.image.BufferedImage image = new java.awt.image.BufferedImage(w,h,java.awt.image.BufferedImage.TYPE_INT_RGB);
                java.awt.Graphics g = image.getGraphics();
                g.setColor(java.awt.Color.white);
                g.fillRect(0,0,w,h);
                renderer.setOutput(g,new java.awt.Rectangle(0,0,w,h));
                start = new Date();
                map.render(renderer,ex);//and finaly try and draw it!
                end = new Date();
                System.out.println("Time to render to image: " +(end.getTime() - start.getTime()));
                java.io.File file = new java.io.File(base.getPath(), "RenderStyleTest"+i+"_"+j+".png");
                java.io.FileOutputStream out = new java.io.FileOutputStream(file);
                javax.imageio.ImageIO.write(image, "PNG", out);
            }
            //Thread.sleep(5000);
            frame.dispose();
        }
        
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
    
    private Polygon makeSamplePolygon(final GeometryFactory geomFac, double xoff, double yoff) {
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
            com.vividsolutions.jts.geom.LinearRing ring = geomFac.createLinearRing(polygonCoordinates);
            Polygon polyg = geomFac.createPolygon(ring,null);
            return polyg;
        }
        catch(com.vividsolutions.jts.geom.TopologyException te){
            fail("Error creating sample polygon for testing "+te);
        }
        return null;
    }
    
    private com.vividsolutions.jts.geom.Point makeSamplePoint(final GeometryFactory geomFac, double x, double y) {
        Coordinate c = new Coordinate(x,y);
        com.vividsolutions.jts.geom.Point point = geomFac.createPoint(c);
        return point;
    }
}


