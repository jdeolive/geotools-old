package org.geotools.data.shapefile;

/*
 * GmlSuite.java
 * JUnit based test
 *
 * Created on 04 March 2002, 16:09
 */
import org.geotools.data.*;
import org.geotools.datasource.extents.*;
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
import org.geotools.data.DataSourceFinder;
import org.geotools.data.DataSource;

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
  
  public void testIsAvailable() {
    Iterator list = DataSourceFinder.getAvailableDataSources();
    boolean found = false;
    while(list.hasNext()){
      DataSourceFactorySpi fac = (DataSourceFactorySpi)list.next();
      if(fac instanceof ShapefileDataSourceFactory){
        found=true;
        break;
      }
    }
    assertTrue("ShapefileDataSourceFactory not registered", found);
  }
  
  public void testShapefileDataSource() throws Exception{
    HashMap params = new HashMap();
    params.put("url", getTestResource(TEST_FILE).toString());
    DataSource ds = DataSourceFinder.getDataSource(params);
    assertNotNull(ds);
  }
  
}
