/*
 * MapBrowser.java
 *
 * Created on March 3, 2003, 2:50 PM
 */

import com.vividsolutions.jts.geom.*;
import org.geotools.feature.*;
import org.geotools.shapefile.*;
import java.io.*;
import java.util.*;


/*
 * MapBrowser.java
 *
 * Created on 23 febbraio 2003, 21.10
 */

/**
 *
 * Just a simple test used to check the shapefile datasource for
 * correctness: reads all of the shapefiles in a filesystem tree,
 * writes each one to a temporary file, reads the temporary file
 * and checks for equality...
 * @author  wolf
 *
 */

public class MapBrowser {
  
  private static int totalOk = 0;
  private static int totalError = 0;
  private static int totalFiles = 0;
  private static int totalExceptions = 0;
  
  private static List brokenFiles = new ArrayList();
  private static File tempFile = null;
  
  /** Given a shapefile, opens it, extracts the features, saves them
   * to the temporary file, reads back and compares it back with the first
   * feature collection. Keeps some accounting about the failures.
   * @param f - the shapefile used for testing the ShapefileDataSource
   */
  
  public static void testFileOpen(File f) {
    
    System.gc();
    System.out.println("Opening: " + f.getAbsolutePath());
    
    totalFiles++;
    
    FeatureCollection fc1 = null;
    FeatureCollection fc2 = null;
    
    try {
      ShapefileDataSource sds = new ShapefileDataSource(f.toURL());
      fc1 = sds.getFeatures(null);
    } catch (Exception e) {
      e.printStackTrace();
    }
    
    if (fc1.getFeatures().length == 0) {
      System.out.println(f.getAbsoluteFile() + ": empty");
      return;
    }
    
    try {
      ShapefileDataSource sds = new ShapefileDataSource(tempFile.toURL());
      sds.setFeatures(fc1);
    } catch (Exception e) {
      totalExceptions++;
      e.printStackTrace();
      return;
    }
    
    try {
      ShapefileDataSource sds = new ShapefileDataSource(tempFile.toURL());
      fc2 = sds.getFeatures(null);
    } catch (Exception e) {
      totalExceptions++;
      e.printStackTrace();
      return;
    }
    
    if (featureCollectionEquals(fc1, fc2)) {
      System.out.println(f.getAbsolutePath() + " OK");
      totalOk++;
    } else {
      System.out.println(f.getAbsolutePath() + " NO");
      totalError++;
      brokenFiles.add(f.getAbsolutePath());
    }
    
    System.out.println();
  }
  
  
  /** Checks for equality a couple of feature collections, that is, they have
   * the same length and the same features
   * @param fc1 - the first FeatureCollection
   * @param fc2 - the second FeatureCollection
   * @return true if the features are equal
   */
  
  private static boolean featureCollectionEquals(FeatureCollection fc1, FeatureCollection fc2) {
    
    Feature[] fs1 = fc1.getFeatures();
    Feature[] fs2 = fc2.getFeatures();
    
    System.out.print("Comparing feature collections: ");
    
    if (fs1.length != fs2.length) {
      return false;
    }
    
    for (int i = 0; i < fs1.length; i++) {
      Feature f1 = fs1[i];
      Feature f2 = fs2[i];
      
      if ((i % 50) == 0) {
        System.out.print("*");
      }
      
      if (!featureEqual(f1, f2)) {
        System.out.println("Difference found in line: " + i);
        return false;
      }
      
    }
    
    System.out.println();
    return true;
    
  }
  
  
  /** Compares two features for equality. Two features are equals if
   *  they have the same number of attributes, the same normalized geometry
   *  and the same attributes, in the same order
   * @param f1 - the first feature
   * @param f2 - the second feature
   * @return true if the features are equals
   */
  
  private static boolean featureEqual(Feature f1, Feature f2) {
    
    Object[] atts1 = f1.getAttributes();
    Object[] atts2 = f2.getAttributes();
    
    if (atts1.length != atts2.length) {
      return false;
    }
    
    for (int i = 0; i < atts1.length; i++) {
      if (atts1[i] instanceof Geometry && atts2[i] instanceof Geometry) {
        Geometry g1 = ((Geometry) atts1[i]);
        Geometry g2 = ((Geometry) atts2[i]);
        g1.normalize();
        g2.normalize();
        if (!g1.equalsExact(g2)) {
          System.out.println("Different geometries:\n" + g1 + "\n" + g2);
          return false;
        }
      } else {
        if (!atts1[i].equals(atts2[i])) {
          System.out.println("Different attribute: [" + atts1[i] + "] - [" + atts2[i] + "]");
          return false; 
        }
      }
    }
    
    return true;
  }
  
  
  /** A simple recursive filesystems scan looking for shapefiles.
   *  Tests the ShapefileDataSource on each file found
   * @param f - the folder to be scanned
   * @throws Exception
   */
  
  public static void scanFolder(File f) throws Exception {
    
    if (!f.isDirectory()) {
      throw new Exception("Error during recursion, " + f + " is not a directory!");
    }
    
    File[] files = f.listFiles();
    for (int i = 0; i < files.length; i++) {
      if (files[i].isDirectory()) {
        if (files[i].canRead()) {
          scanFolder(files[i]);
        }
      }
      
      if (files[i].canRead()) {
        String name = files[i].getName().toUpperCase();
        if (name.endsWith(".SHP")) {
          testFileOpen(files[i]);
        }
      }
    }
  }
  
  
  /**
   *
   * @param args the command line arguments
   *
   */
  
  public static void main(String[] args) throws Exception {
    
    if (args.length == 0) {
      System.out.println("usage : MapBrowser <file>");
      System.exit(0);
    } 
    
    // path to the root of spatial data
    String initialPath = args[0];
    
    // path to a temporary file that will be used for writing
    String tempPath = System.getProperty("java.io.tmpdir") + "/test.shp";
    
    // start recursive exploration
    
    tempFile = new File(tempPath);
    
    System.out.println("Temp file : " + tempFile.getAbsolutePath());
    
    scanFolder(new File(initialPath));
    
    // print some summary information
    
    System.out.println("\nSUMMARY");
    
    System.out.println("Total files read: " + totalFiles);
    
    System.out.println("Total files equals: " + totalOk);
    
    System.out.println("Broken files : ");
    System.out.println("---------------");
    
    for (int i = 0; i < brokenFiles.size(); i++) {
      
      System.out.println(brokenFiles.get(i));
      
    }
    
  }
  
}
