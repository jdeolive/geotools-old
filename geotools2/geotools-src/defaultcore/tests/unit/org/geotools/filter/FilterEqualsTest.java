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
import junit.framework.*;
import com.vividsolutions.jts.geom.*;
import org.geotools.data.*;
import org.geotools.feature.*;

import java.util.*;
import java.util.logging.Logger;

/**
 * Unit test for testing filters equals method.
 *
 * @author James MacGill, CCG
 * @author Rob Hranac, TOPP
 * @author Chris Holmes, TOPP
 */            
public class FilterEqualsTest extends TestCase {
    
    /** Standard logging instance */
    private static final Logger LOGGER = Logger.getLogger("org.geotools.defaultcore");

    private Expression testExp1;
    private Expression testExp2;
    private Expression testExp3;
    private Expression testExp4;
    private Filter tFilter1;
    private Filter tFilter2;

  /** Feature on which to preform tests */
    private static Feature testFeature = null;

    /** Schema on which to preform tests */
    private static FeatureType testSchema = null;
    boolean set = false;
    /** 
     * Constructor with test name.
     */
    public FilterEqualsTest(String testName) {
        super(testName);
    }        
    
    /** 
     * Main for test runner.
     */
    public static void main(String[] args) {
        org.geotools.resources.Geotools.init();
        junit.textui.TestRunner.run(suite());
    }
    
    /** 
     * Required suite builder.
     * @return A test suite for this unit test.
     */
    public static Test suite() {
        
        TestSuite suite = new TestSuite(FilterEqualsTest.class);
        return suite;
    }
    
    /** 
     * Sets up a schema and a test feature.
     * @throws SchemaException If there is a problem setting up the schema.
     * @throws IllegalFeatureException If problem setting up the feature.
     */
    protected void setUp() 
 throws SchemaException, IllegalFeatureException {
        if(set) return;
        set = true;
        // Create the schema attributes
        LOGGER.fine("creating flat feature...");
        AttributeType geometryAttribute = 
            new AttributeTypeDefault("testGeometry", LineString.class);
        LOGGER.fine("created geometry attribute");
        AttributeType booleanAttribute = 
            new AttributeTypeDefault("testBoolean", Boolean.class);
        LOGGER.fine("created boolean attribute");
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
        AttributeType stringAttribute2 = 
            new AttributeTypeDefault("testString2", String.class);
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
        testSchema = testSchema.setAttributeType(stringAttribute);
        testSchema = testSchema.setAttributeType(stringAttribute2);
        LOGGER.fine("added string to feature type");
        
        // Creates coordinates for the linestring
        Coordinate[] coords = new Coordinate[3];
        coords[0] = new Coordinate(1,2);
        coords[1] = new Coordinate(3,4);
        coords[2] = new Coordinate(5,6);
        
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
        FeatureFactory factory = new FeatureFactory(testSchema);
        testFeature = factory.create(attributes);
        LOGGER.fine("...flat feature created");
    

    }

    public void testExpressionLiteral(){
	try {
	    testExp1 = new ExpressionLiteral("test literal");
	    testExp2 = new ExpressionLiteral("test literal");
	    assertTrue(testExp1.equals(testExp2));
	    testExp2 = new ExpressionLiteral("not test literal");
	    assertTrue(!testExp1.equals(testExp2));
	    testExp3 = new ExpressionLiteral(new Integer(34));
	    assertTrue(!testExp1.equals(testExp3));
	    testExp1 = new ExpressionLiteral(new Integer(34));
	    assertTrue(testExp1.equals(testExp3));
	}  catch (IllegalFilterException e) {
	    LOGGER.warning("bad filter " + e.getMessage());
	}
    }   

    public void testExpressionMath(){
	try {
	    ExpressionMath testMath1;
	    ExpressionMath testMath2;
	    testExp1 = new ExpressionLiteral(new Double(5));
	    testExp2 = new ExpressionLiteral(new Double(5));
	    testMath1 = new ExpressionMath(ExpressionDefault.MATH_ADD);
	    testMath1.addLeftValue(testExp1);
	    testMath1.addRightValue(testExp2);
	    testMath2 =  new ExpressionMath(ExpressionDefault.MATH_ADD);
	    testMath2.addLeftValue(testExp2);
	    testMath2.addRightValue(testExp1);
	    assertTrue(testMath1.equals(testMath2));
	    testExp3 = new ExpressionLiteral(new Integer(4));
	    testExp4 = new ExpressionLiteral(new Integer(4));
	    testMath2.addLeftValue(testExp3);
	    assertTrue(!testMath1.equals(testMath2));
	    testMath1.addLeftValue(testExp4);
	    assertTrue(testMath1.equals(testMath2));
	    testMath1 = new ExpressionMath(ExpressionDefault.MATH_SUBTRACT);
	    testMath1.addLeftValue(testExp4);
	    testMath1.addLeftValue(testExp2);
	    assertTrue(!testMath1.equals(testMath2));
	} catch (IllegalFilterException e){
	    LOGGER.warning("bad filter: " + e.getMessage());
	}
    }

    public void testExpressionAttribute()
	throws IllegalFilterException, SchemaException {
	AttributeType[] testAttr2= { 
	    new AttributeTypeDefault("testBoolean", Boolean.class),
	    new AttributeTypeDefault("testString", String.class)
		};
	FeatureType testSchema2 = new FeatureTypeFlat(testAttr2); 
	testExp1 = new ExpressionAttribute(testSchema, "testBoolean");
	testExp2 = new ExpressionAttribute(testSchema, "testBoolean");
	assertTrue(testExp1.equals(testExp2));
	testExp3 = new ExpressionAttribute(testSchema, "testString");
	assertTrue(!testExp1.equals(testExp3));
	testExp4 = new ExpressionAttribute(testSchema2, "testBoolean");
	assertTrue(!testExp1.equals(testExp4));
	testExp1 = new ExpressionAttribute(testSchema2, "testBoolean");
	assertTrue(testExp1.equals(testExp4));
		   
    }

    public void testCompareFilter()
	throws IllegalFilterException {
	CompareFilter cFilter1 = new CompareFilter(AbstractFilter.COMPARE_EQUALS);
	CompareFilter cFilter2 = new CompareFilter(AbstractFilter.COMPARE_EQUALS);
	testExp1 = new ExpressionLiteral(new Integer(45));
	testExp2 = new ExpressionLiteral(new Integer(45));
	testExp3 = new ExpressionAttribute(testSchema, "testInteger");
	testExp4 = new ExpressionAttribute(testSchema, "testInteger");
	cFilter1.addLeftValue(testExp1);
	cFilter2.addLeftValue(testExp1);
	cFilter1.addRightValue(testExp3);
	cFilter2.addRightValue(testExp3);
	assertTrue(cFilter1.equals(cFilter2));
	cFilter2.addLeftValue(testExp2);
	cFilter2.addRightValue(testExp4);
	assertTrue(cFilter1.equals(cFilter2));
	cFilter2.addRightValue(new ExpressionLiteral(new Double(45)));
	assertTrue(!cFilter1.equals(cFilter2));
	tFilter1 = new BetweenFilter();
	assertTrue(!cFilter1.equals(tFilter1));
    }	
    
    public void testBetweenFilter()
	throws IllegalFilterException {
	BetweenFilter bFilter1 = new BetweenFilter();
	BetweenFilter bFilter2 = new BetweenFilter();
	ExpressionLiteral testLit1 = new ExpressionLiteral(new Integer(55));
	ExpressionLiteral testLit2 = new ExpressionLiteral(new Integer(55));
	testExp1 = new ExpressionLiteral(new Integer(45));
	testExp2 = new ExpressionLiteral(new Integer(45));
	testExp3 = new ExpressionAttribute(testSchema, "testInteger");
	testExp4 = new ExpressionAttribute(testSchema, "testInteger");
	bFilter1.addLeftValue(testExp1);
	bFilter2.addLeftValue(testExp2);
	bFilter1.addMiddleValue(testExp3);
	bFilter2.addMiddleValue(testExp4);
	bFilter1.addRightValue(testLit1);
	bFilter2.addRightValue(testLit2);
	assertTrue(bFilter2.equals(bFilter1));
	tFilter1 = new CompareFilter(AbstractFilter.COMPARE_EQUALS);
	assertTrue(!bFilter2.equals(tFilter1));
	bFilter2.addRightValue(new ExpressionLiteral(new Integer(65)));
	assertTrue(!bFilter2.equals(bFilter1));
    }
    
     public void testLikeFilter()
	throws IllegalFilterException {
	 LikeFilter lFilter1 = new LikeFilter();
	 LikeFilter lFilter2 = new LikeFilter();
	 String pattern = "te_st!";
	 String wcMulti = "!";
	 String wcSingle = "_";
	 String escape = "#";
	 testExp2 = new ExpressionLiteral(new Integer(45));
	testExp3 = new ExpressionAttribute(testSchema, "testInteger");
	testExp4 = new ExpressionAttribute(testSchema, "testInteger");
	lFilter1.setValue(testExp3);
	lFilter2.setValue(testExp4);
	lFilter1.setPattern(pattern, wcMulti, wcSingle, escape);
	lFilter2.setPattern(pattern, wcMulti, wcSingle, escape);
	assertTrue(lFilter1.equals(lFilter2));
	lFilter2.setPattern("te__t!", wcMulti, wcSingle, escape);
	assertTrue(!lFilter1.equals(lFilter2));
	lFilter2.setPattern(pattern, wcMulti, wcSingle, escape);
	lFilter2.setValue(testExp2);
	assertTrue(!lFilter1.equals(lFilter2));
    }	

    public void testLogicFilter()
	throws IllegalFilterException{
	CompareFilter cFilter1 = new CompareFilter(AbstractFilter.COMPARE_EQUALS);
	CompareFilter cFilter2 = new CompareFilter(AbstractFilter.COMPARE_EQUALS);
	testExp1 = new ExpressionLiteral(new Integer(45));
	testExp2 = new ExpressionLiteral(new Integer(45));
	testExp3 = new ExpressionAttribute(testSchema, "testInteger");
	testExp4 = new ExpressionAttribute(testSchema, "testInteger");
	cFilter1.addLeftValue(testExp1);
	cFilter2.addLeftValue(testExp2);
	cFilter1.addRightValue(testExp2);
	cFilter2.addRightValue(testExp4);
	LogicFilter logFilter1 = new LogicFilter(cFilter1, cFilter2, AbstractFilter.LOGIC_AND);
	LogicFilter logFilter2 = new LogicFilter(cFilter1, cFilter2, AbstractFilter.LOGIC_AND);
	assertTrue(logFilter1.equals(logFilter2));
	
	logFilter1 = new LogicFilter(cFilter2, AbstractFilter.LOGIC_NOT);
	assertTrue(!logFilter1.equals(logFilter2));
	cFilter1.addRightValue(testExp3);
	logFilter2 = new LogicFilter(cFilter1, AbstractFilter.LOGIC_NOT);
	assertTrue(logFilter1.equals(logFilter2));
        assertTrue(!logFilter1.equals(new BetweenFilter()));
	Filter logFilter3 = new LogicFilter(logFilter1, logFilter2, AbstractFilter.LOGIC_OR);
	Filter logFilter4 = new LogicFilter(logFilter1, logFilter2, AbstractFilter.LOGIC_OR);
	assertTrue(logFilter3.equals(logFilter4));

	//Questionable behavior.  Is this what we want?
	Filter logFilter5 = cFilter1.or(logFilter3);
	//does not change structure of logFilter3
	Filter logFilter6 = logFilter4.or(cFilter1);
	//does change structure of logFilter4
	assertTrue(!logFilter5.equals(logFilter6));//do we want these equal? 
	//the order of ORs is different, but the effect the same.
	assertTrue(!logFilter4.equals(logFilter3));//shouldn't they be equal?
	//need to change implementation of LogicFilter's ands & ors, so that
	//they return a new filter, instead of changing the internal structure,
	//or else change the abstract filter's ors & ands to detect when adding
	//to a Logic filter, and then add it to the correct subfilter.

    }

    public void testNullFilter()
	throws IllegalFilterException{
	    testExp1 = new ExpressionAttribute(testSchema, "testDouble");
	    testExp2 = new ExpressionAttribute(testSchema, "testDouble");
	    testExp3 = new  ExpressionAttribute(testSchema, "testBoolean");
	    NullFilter nullFilter1 = new NullFilter();
	    NullFilter nullFilter2 = new NullFilter();
	    nullFilter1.nullCheckValue(testExp1);
	    nullFilter2.nullCheckValue(testExp2);
	    assertTrue(nullFilter1.equals(nullFilter2));
	    nullFilter1.nullCheckValue(testExp3);
	    assertTrue(!nullFilter1.equals(nullFilter2));
	    assertTrue(!nullFilter1.equals(new BetweenFilter()));
	}

     public void testGeometryFilter()
	throws IllegalFilterException {
	GeometryFilter geomFilter1 = new GeometryFilter(AbstractFilter.GEOMETRY_DISJOINT);
	GeometryFilter geomFilter2 = new GeometryFilter(AbstractFilter.GEOMETRY_DISJOINT);
	testExp1 = new ExpressionLiteral(new Integer(45));
	testExp2 = new ExpressionLiteral(new Integer(45));
	testExp3 = new ExpressionAttribute(testSchema, "testGeometry");
	testExp4 = new ExpressionAttribute(testSchema, "testGeometry");
	geomFilter1.addLeftGeometry(testExp1);
	geomFilter2.addLeftGeometry(testExp2);
	geomFilter1.addRightGeometry(testExp3);
	geomFilter2.addRightGeometry(testExp4);
	assertTrue(geomFilter1.equals(geomFilter2));
	geomFilter2.addRightGeometry(new ExpressionLiteral(new Double(45)));
	assertTrue(!geomFilter1.equals(geomFilter2));
	tFilter1 = new BetweenFilter();
	assertTrue(!geomFilter1.equals(tFilter1));
    }	
    
}
