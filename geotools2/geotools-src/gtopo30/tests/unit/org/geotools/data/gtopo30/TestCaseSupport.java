/*
 * TestCaseSupport.java
 *
 * Created on April 30, 2003, 12:16 PM
 */

package org.geotools.data.gtopo30;

import java.io.File;
import java.io.InputStream;
import junit.framework.*;
import java.net.*;
import java.nio.channels.ReadableByteChannel;

/**
 *
 * @author  Ian Schneider
 */
public abstract class TestCaseSupport extends TestCase {

  /** Creates a new instance of TestCaseSupport */
  public TestCaseSupport(String name) {
    super(name);
  }
  
  protected File getFile(String name) {
      java.net.URL base = getClass().getResource("testData/");
      return new File(base.getPath(), name);
  }
  
  protected URL getTestResource(String name) {
    URL r = TestCaseSupport.class.getResource("testData/" + name);
    if (r == null)
      throw new RuntimeException("Could not locate resource : " + name);
    return r;
  }
  
  protected InputStream getTestResourceAsStream(String name) {
    InputStream in = TestCaseSupport.class.getResourceAsStream("testData/" + name);
    if (in == null)
      throw new RuntimeException("Could not locate resource : " + name);
    return in;
  }
  
  protected ReadableByteChannel getTestResourceChannel(String name) {
    return java.nio.channels.Channels.newChannel(getTestResourceAsStream(name));
  }
  
  public static Test suite(Class c) {
    return new TestSuite(c);
  }
  
}
