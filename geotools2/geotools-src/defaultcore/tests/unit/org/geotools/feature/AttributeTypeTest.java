/*
 * AttributeTypeTest.java
 * JUnit based test
 *
 * Created on July 18, 2003, 12:56 PM
 */

package org.geotools.feature;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;
import java.util.ArrayList;
import java.util.List;
import junit.framework.*;
import org.geotools.feature.AttributeType;
import org.geotools.feature.AttributeTypeFactory;
import org.geotools.feature.FeatureTypeFactory;

/**
 *
 * @author jamesm
 */
public class AttributeTypeTest extends TestCase {
    
    public AttributeTypeTest(java.lang.String testName) {
        super(testName);
    }
    
    public static Test suite() {
        TestSuite suite = new TestSuite(AttributeTypeTest.class);
        return suite;
    }
    
    public void testAttributeTypeFactory(){
        AttributeType type = AttributeTypeFactory.newAttributeType("testAttribute", Double.class);
        assertNotNull(type);
        type = AttributeTypeFactory.newAttributeType("testAttribute", Double.class, true);
        assertNotNull(type);
        type = AttributeTypeFactory.newAttributeType("testAttribute", Double.class, false);
        assertNotNull(type);
    }
    
    public void testGetName(){
        AttributeType type = AttributeTypeFactory.newAttributeType("testAttribute", Double.class);
        assertEquals("testAttribute", type.getName());
    }
    
    public void testGetType(){
        AttributeType type = AttributeTypeFactory.newAttributeType("testAttribute", Double.class);
        assertEquals(Double.class, type.getType());
    }
    
    
    public void testEquals(){
        AttributeType typeA = AttributeTypeFactory.newAttributeType("testAttribute", Double.class);
        AttributeType typeB = AttributeTypeFactory.newAttributeType("testAttribute", Double.class);
        AttributeType typeC = AttributeTypeFactory.newAttributeType("differnetName", Double.class);
        AttributeType typeD = AttributeTypeFactory.newAttributeType("testAttribute", Integer.class);
        AttributeType typeE = AttributeTypeFactory.newAttributeType(null, Integer.class);
        AttributeType typeF = AttributeTypeFactory.newAttributeType(null, Integer.class);
        assertTrue(null == typeF.getName());
        assertTrue(typeA.equals(typeA));
        assertTrue(typeA.equals(typeB));
        assertTrue(typeE.equals(typeF));
        assertTrue(!typeA.equals(typeC));
        assertTrue(!typeA.equals(typeD));
        assertTrue(!typeA.equals(null));
        assertTrue(!typeA.equals(typeE));
    }
        
        
        
    public void testIsNillable(){
        AttributeType type = AttributeTypeFactory.newAttributeType("testAttribute", Double.class);
        assertEquals(true, type.isNillable());
        type = AttributeTypeFactory.newAttributeType("testAttribute", Double.class, true);
        assertEquals(true, type.isNillable());
        type = AttributeTypeFactory.newAttributeType("testAttribute", Double.class, false);
        assertEquals(false, type.isNillable());
    }
    
    public void testIsGeometry(){
        AttributeType type = AttributeTypeFactory.newAttributeType("testAttribute", Double.class);
        assertEquals(false, type.isGeometry());
        type = AttributeTypeFactory.newAttributeType("testAttribute", Point.class);
        assertEquals(true, type.isGeometry());
        type = AttributeTypeFactory.newAttributeType("testAttribute", Geometry.class);
        assertEquals(true, type.isGeometry());
    }
    
    public void testValidate(){
        AttributeType type = AttributeTypeFactory.newAttributeType("testAttribute", Double.class, true);
        try{
            type.validate(new Double(3));
        }
        catch(IllegalArgumentException iae){
            this.fail();
        }
        try{
            type.validate(new Integer(3));
            this.fail("Integer should not be validated by a Double type");
        }
        catch(IllegalArgumentException iae){
            
        }
        try{
            type.validate(null);
        }
        catch(IllegalArgumentException iae){
            this.fail("null should have been allowed as type is Nillable");
        }
        type = AttributeTypeFactory.newAttributeType("testAttribute", Double.class, false);
        try{
            type.validate(null);
            type.validate((Double)null);
            this.fail("null should not have been allowed as type is not Nillable");
        }
        catch(IllegalArgumentException iae){
            
        }
        
        
        
        type = AttributeTypeFactory.newAttributeType("testAttribute", List.class, false);
        try{
            type.validate(new ArrayList());
        }
        catch(IllegalArgumentException iae){
            this.fail("decended types should be allowed");
        }
        
        
    }
    
    public void testFeatureConstruction() throws SchemaException {
        FeatureType a = FeatureTypeFactory.newFeatureType(new AttributeType[]{},"noAttribs");
        FeatureType b = FeatureTypeFactory.newFeatureType(new AttributeType[]{AttributeTypeFactory.newAttributeType("testAttribute", Double.class)},"oneAttribs");
        //Direct construction should never be used like this, however it is the only way to test
        //the code fully
        AttributeType feat = new DefaultAttributeType.Feature("good",  a, false);
        assertNotNull(feat);
        
    }
    
    public void testFeatureValidate() throws SchemaException {
        try{
            //FeatureType b = FeatureTypeFactory.newFeatureType(new AttributeType[]{AttributeTypeFactory.newAttributeType("testAttribute", Double.class)},"oneAttribs");
            
            FeatureType type = FeatureTypeFactory.newFeatureType(new AttributeType[]{},"noAttribs");
            AttributeType feat = AttributeTypeFactory.newAttributeType("foo",  type);
            Feature good = type.create(new Object[]{});
            feat.validate(good);
        }
        catch(IllegalAttributeException iae){
            fail();
        }
        Feature bad = null;
        FeatureType b = FeatureTypeFactory.newFeatureType(new AttributeType[]{AttributeTypeFactory.newAttributeType("testAttribute", Double.class)},"oneAttribs");
        
        try{
            bad = b.create(new Object[]{new Double(4)});
        }
        catch(IllegalAttributeException iae){
            fail();
        }
        
       try{
            FeatureType type = FeatureTypeFactory.newFeatureType(new AttributeType[]{},"noAttribs");
            AttributeType feat = AttributeTypeFactory.newAttributeType("foo",  type);
            feat.validate(bad);
            fail();
       }
       catch(IllegalArgumentException iae){
           
       }
           
        
        
    }
    
    
    public void testNumericConstruction(){
        //Direct construction should never be used like this, however it is the only way to test
        //the code fully
        AttributeType num = new DefaultAttributeType.Numeric("good",  Double.class, false);
        assertNotNull(num);
        try{
            num = new DefaultAttributeType.Numeric("bad",  String.class, false);
            fail("Numeric type should not be constructable with type String");
        }
        catch(IllegalArgumentException iae){
        }
    }
    
    
    public void testIsNested(){
        AttributeType type = AttributeTypeFactory.newAttributeType("testAttribute", Double.class, true);
        assertEquals(false, type.isNested());
    }
    
    
    public void testParse(){
        AttributeType type = AttributeTypeFactory.newAttributeType("testAttribute", Double.class, true);
        assertEquals(null, type.parse(null));
        
        type = AttributeTypeFactory.newAttributeType("testAttribute", String.class, true);
        assertEquals("foo",type.parse("foo"));
        
        type = AttributeTypeFactory.newAttributeType("testAttribute", Number.class, true);
        assertEquals(3d,((Number)type.parse(new Long(3))).doubleValue(),0);
        assertEquals(4.4d,((Number)type.parse("4.4")).doubleValue(),0);
        type = AttributeTypeFactory.newAttributeType("testAttribute", Number.class, true);
        
        
    }
    
    
}
