/*
 * FlatFeatureFactorySpiTest.java
 * JUnit based test
 *
 * Created on May 20, 2003, 11:26 AM
 */

package org.geotools.feature;

import junit.framework.*;
import org.geotools.feature.FeatureTypeFactory;

/**
 *
 * @author jamesm
 */
public class FlatFeatureFactorySpiTest extends TestCase {
    
    public FlatFeatureFactorySpiTest(java.lang.String testName) {
        super(testName);
    }
    
    public static Test suite() {
        TestSuite suite = new TestSuite(FlatFeatureFactorySpiTest.class);
        return suite;
    }
    
    public void testObtainFactory(){
        FeatureTypeFactory factory = FeatureTypeFactory.newInstance("test");
        assertNotNull(factory);
    }
    
    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }

    
}
