/*
 * GeocentricTest.java
 * JUnit based test
 *
 * Created on 20 February 2002, 00:45
 */                

package org.geotools.proj4j;

import junit.framework.*;

/**
 *
 * @author James Macgill
 */                                
public class GeocentricTest extends TestCase {
    
    public GeocentricTest(java.lang.String testName) {
        super(testName);
    }        
    
    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(suite());
    }
    
    public static Test suite() {
        TestSuite suite = new TestSuite(GeocentricTest.class);
        
        return suite;
    }
    
    /** Test of setGeocentricParameters method, of class org.geotools.proj4j.Geocentric. */
    public void testSetGeocentricParameters() {
        System.out.println("testSetGeocentricParameters");
        
        // Add your test code below by replacing the default call to fail.
        fail("The test case is empty.");
    }
    
    /** Test of getGeocentricParameters method, of class org.geotools.proj4j.Geocentric. */
    public void testGetGeocentricParameters() {
        System.out.println("testGetGeocentricParameters");
        
        // Add your test code below by replacing the default call to fail.
        fail("The test case is empty.");
    }
    
    /** Test of convertGeodeticToGeocentric method, of class org.geotools.proj4j.Geocentric. */
    public void testConvertGeodeticToGeocentric() {
        System.out.println("testConvertGeodeticToGeocentric");
        
        // Add your test code below by replacing the default call to fail.
        fail("The test case is empty.");
    }
    
    /** Test of Convert_Geocentric_To_Geodetic method, of class org.geotools.proj4j.Geocentric. */
    public void testConvert_Geocentric_To_Geodetic() {
        System.out.println("testConvert_Geocentric_To_Geodetic");
        
        // Add your test code below by replacing the default call to fail.
        fail("The test case is empty.");
    }
    
    // Add test methods here, they have to start with 'test' name.
    // for example: 
    // public void testHello() {}


}
