/*
 *    GeoTools - The Open Source Java GIS Tookit
 *    http://geotools.org
 * 
 *    (C) 2006-2008, Open Source Geospatial Foundation (OSGeo)
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

import org.junit.Assert;
import org.junit.Test;
import org.opengis.filter.Filter;
import org.opengis.filter.PropertyIsEqualTo;
import org.opengis.filter.PropertyIsLessThan;
import org.opengis.filter.expression.Expression;
import org.opengis.filter.expression.Literal;
import org.opengis.filter.expression.PropertyName;

/**
 * Unit Test for Comparison Predicate
 * <p>
 * By default test execute the test parsing Comparison Predicate using
 * the CQL compiler.
 * </p>
 * 
 * <p>
 * The subclass could instance the test case with different language.
 * </p>
 *
 * @author Mauricio Pazos (Axios Engineering)
 * @since 2.5
 */
public class CQLComparisonPredicateTest {
    
    private CompilerFactory.Language language = null;

    
    public CQLComparisonPredicateTest(){
        
        this(CompilerFactory.Language.CQL);
    }

    public CQLComparisonPredicateTest(final CompilerFactory.Language language){
        
        
        assert language != null: "language cannot be null value";
        
        this.language = language;
    }
    
    protected Filter parse(final String predicate) throws CQLException {

        assert predicate != null:"predicate cannot be null value";
        
        ICompiler compiler = CompilerFactory.makeCompiler(this.language, predicate, null);
        compiler.compileFilter();
        Filter result = compiler.getFilter();

        return result;
    }
    
    
    /**
     * Test Comparison Predicate
     * <p>
     *
     * <pre>
     * &lt;comparison predicate &gt; ::=
     *      &lt;attribute name &gt;  &lt;comp op &gt;  &lt;literal &gt;
     * </pre>
     *
     * </p>
     */
    @Test
    public void comparisonOperators() throws Exception {
        Filter expected;
        Filter actual;
        // attr1 < 5
        expected = FilterSample.getSample(FilterSample.LESS_FILTER_SAMPLE);

        actual = parse(FilterSample.LESS_FILTER_SAMPLE);

        Assert.assertNotNull("expects filter not null", actual);

        Assert.assertEquals("less than compare filter error", expected, actual);

        // attr1 <= 5
        expected = FilterSample.getSample(FilterSample.LESS_EQ_FILTER_SAMPLE);

        actual = parse(FilterSample.LESS_EQ_FILTER_SAMPLE);

        Assert.assertNotNull("expects filter not null", actual);

        Assert.assertEquals("less or equal compare filter error", expected, actual);

        // attr <> 5
        expected = FilterSample.getSample(FilterSample.NOT_EQ_FILTER);

        actual = parse(FilterSample.NOT_EQ_FILTER);

        Assert.assertNotNull("expects filter not null", actual);

        Assert.assertEquals("not equal compare filter error", expected, actual);

        // "gmd:aa:bb.gmd:cc.gmd:dd"
        final String prop = "gmd:aa:bb.gmd:cc.gmd:dd";
        final String propExpected = "gmd:aa:bb/gmd:cc/gmd:dd";
        actual = CQL.toFilter(prop + " < 100");

        Assert.assertTrue("PropertyIsLessThan filter was expected", actual instanceof PropertyIsLessThan);

        PropertyIsLessThan lessFilter = (PropertyIsLessThan) actual;
        Expression property = lessFilter.getExpression1();

        Assert.assertEquals(propExpected, property.toString());
    }

    /**
     * Test comparison Predicate.
     * <p>
     *
     * <pre>
     * &lt;comparison predicate &gt; ::=
     *      &lt;attrsibute name &gt;  &lt;comp op &gt;  &lt;literal &gt;
     * </pre>
     *
     * </p>
     */
    @Test
    public void propertyComparisonSimpleExpressions()
            throws Exception {
        Filter expected;
        Filter actual;

        expected = FilterSample.getSample(FilterSample.FILTER_SIMPLE_EXPR);
        actual = parse(FilterSample.FILTER_SIMPLE_EXPR);

        Assert.assertNotNull("expects filter not null", actual);

        Assert.assertEquals("not equal compare filter error", expected, actual);
    }
    
    /**
     * Comparison with boolean values
     * @throws Exception
     */
    @Test
    public void booleanLiteral() throws Exception {
       
        Filter filter;
        PropertyIsEqualTo eqFilter;
        
        //test true value
        filter = CQL.toFilter("attr = true");
        Assert.assertNotNull(filter);
        Assert.assertTrue(filter instanceof PropertyIsEqualTo);

        eqFilter = (PropertyIsEqualTo) filter;
        Assert.assertEquals("attr", ((PropertyName) eqFilter.getExpression1()).getPropertyName());
        Assert.assertEquals(Boolean.TRUE, ((Literal) eqFilter.getExpression2()).getValue());

        //test false value
        filter = CQL.toFilter("attr = false");
        Assert.assertNotNull(filter);
        Assert.assertTrue(filter instanceof PropertyIsEqualTo);

        eqFilter = (PropertyIsEqualTo) filter;
        Assert.assertEquals("attr", ((PropertyName) eqFilter.getExpression1()).getPropertyName());
        Assert.assertEquals(Boolean.FALSE, ((Literal) eqFilter.getExpression2()).getValue());
    }

}
