/*
 * FeatureComparatorTest.java
 * NetBeans JUnit based test
 *
 * Created on 20 February 2002, 15:51
 */                

package org.geotools.datasource;
 
import junit.framework.*;
import java.util.*;
         
/**
 *
 * @author ian
 */
public class FeatureComparatorTest extends TestCase {

    public FeatureComparatorTest(java.lang.String testName) {
        super(testName);
    }        
        
    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(suite());
    }
    
    public static Test suite() {
        TestSuite suite = new TestSuite(FeatureComparatorTest.class);
        
        return suite;
    }
    
    /** Test of compare method, of class org.geotools.datasource.FeatureComparator. */
    public void testCompare() {
        System.out.println("testCompare");
        
        // Add your test code below by replacing the default call to fail.
        fail("The test case is empty.");
    }
    
    /** Test of equals method, of class org.geotools.datasource.FeatureComparator. */
    public void testEquals() {
        System.out.println("testEquals");
        
        // Add your test code below by replacing the default call to fail.
        fail("The test case is empty.");
    }
    
    // Add test methods here, they have to start with 'test' name.
    // for example: 
    // public void testHello() {}



}
