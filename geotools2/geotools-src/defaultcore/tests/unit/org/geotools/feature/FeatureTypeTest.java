/*
 * FeatureTypeTest.java
 *
 * Created on July 21, 2003, 4:00 PM
 */

package org.geotools.feature;

import java.util.HashSet;
import java.util.Set; 
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.lang.reflect.Array;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import java.util.List;
import java.util.Map;
import java.util.Collections;
import java.util.Set;
import org.geotools.data.LockingDataSourceFixture;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.PrecisionModel;



/**
 *
 * @author  en
 * @author jgarnett
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

     public void testCopyFeature() throws Exception {
        LockingDataSourceFixture fixture = new LockingDataSourceFixture();        
        Feature feature = fixture.createFeature( "Feature", 1, 2 );
        
        assertDuplicate( "feature", feature, feature.getFeatureType().duplicate( feature  ) );
        //TODO Implement copyFeature().
    }
    public void testDeepCopy() throws Exception {
        // primative        
        String str = "FooBar";
        Integer i = new Integer(3);
        Float f = new Float( 3.14);
        Double d = new Double( 3.14159 );
        AttributeType testType = AttributeTypeFactory.newAttributeType("test",
								       Object.class);
        assertSame( "String", str, testType.duplicate( str ) );
        assertSame( "Integer", i, testType.duplicate( i ) );
        assertSame( "Float", f, testType.duplicate( f ) );
        assertSame( "Double", d, testType.duplicate( d ) );
        
        // collections
        Object objs[] = new Object[]{ str, i, f, d, };
        int ints[] = new int[]{ 1, 2, 3, 4, };
        List list = new ArrayList();
        list.add( str );
        list.add( i );
        list.add( f );
        list.add( d );
        Map map = new HashMap();
        map.put("a", str );
        map.put("b", i );
        map.put("c", f );
        map.put("d", d );
        assertDuplicate( "objs", objs, testType.duplicate( objs ) );        
        assertDuplicate( "ints", ints, testType.duplicate( ints ) );
        assertDuplicate( "list", list, testType.duplicate( list ) );
        assertDuplicate( "map", map, testType.duplicate( map ) );
        
        // complex type
        LockingDataSourceFixture fixture = new LockingDataSourceFixture();
        Feature feature = fixture.createFeature( "Feature", 1, 2 );
        
        PrecisionModel precisionModel = new PrecisionModel();
        Coordinate coords = new Coordinate(1, 3); 
        Coordinate coords2 = new Coordinate(1, 3);
        Geometry point = new Point( coords, precisionModel, 1);
        Geometry point2 = new Point( coords2, precisionModel, 1);
        
        // JTS does not implement Object equals contract
        assertTrue( "jts identity", point != point2 );
        assertTrue( "jts equals1", point.equals( point2 ) );
        assertTrue( "jts equals", !point.equals( (Object) point2 ) );
        
        assertDuplicate( "jts duplicate", point, point2 );        
        assertDuplicate( "feature", feature, testType.duplicate( feature ) );
        assertDuplicate( "point", point, testType.duplicate( point ) );
    }
    static Set immutable;
    static {
        immutable = new HashSet();
        immutable.add( String.class );
        immutable.add( Integer.class );
        immutable.add( Double.class );
        immutable.add( Float.class );
    }
    protected void assertDuplicate( String message, Object expected, Object value ){
        // Ensure value is equal to expected 
        if (expected.getClass().isArray()){
            int length1 = Array.getLength( expected );
            int length2 = Array.getLength( value );
            assertEquals( message, length1, length2);
            for( int i=0; i<length1; i++){
                assertDuplicate(
                    message+"["+i+"]",
                    Array.get( expected, i),
                    Array.get( value, i)
                );
            }
            //assertNotSame( message, expected, value );
        }
        else if (expected instanceof Geometry ){
            // JTS Geometry does not meet the Obejct equals contract!
            // So we need to do our assertEquals statement
            //
            assertTrue( message, value instanceof Geometry );
            assertTrue( message, expected instanceof Geometry );
            Geometry expectedGeom = (Geometry) expected;
            Geometry actualGeom = (Geometry) value;
            assertTrue( message, expectedGeom.equals( actualGeom ) );
        } else if (expected instanceof Feature) {
	    assertDuplicate(message, ((Feature)expected).getAttributes(null), 
			    ((Feature)value).getAttributes(null));
        } else {
            assertEquals( message, expected, value );
        }
        // Ensure Non Immutables are actually copied
        if( ! immutable.contains( expected.getClass() )){
            //assertNotSame( message, expected, value );
        }
    }
}
