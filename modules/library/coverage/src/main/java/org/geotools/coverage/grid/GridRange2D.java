/*
 *    GeoTools - The Open Source Java GIS Tookit
 *    http://geotools.org
 * 
 *    (C) 2007-2008, Open Source Geospatial Foundation (OSGeo)
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

import java.awt.Rectangle;
import org.opengis.util.Cloneable;
import org.opengis.coverage.grid.GridRange;


/**
 * Defines a range of two-dimensional grid coverage coordinates. This implementation extends
 * {@link Rectangle} for interoperability with Java2D. Note that at the opposite of
 * {@link GeneralGridRange}, this class is mutable.
 *
 * @since 2.5
 * @source $URL$
 * @version $Id$
 * @author Martin Desruisseaux
 *
 * @see GeneralGridRange
 */
public class GridRange2D extends Rectangle implements GridRange, Cloneable {
    /**
     * For cross-version interoperability.
     */
    private static final long serialVersionUID = 6899195945793291045L;

    /**
     * Creates an initially empty grid range.
     */
    public GridRange2D() {
    }

    /**
     * Creates a grid range initialized to the specified rectangle.
     */
    public GridRange2D(final Rectangle rectangle) {
        super(rectangle);
    }

    /**
     * Creates a grid range initialized to the specified rectangle.
     */
    public GridRange2D(final int x, final int y, final int width, final int height) {
        super(x, y, width, height);
    }

    /**
     * Returns the number of dimensions, which is always 2.
     */
    public final int getDimension() {
        return 2;
    }

    /**
     * Returns the valid minimum inclusive grid coordinate along the specified dimension.
     */
    public int getLower(final int dimension) {
        switch (dimension) {
            case 0:  return x;
            case 1:  return y;
            default: throw new IndexOutOfBoundsException(GridCoordinates2D.indexOutOfBounds(dimension));
        }
    }

    /**
     * Returns the valid maximum exclusive grid coordinate along the specified dimension.
     */
    public int getUpper(final int dimension) {
        switch (dimension) {
            case 0:  return x + width;
            case 1:  return y + height;
            default: throw new IndexOutOfBoundsException(GridCoordinates2D.indexOutOfBounds(dimension));
        }
    }

    /**
     * Returns the number of integer grid coordinates along the specified dimension.
     * This is equals to {@code getUpper(dimension)-getLower(dimension)}.
     */
    public int getLength(final int dimension) {
        switch (dimension) {
            case 0:  return width;
            case 1:  return height;
            default: throw new IndexOutOfBoundsException(GridCoordinates2D.indexOutOfBounds(dimension));
        }
    }

    /**
     * Returns the valid minimum inclusive grid coordinate.
     */
    public GridCoordinates2D getLower() {
        return new GridCoordinates2D(x, y);
    }

    /**
     * Returns the valid maximum exclusive grid coordinate.
     */
    public GridCoordinates2D getUpper() {
        return new GridCoordinates2D(x + width, y + height);
    }

    // Inherit 'hashCode()' and 'equals' from Rectangle2D, which provides an implementation
    // aimed to be common for every Rectangle2D subclasses (not just the Java2D ones) -  we
    // don't want to change this behavior in order to stay consistent with Java2D.

    /**
     * Returns a string représentation of this grid range. The returned string is
     * implementation dependent. It is usually provided for debugging purposes.
     */
    @Override
    public String toString() {
        return GeneralGridRange.toString(this);
    }

    /**
     * Returns a clone of this grid range.
     */
    @Override
    public GridRange2D clone() {
        return (GridRange2D) super.clone();
    }
}
