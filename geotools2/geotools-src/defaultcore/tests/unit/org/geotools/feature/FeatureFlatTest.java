package org.geotools.feature;

import junit.framework.*;
import com.vividsolutions.jts.geom.*;

import java.util.*;
import java.util.logging.Logger;

import org.geotools.resources.*;

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
        Geotools.init();
        junit.textui.TestRunner.run(suite());
    }

    public static Test suite() {
        TestSuite suite = new TestSuite(FeatureFlatTest.class);
        return suite;
    }
    
    public void setUp() {
        testFeature = SampleFeatureFixtures.createFeature();
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

    /*
     * This is actually a test for FeatureTypeFlat, but there is no test for that
     * written right now, so I'm just putting it here, as I just changed the
     * getDefaultGeometry method, and it should have a unit test.  It tests 
     * to make sure getDefaultGeometry returns null if there is no geometry,
     * as we now allow 
     */
    public void testGetDefaultGeometry() throws SchemaException {
	FeatureType testType = testFeature.getSchema();
	LOGGER.fine("testType = " + testType);
	AttributeType geometry = testType.getAttributeType("testGeometry");
	LOGGER.fine("geometry attr = " + geometry);
	assertTrue(geometry != null);
	testType = testType.removeAttributeType("testGeometry");
	LOGGER.fine("test Type after removing = " + testType);
	geometry = testType.getAttributeType("testGeometry");
	assertTrue(geometry == null);
    }
	    
}
