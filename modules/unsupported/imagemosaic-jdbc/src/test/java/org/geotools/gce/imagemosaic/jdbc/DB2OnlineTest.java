package org.geotools.gce.imagemosaic.jdbc;


import java.net.URL;

import junit.framework.Test;
import junit.framework.TestSuite;


public class DB2OnlineTest extends AbstractTest {
    public DB2OnlineTest(String test) {
        super(test);
    }

    public static Test suite() {
    	
    	TestSuite suite = new TestSuite();
    	
    	DB2OnlineTest test = new DB2OnlineTest("");
    	if (test.checkPreConditions()==false) return suite;
    	       
        suite.addTest(new DB2OnlineTest("testDrop"));
        suite.addTest(new DB2OnlineTest("testCreate"));
        suite.addTest(new DB2OnlineTest("testImage1"));
        suite.addTest(new DB2OnlineTest("testFullExtent"));
        suite.addTest(new DB2OnlineTest("testNoData"));
        suite.addTest(new DB2OnlineTest("testPartial"));
        suite.addTest(new DB2OnlineTest("testVienna"));
        suite.addTest(new DB2OnlineTest("testViennaEnv"));
        suite.addTest(new DB2OnlineTest("testDrop"));
        suite.addTest(new DB2OnlineTest("testCreateJoined"));
        suite.addTest(new DB2OnlineTest("testImage1Joined"));
        suite.addTest(new DB2OnlineTest("testFullExtentJoined"));
        suite.addTest(new DB2OnlineTest("testNoDataJoined"));
        suite.addTest(new DB2OnlineTest("testPartialJoined"));
        suite.addTest(new DB2OnlineTest("testViennaJoined"));
        suite.addTest(new DB2OnlineTest("testViennaEnvJoined"));
        suite.addTest(new DB2OnlineTest("testDrop"));

        return suite;
    }

    @Override
    protected String getSubDir() {
        return "db2";
    }

    
    
    static JDBCSetup setup=null;
    
    @Override
    protected JDBCSetup getJDBCSetup() {
    	if (setup!=null) return setup;
    	Config config=null;
    	try {
    		config = Config.readFrom(new URL("file:target/resources/oek.db2.xml"));
    	} catch (Exception e) {
    		throw new RuntimeException(e);
    	}
        setup=JDBCSetup.getJDBCSetup(config);
        return setup;
    }
}
