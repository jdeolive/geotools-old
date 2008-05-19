package org.geotools.geometry.iso.primitive;

import junit.framework.Test;
import junit.framework.TestSuite;

public class PrimitiveTestSuite {
	
    public static Test suite() {

        TestSuite suite = new TestSuite();
	
        suite.addTestSuite(PrimitiveFactoryTest.class);
        suite.addTestSuite(BoundaryTest.class);
        suite.addTestSuite(RingTest.class);
        suite.addTestSuite(PointTest.class);
        suite.addTestSuite(CurveTest.class);
        suite.addTestSuite(SurfaceTest.class);
        suite.addTestSuite(Dimension2o5DTest.class);

        return suite;
    }

    /**
     * Runs the test suite using the textual runner.
     */
    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }
}
