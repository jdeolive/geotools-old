/*
 * PNGDataSourceTest.java
 * JUnit based test
 *
 * Created on 30 October 2002, 17:25
 */

package org.geotools.coverage;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import java.io.IOException;
import junit.framework.*;
import org.geotools.data.DataSourceException;
import org.geotools.feature.FeatureCollection;
import org.geotools.filter.Filter;


/**
 *
 * @author iant
 */
public class TestImageDataSource extends TestCase {
    
    public TestImageDataSource(java.lang.String testName) {
        super(testName);
    }
    
    public static void main(java.lang.String[] args) throws Exception {
        junit.textui.TestRunner.run(suite());
    }
    
    public static Test suite() throws Exception {
        TestSuite suite = new TestSuite(TestImageDataSource.class);
        
        return suite;
    }
    private static boolean setup = false;
    private static ImageDataSource ds;
    private static String dataFolder;
    
    public void setUp() throws Exception {
        if(setup) return;
        setup = true;
        dataFolder = System.getProperty("dataFolder");
        if(dataFolder==null){
            //then we are being run by maven
            dataFolder = System.getProperty("basedir");
            dataFolder+="/tests/unit/testData/";
        }
        ds = new ImageDataSource(dataFolder+"etopo.png");
        
        if(ds == null){
            fail("unable to build datasource " + dataFolder+"etopo.png");
        }
        System.out.println("get a datasource " + ds);
    }
    
    
    
    
    
    
    /** Test of getFeatures method, of class org.geotools.coverage.PNGDataSource. */
    public void testGetFeatures() throws Exception{
        System.out.println("testGetFeatures");
        
        FeatureCollection fc = ds.getFeatures(null);
        System.out.println("got " + fc.size() + " features ");
        
    }
    
    /** Test of getBbox method, of class org.geotools.coverage.PNGDataSource. */
    public void testGetBbox() {
        System.out.println("testGetBbox");
        Envelope env = ds.getBbox(false);
        assertNotNull("null bounding box",env);
        System.out.println("Bounding box = " + env.toString());
        
        
    }
    // Add test methods here, they have to start with 'test' name.
    // for example:
    // public void testHello() {}
    
    
}
