/*
 * BetweenTest.java
 * JUnit based test
 *
 * Created on 20 June 2002, 18:53
 */

package org.geotools.filter;

import junit.framework.*;

import org.geotools.feature.*;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;

/**
 *
 * @author James
 */
public class BetweenTest extends TestCase {
    
    public BetweenTest(java.lang.String testName) {
        super(testName);
    }
    
    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(suite());
    }
    
    public static Test suite() {
        TestSuite suite = new TestSuite(BetweenTest.class);
        return suite;
    }

    public void testContains() throws Exception{
        //this should move out to a more configurable system run from scripts
        //but we can start with a set of hard coded tests
        
        BetweenFilterImpl a = new BetweenFilterImpl();
        
        
        AttributeType a1 = new AttributeTypeDefault("value",Integer.class).setPosition(0);
        AttributeType a2 = new AttributeTypeDefault("geometry",Geometry.class).setPosition(1);
        FeatureType schema = new FeatureTypeFlat(new AttributeType[]{a1,a2});
        
        a.addLeftValue(new LiteralExpressionImpl(new Double(5)));
        a.addRightValue(new LiteralExpressionImpl(new Double(15)));
        a.addMiddleValue(new AttributeExpressionImpl(schema,"value"));
        
        System.out.println("a1 official name is "+a1.getName());
        FlatFeatureFactory fFac = new FlatFeatureFactory(schema);
        System.out.println("geometry is "+schema.getAttributeType("geometry"));
        System.out.println("value is "+schema.getAttributeType("value"));
        System.out.println("schema has value in it ? "+schema.hasAttributeType("value"));
        
        Feature f1 = fFac.create(new Object[]{new Integer(12),new GeometryCollection(null,null,-1)});
        Feature f2 = fFac.create(new Object[]{new Integer(3),new GeometryCollection(null,null,-1)});
        Feature f3 = fFac.create(new Object[]{new Integer(15),new GeometryCollection(null,null,-1)});
        Feature f4 = fFac.create(new Object[]{new Integer(5),new GeometryCollection(null,null,-1)});
        Feature f5 = fFac.create(new Object[]{new Integer(30),new GeometryCollection(null,null,-1)});
        
        assertEquals(true,a.contains(f1));// in between
        assertEquals(false,a.contains(f2));// too small
        assertEquals(true,a.contains(f3));// max value
        assertEquals(true,a.contains(f4));// min value
        assertEquals(false,a.contains(f5));// too large
        
    }
    
    
}
