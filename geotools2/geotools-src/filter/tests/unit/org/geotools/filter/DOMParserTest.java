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
import org.geotools.gml.GMLFilterDocument;
import org.geotools.gml.GMLFilterGeometry;
import org.w3c.dom.*;
import java.io.*;
import java.util.*;
import java.util.logging.Logger;
import javax.xml.parsers.*;


/**
 * Tests for the DOM parser.
 *
 * @author James MacGill, CCG
 * @author Rob Hranac, TOPP
 * @author Chris Holmes, TOPP
 */
public class DOMParserTest extends FilterTestSupport {

    /** Feature on which to preform tests */
    private Filter filter = null;

    /** Test suite for this test case */
    TestSuite suite = null;

    /** Constructor with test name. */
    String dataFolder = "";
    boolean setup = false;

    public DOMParserTest(String testName) {
        super(testName);
        LOGGER.info("running DOMParserTests");
        System.out.println("running DOMParserTests");
        dataFolder = System.getProperty("dataFolder");

        if (dataFolder == null) {
            //then we are being run by maven
            dataFolder = System.getProperty("basedir");
            dataFolder = "file:////" + dataFolder + "/tests/unit/testData"; 
            LOGGER.fine("data folder is " + dataFolder);
        }
    }

    /**
     * Main for test runner.
     *
     * @param args the passed in arguments (not used).
     */
    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }

    public void setUp()  throws SchemaException, 
    IllegalAttributeException {
	super.setUp();
	FeatureTypeFactory feaTypeFactory = FeatureTypeFactory.createTemplate(testSchema);
	AttributeType doubleAttribute2 = attFactory.newAttributeType("testZeroDouble",
                Double.class);
	feaTypeFactory.addType(doubleAttribute2);
	testSchema = feaTypeFactory.getFeatureType();

	    GeometryFactory geomFac = new GeometryFactory();

        // Creates coordinates for the linestring
        Coordinate[] coords = new Coordinate[3];
        coords[0] = new Coordinate(1, 2);
        coords[1] = new Coordinate(3, 4);
        coords[2] = new Coordinate(5, 6);

        // Builds the test feature
        Object[] attributes = new Object[11];
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
	attributes[10] = new Double(0.0);
        // Creates the feature itself
        
        testFeature = testSchema.create(attributes);
    }

    /**
     * Required suite builder.
     *
     * @return A test suite for this unit test.
     */
    public static Test suite() {
        //_log.getLoggerRepository().setThreshold(Level.INFO);
        TestSuite suite = new TestSuite(DOMParserTest.class);

        return suite;
    }

    public void test1() throws Exception {
        Filter test = parseDocument(dataFolder + "/test1.xml");
        LOGGER.fine("parsed filter is " + test);
    }

    public void test2() throws Exception {
        Filter test = parseDocument(dataFolder + "/test2.xml");
        LOGGER.fine("parsed filter is " + test);
    }

    public void test3a() throws Exception {
        Filter test = parseDocument(dataFolder + "/test3a.xml");
        LOGGER.fine("parsed filter is " + test);
    }

    public void test3b() throws Exception {
        Filter test = parseDocument(dataFolder + "/test3b.xml");
        LOGGER.fine("parsed filter is " + test);
    }

    public void test4() throws Exception {
        Filter test = parseDocument(dataFolder + "/test4.xml");
        LOGGER.fine("parsed filter is " + test);
    }

    public void test8() throws Exception {
        Filter test = parseDocument(dataFolder + "/test8.xml");
        LOGGER.fine("parsed filter is " + test);
    }

    public void test9() throws Exception {
        Filter test = parseDocument(dataFolder + "/test9.xml");
        LOGGER.fine("parsed filter is " + test);
    }

    public void test11() throws Exception {
        Filter test = parseDocument(dataFolder + "/test11.xml");
        LOGGER.fine("parsed filter is " + test);
    }

    public void test12() throws Exception {
        Filter test = parseDocument(dataFolder + "/test12.xml");
        LOGGER.fine("parsed filter is " + test);
    }

    public void test13() throws Exception {
        Filter test = parseDocument(dataFolder + "/test13.xml");
        LOGGER.fine("parsed filter is " + test);
    }

    public void test14() throws Exception {
        Filter test = parseDocument(dataFolder + "/test14.xml");
        LOGGER.fine("parsed filter is " + test);
    }

    public void test15() throws Exception {
        Filter test = parseDocument(dataFolder + "/test15.xml");
        LOGGER.fine("parsed filter is " + test);
    }

    public void test16() throws Exception {
        Filter test = parseDocument(dataFolder + "/test16.xml");
        LOGGER.fine("parsed filter is " + test);
    }

    public void test27() throws Exception {
        Filter test = parseDocument(dataFolder + "/test27.xml");
        LOGGER.fine("parsed filter is " + test);
    }

	public void test28() throws Exception {
		FidFilter filter = (FidFilter)parseDocumentFirst(dataFolder + "/test28.xml");
		String[] fids = filter.getFids();
                List list = Arrays.asList(fids);
		assertEquals(3,fids.length);
                assertTrue(list.contains("FID.3"));
                assertTrue(list.contains("FID.2"));
                assertTrue(list.contains("FID.1"));
	}

    public Filter parseDocument(String uri) throws Exception {
        Filter filter = null;
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document dom = db.parse(uri);
        LOGGER.info("parsing " + uri);

        // first grab a filter node
        NodeList nodes = dom.getElementsByTagName("Filter");

        for (int j = 0; j < nodes.getLength(); j++) {
            Element filterNode = (Element) nodes.item(j);
            NodeList list = filterNode.getChildNodes();
            Node child = null;

            for (int i = 0; i < list.getLength(); i++) {
                child = list.item(i);

                if ((child == null) ||
                        (child.getNodeType() != Node.ELEMENT_NODE)) {
                    continue;
                }

                filter = FilterDOMParser.parseFilter(child);
                assertNotNull("Null filter returned", filter);
                LOGGER.finer("filter: " + filter.getClass().toString());
                LOGGER.info("parsed: " + filter.toString());
                LOGGER.finer("result " + filter.contains(testFeature));
            }
        }

        return filter;
    }

    public Filter parseDocumentFirst(String uri) throws Exception {
	        Filter filter = null;
	        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
	        DocumentBuilder db = dbf.newDocumentBuilder();
	        Document dom = db.parse(uri);
	        LOGGER.info("parsing " + uri);

	        // first grab a filter node
	        NodeList nodes = dom.getElementsByTagName("Filter");

	        for (int j = 0; j < nodes.getLength(); j++) {
	            Element filterNode = (Element) nodes.item(j);
	            NodeList list = filterNode.getChildNodes();
	            Node child = null;

	            for (int i = 0; i < list.getLength(); i++) {
	                child = list.item(i);

	                if ((child == null) ||
	                        (child.getNodeType() != Node.ELEMENT_NODE)) {
	                    continue;
	                }

	                filter = FilterDOMParser.parseFilter(child);
	                assertNotNull("Null filter returned", filter);
	                LOGGER.finer("filter: " + filter.getClass().toString());
	                LOGGER.info("parsed: " + filter.toString());
	                LOGGER.finer("result " + filter.contains(testFeature));
	                return filter;
	            }
	        }

	        return filter;
    }
}
