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
    public void testGetEllipse() {
        System.out.println("testGetEllips");
        Ellipse test1 = Ellipse.getEllipse("MERIT");
        assertNotNull(test1);
        
        System.out.println("    checking values are correct");
        assertEquals("a=6378137.0",test1.getMajor());
        assertEquals("rf=298.257",test1.getEll());
        assertEquals("MERIT 1983",test1.getName());
        
        System.out.println("    checking case insensitive fetch");
        Ellipse test2 = Ellipse.getEllipse("merit");
        assertSame(test1,test2);
        
        System.out.println("    testing non existent ellipse is null");
        Ellipse test3 = Ellipse.getEllipse("foobar");
        assertNull(test3);
    }
     
    // Add test methods here, they have to start with 'test' name.
    // for example: 
    // public void testHello() {}
}
