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
import org.xml.sax.*;
import org.xml.sax.helpers.*;
import java.io.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.*;


/**
 * Unit test for expressions.  This is a complimentary test suite with the
 * filter test suite.
 *
 * @author James MacGill, CCG
 * @author Rob Hranac, TOPP
 */
public class ParserTest extends TestCase {
    /** Standard logging instance */
    private static final Logger LOGGER = Logger.getLogger("org.geotools.filter");

    static {
        Geotools.init("Log4JFormatter", Level.FINE);
    }

    /** Schema on which to preform tests */
    private static FeatureType testSchema = null;

    /** Schema on which to preform tests */
    private static Feature testFeature = null;

    /** Test suite for this test case */
    TestSuite suite = null;

    /** Constructor with test name. */
    String dataFolder = "";
    boolean setup = false;

    public ParserTest(String testName) {
        super(testName);
        dataFolder = System.getProperty("dataFolder");

        if (dataFolder == null) {
            //then we are being run by maven
            dataFolder = System.getProperty("basedir");
            dataFolder = "file:////" + dataFolder + "/tests/unit/testData"; //url.toString();
            LOGGER.fine("data folder is " + dataFolder);
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
        TestSuite suite = new TestSuite(ParserTest.class);

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
        LOGGER.fine("creating flat feature...");

        AttributeType geometryAttribute = new AttributeTypeDefault("testGeometry",
                LineString.class);
        LOGGER.fine("created geometry attribute");

        AttributeType booleanAttribute = new AttributeTypeDefault("testBoolean",
                Boolean.class);
        LOGGER.fine("created boolean attribute");

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
        AttributeType doubleZeroAttribute = new AttributeTypeDefault("testZeroDouble",
                Double.class);
        AttributeType stringAttribute = new AttributeTypeDefault("testString",
                String.class);

        // Builds the schema
        testSchema = new FeatureTypeFlat(geometryAttribute);
        LOGGER.fine("created feature type and added geometry");
        testSchema = testSchema.setAttributeType(booleanAttribute);
        LOGGER.fine("added boolean to feature type");
        testSchema = testSchema.setAttributeType(charAttribute);
        LOGGER.fine("added character to feature type");
        testSchema = testSchema.setAttributeType(byteAttribute);
        LOGGER.fine("added byte to feature type");
        testSchema = testSchema.setAttributeType(shortAttribute);
        LOGGER.fine("added short to feature type");
        testSchema = testSchema.setAttributeType(intAttribute);
        LOGGER.fine("added int to feature type");
        testSchema = testSchema.setAttributeType(longAttribute);
        LOGGER.fine("added long to feature type");
        testSchema = testSchema.setAttributeType(floatAttribute);
        LOGGER.fine("added float to feature type");
        testSchema = testSchema.setAttributeType(doubleAttribute);
        LOGGER.fine("added double to feature type");
        testSchema = testSchema.setAttributeType(doubleZeroAttribute);
        LOGGER.fine("added doubleZero to feature type");
        testSchema = testSchema.setAttributeType(stringAttribute);
        LOGGER.fine("added string to feature type");

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
        attributes[9] = new Double(0.0);
        attributes[10] = "test string data";

        // Creates the feature itself
        FlatFeatureFactory factory = new FlatFeatureFactory(testSchema);
        testFeature = factory.create(attributes);
        LOGGER.fine("...flat feature created");
    }

    public void test1() throws Exception {
        Filter test = parseDocument(dataFolder + "/test1.xml");
        LOGGER.fine("filter: " + test.getClass().toString());
        LOGGER.fine("parsed: " + test.toString());
    }

    public void test2() throws Exception {
        Filter test = parseDocument(dataFolder + "/test2.xml");
        LOGGER.fine("filter: " + test.getClass().toString());
        LOGGER.fine("parsed: " + test.toString());
    }

    public void test3a() throws Exception {
        Filter test = parseDocument(dataFolder + "/test3a.xml");
        LOGGER.fine("filter: " + test.getClass().toString());
        LOGGER.fine("parsed: " + test.toString());
    }

    public void test3b() throws Exception {
        Filter test = parseDocument(dataFolder + "/test3b.xml");
        LOGGER.fine("filter: " + test.getClass().toString());
        LOGGER.fine("parsed: " + test.toString());
    }

    public void test4() throws Exception {
        Filter test = parseDocument(dataFolder + "/test4.xml");
        LOGGER.fine("filter: " + test.getClass().toString());
        LOGGER.fine("parsed: " + test.toString());
    }

    public void test5() throws Exception {
        Filter test = parseDocument(dataFolder + "/test5.xml");
        LOGGER.fine("filter: " + test.getClass().toString());
        LOGGER.fine("parsed: " + test.toString());
    }

    public void test6() throws Exception {
        Filter test = parseDocument(dataFolder + "/test6.xml");
        LOGGER.fine("filter: " + test.getClass().toString());
        LOGGER.fine("parsed: " + test.toString());
    }

    public void test8() throws Exception {
        Filter test = parseDocument(dataFolder + "/test8.xml");
        LOGGER.fine("filter: " + test.getClass().toString());
        LOGGER.fine("parsed: " + test.toString());
    }

    public void test9() throws Exception {
        Filter test = parseDocument(dataFolder + "/test9.xml");
        LOGGER.fine("filter: " + test.getClass().toString());
        LOGGER.fine("parsed: " + test.toString());
    }

    public void test11() throws Exception {
        Filter test = parseDocument(dataFolder + "/test11.xml");
        LOGGER.fine("filter: " + test.getClass().toString());
        LOGGER.fine("parsed: " + test.toString());
    }

    public void test12() throws Exception {
        Filter test = parseDocument(dataFolder + "/test12.xml");
        LOGGER.fine("filter: " + test.getClass().toString());
        LOGGER.fine("parsed: " + test.toString());
    }

    public void test13() throws Exception {
        Filter test = parseDocument(dataFolder + "/test13.xml");
        LOGGER.fine("filter: " + test.getClass().toString());
        LOGGER.fine("parsed: " + test.toString());
    }

    public void test14() throws Exception {
        Filter test = parseDocument(dataFolder + "/test14.xml");
        LOGGER.fine("filter: " + test.getClass().toString());
        LOGGER.fine("parsed: " + test.toString());
    }

    public void test15() throws Exception {
        Filter test = parseDocument(dataFolder + "/test15.xml");
        LOGGER.fine("filter: " + test.getClass().toString());
        LOGGER.fine("parsed: " + test.toString());
    }

    public void test16() throws Exception {
        Filter test = parseDocument(dataFolder + "/test16.xml");
        LOGGER.fine("filter: " + test.getClass().toString());
        LOGGER.fine("parsed: " + test.toString());
    }

    public void test17() throws Exception {
        Filter test = parseDocument(dataFolder + "/test17.xml");
        LOGGER.fine("filter: " + test.getClass().toString());
        LOGGER.fine("parsed: " + test.toString());
    }

    /* 18 and 19 have multiple filters, and only last one will print,
       but logicSAXParser was messing up with multiple filters, and
       I used these tests to fix it.  To make these effective the
       parser test should be able to get multiple filters.  And shouldn't
       we also be checking the filters generated programmatically, so they
       fail if things mess up?  I don't have time right now, but maybe
       some time soon...cholmes */
    public void test18() throws Exception {
        Filter test = parseDocument(dataFolder + "/test18.xml");
        LOGGER.fine("filter: " + test.getClass().toString());
        LOGGER.fine("parsed: " + test.toString());
    }

    public void test19() throws Exception {
        Filter test = parseDocument(dataFolder + "/test19.xml");
        LOGGER.fine("filter: " + test.getClass().toString());
        LOGGER.fine("parsed: " + test.toString());
    }

    public void test20() throws Exception {
        Filter test = parseDocument(dataFolder + "/test20.xml");

        LOGGER.fine("filter: " + test.getClass().toString());
        LOGGER.fine("parsed: " + test.toString());
    }

    //    public void test27()
    //        throws Exception {
    //        Filter test = parseDocument(dataFolder+"/test27.xml");
    //        LOGGER.fine("filter: " + test.getClass().toString());
    //        LOGGER.fine("parsed: " + test.toString());
    //    } 
    public Filter parseDocument(String uri) throws Exception {
        SAXParserFactory factory = SAXParserFactory.newInstance();
        LOGGER.info("about to parse: " + uri);
        LOGGER.fine("just created factory");

        // chains all the appropriate filters together (in correct order)
        //  and initiates parsing
        TestFilterHandler filterHandler = new TestFilterHandler();
        FilterFilter filterFilter = new FilterFilter(filterHandler, null);
        GMLFilterGeometry geometryFilter = new GMLFilterGeometry(filterFilter);
        GMLFilterDocument documentFilter = new GMLFilterDocument(geometryFilter);

        LOGGER.fine("about to make parser");

        //XMLReader parser = XMLReaderFactory.createXMLReader(/*"org.apache.xerces.parsers.SAXParser"*/); 
        // uncomment to use xerces parser
        //parser.setContentHandler(documentFilter);
        //parser.parse(uri);
        SAXParserFactory fac = SAXParserFactory.newInstance();
        SAXParser parser = fac.newSAXParser();

        ParserAdapter p = new ParserAdapter(parser.getParser());
        p.setContentHandler(documentFilter);
        LOGGER.fine("just made parser, " + uri);
        p.parse(uri);
        LOGGER.fine("just parsed: " + uri);

        return filterHandler.getFilter();
    }
}
