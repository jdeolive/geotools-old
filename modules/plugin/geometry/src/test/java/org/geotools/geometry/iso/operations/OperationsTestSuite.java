package org.geotools.geometry.iso.operations;

import junit.framework.Test;
import junit.framework.TestSuite;

public class OperationsTestSuite {
	
    public static Test suite() {

        TestSuite suite = new TestSuite();
	
        suite.addTestSuite(SetOperationsTest.class);
        suite.addTestSuite(RelateOperatorsTest.class);
        suite.addTestSuite(IsSimpleOperationTest.class);
        suite.addTestSuite(CentroidTest.class);
        suite.addTestSuite(ConvexHullTest.class);
        suite.addTestSuite(ClosureTest.class);

        return suite;
    }

    /**
     * Runs the test suite using the textual runner.
     */
    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
        DisplayGeometry.main(args);
    }
}
