package org.geotools.data.shapefile;

/*
 * GmlSuite.java
 * JUnit based test
 *
 * Created on 04 March 2002, 16:09
 */
import org.geotools.data.*;
import org.geotools.feature.*;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;
import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.net.URL;
import java.util.*;
import java.util.HashMap;
import java.util.Iterator;


import junit.framework.*;
import org.geotools.data.DataStoreFinder;
import org.geotools.data.DataStore;

/**
 *
 * @author ian
 */
public class ServiceTest extends TestCaseSupport {
  
  final String TEST_FILE = "statepop.shp";
  
  public ServiceTest(java.lang.String testName) {
    super(testName);
  }
  
  public static void main(java.lang.String[] args) {
    junit.textui.TestRunner.run(suite(ServiceTest.class));
  }
  
  /**
   * Make sure that the loading mechanism is working properly.
   */
  public void testIsAvailable() {
    Iterator list = DataStoreFinder.getAvailableDataStores();
    boolean found = false;
    while(list.hasNext()){
      DataStoreFactorySpi fac = (DataStoreFactorySpi)list.next();
      if(fac instanceof ShapefileDataStoreFactory){
        found=true;
        assertNotNull(fac.getDescription());
        break;
      }
    }
    assertTrue("ShapefileDataSourceFactory not registered", found);
  }
  
  /**
   * Ensure that we can create a DataStore using url OR string url.
   */ 
  public void testShapefileDataStore() throws Exception{
    HashMap params = new HashMap();
    params.put("url", getTestResource(TEST_FILE));
    DataStore ds = DataStoreFinder.getDataStore(params);
    assertNotNull(ds);
    params.put("url", getTestResource(TEST_FILE).toString());
    assertNotNull(ds);
  }
  
  public void testBadURL() {
    HashMap params = new HashMap();
    params.put("url","aaa://bbb.ccc");
    try {
        ShapefileDataStoreFactory f = new ShapefileDataStoreFactory();
        f.createDataStore(params);
        fail("did not throw error");
    } catch (java.io.IOException ioe) {
        // this is actually good
    }
   
  }
  
}
