/*
 * RenderStyleTest.java
 *
 * Created on 27 May 2002, 15:40
 */

package org.geotools.styling;
import org.geotools.renderer.*;
import org.geotools.data.*;
import com.vividsolutions.jts.geom.*;
import org.geotools.feature.*;
import org.geotools.styling.*;
import org.geotools.map.*;
import java.util.*;
import java.io.*;
import junit.framework.*;
import java.awt.Frame;
import java.awt.Panel;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.*;
import java.io.*;
import javax.imageio.*;
import javax.swing.*;


/**
 *
 * @author jamesm
 */
public class DefaultMarkTest extends TestCase {
    
    public DefaultMarkTest(java.lang.String testName) {
        super(testName);
        
    }
    
    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(suite());
    }
    
    public static Test suite() {
        TestSuite suite = new TestSuite(DefaultMarkTest.class);
        return suite;
    }
    
    public void testSimpleRender()throws Exception {
        //same as the datasource test, load in some features into a table
        System.out.println("\n\nMark Test\n");
        // Request extent
        Envelope ex = new Envelope(0, 45,0,45);
        //EnvelopeExtent ex = new EnvelopeExtent(0, 45, 0, 45);
        
        GeometryFactory geomFac = new GeometryFactory();
        ArrayList features = new ArrayList();
        
        AttributeType[] pointAttribute = new AttributeType[4];
        pointAttribute[0] = AttributeTypeFactory.newAttributeType("centre", com.vividsolutions.jts.geom.Point.class);
        pointAttribute[1] = AttributeTypeFactory.newAttributeType("size",Double.class);
        pointAttribute[2] = AttributeTypeFactory.newAttributeType("rotation",Double.class);
        pointAttribute[3] = AttributeTypeFactory.newAttributeType("name",String.class);
        FeatureType pointType = FeatureTypeFactory.newFeatureType(pointAttribute,"testPoint");
        
        AttributeType[] labelAttribute = new AttributeType[4];
        labelAttribute[0] = AttributeTypeFactory.newAttributeType("centre", com.vividsolutions.jts.geom.Point.class);
        labelAttribute[1] = AttributeTypeFactory.newAttributeType("name",String.class);
        labelAttribute[2] = AttributeTypeFactory.newAttributeType("X",Double.class);
        labelAttribute[3] = AttributeTypeFactory.newAttributeType("Y",Double.class);
        FeatureType labelType = FeatureTypeFactory.newFeatureType(labelAttribute,"labelPoint");
        String[] marks = {"Circle","Triangle","Cross","Star","X","Square","Arrow"};
        double size = 6;
        double rotation = 0;
        int rows = 7;
        for(int j=0;j<rows;j++){
            Point point = makeSamplePoint(geomFac,2,5.0+j*5);
            Feature pointFeature = labelType.create(new Object[]{point,""+size+"/"+rotation,new Double(0.3),new Double(.5)});
            features.add(pointFeature);
            for(int i=0; i<marks.length; i++){
                point = makeSamplePoint(geomFac,(double)i*5.0+10.0, 5.0+j*5);
                pointFeature = pointType.create(new Object[]{point,new Double(size),new Double(rotation),marks[i]});
                features.add(pointFeature);
            }
            size+=2;
            rotation+=45;
        }
        for(int i=0; i<marks.length; i++){
            Point point = makeSamplePoint(geomFac,(double)i*5.0+10.0,5.0+rows*5);
            Feature pointFeature = labelType.create(new Object[]{point,marks[i],new Double(.5),new Double(0)});
            features.add(pointFeature);
        }
//        System.out.println("got "+features.size()+" features");
        FeatureCollection ft = FeatureCollections.newCollection();
        ft.addAll(features);
        
        org.geotools.map.Map map = new DefaultMap();
        java.net.URL base = getClass().getResource("testData/");
//        String dataFolder = System.getProperty("dataFolder");
//        if(dataFolder==null){
//            //then we are being run by maven
//            dataFolder = System.getProperty("basedir");
//            if(dataFolder == null) dataFolder = ".";
//            dataFolder+="/tests/unit/testData";
//        }
//        File f = new File(dataFolder,"markTest.sld");
//        System.out.println("testing reader using "+ url);
        
        
        StyleFactory factory = StyleFactory.createStyleFactory();
        java.net.URL surl = new java.net.URL(base + "markTest.sld");
        SLDStyle stylereader = new SLDStyle(factory,surl);
        Style[] style = stylereader.readXML();
        
        map.addFeatureTable(ft,style[0]);
        Java2DRenderer renderer = new org.geotools.renderer.Java2DRenderer();
        Frame frame = new Frame("default mark test");
        frame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {e.getWindow().dispose(); }
        });
        Panel p = new Panel();
        frame.add(p);
        frame.setSize(300,300);
        frame.setLocation(300,0);
        frame.setVisible(true);
        renderer.setOutput(p.getGraphics(),p.getBounds());
        map.render(renderer,ex);//and finaly try and draw it!
        int w=400, h=400;
        BufferedImage image = new BufferedImage(w,h,BufferedImage.TYPE_INT_RGB);
        Graphics g = image.getGraphics();
        g.setColor(Color.white);
        g.fillRect(0,0,w,h);
        renderer.setOutput(g,new java.awt.Rectangle(0,0,w,h));
        map.render(renderer,ex);//and finaly try and draw it!
        File file = new File(base.getPath(), "DefaultMarkTest.jpg"); 
        FileOutputStream out = new FileOutputStream(file);
        boolean fred = ImageIO.write(image, "JPEG", out); 
        if(!fred){
            System.out.println("Failed to write image to " + file.toString());
        }
        
        //Thread.sleep(5000);
        frame.dispose();
    }
    
    private Point makeSamplePoint(final GeometryFactory geomFac,double x, double y) {
        Coordinate c = new Coordinate(x,y);
        Point point = geomFac.createPoint(c);
        return point;
    }
}



