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
        org.apache.log4j.BasicConfigurator.configure();
        TestSuite suite = new TestSuite("All filter tests");
        suite.addTestSuite(LiteralTest.class);
        suite.addTestSuite(AttributeTest.class);
        suite.addTestSuite(BetweenTest.class);
        suite.addTestSuite(MathTest.class);
        suite.addTestSuite(DOMParserTest.class);
        suite.addTestSuite(ParserTest.class);
        suite.addTestSuite(XMLEncoderTest.class);
        return suite;
    }
    
    
}
