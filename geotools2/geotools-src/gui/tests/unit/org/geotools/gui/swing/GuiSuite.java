/*
 * FilterSuite.java
 * JUnit based test
 *
 * Created on June 21, 2002, 12:30 PM
 */

package org.geotools.gui.swing;

import junit.framework.*;
import org.geotools.gui.swing.tables.*;


/**
 *
 * @author jamesm
 */
public class GuiSuite extends TestCase {
    
    public GuiSuite(java.lang.String testName) {
        super(testName);
    }
    
    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(suite());
    }
    
    public static Test suite() {
   //     org.apache.log4j.BasicConfigurator.configure();
        TestSuite suite = new TestSuite("All swing gui tests");
        // suite.addTestSuite(NavigationPaneTest.class);
        suite.addTestSuite(FeatureTableModelTest.class);
        return suite;
    }
    
    
}
