/*
 * EllipseTest.java
 * JUnit based test
 *
 * Created on 24 February 2002, 12:28
 */                

package org.geotools.proj4j;

import junit.framework.*;

/**
 *
 * @author James Macgill
 */                                
public class EllipseTest extends TestCase {
    
    public EllipseTest(java.lang.String testName) {
        super(testName);
    }        
    
    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(suite());
    }
    
    public static Test suite() {
        TestSuite suite = new TestSuite(EllipseTest.class);
        
        return suite;
    }
    
    /** Test of getEllips method, of class org.geotools.proj4j.Ellips. */
    public void testGetDefaultsForEllipse() {
        System.out.println("testGetEllips");
        String[] test1 = Ellipse.getDefaultsForEllipse("MERIT");
        assertNotNull(test1);
        
        System.out.println("    checking values are correct");
        assertEquals("a=6378137.0",test1[1]);
        assertEquals("rf=298.257",test1[2]);
        assertEquals("MERIT 1983",test1[3]);
        
        System.out.println("    checking case insensitive fetch");
        String[] test2 = Ellipse.getDefaultsForEllipse("merit");
        assertSame(test1,test2);
        
        System.out.println("    testing non existent ellipse is null");
        String[] test3 = Ellipse.getDefaultsForEllipse("foobar");
        assertNull(test3);
    }
     
    // Add test methods here, they have to start with 'test' name.
    // for example: 
    // public void testHello() {}
}
