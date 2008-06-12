/*
 *    GeoTools - The Open Source Java GIS Tookit
 *    http://geotools.org
 * 
 *    (C) 2007-2008, Open Source Geospatial Foundation (OSGeo)
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
package org.geotools.image.io.metadata;

import org.opengis.geometry.Envelope;                   // For javadoc
import org.opengis.coverage.grid.GridRange;             // For javadoc
import org.opengis.metadata.spatial.PixelOrientation;   // For javadoc

import org.geotools.util.NumberRange;
import org.geotools.resources.i18n.ErrorKeys;


/**
 * A {@code <GridGeometry>} element in
 * {@linkplain GeographicMetadataFormat geographic metadata format}. This class offers similar
 * service than {@link GridRange} and {@link Envelope}, except that the maximum value for
 * {@linkplain #getGridRange grid range} and {@linkplain #getCoordinateRange coordinate range}
 * are inclusives.
 * <p>
 * The {@code <GridRange>} child element is typically (but not always) initialized
 * to the following ranges:
 * <p>
 * <li>
 *   <ul>[0 .. {@linkplain java.awt.image.RenderedImage#getWidth image width} - 1]</ul>
 *   <ul>[0 .. {@linkplain java.awt.image.RenderedImage#getHeight image height} - 1]</ul>
 * </li>
 * <p>
 * However <var>n</var>-dimensional grid coverages may contains additional entries.
 *
 * @since 2.4
 * @source $URL$
 * @version $Id$
 * @author Martin Desruisseaux
 */
public class ImageGeometry extends MetadataAccessor {
    /**
     * The {@code "GridGeometry/GridRange"} node.
     */
    private final MetadataAccessor gridRange;

    /**
     * The {@code "GridGeometry/Envelope"} node.
     */
    private final MetadataAccessor envelope;

    /**
     * Creates a parser for a grid geometry. This constructor should not be invoked
     * directly; use {@link GeographicMetadata#getGeometry} instead.
     *
     * @param metadata The metadata node.
     */
    protected ImageGeometry(final GeographicMetadata metadata) {
        super(metadata, "GridGeometry", null);
        envelope  = new MetadataAccessor(metadata, "GridGeometry/Envelope",  "CoordinateValues");
        gridRange = new MetadataAccessor(metadata, "GridGeometry/GridRange", "IndexRange");
    }

    /**
     * Returns the number of dimensions. If the {@linkplain GridRange grid range} and
     * {@linkplain Envelope envelope} don't have the same dimension, then a warning
     * is logged and the smallest dimension is returned.
     */
    public int getDimension() {
        final int dim1 = gridRange.childCount();
        final int dim2  = envelope .childCount();
        if (dim1 != dim2) {
            warning("getDimension", ErrorKeys.MISMATCHED_DIMENSION_$2, new Integer[] {dim1, dim2});
        }
        return Math.min(dim1, dim2);
    }

    /**
     * Returns the range of grid index along the specified dimension. Note that range
     * {@linkplain NumberRange#getMinValue minimum value},
     * {@linkplain NumberRange#getMaxValue maximum value} or both may be null if no
     * {@code "minimum"} or {@code "maximum"} attribute were found for the
     * {@code "GridGeometry/GridRange/IndexRange"} element.
     *
     * @param dimension The dimension index, from 0 inclusive to {@link #getDimension} exclusive.
     */
    public NumberRange getGridRange(final int dimension) {
        gridRange.selectChild(dimension);
        final Integer minimum = gridRange.getInteger("minimum");
        final Integer maximum = gridRange.getInteger("maximum");
        return new NumberRange(Integer.class, minimum, true, maximum, true);
    }

    /**
     * Set the grid range along the specified dimension.
     *
     * @param dimension The dimension to set, from 0 inclusive to {@link #getDimension} exclusive.
     * @param minimum   The minimum value along the specified dimension (inclusive).
     * @param maximum   The maximum value along the specified dimension (<strong>inclusive</strong>).
     */
    public void setGridRange(final int dimension, final int minimum, final int maximum) {
        gridRange.selectChild(dimension);
        gridRange.setInteger("minimum", minimum);
        gridRange.setInteger("maximum", maximum);
    }

    /**
     * Returns the range of coordinate values along the specified dimension. Note that range
     * {@linkplain NumberRange#getMinValue minimum value},
     * {@linkplain NumberRange#getMaxValue maximum value} or both may be null if no
     * {@code "minimum"} or {@code "maximum"} attribute were found for the
     * {@code "GridGeometry/Envelope/CoordinateValues"} element.
     *
     * @param dimension The dimension index, from 0 inclusive to {@link #getDimension} exclusive.
     */
    public NumberRange getCoordinateRange(final int dimension) {
        envelope.selectChild(dimension);
        final Double minimum = envelope.getDouble("minimum");
        final Double maximum = envelope.getDouble("maximum");
        return new NumberRange(Double.class, minimum, true, maximum, true);
    }

    /**
     * Set the envelope range along the specified dimension.
     *
     * @param dimension The dimension to set, from 0 inclusive to {@link #getDimension} exclusive.
     * @param minimum   The minimum value along the specified dimension (inclusive).
     * @param maximum   The maximum value along the specified dimension (<strong>inclusive</strong>).
     */
    public void setCoordinateRange(final int dimension, final double minimum, final double maximum) {
        envelope.selectChild(dimension);
        envelope.setDouble("minimum", minimum);
        envelope.setDouble("maximum", maximum);
    }

    /**
     * Adds the range of values for an envelope along a dimension. Invoking this method
     * will increase the grid and envelope {@linkplain #getDimension dimension} by one.
     * The ranges should be added in the same order than
     * {@linkplain GeographicMetadata#addAxis axis}.
     *
     * @param minIndex The minimal index value, inclusive. This is usually 0.
     * @param maxIndex The maximal index value, <strong>inclusive</strong>.
     * @param minValue The minimal coordinate value, inclusive.
     * @param maxValue The maximal coordinate value, <strong>inclusive</strong>.
     *
     * @see #addCoordinateValues
     */
    public void addCoordinateRange(final int    minIndex, final int    maxIndex,
                                   final double minValue, final double maxValue)
    {
        setGridRange      (gridRange.appendChild(), minIndex, maxIndex);
        setCoordinateRange(envelope .appendChild(), minValue, maxValue);
    }

    /**
     * Returns the coordinate values along the specified dimension, or {@code null} if none.
     * This method returns a non-null values only if an array of was explicitly specified,
     * for example by a call to {@link #setCoordinateValues}.
     *
     * @param dimension The dimension index, from 0 inclusive to {@link #getDimension} exclusive.
     */
    public double[] getCoordinateValues(final int dimension) {
        envelope.selectChild(dimension);
        return (double[]) envelope.getUserObject();
    }

    /**
     * Set the envelope coordinate values along the specified dimension. The minimum and
     * maximum coordinates will be determined from the specified array.
     *
     * @param dimension The dimension to set, from 0 inclusive to {@link #getDimension} exclusive.
     * @param values The coordinate values.
     */
    public void setCoordinateValues(final int dimension, final double[] values) {
        double minimum = Double.POSITIVE_INFINITY;
        double maximum = Double.NEGATIVE_INFINITY;
        if (values != null) {
            for (int i=0; i<values.length; i++) {
                final double value = values[i];
                if (value < minimum) minimum = value;
                if (value > maximum) maximum = value;
            }
        }
        setCoordinateRange(dimension, minimum, maximum);
        envelope.setUserObject(values);
    }

    /**
     * Adds coordinate values for an envelope along a dimension. Invoking this method
     * will increase the envelope {@linkplain #getDimension dimension} by one. This method
     * may be invoked in replacement of {@link #addCoordinateRange} when every cell
     * coordinates need to be specified explicitly.
     *
     * @param minIndex The minimal index value, inclusive. This is usually 0.
     * @param values The coordinate values.
     *
     * @see #addCoordinateRange
     */
    public void addCoordinateValues(final int minIndex, final double[] values) {
        setGridRange(gridRange.appendChild(), minIndex, minIndex + values.length - 1);
        setCoordinateValues(envelope.appendChild(), values);
    }

    /**
     * Returns the point in a pixel corresponding to the Earth location of the pixel. In the JAI
     * framework, this is typically the {@linkplain PixelOrientation#UPPER_LEFT upper left} corner.
     * In some OGC specifications, this is often the pixel {@linkplain PixelOrientation#CENTER center}.
     *
     * @param pixelOrientation The pixel orientation (usually {@code "center"},
     *        {@code "lower left"}, {@code "lower right"}, {@code "upper right"}
     *        or {@code "upper left"}), or {@code null} if unknown.
     *
     * @see PixelOrientation
     */
    public String getPixelOrientation() {
        return getString("pixelOrientation");
    }

    /**
     * Set the pixel orientation to the specified value. The pixel orientation gives the point
     * in a pixel corresponding to the Earth location of the pixel. In the JAI framework, this
     * is typically the {@linkplain PixelOrientation#UPPER_LEFT upper left} corner. In some OGC
     * specifications, this is often the pixel {@linkplain PixelOrientation#CENTER center}.
     *
     * @param pixelOrientation The pixel orientation (usually {@code "center"},
     *        {@code "lower left"}, {@code "lower right"}, {@code "upper right"}
     *        or {@code "upper left"}), or {@code null} if unknown.
     *
     * @see PixelOrientation
     */
    public void setPixelOrientation(final String pixelOrientation) {
        setEnum("pixelOrientation", pixelOrientation, GeographicMetadataFormat.PIXEL_ORIENTATIONS);
    }
}
