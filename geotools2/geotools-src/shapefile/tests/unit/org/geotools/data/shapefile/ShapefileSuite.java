/*
 * ProjectionTestSuite.java
 * JUnit based test
 *
 * Created on February 22, 2002, 3:58 PM
 */

package org.geotools.data.shapefile;

import junit.framework.*;


/**
 *
 * @author jamesm
 */
public class ShapefileSuite extends TestCase {
  
  public ShapefileSuite(String testName) {
    super(testName);
  }
  
  public static void main(java.lang.String[] args) {
    junit.textui.TestRunner.run(suite());
  }
  
  public static Test suite() {
    TestSuite suite = new TestSuite("All ShapefileDataSource Tests");

    suite.addTestSuite(DbaseFileTest.class);
    suite.addTestSuite(ShapefileDataSourceTest.class);
    suite.addTestSuite(ShapefileTest.class);
    suite.addTestSuite(ShapefileReadWriteTest.class);
    suite.addTestSuite(ShapefileDataStoreTest.class);
    
    return suite;
  }
}
