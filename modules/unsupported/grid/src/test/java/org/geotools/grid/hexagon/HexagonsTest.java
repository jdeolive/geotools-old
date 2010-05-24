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

import java.util.HashMap;
import java.util.Map;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;

import org.geotools.data.DataUtilities;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.grid.AttributeSetter;
import org.geotools.grid.GridElement;
import org.geotools.grid.hexagon.Hexagon.Neighbor;
import org.geotools.grid.hexagon.Hexagon.Orientation;

import org.opengis.feature.simple.SimpleFeatureType;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Unit tests for the Hexagons utility class.
 *
 * @author mbedward
 * @since 2.7
 * @source $URL$
 * @version $Id$
 */
public class HexagonsTest extends HexagonTestBase {

    @Test
    public void calculateArea() {
        assertEquals(AREA, Hexagons.sideLengthToArea(SIDE_LEN), TOL);
    }

    @Test(expected=IllegalArgumentException.class)
    public void calculateAreaInvalidArg() {
        Hexagons.sideLengthToArea(0.0);
    }

    @Test
    public void calculateSideLen() {
        assertEquals(SIDE_LEN, Hexagons.areaToSideLength(AREA), TOL);
    }

    @Test(expected=IllegalArgumentException.class)
    public void calculateSideLenInvalidArg() {
        Hexagons.areaToSideLength(0.0);
    }

    @Test
    public void createHexagon() {
        Hexagon hexagon = Hexagons.create(SIDE_LEN, 0.0, 0.0, Orientation.FLAT);
        assertNotNull(hexagon);
    }

    @Test
    public void getVerticesFlat() {
        Hexagon hexagon = Hexagons.create(SIDE_LEN, 0.0, 0.0, Orientation.FLAT);
        assertVertices(hexagon, SIDE_LEN, 0.0, 0.0, Orientation.FLAT);
    }
    
    @Test
    public void getVerticesAngled() {
        Hexagon hexagon = Hexagons.create(SIDE_LEN, 0.0, 0.0, Orientation.ANGLED);
        assertVertices(hexagon, SIDE_LEN, 0.0, 0.0, Orientation.ANGLED);
    }

    @Test
    public void validNeighborPosition() {
        class Case {
            Orientation o;
            Neighbor n;
            boolean valid;

            public Case(Orientation o, Neighbor n, boolean valid) {
                this.o = o;
                this.n = n;
                this.valid = valid;
            }
        }

        Case[] cases = {
            new Case(Orientation.ANGLED, Neighbor.LEFT, true),
            new Case(Orientation.ANGLED, Neighbor.LOWER, false),
            new Case(Orientation.ANGLED, Neighbor.LOWER_LEFT, true),
            new Case(Orientation.ANGLED, Neighbor.LOWER_RIGHT, true),
            new Case(Orientation.ANGLED, Neighbor.RIGHT, true),
            new Case(Orientation.ANGLED, Neighbor.UPPER, false),
            new Case(Orientation.ANGLED, Neighbor.UPPER_LEFT, true),
            new Case(Orientation.ANGLED, Neighbor.UPPER_RIGHT, true),

            new Case(Orientation.FLAT, Neighbor.LEFT, false),
            new Case(Orientation.FLAT, Neighbor.LOWER, true),
            new Case(Orientation.FLAT, Neighbor.LOWER_LEFT, true),
            new Case(Orientation.FLAT, Neighbor.LOWER_RIGHT, true),
            new Case(Orientation.FLAT, Neighbor.RIGHT, false),
            new Case(Orientation.FLAT, Neighbor.UPPER, true),
            new Case(Orientation.FLAT, Neighbor.UPPER_LEFT, true),
            new Case(Orientation.FLAT, Neighbor.UPPER_RIGHT, true),
        };

        for (Case c : cases) {
            assertEquals("Failed for case: " + c.o + " " + c.n,
                    c.valid, Hexagons.isValidNeighbor(c.o, c.n));
        }
    }

    @Test
    public void createNeighbor() {
        Hexagon hn = null;

        class Shift {
            double dx;
            double dy;

            public Shift(double dx, double dy) {
                this.dx = dx;
                this.dy = dy;
            }
        }

        final double MAJOR = 2.0 * SIDE_LEN;
        final double MINOR = Math.sqrt(3.0) * SIDE_LEN;

        Map<Neighbor, Shift> flatShifts = new HashMap<Neighbor, Shift>();
        flatShifts.put(Neighbor.LOWER, new Shift(0.0, -MINOR));
        flatShifts.put(Neighbor.LOWER_LEFT, new Shift(-0.75 * MAJOR, -0.5 * MINOR));
        flatShifts.put(Neighbor.LOWER_RIGHT, new Shift(0.75 * MAJOR, -0.5 * MINOR));
        flatShifts.put(Neighbor.UPPER, new Shift(0.0, MINOR));
        flatShifts.put(Neighbor.UPPER_LEFT, new Shift(-0.75 * MAJOR, 0.5 * MINOR));
        flatShifts.put(Neighbor.UPPER_RIGHT, new Shift(0.75 * MAJOR, 0.5 * MINOR));

        Map<Neighbor, Shift> angledShifts = new HashMap<Neighbor, Shift>();
        angledShifts.put(Neighbor.LEFT, new Shift(-MINOR, 0.0));
        angledShifts.put(Neighbor.LOWER_LEFT, new Shift(-0.5 * MINOR, -0.75 * MAJOR));
        angledShifts.put(Neighbor.LOWER_RIGHT, new Shift(0.5 * MINOR, -0.75 * MAJOR));
        angledShifts.put(Neighbor.RIGHT, new Shift(MINOR, 0.0));
        angledShifts.put(Neighbor.UPPER_LEFT, new Shift(-0.5 * MINOR, 0.75 * MAJOR));
        angledShifts.put(Neighbor.UPPER_RIGHT, new Shift(0.5 * MINOR, 0.75 * MAJOR));

        Map<Orientation, Map<Neighbor, Shift>> table = new HashMap<Orientation, Map<Neighbor, Shift>>();
        table.put(Orientation.FLAT, flatShifts);
        table.put(Orientation.ANGLED, angledShifts);

        for (Orientation o : Orientation.values()) {
            Hexagon h0 = Hexagons.create(SIDE_LEN, 0.0, 0.0, o);

            for (Neighbor n : Neighbor.values()) {
                boolean expectEx = !Hexagons.isValidNeighbor(o, n);
                boolean gotEx = false;
                try {
                    hn = Hexagons.createNeighbor(h0, n);
                } catch (IllegalArgumentException ex) {
                    gotEx = true;
                }

                assertEquals("Failed for case " + o + " " + n, expectEx, gotEx);

                if (!gotEx) {
                    Shift shift = table.get(o).get(n);
                    assertNotNull("Error in test code", shift);
                    assertNeighborVertices(h0, hn, shift.dx, shift.dy);
                }
            }
        }
    }

    @Test
    public void createLattice() throws Exception {
        final SimpleFeatureType TYPE = DataUtilities.createType("hextype", "hexagon:Polygon,id:Integer");

        final Envelope bounds = new Envelope(0, 100, 0, 100);

        AttributeSetter attributeSetter = new AttributeSetter(TYPE) {
            private int id = 1;

            public void setAttributes(GridElement h, Map<String, Object> attributes) {
                attributes.put("id", id++);
            }
        };

        SimpleFeatureCollection lattice = Hexagons.createGrid(bounds, SIDE_LEN, Orientation.FLAT, attributeSetter);

        assertNotNull(lattice);
    }

    private void assertNeighborVertices(Hexagon h0, Hexagon h1, double dx, double dy) {
        Coordinate[] expected = h0.getVertices();
        Coordinate[] result = h1.getVertices();
        for (int i = 0; i < 6; i++) {
            expected[i].x += dx;
            expected[i].y += dy;
            assertCoordinate(expected[i], result[i]);
        }

    }
}
