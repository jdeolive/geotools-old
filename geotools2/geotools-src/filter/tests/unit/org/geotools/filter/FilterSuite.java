/*
 *    Geotools2 - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002, Geotools Project Managment Committee (PMC)
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    version 2.1 of the License.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *
 */
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
 * Runs the filter tests.
 *
 * @author James Macgill<br>
 * @author Chris Holmes
 *
 * @task REVISIT: Is there still need for this with maven?  It runs everything
 *       that ends with Test.
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
        suite.addTestSuite(MathTest.class);
        suite.addTestSuite(DOMParserTest.class);
        suite.addTestSuite(ParserTest.class);
        suite.addTestSuite(XMLEncoderTest.class);
        suite.addTestSuite(SQLEncoderTest.class);
        suite.addTestSuite(SQLEncoderPostgis.class);
        suite.addTestSuite(SQLUnpackerTest.class);
        suite.addTestSuite(CapabilitiesTest.class);

        return suite;
    }
}
