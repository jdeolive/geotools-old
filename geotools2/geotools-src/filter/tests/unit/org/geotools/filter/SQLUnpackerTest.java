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
 * Unit test for SQLUnpacker.  
 *
 * @author Chris Holmes, TOPP
 */
public class SQLUnpackerTest extends TestCase {
    
    /** Standard logging instance */
    private static Logger _log = Logger.getLogger("filter");
    
    /** Filters on which to perform tests*/
    private BetweenFilter btwnFilter;
    private CompareFilter compFilter;
    private GeometryFilter geomFilter;
    private LikeFilter likeFilter;
    private NullFilter nullFilter;
    private ExpressionAttribute attrExp1;
    private ExpressionAttribute attrExp2;
    private ExpressionLiteral litExp1;
    private ExpressionLiteral litExp2;
    private ExpressionMath mathExp1;

    /** strings for Like filter */
    private String pattern = "te_st!";
    private String wcMulti = "!";
    private String wcSingle = "_";
    private String escape = "#";
    
    /** Schema on which to preform tests */
    private static FeatureType testSchema = null;
    
    /** Schema on which to preform tests */
    private static Feature testFeature = null;

    /** capabilities for unpacker */
    private FilterCapabilities capabilities;

    //    private SQLEncoder encoder;

    private SQLUnpacker unpacker;
    
    /** Test suite for this test case */
    TestSuite suite = null;
    boolean setup = false;    
    
    /**
     * Constructor with test name.
     */

    public SQLUnpackerTest(String testName) {
        super(testName);
        _log.info("running SQLUnpackerTests");
        
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
        
        TestSuite suite = new TestSuite(SQLUnpackerTest.class);
	return suite;
    }
    
    
    /**
     * Sets up a schema and a test feature.
     * @throws SchemaException If there is a problem setting up the schema.
     * @throws IllegalFeatureException If problem setting up the feature.
     */
    protected void setUp() throws SchemaException, IllegalFilterException {
        if(setup) return;
        setup=true;
        _log.getLoggerRepository().setThreshold(Level.INFO);
	//Set capabilities for the SQLUnpacker
	capabilities = new FilterCapabilities();
	capabilities.addType(AbstractFilter.LOGIC_OR);
	capabilities.addType(AbstractFilter.LOGIC_AND);
	capabilities.addType(AbstractFilter.LOGIC_NOT);
	capabilities.addType(AbstractFilter.COMPARE_EQUALS);
	capabilities.addType(AbstractFilter.COMPARE_LESS_THAN);
	capabilities.addType(AbstractFilter.COMPARE_GREATER_THAN);
	capabilities.addType(AbstractFilter.COMPARE_LESS_THAN_EQUAL);
	capabilities.addType(AbstractFilter.COMPARE_GREATER_THAN_EQUAL);
	capabilities.addType(AbstractFilter.NULL);
	capabilities.addType(AbstractFilter.BETWEEN);	
	unpacker = new SQLUnpacker(capabilities);


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
        
	attrExp1 = new ExpressionAttribute(testSchema, "testInteger");
	attrExp2 = new ExpressionAttribute(testSchema, "testGeometry");
	litExp1 = new ExpressionLiteral(new Integer(65));
	litExp2 = new ExpressionLiteral(new Integer(35));
	mathExp1 = new ExpressionMath(ExpressionDefault.MATH_ADD);
	mathExp1.addLeftValue(litExp1);
	mathExp1.addRightValue(litExp2);

	btwnFilter = new BetweenFilter();
	btwnFilter.addLeftValue(litExp1);
	btwnFilter.addMiddleValue(attrExp1);
	btwnFilter.addRightValue(mathExp1);

	compFilter = new CompareFilter(AbstractFilter.COMPARE_LESS_THAN);
	compFilter.addLeftValue(attrExp1);
	compFilter.addRightValue(litExp2);

	geomFilter = new GeometryFilter(AbstractFilter.GEOMETRY_TOUCHES);
	geomFilter.addLeftGeometry(attrExp2);
	geomFilter.addRightGeometry(litExp2);

	likeFilter = new LikeFilter();
	likeFilter.setValue(attrExp1);
	likeFilter.setPattern(pattern, wcMulti, wcSingle, escape);

	nullFilter = new NullFilter();
	nullFilter.nullCheckValue(attrExp2);

    

	 //    testFeature = factory.create(attributes);
        _log.debug("...set up complete");
        _log.getLoggerRepository().setThreshold(Level.DEBUG);
    }
    

    
    public void testBasics() 
    throws IllegalFilterException {
	unpacker.unPack(btwnFilter);
	//Unsupported should be null, supported btwnFilter
	assertNull(unpacker.getUnSupported());
	assertTrue(btwnFilter.equals(unpacker.getSupported()));
	
	unpacker.unPack(compFilter);
	//null, compFilter
	assertNull(unpacker.getUnSupported());
	assertTrue(compFilter.equals(unpacker.getSupported()));

	unpacker.unPack(geomFilter);
	//Unsupported should be geomFilter, supported null
	assertTrue(geomFilter.equals(unpacker.getUnSupported()));
	assertNull(unpacker.getSupported());

	unpacker.unPack(likeFilter);
	//likeFilter, null
	assertTrue(likeFilter.equals(unpacker.getUnSupported()));
	assertNull(unpacker.getSupported());

	unpacker.unPack(nullFilter);
	//null, nullFilter
	assertNull(unpacker.getUnSupported());
	assertTrue(nullFilter.equals(unpacker.getSupported()));


    }    

    public void testAnd()
	throws IllegalFilterException{

    //I will use the notation (Unsupported, Supported) to indicate
    //the filters that should result, that we are testing against.
	Filter andFilter = btwnFilter.and(compFilter);
	unpacker.unPack(andFilter);
	//both supported (null, andFilter)
	assertNull(unpacker.getUnSupported());
	assertTrue(andFilter.equals(unpacker.getSupported()));

	andFilter = likeFilter.and(compFilter);
	unpacker.unPack(andFilter);
	//Comp supported, Like not: (likeFilter, compFilter)
	assertTrue(likeFilter.equals(unpacker.getUnSupported()));
	assertTrue(compFilter.equals(unpacker.getSupported()));

	andFilter = likeFilter.and(geomFilter);
	unpacker.unPack(andFilter);
	//both unsupported (andFilter, null)
	assertTrue(andFilter.equals(unpacker.getUnSupported()));
	assertNull(unpacker.getSupported());
    }
		
    public void testNot()
	throws IllegalFilterException{
	Filter notFilter = nullFilter.not();
	unpacker.unPack(notFilter);
	//nullFilters supported (null, nullFilter)
	assertNull(unpacker.getUnSupported());
	assertTrue(notFilter.equals(unpacker.getSupported()));

	notFilter = geomFilter.not();
	unpacker.unPack(notFilter);
	//geomFilters not supported (geomFilter, null);
	assertTrue(notFilter.equals(unpacker.getUnSupported()));
	assertNull(unpacker.getSupported());
    }

    public void testOr()
	throws IllegalFilterException{
	Filter orFilter = btwnFilter.or(compFilter);
	unpacker.unPack(orFilter);
	//both supported (null, orFilter)
	assertNull(unpacker.getUnSupported());
	assertTrue(orFilter.equals(unpacker.getSupported()));

	orFilter = likeFilter.or(compFilter);
	unpacker.unPack(orFilter);
	//both unsupported: (orFilter, null)  
	assertTrue(orFilter.equals(unpacker.getUnSupported()));
	assertNull(unpacker.getSupported());

	orFilter = likeFilter.and(geomFilter);
	unpacker.unPack(orFilter);
	//both unsupported (orFilter, null)
	assertTrue(orFilter.equals(unpacker.getUnSupported()));
	assertNull(unpacker.getSupported());
	
    }

    public void testComplex()
	throws IllegalFilterException{
	Filter orFilter = likeFilter.or(btwnFilter);
	Filter andFilter = orFilter.and(compFilter);
	unpacker.unPack(andFilter);
	//compFilter supported, none of orFilter: (orFilter, compFilter)
	assertTrue(orFilter.equals(unpacker.getUnSupported()));
	assertTrue(compFilter.equals(unpacker.getSupported()));
	
	Filter bigOrFilter = (andFilter.or(compFilter)).or(nullFilter);
	Filter biggerOrFilter = bigOrFilter.or(nullFilter);
	//Top level or with one unsupported (like from orFilter)
	//makes all unsupported: (bigOrFilter, null)
	unpacker.unPack(biggerOrFilter);
	assertTrue(biggerOrFilter.equals(unpacker.getUnSupported()));
	assertNull(unpacker.getSupported());

	Filter bigAndFilter = (geomFilter.and(compFilter)).and(orFilter);
	//comp supported, orFilter not, geomFilter not:
	//(orFilter AND geomFilter, compFilter)
	unpacker.unPack(bigAndFilter);
	assertTrue((orFilter.and(geomFilter)).equals(unpacker.getUnSupported()));
	assertTrue(compFilter.equals(unpacker.getSupported()));

	Filter hugeAndFilter = (bigAndFilter.and(andFilter)).and(bigOrFilter);
	unpacker.unPack(hugeAndFilter);
	//two comps should be supported; geom, or and bigOr unsupported
	//(compFilter AND compFilter, geomFilter AND orFilter AND orFilter AND bigOrFilter)
	assertTrue((compFilter.and(compFilter)).equals(unpacker.getSupported()));
	assertTrue((((geomFilter.and(orFilter)).and(orFilter)).and(bigOrFilter))
		   .equals(unpacker.getUnSupported()));
    }

}
