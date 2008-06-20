package org.geotools.gce.imagemosaic.jdbc;


import java.net.URL;

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

    static JDBCSetup setup=null;
    
    @Override
    protected JDBCSetup getJDBCSetup() {
    	if (setup!=null) return setup;
    	Config config=null;
    	try {
    		config = Config.readFrom(new URL("file:target/resources/oek.mysql.xml"));
    	} catch (Exception e) {
    		throw new RuntimeException(e);
    	}
        setup=JDBCSetup.getJDBCSetup(config);
        return setup;
    }
}
