package org.geotools.feature;

import junit.framework.*;
import com.vividsolutions.jts.geom.*;

import java.util.*;
import java.util.logging.Logger;

public class FeatureFlatTest extends TestCase {

    /**
     * The logger for the default core module.
     */
    private static final Logger LOGGER = Logger.getLogger("org.geotools.defaultcore");

    /** Feature on which to preform tests */
    private Feature testFeature = null;

    TestSuite suite = null;

    public FeatureFlatTest(String testName){
        super(testName);
    }

    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }

    public static Test suite() {
        TestSuite suite = new TestSuite(FeatureFlatTest.class);
        return suite;
    }
    
    public void setUp() {
        try {
            LOGGER.fine("creating flat feature...");
            AttributeType geometryAttribute = new AttributeTypeDefault("testGeometry", Point.class);
            LOGGER.fine("created geometry attribute");
            AttributeType booleanAttribute = new AttributeTypeDefault("testBoolean", Boolean.class);
            LOGGER.fine("created boolean attribute");
            AttributeType charAttribute = new AttributeTypeDefault("testCharacter", Character.class);
            AttributeType byteAttribute = new AttributeTypeDefault("testByte", Byte.class);
            AttributeType shortAttribute = new AttributeTypeDefault("testShort", Short.class);
            AttributeType intAttribute = new AttributeTypeDefault("testInteger", Integer.class);
            AttributeType longAttribute = new AttributeTypeDefault("testLong", Long.class);
            AttributeType floatAttribute = new AttributeTypeDefault("testFloat", Float.class);
            AttributeType doubleAttribute = new AttributeTypeDefault("testDouble", Double.class);
            AttributeType stringAttribute = new AttributeTypeDefault("testString", String.class);

            FeatureType testType = new FeatureTypeFlat(geometryAttribute); 
            LOGGER.fine("created feature type and added geometry");
            testType = testType.setAttributeType(booleanAttribute);
            LOGGER.fine("added boolean to feature type");
            testType = testType.setAttributeType(charAttribute);
            LOGGER.fine("added character to feature type");
            testType = testType.setAttributeType(byteAttribute);
            LOGGER.fine("added byte to feature type");
            testType = testType.setAttributeType(shortAttribute);
            LOGGER.fine("added short to feature type");
            testType = testType.setAttributeType(intAttribute);
            LOGGER.fine("added int to feature type");
            testType = testType.setAttributeType(longAttribute);
            LOGGER.fine("added long to feature type");
            testType = testType.setAttributeType(floatAttribute);
            LOGGER.fine("added float to feature type");
            testType = testType.setAttributeType(doubleAttribute);
            LOGGER.fine("added double to feature type");
            testType = testType.setAttributeType(stringAttribute);
            LOGGER.fine("added string to feature type");

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
            LOGGER.fine("...flat feature created");
        }
        catch(SchemaException e) {
            LOGGER.fine("Test failed during schema creation: ");
            LOGGER.fine(e.getMessage());
        }
        catch(IllegalFeatureException e) {
            LOGGER.fine("Test failed during feature creation: ");
            LOGGER.fine(e.getMessage());
        }
    }

    public void testRetrieve() {
        try {
            LOGGER.fine("starting retrieval tests...");
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
            LOGGER.fine("...ending retrieval tests");
        }
        catch(IllegalFeatureException e) {
            LOGGER.fine("Feature threw exception: ");
            LOGGER.fine(e.getMessage());
        }
    }

    public void testModify() {
        try {
            LOGGER.fine("starting attribute modification tests...");
            
            testFeature.setAttribute("testString", "new test string data");
            assertEquals("match modified (string) attribute",
                         testFeature.getAttribute("testString"),
                         "new test string data");

            testFeature.setAttribute("testGeometry", 
                                     new Point(new Coordinate(3,4), new PrecisionModel(), 1));
            assertTrue("match modified (geometry) attribute",
                       ((Point) testFeature.getAttribute("testGeometry")).
                       equals(new Point(new Coordinate(3,4), new PrecisionModel(), 1)));

            LOGGER.fine("...ending attribute modification tests");
        }
        catch(IllegalFeatureException e) {
            LOGGER.fine("Feature threw exception: ");
            LOGGER.fine(e.getMessage());
        }
    }


    public void testEnforceType() {
        LOGGER.fine("starting type enforcement tests...");
        LOGGER.fine("...ending type enforcement tests");
    }

    
}
