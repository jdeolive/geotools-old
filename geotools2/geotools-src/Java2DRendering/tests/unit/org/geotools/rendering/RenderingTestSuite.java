/*
 * SLDStyleSuite.java
 * JUnit based test
 *
 * $Id: RenderingTestSuite.java,v 1.3 2002/10/25 17:03:19 ianturton Exp $
 */                

package org.geotools.rendering;

import junit.framework.*;

import org.geotools.styling.*;



/**
 *
 * @author iant
 */                                
public class RenderingTestSuite extends TestCase {
    
    public RenderingTestSuite(java.lang.String testName) {
        super(testName);
        
    }        
    
    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(suite());
    }
    
    public static Test suite() {
        TestSuite suite = new TestSuite("All Rendering tests");
       
        suite.addTestSuite(Rendering2DTest.class);
        //suite.addTestSuite(RenderStyleTest.class);

        return suite;
    }
}
