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

import java.util.*;
import java.util.logging.Logger;
import junit.framework.*;
import com.vividsolutions.jts.geom.*;
import org.geotools.data.*;
import org.geotools.feature.*;


/**
 * Unit test for expressions.  This is a complimentary test suite with the
 * filter test suite.
 *
 * @author James MacGill, CCG
 * @author Rob Hranac, TOPP
 */                                
public class AreaFunctionTest extends TestCase {
    

      /** Standard logging instance */
    protected static final Logger LOGGER = Logger.getLogger(
            "org.geotools.filter");
    protected static AttributeTypeFactory attFactory = AttributeTypeFactory.newInstance();
    
    /** Feature on which to preform tests */
    private static Feature testFeature = null;

    /** Schema on which to preform tests */
    private static FeatureType testSchema = null;
    boolean setup = false;
    /** Test suite for this test case */
    TestSuite suite = null;


    /** 
     * Constructor with test name.
     */
    public AreaFunctionTest(String testName) {
        super(testName);
        //BasicConfigurator.configure();
    
        
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
       
        TestSuite suite = new TestSuite(AreaFunctionTest.class);
        return suite;
    }
    
    /** 
     * Sets up a schema and a test feature.
     * @throws SchemaException If there is a problem setting up the schema.
     * @throws IllegalFeatureException If problem setting up the feature.
     */
    protected void setUp() throws SchemaException, IllegalAttributeException {
        if (setup) {
            return;
        } else {
            prepareFeatures();
        }

        setup = true;
    }

    //HACK - this is cut and pasted from filter module tests.  Should be 
    //in a test support module.
    protected void prepareFeatures()
        throws SchemaException, IllegalAttributeException {
        //_log.getLoggerRepository().setThreshold(Level.INFO);
        // Create the schema attributes
        LOGGER.finer("creating flat feature...");

        AttributeType geometryAttribute = attFactory.newAttributeType("testGeometry",
                Polygon.class);
        LOGGER.finer("created geometry attribute");

        AttributeType booleanAttribute = attFactory.newAttributeType("testBoolean",
                Boolean.class);
        LOGGER.finer("created boolean attribute");

        AttributeType charAttribute = attFactory.newAttributeType("testCharacter",
                Character.class);
        AttributeType byteAttribute = attFactory.newAttributeType("testByte",
                Byte.class);
        AttributeType shortAttribute = attFactory.newAttributeType("testShort",
                Short.class);
        AttributeType intAttribute = attFactory.newAttributeType("testInteger",
                Integer.class);
        AttributeType longAttribute = attFactory.newAttributeType("testLong",
                Long.class);
        AttributeType floatAttribute = attFactory.newAttributeType("testFloat",
                Float.class);
        AttributeType doubleAttribute = attFactory.newAttributeType("testDouble",
                Double.class);
        AttributeType stringAttribute = attFactory.newAttributeType("testString",
                String.class);

        AttributeType[] types = {
            geometryAttribute, booleanAttribute, charAttribute, byteAttribute,
            shortAttribute, intAttribute, longAttribute, floatAttribute,
            doubleAttribute, stringAttribute
        };

        // Builds the schema
        testSchema = FeatureTypeFactory.newFeatureType(types,"testSchema");

        GeometryFactory geomFac = new GeometryFactory();

        // Creates coordinates for the linestring
        Coordinate[] coords = new Coordinate[5];
        coords[0] = new Coordinate(0, 0);
        coords[1] = new Coordinate(10, 0);
        coords[2] = new Coordinate(10, 10);
        coords[3] = new Coordinate(0, 10);
        coords[4] = new Coordinate(0, 0);

        // Builds the test feature
        Object[] attributes = new Object[10];
	LinearRing ring = new LinearRing(coords, new PrecisionModel(), 1);
        attributes[0] = new Polygon(ring, new PrecisionModel(), 1);
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
        testFeature = testSchema.create(attributes);
        LOGGER.finer("...flat feature created");

        //_log.getLoggerRepository().setThreshold(Level.DEBUG);
    }

    static FilterFactory filterFactory = FilterFactory.createFilterFactory();
     /** 
     * Tests the min function expression.
     */
    public void testAreaFunction()
        throws IllegalFilterException {
            
        Expression a = filterFactory.createAttributeExpression(testSchema, "testGeometry");         
        
        AreaFunction area = new AreaFunction();
        area.setArgs(new Expression[]{a});         
        assertEquals(100d,((Double)area.getValue(testFeature)).doubleValue(),0);
    }
    
   
    
}
