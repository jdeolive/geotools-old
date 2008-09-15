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

package org.geotools.filter.text.txt;

import org.geotools.factory.CommonFactoryFinder;
import org.geotools.factory.Hints;
import org.geotools.filter.text.commons.CompilerUtil;
import org.geotools.filter.text.cql2.CQLRelGeoOpTest;
import org.geotools.filter.text.cql2.CompilerFactory.Language;
import org.junit.Assert;
import org.junit.Test;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory;
import org.opengis.filter.expression.Expression;
import org.opengis.filter.expression.Literal;
import org.opengis.filter.spatial.DWithin;
import org.opengis.filter.spatial.DistanceBufferOperator;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.WKTReader;

/**
 * Relation geo operation 
 *
 * @author Mauricio Pazos (Axios Engineering)
 * @since 2.6
 */
public class TXTRelGeoOpTest extends CQLRelGeoOpTest {
    protected static final FilterFactory FILTER_FACTORY = CommonFactoryFinder.getFilterFactory((Hints) null);

    public TXTRelGeoOpTest(){
        super(Language.TXT);
    }

    @Test
    public void dwithinLineString() throws Exception {
        Filter resultFilter;

        // DWITHIN
        final String propExpected = "ATTR1";
        final String strGeomExpected= "LINESTRING( 1 2, 3 4)";
        resultFilter = CompilerUtil.parseFilter(language,
                "DWITHIN("+propExpected+", "+strGeomExpected+", 10, kilometers)");

        Assert.assertTrue("DistanceBufferOperator filter was expected",
                resultFilter instanceof DWithin);

        DistanceBufferOperator filter = (DWithin) resultFilter;
        Expression property = filter.getExpression1();

        Assert.assertEquals(propExpected, property.toString());

        Expression actualGeometry = filter.getExpression2();
        assertEqualsGeometries(strGeomExpected, actualGeometry );
        
    }

    @Test
    public void dwithinPolygon() throws Exception {
        Filter resultFilter;

        // DWITHIN
        final String propExpected = "ATTR1";
        final String strGeomExpected = "POLYGON ((40 60, 420 60, 420 320, 40 320, 40 60))";
        resultFilter = CompilerUtil.parseFilter(language,
                "DWITHIN("+propExpected+", " + strGeomExpected+", 10, kilometers)" );

        Assert.assertTrue("DistanceBufferOperator filter was expected",
                resultFilter instanceof DWithin);

        DistanceBufferOperator filter = (DWithin) resultFilter;
        Expression property = filter.getExpression1();

        Assert.assertEquals(propExpected, property.toString());

        Expression actualGeometry = filter.getExpression2();
        assertEqualsGeometries(strGeomExpected, actualGeometry);
    }
    
    @Test
    public void dwithinPolygonWithHoles() throws Exception{

        // DWITHIN
        final String propExpected = "ATTR1";
        final String strGeomExpected = "POLYGON ((40 60, 420 60, 420 320, 40 320, 40 60), (200 140, 160 220, 260 200, 200 140))";
        Filter resultFilter = CompilerUtil.parseFilter(language,
                "DWITHIN("+propExpected+", " + strGeomExpected+", 10, kilometers)" );

        Assert.assertTrue("DistanceBufferOperator filter was expected",
                resultFilter instanceof DWithin);

        DistanceBufferOperator filter = (DWithin) resultFilter;
        Expression property = filter.getExpression1();

        Assert.assertEquals(propExpected, property.toString());

        Expression actualGeometry = filter.getExpression2();
        assertEqualsGeometries(strGeomExpected, actualGeometry);
    }

    @Test //FIXME fail
    public void dwithinMultipoint() throws Exception {
        Filter resultFilter;

        // DWITHIN
        final String propExpected = "ATTR1";
        final String cqlGeom = "MULTIPOINT((40 40), (60 100), (100 60), (120 120))";
        final String jtsExpected = "MULTIPOINT(40 40, 60 100, 100 60, 120 120)";
        resultFilter = CompilerUtil.parseFilter(language,
                "DWITHIN("+propExpected+", " + cqlGeom+", 10, kilometers)" );

        Assert.assertTrue("DistanceBufferOperator filter was expected",
                resultFilter instanceof DWithin);

        DistanceBufferOperator filter = (DWithin) resultFilter;
        Expression property = filter.getExpression1();

        Assert.assertEquals(propExpected, property.toString());

        Expression actualGeometry = filter.getExpression2();
        assertEqualsGeometries(jtsExpected, actualGeometry);
    }

    private void assertEqualsGeometries(final String strGeomExpected,
            final Expression actualGeometry)throws Exception {

        WKTReader reader = new WKTReader();
        Geometry geomExpected = reader.read(strGeomExpected);
        Literal LiteralGeomExpected = FILTER_FACTORY.literal(geomExpected);
        Assert.assertEquals(LiteralGeomExpected, actualGeometry);

    }
    //TODO test for MULTILINESTRING ((20 20, 90 20, 170 20), (90 20, 90 80, 90 140))
    
    //TODO test for MULTIPOLYGON (((80 40, 120 40, 120 80, 80 80, 80 40)), ((120 80, 160 80, 160 120, 120 120, 120 80)), ((80 120, 120 120, 120 160, 80 160, 80 120)), ((40 80, 80 80, 80 120, 40 120, 40 80)))
    
}
