/*
 * Geotools 2 - OpenSource mapping toolkit
 * (C) 2003, Geotools Project Management Committee (PMC)
 * (C) 2001, Institut de Recherche pour le Développement
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
 *    You should have received a copy of the GNU Lesser General Public
 *    License along with this library; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 *
 * Contacts:
 *     UNITED KINGDOM: James Macgill
 *             mailto:j.macgill@geog.leeds.ac.uk
 *
 *     FRANCE: Surveillance de l'Environnement Assistée par Satellite
 *             Institut de Recherche pour le Développement / US-Espace
 *             mailto:seasnet@teledetection.fr
 *
 *     CANADA: Observatoire du Saint-Laurent
 *             Institut Maurice-Lamontagne
 *             mailto:osl@osl.gc.ca
 *
 *    This package contains documentation from OpenGIS specifications.
 *    OpenGIS consortium's work is fully acknowledged here.
 */
package org.geotools.gc;

// J2SE dependencies
import java.util.Arrays;
import java.awt.image.Raster;
import java.awt.image.DataBuffer;
import java.awt.image.ColorModel;
import java.awt.image.SampleModel;
import java.awt.image.RenderedImage;
import java.awt.RenderingHints;
import java.awt.Color;

// JAI dependencies
import javax.media.jai.Histogram;
import javax.media.jai.iterator.RectIter;
import javax.media.jai.iterator.RectIterFactory;

// Geotools dependencies
import org.geotools.gp.Hints;
import org.geotools.cv.Category;
import org.geotools.cv.SampleDimension;
import org.geotools.cv.SampleDimensionType;
import org.geotools.cv.ColorInterpretation;
import org.geotools.ct.MathTransform1D;
import org.geotools.units.Unit;
import org.geotools.util.NumberRange;
import org.geotools.resources.ClassChanger;
import org.geotools.resources.gcs.Resources;
import org.geotools.resources.gcs.ResourceKeys;


/**
 * Describes the band values for a grid coverage.
 *
 * @version $Id: GridSampleDimension.java,v 1.9 2003/05/13 10:59:52 desruisseaux Exp $
 * @author <A HREF="www.opengis.org">OpenGIS</A>
 * @author Martin Desruisseaux
 */
final class GridSampleDimension extends SampleDimension {
    /**
     * The range of sample values after a transformation from
     * integer geophysics value to 8 bits indexed image.
     */
    private static final NumberRange INTEGER_TO_UBYTE;

    /**
     * The range of sample values after a transformation from
     * integer geophysics value to 16 bits indexed image.
     */
    private static final NumberRange INTEGER_TO_USHORT;

    /**
     * The range of sample values after a transformation from floating point geophysics
     * value to 8 bits indexed image. Index 0 is reserved for {@link Category#NODATA},
     * which maps to {@link Float#NaN} values.
     */
    private static final NumberRange FLOAT_TO_UBYTE;

    /**
     * The range of sample values after a transformation from floating point geophysics
     * value to 16 bits indexed image. Index 0 is reserved for {@link Category#NODATA},
     * which maps to {@link Float#NaN} values.
     */
    private static final NumberRange FLOAT_TO_USHORT;
    static {
        final Integer ZERO = new Integer(0);
        final Integer ONE  = new Integer(1);
        final Integer U08  = new Integer(255);
        final Integer U16  = new Integer(65535);
        INTEGER_TO_UBYTE   = new NumberRange(Integer.class, ZERO, U08);
        INTEGER_TO_USHORT  = new NumberRange(Integer.class, ZERO, U16);
        FLOAT_TO_UBYTE     = new NumberRange(Integer.class, ONE,  U08);
        FLOAT_TO_USHORT    = new NumberRange(Integer.class, ONE,  U16);
    }

    /**
     * Band number for this sample dimension.
     */
    private final int band;

    /**
     * The number of bands in the {@link GridCoverage} who own this sample dimension.
     */
    private final int numBands;

    /**
     * The grid value data type.
     */
    private final SampleDimensionType type;
    
    /**
     * Construct a sample dimension with a set of categories from an other sample dimension.
     *
     * @param band  The originating sample dimension.
     * @param image The image to be wrapped by {@link GridCoverage}.
     * @param bandNumber The band number.
     */
    private GridSampleDimension(final SampleDimension band,
                                final RenderedImage   image,
                                final int             bandNumber)
    {
        super(band);
        final SampleModel model = image.getSampleModel();
        this.band     = bandNumber;
        this.numBands = model.getNumBands();
        this.type     = SampleDimensionType.getEnum(model, bandNumber);
    }

    /**
     * Create a set of sample dimensions for the given image. The array length of both
     * arguments must matches the number of bands in the supplied <code>image</code>.
     *
     * @param  name  The name for data (e.g. "Elevation").
     * @param  image The image for which to create a set of sample dimensions.
     * @param  src   User-provided sample dimensions, or <code>null</code> if none.
     * @param  dst   The array where to put sample dimensions.
     * @return <code>true</code> if all sample dimensions are geophysics, or <code>false</code>
     *         if all sample dimensions are <strong>not</strong> geophysics.
     * @throws IllegalArgumentException if geophysics and non-geophysics dimensions are mixed.
     */
    static boolean create(final String          name,
                          final RenderedImage   image,
                          final SampleDimension[] src,
                          final SampleDimension[] dst)
    {
        final int numBands = image.getSampleModel().getNumBands();
        if (src!=null && src.length!=numBands) {
            throw new IllegalArgumentException(Resources.format(
                    ResourceKeys.ERROR_NUMBER_OF_BANDS_MISMATCH_$3,
                    new Integer(numBands), new Integer(src.length), "SampleDimension"));
        }
        if (dst.length != numBands) {
            throw new IllegalArgumentException(Resources.format(
                    ResourceKeys.ERROR_NUMBER_OF_BANDS_MISMATCH_$3,
                    new Integer(numBands), new Integer(dst.length), "SampleDimension"));
        }
        int nGeo = 0;
        int nInt = 0;
        SampleDimension[] defaultSD = null;
        for (int i=0; i<numBands; i++) {
            SampleDimension sd = (src!=null) ? src[i] : null;
            if (sd == null) {
                if (defaultSD == null) {
                    defaultSD = new SampleDimension[numBands];
                    create(name, RectIterFactory.create(image, null),
                           image.getSampleModel().getDataType(),
                           null, null, null, null, defaultSD, null);
                }
                sd = defaultSD[i];
            }
            sd = new GridSampleDimension(sd, image, i);
            dst[i] = sd;
            if (sd.geophysics(true ) == sd) nGeo++;
            if (sd.geophysics(false) == sd) nInt++;
        }
        if (nGeo == numBands) {
            return true;
        }
        if (nInt == numBands) {
            return false;
        }
        throw new IllegalArgumentException(Resources.format(ResourceKeys.ERROR_MIXED_CATEGORIES));
    }

    /**
     * Create a set of sample dimensions for the given raster.
     *
     * @param  name The name for data (e.g. "Elevation").
     * @param  raster The raster.
     * @param  min The minimal value for each bands, or <code>null</code> for computing it
     *         automatically.
     * @param  max The maximal value for each bands, or <code>null</code> for computing it
     *         automatically.
     * @param  units The units of sample values, or <code>null</code> if unknow.
     * @param  colors The colors to use for values from <code>min</code> to <code>max</code>
     *         for each bands, or <code>null</code> for a default color palette. If non-null,
     *         each arrays <code>colors[b]</code> may have any length; colors will be interpolated
     *         as needed.
     * @param  hints An optional set of rendering hints, or <code>null</code> if none.
     *         Those hints will not affect the sample dimensions to be created. However,
     *         they may affect the sample dimensions to be returned by
     *         <code>{@link #geophysics geophysics}(false)</code>, i.e.
     *         the view to be used at rendering time. The optional hint
     *         {@link Hints#SAMPLE_DIMENSION_TYPE} specifies the {@link SampleDimensionType}
     *         to be used at rendering time, which can be one of
     *         {@link SampleDimensionType#UBYTE UBYTE} or
     *         {@link SampleDimensionType#USHORT USHORT}.
     * @return The sample dimension for the given raster.
     */
    static SampleDimension[] create(final String         name,
                                    final Raster         raster,
                                    final double[]       min,
                                    final double[]       max,
                                    final Unit           units,
                                    final Color[][]      colors,
                                    final RenderingHints hints)
    {
        final SampleDimension[] dst = new SampleDimension[raster.getNumBands()];
        create(name, (min==null || max==null) ? RectIterFactory.create(raster, null) : null,
               raster.getDataBuffer().getDataType(), min, max, units, colors, dst, hints);
        return dst;
    }

    /**
     * Create a set of sample dimensions for the data backing the given iterator.
     *
     * @param  name The name for data (e.g. "Elevation").
     * @param  iterator The iterator through the raster data, or <code>null</code>.
     * @param  rasterType The data type of the image sample values.
     *         Must be one of {@link DataBuffer} constants.
     * @param  min The minimal value, or <code>null</code> for computing it automatically.
     * @param  max The maximal value, or <code>null</code> for computing it automatically.
     * @param  units The units of sample values, or <code>null</code> if unknow.
     * @param  colors The colors to use for values from <code>min</code> to <code>max</code>
     *         for each bands, or <code>null</code> for a default color palette. If non-null,
     *         each arrays <code>colors[b]</code> may have any length; colors will be interpolated
     *         as needed.
     * @param  dst The array where to store sample dimensions. The array length must matches
     *         the number of bands.
     * @param  hints An optional set of rendering hints, or <code>null</code> if none.
     *         Those hints will not affect the sample dimensions to be created. However,
     *         they may affect the sample dimensions to be returned by
     *         <code>{@link #geophysics geophysics}(false)</code>, i.e.
     *         the view to be used at rendering time. The optional hint
     *         {@link Hints#SAMPLE_DIMENSION_TYPE} specifies the {@link SampleDimensionType}
     *         to be used at rendering time, which can be one of
     *         {@link SampleDimensionType#UBYTE UBYTE} or
     *         {@link SampleDimensionType#USHORT USHORT}.
     */
    private static void create(final String            name,
                               final RectIter          iterator,
                               final int               rasterType,
                                     double[]          min,
                                     double[]          max,
                               final Unit              units,
                               final Color[][]         colors,
                               final SampleDimension[] dst,
                               final RenderingHints    hints)
    {
        final int     numBands   = dst.length;
        final boolean computeMin = (min == null);
        final boolean computeMax = (max == null);
        if (computeMin) {
            min = new double[numBands];
            Arrays.fill(min, Double.POSITIVE_INFINITY);
        }
        if (computeMax) {
            max = new double[numBands];
            Arrays.fill(max, Double.NEGATIVE_INFINITY);
        }
        if (min.length != numBands) {
            throw new IllegalArgumentException(Resources.format(
                    ResourceKeys.ERROR_NUMBER_OF_BANDS_MISMATCH_$3,
                    new Integer(numBands), new Integer(min.length), "min[i]"));
        }
        if (max.length != numBands) {
            throw new IllegalArgumentException(Resources.format(
                    ResourceKeys.ERROR_NUMBER_OF_BANDS_MISMATCH_$3,
                    new Integer(numBands), new Integer(max.length), "max[i]"));
        }
        if (colors!=null && colors.length != numBands) {
            throw new IllegalArgumentException(Resources.format(
                    ResourceKeys.ERROR_NUMBER_OF_BANDS_MISMATCH_$3,
                    new Integer(numBands), new Integer(colors.length), "colors[i]"));
        }
        /*
         * Arguments are now know to be valids. We now need to compute two ranges:
         *
         * STEP 1: Range of sample values. This is computed in the following block.
         * STEP 2: Range of geophysics values. It will be computed one block later.
         *
         * The range of sample values will range from 0 to 255 or 0 to 65535 according
         * the rendering hint provided. If the raster data use floating point numbers,
         * then a "nodata" category will be added in order to handle NaN values. If the
         * the raster data use integer numbers, then we will rescale the numbers only
         * if they would not fit in the rendering type.
         */
        SampleDimensionType renderingType = SampleDimensionType.UBYTE;
        if (rasterType!=DataBuffer.TYPE_BYTE && hints!=null) {
            renderingType = (SampleDimensionType) hints.get(Hints.SAMPLE_DIMENSION_TYPE);
        }
        final boolean byteRenderingType = renderingType.getSize()<=8;
        final NumberRange sampleValueRange;
        final Category[]  categories;
        boolean needScaling = true;
        switch (rasterType) {
            case DataBuffer.TYPE_FLOAT:
            case DataBuffer.TYPE_DOUBLE: {
                categories = new Category[2];
                categories[1] = Category.NODATA;
                sampleValueRange = byteRenderingType ? FLOAT_TO_UBYTE : FLOAT_TO_USHORT;
                break;
            }
            case DataBuffer.TYPE_BYTE:
            case DataBuffer.TYPE_USHORT: {
                if (rasterType == renderingType.getDataBufferType()) {
                    needScaling = false;
                }
                // fall through
            }
            default: {
                categories = new Category[1];
                sampleValueRange = byteRenderingType ? INTEGER_TO_UBYTE : INTEGER_TO_USHORT;
                break;
            }
        }
        /*
         * Compute the minimal and maximal values, if not explicitely provided.
         * This information is required for determining the range of geophysics
         * values.
         */
        if (computeMin || computeMax) {
            int b=0;
            iterator.startBands();
            if (!iterator.finishedBands()) do {
                iterator.startLines();
                if (!iterator.finishedLines()) do {
                    iterator.startPixels();
                    if (!iterator.finishedPixels()) do {
                        final double z = iterator.getSampleDouble();
                        if (computeMin && z<min[b]) min[b]=z;
                        if (computeMax && z>max[b]) max[b]=z;
                    } while (!iterator.nextPixelDone());
                } while (!iterator.nextLineDone());
                if (computeMin && computeMax) {
                    if (!(min[b] < max[b])) {
                        min[b] = 0;
                        max[b] = 1;
                    }
                }
                b++;
            } while (!iterator.nextBandDone());
        }
        /*
         * Determine the class of geophysics values. This class can generally be infered from
         * the raster data type. In the exceptional case where the data type is unknow, we will
         * determine a default class based on the range of values computed just above.
         */
        Class classe = null;
        switch (rasterType) {
            case DataBuffer.TYPE_BYTE:   // Fall through
            case DataBuffer.TYPE_SHORT:  classe =   Short.class; break;
            case DataBuffer.TYPE_USHORT: // Fall through
            case DataBuffer.TYPE_INT:    classe = Integer.class; break;
            case DataBuffer.TYPE_FLOAT:  classe =   Float.class; break;
            case DataBuffer.TYPE_DOUBLE: classe =  Double.class; break;
            default: {
                // Unrecognized type. Fallback on the finest
                // type capable to hold the range of all bands.
                for (int b=0; b<numBands; b++) {
                    classe = ClassChanger.getWidestClass(classe,
                             ClassChanger.getWidestClass(
                             ClassChanger.getFinestClass(min[b]),
                             ClassChanger.getFinestClass(max[b])));
                }
                break;
            }
        }
        /*
         * Now, construct the sample dimensions. We will inconditionnaly provides a "nodata"
         * category for floating point images, since we don't know if the user plan to have
         * NaN values. Even if the current image doesn't have NaN values, it could have NaN
         * later if the image uses a writable raster.
         */
        for (int b=0; b<numBands; b++) {
            NumberRange geophysicsValueRange = new NumberRange(min[b], max[b]).castTo(classe);
            final Color[] c = colors!=null ? colors[b] : null;
            if (needScaling) {
                categories[0] = new Category(name, c, sampleValueRange, geophysicsValueRange);
            } else {
                categories[0] = new Category(name, c, sampleValueRange, MathTransform1D.IDENTITY);
            }
            dst[b] = new SampleDimension(categories, units).geophysics(true);
        }
    }

    /**
     * Returns a code value indicating grid value data type.
     * This will also indicate the number of bits for the data type.
     *
     * @return a code value indicating grid value data type.
     */
    public SampleDimensionType getSampleDimensionType() {
        return type;
    }
    
    /**
     * Returns the color interpretation of the sample dimension.
     */
    public ColorInterpretation getColorInterpretation() {
        return ColorInterpretation.getEnum(getColorModel(band, numBands), band);
    }

    /**
     * Returns a color model for this sample dimension.
     */
    public ColorModel getColorModel() {
        return getColorModel(band, numBands);
    }
    
    /**
     * Returns the minimum value occurring in this sample dimension.
     */
    //  public double getMinimumValue()
    //  {return getHistogram().getLowValue(band);}
    
    /**
     * Returns the maximum value occurring in this sample dimension.
     */
    //  public double getMaximumValue()
    //  {return getHistogram().getHighValue(band);}
    
    /**
     * Determine the mode grid value in this sample dimension.
     */
    //  public double getModeValue()
    //  {throw new UnsupportedOperationException("Not implemented");}
    
    /**
     * Determine the median grid value in this sample dimension.
     */
    //  public double getMedianValue()
    //  {throw new UnsupportedOperationException("Not implemented");}
    
    /**
     * Determine the mean grid value in this sample dimension.
     */
    //  public double getMeanValue()
    //  {return getHistogram().getMean()[band];}
    
    /**
     * Determine the standard deviation from the mean
     * of the grid values in a sample dimension.
     */
    //  public double getStandardDeviation()
    //  {return getHistogram().getStandardDeviation()[band];}
    
    /**
     * Gets the histogram for the underlying grid coverage.
     */
    //  private Histogram getHistogram()
    //  {throw new UnsupportedOperationException("Not implemented");}
}
