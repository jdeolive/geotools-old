/*
 * FeatureTypeTest.java
 *
 * Created on July 21, 2003, 4:00 PM
 */

package org.geotools.feature;

import java.util.HashSet;
import java.util.Set;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 *
 * @author  en
 */
public class FeatureTypeTest extends TestCase {
  
  public FeatureTypeTest(String testName){
    super(testName);
  }
  
  public static void main(String[] args) {
    junit.textui.TestRunner.run(suite());
  }
  
  public static Test suite() {
    TestSuite suite = new TestSuite(FeatureTypeTest.class);
    return suite;
  }
  
  public void testAbstractType() throws Exception {
    
    FeatureTypeFactory at = FeatureTypeFactory.newInstance("AbstractThing");
    at.setAbstract(true);
    at.setNamespace("http://www.nowhereinparticular.net");
    
    FeatureType type1 = at.getFeatureType();
    at.addType(AttributeTypeFactory.newAttributeType("X",String.class));
    Set bases = new HashSet();
    bases.add(type1);
    at.setSuperTypes(bases);
    FeatureType type2 = at.getFeatureType();
    assertTrue(type1.isAbstract());
    assertTrue(type2.isAbstract());
    
    assertTrue(type1.isDescendedFrom("http://www.opengis.net/gml","Feature"));
    assertTrue(type2.isDescendedFrom("http://www.opengis.net/gml","Feature"));
    assertTrue(type2.isDescendedFrom(type1));
    assertTrue(!type1.isDescendedFrom(type2));
    
    try {
      type1.create(new Object[0]);
      fail("abstract type allowed create");
    } catch (IllegalAttributeException iae) {
      
    } catch (UnsupportedOperationException uoe) {
      
    }
    try {
      type2.create(new Object[0]);
      fail("abstract type allowed create");
    } catch (IllegalAttributeException iae) {
      
    } catch (UnsupportedOperationException uoe) {
      
    }
    
    // with non-abstract super
    try {
      FeatureType[] supers = new FeatureType[1];
      supers[0] = FeatureTypeFactory.newFeatureType(null,"SillyThing","",false);
      FeatureTypeFactory.newFeatureType(null,"BadFeature","",true,supers);
      fail("allowed bad super");
    } catch (SchemaException se) {
      
    }
  }
  
  public void testEquals() throws SchemaException {
    FeatureTypeFactory at = FeatureTypeFactory.newInstance("Thing");
    at.setNamespace("http://www.nowhereinparticular.net");
    at.addType(AttributeTypeFactory.newAttributeType("X",String.class));
    final FeatureType ft = at.getFeatureType();
    at = FeatureTypeFactory.newInstance("Thing");
    at.setNamespace("http://www.nowhereinparticular.net");
    at.addType(AttributeTypeFactory.newAttributeType("X",String.class));
    FeatureType ft2 = at.getFeatureType();
    assertEquals(ft,ft2);
    at.setName("Thingee");
    assertTrue(! ft.equals(at.getFeatureType()));
    at = FeatureTypeFactory.createTemplate(ft);
    at.setNamespace("http://www.somewhereelse.net");
    assertTrue(! ft.equals(at.getFeatureType()));
    assertTrue(! ft.equals(null));
  }
}
