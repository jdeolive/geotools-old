package org.geotools.data.gtopo30;

import junit.framework.*;


/**
 *
 * @author aaime
 */
public class GTOPO30Suite extends TestCase {
  
  public GTOPO30Suite(String testName) {
    super(testName);
  }
  
  public static void main(java.lang.String[] args) {
    junit.textui.TestRunner.run(suite());
  }
  
  public static Test suite() {
    TestSuite suite = new TestSuite("All GTOPO30DataSource Tests");
    
    suite.addTestSuite(GT30HeaderTest.class);
    suite.addTestSuite(GT30StatTest.class);
    suite.addTestSuite(GT30DemTest.class);
    suite.addTestSuite(ServiceTest.class);
    
    return suite;
  }
}
