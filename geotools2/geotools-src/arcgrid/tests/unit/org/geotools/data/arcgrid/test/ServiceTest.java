package org.geotools.data.arcgrid.test;

/*
 * GmlSuite.java
 * JUnit based test
 *
 * Created on 04 March 2002, 16:09
 */
import java.util.HashMap;
import java.util.Iterator;

import org.geotools.data.DataSource;
import org.geotools.data.DataSourceFactorySpi;
import org.geotools.data.DataSourceFinder;
import org.geotools.data.arcgrid.ArcGridDataSourceFactory;

/**
 *
 * @author ian
 */
public class ServiceTest extends TestCaseSupport {
  
  final String TEST_FILE = "ArcGrid.asc";
  
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
      if(fac instanceof ArcGridDataSourceFactory){
        found=true;
        assertNotNull(fac.getDescription());
        break;
      }
    }
    assertTrue("ArcGridDataSourceFactory not registered", found);
  }
  
  public void testArcGridDataSource() throws Exception{
    HashMap params = new HashMap();
    params.put("url", getTestResource(TEST_FILE).toString());
    DataSource ds = DataSourceFinder.getDataSource(params);
    assertNotNull(ds);
  }
  
}
