/*
 * TestCaseSupport.java
 *
 * Created on April 30, 2003, 12:16 PM
 */

package org.geotools.data.shapefile;

import java.io.*;
import junit.framework.*;
import java.net.*;
import java.nio.channels.ReadableByteChannel;
import java.util.zip.*;
import org.geotools.feature.Feature;
import org.geotools.feature.FeatureCollection;

/**
 *
 * @author  Ian Schneider
 */
public abstract class TestCaseSupport extends TestCase {

  /** Creates a new instance of TestCaseSupport */
  public TestCaseSupport(String name) {
    super(name);
    prepareData();
  }
  
  private void prepareData() {
    ZipInputStream zip = new ZipInputStream(getTestResourceAsStream("data.zip"));
    File base = new File(getTestResource("").getPath());
    try {
      ZipEntry entry;
      byte[] bytes = new byte[8096];
      while ( (entry = zip.getNextEntry()) != null) {
        File dest = new File(base,entry.getName());
        FileOutputStream fout = new FileOutputStream(dest);
        int r;
        while (zip.available() == 1 && (r = zip.read(bytes)) != -1) {
          fout.write(bytes,0,r);
        }
        fout.close();
      }
    } catch (java.io.IOException ioe) {
      throw new RuntimeException("Error extracting test data " + ioe.getMessage(),ioe); 
    }
  }
  
  protected URL getTestResource(String name) {
    URL r = getClass().getResource("/testData/" + name);
    if (r == null)
      throw new RuntimeException("Could not locate resource : " + name);
    return r;
  }
  
  protected InputStream getTestResourceAsStream(String name) {
    InputStream in = getClass().getResourceAsStream("/testData/" + name);
    if (in == null)
      throw new RuntimeException("Could not locate resource : " + name);
    return in;
  }
  
  protected ReadableByteChannel getTestResourceChannel(String name) {
    return java.nio.channels.Channels.newChannel(getTestResourceAsStream(name));
  }
  
  protected ReadableByteChannel getReadableFileChannel(String name) throws IOException {
    URL resource = getTestResource(name);
    File f = new File(resource.getPath());
    return new FileInputStream(f).getChannel();
  }
  
  protected Feature firstFeature(FeatureCollection fc) {
    return fc.features().next(); 
  }
  
  public static Test suite(Class c) {
    return new TestSuite(c);
  }
  
}
