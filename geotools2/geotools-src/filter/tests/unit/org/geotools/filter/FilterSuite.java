/*
 * FilterSuite.java
 * JUnit based test
 *
 * Created on June 21, 2002, 12:30 PM
 */

package org.geotools.filter;

import junit.framework.*;
import java.util.logging.Level;


/**
 *
 * @author jamesm
 */
public class FilterSuite extends TestCase {
    
    static {
        org.geotools.resources.Geotools.init("Log4JFormatter", Level.ALL);
    }

    public FilterSuite(java.lang.String testName) {
        super(testName);
    }
    
    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(suite());
    }
    
    public static Test suite() {
        TestSuite suite = new TestSuite("All filter tests");
        suite.addTestSuite(ParserTest.class);
        /*
        suite.addTestSuite(LiteralTest.class);
        suite.addTestSuite(AttributeTest.class);
        suite.addTestSuite(BetweenTest.class);
        suite.addTestSuite(MathTest.class);
        suite.addTestSuite(DOMParserTest.class);
        suite.addTestSuite(XMLEncoderTest.class);      
        */
        return suite;
    }
    
    
}
