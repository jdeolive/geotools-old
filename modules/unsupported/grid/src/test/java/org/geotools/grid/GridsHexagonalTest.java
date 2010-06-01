/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
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
 *
 * @author michael
 */
public class GridsHexagonalTest {

    private final ReferencedEnvelope BOUNDS = new ReferencedEnvelope(0, 90, 0, 100, null);
    private final double SIDE_LEN = 5.0;
    
    private final int expectedCols = (int) ((BOUNDS.getWidth() - 2 * SIDE_LEN) / (1.5 * SIDE_LEN)) + 1;
    private final int expectedRows = (int) (BOUNDS.getHeight() / (Math.sqrt(3.0) * SIDE_LEN));
    private final int expectedNumElements = expectedRows * expectedCols;

    @Test
    public void createGrid() {
        SimpleFeatureCollection grid = Grids.createHexagonalGrid(BOUNDS, SIDE_LEN);
        assertGridSizeAndIds(grid);

        SimpleFeatureIterator iter = grid.features();
        try {
            SimpleFeature f = iter.next();
            Polygon poly = (Polygon) f.getDefaultGeometry();
            assertEquals(6, poly.getCoordinates().length - 1);
        } finally {
            iter.close();
        }
    }

    @Test
    public void createDensifiedGrid() {
        final int vertexDensity = 10;
        SimpleFeatureCollection grid = Grids.createHexagonalGrid(BOUNDS, SIDE_LEN, SIDE_LEN / vertexDensity);
        assertGridSizeAndIds(grid);

        SimpleFeatureIterator iter = grid.features();
        try {
            SimpleFeature f = iter.next();
            Polygon poly = (Polygon) f.getDefaultGeometry();
            assertTrue(poly.getCoordinates().length - 1 >= 6 * vertexDensity);
        } finally {
            iter.close();
        }
    }

    @Test(expected=IllegalArgumentException.class)
    public void createGrid_InvalidBounds() {
        Grids.createHexagonalGrid(ReferencedEnvelope.EVERYTHING, SIDE_LEN);
    }

    @Test(expected=IllegalArgumentException.class)
    public void createGrid_NullBounds() {
        Grids.createHexagonalGrid(null, SIDE_LEN);
    }

    @Test(expected=IllegalArgumentException.class)
    public void createGrid_InvalidSideLength() {
        Grids.createHexagonalGrid(BOUNDS, 0);
    }

    private void assertGridSizeAndIds(SimpleFeatureCollection grid) {
        assertEquals(expectedNumElements, grid.size());
        //assertTrue(expectedBounds.boundsEquals2D(grid.getBounds(), TOL));

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
