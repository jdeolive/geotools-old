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
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.geom.PrecisionModel;


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
    
    FilterFactory fac;

      private static AttributeTypeFactory attFactory = AttributeTypeFactory.newInstance();

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
     * @throws IllegalAttributeException If problem setting up the feature.
     */
    protected void setUp() throws SchemaException, IllegalAttributeException {
        if (set) {
            return;
        }

        set = true;
        
        fac = FilterFactory.createFilterFactory();

        // Create the schema attributes
        //LOGGER.debug("creating flat feature...");
        AttributeType geometryAttribute = AttributeTypeFactory.newAttributeType("testGeometry",
                LineString.class);

        //LOGGER.debug("created geometry attribute");
        AttributeType booleanAttribute = AttributeTypeFactory.newAttributeType("testBoolean",
                Boolean.class);

        //LOGGER.debug("created boolean attribute");
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
        AttributeType stringAttribute2 = AttributeTypeFactory.newAttributeType("testString2",
                String.class);

        // Builds the schema
	FeatureTypeFactory feaTypeFactory = FeatureTypeFactory.newInstance("test");
	feaTypeFactory.addType(geometryAttribute);
	 
        LOGGER.finest("created feature type and added geometry");
        feaTypeFactory.addType(booleanAttribute);
        LOGGER.finest("added boolean to feature type");
        feaTypeFactory.addType(charAttribute);
        LOGGER.finest("added character to feature type");
        feaTypeFactory.addType(byteAttribute);
        LOGGER.finest("added byte to feature type");
        feaTypeFactory.addType(shortAttribute);
        //LOGGER.finer("added short to feature type");
       feaTypeFactory.addType(intAttribute);

        //LOGGER.finer("added int to feature type");
       feaTypeFactory.addType(longAttribute);

        //LOGGER.finer("added long to feature type");
       feaTypeFactory.addType(floatAttribute);

        //LOGGER.finer("added float to feature type");
       feaTypeFactory.addType(doubleAttribute);

        //LOGGER.finer("added double to feature type");
       feaTypeFactory.addType(stringAttribute);
       feaTypeFactory.addType(stringAttribute2);
       
       testSchema = feaTypeFactory.getFeatureType();

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
        //FlatFeatureFactory factory = new FlatFeatureFactory(testSchema);
	testFeature = testSchema.create(attributes);
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

        LikeFilter filter = fac.createLikeFilter();
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

        NullFilter filter = fac.createNullFilter();
        assertTrue(!filter.contains(testFeature));
        
        filter.nullCheckValue(testAttribute);
        assertEquals(testAttribute , filter.getNullCheckValue());

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
        BetweenFilter filter = fac.createBetweenFilter();
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

    public void testFid() throws IllegalAttributeException {
        
        FidFilter ff = fac.createFidFilter();
        assertTrue(!ff.contains(testFeature));
        ff.addFid(testFeature.getID());
        assertTrue(ff.contains(testFeature));
        assertTrue(!ff.contains(null));
        
        FidFilter ff2 = fac.createFidFilter(testFeature.getID());
        assertNotNull(ff2);
        assertTrue(ff2.contains(testFeature));  
        
        assertEquals(1,((FidFilterImpl)ff).getFids().length);
        ff.addFid("another id");
        assertEquals(2,((FidFilterImpl)ff).getFids().length);
       
        ff.removeFid("another id");
        assertEquals(1,((FidFilterImpl)ff).getFids().length);
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
        CompareFilter filterTrue = fac.createCompareFilter(AbstractFilter.COMPARE_EQUALS);
        Expression testLiteral;
        filterTrue.addLeftValue(testAttribute);
        testLiteral = new LiteralExpressionImpl("test string data");
        filterTrue.addRightValue(testLiteral);

        // Set up false sub filter
        CompareFilter filterFalse = fac.createCompareFilter(AbstractFilter.COMPARE_EQUALS);
        filterFalse.addLeftValue(testAttribute);
        testLiteral = new LiteralExpressionImpl("incorrect test string data");
        filterFalse.addRightValue(testLiteral);

        // Test OR for false negatives
        LogicFilter filter = fac.createLogicFilter(filterFalse, filterTrue,
                AbstractFilter.LOGIC_OR);

        //LOGGER.info( filter.toString());            
        //LOGGER.info( "contains feature: " + filter.contains(testFeature));
        assertTrue(filter.contains(testFeature));

        // Test OR for false negatives
        filter = fac.createLogicFilter(filterTrue, filterTrue,
                AbstractFilter.LOGIC_OR);

        //LOGGER.info( filter.toString());            
        //LOGGER.info( "contains feature: " + filter.contains(testFeature));
        assertTrue(filter.contains(testFeature));

        // Test OR for false positives
        filter = fac.createLogicFilter(filterFalse, filterFalse,
                AbstractFilter.LOGIC_OR);
         //as above but with shortcut
        Filter filter2 = filterFalse.or(filterTrue);
        assertTrue(!filter.contains(testFeature));

        //LOGGER.info( filter.toString());            
        //LOGGER.info( "contains feature: " + filter.contains(testFeature));
        assertTrue(!filter.contains(testFeature));
        //as above but with shortcut
        filter2 = filterFalse.or(filterFalse);
        assertTrue(!filter.contains(testFeature));

        // Test AND for false positives
        filter = fac.createLogicFilter(filterFalse, filterTrue,
                AbstractFilter.LOGIC_AND);

        //LOGGER.info( filter.toString());            
        //LOGGER.info( "contains feature: " + filter.contains(testFeature));
        assertTrue(!filter.contains(testFeature));

        // Test AND for false positives
        filter = fac.createLogicFilter(filterTrue, filterFalse,
                AbstractFilter.LOGIC_AND);

        //LOGGER.info( filter.toString());            
        //LOGGER.info( "contains feature: " + filter.contains(testFeature));
        assertTrue(!filter.contains(testFeature));

        // Test AND for false positives
        filter = fac.createLogicFilter(filterTrue, filterTrue,
                AbstractFilter.LOGIC_AND);
        filter2 = filterTrue.and(filterTrue);
        //LOGGER.info( filter.toString());            
        //LOGGER.info( "contains feature: " + filter.contains(testFeature));
        assertTrue(filter.contains(testFeature));
        assertTrue(filter2.contains(testFeature));
        
        //finaly test noting shortcut
        filter2 = filterFalse.not();
        assertTrue(filter2.contains(testFeature));
    }
}
