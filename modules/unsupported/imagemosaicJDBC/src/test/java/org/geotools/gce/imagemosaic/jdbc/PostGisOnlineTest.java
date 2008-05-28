package org.geotools.gce.imagemosaic.jdbc;

import junit.framework.Test;
import junit.framework.TestSuite;


public class PostGisOnlineTest extends AbstractTest {
    public PostGisOnlineTest(String test) {
        super(test);
    }

    public static Test suite() {
        TestSuite suite = new TestSuite();
        suite.addTest(new PostGisOnlineTest("testDrop"));
        suite.addTest(new PostGisOnlineTest("testCreate"));
        suite.addTest(new PostGisOnlineTest("testImage1"));
        suite.addTest(new PostGisOnlineTest("testFullExtent"));
        suite.addTest(new PostGisOnlineTest("testNoData"));
        suite.addTest(new PostGisOnlineTest("testPartial"));
        suite.addTest(new PostGisOnlineTest("testVienna"));
        suite.addTest(new PostGisOnlineTest("testViennaEnv"));
        suite.addTest(new PostGisOnlineTest("testDrop"));
        suite.addTest(new PostGisOnlineTest("testCreateJoined"));
        suite.addTest(new PostGisOnlineTest("testImage1Joined"));
        suite.addTest(new PostGisOnlineTest("testFullExtentJoined"));
        suite.addTest(new PostGisOnlineTest("testNoDataJoined"));
        suite.addTest(new PostGisOnlineTest("testPartialJoined"));
        suite.addTest(new PostGisOnlineTest("testViennaJoined"));
        suite.addTest(new PostGisOnlineTest("testViennaEnvJoined"));
        suite.addTest(new PostGisOnlineTest("testDrop"));

        return suite;
    }

    @Override
    protected String getSubDir() {
        return "postgis";
    }

    @Override
    protected JDBCSetup getJDBCSetup() {
        return PostgisSetup.Singleton;
    }
}
