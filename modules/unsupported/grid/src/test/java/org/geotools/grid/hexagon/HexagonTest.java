/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 *
 *    (C) 2010, Open Source Geospatial Foundation (OSGeo)
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

package org.geotools.grid.hexagon;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Polygon;

import org.geotools.grid.hexagon.Hexagon.Orientation;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Unit tests for the Hexagon class.
 *
 * @author mbedward
 * @since 2.7
 * @source $URL$
 * @version $Id$
 */
public class HexagonTest extends HexagonTestBase {

    @Test
    public void getVerticesFlat() {
        double minx = 1.0;
        double miny = -1.0;
        Hexagon hexagon = new HexagonImpl(SIDE_LEN, minx, miny, Orientation.FLAT);

        assertVertices(hexagon, SIDE_LEN, minx, miny, Orientation.FLAT);
    }

    @Test
    public void badSideLen() throws Exception {
        boolean gotEx = false;
        try {
            Hexagon h = new HexagonImpl(0.0, 0.0, 0.0, Orientation.FLAT);
        } catch (IllegalArgumentException ex) {
            gotEx = true;
        }

        assertTrue(gotEx);
    }

    @Test
    public void badOrientation() throws Exception {
        boolean gotEx = false;
        try {
            Hexagon h = new HexagonImpl(SIDE_LEN, 0.0, 0.0, null);
        } catch (IllegalArgumentException ex) {
            gotEx = true;
        }

        assertTrue(gotEx);
    }

    @Test
    public void getOrientation() {
        Hexagon hexagon = new HexagonImpl(SIDE_LEN, 0.0, 0.0, Orientation.ANGLED);
        assertEquals(Orientation.ANGLED, hexagon.getOrientation());

        hexagon = new HexagonImpl(SIDE_LEN, 0.0, 0.0, Orientation.FLAT);
        assertEquals(Orientation.FLAT, hexagon.getOrientation());
    }

    @Test
    public void getVerticesAngled() {
        double minx = 1.0;
        double miny = -1.0;
        Hexagon hexagon = new HexagonImpl(SIDE_LEN, minx, miny, Orientation.ANGLED);

        assertVertices(hexagon, SIDE_LEN, minx, miny, Orientation.ANGLED);
    }

    @Test
    public void getCenterFlat() throws Exception {
        Hexagon hexagon = new HexagonImpl(SIDE_LEN, 0.0, 0.0, Orientation.FLAT);
        Coordinate expected = new Coordinate(SIDE_LEN, 0.5 * Math.sqrt(3.0) * SIDE_LEN);
        Coordinate result = hexagon.getCenter();

        assertCoordinate(expected, result);
    }

    @Test
    public void getCenterAngled() {
        Hexagon hexagon = new HexagonImpl(SIDE_LEN, 0.0, 0.0, Orientation.ANGLED);
        Coordinate expected = new Coordinate(0.5 * Math.sqrt(3.0) * SIDE_LEN, SIDE_LEN);
        Coordinate result = hexagon.getCenter();

        assertCoordinate(expected, result);
    }

    @Test
    public void getBoundsFlat() {
        Hexagon hexagon = new HexagonImpl(SIDE_LEN, 0.0, 0.0, Orientation.FLAT);

        Envelope expected = new Envelope(
                0.0,
                2.0 * SIDE_LEN,
                0.0,
                Math.sqrt(3.0) * SIDE_LEN);

        Envelope result = hexagon.getBounds();

        assertEnvelope(expected, result);
    }

    @Test
    public void getBoundsAngled() {
        Hexagon hexagon = new HexagonImpl(SIDE_LEN, 0.0, 0.0, Orientation.ANGLED);

        Envelope expected = new Envelope(
                0.0,
                Math.sqrt(3.0) * SIDE_LEN,
                0.0,
                2.0 * SIDE_LEN);

        Envelope result = hexagon.getBounds();

        assertEnvelope(expected, result);
    }

    @Test
    public void toPolygon() {
        Hexagon hexagon = Hexagons.create(SIDE_LEN, 0.0, 0.0, Orientation.FLAT);
        Geometry polygon = hexagon.toPolygon();
        assertNotNull(polygon);
        assertTrue(polygon instanceof Polygon);
    }

}
