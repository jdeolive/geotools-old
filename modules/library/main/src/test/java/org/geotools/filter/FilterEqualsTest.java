/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002-2006, Geotools Project Managment Committee (PMC)
 *    (C) 2002, Centre for Computational Geography
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
package org.geotools.filter;

import java.util.logging.Logger;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.geotools.feature.IllegalAttributeException;
import org.geotools.feature.SchemaException;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.filter.expression.AddImpl;
import org.geotools.filter.expression.SubtractImpl;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.PrecisionModel;

/**
 * Unit test for testing filters equals method.
 *
 * @author Chris Holmes, TOPP
 * @author James MacGill, CCG
 * @author Rob Hranac, TOPP
 * @source $URL$
 */            
public class FilterEqualsTest extends TestCase {
    
    /** Standard logging instance */
    private static final Logger LOGGER = org.geotools.util.logging.Logging.getLogger("org.geotools.defaultcore");

    private Expression testExp1;
    private Expression testExp2;
    private Expression testExp3;
    private Expression testExp4;
    private Filter tFilter1;
    private Filter tFilter2;

  /** Feature on which to preform tests */
    private static SimpleFeature testFeature = null;

    /** Schema on which to preform tests */
    private static SimpleFeatureType testSchema = null;
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
        org.geotools.util.logging.Logging.GEOTOOLS.forceMonolineConsoleOutput();
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
     *
     * @throws SchemaException If there is a problem setting up the schema.
     * @throws IllegalAttributeException If problem setting up the feature.
     */
    protected void setUp() throws SchemaException, IllegalAttributeException {
        if (set) {
            return;
        }

        set = true;
        
        SimpleFeatureTypeBuilder ftb = new SimpleFeatureTypeBuilder();
    	ftb.add("testGeometry", LineString.class);
    	ftb.add("testBoolean", Boolean.class);
    	ftb.add("testCharacter", Character.class);
    	ftb.add("testByte", Byte.class);
    	ftb.add("testShort", Short.class);
    	ftb.add("testInteger", Integer.class);
    	ftb.add("testLong", Long.class);
    	ftb.add("testFloat", Float.class);
    	ftb.add("testDouble", Double.class);
    	ftb.add("testString", String.class);
    	ftb.add("testZeroDouble", Double.class);
    	ftb.setName("testSchema");
        testSchema = ftb.buildFeatureType();

        //LOGGER.finer("added string to feature type");
        // Creates coordinates for the linestring
        Coordinate[] coords = new Coordinate[3];
        coords[0] = new Coordinate(1, 2);
        coords[1] = new Coordinate(3, 4);
        coords[2] = new Coordinate(5, 6);

        // Builds the test feature
        Object[] attributes = new Object[11];
        GeometryFactory gf = new GeometryFactory(new PrecisionModel());
        attributes[0] = gf.createLineString(coords);
        attributes[1] = new Boolean(true);
        attributes[2] = new Character('t');
        attributes[3] = new Byte("10");
        attributes[4] = new Short("101");
        attributes[5] = new Integer(1002);
        attributes[6] = new Long(10003);
        attributes[7] = new Float(10000.4);
        attributes[8] = new Double(100000.5);
        attributes[9] = "test string data";
        attributes[10] = "0.0";

        // Creates the feature itself
        //FlatFeatureFactory factory = new FlatFeatureFactory(testSchema);
        testFeature = SimpleFeatureBuilder.build(testSchema, attributes, null);
        //LOGGER.finer("...flat feature created");
    }

    public void testLiteralExpressionImplEquals(){
    	try {
    	    Expression testString1 = new LiteralExpressionImpl("test literal");
            Expression testString2 = new LiteralExpressionImpl("test literal");
    	    assertTrue(testString1.equals(testString2));
            
            Expression testOtherString = new LiteralExpressionImpl("not test literal");
    	    assertFalse( testString1.equals(testOtherString) );
            
    	    Expression testNumber34 = new LiteralExpressionImpl(new Integer(34));
    	    assertFalse( testString1.equals(testNumber34));
            
            Expression testOtherNumber34 = new LiteralExpressionImpl(new Integer(34));	    
    	    assertTrue(testNumber34.equals(testOtherNumber34));                  
    	}  catch (IllegalFilterException e) {
    	    LOGGER.warning("bad filter " + e.getMessage());
    	}
    }
    
    

    public void testFidFilter(){
        FidFilter ff = new FidFilterImpl();
        ff.addFid("1");
        
        FidFilter ff2 = new FidFilterImpl("1");
        assertNotNull(ff2);
        assertEquals(ff, ff2);
        assertTrue(!ff.equals(null));
        assertTrue(!ff.equals("a string not even a filter"));
        ff2.addFid("2");
        assertTrue(!ff.equals(ff2));
        
        
        
        ff.addFid("2");
        assertEquals(ff, ff2);
        
        FidFilterImpl ff3 = new FidFilterImpl();
        ff3.filterType = -1;//REVISIT: should I even be able to do that?
        
        assertTrue(!ff2.equals(ff3));
        
    }
    
    public void testExpressionMath(){
        try {
	    MathExpressionImpl testMath1;
	    MathExpressionImpl testMath2;
	    testExp1 = new LiteralExpressionImpl(new Double(5));
	    testExp2 = new LiteralExpressionImpl(new Double(5));
	    testMath1 = new AddImpl(null,null);
	    testMath1.addLeftValue(testExp1);
	    testMath1.addRightValue(testExp2);
	    testMath2 =  new AddImpl(null,null);
	    testMath2.addLeftValue(testExp2);
	    testMath2.addRightValue(testExp1);
	    assertTrue(testMath1.equals(testMath2));
	    testExp3 = new LiteralExpressionImpl(new Integer(4));
	    testExp4 = new LiteralExpressionImpl(new Integer(4));
	    testMath2.addLeftValue(testExp3);
	    assertTrue(!testMath1.equals(testMath2));
	    testMath1.addLeftValue(testExp4);
	    assertTrue(testMath1.equals(testMath2));
	    testMath1 = new SubtractImpl(null,null);
	    testMath1.addLeftValue(testExp4);
	    testMath1.addLeftValue(testExp2);
	    assertTrue(!testMath1.equals(testMath2));
            assertTrue(!testMath1.equals("Random Object that happens to be a string"));
	} catch (IllegalFilterException e){
	    LOGGER.warning("bad filter: " + e.getMessage());
	}
            
    }

    public void testExpressionAttribute()
	throws IllegalFilterException, SchemaException {
    	SimpleFeatureTypeBuilder ftb = new SimpleFeatureTypeBuilder();
    	ftb.add("testBoolean", Boolean.class);
    	ftb.add("testString", String.class);
    	ftb.setName("test2");
	
	SimpleFeatureType testSchema2 = ftb.buildFeatureType();
	//FeatureType testSchema2 = feaTypeFactory.getFeatureType(); 
	testExp1 = new AttributeExpressionImpl(testSchema, "testBoolean");
	testExp2 = new AttributeExpressionImpl(testSchema, "testBoolean");
	assertTrue(testExp1.equals(testExp2));
	testExp3 = new AttributeExpressionImpl(testSchema, "testString");
	assertTrue(!testExp1.equals(testExp3));

	
	testExp4 = new AttributeExpressionImpl(testSchema2, "testBoolean");
	assertTrue(!testExp1.equals(testExp4));
 
	testExp1 = new AttributeExpressionImpl(testSchema2, "testBoolean");
	assertTrue(testExp1.equals(testExp4));
		   
    }

    public void testCompareFilter()
	throws IllegalFilterException {
    	FilterFactory factory = FilterFactoryFinder.createFilterFactory();
	CompareFilter cFilter1 = factory.createCompareFilter(FilterType.COMPARE_EQUALS);
	CompareFilter cFilter2 = factory.createCompareFilter(FilterType.COMPARE_EQUALS);
	testExp1 = new LiteralExpressionImpl(new Integer(45));
	testExp2 = new LiteralExpressionImpl(new Integer(45));
	testExp3 = new AttributeExpressionImpl(testSchema, "testInteger");
	testExp4 = new AttributeExpressionImpl(testSchema, "testInteger");
	cFilter1.addLeftValue(testExp1);
	cFilter2.addLeftValue(testExp1);
	cFilter1.addRightValue(testExp3);
	cFilter2.addRightValue(testExp3);
	assertTrue(cFilter1.equals(cFilter2));
	cFilter2.addLeftValue(testExp2);
	cFilter2.addRightValue(testExp4);
	assertTrue(cFilter1.equals(cFilter2));
	cFilter2.addRightValue(new LiteralExpressionImpl(new Double(45)));
	assertTrue(!cFilter1.equals(cFilter2));
	tFilter1 = new BetweenFilterImpl();
	assertTrue(!cFilter1.equals(tFilter1));
    }	
    
    public void testBetweenFilter()
	throws IllegalFilterException {
	BetweenFilterImpl bFilter1 = new BetweenFilterImpl();
	BetweenFilterImpl bFilter2 = new BetweenFilterImpl();
	LiteralExpressionImpl testLit1 = new LiteralExpressionImpl(new Integer(55));
	LiteralExpressionImpl testLit2 = new LiteralExpressionImpl(new Integer(55));
	testExp1 = new LiteralExpressionImpl(new Integer(45));
	testExp2 = new LiteralExpressionImpl(new Integer(45));
	testExp3 = new AttributeExpressionImpl(testSchema, "testInteger");
	testExp4 = new AttributeExpressionImpl(testSchema, "testInteger");
	bFilter1.addLeftValue(testExp1);
	bFilter2.addLeftValue(testExp2);
	bFilter1.addMiddleValue(testExp3);
	bFilter2.addMiddleValue(testExp4);
	bFilter1.addRightValue(testLit1);
	bFilter2.addRightValue(testLit2);
	assertTrue(bFilter2.equals(bFilter1));
	tFilter1 = FilterFactoryFinder.createFilterFactory()
		.createCompareFilter(FilterType.COMPARE_EQUALS);
	assertTrue(!bFilter2.equals(tFilter1));
	bFilter2.addRightValue(new LiteralExpressionImpl(new Integer(65)));
	assertTrue(!bFilter2.equals(bFilter1));
    }
    
     public void testLikeFilter()
	throws IllegalFilterException {
	 LikeFilterImpl lFilter1 = new LikeFilterImpl();
	 LikeFilterImpl lFilter2 = new LikeFilterImpl();
	 String pattern = "te_st!";
	 String wcMulti = "!";
	 String wcSingle = "_";
	 String escape = "#";
	 testExp2 = new LiteralExpressionImpl(new Integer(45));
	testExp3 = new AttributeExpressionImpl(testSchema, "testInteger");
	testExp4 = new AttributeExpressionImpl(testSchema, "testInteger");
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
    FilterFactory factory = FilterFactoryFinder.createFilterFactory();
	CompareFilter cFilter1 = factory.createCompareFilter(FilterType.COMPARE_EQUALS);
	CompareFilter cFilter2 = factory.createCompareFilter(FilterType.COMPARE_EQUALS);
	testExp1 = new LiteralExpressionImpl(new Integer(45));
	testExp2 = new LiteralExpressionImpl(new Integer(45));
	testExp3 = new AttributeExpressionImpl(testSchema, "testInteger");
	testExp4 = new AttributeExpressionImpl(testSchema, "testInteger");
	cFilter1.addLeftValue(testExp1);
	cFilter2.addLeftValue(testExp2);
	cFilter1.addRightValue(testExp2);
	cFilter2.addRightValue(testExp4);
	
	LogicFilter logFilter1 = factory.createLogicFilter(cFilter1,cFilter2,AbstractFilter.LOGIC_AND);
	LogicFilter logFilter2 = factory.createLogicFilter(cFilter1,cFilter2,AbstractFilter.LOGIC_AND);
	assertTrue(logFilter1.equals(logFilter2));
	
	logFilter1 = factory.createLogicFilter(cFilter2, AbstractFilter.LOGIC_NOT);
	assertTrue(!logFilter1.equals(logFilter2));
	cFilter1.addRightValue(testExp3);
	logFilter2 = factory.createLogicFilter(cFilter1, AbstractFilter.LOGIC_NOT);
	assertTrue(logFilter1.equals(logFilter2));
        assertTrue(!logFilter1.equals(new BetweenFilterImpl()));
	Filter logFilter3 = factory.createLogicFilter(logFilter1, logFilter2, AbstractFilter.LOGIC_OR);
	Filter logFilter4 = factory.createLogicFilter(logFilter1, logFilter2, AbstractFilter.LOGIC_OR);
	assertTrue(logFilter3.equals(logFilter4));

	//Questionable behavior.  Is this what we want?
	Filter logFilter5 = (org.geotools.filter.Filter)  cFilter1.or(logFilter3);
	//does not change structure of logFilter3
	Filter logFilter6 = (org.geotools.filter.Filter)  logFilter4.or(cFilter1);
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
	    testExp1 = new AttributeExpressionImpl(testSchema, "testDouble");
	    testExp2 = new AttributeExpressionImpl(testSchema, "testDouble");
	    testExp3 = new  AttributeExpressionImpl(testSchema, "testBoolean");
	    NullFilterImpl nullFilter1 = new NullFilterImpl();
	    NullFilterImpl nullFilter2 = new NullFilterImpl();
	    nullFilter1.nullCheckValue(testExp1);
	    nullFilter2.nullCheckValue(testExp2);
	    assertTrue(nullFilter1.equals(nullFilter2));
	    nullFilter1.nullCheckValue(testExp3);
	    assertTrue(!nullFilter1.equals(nullFilter2));
	    assertTrue(!nullFilter1.equals(new BetweenFilterImpl()));
	}

     public void testGeometryFilter()
	throws IllegalFilterException {
    	 FilterFactory factory = FilterFactoryFinder.createFilterFactory();
	GeometryFilter geomFilter1 = factory.createGeometryFilter(AbstractFilter.GEOMETRY_DISJOINT);
	GeometryFilter geomFilter2 = factory.createGeometryFilter(AbstractFilter.GEOMETRY_DISJOINT);
	testExp1 = new LiteralExpressionImpl(new Integer(45));
	testExp2 = new LiteralExpressionImpl(new Integer(45));
	testExp3 = new AttributeExpressionImpl(testSchema, "testGeometry");
	testExp4 = new AttributeExpressionImpl(testSchema, "testGeometry");
	geomFilter1.addLeftGeometry(testExp1);
	geomFilter2.addLeftGeometry(testExp2);
	geomFilter1.addRightGeometry(testExp3);
	geomFilter2.addRightGeometry(testExp4);
	assertTrue(geomFilter1.equals(geomFilter2));
	geomFilter2.addRightGeometry(new LiteralExpressionImpl(new Double(45)));
	assertTrue(!geomFilter1.equals(geomFilter2));
	tFilter1 = new BetweenFilterImpl();
	assertTrue(!geomFilter1.equals(tFilter1));
    }	
    
}
