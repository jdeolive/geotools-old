package org.geotools.geometry.iso.aggregate;

import junit.framework.Test;
import junit.framework.TestSuite;

public class AggregateTestSuite {
	
    public static Test suite() {
        TestSuite suite = new TestSuite();
	
        suite.addTestSuite(PicoMultiCurveTest.class);
        suite.addTestSuite(PicoMultiPointTest.class);
        suite.addTestSuite(PicoMultiPrimitiveTest.class);
        suite.addTestSuite(PicoMultiSurfaceTest.class);

        return suite;
    }

    /**
     * Runs the test suite using the textual runner.
     */
    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }
}
