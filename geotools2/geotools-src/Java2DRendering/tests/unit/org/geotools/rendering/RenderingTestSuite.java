/*
 * SLDStyleSuite.java
 * JUnit based test
 *
 * $Id: RenderingTestSuite.java,v 1.1 2002/10/16 16:51:07 ianturton Exp $
 */                

package org.geotools.rendering;

import junit.framework.*;

import org.geotools.styling.*;

import org.apache.log4j.Logger;
import org.apache.log4j.BasicConfigurator;

/**
 *
 * @author iant
 */                                
public class RenderingTestSuite extends TestCase {
    
    public RenderingTestSuite(java.lang.String testName) {
        super(testName);
        BasicConfigurator.configure();
        
    }        
    
    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(suite());
    }
    
    public static Test suite() {
        TestSuite suite = new TestSuite("All Rendering tests");
       
        suite.addTestSuite(Rendering2DTest.class);
        suite.addTestSuite(RenderStyleTest.class);

        return suite;
    }
}
