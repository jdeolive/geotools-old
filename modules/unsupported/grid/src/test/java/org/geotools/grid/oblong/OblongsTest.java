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
package org.geotools.grid.oblong;

import com.vividsolutions.jts.geom.Coordinate;
import java.util.HashMap;
import java.util.Map;

import com.vividsolutions.jts.geom.Envelope;
import org.geotools.data.DataUtilities;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.grid.AttributeSetter;

import org.geotools.grid.GridElement;
import org.geotools.grid.Neighbor;
import org.geotools.grid.TestBase;

import org.junit.Test;
import org.opengis.feature.simple.SimpleFeatureType;
import static org.junit.Assert.*;

/**
 * Unit tests for the Oblongs class.
 *
 * @author mbedward
 * @since 2.7
 * @source $URL$
 * @version $Id$
 */
public class OblongsTest extends TestBase {

    @Test
    public void create() {
        GridElement oblong = Oblongs.create(1, 2, 3, 4);
        assertNotNull(oblong);
        assertEnvelope(new Envelope(1, 4, 2, 6), oblong.getBounds());
    }

    @Test(expected = IllegalArgumentException.class)
    public void badWidth() {
        Oblongs.create(1, 2, -1, 4);
    }

    @Test(expected = IllegalArgumentException.class)
    public void badHeight() {
        Oblongs.create(1, 2, 3, -1);
    }

    @Test
    public void createNeighbor() {
        Oblong neighbor = null;

        class Shift {

            double dx;
            double dy;

            public Shift(double dx, double dy) {
                this.dx = dx;
                this.dy = dy;
            }
        }

        final double WIDTH = 2.0;
        final double HEIGHT = 1.0;

        Map<Neighbor, Shift> shifts = new HashMap<Neighbor, Shift>();
        shifts.put(Neighbor.LOWER, new Shift(0.0, -HEIGHT));
        shifts.put(Neighbor.LOWER_LEFT, new Shift(-WIDTH, -HEIGHT));
        shifts.put(Neighbor.LOWER_RIGHT, new Shift(WIDTH, -HEIGHT));
        shifts.put(Neighbor.LEFT, new Shift(-WIDTH, 0.0));
        shifts.put(Neighbor.RIGHT, new Shift(WIDTH, 0.0));
        shifts.put(Neighbor.UPPER, new Shift(0.0, HEIGHT));
        shifts.put(Neighbor.UPPER_LEFT, new Shift(-WIDTH, HEIGHT));
        shifts.put(Neighbor.UPPER_RIGHT, new Shift(WIDTH, HEIGHT));

        Oblong oblong = Oblongs.create(0.0, 0.0, WIDTH, HEIGHT);

        for (Neighbor n : Neighbor.values()) {
            neighbor = Oblongs.createNeighbor(oblong, n);

            Shift shift = shifts.get(n);
            assertNotNull("Error in test code", shift);
            assertNeighbor(oblong, neighbor, shift.dx, shift.dy);
        }
    }

    @Test
    public void createGrid() throws Exception {
        final SimpleFeatureType TYPE = DataUtilities.createType("obtype", "oblong:Polygon,id:Integer");

        final double SPAN = 100;
        final Envelope bounds = new Envelope(0, SPAN, 0, SPAN);

        class Setter extends AttributeSetter {
            int id = 0;

            public Setter(SimpleFeatureType type) {
                super(type);
            }

            @Override
            public void setAttributes(GridElement el, Map<String, Object> attributes) {
                attributes.put("id", ++id);
            }
        }

        Setter setter = new Setter(TYPE);

        final double WIDTH = 5.0;
        final double HEIGHT = 10.0;
        SimpleFeatureCollection grid = Oblongs.createGrid(bounds, WIDTH, HEIGHT, setter);
        assertNotNull(grid);

        int expectedCols = (int) (SPAN / WIDTH);
        int expectedRows = (int) (SPAN / HEIGHT);

        assertEquals(expectedCols * expectedRows, setter.id);
        assertEquals(setter.id, grid.size());
    }

    private void assertNeighbor(Oblong refEl, Oblong neighbor, double dx, double dy) {
        Coordinate[] refCoords = refEl.getVertices();
        Coordinate[] neighborCoords = neighbor.getVertices();

        for (int i = 0; i < refCoords.length; i++) {
            refCoords[i].x += dx;
            refCoords[i].y += dy;
            assertCoordinate(refCoords[i], neighborCoords[i]);
        }
    }
}
