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

import java.io.*;
import java.util.*;
import javax.xml.parsers.*;
import org.w3c.dom.*;
import org.apache.log4j.Level;
import org.apache.log4j.Hierarchy;
import org.apache.log4j.Logger;
import org.apache.log4j.BasicConfigurator;
import junit.framework.*;
import com.vividsolutions.jts.geom.*;
import org.geotools.datasource.extents.*;
import org.geotools.feature.*;
import org.geotools.data.*;
import org.geotools.gml.GMLFilterGeometry;
import org.geotools.gml.GMLFilterDocument;


/**
 * Unit test for expressions.  This is a complimentary test suite with the
 * filter test suite.
 *
 * @author James MacGill, CCG
 * @author Rob Hranac, TOPP
 */
public class DOMParserTest extends TestCase {
    
    /** Standard logging instance */
    private static Logger _log = Logger.getLogger(DOMParserTest.class);
    
    /** Feature on which to preform tests */
    private Filter filter = null;
    
    /** Schema on which to preform tests */
    private static FeatureType testSchema = null;
    
    /** Schema on which to preform tests */
    private static Feature testFeature = null;
    
    /** Test suite for this test case */
    TestSuite suite = null;
    
    
    /**
     * Constructor with test name.
     */
    public DOMParserTest(String testName) {
        super(testName);
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
        BasicConfigurator.configure();
        _log.getLoggerRepository().setThreshold(Level.DEBUG);
        TestSuite suite = new TestSuite(DOMParserTest.class);
        return suite;
    }
    
    
    /**
     * Sets up a schema and a test feature.
     * @throws SchemaException If there is a problem setting up the schema.
     * @throws IllegalFeatureException If problem setting up the feature.
     */
    protected void setUp()
    throws SchemaException, IllegalFeatureException {
        
        // Create the schema attributes
        _log.debug("creating flat feature...");
        AttributeType geometryAttribute =
        new AttributeTypeDefault("testGeometry", LineString.class);
        _log.debug("created geometry attribute");
        AttributeType booleanAttribute =
        new AttributeTypeDefault("testBoolean", Boolean.class);
        _log.debug("created boolean attribute");
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
        _log.debug("created feature type and added geometry");
        testSchema = testSchema.setAttributeType(booleanAttribute);
        _log.debug("added boolean to feature type");
        testSchema = testSchema.setAttributeType(charAttribute);
        _log.debug("added character to feature type");
        testSchema = testSchema.setAttributeType(byteAttribute);
        _log.debug("added byte to feature type");
        testSchema = testSchema.setAttributeType(shortAttribute);
        _log.debug("added short to feature type");
        testSchema = testSchema.setAttributeType(intAttribute);
        _log.debug("added int to feature type");
        testSchema = testSchema.setAttributeType(longAttribute);
        _log.debug("added long to feature type");
        testSchema = testSchema.setAttributeType(floatAttribute);
        _log.debug("added float to feature type");
        testSchema = testSchema.setAttributeType(doubleAttribute);
        _log.debug("added double to feature type");
        testSchema = testSchema.setAttributeType(stringAttribute);
        _log.debug("added string to feature type");
        
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
        FeatureFactory factory = new FeatureFactory(testSchema);
        testFeature = factory.create(attributes);
        _log.debug("...flat feature created");
    }
    
    public void testParser() throws Exception {
        parseDocument(System.getProperty("dataFolder")+"/iantest.xml");

    }
    
    /*
    public void test2()
        throws Exception {
        Filter test = parseDocument("/home/rob/wfs/geotools/geotools2/geotools-src/filter/tests/unit/data/test2.xml");
        _log.debug("filter: " + test.getClass().toString());
        _log.info("parsed: " + test.toString());
    }
     
    public void test3a()
        throws Exception {
        Filter test = parseDocument("/home/rob/wfs/geotools/geotools2/geotools-src/filter/tests/unit/data/test3a.xml");
        _log.debug("filter: " + test.getClass().toString());
        _log.info("parsed: " + test.toString());
    }
     
    public void test3b()
        throws Exception {
        Filter test = parseDocument("/home/rob/wfs/geotools/geotools2/geotools-src/filter/tests/unit/data/test3b.xml");
        _log.debug("filter: " + test.getClass().toString());
        _log.info("parsed: " + test.toString());
    }
     
    public void test4()
        throws Exception {
        Filter test = parseDocument("/home/rob/wfs/geotools/geotools2/geotools-src/filter/tests/unit/data/test4.xml");
        _log.debug("filter: " + test.getClass().toString());
        _log.info("parsed: " + test.toString());
    }
     
     */
    public void parseDocument(String uri) throws Exception {
        Filter filter = null;
        
        
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document dom = db.parse(uri);
        
        _log.debug("about to parse: " + uri);
        // first grab a filter node
        NodeList nodes = dom.getElementsByTagName("Filter");
        for(int j=0;j<nodes.getLength();j++){
            Element filterNode = (Element)nodes.item(j);
            NodeList list = filterNode.getChildNodes();
            Node child = null;
            for(int i=0;i<list.getLength();i++){
                child = list.item(i);
                if(child == null || child.getNodeType() != Node.ELEMENT_NODE) continue;
                filter = FilterXMLParser.parseFilter(child);
                _log.debug("filter: " + filter.getClass().toString());
                _log.info("parsed: " + filter.toString());
                _log.debug("result "+filter.contains(testFeature));
            }
        }
        
        _log.debug("just parsed: " + uri);
        
        return;
    }
    
    
    public void filter(Filter filter) {
        this.filter = filter;
    }
    
}
