/*
 * ProjectionTestSuite.java
 * JUnit based test
 *
 * Created on February 22, 2002, 3:58 PM
 */                

package org.geotools.shapefile;

import junit.framework.*;


/**
 *
 * @author jamesm
 */                                
public class ShapefileSuite extends TestCase {
    
    public ShapefileSuite(java.lang.String testName) {
        super(testName);
    }        
    
    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(suite());
    }
    
    public static Test suite() {
        if (System.getProperty("dataFolder") == null)
        System.setProperty("dataFolder", ShapefileTest.class.getResource("/testData").getFile());
        TestSuite suite = new TestSuite("All Tests");
        suite.addTestSuite(DbaseFileTest.class);
        suite.addTestSuite(ShapefileDataSourceTest.class);
        suite.addTestSuite(ShapefileTest.class);
        return suite;
    }
}
