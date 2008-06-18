package org.geotools.gce.imagemosaic.jdbc;

import junit.framework.Test;
import junit.framework.TestSuite;


public class MySqlOnlineTest extends AbstractTest {
    public MySqlOnlineTest(String test) {
        super(test);
    }

    public static Test suite() {
    	

        TestSuite suite = new TestSuite();
    	
    	MySqlOnlineTest test = new MySqlOnlineTest("");
    	if (test.checkPreConditions()==false) return suite;
    	       
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

        return suite;
    }

    @Override
    protected String getSubDir() {
        return "mysql";
    }

    @Override
    protected JDBCSetup getJDBCSetup() {
        return MySqlSetup.Singleton;
    }
}
