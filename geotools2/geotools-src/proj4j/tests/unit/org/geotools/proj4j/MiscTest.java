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
        // "increase" Longitude by a turn and a half
        assertEquals(-Math.PI/2,Misc.adjlon(Math.PI/2+Math.PI*3),1e-10);
        assertEquals(-Math.PI/2,Misc.adjlon(Math.PI/2-Math.PI*3),1e-10);
        assertEquals(Math.PI/2,Misc.adjlon(Math.PI/2+Math.PI*4),1e-10);
        assertEquals(Math.PI/2,Misc.adjlon(Math.PI/2-Math.PI*4),1e-10);
        assertEquals(Math.PI/2,Misc.adjlon(-Math.PI/2-Math.PI*3),1e-10);
        assertEquals(-Math.PI/2,Misc.adjlon(-Math.PI/2+Math.PI*4),1e-10);
        // no change as in range
        assertEquals(Math.PI/2,Misc.adjlon(Math.PI/2),1e-10);
        assertEquals(-Math.PI/2,Misc.adjlon(-Math.PI/2),1e-10);
        assertEquals(0,Misc.adjlon(Math.PI*2),0);
        assertEquals(Math.PI,Math.abs(Misc.adjlon(Math.PI*3)),0);
        // Add your test code below by replacing the default call to fail.
        
    }
    
    /** Test of dmsToR method, of class org.geotools.proj4j.Misc. */
    public void testDmsToR() {
        System.out.println("testDmsToR");
        
        assertEquals(0.17453292519943298,Misc.dmsToR("10"),0);
        assertEquals(Math.PI,Misc.dmsToR("180"),1e-12);
        assertEquals(-Math.PI,Misc.dmsToR("180s"),1e-12);
        assertEquals(-Math.PI,Misc.dmsToR("-180"),1e-12);
        assertEquals(Math.PI/2.0,Misc.dmsToR("90E"),1e-12);
        assertEquals(-Math.PI/2.0,Misc.dmsToR("90W"),1e-12);
        assertEquals(0.19794943,Misc.dmsToR("11d20'30\""),1e-5);
        assertEquals(Misc.dmsToR("13d0'0\""),Misc.dmsToR("13d"),1e-5);//no min or sec
        assertEquals(0.19794943,Misc.dmsToR("11.341667"),1e-5);
        assertEquals(10,Misc.dmsToR("10r"),0);
        assertEquals(Double.MAX_VALUE,Misc.dmsToR("abcd"),0);
        
    }
    
    // Add test methods here, they have to start with 'test' name.
    // for example:
    // public void testHello() {}
    
    
}
