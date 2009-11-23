/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 *
 *    (C) 2009, Open Source Geospatial Foundation (OSGeo)
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

package org.geotools.geometry.jts;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Michael Bedward
 * @since 2.6
 * @source $URL$
 * @version $Id$
 */
public class GeometryTypeTest {

    @Test
    public void testGetBinding() {
        System.out.println("   getBinding");

        assertEquals(GeometryType.POINT.getBinding(), Point.class);
        assertEquals(GeometryType.MULTIPOINT.getBinding(), MultiPoint.class);
        assertEquals(GeometryType.LINESTRING.getBinding(), LineString.class);
        assertEquals(GeometryType.MULTILINESTRING.getBinding(), MultiLineString.class);
        assertEquals(GeometryType.POLYGON.getBinding(), Polygon.class);
        assertEquals(GeometryType.MULTIPOLYGON.getBinding(), MultiPolygon.class);
        assertEquals(GeometryType.GEOMETRY.getBinding(), Geometry.class);
        assertEquals(GeometryType.GEOMETRYCOLLECTION.getBinding(), GeometryCollection.class);
    }

    @Test
    public void testGetByClass() {
        System.out.println("   get (by class)");

        assertEquals(GeometryType.get(Point.class), GeometryType.POINT);
        assertEquals(GeometryType.get(MultiPoint.class), GeometryType.MULTIPOINT);
        assertEquals(GeometryType.get(LineString.class), GeometryType.LINESTRING);
        assertEquals(GeometryType.get(MultiLineString.class), GeometryType.MULTILINESTRING);
        assertEquals(GeometryType.get(Polygon.class), GeometryType.POLYGON);
        assertEquals(GeometryType.get(MultiPolygon.class), GeometryType.MULTIPOLYGON);
        assertEquals(GeometryType.get(Geometry.class), GeometryType.GEOMETRY);
        assertEquals(GeometryType.get(GeometryCollection.class), GeometryType.GEOMETRYCOLLECTION);
    }

    @Test
    public void testGetByObject() {
        System.out.println("   get (by object)");

        GeometryFactory geomFactory = JTSFactoryFinder.getGeometryFactory(null);
        Coordinate[] coords = {
            new Coordinate(0, 0),
            new Coordinate(0, 10),
            new Coordinate(10, 10),
            new Coordinate(10, 0),
            new Coordinate(0, 0)
        };

        assertEquals(GeometryType.get(Point.class), GeometryType.POINT);
        assertEquals(GeometryType.get(MultiPoint.class), GeometryType.MULTIPOINT);

        Geometry line = geomFactory.createLineString(coords);
        assertEquals(GeometryType.get(LineString.class), GeometryType.LINESTRING);

        LineString[] lines = {
            geomFactory.createLineString(new Coordinate[]{coords[0], coords[1]}),
            geomFactory.createLineString(new Coordinate[]{coords[2], coords[3]})
        };
        Geometry multiLine = geomFactory.createMultiLineString(lines);
        assertEquals(GeometryType.get(multiLine), GeometryType.MULTILINESTRING);

        Polygon poly = geomFactory.createPolygon(geomFactory.createLinearRing(coords), null);
        assertEquals(GeometryType.get(poly), GeometryType.POLYGON);

        Polygon[] polys = {poly, poly};
        Geometry multiPoly = geomFactory.createMultiPolygon(polys);
        assertEquals(GeometryType.get(multiPoly), GeometryType.MULTIPOLYGON);

        Geometry gc = geomFactory.createGeometryCollection(polys);
        assertEquals(GeometryType.get(gc), GeometryType.GEOMETRYCOLLECTION);
    }

    /**
     * Tests getName and getSimpleName
     */
    @Test
    public void testGetName() {
        System.out.println("   getName and getSimpleName");

        for (GeometryType type : GeometryType.values()) {
            String className = type.getBinding().getSimpleName();
            assertTrue(type.getName().equalsIgnoreCase(className));

            if (className.startsWith("Multi")) {
                assertTrue(type.getSimpleName().equalsIgnoreCase(className.substring(5)));
            } else {
                assertTrue(type.getSimpleName().equalsIgnoreCase(className));
            }
        }
    }

    @Test
    public void testGetForSQLType() {
        System.out.println("   getSQLType and getForSQLType");
        for (GeometryType type : GeometryType.values()) {
            int sqlType = type.getSQLType();
            assertEquals(GeometryType.getForSQLType(sqlType), type);
        }
    }

}