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

package org.geotools.grid;

import com.vividsolutions.jts.geom.Polygon;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.junit.Test;
import org.opengis.feature.simple.SimpleFeature;
import static org.junit.Assert.*;

/**
 * Unit tests for the Grids class.
 *
 * @author mbedward
 * @since 2.7
 * @source $URL$
 * @version $Id$
 */
public class GridsTest {
    private static final double TOL = 1.0e-8d;

    private final ReferencedEnvelope bounds = new ReferencedEnvelope(0, 90, 0, 100, null);
    private final double sideLen = 9.0;
    private final int expectedRows = (int) (bounds.getHeight() / sideLen);
    private final int expectedCols = (int) (bounds.getWidth() / sideLen);
    private final int expectedNumElements = expectedRows * expectedCols;
    private final ReferencedEnvelope expectedBounds = new ReferencedEnvelope(0, sideLen * expectedCols, 0, sideLen * expectedRows, null);

    @Test
    public void createUndensifiedSquareGrid() {
        SimpleFeatureCollection grid = Grids.createSquareGrid(bounds, sideLen);
        assertGridSizeAndIds(grid);

        SimpleFeatureIterator iter = grid.features();
        try {
            Polygon poly = (Polygon) iter.next().getAttribute("element");
            assertEquals(5, poly.getCoordinates().length);
        } finally {
            iter.close();
        }
    }

    @Test
    public void createDensifiedSquareGrid() {
        SimpleFeatureCollection grid = Grids.createSquareGrid(bounds, sideLen, sideLen / 10.0);
        assertEquals(expectedNumElements, grid.size());
        SimpleFeatureIterator iter = grid.features();
        try {
            Polygon poly = (Polygon) iter.next().getAttribute("element");
            assertTrue(poly.getCoordinates().length - 1 >= 4 * (int)(sideLen / 10.0));
        } finally {
            iter.close();
        }
    }

    private void assertGridSizeAndIds(SimpleFeatureCollection grid) {
        assertEquals(expectedNumElements, grid.size());
        assertTrue(expectedBounds.boundsEquals2D(grid.getBounds(), TOL));

        boolean[] flag = new boolean[expectedNumElements + 1];
        int count = 0;

        SimpleFeatureIterator iter = grid.features();
        try {
            while (iter.hasNext()) {
                SimpleFeature f = iter.next();
                int id = (Integer) f.getAttribute("id");
                assertFalse(id == 0);
                assertFalse(flag[id]);
                flag[id] = true;
                count++ ;
            }

        } finally {
            iter.close();
        }

        assertEquals(expectedNumElements, count);
    }

}
