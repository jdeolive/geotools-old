/*
 * ProjectionsSuite.java
 * JUnit based test
 *
 * Created on February 22, 2002, 4:11 PM
 */                

package org.geotools.proj4j.projections;

import junit.framework.*;

/**
 *
 * @author jamesm
 */                                
public class ProjectionsSuite extends TestCase {
    
    public ProjectionsSuite(java.lang.String testName) {
        super(testName);
    }        
    
    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(suite());
    }
    
    public static Test suite() {
        
        
        TestSuite suite = new TestSuite("ProjectionsSuite");
        suite.addTestSuite(TmercTest.class);
        suite.addTestSuite(UtmTest.class);
        suite.addTestSuite(AeaTest.class);
       
        return suite;
    }
    
    // Add test methods here, they have to start with 'test' name.
    // for example: 
    // public void testHello() {}


}
