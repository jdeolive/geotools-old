/*
 * ProjectionTestSuite.java
 * JUnit based test
 *
 * Created on February 22, 2002, 3:58 PM
 */                

package org.geotools.proj4j;

import junit.framework.*;
import org.geotools.proj4j.projections.*;

/**
 *
 * @author jamesm
 */                                
public class Proj4jSuite extends TestCase {
    
    public Proj4jSuite(java.lang.String testName) {
        super(testName);
    }        
    
    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(suite());
    }
    
    public static Test suite() {
        TestSuite suite = new TestSuite("All Tests");
        suite.addTestSuite(ProjectionFactoryTest.class);
        suite.addTestSuite(MiscTest.class);
        suite.addTestSuite(ParamSetTest.class);
        suite.addTest(ProjectionsSuite.suite());
        suite.addTestSuite(LPTest.class);
        suite.addTestSuite(EllipseTest.class);
        suite.addTestSuite(TransformerTest.class);
        return suite;
    }
}
