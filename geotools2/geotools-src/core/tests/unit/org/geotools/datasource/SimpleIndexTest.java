/*
 * SimpleIndexTest.java
 * NetBeans JUnit based test
 *
 * Created on 20 February 2002, 15:51
 */                

package org.geotools.datasource;
 
import junit.framework.*;
import com.sun.java.util.collections.List;
import com.sun.java.util.collections.Collections;
import com.sun.java.util.collections.Iterator;
import com.sun.java.util.collections.Vector;
         
/**
 *
 * @author ian
 */
public class SimpleIndexTest extends TestCase {

    public SimpleIndexTest(java.lang.String testName) {
        super(testName);
    }        
        
    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(suite());
    }
    
    public static Test suite() {
        TestSuite suite = new TestSuite(SimpleIndexTest.class);
        
        return suite;
    }
    
    /** Test of rebuild method, of class org.geotools.datasource.SimpleIndex. */
    public void testRebuild() {
        System.out.println("testRebuild");
        
        // Add your test code below by replacing the default call to fail.
        fail("The test case is empty.");
    }
    
    /** Test of getState method, of class org.geotools.datasource.SimpleIndex. */
    public void testGetState() {
        System.out.println("testGetState");
        
        // Add your test code below by replacing the default call to fail.
        fail("The test case is empty.");
    }
    
    /** Test of getFeatures method, of class org.geotools.datasource.SimpleIndex. */
    public void testGetFeatures() {
        System.out.println("testGetFeatures");
        
        // Add your test code below by replacing the default call to fail.
        fail("The test case is empty.");
    }
    
    // Add test methods here, they have to start with 'test' name.
    // for example: 
    // public void testHello() {}



}
