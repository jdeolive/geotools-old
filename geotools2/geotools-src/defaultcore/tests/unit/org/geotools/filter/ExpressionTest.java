/*
 *    Geotools2 - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002, Geotools Project Managment Committee (PMC)
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
 */
package org.geotools.filter;

import java.util.logging.Logger;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.geotools.feature.AttributeType;
import org.geotools.feature.AttributeTypeFactory;
import org.geotools.feature.Feature;
import org.geotools.feature.FeatureType;
import org.geotools.feature.FeatureTypeFactory;
import org.geotools.feature.IllegalAttributeException;
import org.geotools.feature.SchemaException;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.PrecisionModel;


/**
 * Unit test for expressions.  This is a complimentary test suite with the
 * filter test suite.
 *
 * @author James MacGill, CCG
 * @author Rob Hranac, TOPP
 */
public class ExpressionTest extends TestCase {
    /** Standard logging instance */
    private static final Logger LOGGER = Logger.getLogger(
            "org.geotools.defaultcore");

    /** Feature on which to preform tests */
    private static Feature testFeature = null;
    
    /** Schema on which to preform tests */
    private static FeatureType testSchema = null;
    private static AttributeTypeFactory attFactory = AttributeTypeFactory.newInstance();
    static FilterFactory filterFactory = FilterFactory.createFilterFactory();
    boolean set = false;

    /** Test suite for this test case */
    TestSuite suite = null;

    /**
     * Constructor with test name.
     *
     * @param testName DOCUMENT ME!
     */
    public ExpressionTest(String testName) {
        super(testName);
    }

    /**
     * Main for test runner.
     *
     * @param args arguments to run main
     */
    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }

    /**
     * Required suite builder.
     *
     * @return A test suite for this unit test.
     */
    public static Test suite() {
        TestSuite suite = new TestSuite(ExpressionTest.class);

        return suite;
    }

    /**
     * Sets up a schema and a test feature.
     *
     * @throws SchemaException If there is a problem setting up the schema.
     * @throws IllegalFeatureException If problem setting up the feature.
     */
    protected void setUp() throws SchemaException, IllegalAttributeException {
        if (set) {
            return;
        }

        set = true;

        // Create the schema attributes
        LOGGER.finer("creating flat feature...");

        AttributeType geometryAttribute = AttributeTypeFactory.newAttributeType("testGeometry",
                LineString.class);
        LOGGER.finer("created geometry attribute");

        AttributeType booleanAttribute = AttributeTypeFactory.newAttributeType("testBoolean",
                Boolean.class);
        LOGGER.finer("created boolean attribute");

        AttributeType charAttribute = AttributeTypeFactory.newAttributeType("testCharacter",
                Character.class);
        AttributeType byteAttribute = AttributeTypeFactory.newAttributeType("testByte",
                Byte.class);
        AttributeType shortAttribute = AttributeTypeFactory.newAttributeType("testShort",
                Short.class);
        AttributeType intAttribute = AttributeTypeFactory.newAttributeType("testInteger",
                Integer.class);
        AttributeType longAttribute = AttributeTypeFactory.newAttributeType("testLong",
                Long.class);
        AttributeType floatAttribute = AttributeTypeFactory.newAttributeType("testFloat",
                Float.class);
        AttributeType doubleAttribute = AttributeTypeFactory.newAttributeType("testDouble",
                Double.class);
        AttributeType stringAttribute = AttributeTypeFactory.newAttributeType("testString",
                String.class);

        // Builds the schema
        FeatureTypeFactory feaTypeFactory = FeatureTypeFactory.newInstance("test");
        feaTypeFactory.addType(geometryAttribute);

        //testSchema = new FeatureTypeFlat(geometryAttribute); 
        LOGGER.finer("created feature type and added geometry");
        feaTypeFactory.addType(booleanAttribute);
        LOGGER.finer("added boolean to feature type");
        feaTypeFactory.addType(charAttribute);
        LOGGER.finer("added character to feature type");
        feaTypeFactory.addType(byteAttribute);
        LOGGER.finer("added byte to feature type");
        feaTypeFactory.addType(shortAttribute);
        LOGGER.finer("added short to feature type");
        feaTypeFactory.addType(intAttribute);
        LOGGER.finer("added int to feature type");
        feaTypeFactory.addType(longAttribute);
        LOGGER.finer("added long to feature type");
        feaTypeFactory.addType(floatAttribute);
        LOGGER.finer("added float to feature type");
        feaTypeFactory.addType(doubleAttribute);
        LOGGER.finer("added double to feature type");
        feaTypeFactory.addType(stringAttribute);
        LOGGER.finer("added string to feature type");
        testSchema = feaTypeFactory.getFeatureType();

        // Creates coordinates for the linestring
        Coordinate[] coords = new Coordinate[3];
        coords[0] = new Coordinate(1, 2);
        coords[1] = new Coordinate(3, 4);
        coords[2] = new Coordinate(5, 6);

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
        //FlatFeatureFactory factory = new FlatFeatureFactory(testSchema);
        testFeature = testSchema.create(attributes);
        LOGGER.finer("...feature created");
    }

    /**
     * Tests the attribute expression.
     *
     * @throws IllegalFilterException if filters mess up.
     */
    public void testAttribute() throws IllegalFilterException {
        // Test integer attribute
        Expression testAttribute = new AttributeExpressionImpl(testSchema,
                "testInteger");
        LOGGER.fine("integer attribute expression equals: " +
            testAttribute.getValue(testFeature));
        assertEquals(new Integer(1002), testAttribute.getValue(testFeature));
        
        // Test string attribute
        testAttribute = new AttributeExpressionImpl(testSchema, "testString");
        LOGGER.fine("string attribute expression equals: " +
            testAttribute.getValue(testFeature));
        assertEquals("test string data", testAttribute.getValue(testFeature));
    }

    /**
     * Tests the literal expression.
     *
     * @throws IllegalFilterException if there are problems
     */
    public void testLiteral() throws IllegalFilterException {
        // Test integer attribute
        Expression testLiteral = new LiteralExpressionImpl(new Integer(1002));
        LOGGER.fine("integer literal expression equals: " +
            testLiteral.getValue(testFeature));
        assertEquals(new Integer(1002), testLiteral.getValue(testFeature));
        
        // Test string attribute
        testLiteral = new LiteralExpressionImpl("test string data");
        LOGGER.fine("string literal expression equals: " +
            testLiteral.getValue(testFeature));
        assertEquals("test string data", testLiteral.getValue(testFeature));
    }

    /**
     * Tests the min function expression.
     *
     * @throws IllegalFilterException if filter problems
     */
    public void testMinFunction() throws IllegalFilterException {
        Expression a = new AttributeExpressionImpl(testSchema, "testInteger");
        Expression b = new LiteralExpressionImpl(new Double(1004));

        FunctionExpression min = filterFactory.createFunctionExpression("min");
        min.setArgs(new Expression[]{a,b});
        
        assertEquals(1002d,((Double)min.getValue(testFeature)).doubleValue(),0);
        
        b = filterFactory.createLiteralExpression(new Double(-100.001));
        min.setArgs(new Expression[]{a,b});
        assertEquals(-100.001,((Double)min.getValue(testFeature)).doubleValue(),0);
        
        assertEquals(FunctionExpressionImpl.FUNCTION,min.getType());
        
        assertEquals("Min", min.getName());
        assertEquals(2, min.getArgCount());
        assertEquals(min.getArgs()[0],a);
        assertEquals(min.getArgs()[1],b);
        min.toString();
    }
    
    public void testNonExistentFunction(){
        try{
            Expression nochance = filterFactory.createFunctionExpression("%$#%$%#%#$@#%@");
            fail();
        }
        catch(RuntimeException re){
        }
          
    }
    
    public void testFunctionNameTrim() throws IllegalFilterException {
        FunctionExpression min = filterFactory.createFunctionExpression("minFunction");
        assertTrue(min != null);  
    }
    /**
     * Tests the max function expression.
     *
     * @throws IllegalFilterException if filter problems
     */
    public void testMaxFunction() throws IllegalFilterException {
        Expression a = new AttributeExpressionImpl(testSchema, "testInteger");
        Expression b = new LiteralExpressionImpl(new Double(1004));

        FunctionExpression max = filterFactory.createFunctionExpression(
                "MaxFunction");
        max.setArgs(new Expression[] { a, b });
        assertEquals(1004d, ((Double) max.getValue(testFeature)).doubleValue(),
            0);

        b = new LiteralExpressionImpl(new Double(-100.001));
        max.setArgs(new Expression[]{a,b});
        assertEquals(1002d,((Double)max.getValue(testFeature)).doubleValue(),0);
        
        assertEquals("Max", max.getName());
        assertEquals(2, max.getArgCount());
        assertEquals(max.getArgs()[0],a);
        assertEquals(max.getArgs()[1],b);
        max.toString();
        
    }
    
    
    public void testInvalidMath(){
        try{
            MathExpressionImpl bad = new MathExpressionImpl(DefaultExpression.ATTRIBUTE);
            fail("Only math types should be allowed when constructing");
        }
        catch(IllegalFilterException ife){
        }
    }
    
    public void testDisalowedLeftAndRightExpressions() throws IllegalFilterException {
        Expression geom = new LiteralExpressionImpl(new Point(new Coordinate(2,2),null,2));
        Expression text = new LiteralExpressionImpl("text");
        MathExpressionImpl mathTest = new MathExpressionImpl(DefaultExpression.MATH_ADD);
        try{
            mathTest.addLeftValue(geom);
            fail("geometries are not allowed in math expressions");
        }
        catch(IllegalFilterException ife){
        }
        try{
            mathTest.addRightValue(geom);
            fail("geometries are not allowed in math expressions");
        }
        catch(IllegalFilterException ife){
        }
        try{
            mathTest.addLeftValue(text);
            fail("text strings are not allowed in math expressions");
        }
        catch(IllegalFilterException ife){
        }
        try{
            mathTest.addRightValue(text);
            fail("text strings are not allowed in math expressions");
        }
        catch(IllegalFilterException ife){
        }
    }
    
    public void testIncompleteMathExpression() throws IllegalFilterException {
        Expression testAttribute1 = new LiteralExpressionImpl(new Integer(4));

        MathExpressionImpl mathTest = new MathExpressionImpl(DefaultExpression.MATH_ADD);
        mathTest.addLeftValue(testAttribute1);
        try{
            mathTest.getValue(testFeature);
            fail("math expressions should not work if right hand side is not set");
        }
        catch(IllegalArgumentException ife){
        }
        mathTest = new MathExpressionImpl(DefaultExpression.MATH_ADD);
        mathTest.addRightValue(testAttribute1);
        try{
            mathTest.getValue(testFeature);
            fail("math expressions should not work if left hand side is not set");
        }
        catch(IllegalArgumentException ife){
        }
    }
    
    /**
     * Tests the math expression.
     *
     * @throws IllegalFilterException if filter problems
     */
    public void testMath() throws IllegalFilterException {
        // Test integer attribute
        Expression testAttribute1 = new LiteralExpressionImpl(new Integer(4));
        Expression testAttribute2 = new LiteralExpressionImpl(new Integer(2));
        
        // Test addition
        MathExpressionImpl mathTest = new MathExpressionImpl(DefaultExpression.MATH_ADD);
        mathTest.addLeftValue(testAttribute1);
        mathTest.addRightValue(testAttribute2);
        LOGGER.fine("math test: " + testAttribute1.getValue(testFeature) +
            " + " + testAttribute2.getValue(testFeature) + " = " +
            mathTest.getValue(testFeature));
        assertEquals(new Double(6), mathTest.getValue(testFeature));
        
        // Test subtraction
        mathTest = new MathExpressionImpl(DefaultExpression.MATH_SUBTRACT);
        mathTest.addLeftValue(testAttribute1);
        mathTest.addRightValue(testAttribute2);
        LOGGER.fine("math test: " + testAttribute1.getValue(testFeature) +
            " - " + testAttribute2.getValue(testFeature) + " = " +
            mathTest.getValue(testFeature));
        assertEquals(new Double(2), mathTest.getValue(testFeature));
        
        // Test multiplication
        mathTest = new MathExpressionImpl(DefaultExpression.MATH_MULTIPLY);
        mathTest.addLeftValue(testAttribute1);
        mathTest.addRightValue(testAttribute2);
        LOGGER.fine("math test: " + testAttribute1.getValue(testFeature) +
            " * " + testAttribute2.getValue(testFeature) + " = " +
            mathTest.getValue(testFeature));
        assertEquals(new Double(8), mathTest.getValue(testFeature));
        
        // Test division
        mathTest = new MathExpressionImpl(DefaultExpression.MATH_DIVIDE);
        mathTest.addLeftValue(testAttribute1);
        mathTest.addRightValue(testAttribute2);
        LOGGER.fine("math test: " + testAttribute1.getValue(testFeature) +
            " / " + testAttribute2.getValue(testFeature) + " = " +
            mathTest.getValue(testFeature));
        assertEquals(new Double(2), mathTest.getValue(testFeature));
    }
}
