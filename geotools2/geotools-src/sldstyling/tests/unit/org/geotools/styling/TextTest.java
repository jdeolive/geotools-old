/*
 * TextTest.java
 *
 * Created on 04 July 2002, 10:02
 */

package org.geotools.styling;

import org.geotools.renderer.*;
import org.geotools.data.*;
import com.vividsolutions.jts.geom.*;
import com.vividsolutions.jts.geom.Envelope;
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
import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.*;
import java.io.*;
import javax.imageio.*;
import javax.swing.*;


/**
 *
 * @author  iant
 */
public class TextTest extends TestCase {
    
    /** Creates a new instance of TextTest */
    public TextTest(java.lang.String testName) {
        super(testName);
       
    }
    
    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(suite());
    }
    
    public static Test suite() {
        TestSuite suite = new TestSuite(TextTest.class);
        return suite;
    }
    
    public void testTextRender()throws Exception {
        System.out.println("\n\nText Test\n\n");
        Frame frame = new Frame("text test");
        frame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {e.getWindow().dispose(); }
        });
        Envelope ex = new Envelope(0, 50, 0, 100);
        frame.setSize(300,600);
        GeometryFactory geomFac = new GeometryFactory();
        ArrayList features = new ArrayList();
        int points = 4;
        int rows = 3;
        AttributeType[] pointAttribute = new AttributeType[3];
        pointAttribute[0] = AttributeTypeFactory.newAttributeType("centre", com.vividsolutions.jts.geom.Point.class);
        pointAttribute[1] = AttributeTypeFactory.newAttributeType("size",Double.class);
        pointAttribute[2] = AttributeTypeFactory.newAttributeType("rotation",Double.class);
        FeatureType pointType = FeatureTypeFactory.newFeatureType(pointAttribute,"testPoint");
        for(int j=0;j<rows;j++){
            double angle =0.0;
            for(int i=0; i<points; i++){

                Point point = makeSamplePoint(geomFac,
                    2.0+(double)i*((ex.getWidth()-4)/points), 
                    50.0+(double)j*((50)/rows));
                
                Double size = new Double(5.0+j*5);
                Double rotation = new Double(angle);
                angle+=90.0;
                Feature pointFeature = pointType.create(new Object[]{point,size,rotation});
//                System.out.println(""+pointFeature);
                features.add(pointFeature);
            }
        }
        
        AttributeType[] lineAttribute = new AttributeType[3];
        lineAttribute[0] = AttributeTypeFactory.newAttributeType("edge", com.vividsolutions.jts.geom.LineString.class);
        lineAttribute[1] = AttributeTypeFactory.newAttributeType("size",Double.class);
        lineAttribute[2] = AttributeTypeFactory.newAttributeType("perpendicularoffset",Double.class);
        FeatureType lineType = FeatureTypeFactory.newFeatureType(lineAttribute,"testLine");
        rows = 2;
        points = 3;
        
        double off = 1;
        for(int j=0;j<rows;j++){
            double angle =0.0;
            int sign = 1;
            for(int i=0; i<points; i++){
                LineString line = makeSimpleLineString(geomFac,i*ex.getWidth()/points,j*20,sign*20,20);
                Double size = new Double(12);
                Double poffset = new Double(off);
                sign--;
                Feature lineFeature = lineType.create(new Object[]{line,size,poffset});
                features.add(lineFeature);
            }   
            off-=2;
        }
        
        System.out.println("got "+features.size()+" features");
        FeatureCollection ft = FeatureCollections.newCollection();
        ft.addAll(features);
        
        org.geotools.map.Map map = new DefaultMap();
//        String dataFolder = System.getProperty("dataFolder");
//        if(dataFolder==null){
//            //then we are being run by maven
//            dataFolder = System.getProperty("basedir");
//            if(dataFolder == null) dataFolder = ".";
//            dataFolder+="/tests/unit/testData";
//        }
        java.net.URL url = getClass().getResource("testData/");
        File f = new File(url.getPath(),"textTest.sld");
        System.out.println("testing reader using "+f.toString());
        StyleFactory factory = StyleFactory.createStyleFactory();
        SLDStyle stylereader = new SLDStyle(factory,f);
        Style[] style = stylereader.readXML();
        map.addFeatureTable(ft,style[0]);
        Java2DRenderer renderer = new org.geotools.renderer.Java2DRenderer();
        
        Panel p = new Panel();
        frame.add(p);
        
        frame.setLocation(600,0);
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
        File file = new File(url.getPath(), "TextTest.jpg"); 
        FileOutputStream out = new FileOutputStream(file);
        ImageIO.write(image, "JPEG", out); 
        
        //Thread.sleep(5000);
        frame.dispose();
    }
    private Point makeSamplePoint(final GeometryFactory geomFac,double x, double y) {
        Coordinate c = new Coordinate(x,y);
        Point point = geomFac.createPoint(c);
        return point;
    }
    private LineString makeSampleLineString(final GeometryFactory geomFac, double xoff, double yoff) {
        Coordinate[] linestringCoordinates = new Coordinate[4];
        linestringCoordinates[0] = new Coordinate(0.0d+xoff,0.0d+yoff);
        linestringCoordinates[1] = new Coordinate(10.0d+xoff,10.0d+yoff);
        linestringCoordinates[2] = new Coordinate(15.0d+xoff,20.0d+yoff);
        linestringCoordinates[3] = new Coordinate(20.0d+xoff,30.0d+yoff);
        
        LineString line = geomFac.createLineString(linestringCoordinates);
        
        return line;
    }
    private LineString makeSimpleLineString(final GeometryFactory geomFac, double xstart, double ystart, double width, double height) {
        Coordinate[] linestringCoordinates = new Coordinate[2];
        linestringCoordinates[0] = new Coordinate(xstart,ystart);
        
        linestringCoordinates[1] = new Coordinate(xstart+width,ystart+height);
        
        LineString line = geomFac.createLineString(linestringCoordinates);
        
        return line;
    }
}
