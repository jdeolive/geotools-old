/*
 * FlatFeatureFactorySpiTest.java
 * JUnit based test
 *
 * Created on May 20, 2003, 11:26 AM
 */

package org.geotools.feature;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

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
    
    public void testDefaultFactory() {
       FeatureTypeFactory factory = FeatureTypeFactory.newInstance("test");
       AttributeType a1 = AttributeTypeFactory.newAttributeType("X",Integer.class);
       AttributeType a2 = AttributeTypeFactory.newAttributeType("Y",Double.class);
       factory.addType(0,a1);
       assertEquals(a1, factory.get(factory.getAttributeCount() - 1));
       factory.addType(1,a2);
       assertEquals(a2, factory.get(factory.getAttributeCount() - 1));
       factory.removeType(1);
       factory.removeType(0);
       assertEquals(factory.getAttributeCount(),0);
       factory.addType(a1);
       factory.addType(a2);
       factory.swap(0,1);
       assertEquals(a1, factory.get(1));
       assertEquals(a2, factory.get(0));
       factory.removeType(a1);
       factory.removeType(a2);
       assertEquals(factory.getAttributeCount(),0);
    } 
    
    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }

    
}
