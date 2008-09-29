/*
 *    GeoTools - The Open Source Java GIS Tookit
 *    http://geotools.org
 *
 *    (C) 2004-2008, Open Source Geospatial Foundation (OSGeo)
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

import org.geotools.filter.text.commons.CompilerUtil;
import org.geotools.filter.text.cql2.CompilerFactory.Language;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.opengis.filter.Filter;

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
 * 
 * @author Mauricio Pazos (Axios Engineering)
 * @since 2.6
 */
public class CQLBooleanValueExpressionTest {
    protected final CompilerFactory.Language language;

    public CQLBooleanValueExpressionTest(){
        
        this(Language.CQL);
    }

    public CQLBooleanValueExpressionTest(final CompilerFactory.Language language){
        
        assert language != null: "language cannot be null value";
        
        this.language = language;
    }

    /**
     * Sample: ATTR1 < 10 AND ATTR2 < 2
     * @throws CQLException 
     */
    @Test 
    public void and() throws CQLException{
        Filter result = CompilerUtil.parseFilter(this.language,FilterCQLSample.FILTER_AND);

        Assert.assertNotNull("filter expected", result);

        Filter expected = FilterCQLSample.getSample(FilterCQLSample.FILTER_AND);

        Assert.assertEquals("ATTR1 < 10 AND ATTR2 < 2 was expected", expected, result);
        
    }

    /**
     * Sample: "ATTR1 > 10 OR ATTR2 < 2"
     * @throws CQLException 
     */
    @Test
    public void or() throws CQLException{
        // "ATTR1 > 10 OR ATTR2 < 2"
        Filter result = CompilerUtil.parseFilter(this.language,FilterCQLSample.FILTER_OR);

        Assert.assertNotNull("filter expected", result);

        Filter expected = FilterCQLSample.getSample(FilterCQLSample.FILTER_OR);

        Assert.assertEquals("ATTR1 > 10 OR ATTR2 < 2 was expected", expected, result);
        
    }

    /**
     * Sample 1: ATTR1 < 10 AND ATTR2 < 2 OR ATTR3 > 10
     * Sample 2: ATTR3 < 4 AND (ATT1 > 10 OR ATT2 < 2)
     * @throws CQLException 
     */
    @Ignore // FIXME it fail for txt
    public void andOr() throws CQLException{
        Filter result;
        Filter expected;
        // ATTR1 < 10 AND ATTR2 < 2 OR ATTR3 > 10
        result = CompilerUtil.parseFilter(language, FilterCQLSample.FILTER_OR_AND);

        Assert.assertNotNull("filter expected", result);

        expected = FilterCQLSample.getSample(FilterCQLSample.FILTER_OR_AND);

        Assert.assertEquals("(ATTR1 < 10 AND ATTR2 < 2 OR ATTR3 > 10) was expected", expected, result);

        // ATTR3 < 4 AND (ATT1 > 10 OR ATT2 < 2)
        result = CompilerUtil.parseFilter(language, FilterCQLSample.FILTER_OR_AND_PARENTHESIS);

        Assert.assertNotNull("filter expected", result);

        expected = FilterCQLSample.getSample(FilterCQLSample.FILTER_OR_AND_PARENTHESIS);

        Assert.assertEquals("ATTR3 < 4 AND (ATT1 > 10 OR ATT2 < 2) was expected", expected, result);
        
    }
    
    @Ignore // FIXME it fail for txt
    public void andNot() throws Exception {
        Filter result;
        Filter expected;

        // ATTR3 < 4 AND (NOT( ATTR1 < 10 AND ATTR2 < 2))
        result = CompilerUtil.parseFilter(language, FilterCQLSample.FILTER_AND_NOT_AND);

        Assert.assertNotNull("filter expected", result);

        expected = FilterCQLSample.getSample(FilterCQLSample.FILTER_AND_NOT_AND);

        Assert.assertEquals("ATTR3 < 4 AND (NOT( ATTR1 < 10 AND ATTR2 < 2)) was expected", expected, result);

        // "ATTR1 < 1 AND (NOT (ATTR2 < 2)) AND ATTR3 < 3"
        result = CompilerUtil.parseFilter(language, FilterCQLSample.FILTER_AND_NOT_COMPARASION);

        Assert.assertNotNull("filter expected", result);

        expected = FilterCQLSample.getSample(FilterCQLSample.FILTER_AND_NOT_COMPARASION);

        Assert.assertEquals("ATTR1 < 4 AND (NOT (ATTR2 < 4)) AND ATTR3 < 4 was expected", expected, result);
    }
     


}
