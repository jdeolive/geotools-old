/*
 * FeatureTableModelTest.java
 * JUnit based test
 *
 * Created on March 18, 2002, 4:24 PM
 */

package org.geotools.gui.swing;

import junit.framework.*;
import org.geotools.map.ContextFactory;
import org.geotools.map.BoundingBox;
import org.geotools.ct.Adapters;
import org.geotools.cs.GeographicCoordinateSystem;

import com.vividsolutions.jts.geom.*;

import javax.swing.*;

/**
 *
 * @author jamesm
 */
public class NavigationPaneTest extends TestCase {
    
    public NavigationPaneTest(java.lang.String testName) {
        super(testName);
    }
    
    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(suite());
    }
    
    public static Test suite() {
        TestSuite suite = new TestSuite(NavigationPaneTest.class);
        return suite;
    }
    
    public void testDisplay() throws Exception{
        JFrame frame = new JFrame();
        frame.setSize(400,400);
        NavigationPane nav = new NavigationPane();
        frame.getContentPane().add(nav,"Center");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setVisible(true);
        
        ContextFactory contextFactory = ContextFactory.createFactory();
        BoundingBox aoi = 
            contextFactory.createBoundingBox(new Envelope(20, 40, 30, 70), 
            Adapters.getDefault().export(GeographicCoordinateSystem.WGS84) );
        nav.setAoiModel(aoi);
        nav.setTotalAoi(new Envelope(0,500,0,500));
        for(int i = 0; i < 10; i++){
            Thread.sleep(100);
            aoi.setAreaOfInterest( new Envelope( 0.5d, 0.5d, 0.5d, 0.5d ) );
        }
        frame.dispose();
    }
    
    
}
