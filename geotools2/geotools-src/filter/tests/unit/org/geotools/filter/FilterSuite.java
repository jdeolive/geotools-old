/*
 * FilterSuite.java
 * JUnit based test
 *
 * Created on June 21, 2002, 12:30 PM
 */

package org.geotools.filter;

import junit.framework.*;


/**
 *
 * @author jamesm
 */
public class FilterSuite extends TestCase {
    
    public FilterSuite(java.lang.String testName) {
        super(testName);
    }
    
    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(suite());
    }
    
    public static Test suite() {
        TestSuite suite = new TestSuite("All filter tests");
        suite.addTestSuite(LiteralTest.class);
        suite.addTestSuite(AttributeTest.class);
        suite.addTestSuite(BetweenTest.class);
        return suite;
    }
    
    
}
