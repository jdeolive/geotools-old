/*
 * Geotools - OpenSource mapping toolkit
 * (C) 2002, Centre for Computational Geography
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
package org.geotools.cv;

// J2SE dependencies
import java.awt.Color;
import java.util.Arrays;
import java.util.Locale;
import java.io.Serializable;

// JAI dependencies
import javax.media.jai.util.Range;

// OpenGIS dependencies
import org.opengis.cv.CV_SampleDimension;

// Geotools dependencies
import org.geotools.ct.MathTransform1D;
import org.geotools.ct.TransformException;

// Resources
import org.geotools.resources.XMath;
import org.geotools.resources.Utilities;
import org.geotools.resources.gcs.Resources;
import org.geotools.resources.gcs.ResourceKeys;


/**
 * A category delimited by a range of sample values.
 * A category may represent qualitative or quantitative information,   for
 * example geomorphologic structures or geophysics parameters.  Some image
 * mixes both qualitative and quantitative categories. For example, images
 * of Sea Surface Temperature  (SST)  may have a quantitative category for
 * temperature with values ranging from –2 to 35°C,  and three qualitative
 * categories for cloud, land and ice.
 * <br><br>
 * All categories must have a human readable name. In addition, quantitative
 * categories may define a transformation between sample values <var>s</var>
 * and geophysics values <var>x</var>.   This transformation is usually (but
 * not always) a linear equation of the form:
 *
 * <center><var>x</var><code>&nbsp;=&nbsp;{@link CV_SampleDimension#getOffset()
 * offset}&nbsp;+&nbsp;{@link CV_SampleDimension#getScale()
 * scale}&times;</code><var>s</var></center>
 *
 * More general equation are allowed. For example, <cite>SeaWiFS</cite> images
 * use a logarithmic transform. General transformations are expressed with a
 * {@link MathTransform1D} object.
 *
 * @version $Id: Category.java,v 1.2 2002/07/17 23:30:55 desruisseaux Exp $
 * @author Martin Desruisseaux
 */
public class Category implements Serializable {
    /**
     * Serial number for interoperability with different versions.
     */
    private static final long serialVersionUID = -3531834320875149028L;
    
    /**
     * The category name (may not be localized).
     */
    private final String name;
    
    /**
     * The minimal sample value (inclusive). This category is made of all sample values
     * in the range <code>minSample</code> to <code>maxSample</code> inclusive.
     */
    final double minSample;
    
    /**
     * The maximal sample value (inclusive). This category is made of all sample values
     * in the range <code>minSample</code> to <code>maxSample</code> inclusive.
     */
    final double maxSample;
    
    /**
     * The minimal geophysics value, inclusive. This value is usually (but not always)
     * equals to <code>{@link #toGeophysicsValue toGeophysicsValue}(minSample)</code>.
     *
     * For qualitative categories, this value is <code>NaN</code>.
     */
    final double minValue;
    
    /**
     * The maximal geophysics value, inclusive. This value is usually (but not always)
     * equals to <code>{@link #toGeophysicsValue toGeophysicsValue}(maxSample)</code>.
     *
     * For qualitative categories, this value is <code>NaN</code>.
     */
    final double maxValue;

    /**
     * The range of sample values.
     */
    private final Range sampleRange;

    /**
     * The range of geophysics value.
     * Will be computed only when first requested.
     */
    private transient Range valueRange;

    /**
     * The math transform from sample to geophysics values, or
     * <code>null</code> if this category is a qualitative one.
     *
     * @see CV_SampleDimension#getScale()
     * @see CV_SampleDimension#getOffset()
     */
    private final MathTransform1D sampleToGeophysics;

    /**
     * The math transform from geophysics to sample values, or
     * <code>null</code> if this category is a qualitative one.
     * This is equals to <code>{@link #sampleToGeophysics}.inverse()</code>.
     */
    private final MathTransform1D geophysicsToSample;
    
    /**
     * Codes ARGB des couleurs de la catégorie. Les couleurs par
     * défaut seront un gradient allant du noir au blanc opaque.
     */
    private final int[] ARGB;
    
    /**
     * Codes ARGB par défaut. On utilise un exemplaire unique
     * pour toutes les création d'objets {@link Category}.
     */
    private static final int[] DEFAULT = {0xFF000000, 0xFFFFFFFF};
    
    /**
     * Construct a qualitative category for sample value <code>sample</code>.
     *
     * @param  name    The category name.
     * @param  color   The category color, or <code>null</code> for a default color.
     * @param  sample  The sample value as an integer, usually in the range 0 to 255.
     */
    public static Category create(final String name,
                                  final Color  color,
                                  final int    sample)
    {
        return new QualitativeCategory(name, color, new Integer(sample));
    }
    
    /**
     * Construct a quantitative category for sample values ranging from <code>lower</code>
     * inclusive to <code>upper</code> exclusive. Sample values are converted into geophysics
     * values using the following linear equation:
     *
     * <center><var>x</var><code>&nbsp;=&nbsp;{@link CV_SampleDimension#getOffset()
     * offset}&nbsp;+&nbsp;{@link CV_SampleDimension#getScale()
     * scale}&times;</code><var>s</var></center>
     *
     * @param  name    The category name.
     * @param  colors  A set of colors for this category. This array may have any length;
     *                 colors will be interpolated as needed. An array of length 1 means
     *                 that an uniform color should be used for all sample values. An array
     *                 of length 0 or a <code>null</code> array means that some default colors
     *                 should be used (usually a gradient from opaque black to opaque white).
     * @param  lower   The lower sample value, inclusive.
     * @param  upper   The upper sample value, exclusive.
     * @param  scale   The {@link CV_SampleDimension#getScale() scale} value which is multiplied
     *                 to sample values for this category.
     * @param  offset  The {@link CV_SampleDimension#getOffset() offset} value to add to sample
     *                 values for this category.
     *
     * @throws IllegalArgumentException if <code>lower</code> is not smaller than
     *         <code>upper</code>.
     * @throws IllegalArgumentException if <code>scale</code> or <code>offset</code> are
     *         not real numbers.
     */
    public static Category create(final String  name,
                                  final Color[] colors,
                                  final int     lower,
                                  final int     upper,
                                  final double  scale,
                                  final double  offset) throws IllegalArgumentException
    {
        Range range = new Range(Integer.class, new Integer(lower), true,    // Inclusive
                                               new Integer(upper), false);  // Exclusive
        return new LinearCategory(name, toARGB(colors), range, scale, offset);
    }
    
    /**
     * Construct a quantitative category for sample values in the specified range.
     * Sample values are converted into geophysics values using the following linear
     * equation:
     *
     * <center><var>x</var><code>&nbsp;=&nbsp;{@link CV_SampleDimension#getOffset()
     * offset}&nbsp;+&nbsp;{@link CV_SampleDimension#getScale()
     * scale}&times;</code><var>s</var></center>
     *
     * @param  name    The category name.
     * @param  colors  A set of colors for this category. This array may have any length;
     *                 colors will be interpolated as needed. An array of length 1 means
     *                 that an uniform color should be used for all sample values. An array
     *                 of length 0 or a <code>null</code> array means that some default colors
     *                 should be used (usually a gradient from opaque black to opaque white).
     * @param  lower   The lower sample value, inclusive.
     * @param  sampleValueRange The range of sample values for this category. Element class
     *                 is usually {@link Integer}, but {@link Float} and {@link Double} are
     *                 accepted as well.
     * @param  offset  The {@link CV_SampleDimension#getOffset() offset} value to add to sample
     *                 values for this category.
     *
     * @throws IllegalArgumentException if <code>lower</code> is not smaller than
     *         <code>upper</code>.
     * @throws IllegalArgumentException if <code>scale</code> or <code>offset</code> are
     *         not real numbers.
     */
    public static Category create(final String  name,
                                  final Color[] colors,
                                  final Range   sampleValueRange,
                                  final double  scale,
                                  final double  offset) throws IllegalArgumentException
    {
        return new LinearCategory(name, toARGB(colors), sampleValueRange, scale, offset);
    }
    
    /**
     * Construct a quantitative category mapping samples to geophysics values in the specified
     * range. Sample values in the <code>sampleValueRange</code> will be mapped to geophysics
     * values in the <code>geophysicsValueRange</code> through a linear equation of the form:
     *
     * <center><var>x</var><code>&nbsp;=&nbsp;{@link CV_SampleDimension#getOffset()
     * offset}&nbsp;+&nbsp;{@link CV_SampleDimension#getScale()
     * scale}&times;</code><var>s</var></center>
     *
     * <code>scale</code> and <code>offset</code> coefficients are computed from the
     * ranges supplied in arguments.
     *
     * @param  name    The category name.
     * @param  colors  A set of colors for this category. This array may have any length;
     *                 colors will be interpolated as needed. An array of length 1 means
     *                 that an uniform color should be used for all sample values. An array
     *                 of length 0 or a <code>null</code> array means that some default colors
     *                 should be used (usually a gradient from opaque black to opaque white).
     * @param  sampleValueRange The range of sample values for this category. Element class
     *                 is usually {@link Integer}, but {@link Float} and {@link Double} are
     *                 accepted as well.
     * @param  geophysicsValueRange The range of geophysics values for this category.
     *                 Element class is usually {@link Float} or {@link Double}.
     *
     * @throws ClassCastException if the range element class is not a {@link Number} subclass.
     * @throws IllegalArgumentException if the range is invalid.
     */
    public static Category create(final String  name,
                                  final Color[] colors,
                                  final Range   sampleValueRange,
                                  final Range   geophysicsValueRange)
        throws IllegalArgumentException
    {
        return new LinearCategory(name, toARGB(colors), sampleValueRange, geophysicsValueRange);
    }
    
    /**
     * Construct a qualitative or quantitative category for samples in the specified range.
     * Sample values (usually integers) will be converted into geophysics values (usually
     * floating-point) through the <code>sampleToGeophysics</code> transform.
     *
     * @param  name    The category name.
     * @param  colors  A set of colors for this category. This array may have any length;
     *                 colors will be interpolated as needed. An array of length 1 means
     *                 that an uniform color should be used for all sample values. An array
     *                 of length 0 or a <code>null</code> array means that some default colors
     *                 should be used (usually a gradient from opaque black to opaque white).
     * @param  sampleValueRange The range of sample values for this category. Element class
     *                 is usually {@link Integer}, but {@link Float} and {@link Double} are
     *                 accepted as well.
     * @param  sampleToGeophysics A transform from sample values to geophysics values,
     *                 or <code>null</code> if this category is not a quantitative one.
     *
     * @throws ClassCastException if the range element class is not a {@link Number} subclass.
     * @throws IllegalArgumentException if the range is invalid.
     */
    public static Category create(final String          name,
                                  final Color[]         colors,
                                  final Range           sampleValueRange,
                                  final MathTransform1D sampleToGeophysics)
        throws IllegalArgumentException
    {
        return create(name, toARGB(colors), sampleValueRange, sampleToGeophysics);
    }

    /**
     * Construct a qualitative or quantitative category for sample in the specified range.
     * This private factory method is used for both qualitative and quantitative category
     * creation. It also used by {@link #recolor} in order to construct a new category
     * similar to an existing one except for ARGB codes.
     */
    private static Category create(final String          name,
                                   final int[]           ARGB,
                                   final Range           sampleValueRange,
                                   final MathTransform1D sampleToGeophysics)
        throws IllegalArgumentException
    {
        if (sampleToGeophysics == null) {
            return new QualitativeCategory(name, ARGB, sampleValueRange);
        }
        try {
            if (!Double.isNaN(sampleToGeophysics.derivative(Double.NaN))) {
                return new LinearCategory(name, ARGB, sampleValueRange, sampleToGeophysics);
            }
        } catch (TransformException exception) {
            // Ignore... We will give up the optimized category.
        }
        return new Category(name, ARGB, sampleValueRange, sampleToGeophysics);
    }
    
    /**
     * Construct a qualitative or quantitative category for samples in the specified range.
     * Sample values (usually integers) will be converted into geophysics values (usually
     * floating-point) through an equation specified by a {@link MathTransform1D} object.
     *
     * @param  name    The category name.
     * @param  colors  A set of colors for this category. This array may have any length;
     *                 colors will be interpolated as needed. An array of length 1 means
     *                 that an uniform color should be used for all sample values. An array
     *                 of length 0 or a <code>null</code> array means that some default colors
     *                 should be used (usually a gradient from opaque black to opaque white).
     * @param  sampleValueRange The range of sample values for this category. Element class
     *                 is usually {@link Integer}, but {@link Float} and {@link Double} are
     *                 accepted as well.
     * @param  sampleToGeophysics A transform from sample values to geophysics values,
     *                 or <code>null</code> if this category is not a quantitative one.
     *
     * @throws ClassCastException if the range element class is not a {@link Number} subclass.
     * @throws IllegalArgumentException if the range is invalid.
     */
    protected Category(final String          name,
                       final Color[]         colors,
                       final Range           sampleValueRange,
                       final MathTransform1D sampleToGeophysics) throws IllegalArgumentException
    {
        this(name, toARGB(colors), sampleValueRange, sampleToGeophysics);
    }
    
    /**
     * Construct a category with the specified math transform.  This private constructor is
     * used for both qualitative and quantitative category constructors.    It also used by
     * {@link #recolor} in order to construct a new category similar to this one except for
     * ARGB codes.
     */
    Category(final String          name,
             final int[]           ARGB,
             final Range           range,
             final MathTransform1D sampleToGeophysics) throws IllegalArgumentException
    {
        final Class type = range.getElementClass();
        this.minSample   = doubleValue(type, range.getMinValue(), range.isMinIncluded() ? 0 : +1);
        this.maxSample   = doubleValue(type, range.getMaxValue(), range.isMaxIncluded() ? 0 : -1);
        this.name        = name.trim();
        this.ARGB        = ARGB;
        this.sampleRange = range;
        this.sampleToGeophysics = sampleToGeophysics;
        // Use '!' in comparaison in order to catch NaN
        if (!(minSample<=maxSample) || Double.isInfinite(minSample) || Double.isInfinite(maxSample))
        {
            throw new IllegalArgumentException(Resources.format(
                    ResourceKeys.ERROR_BAD_RANGE_$2, range.getMinValue(), range.getMaxValue()));
        }
        if (sampleToGeophysics == null) {
            geophysicsToSample =  null;
            minValue = maxValue = toNaN((int) Math.round((minSample+maxSample)/2));
            return;
        }
        /*
         * Compute 'minValue' and 'maxValue' (which must be real numbers) using the supplied
         * transform. To be strict, we should check if the transform is always increasing or
         * decreasing with input <var>x</var>. This is always the case for linear and
         * logarithmic transforms. We ignore more general cases for now.
         */
        TransformException cause=null;
        try {
            geophysicsToSample = (MathTransform1D) sampleToGeophysics.inverse();
            final double   min = sampleToGeophysics.transform(minSample);
            final double   max = sampleToGeophysics.transform(maxSample);
            if (min > max) {
                this.minValue = max;
                this.maxValue = min;
            } else {
                this.minValue = min;
                this.maxValue = max;
            }
            // Check for NaN
            if (minValue <= maxValue) {
                return;
            }
        } catch (TransformException exception) {
            cause = exception;
        }
        IllegalArgumentException exception = new IllegalArgumentException(Resources.format(
                    ResourceKeys.ERROR_BAD_TRANSFORM_$1,
                    Utilities.getShortClassName(sampleToGeophysics)));
        exception.initCause(cause);
        throw exception;
    }

    /**
     * Returns a <code>double</code> value for the specified number. If <code>direction</code>
     * is non-zero, then this method will returns the closest representable number of type
     * <code>type</code> before or after the double value.
     *
     * @param type      The range element class. <code>number</code> must be
     *                  an instance of this class (this will not be checked).
     * @param number    The number to transform to a <code>double</code> value.
     * @param direction -1 to return the previous representable number,
     *                  +1 to return the next representable number, or
     *                   0 to return the number with no change.
     */
    static double doubleValue(final Class        type,
                              final Comparable number,
                              final int     direction)
    {
        double value = ((Number) number).doubleValue();
        if (direction != 0) {
            if (Float.class.isAssignableFrom(type)) {
                final float f = (float) value;
                value = (direction<0) ? XMath.previous(f) : XMath.next(f);
            } else if (Double.class.isAssignableFrom(type)) {
                value = (direction<0) ? XMath.previous(value) : XMath.next(value);
            }
            else value += direction;
        }
        return value;
    }
    
    /**
     * Returns a NaN number for the specified category number. Valid NaN numbers have
     * bit fields ranging from <code>0x7f800001</code> through <code>0x7fffffff</code>
     * or <code>0xff800001</code> through <code>0xffffffff</code>. The standard {@link
     * Float#NaN} has bit fields <code>0x7fc00000</code>.
     *
     * @param  index The category number, from -2097152 to 2097151 inclusive. This number
     *               doesn't need to matches sample values.    Different categories don't
     *               need to use increasing,  different or contiguous numbers. This is up
     *               to the user to manage his category numbers.  Category numbers may be
     *               anything like 1 for "cloud", 2 for "ice", 3 for "land", etc.
     * @return       The NaN value as a float. We limit ourself to the float type instead
     *               of double because the underlying image storage type way be float.
     * @throws IndexOutOfBoundsException if the specified index is out of bounds.
     */
    private static float toNaN(int index) throws IndexOutOfBoundsException {
        index += 0x200000;
        if (index>=0 && index<=0x3FFFFF) {
            final float value = Float.intBitsToFloat(0x7FC00000 + index);
            assert Float.isNaN(value);
            return value;
        }
        else {
            throw new IndexOutOfBoundsException(String.valueOf(index));
        }
    }
    
    /**
     * Convert an array of colors to an array of ARGB values.
     * If <code>colors</code> is null, then a default array
     * will be returned.
     *
     * @param  colors The array of colors to convert (may be null).
     * @return The colors as ARGB values. Never null.
     */
    static int[] toARGB(final Color[] colors) {
        final int[] ARGB;
        if (colors!=null && colors.length!=0) {
            ARGB = new int[colors.length];
            for (int i=0; i<ARGB.length; i++) {
                ARGB[i] = colors[i].getRGB();
            }
        } else {
            ARGB = DEFAULT;
        }
        return ARGB;
    }
    
    /**
     * Returns the category name localized in the specified locale. If no name is
     * available for the specified locale, then an arbitrary locale may be used.
     * The default implementation returns the <code>name</code> argument specified
     * at construction time.
     *
     * @param  locale The desired locale, or <code>null</code> for the default locale.
     * @return The category name, localized if possible.
     */
    public String getName(final Locale locale) {
        return name;
    }
    
    /**
     * Returns the set of colors for this category.
     * Change to the returned array will not affect
     * this category.
     *
     * @see SampleDimension#getColorModel
     */
    public Color[] getColors() {
        final Color[] colors=new Color[ARGB.length];
        for (int i=0; i<colors.length; i++) {
            colors[i] = new Color(ARGB[i], true);
        }
        return colors;
    }

    /**
     * Returns the range of values occurring in this category.   If <code>type</code> is
     * {@link SampleInterpretation#INDEXED}, then the range of sample values is returned.
     * Otherwise, if <code>type</code> is {@link SampleInterpretation#GEOPHYSICS},  then
     * the range of geophysics values (as computed by the {@link #getSampleToGeophysics}
     * transform) is returned.
     * <br><br>
     * This method is final because current {@link SampleDimension} implementation
     * makes some assumptions about range values, for performance reasons.
     *
     * @param  type {@link SampleInterpretation#INDEXED} for the range of sample values (as
     *         specified to the constructor), or {@link SampleInterpretation#GEOPHYSICS} for
     *         the range of geophysics values (as transformed by {@link #getSampleToGeophysics}).
     * @return The range of sample or geophysics values.
     * @throws IllegalStateException if <code>type</code> is {@link SampleInterpretation#GEOPHYSICS}
     *         and this category is not a quantitative one (i.e. {@link #getSampleToGeophysics}
     *         returns <code>null</code>).
     *
     * @see SampleDimension#getMinimumValue()
     * @see SampleDimension#getMaximumValue()
     */
    public final Range getRange(final SampleInterpretation type) throws IllegalStateException {
        if (SampleInterpretation.INDEXED.equals(type)) {
            return sampleRange;
        }
        if (SampleInterpretation.GEOPHYSICS.equals(type)) {
            if (valueRange == null) {
                if (sampleToGeophysics==null) {
                    throw new IllegalStateException(Resources.format(
                            ResourceKeys.ERROR_QUALITATIVE_CATEGORY_$1, getName(null)));
                }
                double min = toGeophysicsValue(((Number) sampleRange.getMinValue()).doubleValue());
                double max = toGeophysicsValue(((Number) sampleRange.getMaxValue()).doubleValue());
                boolean minIncluded = sampleRange.isMinIncluded();
                boolean maxIncluded = sampleRange.isMaxIncluded();
                if (min > max) {
                    final double tmp;
                    final boolean tmpIncluded;
                    tmp = min;   tmpIncluded = minIncluded;
                    min = max;   minIncluded = maxIncluded;
                    max = tmp;   maxIncluded = tmpIncluded;
                }
                valueRange = new Range(Double.class,
                                       new Double(min), minIncluded,
                                       new Double(max), maxIncluded);
            }
            return valueRange;
        }
        throw new IllegalArgumentException(Resources.format(ResourceKeys.ERROR_BAD_PARAMETER_$2,
                                           "type", (type!=null) ? type.getName(null) : null));
    }

    /**
     * Returns a transform from sample values to geophysics values. If this category
     * is not a quantitative one, then this method returns <code>null</code>.
     */
    public final MathTransform1D getSampleToGeophysics() {
        return sampleToGeophysics;
    }
    
    /**
     * Compute the geophysics value from a sample value.
     * The sample value <var>s</var> should be in the range <code>[minSample..maxSample]</code>,
     * (supplied at construction time). However, this methods do not performs any range check.
     * Index out of range may lead to extrapolation, which may or may not have a physical maining.
     *
     * @param s  The sample value.
     * @return   The geophysics value as a real number if this category is a quantitative one,
     *           or one of <code>NaN</code> values if this category is a qualitative one.
     *
     * @see SampleDimension#toGeophysicsValue
     */
    double toGeophysicsValue(final double s) {
        if (sampleToGeophysics != null) try {
            return sampleToGeophysics.transform(s);
        } catch (TransformException exception) {
            return Double.NaN;
        }
        return minValue;
    }

    /**
     * Compute the sample value from a geophysics value. This operation is the inverse
     * of {@link #toGeophysicsValue}, except for a range check: if the resulting value
     * is outside this category's range (<code>[minSample..maxSample]</code>), then it
     * will be clamp to <code>minSample</code> or <code>maxSample</code> as necessary.
     *
     * @param  value The geophysics value. May be one of <code>NaN</code> values.
     * @return The sample value in the range <code>[minSample..maxSample]</code>.
     *
     * @see SampleDimension#toSampleValue
     */
    double toSampleValue(final double value) {
        if (geophysicsToSample != null) try {
            final double sample = geophysicsToSample.transform(value);
            if (sample >= maxSample) {
                return maxSample;
            }
            if (sample >= minSample) {
                return sample;
            }
            // If NaN, returns minSample.
        } catch (TransformException exception) {
            // Ignore. Will returns minSample.
        }
        return minSample;
    }
    
    /**
     * Returns <code>true</code> if this category is quantitative. A quantitative category
     * has a non-null {@link #getSampleToGeophysics() sampleToGeophysics} transform.
     *
     * @return <code>true</code> if this category is quantitative, or
     *         <code>false</code> if this category is qualitative.
     */
    public boolean isQuantitative() {
        return sampleToGeophysics != null;
    }
    
    /**
     * Returns <code>true</code> if sample values are equal to geophysics values in this category.
     * More specifically, returns <code>true</code> if this category is a quantitative category
     * and {@link #getSampleToGeophysics() sampleToGeophysics} is an identity transform.
     *
     * @deprecated Use <code>{@link #getSampleToGeophysics()}.isIdentity()</code> instead.
     */
    public boolean isIdentity() {
        return sampleToGeophysics!=null && sampleToGeophysics.isIdentity();
    }
    
    /**
     * Returns a new category for the same sample and geophysics values but
     * a different color palette.
     *
     * @param colors A set of colors for the new category. This array may have
     *               any length; colors will be interpolated as needed. An array
     *               of length 1 means that an uniform color should be used for
     *               all sample values. An array of length 0 or a <code>null</code>
     *               array means that some default colors should be used (usually
     *               a gradient from opaque black to opaque white).
     * @return A category with the new color palette, or <code>this</code>
     *         if the new colors are identical to the current ones.
     */
    public Category recolor(final Color[] colors) {
        final int[] newARGB = toARGB(colors);
        if (Arrays.equals(ARGB, newARGB)) {
            return this;
        }
        return create(name, newARGB, sampleRange, sampleToGeophysics);
    }
    
    /**
     * Returns a hash value for this category.
     * This value need not remain consistent between
     * different implementations of the same class.
     */
    public int hashCode() {
        return name.hashCode();
    }
    
    /**
     * Compares the specified object with
     * this category for equality.
     */
    public boolean equals(final Object object) {
        if (object==this) {
            // Slight optimization
            return true;
        }
        if (object!=null && object.getClass().equals(getClass())) {
            final Category that = (Category) object;
            return Double.doubleToRawLongBits(this.minSample) == Double.doubleToRawLongBits(that.minSample) &&
                   Double.doubleToRawLongBits(this.maxSample) == Double.doubleToRawLongBits(that.maxSample) &&
                   Double.doubleToRawLongBits(this.minValue ) == Double.doubleToRawLongBits(that.minValue ) &&
                   Double.doubleToRawLongBits(this.maxValue ) == Double.doubleToRawLongBits(that.maxValue ) &&
                   Utilities.equals(this.sampleToGeophysics, that.sampleToGeophysics) &&
                   Utilities.equals(this.name, that.name) &&
                      Arrays.equals(this.ARGB, that.ARGB);
        }
        return false;
    }
    
    /**
     * Returns a string representation of this category.
     * The returned string is implementation dependent.
     * It is usually provided for debugging purposes.
     */
    public String toString() {
        final StringBuffer buffer = new StringBuffer(Utilities.getShortClassName(this));
        buffer.append("[\"");
        buffer.append(name);
        buffer.append("\":[");
        buffer.append(minSample);
        buffer.append("..");
        buffer.append(maxSample); // Inclusive
        buffer.append("]]");
        return buffer.toString();
    }
}
