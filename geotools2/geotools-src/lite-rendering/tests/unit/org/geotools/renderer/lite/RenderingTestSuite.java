/*
 * SLDStyleSuite.java
 * JUnit based test
 *
 * $Id: RenderingTestSuite.java,v 1.1 2003/08/05 05:33:30 aaime Exp $
 */                

package org.geotools.renderer.lite;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.textui.TestRunner;




/**
 *
 * @author iant
 */                                
public class RenderingTestSuite extends TestCase {
    
    public RenderingTestSuite(java.lang.String testName) {
        super(testName);
        
    }        
    
    public static void main(java.lang.String[] args) {
        TestRunner.run(suite());
    }
    
    public static Test suite() {
        TestSuite suite = new TestSuite("All Rendering tests");
       
        suite.addTestSuite(Rendering2DTest.class);
        suite.addTestSuite(RenderStyleTest.class);
        suite.addTestSuite(DefaultMarkTest.class);
        suite.addTestSuite(TextTest.class);

        return suite;
    }
}
