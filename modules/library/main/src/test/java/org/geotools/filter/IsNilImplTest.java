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
package org.geotools.filter;

import junit.framework.TestCase;

import org.geotools.factory.CommonFactoryFinder;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.filter.PropertyIsNil;
import org.opengis.filter.PropertyIsNull;

public class IsNilImplTest extends TestCase {

    org.opengis.filter.FilterFactory filterFactory = CommonFactoryFinder.getFilterFactory(null);
    SimpleFeature feature;
    
    protected void setUp() throws Exception {
        SimpleFeatureTypeBuilder tb = new SimpleFeatureTypeBuilder();
        tb.setName("feature");
        tb.add("foo", String.class);
        tb.add("bar", String.class);
        tb.add("baz", Integer.class);
        
        SimpleFeatureBuilder b = new SimpleFeatureBuilder(tb.buildFeatureType());
        b.add("hello");
        b.add(null);
        b.add(-1);
        feature = b.buildFeature(null);
    };
    
    public void test() throws Exception {
        PropertyIsNil isNil = filterFactory.isNil(filterFactory.property("foo"), null);
        assertFalse(isNil.evaluate(feature));
        
        isNil = filterFactory.isNil(filterFactory.property("foo"), "hello");
        assertTrue(isNil.evaluate(feature));
        
        isNil = filterFactory.isNil(filterFactory.property("bar"), null);
        assertTrue(isNil.evaluate(feature));
        
        isNil = filterFactory.isNil(filterFactory.property("baz"), "-1");
        assertTrue(isNil.evaluate(feature));
        
        isNil = filterFactory.isNil(filterFactory.property("baz"), -1);
        assertTrue(isNil.evaluate(feature));
    }

}
