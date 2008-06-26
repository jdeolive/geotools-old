/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 * 
 *    (C) 2002-2008, Open Source Geospatial Foundation (OSGeo)
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
 */
package org.geotools.filter.text.cql2;

import junit.framework.TestCase;

import org.opengis.filter.Filter;
import org.opengis.filter.PropertyIsBetween;
import org.opengis.filter.PropertyIsEqualTo;
import org.opengis.filter.PropertyIsLike;
import org.opengis.filter.expression.Add;
import org.opengis.filter.expression.Expression;
import org.opengis.filter.expression.Literal;
import org.opengis.filter.expression.PropertyName;
import org.opengis.filter.spatial.BBOX;
import org.opengis.filter.spatial.Beyond;
import org.opengis.filter.spatial.BinarySpatialOperator;
import org.opengis.filter.spatial.Contains;
import org.opengis.filter.spatial.Crosses;
import org.opengis.filter.spatial.DWithin;
import org.opengis.filter.spatial.Disjoint;
import org.opengis.filter.spatial.DistanceBufferOperator;
import org.opengis.filter.spatial.Equals;
import org.opengis.filter.spatial.Intersects;
import org.opengis.filter.spatial.Overlaps;
import org.opengis.filter.spatial.Touches;
import org.opengis.filter.spatial.Within;

import com.vividsolutions.jts.geom.Point;


/**
 * CQL Test
 * 
 * <p>
 * Test Common CQL language
 * </p>
 *
 * @author Mauricio Pazos (Axios Engineering)
 * @since 2.5 
 */
public class CQLTest extends TestCase {

    public CQLTest(String testName) {
        super(testName);
    }


    /**
     * Test Existence Predicate.
     * <p>
     * EXIST: evaluates as true for all record instances where the
     * attribute_name is a member of the record schema. DOES-NOT-EXIST: opposite
     * to EXISTS
     * </p>
     * <p>
     *
     * <pre>
     *  &lt;existence_predicate &gt; ::=
     *          &lt;attribute_name &gt; EXISTS
     *      |   &lt;attribute_name &gt; DOES-NOT-EXIST
     * </pre>
     *
     * </p>
     */
    public void testExistencePredicate() throws Exception {
        Filter resultFilter;
        Filter expected;
        PropertyIsEqualTo eqToResultFilter;

        // -------------------------------------------------------------
        // <attribute_name> DOES-NOT-EXIST
        // -------------------------------------------------------------
        resultFilter = CQL.toFilter(FilterSample.ATTRIBUTE_NAME_DOES_NOT_EXIST);

        assertNotNull("Filter expected", resultFilter);

        expected = FilterSample.getSample(FilterSample.ATTRIBUTE_NAME_DOES_NOT_EXIST);

        assertEquals(expected, resultFilter);

        // -------------------------------------------------------------
        // <attribute_name> EXISTS
        // TODO Exist function must be implemented in Geotools
        // -------------------------------------------------------------
        resultFilter = CQL.toFilter(FilterSample.ATTRIBUTE_NAME_EXISTS);

        assertNotNull("Filter expected", resultFilter);

        assertTrue(resultFilter instanceof PropertyIsEqualTo);

        eqToResultFilter = (PropertyIsEqualTo) resultFilter;

        expected = FilterSample.getSample(FilterSample.ATTRIBUTE_NAME_EXISTS);

        assertEquals(expected, eqToResultFilter);

        assertNotNull("implementation of function was expected", eqToResultFilter.getExpression1());

    }

    /**
     * Test Null Predicate:
     * <p>
     *
     * <pre>
     * &lt;null predicate &gt; ::=  &lt;attribute name &gt; IS [ NOT ] NULL
     * </pre>
     *
     * </p>
     */
    public void testNullPredicate() throws Exception {
        Filter expected;
        Filter resultFilter;
        // -------------------------------------------------------------
        // ATTR1 IS NULL
        // -------------------------------------------------------------
        expected = FilterSample.getSample(FilterSample.PROPERTY_IS_NULL);
        resultFilter = CQL.toFilter(FilterSample.PROPERTY_IS_NULL);

        assertNotNull("Filter expected", resultFilter);

        assertEquals("PropertyIsNull filter was expected", resultFilter, expected);

        // -------------------------------------------------------------
        // ATTR1 IS NOT NULL
        // -------------------------------------------------------------
        expected = FilterSample.getSample(FilterSample.PROPERTY_IS_NOT_NULL);
        resultFilter = CQL.toFilter(FilterSample.PROPERTY_IS_NOT_NULL);

        assertNotNull("Filter expected", resultFilter);

        assertEquals("Not PropertyIsNull filter was expected", resultFilter, expected);
    }

    public void testParenRoundtripExpression() throws Exception {
        // ATTR1 > ((1 + 2) / 3)
        testEqualsExpressions(FilterSample.FILTER_WITH_PAREN_ROUNDTRIP_EXPR);

        // "ATTR1 < (1 + ((2 / 3) * 4))"
        testEqualsExpressions(FilterSample.FILTER_WITH_NESTED_PAREN_EXPR);
    }

    public void testBracketRoundtripFilter() throws Exception {
        // ATTR1 > [[1 + 2] / 3]
        testEqualsExpressions(FilterSample.FILTER_WITH_BRACKET_ROUNDTRIP_EXPR);

        // TODO more test
        // roundtripFilter("[[[ 3 < 4 ] AND NOT [ 2 < 4 ]] AND [ 5 < 4 ]]");
        // roundtripFilter("[3<4 AND 2<4 ] OR 5<4");
        // roundtripFilter("3<4 && 2<4");
    }

    /**
     * Test Between Predicate.
     * <p>
     *
     * <pre>
     *  This cql clause is an extension for convenience.
     *  &lt;between predicate &gt; ::=
     *  &lt;attribute name &gt; [ NOT ] BETWEEN  &lt;literal&amp; #62; AND  &lt; literal  &gt;
     * </pre>
     *
     * </p>
     */
    public void testBetweenPredicate() throws Exception {
        Filter resultFilter;
        Filter expected;

        // between
        resultFilter = CQL.toFilter(FilterSample.BETWEEN_FILTER);

        assertNotNull("Filter expected", resultFilter);

        expected = FilterSample.getSample(FilterSample.BETWEEN_FILTER);

        assertEquals("Between filter was expected", expected, resultFilter);

        // not between
        resultFilter = CQL.toFilter(FilterSample.NOT_BETWEEN_FILTER);

        assertNotNull("Filter expected", resultFilter);

        expected = FilterSample.getSample(FilterSample.NOT_BETWEEN_FILTER);

        assertEquals("Between filter was expected", expected, resultFilter);

        // test compound attribute gmd:aa:bb.gmd:cc.gmd:dd
        final String prop = "gmd:aa:bb.gmd:cc.gmd:dd";
        final String propExpected = "gmd:aa:bb/gmd:cc/gmd:dd";
        resultFilter = CQL.toFilter(prop + " BETWEEN 100 AND 200 ");

        assertTrue("PropertyIsBetween filter was expected",
            resultFilter instanceof PropertyIsBetween);

        PropertyIsBetween filter = (PropertyIsBetween) resultFilter;
        Expression property = filter.getExpression();

        assertEquals(propExpected, property.toString());
    }

    /**
     * Test Attribute
     * <p>
     *
     * <pre>
     *  &lt;attribute name &gt; ::=
     *          &lt;simple attribute name &gt;
     *      |    &lt;compound attribute name &gt;
     *  &lt;simple attribute name &gt; ::=  &lt;identifier &gt;
     *  &lt;compound attribute name &gt; ::=  &lt;identifier &gt; &lt;period &gt; [{ &lt;identifier &gt; &lt;period &gt;}...] &lt;simple attribute name &gt;
     *  &lt;identifier &gt; ::=  &lt;identifier start [ {  &lt;colon &gt; |  &lt;identifier part &gt; }... ]
     *  &lt;identifier start &gt; ::=  &lt;simple Latin letter &gt;
     *  &lt;identifier part &gt; ::=  &lt;simple Latin letter &gt; |  &lt;digit &gt;
     * </pre>
     *
     * </p>
     */
    public void testAttribute() throws Exception {
        // Simple attribute name
        testAttribute("startPart");

        testAttribute("startpart:part1:part2");

        // Compound attribute name
        testAttribute("s11:p12:p13.s21:p22.s31:p32");

        testAttribute(
            "gmd:MD_Metadata.gmd:identificationInfo.gmd:MD_DataIdentification.gmd:abstract");
    }

    private void testAttribute(final String attSample) throws Exception {
        PropertyIsLike result;
        PropertyName attResult = null;

        String expected = attSample.replace('.', '/');

        result = (PropertyIsLike) CQL.toFilter(attSample + " LIKE 'abc%'");

        attResult = (PropertyName) result.getExpression();

        assertEquals(expected, attResult.getPropertyName());
    }

    /**
     * Test boolean value expressions.
     * <p>
     *
     * <pre>
     *  &lt;boolean value expression &gt; ::=
     *          &lt;boolean term &gt;
     *      |   &lt;boolean value expression &gt; OR  &lt;boolean term &gt;
     *  &lt;boolean term &gt; ::=
     *          &lt;boolean factor &gt;
     *      |   &lt;boolean term &gt; AND  &lt;boolean factor&gt;
     * </pre>
     *
     * </p>
     */
    public void testBooleanValueExpression() throws Exception {
        Filter result;
        Filter expected;

        // ATTR1 < 10 AND ATTR2 < 2
        result = CQL.toFilter(FilterSample.FILTER_AND);

        assertNotNull("filter expected", result);

        expected = FilterSample.getSample(FilterSample.FILTER_AND);

        assertEquals("ATTR1 < 10 AND ATTR2 < 2 was expected", expected, result);

        // "ATTR1 > 10 OR ATTR2 < 2"
        result = CQL.toFilter(FilterSample.FILTER_OR);

        assertNotNull("filter expected", result);

        expected = FilterSample.getSample(FilterSample.FILTER_OR);

        assertEquals("ATTR1 > 10 OR ATTR2 < 2 was expected", expected, result);

        // ATTR1 < 10 AND ATTR2 < 2 OR ATTR3 > 10
        result = CQL.toFilter(FilterSample.FILTER_OR_AND);

        assertNotNull("filter expected", result);

        expected = FilterSample.getSample(FilterSample.FILTER_OR_AND);

        assertEquals("(ATTR1 < 10 AND ATTR2 < 2 OR ATTR3 > 10) was expected", expected, result);

        // ATTR3 < 4 AND (ATT1 > 10 OR ATT2 < 2)
        result = CQL.toFilter(FilterSample.FILTER_OR_AND_PARENTHESIS);

        assertNotNull("filter expected", result);

        expected = FilterSample.getSample(FilterSample.FILTER_OR_AND_PARENTHESIS);

        assertEquals("ATTR3 < 4 AND (ATT1 > 10 OR ATT2 < 2) was expected", expected, result);

        // ATTR3 < 4 AND (NOT( ATTR1 < 10 AND ATTR2 < 2))
        result = CQL.toFilter(FilterSample.FILTER_AND_NOT_AND);

        assertNotNull("filter expected", result);

        expected = FilterSample.getSample(FilterSample.FILTER_AND_NOT_AND);

        assertEquals("ATTR3 < 4 AND (NOT( ATTR1 < 10 AND ATTR2 < 2)) was expected", expected, result);

        // "ATTR1 < 1 AND (NOT (ATTR2 < 2)) AND ATTR3 < 3"
        result = CQL.toFilter(FilterSample.FILTER_AND_NOT_COMPARASION);

        assertNotNull("filter expected", result);

        expected = FilterSample.getSample(FilterSample.FILTER_AND_NOT_COMPARASION);

        assertEquals("ATTR1 < 4 AND (NOT (ATTR2 < 4)) AND ATTR3 < 4 was expected", expected, result);
    }


    /**
     * Test Geo Operations.
     * <p>
     *
     * <pre>
     *   &lt;routine invocation &gt; ::=
     *           &lt;geoop name &gt; &lt;georoutine argument list &gt;[*]
     *       |   &lt;relgeoop name &gt; &lt;relgeoop argument list &gt;
     *       |   &lt;routine name &gt; &lt;argument list &gt;
     *   &lt;geoop name &gt; ::=
     *           EQUAL | DISJOINT | INTERSECT | TOUCH | CROSS | [*]
     *           WITHIN | CONTAINS |OVERLAP | RELATE [*]
     *   That rule is extended with bbox for convenience.
     *   &lt;bbox argument list &gt;::=
     *       &quot;(&quot;  &lt;attribute &gt; &quot;,&quot; &lt;min X &gt; &quot;,&quot; &lt;min Y &gt; &quot;,&quot; &lt;max X &gt; &quot;,&quot; &lt;max Y &gt;[&quot;,&quot;  &lt;srs &gt;] &quot;)&quot;
     *       &lt;min X &gt; ::=  &lt;signed numerical literal &gt;
     *       &lt;min Y &gt; ::=  &lt;signed numerical literal &gt;
     *       &lt;max X &gt; ::=  &lt;signed numerical literal &gt;
     *       &lt;max Y &gt; ::=  &lt;signed numerical literal &gt;
     *       &lt;srs &gt; ::=
     * </pre>
     *
     * </p>
     * TODO Note: RELATE is not supported (implementation in GeoTools is
     * required)
     */
    public void testRoutineInvocationGeoOp() throws Exception {
        Filter resultFilter;

        resultFilter = CQL.toFilter("DISJOINT(ATTR1, POINT(1 2))");

        assertTrue("Disjoint was expected", resultFilter instanceof Disjoint);

        resultFilter = CQL.toFilter("INTERSECT(ATTR1, POINT(1 2))");

        assertTrue("Intersects was expected", resultFilter instanceof Intersects);

        resultFilter = CQL.toFilter("TOUCH(ATTR1, POINT(1 2))");

        assertTrue("Touches was expected", resultFilter instanceof Touches);

        resultFilter = CQL.toFilter("CROSS(ATTR1, POINT(1 2))");

        assertTrue("Crosses was expected", resultFilter instanceof Crosses);

        resultFilter = CQL.toFilter("CONTAINS(ATTR1, POINT(1 2))");

        assertTrue("Contains was expected", resultFilter instanceof Contains);

        resultFilter = CQL.toFilter("OVERLAP(ATTR1, POINT(1 2))");

        assertTrue("Overlaps was expected", resultFilter instanceof Overlaps);

        // BBOX
        resultFilter = CQL.toFilter("BBOX(ATTR1, 10.0,20.0,30.0,40.0)");
        assertTrue("BBox was expected", resultFilter instanceof BBOX);
        BBOX bboxFilter = (BBOX) resultFilter;
        assertEquals(bboxFilter.getMinX(), 10.0);
        assertEquals(bboxFilter.getMinY(), 20.0);
        assertEquals(bboxFilter.getMaxX(), 30.0);
        assertEquals(bboxFilter.getMaxY(), 40.0);
        assertEquals(null, bboxFilter.getSRS());

        resultFilter = CQL.toFilter("BBOX(ATTR1, 10.0,20.0,30.0,40.0, 'EPSG:4326')");
        assertTrue("BBox was expected", resultFilter instanceof BBOX);
        bboxFilter = (BBOX) resultFilter;
        assertEquals("EPSG:4326", bboxFilter.getSRS());
        
        // EQUALS
        resultFilter = CQL.toFilter("EQUAL(ATTR1, POINT(1 2))");

        assertTrue("not an instance of Equals", resultFilter instanceof Equals);

        resultFilter = CQL.toFilter("WITHIN(ATTR1, POLYGON((1 2, 1 10, 5 10, 1 2)) )");

        assertTrue("Within was expected", resultFilter instanceof Within);
    }

    /**
     * Test RelGeo Operations [*]
     * <p>
     *
     * <pre>
     *   &lt;routine invocation &gt; ::=
     *       &lt;geoop name &gt; &lt;georoutine argument list &gt;
     *   |   &lt;relgeoop name &gt; &lt;relgeoop argument list &gt; [*]
     *   |  &lt;routine name &gt; &lt;argument list &gt;
     *   &lt;relgeoop name &gt; ::=
     *       DWITHIN | BEYOND [*]
     * </pre>
     *
     * </p>
     */
    public void testRoutineInvocationRelGeoOp() throws Exception {
        Filter resultFilter;

        // DWITHIN
        resultFilter = CQL.toFilter("DWITHIN(ATTR1, POINT(1 2), 10, kilometers)");

        assertTrue(resultFilter instanceof DistanceBufferOperator);

        // test compound attribute gmd:aa:bb.gmd:cc.gmd:dd
        final String prop = "gmd:aa:bb.gmd:cc.gmd:dd";
        final String propExpected = "gmd:aa:bb/gmd:cc/gmd:dd";
        resultFilter = CQL.toFilter("DWITHIN(" + prop + ", POINT(1 2), 10, kilometers) ");

        assertTrue("DistanceBufferOperator filter was expected",
            resultFilter instanceof DWithin);

        DistanceBufferOperator filter = (DWithin) resultFilter;
        Expression property = filter.getExpression1();

        assertEquals(propExpected, property.toString());
        
        // Beyond
        resultFilter = CQL.toFilter("BEYOND(ATTR1, POINT(1.0 2.0), 10.0, kilometers)");
        assertTrue(resultFilter instanceof Beyond);
        Beyond beyondFilter  = (Beyond) resultFilter;
        
        assertEquals(beyondFilter.getDistance(), 10.0);
        assertEquals(beyondFilter.getDistanceUnits(), "kilometers");
        assertEquals(beyondFilter.getExpression1().toString(), "ATTR1");
        
        Expression geomExpression = beyondFilter.getExpression2();
        assertTrue(geomExpression instanceof Literal);
        Literal literalPoint = (Literal) geomExpression;
        
        Object pointValue = literalPoint.getValue();
        assertTrue(pointValue instanceof Point);
        Point point = (Point) pointValue;
        assertEquals(point.getX(),1.0);
        assertEquals(point.getY(),2.0);

        // syntax error test (POINTS must be POINT)
        try{
            resultFilter = CQL.toFilter("BEYOND(ATTR1, POINTS(1.0 2.0), 10.0, kilometers)");
            fail("CQLException was expected");
        
        } catch(CQLException e){
            
            assertNotNull("Syntax error was expected (should be POINT)", e.getMessage());
        }
        
    }

    /**
     * Test RelGeo Operations [*]
     * <p>
     * 
     * <pre>
     *   &lt;routine invocation &gt; ::=
     *       &lt;geoop name &gt; &lt;georoutine argument list &gt;
     *   |   &lt;relgeoop name &gt; &lt;relgeoop argument list &gt;
     *   |   &lt;routine name &gt; &lt;argument list &gt; [*]
     *  &lt;argument list&gt; ::=    [*]
     *       &lt;left paren&gt; [&lt;positional arguments&gt;] &lt;right paren&gt;
     *  &lt;positional arguments&gt; ::=
     *       &lt;argument&gt; [ { &lt;comma&amp;gt &lt;argument&gt; }... ]
     *  &lt;argument&gt;  ::=
     *       &lt;literal&gt;
     *   |   &lt;attribute name&gt;
     * </pre>
     * 
     * </p>
     * 
     * @throws Exception
     */
    public void testRoutineInvocationGeneric() throws Exception {
        // TODO not implement it
        // (Mauricio Comments) This case is not implemented because the filter
        // model has not a
        // Routine (Like functions in Expression). We could develop easily the
        // parser but we can not build a filter for CQL <Routine invocation>.
    }

    

    /**
     * Tests Geometry Literals
     * <p>
     *
     * <pre>
     *  &lt;geometry literal &gt; :=
     *          &lt;Point Tagged Text &gt;
     *      |   &lt;LineString Tagged Text &gt;
     *      |   &lt;Polygon Tagged Text &gt;
     *      |   &lt;MultiPoint Tagged Text &gt;
     *      |   &lt;MultiLineString Tagged Text &gt;
     *      |   &lt;MultiPolygon Tagged Text &gt;
     *      |   &lt;GeometryCollection Tagged Text &gt;
     *      |   &lt;Envelope Tagged Text &gt;
     * </pre>
     *
     * </p>
     */
    public void testGeometryLiterals() throws Exception {
        BinarySpatialOperator result;
        Literal geom;

        // Point
        result = (BinarySpatialOperator) CQL.toFilter("CROSS(ATTR1, POINT(1 2))");

        geom = (Literal) result.getExpression2();

        assertNotNull(geom.getValue());
        assertTrue(geom.getValue() instanceof com.vividsolutions.jts.geom.Point);

        // LineString
        result = (BinarySpatialOperator) CQL.toFilter("CROSS(ATTR1, LINESTRING(1 2, 10 15))");

        geom = (Literal) result.getExpression2();

        assertNotNull(geom.getValue());
        assertTrue(geom.getValue() instanceof com.vividsolutions.jts.geom.LineString);

        // Poligon
        result = (BinarySpatialOperator) CQL.toFilter(
                "CROSS(ATTR1, POLYGON((1 2, 15 2, 15 20, 15 21, 1 2)))");

        geom = (Literal) result.getExpression2();

        assertNotNull(geom.getValue());
        assertTrue(geom.getValue() instanceof com.vividsolutions.jts.geom.Polygon);

        // MultiPoint
        result = (BinarySpatialOperator) CQL.toFilter(
                "CROSS(ATTR1, MULTIPOINT( (1 2), (15 2), (15 20), (15 21), (1 2) ))");

        geom = (Literal) result.getExpression2();

        assertNotNull(geom.getValue());
        assertTrue(geom.getValue() instanceof com.vividsolutions.jts.geom.MultiPoint);

        // MultiLineString
        result = (BinarySpatialOperator) CQL.toFilter(
                "CROSS(ATTR1, MULTILINESTRING((10 10, 20 20),(15 15,30 15)) )");

        geom = (Literal) result.getExpression2();

        assertNotNull(geom.getValue());
        assertTrue(geom.getValue() instanceof com.vividsolutions.jts.geom.MultiLineString);

        // MultiPolygon
        result = (BinarySpatialOperator) CQL.toFilter(
                "CROSS(ATTR1, MULTIPOLYGON( ((10 10, 10 20, 20 20, 20 15, 10 10)),((60 60, 70 70, 80 60, 60 60 )) ) )");

        geom = (Literal) result.getExpression2();

        assertNotNull(geom.getValue());
        assertTrue(geom.getValue() instanceof com.vividsolutions.jts.geom.MultiPolygon);

        // GEOMETRYCOLLECTION
        result = (BinarySpatialOperator) CQL.toFilter(
                "CROSS(ATTR1, GEOMETRYCOLLECTION (POINT (10 10),POINT (30 30),LINESTRING (15 15, 20 20)) )");

        geom = (Literal) result.getExpression2();

        assertNotNull(geom.getValue());
        assertTrue(geom.getValue() instanceof com.vividsolutions.jts.geom.GeometryCollection);

        // ENVELOPE
        result = (BinarySpatialOperator) CQL.toFilter(
                "CROSS(ATTR1, ENVELOPE( 10, 20, 30, 40) )");

        geom = (Literal) result.getExpression2();

        assertNotNull(geom.getValue());
        assertTrue(geom.getValue() instanceof com.vividsolutions.jts.geom.Polygon);
    }

    /**
     * Test error at geometry literal
     *
     */
    public void testGeometryLiteralsError() {
        final String filterError = "WITHIN(ATTR1, POLYGON((1 2, 10 15), (10 15, 1 2)))";

        try {
            CQL.toFilter(filterError);

            fail("polygon error was expected");
        } catch (CQLException e) {
            String message = e.getSyntaxError();
            assertFalse("error message is expected", "".equals(message));
        }
    }
    
    /**
     * Some token errors 
     */
    public void testTokensError(){

        // double quote in expression
        try {
            String cqlExpression = "strConcat(A, \".\")";
            CQL.toExpression(cqlExpression);

            fail("CQLException was expected");

        } catch (CQLException e) {
            
            assertNotNull("Token message error was expected", e.getMessage());
        }
        // double quote in predicate 
        try {
            String cqlExpression = "A=\"2\"";
            CQL.toFilter(cqlExpression);

            fail("CQLException was expected");

        } catch (CQLException e) {
            
            assertNotNull("Token message error was expected", e.getMessage());

        }

        
        // token identifier error
        try {
            String cqlExpression = "1A=2";
            CQL.toFilter(cqlExpression);

            fail("CQLException was expected");

        } catch (CQLException e) {
            
            assertNotNull("Token message error was expected", e.getMessage());
        }
    }
    
    /**
     * <pre>
     * &lt;character string literal&gt; ::= &lt;quote&gt; [ {&lt;character representation&lt;} ]  &lt;quote&gt;
     * &lt;character representation&gt; ::= 
     * 			&lt;nonquote character&gt; 
     * 		| 	&lt;quote symbol&gt;
 	 * &lt;quote symbol&gt; ::=  &lt;quote&gt; &lt;quote&gt;
 	 *  </pre>
     */
    public void testCharacterStringLiteral() throws Exception{
    	
    	PropertyIsEqualTo eqFilter; 

    	// space check
    	final String strWithSpace = "ALL PRACTICES";
    	Filter filterWithSpace = CQL.toFilter("practice='" +  strWithSpace + "'");
        assertNotNull(filterWithSpace);
        assertTrue(filterWithSpace instanceof PropertyIsEqualTo);

        eqFilter = (PropertyIsEqualTo) filterWithSpace;
        Expression spacesLiteral = eqFilter.getExpression2();
        assertEquals(strWithSpace, spacesLiteral.toString());
        
        // empty string ''
    	Filter emptyFilter = CQL.toFilter("MAJOR_WATERSHED_SYSTEM = ''");

        assertNotNull(emptyFilter);
        assertTrue(emptyFilter instanceof PropertyIsEqualTo);

        eqFilter = (PropertyIsEqualTo) emptyFilter;
        Expression emptyLiteral = eqFilter.getExpression2();
        assertEquals("", emptyLiteral.toString());
        
        //character string without quote
        final String expectedWithout = "ab";

        Filter filterWithoutQuote = CQL.toFilter("MAJOR_WATERSHED_SYSTEM = '"
                + expectedWithout + "'");

        assertNotNull(filterWithoutQuote);
        assertTrue(filterWithoutQuote instanceof PropertyIsEqualTo);

        eqFilter = (PropertyIsEqualTo) filterWithoutQuote;
        Expression actualWhithoutQuote = eqFilter.getExpression2();
        assertEquals(expectedWithout, actualWhithoutQuote.toString());
        
    	// <quote symbol>
        final String expected = "cde'' fg";

        Filter filter = CQL.toFilter("MAJOR_WATERSHED_SYSTEM = '" + expected
                + "'");

        assertNotNull(filter);
        assertTrue(filter instanceof PropertyIsEqualTo);

        eqFilter = (PropertyIsEqualTo) filter;
        Expression actual = eqFilter.getExpression2();
        assertEquals(expected.replaceAll("''", "'"), actual.toString());
    	
    }

    /**
     * Test for expressions
     *  
     * @throws Exception
     */
    public void testParseExpression() throws Exception {
        Expression expression = CQL.toExpression("attName");
        assertNotNull(expression);
        assertTrue(expression instanceof PropertyName);
        assertEquals("attName", ((PropertyName) expression).getPropertyName());

        expression = CQL.toExpression("a + b + x.y.z");
        assertNotNull(expression);
        assertTrue(expression instanceof Add);

        Add add = (Add) expression;
        Expression e1 = add.getExpression1();
        Expression e2 = add.getExpression2();

        assertTrue(e1 instanceof Add);
        assertTrue(e2 instanceof PropertyName);
        assertEquals("x/y/z", ((PropertyName) e2).getPropertyName());
    }

    /**
     * General test for cql expressions
     *
     * @param cqlSample
     * @throws Exception
     */
    private void testEqualsExpressions(final String cqlSample)
        throws Exception {
        Filter expected = FilterSample.getSample(cqlSample);
        Filter actual = CQL.toFilter(cqlSample);

        assertNotNull("expects filter not null", actual);
        assertEquals("this is not the filter expected", expected, actual);
    }
   
    public final void testGetSyntaxError() {

        try {
            final String malformedExp = "12 / ] + 4";
            CQL.toFilter(malformedExp);
            fail("expected Exception");
        } catch (CQLException pe) {
            String error = pe.getSyntaxError();
            assertFalse("".equals(error));
        }

        try {
            String malformedGeometry = "WITHIN(ATTR1, POLYGON((1 2, 10 15), (10 15, 1 2)))";
            CQL.toFilter(malformedGeometry);

            fail("polygon error was expected");
        } catch (CQLException e) {
            String message = e.getSyntaxError();
            assertFalse("error message is expected", "".equals(message));
        }
        
    }
}
