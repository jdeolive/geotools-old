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
import org.geotools.datasource.extents.*;
import org.geotools.feature.*;
import org.geotools.gml.GMLFilterDocument;
import org.geotools.gml.GMLFilterGeometry;
import org.geotools.resources.Geotools;
import org.w3c.dom.*;
import java.io.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.*;


/**
 * Unit test for SQLEncoderPostgis.  This is a complimentary  test suite with
 * the filter test suite.
 *
 * @author James MacGill, CCG
 * @author Chris Holmes, TOPP
 */
public class SQLEncoderPostgisTest extends TestCase {
    /** Standard logging instance */
    private static final Logger LOGGER = Logger.getLogger("org.geotools.filter");

    /** Schema on which to preform tests */
    private static FeatureType testSchema = null;

    /** Schema on which to preform tests */
    private static Feature testFeature = null;

    static {
        Geotools.init("Log4JFormatter", Level.FINER);
    }

    /** Test suite for this test case */
    TestSuite suite = null;

    /** Constructor with test name. */
    String dataFolder = "";
    boolean setup = false;

    public SQLEncoderPostgisTest(String testName) {
        super(testName);
        LOGGER.info("running SQLEncoderTests");
        ;
        dataFolder = System.getProperty("dataFolder");

        if (dataFolder == null) {
            //then we are being run by maven
            dataFolder = System.getProperty("basedir");
            dataFolder += "/tests/unit/testData";
        }
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
        TestSuite suite = new TestSuite(SQLEncoderPostgisTest.class);

        return suite;
    }

    /**
     * Sets up a schema and a test feature.
     *
     * @throws SchemaException If there is a problem setting up the schema.
     * @throws IllegalFeatureException If problem setting up the feature.
     */
    protected void setUp() throws SchemaException, IllegalFeatureException {
        if (setup) {
            return;
        }

        setup = true;

        // Create the schema attributes
        LOGGER.finer("creating flat feature...");

        AttributeType geometryAttribute = new AttributeTypeDefault("testGeometry",
                LineString.class);
        LOGGER.finer("created geometry attribute");

        AttributeType booleanAttribute = new AttributeTypeDefault("testBoolean",
                Boolean.class);
        LOGGER.finer("created boolean attribute");

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

        // Builds the schema
        testSchema = new FeatureTypeFlat(geometryAttribute);
        LOGGER.finer("created feature type and added geometry");
        testSchema = testSchema.setAttributeType(booleanAttribute);
        LOGGER.finer("added boolean to feature type");
        testSchema = testSchema.setAttributeType(charAttribute);
        LOGGER.finer("added character to feature type");
        testSchema = testSchema.setAttributeType(byteAttribute);
        LOGGER.finer("added byte to feature type");
        testSchema = testSchema.setAttributeType(shortAttribute);
        LOGGER.finer("added short to feature type");
        testSchema = testSchema.setAttributeType(intAttribute);
        LOGGER.finer("added int to feature type");
        testSchema = testSchema.setAttributeType(longAttribute);
        LOGGER.finer("added long to feature type");
        testSchema = testSchema.setAttributeType(floatAttribute);
        LOGGER.finer("added float to feature type");
        testSchema = testSchema.setAttributeType(doubleAttribute);
        LOGGER.finer("added double to feature type");
        testSchema = testSchema.setAttributeType(stringAttribute);
        LOGGER.finer("added string to feature type");

        GeometryFactory geomFac = new GeometryFactory();

        // Creates coordinates for the linestring
        Coordinate[] coords = new Coordinate[3];
        coords[0] = new Coordinate(1, 2);
        coords[1] = new Coordinate(3, 4);
        coords[2] = new Coordinate(5, 6);

        // Builds the test feature
        Object[] attributes = new Object[10];
        attributes[0] = geomFac.createLineString(coords);
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
        FeatureFactory factory = new FeatureFactory(testSchema);
        testFeature = factory.create(attributes);
        LOGGER.finer("...flat feature created");
    }

    public void test1() throws Exception {
        GeometryFilterImpl gf = new GeometryFilterImpl(AbstractFilter.GEOMETRY_BBOX);
        LiteralExpressionImpl right = new BBoxExpressionImpl(new Envelope(0,
                    300, 0, 300));
        gf.addRightGeometry(right);

        AttributeExpressionImpl left = new AttributeExpressionImpl(testSchema,
                "testGeometry");
        gf.addLeftGeometry(left);

        SQLEncoderPostgis encoder = new SQLEncoderPostgis(2346);
        String out = encoder.encode((AbstractFilterImpl) gf);
        LOGGER.finer("Resulting SQL filter is \n" + out);
        assertTrue(out.equals("WHERE testGeometry && GeometryFromText(" +
                "'POLYGON ((0 0, 0 300, 300 300, 300 0, 0 0))'" + ", 2346)"));
    }

    public void test2() throws Exception {
        GeometryFilterImpl gf = new GeometryFilterImpl(AbstractFilter.GEOMETRY_BBOX);
        LiteralExpressionImpl left = new BBoxExpressionImpl(new Envelope(10,
                    300, 10, 300));
        gf.addLeftGeometry(left);

        AttributeExpressionImpl right = new AttributeExpressionImpl(testSchema,
                "testGeometry");
        gf.addRightGeometry(right);

        SQLEncoderPostgis encoder = new SQLEncoderPostgis(2346);
        String out = encoder.encode((AbstractFilterImpl) gf);
        LOGGER.finer("Resulting SQL filter is \n" + out);
        assertTrue(out.equals("WHERE GeometryFromText(" +
                "'POLYGON ((10 10, 10 300, 300 300, 300 10, 10 10))'" +
                ", 2346) && testGeometry"));
    }

    public void test3() throws Exception {
        FilterFactory filterFac = FilterFactory.createFilterFactory();
        CompareFilter compFilter = filterFac.createCompareFilter(AbstractFilter.COMPARE_EQUALS);
        compFilter.addLeftValue(filterFac.createAttributeExpression(
                testSchema, "testInteger"));
        compFilter.addRightValue(filterFac.createLiteralExpression(
                new Double(5)));

        SQLEncoderPostgis encoder = new SQLEncoderPostgis(2346);
        String out = encoder.encode((AbstractFilterImpl) compFilter);
        LOGGER.finer("Resulting SQL filter is \n" + out);
    }

    public void testException() throws Exception {
        GeometryFilterImpl gf = new GeometryFilterImpl(AbstractFilter.GEOMETRY_BEYOND);
        LiteralExpressionImpl right = new BBoxExpressionImpl(new Envelope(10,
                    10, 300, 300));
        gf.addRightGeometry(right);

        AttributeExpressionImpl left = new AttributeExpressionImpl(testSchema,
                "testGeometry");
        gf.addLeftGeometry(left);

        try {
            SQLEncoderPostgis encoder = new SQLEncoderPostgis(2346);
            String out = encoder.encode((AbstractFilterImpl) gf);
            LOGGER.fine("out is " + out);
        } catch (SQLEncoderException e) {
            LOGGER.finer(e.getMessage());
            assertTrue(e.getMessage().equals("Filter type not supported"));
        }
    }
}
