/*
 * ShapefileTest.java
 * JUnit based test
 *
 * Created on 12 February 2002, 21:27
 */

package org.geotools.data.pickle;

import junit.framework.*;
import java.net.*;
import com.vividsolutions.jts.geom.*;
import java.awt.*;
import java.io.*;
import java.util.*;
import org.geotools.data.shapefile.shp.*;
import org.geotools.feature.*;

/**
 * @author Ian Schneider
 * @author James Macgill
 */
public class PickleTest extends TestCase {
  
  static String tempFile = System.getProperty("java.io.tmpdir","") + "/tmp_pickle_delete_me";
  
  public PickleTest(String testName) {
    super(testName);
  }
  
  public static Test suite(Class c) {
    return new TestSuite(c);
  }
  
  public static void main(java.lang.String[] args) {
    junit.textui.TestRunner.run(suite(PickleTest.class));
  }
  
  private FeatureType createType(String[] names,Class[] classes) throws SchemaException {
    if (names.length != classes.length)
      throw new IllegalArgumentException("unequal lengths");
    AttributeType[] atts = new AttributeType[names.length];
    for (int i = 0, ii = atts.length; i < ii; i++) {
      atts[i] = new AttributeTypeDefault(names[i],classes[i]);
    }
    return new FeatureTypeFlat(atts);
  }
  
  /*
   * Test support for storing and reading more than one type.
   *
   */
  public void testMultiFeatureTypeStorage() throws Exception {
    File file = new File(tempFile);
    PickleDataSource pds = new PickleDataSource(file.getParentFile(), file.getName());
    String[] names = new String[] { "Color","Sweetness","Worms" };
    Class[] clazz = new Class[] { Color.class,Integer.class,Boolean.class };
    FeatureType apple = createType(names,clazz);
    names = new String[] { "Color","Seeds","Size" };
    clazz = new Class[] { Color.class,Boolean.class,Integer.class};
    FeatureType orange = createType(names,clazz);
    FeatureCollection applesNoranges = new FeatureCollectionDefault();
    FeatureFactory appleFactory = new FlatFeatureFactory(apple);
    FeatureFactory orangeFactory = new FlatFeatureFactory(orange);
    Object[] apple1 = new Object[] {Color.red,new Integer(4),Boolean.FALSE};
    Object[] apple2 = new Object[] {Color.green,new Integer(5),Boolean.TRUE};
    applesNoranges.add( appleFactory.create( apple1) );
    applesNoranges.add( appleFactory.create( apple2) );
    Object[] orange1 = new Object[] {Color.red,Boolean.FALSE,new Integer(50)};
    Object[] orange2 = new Object[] {Color.orange,Boolean.TRUE,new Integer(30)};
    applesNoranges.add( orangeFactory.create(orange1) );
    applesNoranges.add( orangeFactory.create(orange2) );
    pds.setFeatures(applesNoranges);
    
    checkFeature(apple1,pds.getFeature(0));
    checkFeature(apple2,pds.getFeature(1));
    checkFeature(orange1,pds.getFeature(2));
    checkFeature(orange2,pds.getFeature(3));
  }
  
  private void checkFeature(Object[] vals,Feature f) {
    Object[] atts = f.getAttributes();
    assertEquals(vals.length,atts.length);
    for (int i = 0, ii = vals.length; i < ii; i++) {
      assertEquals(vals[i], atts[i]);
    }
  }
  
  /*
   * Test support for storage of arbitrary (non-primitive) objects.
   */
  public void testWriteWithArbitraryObjects() throws Exception {
    File file = new File(tempFile);
    PickleDataSource pds = new PickleDataSource(file.getParentFile(), file.getName());
    String[] name = new String[] {"date","rect","innerClass"};
    Class[] clazz = new Class[] {Date.class,Rectangle.class,InnerClassTestObject.class};
    FeatureType test = createType(name, clazz);
    FeatureFactory factory = new FlatFeatureFactory(test);
    FeatureCollection collection = new FeatureCollectionDefault();
    Object[] attVals = new Object[test.attributeTotal()];
    long time = System.currentTimeMillis();
    for (int i = 0; i < 100; i++) {
      attVals[0] = new Date(time + i);
      attVals[1] = new Rectangle(0,0,i+1,i+1);
      attVals[2] = new InnerClassTestObject(i,i);
      collection.add( factory.create(attVals) );
    }
    pds.setFeatures(collection);
    for (int i = 0; i < collection.size(); i++) {
      Feature f = pds.getFeature(i);
      attVals = f.getAttributes();
      assertEquals(time + i, ((Date)attVals[0]).getTime());
      assertEquals(i + 1,((Rectangle)attVals[1]).width);
      assertEquals(i,((InnerClassTestObject)attVals[2]).x);
      assertEquals(0,((InnerClassTestObject)attVals[2]).y);
    }
  }
  
  /*
   * This test shows that "shared" feature attributes are a problem, because
   * the current stream cannot handle this.
   * Suggested Fix:
   *   + when writing objects (unknown to pickler), see if they've been encountered before.
   *   + if they have been, write a special handle object marker,
   *   + when all writing is done, write the shared objects at the end of the feature file
   *   + when resolving, shared objects must be read from the shared pool
   *  Hard Case: two Features which have references to each other)
   *  Hard Case: random access resolving...
   *
   */
  public void testWriteWithSharedObjects() throws Exception {
    File file = new File(tempFile);
    PickleDataSource pds = new PickleDataSource(file.getParentFile(), file.getName());
    AttributeType[] atts = new AttributeType[1];
    atts[0] = new AttributeTypeDefault("rect", Rectangle.class);
    FeatureType test = new FeatureTypeFlat(atts);
    FeatureFactory factory = new FlatFeatureFactory(test);
    FeatureCollection collection = new FeatureCollectionDefault();
    Object[] attVals = new Object[atts.length];
    Rectangle r = new Rectangle(0,0,100,100);
    for (int i = 0; i < 100; i++) {
      attVals[0] = r;
      collection.add( factory.create(attVals) );
    }
    pds.setFeatures(collection);
    Object last = r;
    for (int i = 0; i < collection.size(); i++) {
      Feature f = pds.getFeature(i);
      attVals = f.getAttributes();
      assertEquals(r, attVals[0]);
      assertTrue(last != attVals[0]);
      last = attVals[0];
    }
      
  }
  
  /*
   * Test support for unicode names and values.
   */
  public void testUnicodeSupport() throws Exception {
    File file = new File(tempFile);
    PickleDataSource pds = new PickleDataSource(file.getParentFile(), file.getName());
    AttributeType[] atts = new AttributeType[1];
    atts[0] = new AttributeTypeDefault("\uAAAA\uBBBB\uCCCC\uDDDD\uEEEE", String.class);
    FeatureType test = new FeatureTypeFlat(atts);
    FeatureFactory factory = new FlatFeatureFactory(test);
    FeatureCollection collection = new FeatureCollectionDefault();
    Object[] attVals = new Object[atts.length];
    for (int i = 0; i < 100; i++) {
      attVals[0] = "\uEEEE\uEEEE\uEEEE\uEEEE\uEEEE";
      collection.add( factory.create(attVals) );
    }
    pds.setFeatures(collection);
    FeatureCollection fc = pds.getFeatures();
    Feature[] f = fc.getFeatures();
    assertEquals("\uAAAA\uBBBB\uCCCC\uDDDD\uEEEE",f[0].getSchema().getAttributeType(0).getName());
    for (int i = 0; i < f.length; i++) {
      assertEquals("\uEEEE\uEEEE\uEEEE\uEEEE\uEEEE",f[i].getAttributes()[0]);
    }
  }
  
  public static class InnerClassTestObject implements java.io.Serializable {
    int x;
    transient int y;
    public InnerClassTestObject(int x,int y) {
      this.x = x;
      this.y = y;
    }
  }

}
