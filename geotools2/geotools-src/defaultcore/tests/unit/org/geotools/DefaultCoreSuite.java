/*
 * ProjectionTestSuite.java
 * JUnit based test
 *
 * Created on February 22, 2002, 3:58 PM
 */                

package org.geotools;

import junit.framework.*;
import org.geotools.data.*;
import org.geotools.feature.*;
import org.geotools.filter.*;

import org.apache.log4j.Level;
import org.apache.log4j.Hierarchy;
import org.apache.log4j.Logger;
import org.apache.log4j.BasicConfigurator;
/**
 *
 * @author jamesm
 */                                
public class DefaultCoreSuite extends TestCase {
    static Logger _log;
    public DefaultCoreSuite(java.lang.String testName) {
        super(testName);
        
    }        
    
    public static void main(java.lang.String[] args) {
        
        junit.textui.TestRunner.run(suite());
    }
    
    public static Test suite() {
        _log = Logger.getLogger(DefaultCoreSuite.class);
        _log.getLoggerRepository().setThreshold(Level.INFO);
        BasicConfigurator.configure();
        
        TestSuite suite = new TestSuite("All defaultcore tests");
        suite.addTestSuite(DatasourceTest.class);
        suite.addTestSuite(FeatureFlatTest.class);
        suite.addTestSuite(ExpressionTest.class);
        suite.addTestSuite(FilterTest.class);
        return suite;
    }
}
