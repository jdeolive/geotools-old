/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2003-2006, Geotools Project Managment Committee (PMC)
 *    (C) 2001, Institut de Recherche pour le Développement
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
import java.awt.image.Raster;
import java.awt.image.RenderedImage;
import java.io.Serializable;
import java.util.Arrays;

import org.opengis.geometry.Envelope;
import org.opengis.coverage.grid.GridRange;
import org.opengis.coverage.grid.GridCoordinates;
import org.opengis.referencing.datum.PixelInCell;

import org.geotools.resources.Classes;
import org.geotools.resources.i18n.Errors;
import org.geotools.resources.i18n.ErrorKeys;
import org.geotools.metadata.iso.spatial.PixelTranslation;


/**
 * Defines a range of grid coverage coordinates.
 *
 * @since 2.1
 * @source $URL$
 * @version $Id$
 * @author Martin Desruisseaux
 *
 * @see GridRange2D
 */
public class GeneralGridRange implements GridRange, Serializable {
    /**
     * Serial number for interoperability with different versions.
     */
    private static final long serialVersionUID = 1452569710967224145L;

    /**
     * The lower left corner. Will be created only when first needed.
     */
    private transient GeneralGridCoordinates lower;

    /**
     * The upper right corner. Will be created only when first needed.
     */
    private transient GeneralGridCoordinates upper;

    /**
     * Minimum and maximum grid ordinates. The first half contains minimum
     * ordinates, while the last half contains maximum ordinates.
     */
    private final int[] index;

    /**
     * Check if ordinate values in the minimum index are less than or
     * equal to the corresponding ordinate value in the maximum index.
     *
     * @throws IllegalArgumentException if an ordinate value in the minimum index is not
     *         less than or equal to the corresponding ordinate value in the maximum index.
     */
    private void checkCoherence() throws IllegalArgumentException {
        final int dimension = index.length/2;
        for (int i=0; i<dimension; i++) {
            final int lower = index[i];
            final int upper = index[dimension+i];
            if (!(lower <= upper)) {
                throw new IllegalArgumentException(Errors.format(
                        ErrorKeys.BAD_GRID_RANGE_$3, i, lower, upper));
            }
        }
    }

    /**
     * Constructs an initially empty grid range of the specified dimension.
     * This is used by {@link #getSubGridRange} before a grid range goes public.
     */
    private GeneralGridRange(final int dimension) {
        index = new int[dimension*2];
    }

    /**
     * Constructs one-dimensional grid range.
     *
     * @param lower The minimal inclusive value.
     * @param upper The maximal exclusive value.
     */
    public GeneralGridRange(final int lower, final int upper) {
        index = new int[] {lower, upper};
        checkCoherence();
    }

    /**
     * Constructs a new grid range.
     *
     * @param lower The valid minimum inclusive grid coordinate.
     *              The array contains a minimum value for each
     *              dimension of the grid coverage. The lowest
     *              valid grid coordinate is zero.
     * @param upper The valid maximum exclusive grid coordinate.
     *              The array contains a maximum value for each
     *              dimension of the grid coverage.
     *
     * @see #getLowers
     * @see #getUppers
     */
    public GeneralGridRange(final int[] lower, final int[] upper) {
        if (lower.length != upper.length) {
            throw new IllegalArgumentException(Errors.format(
                    ErrorKeys.MISMATCHED_DIMENSION_$2, lower.length, upper.length));
        }
        index = new int[lower.length + upper.length];
        System.arraycopy(lower, 0, index, 0,            lower.length);
        System.arraycopy(upper, 0, index, lower.length, upper.length);
        checkCoherence();
    }

    /**
     * Constructs two-dimensional range defined by a {@link Rectangle}.
     */
    public GeneralGridRange(final Rectangle rect) {
        this(rect, 2);
    }

    /**
     * Constructs multi-dimensional range defined by a {@link Rectangle}.
     * The two first dimensions are set to the
     * [{@linkplain Rectangle#x x} .. x+{@linkplain Rectangle#width width}] and
     * [{@linkplain Rectangle#y y} .. x+{@linkplain Rectangle#height height}]
     * ranges respectively. Extra dimensions (if any) are set to the [0..1] range.
     *
     * @param rect The rectangle.
     * @param dimension Number of dimensions for this grid range. Must be equals or greater than 2.
     *
     * @since 2.5
     */
    public GeneralGridRange(final Rectangle rect, final int dimension) {
        this(rect.x, rect.y, rect.width, rect.height, dimension);
    }

    /**
     * Constructs two-dimensional range defined by a {@link Raster}.
     */
    public GeneralGridRange(final Raster raster) {
        this(raster, 2);
    }

    /**
     * Constructs multi-dimensional range defined by a {@link Raster}.
     * The two first dimensions are set to the
     * [{@linkplain Raster#getMinX x} .. x+{@linkplain Raster#getWidth width}] and
     * [{@linkplain Raster#getMinY y} .. x+{@linkplain Raster#getHeight height}]
     * ranges respectively. Extra dimensions (if any) are set to the [0..1] range.
     *
     * @param raster The raster.
     * @param dimension Number of dimensions for this grid range. Must be equals or greater than 2.
     *
     * @since 2.5
     */
    public GeneralGridRange(final Raster raster, final int dimension) {
        this(raster.getMinX(), raster.getMinY(), raster.getWidth(), raster.getHeight(), dimension);
    }

    /**
     * Constructs two-dimensional range defined by a {@link RenderedImage}.
     */
    public GeneralGridRange(final RenderedImage image) {
        this(image, 2);
    }

    /**
     * Constructs multi-dimensional range defined by a {@link RenderedImage}.
     * The two first dimensions are set to the
     * [{@linkplain RenderedImage#getMinX x} .. x+{@linkplain RenderedImage#getWidth width}] and
     * [{@linkplain RenderedImage#getMinY y} .. x+{@linkplain RenderedImage#getHeight height}]
     * ranges respectively. Extra dimensions (if any) are set to the [0..1] range.
     *
     * @param image The image.
     * @param dimension Number of dimensions for this grid range. Must be equals or greater than 2.
     *
     * @since 2.5
     */
    public GeneralGridRange(final RenderedImage image, final int dimension) {
        this(image.getMinX(), image.getMinY(), image.getWidth(), image.getHeight(), dimension);
    }

    /**
     * Constructs a multi-dimensional range. We keep this constructor private because the arguments
     * can be confusing. Forcing usage of {@link Rectangle} in public API is probably safer.
     */
    private GeneralGridRange(int x, int y, int width, int height, int dimension) {
        if (dimension < 2) {
            throw new IllegalArgumentException(Errors.format(
                    ErrorKeys.ILLEGAL_ARGUMENT_$2, "dimension", dimension));
        }
        index = new int[dimension*2];
        index[0] = x;
        index[1] = y;
        index[dimension + 0] = x + width;
        index[dimension + 1] = y + height;
        Arrays.fill(index, dimension+2, index.length, 1);
        checkCoherence();
    }

    /**
     * @deprecated Replaced by {@code new GeneralGridRange(envelope, PixelInCell.CELL_CORNER)}.
     *
     * @since 2.2
     */
    @Deprecated
    public GeneralGridRange(final Envelope envelope) {
        this(envelope, PixelInCell.CELL_CORNER);
    }

    /**
     * Casts the specified envelope into a grid range. This is sometime useful after an
     * envelope has been transformed from "real world" coordinates to grid coordinates using the
     * {@linkplain org.opengis.coverage.grid.GridGeometry#getGridToCoordinateSystem grid to CRS}
     * transform. The floating point values are rounded toward the nearest integers.
     * <p>
     * <b>Note about rounding mode</b><br>
     * It would have been possible to round the {@linkplain Envelope#getMinimum minimal value}
     * toward {@linkplain Math#floor floor} and the {@linkplain Envelope#getMaximum maximal value}
     * toward {@linkplain Math#ceil ceil} in order to make sure that the grid range encompass fully
     * the envelope - like what Java2D does when converting {@link java.awt.geom.Rectangle2D} to
     * {@link Rectangle}). But this approach may increase by 1 or 2 units the image
     * {@linkplain RenderedImage#getWidth width} or {@linkplain RenderedImage#getHeight height}. For
     * example the range {@code [-0.25 ... 99.75]} (which is exactly 100 units wide) would be casted
     * to {@code [-1 ... 100]}, which is 101 units wide. This leads to unexpected results when using
     * grid range with image operations like "{@link javax.media.jai.operator.AffineDescriptor Affine}".
     * For avoiding such changes in size, it is necessary to use the same rounding mode for both
     * minimal and maximal values. The selected rounding mode is {@linkplain Math#round nearest
     * integer} in this implementation.
     * <p>
     * <b>Grid type</b><br>
     * According OpenGIS specification, {@linkplain org.opengis.coverage.grid.GridGeometry grid
     * geometry} maps pixel's center. But envelopes typically encompass all pixels. This means
     * that grid coordinates (0,0) has an envelope starting at (-0.5, -0.5). In order to revert
     * back such envelope to a grid range, it is necessary to add 0.5 to every coordinates
     * (including the maximum value since it is exclusive in a grid range). This offset is applied
     * only if {@code anchor} is {@link PixelInCell#CELL_CENTER}. Users who don't want such
     * offset should specify {@link PixelInCell#CELL_CORNER}.
     * <p>
     * The convention is specified as a {@link PixelInCell} code instead than the more detailed
     * {@link org.opengis.metadata.spatial.PixelOrientation} because the latter is restricted to
     * the two-dimensional case while the former can be used for any number of dimensions.
     *
     * @param envelope
     *          The envelope to use for initializing this grid range.
     * @param anchor
     *          Whatever envelope coordinates map to pixel center or pixel corner. Should be
     *          {@link PixelInCell#CELL_CENTER} if an offset of 0.5 should be added to every
     *          envelope coordinate values, or {@link PixelInCell#CELL_CORNER} if no offset
     *          should be applied.
     * @throws IllegalArgumentException
     *          If {@code anchor} is not valid.
     *
     * @since 2.5
     *
     * @see org.geotools.referencing.GeneralEnvelope#GeneralEnvelope(GridRange, PixelInCell,
     *      org.opengis.referencing.operation.MathTransform,
     *      org.opengis.referencing.crs.CoordinateReferenceSystem)
     */
    public GeneralGridRange(final Envelope envelope, final PixelInCell anchor)
            throws IllegalArgumentException
    {
        final double offset = PixelTranslation.getPixelTranslation(anchor) + 0.5;
        final int dimension = envelope.getDimension();
        index = new int[dimension * 2];
        for (int i=0; i<dimension; i++) {
            // See "note about conversion of floating point values to integers" in the JavaDoc.
            index[i            ] = (int) Math.round(envelope.getMinimum(i) + offset);
            index[i + dimension] = (int) Math.round(envelope.getMaximum(i) + offset);
        }
    }

    /**
     * Returns the number of dimensions.
     */
    public int getDimension() {
        return index.length / 2;
    }

    /**
     * Returns the valid minimum inclusive grid coordinate along the specified dimension.
     *
     * @see #getLowers
     */
    public int getLower(final int dimension) {
        if (dimension < index.length/2) {
            return index[dimension];
        }
        throw new ArrayIndexOutOfBoundsException(dimension);
    }

    /**
     * Returns the valid maximum exclusive grid coordinate along the specified dimension.
     *
     * @see #getUppers
     */
    public int getUpper(final int dimension) {
        if (dimension >= 0) {
            return index[dimension + index.length/2];
        }
        throw new ArrayIndexOutOfBoundsException(dimension);
    }

    /**
     * Returns the number of integer grid coordinates along the specified dimension.
     * This is equals to {@code getUpper(dimension)-getLower(dimension)}.
     */
    public int getLength(final int dimension) {
        return index[dimension+index.length/2] - index[dimension];
    }

    /**
     * Returns the valid minimum inclusive grid coordinate.
     * The sequence contains a minimum value for each dimension of the grid coverage.
     *
     * @since 2.4
     */
    public GridCoordinates getLower() {
        if (lower == null) {
            lower = new GeneralGridCoordinates.Immutable(index, 0, index.length/2);
        }
        return lower;
    }

    /**
     * Returns the valid maximum exclusive grid coordinate.
     * The sequence contains a maximum value for each dimension of the grid coverage.
     *
     * @since 2.4
     */
    public GridCoordinates getUpper() {
        if (upper == null) {
            upper = new GeneralGridCoordinates.Immutable(index, index.length/2, index.length);
        }
        return upper;
    }

    /**
     * Returns a new grid range that encompass only some dimensions of this grid range.
     * This method copy this grid range's index into a new grid range, beginning at
     * dimension {@code lower} and extending to dimension {@code upper-1}.
     * Thus the dimension of the subgrid range is {@code upper-lower}.
     *
     * @param  lower The first dimension to copy, inclusive.
     * @param  upper The last  dimension to copy, exclusive.
     * @return The subgrid range.
     * @throws IndexOutOfBoundsException if an index is out of bounds.
     */
    public GeneralGridRange getSubGridRange(final int lower, final int upper) {
        final int curDim = index.length/2;
        final int newDim = upper-lower;
        if (lower<0 || lower>curDim) {
            throw new IndexOutOfBoundsException(Errors.format(
                    ErrorKeys.ILLEGAL_ARGUMENT_$2, "lower", lower));
        }
        if (newDim<0 || upper>curDim) {
            throw new IndexOutOfBoundsException(Errors.format(
                    ErrorKeys.ILLEGAL_ARGUMENT_$2, "upper", upper));
        }
        final GeneralGridRange gridRange = new GeneralGridRange(newDim);
        System.arraycopy(index, lower,        gridRange.index, 0,      newDim);
        System.arraycopy(index, lower+curDim, gridRange.index, newDim, newDim);
        return gridRange;
    }

    /**
     * Returns a {@link Rectangle} with the same bounds as this {@code GeneralGridRange}.
     * This is a convenience method for interoperability with Java2D.
     *
     * @throws IllegalStateException if this grid range is not two-dimensional.
     */
    public Rectangle toRectangle() throws IllegalStateException {
        if (index.length == 4) {
            return new Rectangle(index[0], index[1], index[2]-index[0], index[3]-index[1]);
        } else {
            throw new IllegalStateException(Errors.format(
                    ErrorKeys.NOT_TWO_DIMENSIONAL_$1, getDimension()));
        }
    }

    /**
     * Returns a hash value for this grid range. This value need not remain
     * consistent between different implementations of the same class.
     */
    @Override
    public int hashCode() {
        int code = (int)serialVersionUID;
        if (index != null) {
            for (int i=index.length; --i>=0;) {
                code = code*31 + index[i];
            }
        }
        return code;
    }

    /**
     * Compares the specified object with this grid range for equality.
     */
    @Override
    public boolean equals(final Object object) {
        if (object instanceof GeneralGridRange) {
            final GeneralGridRange that = (GeneralGridRange) object;
            return Arrays.equals(this.index, that.index);
        }
        return false;
    }

    /**
     * Returns a string représentation of this grid range. The returned string is
     * implementation dependent. It is usually provided for debugging purposes.
     */
    @Override
    public String toString() {
        return toString(this);
    }

    /**
     * Returns a string représentation of the specified grid range.
     */
    static String toString(final GridRange range) {
        final int dimension = range.getDimension();
        final StringBuilder buffer = new StringBuilder(Classes.getShortClassName(range));
        buffer.append('[');
        for (int i=0; i<dimension; i++) {
            if (i != 0) {
                buffer.append(", ");
            }
            buffer.append(range.getLower(i)).append("..").append(range.getUpper(i));
        }
        return buffer.append(']').toString();
    }
}
