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

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.geom.PrecisionModel;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.geotools.feature.AttributeType;
import org.geotools.feature.AttributeTypeFactory;
import org.geotools.feature.FeatureType;
import org.geotools.feature.FeatureTypeFactory;
import org.geotools.feature.IllegalAttributeException;
import org.geotools.feature.SchemaException;
import java.util.Set;
import java.util.logging.Logger;


/**
 * Unit test for filters.  Note that this unit test does not encompass all of filter package, just
 * the filters themselves.  There is a seperate unit test for expressions.
 *
 * @author Andrea Aime, SATA
 */
public class FilterAttributeExtractorTest extends TestCase {
    /** The logger for the filter module. */
    private static final Logger LOGGER = Logger.getLogger("org.geotools.filter");

    /** Schema on which to preform tests */
    private static FeatureType testSchema = null;
    private static AttributeTypeFactory attFactory = AttributeTypeFactory.newInstance();
    boolean set = false;
    FilterAttributeExtractor fae;
    FilterFactory fac;

    /** Test suite for this test case */
    TestSuite suite = null;

    /**
     * Constructor with test name.
     *
     * @param testName DOCUMENT ME!
     */
    public FilterAttributeExtractorTest(String testName) {
        super(testName);
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
        TestSuite suite = new TestSuite(FilterAttributeExtractorTest.class);

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

        fae = new FilterAttributeExtractor();

        fac = FilterFactory.createFilterFactory();

        // Create the schema attributes
        AttributeType geometryAttribute = AttributeTypeFactory.newAttributeType("testGeometry",
                LineString.class);

        AttributeType booleanAttribute = AttributeTypeFactory.newAttributeType("testBoolean",
                Boolean.class);

        AttributeType charAttribute = AttributeTypeFactory.newAttributeType("testCharacter",
                Character.class);
        AttributeType byteAttribute = AttributeTypeFactory.newAttributeType("testByte", Byte.class);
        AttributeType shortAttribute = AttributeTypeFactory.newAttributeType("testShort",
                Short.class);
        AttributeType intAttribute = AttributeTypeFactory.newAttributeType("testInteger",
                Integer.class);
        AttributeType longAttribute = AttributeTypeFactory.newAttributeType("testLong", Long.class);
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
    }

    /**
     * Sets up a schema and a test feature.
     *
     * @throws IllegalFilterException If the constructed filter is not valid.
     */
    public void testCompare() throws IllegalFilterException {
        // Test all integer permutations
        Expression testAttribute = new AttributeExpressionImpl(testSchema, "testInteger");

        // Set up the string test.
        testAttribute = new AttributeExpressionImpl(testSchema, "testString");

        CompareFilterImpl filter = new CompareFilterImpl(AbstractFilter.COMPARE_EQUALS);
        Expression testLiteral;
        filter.addLeftValue(testAttribute);
        testLiteral = new LiteralExpressionImpl("test string data");
        filter.addRightValue(testLiteral);

        assertAttributeName(filter, "testString");
    }

    private void assertAttributeName(Filter filter, String name) {
        assertAttributeName(filter, new String[] { name });
    }

    private void assertAttributeName(Filter filter, String[] names) {
        fae.clear();
        filter.accept(fae);

        Set attNames = fae.getAttributeNameSet();

        assertNotNull(attNames);
        assertEquals(attNames.size(), names.length);

        for (int i = 0; i < names.length; i++) {
            assertTrue(attNames.contains(names[i]));
        }
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

        assertAttributeName(filter, "testString");
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

        assertAttributeName(filter, new String[0]);
    }

    /**
     * Test the between operator.
     *
     * @throws IllegalFilterException If the constructed filter is not valid.
     */
    public void testBetween() throws IllegalFilterException {
        // Set up the integer
        BetweenFilter filter = fac.createBetweenFilter();
        Expression testLiteralLower = new LiteralExpressionImpl(new Integer(1001));
        Expression testAttribute = new AttributeExpressionImpl(testSchema, "testInteger");
        Expression testLiteralUpper = new LiteralExpressionImpl(new Integer(1003));

        filter.addLeftValue(testLiteralLower);
        filter.addMiddleValue(testLiteralLower);
        filter.addRightValue(testLiteralUpper);
        assertAttributeName(filter, new String[0]);

        filter.addLeftValue(testLiteralLower);
        filter.addMiddleValue(testAttribute);
        filter.addRightValue(testLiteralUpper);
        assertAttributeName(filter, "testInteger");

        filter.addLeftValue(testAttribute);
        filter.addMiddleValue(testAttribute);
        filter.addRightValue(testAttribute);
        assertAttributeName(filter, "testInteger");

        filter.addLeftValue(new AttributeExpressionImpl(testSchema, "testInteger"));
        filter.addMiddleValue(new AttributeExpressionImpl(testSchema, "testLong"));
        filter.addRightValue(new AttributeExpressionImpl(testSchema, "testFloat"));
        assertAttributeName(filter, new String[] { "testInteger", "testLong", "testFloat" });
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

        Expression geom = new LiteralExpressionImpl(new LineString(coords, new PrecisionModel(), 1));
        filter.addRightGeometry(geom);
        assertAttributeName(filter, "testGeometry");

        filter.addRightGeometry(new AttributeExpressionImpl(testSchema, "testGeometry"));
        assertAttributeName(filter, "testGeometry");

        filter.addLeftGeometry(geom);
        assertAttributeName(filter, "testGeometry");
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
                    new LinearRing(coords2, new PrecisionModel(), 1), null, new PrecisionModel(), 1));
        filter.addRightGeometry(right);
        filter.setDistance(20);

        assertAttributeName(filter, "testGeometry");
    }

    public void testFid() throws IllegalAttributeException {
        FidFilter filter = fac.createFidFilter();
        assertAttributeName(filter, new String[0]);
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
        LogicFilter filter = fac.createLogicFilter(filterFalse, filterTrue, AbstractFilter.LOGIC_OR);

        assertAttributeName(filter, "testString");
    }
}
