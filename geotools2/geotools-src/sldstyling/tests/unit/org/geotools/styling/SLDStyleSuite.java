/*
 * SLDStyleSuite.java
 * JUnit based test
 *
 * $Id: SLDStyleSuite.java,v 1.8 2002/10/21 15:14:52 ianturton Exp $
 */                

package org.geotools.styling;

import junit.framework.*;

import org.geotools.styling.*;

import org.apache.log4j.Logger;
import org.apache.log4j.BasicConfigurator;

/**
 *
 * @author iant
 */                                
public class SLDStyleSuite extends TestCase {
    
    public SLDStyleSuite(java.lang.String testName) {
        super(testName);
        BasicConfigurator.configure();
        
    }        
    
    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(suite());
    }
    
    public static Test suite() {
        TestSuite suite = new TestSuite("All SLD Style tests");
       
        
        suite.addTestSuite(RenderStyleTest.class);
        suite.addTestSuite(DefaultMarkTest.class);
        suite.addTestSuite(TextTest.class);
        

        return suite;
    }
}
