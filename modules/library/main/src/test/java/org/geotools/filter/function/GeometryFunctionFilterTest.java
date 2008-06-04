/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002-2008, GeoTools Project Managment Committee (PMC)
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
package org.geotools.filter.function;

import org.geotools.feature.FeatureIterator;
import org.geotools.filter.Expression;
import org.geotools.filter.FunctionExpression;
import org.opengis.feature.simple.SimpleFeature;


public class GeometryFunctionFilterTest extends FunctionTestSupport {

    public GeometryFunctionFilterTest() {
        super("GeometryFunctionFilterTest");
    }
    
    public void testBasicTest() throws Exception {
        FunctionExpression exp = fac.createFunctionExpression("geometryType");
        exp.setArgs(new Expression[]{ fac.createAttributeExpression("geom") });
        FeatureIterator<SimpleFeature> iter=featureCollection.features();
        while( iter.hasNext() ){
            SimpleFeature feature = iter.next();
            assertEquals( "Point", exp.getValue(feature) );
        }
        
        iter.close();
    }
    
    public void testNullTest() throws Exception {
        FunctionExpression exp = fac.createFunctionExpression("geometryType");
        exp.setArgs(new Expression[]{ fac.createAttributeExpression("geom") });
        FeatureIterator<SimpleFeature> iter=featureCollection.features();
        while( iter.hasNext() ){
        	SimpleFeature feature = iter.next();
            feature.setAttribute("geom",null);
            assertNull( exp.getValue(feature) );
        }
        
        iter.close();
    }
}
