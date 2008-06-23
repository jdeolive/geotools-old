package org.geotools.gce.imagemosaic.jdbc;

import junit.framework.Test;
import junit.framework.TestSuite;

import java.net.URL;

import java.sql.SQLException;


public class PostGisOnlineTest extends AbstractTest {
    static DBDialect dialect = null;

    public PostGisOnlineTest(String test) {
        super(test);
    }

    @Override
    protected String getSrsId() {
        return "4326";
    }

    public static Test suite() {
        TestSuite suite = new TestSuite();

        PostGisOnlineTest test = new PostGisOnlineTest("");

        if (test.checkPreConditions() == false) {
            return suite;
        }

        suite.addTest(new PostGisOnlineTest("testGetConnection"));
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
        suite.addTest(new PostGisOnlineTest("testCloseConnection"));

        return suite;
    }

    @Override
    protected String[] getTileTableNames() {
        return new String[] {  /* "tiles0" , */"tiles1", "tiles2", "tiles3" };
    }

    @Override
    protected String[] getSpatialTableNames() {
        return new String[] {  /* "spatial0", */"spatial1", "spatial2", "spatial3" };
    }

    @Override
    public String getConfigUrl() {
        return "file:target/resources/oek.postgis.xml";
    }

    @Override
    protected String getSubDir() {
        return "postgis";
    }

    @Override
    protected DBDialect getDBDialect() {
        if (dialect != null) {
            return dialect;
        }

        Config config = null;

        try {
            config = Config.readFrom(new URL(getConfigUrl()));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        dialect = DBDialect.getDBDialect(config);

        return dialect;
    }

    void executeRegister(String stmt) throws SQLException {
        Connection.prepareStatement(stmt).executeQuery();
    }

    void executeUnRegister(String stmt) throws SQLException {
        Connection.prepareStatement(stmt).executeQuery();
    }
}
