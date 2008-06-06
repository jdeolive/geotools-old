/*
 *    GeoTools - The Open Source Java GIS Tookit
 *    http://geotools.org
 * 
 *    (C) 2006-2008, Open Source Geospatial Foundation (OSGeo)
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *
 *    This package contains documentation from OpenGIS specifications.
 *    OpenGIS consortium's work is fully acknowledged here.
 */
package org.geotools.coverage.grid;

import java.io.Serializable;
import java.util.Arrays;
import org.opengis.coverage.grid.Grid;      // For javadoc
import org.opengis.coverage.grid.GridPoint; // For javadoc
import org.opengis.coverage.grid.GridCoordinates;
import org.geotools.resources.Classes;


/**
 * Holds the set of grid coordinates that specifies the location of the
 * {@linkplain GridPoint grid point} within the {@linkplain Grid grid}.
 *
 * @since 2.4
 * @source $URL$
 * @version $Id$
 * @author Martin Desruisseaux
 *
 * @see GridCoordinates2D
 */
public class GeneralGridCoordinates implements GridCoordinates, Serializable {
    /**
     * For cross-version compatibility.
     */
    private static final long serialVersionUID = 8146318677770695383L;

    /**
     * The grid coordinates.
     */
    private final int[] coordinates;

    /**
     * Creates a grid coordinates of the specified dimension.
     * All coordinates are initially set to 0.
     */
    public GeneralGridCoordinates(final int dimension) {
        coordinates = new int[dimension];
    }

    /**
     * Creates a grid coordinates initialized to the specified values.
     */
    public GeneralGridCoordinates(final int[] coordinates) {
        this.coordinates = coordinates.clone();
    }

    /**
     * Creates a grid coordinates initialized to the specified values
     * in the specified range.
     */
    GeneralGridCoordinates(final int[] coordinates, final int lower, final int upper) {
        final int length = upper - lower;
        this.coordinates = new int[length];
        System.arraycopy(coordinates, lower, this.coordinates, 0, length);
    }

    /**
     * Creates a grid coordinates which is a copy of the specified one.
     *
     * @since 2.5
     */
    public GeneralGridCoordinates(final GridCoordinates coordinates) {
        this.coordinates = coordinates.getCoordinateValues();
    }

    /**
     * Returns the number of dimensions. This method is equivalent to
     * <code>{@linkplain #getCoordinateValues()}.length</code>. It is
     * provided for efficienty.
     */
    public int getDimension() {
        return coordinates.length;
    }

    /**
     * Returns one integer value for each dimension of the grid. The ordering of these coordinate
     * values shall be the same as that of the elements of {@link Grid#getAxisNames}. The value of
     * a single coordinate shall be the number of offsets from the origin of the grid in the
     * direction of a specific axis.
     *
     * @return A copy of the coordinates. Changes in the returned array will not be reflected
     *         back in this {@code GeneralGridCoordinates} object.
     */
    public int[] getCoordinateValues() {
        return coordinates.clone();
    }

    /**
     * Returns the coordinate value at the specified dimension. This method is equivalent to
     * <code>{@linkplain #getCoordinateValues()}[<var>i</var>]</code>. It is provided for
     * efficienty.
     *
     * @param  dimension The dimension from 0 inclusive to {@link #getDimension} exclusive.
     * @return The value at the requested dimension.
     * @throws ArrayIndexOutOfBoundsException if the specified dimension is out of bounds.
     */
    public int getCoordinateValue(final int dimension) throws ArrayIndexOutOfBoundsException {
        return coordinates[dimension];
    }

    /**
     * Sets the coordinate value at the specified dimension (optional operation).
     *
     * @param  dimension The index of the value to set.
     * @param  value The new value.
     * @throws ArrayIndexOutOfBoundsException if the specified dimension is out of bounds.
     * @throws UnsupportedOperationException if this grid coordinates is not modifiable.
     */
    public void setCoordinateValue(final int dimension, final int value)
            throws ArrayIndexOutOfBoundsException, UnsupportedOperationException
    {
        coordinates[dimension] = value;
    }

    /**
     * Returns a string representation of the specified grid coordinates.
     */
    static String toString(final GridCoordinates coordinates) {
        final StringBuilder buffer = new StringBuilder(Classes.getShortClassName(coordinates));
        final int dimension = coordinates.getDimension();
        for (int i=0; i<dimension; i++) {
            buffer.append(i==0 ? '[' : ',').append(coordinates.getCoordinateValue(i));
        }
        return buffer.append(']').toString();
    }

    /**
     * Returns a string representation of this grid coordinates.
     */
    @Override
    public String toString() {
        return toString(this);
    }

    /**
     * Returns a hash code value for this object.
     *
     * @todo Use {@link Arrays#hashCode(int[])} when we will be allowed to compile for J2SE 1.5.
     */
    @Override
    public int hashCode() {
        int code = (int) serialVersionUID;
        for (int i=0; i<coordinates.length; i++) {
            code = code * 37 + coordinates[i];
        }
        return code;
    }

    /**
     * Compares this grid coordinates with the specified object for equality.
     */
    @Override
    public boolean equals(final Object object) {
        if (object == this) {
            // Slight optimization.
            return true;
        }
        if (object!=null && object.getClass().equals(getClass())) {
            final GeneralGridCoordinates that = (GeneralGridCoordinates) object;
            return Arrays.equals(this.coordinates, that.coordinates);
        }
        return false;
    }

    /**
     * Returns a clone of this coordinates.
     */
    @Override
    public GeneralGridCoordinates clone() {
        return new GeneralGridCoordinates(coordinates);
    }

    /**
     * An immutable flavor of {@link GridCoordinates}.
     */
    static final class Immutable extends GeneralGridCoordinates {
        /**
         * For cross-version compatibility.
         */
        private static final long serialVersionUID = -7723383411061425866L;

        /**
         * Creates a grid coordinates initialized to the specified values
         * in the specified range.
         */
        Immutable(final int[] coordinates, final int lower, final int upper) {
            super(coordinates, lower, upper);
        }

        /**
         * Do not allows modification of this grid coordinates.
         *
         * @throws UnsupportedOperationException always thrown.
         */
        @Override
        public void setCoordinateValue(final int i, final int value)
                throws UnsupportedOperationException
        {
            throw new UnsupportedOperationException();
        }
    }
}
