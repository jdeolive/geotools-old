/*
 * ShapefileTest.java
 * JUnit based test
 *
 * Created on 12 February 2002, 21:27
 */

package org.geotools.data.pickle;

import java.awt.Color;
import java.awt.Rectangle;
import java.io.File;
import java.util.Date;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.geotools.feature.AttributeType;
import org.geotools.feature.AttributeTypeFactory;
import org.geotools.feature.Feature;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureCollections;
import org.geotools.feature.FeatureType;
import org.geotools.feature.FeatureTypeFactory;
import org.geotools.feature.SchemaException;

/**
 * @author Ian Schneider
 * @author James Macgill
 */
public class PickleTest extends TestCase {
  
  int cnt = 0;
  static String tmpFile = System.getProperty("java.io.tmpdir","") + "/tmp_pickle_delete_me";
  
  public PickleTest(String testName) {
    super(testName);
  }
  
  private File tmpFile() {
    File f = new File(tmpFile + cnt++);
    f.deleteOnExit();
    return f;
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
      atts[i] = AttributeTypeFactory.newAttributeType(names[i],classes[i]);
    }
    return FeatureTypeFactory.newFeatureType(atts,"test");
  }
  
  /*
   * Test support for storing and reading more than one type.
   *
   */
  public void testMultiFeatureTypeStorage() throws Exception {
    File file = tmpFile();
    PickleDataSource pds = new PickleDataSource(file.getParentFile(), file.getName());
    String[] names = new String[] { "Color","Sweetness","Worms" };
    Class[] clazz = new Class[] { Color.class,Integer.class,Boolean.class };
    FeatureType apple = createType(names,clazz);
    names = new String[] { "Color","Seeds","Size" };
    clazz = new Class[] { Color.class,Boolean.class,Integer.class};
    FeatureType orange = createType(names,clazz);
    FeatureCollection applesNoranges = FeatureCollections.newCollection();
    Object[] apple1 = new Object[] {Color.red,new Integer(4),Boolean.FALSE};
    Object[] apple2 = new Object[] {Color.green,new Integer(5),Boolean.TRUE};
    applesNoranges.add( apple.create( apple1) );
    applesNoranges.add( apple.create( apple2) );
    Object[] orange1 = new Object[] {Color.red,Boolean.FALSE,new Integer(50)};
    Object[] orange2 = new Object[] {Color.orange,Boolean.TRUE,new Integer(30)};
    applesNoranges.add( orange.create(orange1) );
    applesNoranges.add( orange.create(orange2) );
    pds.setFeatures(applesNoranges);
    
    checkFeature(apple1,pds.getFeature(0));
    checkFeature(apple2,pds.getFeature(1));
    checkFeature(orange1,pds.getFeature(2));
    checkFeature(orange2,pds.getFeature(3));
  }
  
  private void checkFeature(Object[] vals,Feature f) {
    assertEquals(vals.length,f.getNumberOfAttributes());
    for (int i = 0, ii = vals.length; i < ii; i++) {
      assertEquals(vals[i], f.getAttribute(i));
    }
  }
  
  /*
   * Test support for storage of arbitrary (non-primitive) objects.
   */
  public void testWriteWithArbitraryObjects() throws Exception {
    File file = tmpFile();
    PickleDataSource pds = new PickleDataSource(file.getParentFile(), file.getName());
    String[] name = new String[] {"date","rect","innerClass"};
    Class[] clazz = new Class[] {Date.class,Rectangle.class,InnerClassTestObject.class};
    FeatureType test = createType(name, clazz);
    FeatureCollection collection = FeatureCollections.newCollection();
    Object[] attVals = new Object[test.getAttributeCount()];
    long time = System.currentTimeMillis();
    for (int i = 0; i < 100; i++) {
      attVals[0] = new Date(time + i);
      attVals[1] = new Rectangle(0,0,i+1,i+1);
      attVals[2] = new InnerClassTestObject(i,i);
      collection.add( test.create(attVals) );
    }
    pds.setFeatures(collection);
    for (int i = 0; i < collection.size(); i++) {
      Feature f = pds.getFeature(i);
      assertEquals(time + i, ((Date)f.getAttribute(0)).getTime());
      assertEquals(i + 1,((Rectangle)f.getAttribute(1)).width);
      assertEquals(i,((InnerClassTestObject)f.getAttribute(2)).x);
      assertEquals(0,((InnerClassTestObject)f.getAttribute(2)).y);
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
    File file = tmpFile();
    PickleDataSource pds = new PickleDataSource(file.getParentFile(), file.getName());
    AttributeType[] atts = new AttributeType[1];
    atts[0] = AttributeTypeFactory.newAttributeType("rect", Rectangle.class);
    FeatureType test = FeatureTypeFactory.newFeatureType(atts,"test");
    FeatureCollection collection = FeatureCollections.newCollection();
    Object[] attVals = new Object[atts.length];
    Rectangle r = new Rectangle(0,0,100,100);
    for (int i = 0; i < 100; i++) {
      attVals[0] = r;
      collection.add( test.create(attVals) );
    }
    pds.setFeatures(collection);
    Object last = r;
    for (int i = 0; i < collection.size(); i++) {
      Feature f = pds.getFeature(i);
      assertEquals(r, f.getAttribute(0));
      assertTrue(last != f.getAttribute(0));
      last = attVals[0];
    }
      
  }
  
  /*
   * Test support for unicode names and values.
   */
  public void testUnicodeSupport() throws Exception {
    File file = tmpFile();
    PickleDataSource pds = new PickleDataSource(file.getParentFile(), file.getName());
    AttributeType[] atts = new AttributeType[1];
    atts[0] = AttributeTypeFactory.newAttributeType("\uAAAA\uBBBB\uCCCC\uDDDD\uEEEE", String.class);
    FeatureType test = FeatureTypeFactory.newFeatureType(atts,"unicode");
    FeatureCollection collection = FeatureCollections.newCollection();
    Object[] attVals = new Object[atts.length];
    for (int i = 0; i < 100; i++) {
      attVals[0] = "\uEEEE\uEEEE\uEEEE\uEEEE\uEEEE";
      collection.add( test.create(attVals) );
    }
    pds.setFeatures(collection);
    FeatureCollection fc = pds.getFeatures();
    Feature[] f = (Feature[]) fc.toArray(new Feature[fc.size()]);
    assertEquals("\uAAAA\uBBBB\uCCCC\uDDDD\uEEEE",f[0].getFeatureType().getAttributeType(0).getName());
    for (int i = 0; i < f.length; i++) {
      assertEquals("\uEEEE\uEEEE\uEEEE\uEEEE\uEEEE",f[i].getAttribute(0));
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
