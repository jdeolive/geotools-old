/*
 * ProjectionTestSuite.java
 * JUnit based test
 *
 * Created on February 22, 2002, 3:58 PM
 */                

package org.geotools;

import junit.framework.*;
import org.geotools.data.*;
import org.geotools.feature.*;

/**
 *
 * @author jamesm
 */                                
public class DefaultCoreSuite extends TestCase {
    
    public DefaultCoreSuite(java.lang.String testName) {
        super(testName);
    }        
    
    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(suite());
    }
    
    public static Test suite() {
        TestSuite suite = new TestSuite("All defaultcore tests");
        suite.addTestSuite(DatasourceTest.class);
        suite.addTestSuite(FeatureFlatTest.class);
        return suite;
    }
}
