/*
 * FilterSuite.java
 * JUnit based test
 *
 * Created on June 21, 2002, 12:30 PM
 */

package org.geotools.data.mapinfo;

import junit.framework.*;


/**
 *
 * @author jamesm
 */
public class MifMidSuite extends TestCase {
    
    public MifMidSuite(java.lang.String testName) {
        super(testName);
         
    }
    
    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(suite());
    }
    
    public static Test suite() {
        TestSuite suite = new TestSuite("All mifmid tests");
        suite.addTestSuite(LoadTest.class);
        suite.addTestSuite(TestStyling.class);
        return suite;
    }
    
    
}
