/*
 * ShapefileTest.java
 * JUnit based test
 *
 * Created on 12 February 2002, 21:27
 */

package org.geotools.data.gtopo30;

import java.net.URL;
import junit.framework.*;


/**
 * @author Ian Schneider
 * @author James Macgill
 */
public class GT30StatTest extends TestCaseSupport {
  
    public GT30StatTest(String testName) {
    super(testName);
  }
    
  public static void main(java.lang.String[] args) {
    junit.textui.TestRunner.run(suite(GT30StatTest.class));
  }
  
  public void testStat() throws Exception {
      URL statURL = getTestResource("STAT.STX");
      GT30Stats stats = new GT30Stats(statURL);
      assertEquals("Minimum", stats.getMin(), -9999);
      assertEquals("Max", stats.getMax(), 4536);
      assertEquals("Average", stats.getAverage(), -7583.3, 0.00001);
      assertEquals("Standard deviation", stats.getStdDev(), 4396.1, 0.00001);
  }

}
