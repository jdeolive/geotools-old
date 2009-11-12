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

package org.geotools.swing.tool;

import java.util.ArrayList;
import java.util.List;
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.grid.io.AbstractGridCoverage2DReader;
import org.geotools.geometry.DirectPosition2D;
import org.geotools.geometry.jts.ReferencedEnvelope;

/**
 * Helper class used by {@code InfoTool} to query {@code MapLayers}
 * with raster feature data ({@code GridCoverage2D} or {@code AbstractGridCoverage2DReader}).
 *
 * @see InfoTool
 * @see VectorLayerHelper
 *
 * @author Michael Bedward
 * @since 2.6
 * @source $Id$
 * @version $URL$
 */
public class GridLayerHelper extends InfoToolHelper<List<Number>> {
    protected final GridCoverage2D cov;

    /**
     * Create a new helper to work with the given raster data source.
     *
     * @param rasterSource an instance of either
     *        {@code GridCoverage2D} or {@code AbstractGridCoverage2DReader
     */
    public GridLayerHelper(Object rasterSource) {
        super(Type.GRID_HELPER);

        System.out.println("Creating GridLayerHelper instance");

        try {
            if (AbstractGridCoverage2DReader.class.isAssignableFrom(rasterSource.getClass())) {
                this.cov = ((AbstractGridCoverage2DReader) rasterSource).read(null);
            } else {
                this.cov = (GridCoverage2D) rasterSource;
            }
        } catch (Exception ex) {
            throw new IllegalArgumentException(ex);
        }
    }

    /**
     * Get band values at the given position
     *
     * @param pos the location to query
     *
     * @param params currently ignored
     *
     * @return a {@code List} of band values; will be empty if {@code pos} was
     *         outside the coverage bounds
     *
     * @throws Exception if the grid coverage could not be queried
     */
    @Override
    public List<Number> getInfo(DirectPosition2D pos, Object ...params) 
            throws Exception {
        
        List<Number> list = new ArrayList<Number>();

        ReferencedEnvelope env = new ReferencedEnvelope(this.cov.getEnvelope2D());
        if (env.contains(pos)) {
            Object objArray = cov.evaluate(pos);
            Number[] bandValues = asNumberArray(objArray);
            if (bandValues != null) {
                for (Number value : bandValues) {
                    list.add(value);
                }
            }
        }

        return list;
    }

    /**
     * Convert the Object returned by {@linkplain GridCoverage2D#evaluate(DirectPosition)} into
     * an array of {@code Numbers}.
     *
     * @param objArray an Object representing a primitive array
     *
     * @return a new array of Numbers
     */
    private Number[] asNumberArray(Object objArray) {
        Number[] numbers = null;

        if (objArray instanceof byte[]) {
            byte[] values = (byte[]) objArray;
            numbers = new Number[values.length];
            for (int i = 0; i < values.length; i++) {
                numbers[i] = ((int)values[i]) & 0xff;
            }

        } else if (objArray instanceof int[]) {
            int[] values = (int[]) objArray;
            numbers = new Number[values.length];
            for (int i = 0; i < values.length; i++) {
                numbers[i] = values[i];
            }

        } else if (objArray instanceof float[]) {
            float[] values = (float[]) objArray;
            numbers = new Number[values.length];
            for (int i = 0; i < values.length; i++) {
                numbers[i] = values[i];
            }
        } else if (objArray instanceof double[]) {
            double[] values = (double[]) objArray;
            numbers = new Number[values.length];
            for (int i = 0; i < values.length; i++) {
                numbers[i] = values[i];
            }
        }

        return numbers;
    }

}
