package org.geotools.gml;

/*
 * GmlSuite.java
 * JUnit based test
 *
 * Created on 04 March 2002, 16:09
 */                



import junit.framework.*;

/**
 *
 * @author ian
 */                                
public class GmlSuite extends TestCase {
    
    public GmlSuite(java.lang.String testName) {
        super(testName);
    }        
    
    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(suite());
    }
    
    public static Test suite() {
        TestSuite suite = new TestSuite(GmlSuite.class);
        return suite;
    }

    // Add test methods here, they have to start with 'test' name.
    // for example: 
    // public void testHello() {}


}
