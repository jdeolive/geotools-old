/*
 * FlatFeatureFactorySpiTest.java
 * JUnit based test
 *
 * Created on May 20, 2003, 11:26 AM
 */

package org.geotools.feature;

import junit.framework.*;
import org.geotools.feature.FeatureFactory;
import org.geotools.feature.FeatureFactoryFinder;

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
        FeatureType type = SampleFeatureFixtures.createFeature().getSchema();
        FeatureFactory fac = FeatureFactoryFinder.getFeatureFactory(type);
        assertNotNull(fac);
    }
    
}
