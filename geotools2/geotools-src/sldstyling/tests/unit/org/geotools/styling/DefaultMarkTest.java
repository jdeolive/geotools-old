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
        TestSuite suite = new TestSuite(RenderStyleTest.class);
        return suite;
    }
    
    public void testSimpleRender()throws Exception {
        //same as the datasource test, load in some features into a table
        
        // Request extent
        EnvelopeExtent ex = new EnvelopeExtent(0, 35, 0, 35);
        
        GeometryFactory geomFac = new GeometryFactory();
        ArrayList features = new ArrayList();
        String[] marks = {"Circle","Triangle","Cross","Star","X","Square","Arrow"};
        for(int i=0; i<marks.length; i++){
            System.out.println("building test"+marks[i]+" at "+((double)i*5.0+2.0)+",25");
            Point point = makeSamplePoint(geomFac,(double)i*5.0+2.0, 25.0);
            System.out.println(""+point.toString());
            AttributeType pointAttribute = new AttributeTypeDefault("centre", point.getClass());
            FeatureType pointType = new FeatureTypeFlat(pointAttribute).setTypeName("test"+marks[i]);
            FeatureFactory pointFac = new FeatureFactory(pointType);
            Feature pointFeature = pointFac.create(new Object[]{point});
            System.out.println(""+pointFeature);
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



