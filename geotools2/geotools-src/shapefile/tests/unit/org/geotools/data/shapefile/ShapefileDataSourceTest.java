/*
 * ShapefileDataSourceTest.java
 * JUnit based test
 *
 * Created on March 4, 2002, 4:00 PM
 */

package org.geotools.data.shapefile;

import org.geotools.data.*;
import org.geotools.feature.*;
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
  
  private FeatureCollection loadFeatures(String resource,Query q) throws Exception {
    if (q == null) q = new DefaultQuery();
    URL url = getTestResource(resource);
    ShapefileDataSource s = new ShapefileDataSource(url);
    return s.getFeatures(q);
  }
  
  
  public void testLoad() throws Exception {
    loadFeatures(STATE_POP,null);
  }
  
  public void testSchema() throws Exception {
    URL url = getTestResource(STATE_POP);
    ShapefileDataSource s = new ShapefileDataSource(url);
    FeatureType schema = s.getSchema();
    AttributeType[] types = schema.getAttributeTypes();
    assertEquals("Number of Attributes",253,types.length);
  }
  
  public void testLoadAndVerify() throws Exception {
    FeatureCollection features = loadFeatures(STATE_POP,null);
    
    assertEquals("Number of Features loaded",49,features.size());
    
    FeatureType schema = firstFeature(features).getFeatureType();
    assertNotNull(schema.getDefaultGeometry());
    assertEquals("Number of Attributes",253,schema.getAttributeTypes().length);
    assertEquals("Value of statename is wrong",firstFeature(features).getAttribute("STATE_NAME"),"Illinois");
    assertEquals("Value of land area is wrong",((Double)firstFeature(features).getAttribute("LAND_KM")).doubleValue(),143986.61,0.001);
  }
  
  
  public void testQuerySubset() throws Exception {
    DefaultQuery qi = new DefaultQuery();
    qi.setPropertyNames(new String[] {"STATE_NAME"});
    FeatureCollection features = loadFeatures(STATE_POP,qi);
    
    assertEquals("Number of Features loaded",49,features.size());
    FeatureType schema = firstFeature(features).getFeatureType();
    
    assertEquals("Number of Attributes",1,schema.getAttributeTypes().length);
  }
  
//  public void testQueryFill() throws Exception {
//    DefaultQuery qi = new DefaultQuery();
//    qi.setProperties(new AttributeTypeDefault[] {
//      new AttributeTypeDefault("Billy",String.class),
//      new AttributeTypeDefault("STATE_NAME",String.class),
//      new AttributeTypeDefault("LAND_KM",Double.class),
//      new AttributeTypeDefault("the_geom",Geometry.class)});
//    Feature[] features = loadFeatures(STATE_POP,qi);
//    assertEquals("Number of Features loaded",49,features.length);
//    
//    FeatureType schema = features[0].getSchema();
//    assertNotNull(schema.getDefaultGeometry());
//    assertEquals("Number of Attributes",4,schema.getAttributeTypes().length);
//    assertEquals("Value of statename is wrong",features[0].getAttribute("STATE_NAME"),"Illinois");
//    assertEquals("Value of land area is wrong",((Double)features[0].getAttribute("LAND_KM")).doubleValue(),143986.61,0.001);
//    
//    for (int i = 0, ii = features.length; i < ii; i++) {
//      assertNull(features[i].getAttribute("Billy")); 
//    }
//  }
  
  public void testQuerying() throws Exception {
    URL url = getTestResource(STATE_POP);
    ShapefileDataSource s = new ShapefileDataSource(url);
    FeatureType schema = s.getSchema();
    AttributeType[] types = schema.getAttributeTypes();
    for (int i = 0, ii = types.length; i < ii; i++) {
      DefaultQuery q = new DefaultQuery();
      q.setPropertyNames(new String[] {types[i].getName()});
      FeatureCollection fc = s.getFeatures(q);
      assertEquals("Number of Features",49,fc.size());
      assertEquals("Number of Attributes",1,firstFeature(fc).getNumberOfAttributes());
      FeatureType type = firstFeature(fc).getFeatureType();
      assertEquals("Attribute Name",type.getAttributeType(0).getName(),types[i].getName());
      assertEquals("Attribute Type",type.getAttributeType(0).getType(),types[i].getType());
      if (i % 5 == 0) System.out.print(".");
    }
  }
}
