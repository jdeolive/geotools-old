/*
 * MiscTest.java
 * JUnit based test
 *
 * Created on February 21, 2002, 2:07 PM
 */                

package org.geotools.proj4j;

import junit.framework.*;
import java.util.StringTokenizer;

/**
 *
 * @author jamesm
 */                                
public class MiscTest extends TestCase {
    
    public MiscTest(java.lang.String testName) {
        super(testName);
    }        
    
    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(suite());
    }
    
    public static Test suite() {
        TestSuite suite = new TestSuite(MiscTest.class);
        
        return suite;
    }
    
    /** Test of adjlon method, of class org.geotools.proj4j.Misc. */
    public void testAdjlon() {
        System.out.println("testAdjlon");
        
        // Add your test code below by replacing the default call to fail.
        fail("The test case is empty.");
    }
    
    /** Test of dmsToR method, of class org.geotools.proj4j.Misc. */
    public void testDmsToR() {
        System.out.println("testDmsToR");
        
        assertEquals(10d,Misc.dmsToR("10"),0);
    }
    
    // Add test methods here, they have to start with 'test' name.
    // for example: 
    // public void testHello() {}


}
