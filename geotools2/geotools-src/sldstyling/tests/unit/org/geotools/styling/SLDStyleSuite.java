/*
 * SLDStyleSuite.java
 * JUnit based test
 *
 * $Id: SLDStyleSuite.java,v 1.2 2002/05/28 09:41:10 ianturton Exp $
 */                

package org.geotools.styling;

import junit.framework.*;

import org.geotools.styling.*;

/**
 *
 * @author iant
 */                                
public class SLDStyleSuite extends TestCase {
    
    public SLDStyleSuite(java.lang.String testName) {
        super(testName);
    }        
    
    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(suite());
    }
    
    public static Test suite() {
        TestSuite suite = new TestSuite("All SLD Style tests");
       
        suite.addTestSuite(SLDStyleTest.class);
        suite.addTestSuite(RenderStyleTest.class);
        return suite;
    }
}
