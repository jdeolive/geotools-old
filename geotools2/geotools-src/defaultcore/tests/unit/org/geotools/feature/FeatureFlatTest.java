package org.geotools.feature;

import junit.framework.*;
import org.apache.log4j.Category;
import org.apache.log4j.BasicConfigurator;
import com.vividsolutions.jts.geom.*;
import java.util.*;

public class FeatureFlatTest extends TestCase {

    /** Standard logging instance */
    private static Category _log = Category.getInstance(FeatureFlatTest.class.getName());

    /** Feature on which to preform tests */
    private Feature testFeature = null;

    TestSuite suite = null;

    public FeatureFlatTest(String testName){
        super(testName);
    }

    public static void main(String[] args) {
        BasicConfigurator.configure();
        junit.textui.TestRunner.run(suite());
    }

    public static Test suite() {
        BasicConfigurator.configure();
        TestSuite suite = new TestSuite(FeatureFlatTest.class);
        return suite;
    }
    
    public void setUp() {
        try {
            _log.info("creating flat feature...");
            AttributeType geometryAttribute = new AttributeTypeDefault("testGeometry", Point.class.getName());
            AttributeType booleanAttribute = new AttributeTypeDefault("testBoolean", Boolean.class.getName());
            AttributeType charAttribute = new AttributeTypeDefault("testCharacter", Character.class.getName());
            AttributeType byteAttribute = new AttributeTypeDefault("testByte", Byte.class.getName());
            AttributeType shortAttribute = new AttributeTypeDefault("testShort", Short.class.getName());
            AttributeType intAttribute = new AttributeTypeDefault("testInteger", Integer.class.getName());
            AttributeType longAttribute = new AttributeTypeDefault("testLong", Long.class.getName());
            AttributeType floatAttribute = new AttributeTypeDefault("testFloat", Float.class.getName());
            AttributeType doubleAttribute = new AttributeTypeDefault("testDouble", Double.class.getName());
            AttributeType stringAttribute = new AttributeTypeDefault("testString", String.class.getName());

            FeatureType testType = new FeatureTypeFlat(geometryAttribute); 
            _log.info("created feature type and added geometry");
            testType = testType.setAttributeType(booleanAttribute);
            _log.info("added boolean to feature type");
            testType = testType.setAttributeType(charAttribute);
            _log.info("added character to feature type");
            testType = testType.setAttributeType(byteAttribute);
            _log.info("added byte to feature type");
            testType = testType.setAttributeType(shortAttribute);
            _log.info("added short to feature type");
            testType = testType.setAttributeType(intAttribute);
            _log.info("added int to feature type");
            testType = testType.setAttributeType(longAttribute);
            _log.info("added long to feature type");
            testType = testType.setAttributeType(floatAttribute);
            _log.info("added float to feature type");
            testType = testType.setAttributeType(doubleAttribute);
            _log.info("added double to feature type");
            testType = testType.setAttributeType(stringAttribute);
            _log.info("added string to feature type");

            Object[] attributes = new Object[10];
            attributes[0] = new Point(new Coordinate(1,2), new PrecisionModel(), 1);
            attributes[1] = new Boolean(true);
            attributes[2] = new Character('t');
            attributes[3] = new Byte("10");
            attributes[4] = new Short("101");
            attributes[5] = new Integer(1002);
            attributes[6] = new Long(10003);
            attributes[7] = new Float(10000.4);
            attributes[8] = new Double(100000.5);
            attributes[9] = "test string data";
            testFeature = new FeatureFlat((FeatureTypeFlat) testType, attributes);
            _log.info("...flat feature created");
        }
        catch(SchemaException e) {
            _log.info("Test failed during schema creation: ");
            _log.info(e.getMessage());
        }
        catch(IllegalFeatureException e) {
            _log.info("Test failed during feature creation: ");
            _log.info(e.getMessage());
        }
    }

    public void testRetrieve() {
        try {
            _log.info("starting retrieval tests...");
            assertTrue("geometry retrieval and match",
                       ((Point) testFeature.getAttribute("testGeometry")).
                       equals(new Point(new Coordinate(1,2), new PrecisionModel(), 1)));
            assertTrue("boolean retrieval and match",
                       ((Boolean) testFeature.getAttribute("testBoolean")).
                       equals(new Boolean(true)));
            assertTrue("character retrieval and match",
                       ((Character) testFeature.getAttribute("testCharacter")).
                       equals(new Character('t')));
            assertTrue("byte retrieval and match",
                       ((Byte) testFeature.getAttribute("testByte")).
                       equals(new Byte("10")));
            assertTrue("short retrieval and match",
                       ((Short) testFeature.getAttribute("testShort")).
                       equals(new Short("101")));
            assertTrue("integer retrieval and match",
                       ((Integer) testFeature.getAttribute("testInteger")).
                       equals(new Integer(1002)));
            assertTrue("long retrieval and match",
                       ((Long) testFeature.getAttribute("testLong")).
                        equals(new Long(10003)));
            assertTrue("float retrieval and match",
                       ((Float) testFeature.getAttribute("testFloat")).
                        equals(new Float(10000.4)));
            assertTrue("double retrieval and match",
                       ((Double) testFeature.getAttribute("testDouble")).
                        equals(new Double(100000.5)));
            assertTrue("string retrieval and match",
                       ((String) testFeature.getAttribute("testString")).
                        equals("test string data"));
            _log.info("...ending retrieval tests");
        }
        catch(IllegalFeatureException e) {
            _log.info("Feature threw exception: ");
            _log.info(e.getMessage());
        }
    }

    public void testModify() {
        try {
            _log.info("starting attribute modification tests...");
            
            testFeature.setAttribute("testString", "new test string data");
            assertEquals("match modified (string) attribute",
                         testFeature.getAttribute("testString"),
                         "new test string data");

            testFeature.setAttribute("testGeometry", 
                                     new Point(new Coordinate(3,4), new PrecisionModel(), 1));
            assertTrue("match modified (geometry) attribute",
                       ((Point) testFeature.getAttribute("testGeometry")).
                       equals(new Point(new Coordinate(3,4), new PrecisionModel(), 1)));

            _log.info("...ending attribute modification tests");
        }
        catch(IllegalFeatureException e) {
            _log.info("Feature threw exception: ");
            _log.info(e.getMessage());
        }
    }


    public void testEnforceType() {
        _log.info("starting type enforcement tests...");
        _log.info("...ending type enforcement tests");
    }

    
}

