/*
 * SLDStyleSuite.java
 * JUnit based test
 *
 * $Id: SLDStyleSuite.java,v 1.5 2002/07/04 16:46:49 ianturton Exp $
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
       
        //suite.addTestSuite(SLDStyleTest.class);
        suite.addTestSuite(RenderStyleTest.class);
        suite.addTestSuite(DefaultMarkTest.class);
        suite.addTestSuite(TextTest.class);

        return suite;
    }
}
