/*
 * SLDStyleTest.java
 * JUnit based test
 *
 * Created on 22 May 2002, 16:33
 */

package org.geotools.styling;

import junit.framework.*;
import org.w3c.dom.*;
import javax.xml.parsers.*;
import java.net.*;
import java.io.*;
import org.apache.log4j.Logger;
import org.apache.log4j.BasicConfigurator;

/**
 *
 * @author iant
 */
public class SLDStyleTest extends TestCase {
    
    public SLDStyleTest(java.lang.String testName) {
        super(testName);
        BasicConfigurator.configure();
    }
    
    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(suite());
    }
    
    public static Test suite() {
        TestSuite suite = new TestSuite(SLDStyleTest.class);
        
        return suite;
    }
    
    /** Test of getAbstract method, of class org.geotools.styling.SLDStyle. */
    public void testGetAbstract() {
        System.out.println("testGetAbstract");
        
        // Add your test code below by replacing the default call to fail.
        //fail("The test case is empty.");
    }
    
    /** Test of getFeatureTypeStyles method, of class org.geotools.styling.SLDStyle. */
    public void testGetFeatureTypeStyles() {
        System.out.println("testGetFeatureTypeStyles");
        
        // Add your test code below by replacing the default call to fail.
        //fail("The test case is empty.");
    }
    
    /** Test of getName method, of class org.geotools.styling.SLDStyle. */
    public void testGetName() {
        System.out.println("testGetName");
        
        // Add your test code below by replacing the default call to fail.
        //fail("The test case is empty.");
    }
    
    /** Test of getTitle method, of class org.geotools.styling.SLDStyle. */
    public void testGetTitle() {
        System.out.println("testGetTitle");
        
        // Add your test code below by replacing the default call to fail.
        //fail("The test case is empty.");
    }
    
    /** Test of isDefault method, of class org.geotools.styling.SLDStyle. */
    public void testIsDefault() {
        System.out.println("testIsDefault");
        
        // Add your test code below by replacing the default call to fail.
        //fail("The test case is empty.");
    }
    
    /** Test of getAbstractStr method, of class org.geotools.styling.SLDStyle. */
    public void testGetAbstractStr() {
        System.out.println("testGetAbstractStr");
        
        // Add your test code below by replacing the default call to fail.
        //fail("The test case is empty.");
    }
    
    // Add test methods here, they have to start with 'test' name.
    // for example:
    // public void testHello() {}
    public void testReading(){
        File f = new File(System.getProperty("dataFolder"),"sample.sld");
        System.out.println("testing reader using "+f.toString());
        SLDStyle style = new SLDStyle(f);
    }
    
}
