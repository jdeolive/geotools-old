/*
 *    Geotools - OpenSource mapping toolkit
 *    (C) 2002, Centre for Computational Geography
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; 
 *    version 2.1 of the License.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *
 *    You should have received a copy of the GNU Lesser General Public
 *    License along with this library; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *    
 */

package org.geotools.filter;

import java.util.*;
import junit.framework.*;
import com.vividsolutions.jts.geom.*;
import org.geotools.data.*;
import org.geotools.feature.*;


/**
 * Unit test for expressions.  This is a complimentary test suite with the
 * filter test suite.
 *
 * @author James MacGill, CCG
 * @author Rob Hranac, TOPP
 */                                
public class ExpressionTest extends TestCase {
    


    /** Feature on which to preform tests */
    private static Feature testFeature = null;

    /** Schema on which to preform tests */
    private static FeatureType testSchema = null;
    boolean set = false;
    /** Test suite for this test case */
    TestSuite suite = null;


    /** 
     * Constructor with test name.
     */
    public ExpressionTest(String testName) {
        super(testName);
 
        //_log = Logger.getLogger(ExpressionTest.class);
        //_log.getLoggerRepository().setThreshold(Level.INFO);
        
    }        
    
    /** 
     * Main for test runner.
     */
    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }
    
    /** 
     * Required suite builder.
     * @return A test suite for this unit test.
     */
    public static Test suite() {
       
        TestSuite suite = new TestSuite(ExpressionTest.class);
        return suite;
    }
    
    /** 
     * Sets up a schema and a test feature.
     * @throws SchemaException If there is a problem setting up the schema.
     * @throws IllegalFeatureException If problem setting up the feature.
     */
    protected void setUp() 
        throws SchemaException, IllegalFeatureException {
        if(set) return;
        set = true;
        // Create the schema attributes
        //_log.debug("creating flat feature...");
        AttributeType geometryAttribute = 
            new AttributeTypeDefault("testGeometry", LineString.class);
        //_log.debug("created geometry attribute");
        AttributeType booleanAttribute = 
            new AttributeTypeDefault("testBoolean", Boolean.class);
        //_log.debug("created boolean attribute");
        AttributeType charAttribute = 
            new AttributeTypeDefault("testCharacter", Character.class);
        AttributeType byteAttribute = 
            new AttributeTypeDefault("testByte", Byte.class);
        AttributeType shortAttribute = 
            new AttributeTypeDefault("testShort", Short.class);
        AttributeType intAttribute = 
            new AttributeTypeDefault("testInteger", Integer.class);
        AttributeType longAttribute = 
            new AttributeTypeDefault("testLong", Long.class);
        AttributeType floatAttribute = 
            new AttributeTypeDefault("testFloat", Float.class);
        AttributeType doubleAttribute = 
            new AttributeTypeDefault("testDouble", Double.class);
        AttributeType stringAttribute = 
            new AttributeTypeDefault("testString", String.class);
        
        // Builds the schema
        testSchema = new FeatureTypeFlat(geometryAttribute); 
        //_log.debug("created feature type and added geometry");
        testSchema = testSchema.setAttributeType(booleanAttribute);
        //_log.debug("added boolean to feature type");
        testSchema = testSchema.setAttributeType(charAttribute);
        //_log.debug("added character to feature type");
        testSchema = testSchema.setAttributeType(byteAttribute);
        //_log.debug("added byte to feature type");
        testSchema = testSchema.setAttributeType(shortAttribute);
        //_log.debug("added short to feature type");
        testSchema = testSchema.setAttributeType(intAttribute);
        //_log.debug("added int to feature type");
        testSchema = testSchema.setAttributeType(longAttribute);
        //_log.debug("added long to feature type");
        testSchema = testSchema.setAttributeType(floatAttribute);
        //_log.debug("added float to feature type");
        testSchema = testSchema.setAttributeType(doubleAttribute);
        //_log.debug("added double to feature type");
        testSchema = testSchema.setAttributeType(stringAttribute);
        //_log.debug("added string to feature type");
        
        // Creates coordinates for the linestring
        Coordinate[] coords = new Coordinate[3];
        coords[0] = new Coordinate(1,2);
        coords[1] = new Coordinate(3,4);
        coords[2] = new Coordinate(5,6);
        
        // Builds the test feature
        Object[] attributes = new Object[10];
        attributes[0] = new LineString(coords, new PrecisionModel(), 1);
        attributes[1] = new Boolean(true);
        attributes[2] = new Character('t');
        attributes[3] = new Byte("10");
        attributes[4] = new Short("101");
        attributes[5] = new Integer(1002);
        attributes[6] = new Long(10003);
        attributes[7] = new Float(10000.4);
        attributes[8] = new Double(100000.5);
        attributes[9] = "test string data";
        
        // Creates the feature itself
        FlatFeatureFactory factory = new FlatFeatureFactory(testSchema);
        testFeature = factory.create(attributes);
        //_log.debug("...flat feature created");
    }


    /** 
     * Tests the attribute expression.
     */
    public void testAttribute()
        throws IllegalFilterException {

        // Test integer attribute
        Expression testAttribute = new AttributeExpressionImpl(testSchema, "testInteger");
        //_log.info("integer attribute expression equals: " + testAttribute.getValue(testFeature));            
        assertEquals(new Integer(1002), testAttribute.getValue(testFeature));

        // Test string attribute
        testAttribute = new AttributeExpressionImpl(testSchema, "testString");
        //_log.info("string attribute expression equals: " + testAttribute.getValue(testFeature));            
        assertEquals("test string data", testAttribute.getValue(testFeature));
    }
    
    /** 
     * Tests the literal expression.
     */
    public void testLiteral()
        throws IllegalFilterException {

        // Test integer attribute
        Expression testLiteral = new LiteralExpressionImpl(new Integer(1002));
        //_log.info("integer literal expression equals: " + testLiteral.getValue(testFeature));            
        assertEquals(new Integer(1002), testLiteral.getValue(testFeature));

        // Test string attribute
        testLiteral = new LiteralExpressionImpl("test string data");
        //_log.info("string literal expression equals: " + testLiteral.getValue(testFeature));            
        assertEquals("test string data", testLiteral.getValue(testFeature));
    }
    static FilterFactory filterFactory = FilterFactory.createFilterFactory();
     /** 
     * Tests the min function expression.
     */
    public void testMinFunction()
        throws IllegalFilterException {
        Expression a = new AttributeExpressionImpl(testSchema, "testInteger");          
        Expression b = new LiteralExpressionImpl(new Double(1004));
        
        FunctionExpression min = filterFactory.createFunctionExpression("min");
        min.setArgs(new Expression[]{a,b});         
        assertEquals(1002d,((Double)min.getValue(testFeature)).doubleValue(),0);
        
        b = new LiteralExpressionImpl(new Double(-100.001));
        min.setArgs(new Expression[]{a,b});      
        assertEquals(-100.001,((Double)min.getValue(testFeature)).doubleValue(),0);
    }
    
    /** 
     * Tests the max function expression.
     */
    public void testMaxFunction()
        throws IllegalFilterException {
        Expression a = new AttributeExpressionImpl(testSchema, "testInteger");          
        Expression b = new LiteralExpressionImpl(new Double(1004));
        
        FunctionExpression max = filterFactory.createFunctionExpression("MaxFunction");
        max.setArgs(new Expression[]{a,b});         
        assertEquals(1004d,((Double)max.getValue(testFeature)).doubleValue(),0);
        
        b = new LiteralExpressionImpl(new Double(-100.001));
        max.setArgs(new Expression[]{a,b});      
        assertEquals(1002d,((Double)max.getValue(testFeature)).doubleValue(),0);
    }
    
    /** 
     * Tests the math expression.
     */
    public void testMath()
        throws IllegalFilterException {

        // Test integer attribute
        Expression testAttribute1 = new LiteralExpressionImpl(new Integer(4));
        Expression testAttribute2 = new LiteralExpressionImpl(new Integer(2));

        // Test addition
        MathExpressionImpl mathTest = new MathExpressionImpl(DefaultExpression.MATH_ADD);
        mathTest.addLeftValue(testAttribute1);
        mathTest.addRightValue(testAttribute2);
        //_log.info("math test: " +
        //          testAttribute1.getValue(testFeature) + " + " +
        //          testAttribute2.getValue(testFeature) + " = " + 
        //          mathTest.getValue(testFeature));            
        assertEquals(new Double(6), mathTest.getValue(testFeature));

        // Test subtraction
        mathTest = new MathExpressionImpl(DefaultExpression.MATH_SUBTRACT);
        mathTest.addLeftValue(testAttribute1);
        mathTest.addRightValue(testAttribute2);
        //_log.info("math test: " +
        //          testAttribute1.getValue(testFeature) + " - " +
        //          testAttribute2.getValue(testFeature) + " = " + 
        //          mathTest.getValue(testFeature));            
        assertEquals(new Double(2), mathTest.getValue(testFeature));

        // Test multiplication
        mathTest = new MathExpressionImpl(DefaultExpression.MATH_MULTIPLY);
        mathTest.addLeftValue(testAttribute1);
        mathTest.addRightValue(testAttribute2);
        //_log.info("math test: " +
        //          testAttribute1.getValue(testFeature) + " * " +
        //          testAttribute2.getValue(testFeature) + " = " + 
        //          mathTest.getValue(testFeature));            
        assertEquals(new Double(8), mathTest.getValue(testFeature));

        // Test division
        mathTest = new MathExpressionImpl(DefaultExpression.MATH_DIVIDE);
        mathTest.addLeftValue(testAttribute1);
        mathTest.addRightValue(testAttribute2);
        //_log.info("math test: " +
        //          testAttribute1.getValue(testFeature) + " / " +
        //          testAttribute2.getValue(testFeature) + " = " + 
        //          mathTest.getValue(testFeature));            
        assertEquals(new Double(2), mathTest.getValue(testFeature));

    }
    
}
