/*
 * FeatureCollectionTest.java
 *
 * Created on July 21, 2003, 5:58 PM
 */

package org.geotools.feature;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.PrecisionModel;

/**
 *
 * @author  en
 */
public class FeatureCollectionTest extends TestCase {
  
  FeatureCollection features;
  
  public FeatureCollectionTest(String testName){
    super(testName);
  }
  
  public static void main(String[] args) {
    junit.textui.TestRunner.run(suite());
  }
  
  public static Test suite() {
    TestSuite suite = new TestSuite(FeatureCollectionTest.class);
    return suite;
  }
  
  protected void setUp() throws Exception {
    features = FeatureCollections.newCollection();
    FeatureType dumby = FeatureTypeFactory.newFeatureType(null,"Dumby");
    for (int i = 0; i < 100; i++) {
      features.add(dumby.create(null)); 
    }
  }
  
  public Collection randomPiece(Collection original) {
    LinkedList next = new LinkedList();
    Iterator og = original.iterator();
    while (og.hasNext()) {
      if (Math.random() > .5) {
        next.add(og.next());
      } else {
        og.next();
      }
    }
    return next;
  }
  
  public void testBounds() throws Exception {
    PrecisionModel pm = new PrecisionModel();
    Geometry[] g = new Geometry[4];
    g[0] = new Point(new Coordinate(0,0), pm,0);
    g[1] = new Point(new Coordinate(0,10), pm,0);
    g[2] = new Point(new Coordinate(10,0), pm,0);
    g[3] = new Point(new Coordinate(10,10), pm,0);

    GeometryCollection gc = new GeometryCollection(g, pm,0);
    FeatureTypeFactory factory = FeatureTypeFactory.newInstance("bounds");
    factory.addType(AttributeTypeFactory.newAttributeType("p1", Point.class));
    FeatureType t = factory.createFeatureType();
    FeatureCollection fc = FeatureCollections.newCollection();
    for (int i = 0; i < g.length; i++) {
      fc.add(t.create(new Geometry[] {g[i]}));
    } 
    assertEquals(gc.getEnvelopeInternal(),fc.getBounds());
  }
  
  public void testSetAbilities() {
    int size = features.size();
    features.addAll(randomPiece(features));
    assertEquals(features.size(),size);
  }
  
  public void testAddRemoveAllAbilities() throws Exception {
    Collection half = randomPiece(features);
    Collection otherHalf = new ArrayList(features);
    otherHalf.removeAll(half);
    features.removeAll(half);
    assertTrue(features.containsAll(otherHalf));
    assertTrue(!features.containsAll(half));
    features.removeAll(otherHalf);
    assertTrue(features.size() == 0);
    features.addAll(half);
    assertTrue(features.containsAll(half));
    features.addAll(otherHalf);
    assertTrue(features.containsAll(otherHalf));
    features.retainAll(otherHalf);
    assertTrue(features.containsAll(otherHalf));
    assertTrue(!features.containsAll(half));
    features.addAll(otherHalf);
    Iterator i = features.iterator();
    while (i.hasNext()) {
      i.next();
      i.remove();
    }
    assertEquals(features.size(),0);
    assertTrue(! features.remove(FeatureTypeFactory.newFeatureType(null,"XXX").create(null)));
  }
  
  public void testAssorted() {
    FeatureCollection copy = FeatureCollections.newCollection();
    copy.addAll(features);
    copy.clear();
    assertTrue(copy.isEmpty());
    copy.addAll(features);
    assertTrue(!copy.isEmpty());
    ArrayList list = new ArrayList(features);
    Feature[] f1 = (Feature[]) list.toArray(new Feature[list.size()]);
    Feature[] f2 = (Feature[]) features.toArray(new Feature[list.size()]);
    assertEquals(f1.length,f2.length);
    for (int i = 0; i < f1.length; i++) {
      assertSame(f1[i], f2[i]);
    }
    FeatureIterator copyIterator = copy.features();
    FeatureIterator featuresIterator = features.features();
    while (copyIterator.hasNext() && featuresIterator.hasNext()) {
      assertEquals(copyIterator.next(),featuresIterator.next());
    }
    
    FeatureCollection listen = FeatureCollections.newCollection();
    ListenerProxy counter = new ListenerProxy();
    listen.addListener(counter);
    listen.addAll(features);
    assertEquals(1,counter.changeEvents);
    listen.removeListener(counter);
    listen.removeAll(features);
    assertEquals(1,counter.changeEvents);
  }
  
  static class ListenerProxy implements CollectionListener {
    int changeEvents = 0;
    
    public void collectionChanged(org.geotools.feature.CollectionEvent tce) {
      changeEvents++;
    }
    
  }
}
