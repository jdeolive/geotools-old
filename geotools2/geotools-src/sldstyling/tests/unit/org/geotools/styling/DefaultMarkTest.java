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
public class DefaultMarkTest extends TestCase {
    
    public DefaultMarkTest(java.lang.String testName) {
        super(testName);
        //BasicConfigurator.configure();
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
        EnvelopeExtent ex = new EnvelopeExtent(0, 35, 0, 45);
        
        GeometryFactory geomFac = new GeometryFactory();
        ArrayList features = new ArrayList();
        
        AttributeType[] pointAttribute = new AttributeType[4];
        pointAttribute[0] = new AttributeTypeDefault("centre", com.vividsolutions.jts.geom.Point.class);
        pointAttribute[1] = new AttributeTypeDefault("size",Double.class);
        pointAttribute[2] = new AttributeTypeDefault("rotation",Double.class);
        pointAttribute[3] = new AttributeTypeDefault("name",String.class);
        FeatureType pointType = new FeatureTypeFlat(pointAttribute).setTypeName("testPoint");
        FeatureFactory pointFac = new FeatureFactory(pointType);
        
        AttributeType[] labelAttribute = new AttributeType[4];
        labelAttribute[0] = new AttributeTypeDefault("centre", com.vividsolutions.jts.geom.Point.class);
        labelAttribute[1] = new AttributeTypeDefault("name",String.class);
        labelAttribute[2] = new AttributeTypeDefault("X",Double.class);
        labelAttribute[3] = new AttributeTypeDefault("Y",Double.class);
        FeatureType labelType = new FeatureTypeFlat(labelAttribute).setTypeName("labelPoint");
        FeatureFactory labelFac = new FeatureFactory(labelType);
        String[] marks = {"Circle","Triangle","Cross","Star","X","Square","Arrow"};
        double size = 6;
        double rotation = 0;
        int rows = 7;
        for(int j=0;j<rows;j++){
            Point point = makeSamplePoint(geomFac,2,5.0+j*5);
            Feature pointFeature = labelFac.create(new Object[]{point,""+size+"/"+rotation,new Double(0.3),new Double(.5)});
            features.add(pointFeature);
            for(int i=0; i<marks.length; i++){
                point = makeSamplePoint(geomFac,(double)i*5.0+10.0, 5.0+j*5);
                pointFeature = pointFac.create(new Object[]{point,new Double(size),new Double(rotation),marks[i]});
                features.add(pointFeature);
            }
            size+=2;
            rotation+=45;
        }
        for(int i=0; i<marks.length; i++){
            Point point = makeSamplePoint(geomFac,(double)i*5.0+10.0,5.0+rows*5);
            Feature pointFeature = labelFac.create(new Object[]{point,marks[i],new Double(.5),new Double(0)});
            features.add(pointFeature);
        }
        System.out.println("got "+features.size()+" features");
        FeatureCollectionDefault ft = new FeatureCollectionDefault();
        ft.addFeatures(features);
        
        org.geotools.map.Map map = new DefaultMap();
        
        File f = new File(System.getProperty("dataFolder"),"markTest.sld");
        System.out.println("testing reader using "+f.toString());
        SLDStyle style = new SLDStyle(f);
        map.addFeatureTable(ft,style);
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
        map.render(renderer,ex.getBounds());//and finaly try and draw it!
        Thread.sleep(5000);
    }
    
    private Point makeSamplePoint(final GeometryFactory geomFac,double x, double y) {
        Coordinate c = new Coordinate(x,y);
        Point point = geomFac.createPoint(c);
        return point;
    }
}



