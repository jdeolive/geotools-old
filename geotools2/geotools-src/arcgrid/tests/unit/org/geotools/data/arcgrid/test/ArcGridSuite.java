package org.geotools.data.arcgrid.test;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;


/**
 *
 * @author aaime
 */
public class ArcGridSuite extends TestCase {
  
  public ArcGridSuite(String testName) {
    super(testName);
  }
  
  public static void main(java.lang.String[] args) {
    junit.textui.TestRunner.run(suite());
  }
  
  public static Test suite() {
    TestSuite suite = new TestSuite("All ArcGridDataSource Tests");
    
    suite.addTestSuite(ArcGridHeaderTest.class);
    suite.addTestSuite(ArcGridRenderTest.class);
    suite.addTestSuite(ServiceTest.class);
    
    return suite;
  }
}
