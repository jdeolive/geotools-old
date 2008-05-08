package org.geotools.geometry.iso.complex;

import junit.framework.Test;
import junit.framework.TestSuite;

public class ComplexTestSuite {
	
    public static Test suite() {

        TestSuite suite = new TestSuite();
	
        suite.addTestSuite(CompositeSurfaceTest.class);
        suite.addTestSuite(PicoCompositeSurfaceTest.class);
        suite.addTestSuite(PicoCompositeCurveTest.class);
        suite.addTestSuite(PicoCompositePointTest.class);

        return suite;
    }

    /**
     * Runs the test suite using the textual runner.
     */
    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }
}
