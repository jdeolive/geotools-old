package org.geotools.gce.imagemosaic.jdbc;


import java.net.URL;

import junit.framework.Test;
import junit.framework.TestSuite;


public class OracleOnlineTest extends AbstractTest {
    public OracleOnlineTest(String test) {
        super(test);
    }

    public static Test suite() {
    	

        TestSuite suite = new TestSuite();
        
    	OracleOnlineTest test = new OracleOnlineTest("");
    	if (test.checkPreConditions()==false) return suite;
        
        suite.addTest(new OracleOnlineTest("testDrop"));
        suite.addTest(new OracleOnlineTest("testCreate"));
        suite.addTest(new OracleOnlineTest("testImage1"));
        suite.addTest(new OracleOnlineTest("testFullExtent"));
        suite.addTest(new OracleOnlineTest("testNoData"));
        suite.addTest(new OracleOnlineTest("testPartial"));
        suite.addTest(new OracleOnlineTest("testVienna"));
        suite.addTest(new OracleOnlineTest("testViennaEnv"));
        suite.addTest(new OracleOnlineTest("testDrop"));
        suite.addTest(new OracleOnlineTest("testCreateJoined"));
        suite.addTest(new OracleOnlineTest("testImage1Joined"));
        suite.addTest(new OracleOnlineTest("testFullExtentJoined"));
        suite.addTest(new OracleOnlineTest("testNoDataJoined"));
        suite.addTest(new OracleOnlineTest("testPartialJoined"));
        suite.addTest(new OracleOnlineTest("testViennaJoined"));
        suite.addTest(new OracleOnlineTest("testViennaEnvJoined"));
        suite.addTest(new OracleOnlineTest("testDrop"));

        return suite;
    }

    @Override
    protected String getSubDir() {
        return "oracle";
    }

    
    static JDBCSetup setup=null;
    
    @Override
    protected JDBCSetup getJDBCSetup() {
    	if (setup!=null) return setup;
    	Config config=null;
    	try {
    		config = Config.readFrom(new URL("file:target/resources/oek.oracle.xml"));
    	} catch (Exception e) {
    		throw new RuntimeException(e);
    	}
        setup=JDBCSetup.getJDBCSetup(config);
        return setup;
    }
}
