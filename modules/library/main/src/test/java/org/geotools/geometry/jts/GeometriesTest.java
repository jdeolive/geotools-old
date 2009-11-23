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
public class GeometriesTest {

    @Test
    public void testGetBinding() {
        System.out.println("   getBinding");

        assertEquals(Geometries.POINT.getBinding(), Point.class);
        assertEquals(Geometries.MULTIPOINT.getBinding(), MultiPoint.class);
        assertEquals(Geometries.LINESTRING.getBinding(), LineString.class);
        assertEquals(Geometries.MULTILINESTRING.getBinding(), MultiLineString.class);
        assertEquals(Geometries.POLYGON.getBinding(), Polygon.class);
        assertEquals(Geometries.MULTIPOLYGON.getBinding(), MultiPolygon.class);
        assertEquals(Geometries.GEOMETRY.getBinding(), Geometry.class);
        assertEquals(Geometries.GEOMETRYCOLLECTION.getBinding(), GeometryCollection.class);
    }

    @Test
    public void testGetByClass() {
        System.out.println("   get (by class)");

        assertEquals(Geometries.get(Point.class), Geometries.POINT);
        assertEquals(Geometries.get(MultiPoint.class), Geometries.MULTIPOINT);
        assertEquals(Geometries.get(LineString.class), Geometries.LINESTRING);
        assertEquals(Geometries.get(MultiLineString.class), Geometries.MULTILINESTRING);
        assertEquals(Geometries.get(Polygon.class), Geometries.POLYGON);
        assertEquals(Geometries.get(MultiPolygon.class), Geometries.MULTIPOLYGON);
        assertEquals(Geometries.get(Geometry.class), Geometries.GEOMETRY);
        assertEquals(Geometries.get(GeometryCollection.class), Geometries.GEOMETRYCOLLECTION);
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

        assertEquals(Geometries.get(Point.class), Geometries.POINT);
        assertEquals(Geometries.get(MultiPoint.class), Geometries.MULTIPOINT);

        Geometry line = geomFactory.createLineString(coords);
        assertEquals(Geometries.get(LineString.class), Geometries.LINESTRING);

        LineString[] lines = {
            geomFactory.createLineString(new Coordinate[]{coords[0], coords[1]}),
            geomFactory.createLineString(new Coordinate[]{coords[2], coords[3]})
        };
        Geometry multiLine = geomFactory.createMultiLineString(lines);
        assertEquals(Geometries.get(multiLine), Geometries.MULTILINESTRING);

        Polygon poly = geomFactory.createPolygon(geomFactory.createLinearRing(coords), null);
        assertEquals(Geometries.get(poly), Geometries.POLYGON);

        Polygon[] polys = {poly, poly};
        Geometry multiPoly = geomFactory.createMultiPolygon(polys);
        assertEquals(Geometries.get(multiPoly), Geometries.MULTIPOLYGON);

        Geometry gc = geomFactory.createGeometryCollection(polys);
        assertEquals(Geometries.get(gc), Geometries.GEOMETRYCOLLECTION);
    }

    /**
     * Tests getName and getSimpleName
     */
    @Test
    public void testGetName() {
        System.out.println("   getName and getSimpleName");

        for (Geometries type : Geometries.values()) {
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
        for (Geometries type : Geometries.values()) {
            int sqlType = type.getSQLType();
            assertEquals(Geometries.getForSQLType(sqlType), type);
        }
    }

}