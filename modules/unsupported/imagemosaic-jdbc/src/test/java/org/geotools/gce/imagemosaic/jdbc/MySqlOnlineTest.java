package org.geotools.gce.imagemosaic.jdbc;

import junit.framework.Test;
import junit.framework.TestSuite;

import java.net.URL;


public class MySqlOnlineTest extends AbstractTest {
    static DBDialect dialect = null;

    public MySqlOnlineTest(String test) {
        super(test);
    }

    public static Test suite() {
        TestSuite suite = new TestSuite();

        MySqlOnlineTest test = new MySqlOnlineTest("");

        if (test.checkPreConditions() == false) {
            return suite;
        }

        suite.addTest(new MySqlOnlineTest("testGetConnection"));
        suite.addTest(new MySqlOnlineTest("testDrop"));
        suite.addTest(new MySqlOnlineTest("testCreate"));
        suite.addTest(new MySqlOnlineTest("testImage1"));
        suite.addTest(new MySqlOnlineTest("testFullExtent"));
        suite.addTest(new MySqlOnlineTest("testNoData"));
        suite.addTest(new MySqlOnlineTest("testPartial"));
        suite.addTest(new MySqlOnlineTest("testVienna"));
        suite.addTest(new MySqlOnlineTest("testViennaEnv"));
        suite.addTest(new MySqlOnlineTest("testDrop"));
        suite.addTest(new MySqlOnlineTest("testCreateJoined"));
        suite.addTest(new MySqlOnlineTest("testImage1Joined"));
        suite.addTest(new MySqlOnlineTest("testFullExtentJoined"));
        suite.addTest(new MySqlOnlineTest("testNoDataJoined"));
        suite.addTest(new MySqlOnlineTest("testPartialJoined"));
        suite.addTest(new MySqlOnlineTest("testViennaJoined"));
        suite.addTest(new MySqlOnlineTest("testViennaEnvJoined"));
        suite.addTest(new MySqlOnlineTest("testDrop"));
        suite.addTest(new MySqlOnlineTest("testCloseConnection"));

        return suite;
    }

    @Override
    public String getConfigUrl() {
        return "file:target/resources/oek.mysql.xml";
    }

    @Override
    protected String getSubDir() {
        return "mysql";
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
}
