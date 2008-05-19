package org.geotools.geometry.iso.util.algorithmND;

import junit.framework.Test;
import junit.framework.TestSuite;

public class AlgorithmNDTestSuite {
	
    public static Test suite() {

        TestSuite suite = new TestSuite();
	
        suite.addTestSuite(AlgoRectangleNDTest.class);
        suite.addTestSuite(AlgoPointNDTest.class);

        return suite;
    }

    /**
     * Runs the test suite using the textual runner.
     */
    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }
}
