/*
 * ShapefileDataSourceTest.java
 * JUnit based test
 *
 * Created on March 4, 2002, 4:00 PM
 */

package org.geotools.data.shapefile;

import org.geotools.data.*;
import org.geotools.feature.*;
import org.geotools.datasource.extents.*;
import com.vividsolutions.jts.geom.*;
import java.io.*;
import java.net.*;

import junit.framework.*;
import org.geotools.data.shapefile.*;

/**
 * @author Ian Schneider
 * @author jamesm
 */
public class ShapefileDataSourceTest extends TestCaseSupport {
  
  final static String STATE_POP = "statepop.shp";
  
  public ShapefileDataSourceTest(java.lang.String testName) {
    super(testName);
  }
  
  public static void main(java.lang.String[] args) {
    junit.textui.TestRunner.run(suite(ShapefileDataSourceTest.class));
  }
  
  private Feature[] loadFeatures(String resource,Query q) throws Exception {
    if (q == null) q = new QueryImpl();
    URL url = getTestResource(resource);
    ShapefileDataSource s = new ShapefileDataSource(url);
    return s.getFeatures(q).getFeatures();
  }
  
  
  public void testLoad() throws Exception {
    loadFeatures(STATE_POP,null);
  }
  
  public void testLoadAndVerify() throws Exception {
    Feature[] features = loadFeatures(STATE_POP,null);
    
    assertEquals("Number of Features loaded",49,features.length);
    
    FeatureType schema = features[0].getSchema();
    assertNotNull(schema.getDefaultGeometry());
    assertEquals("Number of Attributes",253,schema.getAttributeTypes().length);
    assertEquals("Value of statename is wrong",features[0].getAttribute("STATE_NAME"),"Illinois");
    assertEquals("Value of land area is wrong",((Double)features[0].getAttribute("LAND_KM")).doubleValue(),143986.61,0.001);
  }
  
  
  public void testQueryFill() throws Exception {
    QueryImpl qi = new QueryImpl();
    qi.setProperties(new AttributeTypeDefault[] {new AttributeTypeDefault("Billy",String.class)});
    Feature[] features = loadFeatures(STATE_POP,qi);
    assertEquals("Number of Features loaded",49,features.length);
    
    FeatureType schema = features[0].getSchema();
    assertNotNull(schema.getDefaultGeometry());
    assertEquals("Number of Attributes",254,schema.getAttributeTypes().length);
    assertEquals("Value of statename is wrong",features[0].getAttribute("STATE_NAME"),"Illinois");
    assertEquals("Value of land area is wrong",((Double)features[0].getAttribute("LAND_KM")).doubleValue(),143986.61,0.001);
    
    for (int i = 0, ii = features.length; i < ii; i++) {
      assertNull(features[i].getAttribute("Billy")); 
    }
    
  }
}
