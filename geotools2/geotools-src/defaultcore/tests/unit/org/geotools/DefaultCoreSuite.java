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
import org.geotools.styling.*;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
/**
 *
 * @author jamesm
 */                                
public class DefaultCoreSuite extends TestCase {
    private static final Logger LOGGER = Logger.getLogger("org.geotools.core");
    public DefaultCoreSuite(java.lang.String testName) {
        super(testName);
    }        
    
    public static void main(java.lang.String[] args) {
        org.geotools.resources.Geotools.init();
        junit.textui.TestRunner.run(suite());
    }
    
    public static Test suite() {
        //_log = Logger.getLogger(DefaultCoreSuite.class);
       
        
        TestSuite suite = new TestSuite("All core tests");
        suite.addTestSuite(DatasourceTest.class);
        suite.addTestSuite(MemoryDataSourceTest.class);
        suite.addTestSuite(FeatureFlatTest.class);
        suite.addTestSuite(ExpressionTest.class);
        suite.addTestSuite(FilterEqualsTest.class);
        suite.addTestSuite(FilterTest.class);
        suite.addTestSuite(StyleFactoryImplTest.class);
        suite.addTestSuite(TextSymbolTest.class); 
        return suite;
    }
}
