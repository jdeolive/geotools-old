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

import com.vividsolutions.jts.geom.*;
import junit.framework.*;
import org.geotools.data.*;
import org.geotools.feature.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * Unit test for filters.  Note that this unit test does not encompass all of
 * filter package, just the filters themselves.  There is a seperate unit test
 * for expressions.
 *
 * @author James MacGill, CCG
 * @author Rob Hranac, TOPP
 */
public class FilterTest extends TestCase {
    /** The logger for the filter module. */
    private static final Logger LOGGER = Logger.getLogger("org.geotools.filter");

    /** Feature on which to preform tests */
    private static Feature testFeature = null;

    /** Schema on which to preform tests */
    private static FeatureType testSchema = null;
    boolean set = false;

    /** Test suite for this test case */
    TestSuite suite = null;

    /**
     * Constructor with test name.
     *
     * @param testName DOCUMENT ME!
     */
    public FilterTest(String testName) {
        super(testName);

        //BasicConfigurator.configure();
        //LOGGER = Logger.getLogger(FilterTest.class);
        //LOGGER.getLoggerRepository().setThreshold(Level.INFO);
    }

    /**
     * Main for test runner.
     *
     * @param args DOCUMENT ME!
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
        TestSuite suite = new TestSuite(FilterTest.class);

        return suite;
    }

    /**
     * Sets up a schema and a test feature.
     *
     * @throws SchemaException If there is a problem setting up the schema.
     * @throws IllegalFeatureException If problem setting up the feature.
     */
    protected void setUp() throws SchemaException, IllegalFeatureException {
        if (set) {
            return;
        }

        set = true;

        // Create the schema attributes
        //LOGGER.debug("creating flat feature...");
        AttributeType geometryAttribute = new AttributeTypeDefault("testGeometry",
                LineString.class);

        //LOGGER.debug("created geometry attribute");
        AttributeType booleanAttribute = new AttributeTypeDefault("testBoolean",
                Boolean.class);

        //LOGGER.debug("created boolean attribute");
        AttributeType charAttribute = new AttributeTypeDefault("testCharacter",
                Character.class);
        AttributeType byteAttribute = new AttributeTypeDefault("testByte",
                Byte.class);
        AttributeType shortAttribute = new AttributeTypeDefault("testShort",
                Short.class);
        AttributeType intAttribute = new AttributeTypeDefault("testInteger",
                Integer.class);
        AttributeType longAttribute = new AttributeTypeDefault("testLong",
                Long.class);
        AttributeType floatAttribute = new AttributeTypeDefault("testFloat",
                Float.class);
        AttributeType doubleAttribute = new AttributeTypeDefault("testDouble",
                Double.class);
        AttributeType stringAttribute = new AttributeTypeDefault("testString",
                String.class);
        AttributeType stringAttribute2 = new AttributeTypeDefault("testString2",
                String.class);

        // Builds the schema
        testSchema = new FeatureTypeFlat(geometryAttribute);

        //LOGGER.finer("created feature type and added geometry");
        testSchema = testSchema.setAttributeType(booleanAttribute);

        //LOGGER.finer("added boolean to feature type");
        testSchema = testSchema.setAttributeType(charAttribute);

        //LOGGER.finer("added character to feature type");
        testSchema = testSchema.setAttributeType(byteAttribute);

        //LOGGER.finer("added byte to feature type");
        testSchema = testSchema.setAttributeType(shortAttribute);

        //LOGGER.finer("added short to feature type");
        testSchema = testSchema.setAttributeType(intAttribute);

        //LOGGER.finer("added int to feature type");
        testSchema = testSchema.setAttributeType(longAttribute);

        //LOGGER.finer("added long to feature type");
        testSchema = testSchema.setAttributeType(floatAttribute);

        //LOGGER.finer("added float to feature type");
        testSchema = testSchema.setAttributeType(doubleAttribute);

        //LOGGER.finer("added double to feature type");
        testSchema = testSchema.setAttributeType(stringAttribute);
        testSchema = testSchema.setAttributeType(stringAttribute2);

        //LOGGER.finer("added string to feature type");
        // Creates coordinates for the linestring
        Coordinate[] coords = new Coordinate[3];
        coords[0] = new Coordinate(1, 2);
        coords[1] = new Coordinate(3, 4);
        coords[2] = new Coordinate(5, 6);

        // Builds the test feature
        Object[] attributes = new Object[11];
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
        attributes[10] = "cow $10";

        // Creates the feature itself
        FlatFeatureFactory factory = new FlatFeatureFactory(testSchema);
        testFeature = factory.create(attributes);

        //LOGGER.finer("...flat feature created");
    }

    /**
     * Sets up a schema and a test feature.
     *
     * @throws IllegalFilterException If the constructed filter is not valid.
     */
    public void testCompare() throws IllegalFilterException {
        // Test all integer permutations
        Expression testAttribute = new AttributeExpressionImpl(testSchema,
                "testInteger");
        compareNumberRunner(testAttribute, AbstractFilter.COMPARE_EQUALS,
            false, true, false);
        compareNumberRunner(testAttribute, AbstractFilter.COMPARE_GREATER_THAN,
            true, false, false);
        compareNumberRunner(testAttribute, AbstractFilter.COMPARE_LESS_THAN,
            false, false, true);
        compareNumberRunner(testAttribute,
            AbstractFilter.COMPARE_GREATER_THAN_EQUAL, true, true, false);
        compareNumberRunner(testAttribute,
            AbstractFilter.COMPARE_LESS_THAN_EQUAL, false, true, true);

        // Set up the string test.
        testAttribute = new AttributeExpressionImpl(testSchema, "testString");

        CompareFilterImpl filter = new CompareFilterImpl(AbstractFilter.COMPARE_EQUALS);
        Expression testLiteral;
        filter.addLeftValue(testAttribute);

        // Test for false positive.
        testLiteral = new LiteralExpressionImpl("test string data");
        filter.addRightValue(testLiteral);

        //LOGGER.info( filter.toString());            
        //LOGGER.info( "contains feature: " + filter.contains(testFeature));
        assertTrue(filter.contains(testFeature));

        // Test for false negative.
        testLiteral = new LiteralExpressionImpl("incorrect test string data");
        filter.addRightValue(testLiteral);

        //LOGGER.info( filter.toString());            
        //LOGGER.info( "contains feature: " + filter.contains(testFeature));
        assertTrue(!filter.contains(testFeature));
    }

    /**
     * Helper class for the integer compare operators.
     *
     * @param testAttribute DOCUMENT ME!
     * @param filterType DOCUMENT ME!
     * @param test1 DOCUMENT ME!
     * @param test2 DOCUMENT ME!
     * @param test3 DOCUMENT ME!
     *
     * @throws IllegalFilterException If the constructed filter is not valid.
     */
    public static void compareNumberRunner(Expression testAttribute,
        short filterType, boolean test1, boolean test2, boolean test3)
        throws IllegalFilterException {
        CompareFilterImpl filter = new CompareFilterImpl(filterType);
        Expression testLiteral;
        filter.addLeftValue(testAttribute);

        testLiteral = new LiteralExpressionImpl(new Integer(1001));
        filter.addRightValue(testLiteral);

        //LOGGER.info( filter.toString());            
        //LOGGER.info( "contains feature: " + filter.contains(testFeature));
        assertEquals(filter.contains(testFeature), test1);

        testLiteral = new LiteralExpressionImpl(new Integer(1002));
        filter.addRightValue(testLiteral);

        //LOGGER.info( filter.toString());            
        //LOGGER.info( "contains feature: " + filter.contains(testFeature));
        assertEquals(filter.contains(testFeature), test2);

        testLiteral = new LiteralExpressionImpl(new Integer(1003));
        filter.addRightValue(testLiteral);

        //LOGGER.info( filter.toString());            
        //LOGGER.info( "contains feature: " + filter.contains(testFeature));
        assertEquals(filter.contains(testFeature), test3);
    }

    /**
     * Tests the like operator.
     *
     * @throws IllegalFilterException If the constructed filter is not valid.
     */
    public void testLike() throws IllegalFilterException {
        Expression testAttribute = null;

        // Set up string
        testAttribute = new AttributeExpressionImpl(testSchema, "testString");

        LikeFilterImpl filter = new LikeFilterImpl();
        filter.setValue(testAttribute);

        // Test for false negative.
        filter.setPattern(new LiteralExpressionImpl("test*"), "*", ".", "!");

        //LOGGER.info( filter.toString());            
        //LOGGER.info( "contains feature: " + filter.contains(testFeature));
        assertTrue(filter.contains(testFeature));

        // Test for false positive.
        filter.setPattern(new LiteralExpressionImpl("cows*"), "*", ".", "!");

        //LOGGER.info( filter.toString());            
        //LOGGER.info( "contains feature: " + filter.contains(testFeature));
        assertTrue(!filter.contains(testFeature));

        // Test for \ as an escape char
        filter.setPattern(new LiteralExpressionImpl("cows\\*"), "*", ".", "\\");

        //LOGGER.info( filter.toString());            
        //LOGGER.info( "contains feature: " + filter.contains(testFeature));
        assertEquals("filter string doesn't match",
            "[ testString is like cows\\* ]", filter.toString());

        // Test for \ as an escape char
        filter.setPattern(new LiteralExpressionImpl("cows\\."), "*", ".", "\\");

        //LOGGER.info( filter.toString());            
        //LOGGER.info( "contains feature: " + filter.contains(testFeature));
        assertEquals("filter string doesn't match",
            "[ testString is like cows\\. ]", filter.toString());

        // test for escaped escapes
        filter.setPattern(new LiteralExpressionImpl("co!!ws*"), "*", ".", "!");

        //LOGGER.info( filter.toString());            
        //LOGGER.info( "contains feature: " + filter.contains(testFeature));
        assertEquals("filter string doesn't match",
            "[ testString is like co!ws.* ]", filter.toString());

        // test for escaped escapes followed by wildcard
        filter.setPattern(new LiteralExpressionImpl("co!!*ws*"), "*", ".", "!");

        //LOGGER.info( filter.toString());            
        //LOGGER.info( "contains feature: " + filter.contains(testFeature));
        assertEquals("filter string doesn't match",
            "[ testString is like co!.*ws.* ]", filter.toString());

        // test for "special" characters in string
        testAttribute = new AttributeExpressionImpl(testSchema, "testString2");
        filter.setValue(testAttribute);
        filter.setPattern(new LiteralExpressionImpl("cow $*"), "*", ".", "!");

        //LOGGER.info( filter.toString());            
        //LOGGER.info( "contains feature: " + filter.contains(testFeature));
        assertTrue("contains feature", filter.contains(testFeature));
        assertEquals("filter string doesn't match",
            "[ testString2 is like cow \\$.* ]", filter.toString());

        // test for first wild card
        filter.setPattern(new LiteralExpressionImpl("*cows*"), "*", ".", "!");

        //LOGGER.info( filter.toString());            
        //LOGGER.info( "contains feature: " + filter.contains(testFeature));
        assertEquals("filter string doesn't match",
            "[ testString2 is like .*cows.* ]", filter.toString());

        // test for multi char parameters
        filter.setPattern(new LiteralExpressionImpl("!#coxxwsyyy"), "xx", "yy",
            "!#");

        //LOGGER.info( filter.toString());            
        //LOGGER.info( "contains feature: " + filter.contains(testFeature));
        assertEquals("filter string doesn't match",
            "[ testString2 is like co.*ws.?y ]", filter.toString());

        // test for multi char parameters which are special
        filter.setPattern(new LiteralExpressionImpl("co.*ws.?\\.*"), ".*",
            ".?", "\\");

        //LOGGER.info( filter.toString());            
        //LOGGER.info( "contains feature: " + filter.contains(testFeature));
        assertEquals("filter string doesn't match",
            "[ testString2 is like co.*ws.?\\.\\* ]", filter.toString());

        // test for reading back in strings which are java regexs
        filter.setPattern(new LiteralExpressionImpl("co.*ws.?\\.\\*"), ".*",
            ".?", "\\");

        //LOGGER.info( filter.toString());            
        //LOGGER.info( "contains feature: " + filter.contains(testFeature));
        assertEquals("filter string doesn't match",
            "[ testString2 is like co.*ws.?\\.\\* ]", filter.toString());
    }

    /**
     * Test the null operator.
     *
     * @throws IllegalFilterException If the constructed filter is not valid.
     */
    public void testNull() throws IllegalFilterException {
        Expression testAttribute = null;

        // Test for false positive.
        testAttribute = new AttributeExpressionImpl(testSchema, "testString");

        NullFilterImpl filter = new NullFilterImpl();
        filter.nullCheckValue(testAttribute);

        //LOGGER.info( filter.toString());            
        //LOGGER.info( "contains feature: " + filter.contains(testFeature));
        assertTrue(!filter.contains(testFeature));

        /*
           testFeature.setAttribute("testString", null);
           assertTrue(!filter.contains(testFeature));
        
           testFeature.setAttribute("testString", "test string data");
           //LOGGER.info( filter.toString());
           //LOGGER.info( "contains feature: " + filter.contains(testFeature));
           assertTrue(!filter.contains(testFeature));
         */
    }

    /**
     * Test the between operator.
     *
     * @throws IllegalFilterException If the constructed filter is not valid.
     */
    public void testBetween() throws IllegalFilterException {
        // Set up the integer
        BetweenFilterImpl filter = new BetweenFilterImpl();
        Expression testLiteralLower = new LiteralExpressionImpl(new Integer(
                    1001));
        Expression testAttribute = new AttributeExpressionImpl(testSchema,
                "testInteger");
        Expression testLiteralUpper = new LiteralExpressionImpl(new Integer(
                    1003));

        // String tests
        filter.addLeftValue(testLiteralLower);
        filter.addMiddleValue(testAttribute);
        filter.addRightValue(testLiteralUpper);

        // Test for false negative.
        //LOGGER.info( filter.toString());            
        //LOGGER.info( "contains feature: " + filter.contains(testFeature));
        assertTrue(filter.contains(testFeature));

        // Test for false positive.
        testLiteralLower = new LiteralExpressionImpl(new Integer(1));
        testLiteralUpper = new LiteralExpressionImpl(new Integer(1000));
        filter.addLeftValue(testLiteralLower);
        filter.addRightValue(testLiteralUpper);

        //LOGGER.info( filter.toString());            
        //LOGGER.info( "contains feature: " + filter.contains(testFeature));
        assertTrue(!filter.contains(testFeature));
    }

    /**
     * Test the geometry operators.
     *
     * @throws IllegalFilterException If the constructed filter is not valid.
     */
    public void testGeometry() throws IllegalFilterException {
        Coordinate[] coords = new Coordinate[3];
        coords[0] = new Coordinate(1, 2);
        coords[1] = new Coordinate(3, 4);
        coords[2] = new Coordinate(5, 6);

        // Test Equals
        GeometryFilterImpl filter = new GeometryFilterImpl(AbstractFilter.GEOMETRY_EQUALS);
        Expression left = new AttributeExpressionImpl(testSchema, "testGeometry");
        filter.addLeftGeometry(left);

        Expression right = new LiteralExpressionImpl(new LineString(coords,
                    new PrecisionModel(), 1));
        filter.addRightGeometry(right);

        //LOGGER.info( filter.toString());            
        //LOGGER.info( "contains feature: " + filter.contains(testFeature));
        assertTrue(filter.contains(testFeature));

        coords[0] = new Coordinate(0, 0);
        right = new LiteralExpressionImpl(new LineString(coords,
                    new PrecisionModel(), 1));
        filter.addRightGeometry(right);

        //LOGGER.info( filter.toString());            
        //LOGGER.info( "contains feature: " + filter.contains(testFeature));
        assertTrue(!filter.contains(testFeature));

        // Test Disjoint
        filter = new GeometryFilterImpl(AbstractFilter.GEOMETRY_DISJOINT);
        left = new AttributeExpressionImpl(testSchema, "testGeometry");
        filter.addLeftGeometry(left);

        coords[0] = new Coordinate(0, 0);
        coords[1] = new Coordinate(3, 0);
        coords[2] = new Coordinate(6, 0);
        right = new LiteralExpressionImpl(new LineString(coords,
                    new PrecisionModel(), 1));
        filter.addRightGeometry(right);

        //LOGGER.info( filter.toString());            
        //LOGGER.info( "contains feature: " + filter.contains(testFeature));
        assertTrue(filter.contains(testFeature));

        coords[0] = new Coordinate(1, 2);
        coords[1] = new Coordinate(3, 0);
        coords[2] = new Coordinate(6, 0);
        right = new LiteralExpressionImpl(new LineString(coords,
                    new PrecisionModel(), 1));
        filter.addRightGeometry(right);

        //LOGGER.info( filter.toString());            
        //LOGGER.info( "contains feature: " + filter.contains(testFeature));
        assertTrue(!filter.contains(testFeature));

        // Test BBOX
        filter = new GeometryFilterImpl(AbstractFilter.GEOMETRY_BBOX);
        left = new AttributeExpressionImpl(testSchema, "testGeometry");
        filter.addLeftGeometry(left);

        Coordinate[] coords2 = new Coordinate[5];
        coords2[0] = new Coordinate(0, 0);
        coords2[1] = new Coordinate(10, 0);
        coords2[2] = new Coordinate(10, 10);
        coords2[3] = new Coordinate(0, 10);
        coords2[4] = new Coordinate(0, 0);
        right = new LiteralExpressionImpl(new Polygon(
                    new LinearRing(coords2, new PrecisionModel(), 1), null,
                    new PrecisionModel(), 1));
        filter.addRightGeometry(right);

        //LOGGER.info( filter.toString());
        //LOGGER.info( "contains feature: " + filter.contains(testFeature));
        assertTrue(filter.contains(testFeature));

        coords2[0] = new Coordinate(0, 0);
        coords2[1] = new Coordinate(1, 0);
        coords2[2] = new Coordinate(1, 1);
        coords2[3] = new Coordinate(0, 1);
        coords2[4] = new Coordinate(0, 0);
        right = new LiteralExpressionImpl(new Polygon(
                    new LinearRing(coords2, new PrecisionModel(), 1), null,
                    new PrecisionModel(), 1));
        filter.addRightGeometry(right);

        //LOGGER.info( filter.toString());            
        //LOGGER.info( "contains feature: " + filter.contains(testFeature));
        assertTrue(!filter.contains(testFeature));
    }

    public void testDistanceGeometry() throws Exception {
        // Test DWithin
        GeometryDistanceFilter filter = new CartesianDistanceFilter(AbstractFilter.GEOMETRY_DWITHIN);
        Expression left = new AttributeExpressionImpl(testSchema, "testGeometry");
        filter.addLeftGeometry(left);

        Coordinate[] coords2 = new Coordinate[5];
        coords2[0] = new Coordinate(10, 10);
        coords2[1] = new Coordinate(15, 10);
        coords2[2] = new Coordinate(15, 15);
        coords2[3] = new Coordinate(10, 15);
        coords2[4] = new Coordinate(10, 10);

        Expression right = new LiteralExpressionImpl(new Polygon(
                    new LinearRing(coords2, new PrecisionModel(), 1), null,
                    new PrecisionModel(), 1));
        filter.addRightGeometry(right);
        filter.setDistance(20);
        LOGGER.info(filter.toString());
        LOGGER.info("contains feature: " + filter.contains(testFeature));

        //assertTrue(filter.contains(testFeature));
        filter.setDistance(2);
        LOGGER.info(filter.toString());
        LOGGER.info("contains feature: " + filter.contains(testFeature));

        //Test Beyond
        GeometryDistanceFilter filterB = new CartesianDistanceFilter(AbstractFilter.GEOMETRY_BEYOND);
        filterB.addLeftGeometry(left);
        filterB.addRightGeometry(right);
        filterB.setDistance(20);
        LOGGER.info(filterB.toString());
        LOGGER.info("contains feature: " + filterB.contains(testFeature));

        //assertTrue(filter.contains(testFeature));
        filterB.setDistance(2);
        LOGGER.info(filterB.toString());
        LOGGER.info("contains feature: " + filterB.contains(testFeature));

        /*coords2[0] = new Coordinate(20,20);
           /coords2[1] = new Coordinate(21,20);
           coords2[2] = new Coordinate(21,21);
           coords2[3] = new Coordinate(20,21);
           coords2[4] = new Coordinate(20,20);
           right = new LiteralExpressionImpl(new Polygon(new LinearRing(coords2,new PrecisionModel(), 1),
                                                     null, new PrecisionModel(), 1));
           filter.addRightGeometry(right);
           //LOGGER.info( filter.toString());
           //LOGGER.info( "contains feature: " + filter.contains(testFeature));
           assertTrue(!filter.contains(testFeature));
         */

        //Test Beyond
    }

    /**
     * Test the logic operators.
     *
     * @throws IllegalFilterException If the constructed filter is not valid.
     */
    public void testLogic() throws IllegalFilterException {
        Expression testAttribute = null;

        // Set up true sub filter
        testAttribute = new AttributeExpressionImpl(testSchema, "testString");

        CompareFilterImpl filterTrue = new CompareFilterImpl(AbstractFilter.COMPARE_EQUALS);
        Expression testLiteral;
        filterTrue.addLeftValue(testAttribute);
        testLiteral = new LiteralExpressionImpl("test string data");
        filterTrue.addRightValue(testLiteral);

        // Set up false sub filter
        CompareFilterImpl filterFalse = new CompareFilterImpl(AbstractFilter.COMPARE_EQUALS);
        filterFalse.addLeftValue(testAttribute);
        testLiteral = new LiteralExpressionImpl("incorrect test string data");
        filterFalse.addRightValue(testLiteral);

        // Test OR for false negatives
        LogicFilterImpl filter = new LogicFilterImpl(filterFalse, filterTrue,
                AbstractFilter.LOGIC_OR);

        //LOGGER.info( filter.toString());            
        //LOGGER.info( "contains feature: " + filter.contains(testFeature));
        assertTrue(filter.contains(testFeature));

        // Test OR for false negatives
        filter = new LogicFilterImpl(filterTrue, filterTrue,
                AbstractFilter.LOGIC_OR);

        //LOGGER.info( filter.toString());            
        //LOGGER.info( "contains feature: " + filter.contains(testFeature));
        assertTrue(filter.contains(testFeature));

        // Test OR for false positives
        filter = new LogicFilterImpl(filterFalse, filterFalse,
                AbstractFilter.LOGIC_OR);

        //LOGGER.info( filter.toString());            
        //LOGGER.info( "contains feature: " + filter.contains(testFeature));
        assertTrue(!filter.contains(testFeature));

        // Test AND for false positives
        filter = new LogicFilterImpl(filterFalse, filterTrue,
                AbstractFilter.LOGIC_AND);

        //LOGGER.info( filter.toString());            
        //LOGGER.info( "contains feature: " + filter.contains(testFeature));
        assertTrue(!filter.contains(testFeature));

        // Test AND for false positives
        filter = new LogicFilterImpl(filterTrue, filterFalse,
                AbstractFilter.LOGIC_AND);

        //LOGGER.info( filter.toString());            
        //LOGGER.info( "contains feature: " + filter.contains(testFeature));
        assertTrue(!filter.contains(testFeature));

        // Test AND for false positives
        filter = new LogicFilterImpl(filterTrue, filterTrue,
                AbstractFilter.LOGIC_AND);

        //LOGGER.info( filter.toString());            
        //LOGGER.info( "contains feature: " + filter.contains(testFeature));
        assertTrue(filter.contains(testFeature));
    }
}
