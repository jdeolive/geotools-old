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

import org.junit.Assert;
import org.junit.Test;
import org.opengis.filter.Filter;



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
public class CQLTest {

    @Test
    public void comparison()throws Exception{
        
        // attr1 < 5
        Filter expected = FilterCQLSample.getSample(FilterCQLSample.LESS_FILTER_SAMPLE);

        Filter actual = CQL.toFilter(FilterCQLSample.LESS_FILTER_SAMPLE);

        Assert.assertNotNull("expects filter not null", actual);

        Assert.assertEquals("less than compare filter error", expected, actual);
    }
    //TODO simples samples to shoe the CQL interface use
    
}
