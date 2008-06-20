package org.geotools.gce.imagemosaic.jdbc;


import java.net.URL;

import junit.framework.Test;
import junit.framework.TestSuite;


public class PostGisOnlineTest extends AbstractTest {
    public PostGisOnlineTest(String test) {
        super(test);
    }

    public static Test suite() {
    	
        TestSuite suite = new TestSuite();
        
    	PostGisOnlineTest test = new PostGisOnlineTest("");
    	if (test.checkPreConditions()==false) return suite;

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

    static JDBCSetup setup=null;
    
    @Override
    protected JDBCSetup getJDBCSetup() {
    	if (setup!=null) return setup;
    	Config config=null;
    	try {
    		config = Config.readFrom(new URL("file:target/resources/oek.postgis.xml"));
    	} catch (Exception e) {
    		throw new RuntimeException(e);
    	}
        setup=JDBCSetup.getJDBCSetup(config);
        return setup;
    }
}
