/*
 * LPTest.java
 * JUnit based test
 *
 * Created on February 22, 2002, 4:48 PM
 */                

package org.geotools.proj4j;

import junit.framework.*;
import java.util.StringTokenizer;

/**
 *
 * @author jamesm
 */                                
public class LPTest extends TestCase {
    
    public LPTest(java.lang.String testName) {
        super(testName);
    }        
    
    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(suite());
    }
    
    public static Test suite() {
        TestSuite suite = new TestSuite(LPTest.class);
        
        return suite;
    }
    
    public void testConstruction(){
        System.out.println("Testing with space");
        LP lp = new LP("24N 48S");
        assertEquals(Functions.dmsToR("24N"),lp.lam,0);
        assertEquals(Functions.dmsToR("48S"),lp.phi,0);
        System.out.println("Testing with ,");
        lp = new LP("12d10'4\",34d");
        assertEquals(Functions.dmsToR("12d10'4\""),lp.lam,0);
        assertEquals(Functions.dmsToR("34d"),lp.phi,0);
    }
    // Add test methods here, they have to start with 'test' name.
    // for example: 
    // public void testHello() {}


}
