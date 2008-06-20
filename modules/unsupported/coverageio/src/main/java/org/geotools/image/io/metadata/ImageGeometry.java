/*
 *    GeoTools - The Open Source Java GIS Toolkit
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

import java.util.Arrays;
import org.geotools.resources.XArray;
import org.opengis.geometry.Envelope;
import org.opengis.coverage.grid.GridRange;
import org.opengis.metadata.spatial.PixelOrientation;

import org.geotools.util.NumberRange;
import org.geotools.resources.i18n.ErrorKeys;


/**
 * A combinaison of {@code <Envelope>} and {@code <RectifiedGrid>} elements in
 * {@linkplain GeographicMetadataFormat geographic metadata format}. This class offers similar
 * service than {@link Envelope} and {@link GridRange}, except that the maximum value for
 * {@linkplain #getCoordinateRange coordinate range} and {@linkplain #getGridRange grid range}
 * are inclusives.
 * <p>
 * The {@code <GridEnvelope>} child element is typically (but not always) initialized
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
 * @author Cédric Briançon
 */
public class ImageGeometry extends MetadataAccessor {
    /**
     * The {@code "boundedBy/lowerCorner"} node.
     */
    private final MetadataAccessor lowerCorner;

    /**
     * The {@code "boundedBy/upperCorner"} node.
     */
    private final MetadataAccessor upperCorner;

    /**
     * The {@code "rectifiedGridDomain/cells"} node.
     */
    private final MetadataAccessor cells;

    /**
     * The {@code "rectifiedGridDomain/crs/cs"} node.
     */
    private final MetadataAccessor cs;

    /**
     * The {@code "rectifiedGridDomain/limits/low"} node.
     */
    private final MetadataAccessor low;

    /**
     * The {@code "rectifiedGridDomain/limits/high"} node.
     */
    private final MetadataAccessor high;

    /**
     * The {@code "rectifiedGridDomain/localizationGrid"} node.
     */
    private final MetadataAccessor localizationGrid;

    /**
     * The {@code "rectifiedGridDomain/pixelOrientation"} node.
     */
    private final MetadataAccessor pixelOrientation;

    /**
     * Creates a parser for a grid geometry. This constructor should not be invoked
     * directly; use {@link GeographicMetadata#getGeometry} instead.
     *
     * @param metadata The metadata node.
     */
    protected ImageGeometry(final GeographicMetadata metadata) {
        super(metadata, "rectifiedGridDomain", null);
        lowerCorner      = new MetadataAccessor(metadata,
                "boundedBy/lowerCorner", null);
        upperCorner      = new MetadataAccessor(metadata,
                "boundedBy/upperCorner", null);
        cs               = new MetadataAccessor(metadata,
                "rectifiedGridDomain/crs/cs", "axis");
        cells            = new MetadataAccessor(metadata,
                "rectifiedGridDomain/cells", "offsetVector");
        low              = new MetadataAccessor(metadata,
                "rectifiedGridDomain/limits/low", null);
        high             = new MetadataAccessor(metadata,
                "rectifiedGridDomain/limits/high", null);
        localizationGrid = new MetadataAccessor(metadata,
                "rectifiedGridDomain/localizationGrid", "ordinates");
        pixelOrientation = new MetadataAccessor(metadata,
                "rectifiedGridDomain/pixelOrientation", null);
    }

    /**
     * Returns the number of dimensions. If the {@linkplain CoordinateSystem coordinate system}
     * and the cells don't have the same dimension, then a warning is logged and
     * the smallest dimension is returned.
     * If one of them is empty, the dimension of the oter one is then returned.
     */
    public int getDimension() {
        final int dim1 = cs   .childCount();
        final int dim2 = cells.childCount();
        if (dim2 == 0) {
            return dim1;
        }
        if (dim1 == 0) {
            return dim2;
        }
        if (dim1 != dim2) {
            warning("getDimension", ErrorKeys.MISMATCHED_DIMENSION_$2,
                    new int[] {dim1, dim2});
        }
        return Math.min(dim1, dim2);
    }

    /**
     * Returns the range of grid index along the specified dimension. Note that range
     * {@linkplain NumberRange#getMinValue minimum value},
     * {@linkplain NumberRange#getMaxValue maximum value} or both may be null if no
     * {@code "low"} or {@code "high"} attribute were found for the
     * {@code "rectifiedGridDomain/limits"} element.
     *
     * @param dimension The dimension index, from 0 inclusive to {@link #getDimension} exclusive.
     */
    public NumberRange getGridRange(final int dimension) {
        final int minimum = low. getUserObject(int[].class)[dimension];
        final int maximum = high.getUserObject(int[].class)[dimension];
        return new NumberRange(Integer.class, minimum, true, maximum, true);
    }

    /**
     * Set the grid range along the specified dimension. If the dimension is greater
     * than the current envelope dimension, then this dimension is added.
     *
     * @param dimension The dimension to set. It can eventually be greater than {@link #getDimension}.
     * @param minimum   The minimum value along the specified dimension (inclusive).
     * @param maximum   The maximum value along the specified dimension (<strong>inclusive</strong>).
     */
    public void setGridRange(final int dimension, final int minimum, final int maximum) {
        int[] lows  = low. getUserObject(int[].class);
        int[] highs = high.getUserObject(int[].class);
        final int length = dimension + 1;
        if (lows == null) {
            lows = new int[length];
        } else {
            final int oldLength = lows.length;
            if (length > oldLength) {
                lows = XArray.resize(lows, length);
            }
        }
        if (highs == null) {
            highs = new int[length];
        } else {
            final int oldLength = highs.length;
            if (length > oldLength) {
                highs = XArray.resize(highs, length);
            }
        }
        lows[dimension]  = minimum;
        highs[dimension] = maximum;
        low. setUserObject(lows);
        high.setUserObject(highs);
    }

    /**
     * Returns the range of coordinate values along the specified dimension. Note that range
     * {@linkplain NumberRange#getMinValue minimum value},
     * {@linkplain NumberRange#getMaxValue maximum value} or both may be null if no
     * {@code "lowerCorner"} or {@code "upperCorner"} attribute were found for the
     * {@code "boundedBy/Envelope"} element.
     *
     * @param dimension The dimension index, from 0 inclusive to {@link #getDimension} exclusive.
     */
    public NumberRange getCoordinateRange(final int dimension) {
        final double lower = lowerCorner.getUserObject(double[].class)[dimension];
        final double upper = upperCorner.getUserObject(double[].class)[dimension];
        return new NumberRange(Double.class, lower, true, upper, true);
    }

    /**
     * Set the envelope range along the specified dimension. If the dimension is greater
     * than the current envelope dimension, then this dimension is added.
     *
     * @param dimension The dimension to set. It can eventually be greater than {@link #getDimension}.
     * @param minimum   The minimum value along the specified dimension (inclusive).
     * @param maximum   The maximum value along the specified dimension (<strong>inclusive</strong>).
     */
    public void setCoordinateRange(final int dimension, final double minimum,
                                                        final double maximum)
    {
        double[] lowers = lowerCorner.getUserObject(double[].class);
        double[] uppers = upperCorner.getUserObject(double[].class);
        final int length = dimension + 1;
        if (lowers == null) {
            lowers = new double[length];
            Arrays.fill(lowers, Double.NaN);
        } else {
            final int oldLength = lowers.length;
            if (length > oldLength) {
                lowers = XArray.resize(lowers, length);
                Arrays.fill(lowers, oldLength, length, Double.NaN);
            }
        }
        if (uppers == null) {
            uppers = new double[length];
            Arrays.fill(uppers, Double.NaN);
        } else {
            final int oldLength = uppers.length;
            if (length > oldLength) {
                uppers = XArray.resize(uppers, length);
                Arrays.fill(uppers, oldLength, length, Double.NaN);
            }
        }
        lowers[dimension]  = minimum;
        uppers[dimension]  = maximum;
        lowerCorner.setUserObject(lowers);
        upperCorner.setUserObject(uppers);
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
        int last;
        int[]    lows   = low        .getUserObject(int[]   .class);
        int[]    highs  = high       .getUserObject(int[]   .class);
        double[] lowers = lowerCorner.getUserObject(double[].class);
        double[] uppers = upperCorner.getUserObject(double[].class);
        if (lows == null) {
            last = 0;
            lows = new int[1];
        } else {
            last = lows.length;
            lows = XArray.resize(lows, last + 1);
        }
        lows[last] = minIndex;
        if (highs == null) {
            last = 0;
            highs = new int[1];
        } else {
            last = highs.length;
            highs = XArray.resize(highs, last + 1);
        }
        highs[last] = maxIndex;
        if (lowers == null) {
            last = 0;
            lowers = new double[1];
        } else {
            last = lowers.length;
            lowers = XArray.resize(lowers, last + 1);
        }
        lowers[last] = minValue;
        if (uppers == null) {
            last = 0;
            uppers = new double[1];
        } else {
            last = uppers.length;
            uppers = XArray.resize(uppers, last + 1);
        }
        uppers[last] = maxValue;
        low        .setUserObject(lows);
        high       .setUserObject(highs);
        lowerCorner.setUserObject(lowers);
        upperCorner.setUserObject(uppers);
    }

    /**
     * Returns the ordinate values along the specified dimension, or {@code null} if none.
     * This method returns a non-null values only if an array of was explicitly specified,
     * for example by a call to {@link #setCoordinateValues}.
     *
     * @param dimension The dimension index, from 0 inclusive to {@link #getDimension} exclusive.
     */
    public double[] getOrdinates(final int dimension) {
        localizationGrid.selectChild(dimension);
        return (double[]) localizationGrid.getUserObject();
    }

    /**
     * Set the ordinate values along the specified dimension. The minimum and
     * maximum coordinates will be determined from the specified array.
     *
     * @param dimension The dimension to set, from 0 inclusive to {@link #getDimension} exclusive.
     * @param values The coordinate values.
     */
    public void setOrdinates(final int dimension, final double[] values) {
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
        localizationGrid.selectChild(dimension);
        localizationGrid.setUserObject(values);
    }

    /**
     * Adds ordinate values for an envelope along a dimension. Invoking this method
     * will increase the envelope {@linkplain #getDimension dimension} by one. This method
     * may be invoked in replacement of {@link #addCoordinateRange} when every cell
     * coordinates need to be specified explicitly.
     *
     * @param minIndex The minimal index value, inclusive. This is usually 0.
     * @param values The coordinate values.
     *
     * @see #addCoordinateRange
     */
    public void addOrdinates(final int minIndex, final double[] values) {
        int[] lows  = low. getUserObject(int[].class);
        int[] highs = high.getUserObject(int[].class);
        if (lows != null && highs != null) {
            final int last = Math.max(lows.length, highs.length);
            if (last != lows.length || last != highs.length) {
                warning("addOrdinates", ErrorKeys.MISMATCHED_DIMENSION_$2,
                        new int[]{lows.length, highs.length});
            }
            lows = XArray.resize(lows, last + 1);
            highs = XArray.resize(highs, last + 1);
            lows[last] = minIndex;
            highs[last] = minIndex + values.length - 1;
        } else {
            if (lows == null) {
                lows = new int[1];
                lows[0] = minIndex;
            }
            if (highs == null) {
                highs = new int[1];
                highs[0] = minIndex + values.length - 1;
            }
        }
        low.setUserObject(lows);
        high.setUserObject(highs);
        setOrdinates(localizationGrid.appendChild(), values);
    }

    /**
     * Returns the offset vector for the specified dimension.
     *
     * @param dimension The dimension index, from 0 inclusive to {@link #getDimension} exclusive.
     */
    public double[] getOffsetVector(final int dimension) {
        cells.selectChild(dimension);
        return (double[]) cells.getUserObject();
    }

    /**
     * Set the offset vector for the specified dimension.
     *
     * @param dimension The dimension to set, from 0 inclusive to {@link #getDimension} exclusive.
     * @param values    The offset values.
     */
    public void setOffsetVector(final int dimension, final double[] values) {
        cells.selectChild(dimension);
        cells.setUserObject(values);
    }

    /**
     * Adds offset vector values for a new dimension.
     *
     * @param values The offset values for this new dimension.
     */
    public void addOffsetVector(final double[] values) {
        setOffsetVector(cells.appendChild(), values);
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
        return pixelOrientation.getUserObject(String.class);
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
        if (GeographicMetadataFormat.PIXEL_ORIENTATIONS.contains(pixelOrientation)) {
            this.pixelOrientation.setUserObject(pixelOrientation);
        } else {
            warning("setPixelOrientation", ErrorKeys.BAD_PARAMETER_$2, pixelOrientation);
        }
    }
}
