/*
 * ShapefileReadWriteTest.java
 *
 * Created on April 30, 2003, 4:37 PM
 */

package org.geotools.data.shapefile;

import com.vividsolutions.jts.geom.Geometry;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import org.geotools.feature.Feature;
import org.geotools.feature.FeatureCollection;

/**
 *
 * @author  Ian Schneider
 */
public class ShapefileReadWriteTest extends TestCaseSupport {
  
  final String[] files = new String[] {
    "statepop.shp",
    "polygontest.shp",
    "pointtest.shp",
    "holeTouchEdge.shp"
  };
  final String TMP_FILE = "tmp.shp";
  
  File tmpFile;
  
  /** Creates a new instance of ShapefileReadWriteTest */
  public ShapefileReadWriteTest(String name) {
    super(name);
  }
  
  protected void setUp() throws Exception {
    URL parent = getTestResource("");
    File data = new File(parent.getFile());
    if (!data.exists())
      throw new Exception("Couldn't setup temp file");
    tmpFile = new File(data, TMP_FILE);
    tmpFile.createNewFile();
  } 
  
  protected void tearDown() throws Exception {
    File[] f = tmpFile.getParentFile().listFiles();
    for (int i = 0, ii = f.length; i < ii; i++) {
      if (f[i].getName().equals("tmp.shp"))
        f[i].delete();
      else if (f[i].getName().equals("tmp.dbf"))
        f[i].delete();
      else if (f[i].getName().equals("tmp.shx"))
        f[i].delete();
    }
  }
  
  public void testAll() {
    StringBuffer errors = new StringBuffer();
    for (int i = 0, ii = files.length; i < ii; i++) {
      try {
        test(files[i]);
      } catch (Exception e) {
        e.printStackTrace();
        errors.append("\nFile " + files[i] + " : " + e.getMessage());
      }
    }
    if (errors.length() > 0) {
      fail(errors.toString());
    }
  }
  
  void test(String f) throws Exception {
    ShapefileDataSource s = new ShapefileDataSource(getTestResource(f));
    org.geotools.filter.Filter filter = null;
    FeatureCollection one = s.getFeatures(filter);
    s = new ShapefileDataSource(getTestResource(TMP_FILE));
    s.setFeatures(one);
    
    s = new ShapefileDataSource(getTestResource(TMP_FILE));
    FeatureCollection two = s.getFeatures(filter);
    
    compare(one,two);
  }
  
  void compare(FeatureCollection one,FeatureCollection two) throws Exception {
    Feature[] fs1 = one.getFeatures();
    Feature[] fs2 = two.getFeatures();
    
    if (fs1.length != fs2.length) {
      throw new Exception("Number of Features unequal : " + fs1.length + " != " + fs2.length);
    }
    
    for (int i = 0; i < fs1.length; i++) {
      Feature f1 = fs1[i];
      Feature f2 = fs2[i];
      
      if ((i % 50) == 0) {
        System.out.print("*");
      }
      compare(f1, f2);
    }
    
  }
  
  void compare(Feature f1,Feature f2) throws Exception {
    Object[] atts1 = f1.getAttributes();
    Object[] atts2 = f2.getAttributes();
    
    if (atts1.length != atts2.length) {
      throw new Exception("Unequal number of attributes");
    }
    
    for (int i = 0; i < atts1.length; i++) {
      if (atts1[i] instanceof Geometry && atts2[i] instanceof Geometry) {
        Geometry g1 = ((Geometry) atts1[i]);
        Geometry g2 = ((Geometry) atts2[i]);
        g1.normalize();
        g2.normalize();
        if (!g1.equalsExact(g2)) {
          throw new Exception("Different geometries:\n" + g1 + "\n" + g2);
        }
      } else {
        if (!atts1[i].equals(atts2[i])) {
          throw new Exception("Different attribute: [" + atts1[i] + "] - [" + atts2[i] + "]");
        }
      }
    }
    
  }
  
  public static final void main(String[] args) throws Exception {
    junit.textui.TestRunner.run(suite(ShapefileReadWriteTest.class));
  }
  
}
