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
public class AttributeTest extends TestCase {
    
    public AttributeTest(java.lang.String testName) {
        super(testName);
    }
    
    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(suite());
    }
    
    public static Test suite() {
        TestSuite suite = new TestSuite(BetweenTest.class);
        return suite;
    }
    FeatureType schema = null;
    public Feature[] sampleFeatures() throws Exception{
        AttributeType a1 = new AttributeTypeDefault("value",Integer.class).setPosition(0);
        AttributeType a2 = new AttributeTypeDefault("geometry",Geometry.class).setPosition(1);
        AttributeType a3 = new AttributeTypeDefault("name",String.class).setPosition(2);
        schema = new FeatureTypeFlat(new AttributeType[]{a1,a2,a3});
  
        FeatureFactory fFac = new FeatureFactory(schema);
        Feature[] f = new Feature[3];
        f[0] = fFac.create(new Object[]{
                new Integer(12),
                new GeometryCollection(null,null,-1),
                "first"});
        f[1] = fFac.create(new Object[]{
                new Integer(3),
                new GeometryCollection(null,null,-1),
                "second"});
        f[2] = fFac.create(new Object[]{
                new Integer(15),
                new GeometryCollection(null,null,-1),
                "third"});
                
        return f;
    }
        
    
    public void testTypeMissmatch() throws Exception{
        Feature[] f = sampleFeatures();
        
        //the following are intentionaly backwards
        AttributeExpressionImpl e1 = new AttributeExpressionImpl(schema,"value");
        AttributeExpressionImpl e2 = new AttributeExpressionImpl(schema,"name");
        boolean pass=false;
        Object value = null;
            value = e1.getValue(f[0]);
        
            if(value instanceof Integer) pass = true;
        
        assertTrue("String expresion returned an Integer",pass);
        pass = false;
        
            value = e2.getValue(f[0]);
        if(value instanceof String ) pass = true;
        
        assertTrue("Integer expresion returned a String",pass);
    }
    
    public void testSetupAndExtraction() throws Exception{
        //this should move out to a more configurable system run from scripts
        //but we can start with a set of hard coded tests
        
        Feature[] f = sampleFeatures();
        
        AttributeExpressionImpl e1 = new AttributeExpressionImpl(schema,"value");
        AttributeExpressionImpl e2 = new AttributeExpressionImpl(schema,"name");
        
        assertEquals(12d,((Integer)e1.getValue(f[0])).doubleValue(),0);
        assertEquals(3d,((Integer)e1.getValue(f[1])).doubleValue(),0);
        assertEquals(15d,((Integer)e1.getValue(f[2])).doubleValue(),0);
        assertEquals("first",(String)e2.getValue(f[0]));
        assertEquals("second",(String)e2.getValue(f[1]));
    } 
    
}
