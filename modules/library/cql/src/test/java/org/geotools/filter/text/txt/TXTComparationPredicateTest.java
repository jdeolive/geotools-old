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

package org.geotools.filter.text.txt;

import org.geotools.filter.text.cql2.CQLComparisonPredicateTest;
import org.geotools.filter.text.cql2.CQLException;
import org.geotools.filter.text.cql2.CompilerFactory;
import org.geotools.filter.text.cql2.FilterSample;
import org.junit.Assert;
import org.junit.Ignore;
import org.opengis.filter.Filter;

/**
 * TXT Comparison Predicate Test Case.
 * <p>
 * The implementation must parse comparison predicate using the following grammar rule:
 * <pre>
 * &lt comparison predicate &gt ::= &lt expression &gt &lt comp op &gt &lt expression &gt
 * </pre>
 * </p>
 * <p>
 * This test case extends the from CQL test in order to assure that this extension (TXT) contains
 * the base language (CQL).
 * </p>
 *
 * @author Mauricio Pazos (Axios Engineering)
 * @since 2.5
 */
public class TXTComparationPredicateTest extends CQLComparisonPredicateTest {
    
    public TXTComparationPredicateTest(){
        // sets the language used to execute this test case
        super(CompilerFactory.Language.TXT);
    }
    
    
    /**
     * Test: Expression on the Left hand of comparison predicate
     * Sample:  (1+3) > prop1
     * @throws CQLException 
     */
    @Ignore
    public void expressionGreaterThanProperty() throws CQLException{
        
        Filter expected = FilterSample.getSample(FilterTXTSample.EXPRESION_LESS_PROPERTY);

        Filter actual = parse(FilterTXTSample.EXPRESION_LESS_PROPERTY);

        Assert.assertNotNull("expects filter not null", actual);

        Assert.assertEquals("less than compare filter error", expected, actual);
    }

    /**
     * Test: function on the Left hand of comparison predicate
     * Sample: area( the_geom ) < 30000
     * 
     * @throws CQLException 
     */
    @Ignore
    public void functionLessThanLiteral() throws CQLException{

        Filter expected = FilterSample.getSample(FilterTXTSample.FUNCTION_LESS_LITERAL);

        Filter actual = parse(FilterTXTSample.FUNCTION_LESS_LITERAL);

        Assert.assertNotNull("expects filter not null", actual);

        Assert.assertEquals("compare filter error", expected, actual);
    }
    

}
