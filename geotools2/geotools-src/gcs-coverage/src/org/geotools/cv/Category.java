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
import java.io.ObjectStreamException;

// JAI dependencies
import javax.media.jai.util.Range;

// OpenGIS dependencies
import org.opengis.cv.CV_SampleDimension;

// Geotools dependencies
import org.geotools.pt.Matrix;
import org.geotools.ct.MathTransform;
import org.geotools.ct.MathTransform1D;
import org.geotools.ct.TransformException;
import org.geotools.ct.MathTransformFactory;

// Resources
import org.geotools.util.WeakHashSet;
import org.geotools.resources.XMath;
import org.geotools.resources.Utilities;
import org.geotools.resources.gcs.Resources;
import org.geotools.resources.gcs.ResourceKeys;


/**
 * A category delimited by a range of sample values. A categogy may be either
 * <em>qualitative</em> or <em>quantitative</em>.   For exemple, a classified
 * image may have a qualitative category defining sample value <code>0</code>
 * as water. An other qualitative category may defines sample value <code>1</code>
 * as forest, etc.  An other image may define elevation data as sample values
 * in the range <code>[0..100]</code>.   The later is a <em>quantitative</em>
 * category, because sample values are related to some measurement in the real
 * world. For example, elevation data may be related to an altitude in metres
 * through the following linear relation:
 *
 * <var>altitude</var>&nbsp;=&nbsp;<var>sample&nbsp;value</var>&times;100.
 * 
 * Some image mixes both qualitative and quantitative categories. For example,
 * images of Sea Surface Temperature  (SST)  may have a quantitative category
 * for temperature with values ranging from –2 to 35°C,  and three qualitative
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
 * <br><br>
 * All <code>Category</code> objects are immutable and thread-safe.
 *
 * @version $Id: Category.java,v 1.7 2002/07/29 15:15:27 desruisseaux Exp $
 * @author Martin Desruisseaux
 */
public class Category implements Serializable {
    /**
     * Serial number for interoperability with different versions.
     */
    private static final long serialVersionUID = 3718595090877043700L;

    /**
     * A set of {@link Category} or {@link CategoryList} objects. Used in order to
     * canonicalize object during deserialization, or as a memory optimisation in
     * {@link CategoryList} constructor.
     */
    static final WeakHashSet pool = new WeakHashSet();
    
    /**
     * The category name (may not be localized).
     */
    private final String name;
    
    /**
     * The minimal sample value (inclusive). This category is made of all values
     * in the range <code>minimum</code> to <code>maximum</code> inclusive.
     *
     * If this category is an instance of <code>GeophysicsCategory</code>,
     * then this field is the minimal geophysics value in this category.
     * For qualitative categories, the geophysics value is one of <code>NaN</code> values.
     */
    final double minimum;
    
    /**
     * The maximal sample value (inclusive). This category is made of all values
     * in the range <code>minimum</code> to <code>maximum</code> inclusive.
     *
     * If this category is an instance of <code>GeophysicsCategory</code>,
     * then this field is the maximal geophysics value in this category.
     * For qualitative categories, the geophysics value is one of <code>NaN</code> values.
     */
    final double maximum;

    /**
     * The range of values <code>[minimum..maximum]</code>.
     * May be computed only when first requested.
     */
    Range range;

    /**
     * The math transform from sample to geophysics values (never <code>null</code>).
     *
     * If this category is an instance of <code>GeophysicsCategory</code>, then this transform
     * is the inverse (as computed by {@link MathTransform#inverse()}, except for qualitative
     * categories. Since {@link #getSampleToGeophysics} returns <code>null</code> for
     * qualitative categories, this difference is not visible to the user.
     *
     * @see CV_SampleDimension#getScale()
     * @see CV_SampleDimension#getOffset()
     */
    final MathTransform1D transform;

    /**
     * A reference to the {@link GeophysicsCategory}. If this category is already an
     * instance of {@link GeophysicsCategory}, then <code>inverse</code> is a reference
     * to the {@link Category} object that own it.
     */
    transient Category inverse;
    
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
     * A set of default category colors.
     */
    private static final Color[] CYCLE = {
        Color.blue,    Color.red,   Color.orange, Color.yellow,    Color.pink,
        Color.magenta, Color.green, Color.cyan,   Color.lightGray, Color.gray
    };
    
    /**
     * Construct a qualitative category for sample value <code>sample</code>.
     *
     * @param  name    The category name.
     * @param  color   The category color, or <code>null</code> for a default color.
     * @param  sample  The sample value as an integer, usually in the range 0 to 255.
     */
    public Category(final String name,
                    final Color  color,
                    final int    sample)
    {
        this(name, toARGB(color, sample), new Integer(sample));
        assert minimum == sample : minimum;
        assert maximum == sample : maximum;
    }
    
    /**
     * Construct a qualitative category for sample value <code>sample</code>.
     *
     * @param  name    The category name.
     * @param  color   The category color, or <code>null</code> for a default color.
     * @param  sample  The sample value as a double. May be one of <code>NaN</code> values.
     */
    public Category(final String name,
                    final Color  color,
                    final double sample)
    {
        this(name, toARGB(color, (int)sample), new Double(sample));
        assert Double.doubleToRawLongBits(minimum) == Double.doubleToRawLongBits(sample) : minimum;
        assert Double.doubleToRawLongBits(maximum) == Double.doubleToRawLongBits(sample) : maximum;
    }
    
    /**
     * Construct a qualitative category for sample value <code>sample</code>.
     */
    private Category(final String     name,
                     final int[]      ARGB,
                     final Comparable sample)
    {
        this(name, ARGB, new Range(sample.getClass(), sample, sample), null);
        assert Double.isNaN(inverse.minimum) : inverse.minimum;
        assert Double.isNaN(inverse.maximum) : inverse.maximum;
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
    public Category(final String  name,
                    final Color[] colors,
                    final int     lower,
                    final int     upper,
                    final double  scale,
                    final double  offset) throws IllegalArgumentException
    {
        this(name, colors,
             new Range(Integer.class, new Integer(lower), true, new Integer(upper), false),
             scale, offset);
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
     * @param  sampleValueRange The range of sample values for this category. Element class
     *                 is usually {@link Integer}, but {@link Float} and {@link Double} are
     *                 accepted as well.
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
    public Category(final String  name,
                    final Color[] colors,
                    final Range   sampleValueRange,
                    final double  scale,
                    final double  offset) throws IllegalArgumentException
    {
        this(name, colors, sampleValueRange, createLinearTransform(scale, offset));
        try {
            assert Double.doubleToLongBits(transform.derivative(0)) == Double.doubleToLongBits(scale);
            assert Double.doubleToLongBits(transform.transform (0)) == Double.doubleToLongBits(offset);
        } catch (TransformException exception) {
            throw new AssertionError(exception);
        }
        if (Double.isNaN(scale) || Double.isInfinite(scale)) {
            throw new IllegalArgumentException(Resources.format(
                    ResourceKeys.ERROR_BAD_COEFFICIENT_$2, "scale", new Double(scale)));
        }
        if (Double.isNaN(offset) || Double.isInfinite(offset)) {
            throw new IllegalArgumentException(Resources.format(
                    ResourceKeys.ERROR_BAD_COEFFICIENT_$2, "offset", new Double(offset)));
        }
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
    public Category(final String  name,
                    final Color[] colors,
                    final Range   sampleValueRange,
                    final Range   geophysicsValueRange) throws IllegalArgumentException
    {
        this(name, colors, sampleValueRange,
             createLinearTransform(sampleValueRange, geophysicsValueRange));
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
    public Category(final String          name,
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
    private Category(final String    name,
                     final int[]     ARGB,
                     final Range     range,
                     MathTransform1D sampleToGeophysics) throws IllegalArgumentException
    {
        this.name      = name.trim();
        this.ARGB      = ARGB;
        this.range     = range;
        Class type     = range.getElementClass();
        boolean minInc = range.isMinIncluded();
        boolean maxInc = range.isMaxIncluded();
        this.minimum   = doubleValue(type, range.getMinValue(), minInc ? 0 : +1);
        this.maximum   = doubleValue(type, range.getMaxValue(), maxInc ? 0 : -1);
        /*
         * If we are constructing a qualitative category for a single NaN value,
         * accept it as a valid one. We will consider it as a geophysics category.
         */
        if (sampleToGeophysics==null && minInc && maxInc && Double.isNaN(minimum) &&
            Double.doubleToRawLongBits(minimum) == Double.doubleToRawLongBits(maximum))
        {
            inverse   = this;
            transform = createLinearTransform(0, minimum);
            return;
        }
        /*
         * Check the arguments. Use '!' in comparaison in order to reject NaN values,
         * except for the legal case catched by the "if" block just above.
         */
        if (!(minimum<=maximum) || Double.isInfinite(minimum) || Double.isInfinite(maximum))
        {
            throw new IllegalArgumentException(Resources.format(
                    ResourceKeys.ERROR_BAD_RANGE_$2, range.getMinValue(), range.getMaxValue()));
        }
        /*
         * Now initialize the geophysics category.
         */
        TransformException cause = null;
        try {
            if (sampleToGeophysics == null) {
                inverse = new GeophysicsCategory(this, false);
                transform = createLinearTransform(0, inverse.minimum); // sample to geophysics
                return;
            }
            transform = sampleToGeophysics; // Must be set before GeophysicsCategory construction!
            if (sampleToGeophysics.isIdentity()) {
                inverse = this;
            } else {
                inverse = new GeophysicsCategory(this, true);
            }
            if (inverse.minimum <= inverse.maximum) {
                return;
            }
            // If we reach this point, geophysics range is NaN. This is an illegal argument.
        } catch (TransformException exception) {
            cause = exception;
        }
        IllegalArgumentException exception = new IllegalArgumentException(Resources.format(
            ResourceKeys.ERROR_BAD_TRANSFORM_$1, Utilities.getShortClassName(sampleToGeophysics)));
        exception.initCause(cause);
        throw exception;
    }

    /**
     * Construct a geophysics category. <strong>This constructor should never
     * be invoked outside {@link GeophysicsCategory} constructor.</strong>
     *
     * @param  inverse The originating {@link Category}.
     * @param  isQuantitative <code>true</code> if the originating category is quantitative.
     * @throws TransformException if a transformation failed.
     */
    Category(final Category inverse, final boolean isQuantitative) throws TransformException {
        assert  (this    instanceof GeophysicsCategory);
        assert !(inverse instanceof GeophysicsCategory);
        this.inverse = inverse;
        this.name    = inverse.name;
        this.ARGB    = inverse.ARGB;
        if (!isQuantitative) {
            minimum = maximum = toNaN((int) Math.round((inverse.minimum + inverse.maximum)/2));
            transform = createLinearTransform(0, inverse.minimum); // geophysics to sample
            return;
        }
        /*
         * Compute 'minimum' and 'maximum' (which must be real numbers) using the transformation
         * from sample to geophysics values. To be strict, we should use some numerical algorithm
         * for finding a function's minimum and maximum. For linear and logarithmic functions,
         * minimum and maximum are always at the bounding input values, so we are using a very
         * simple algorithm for now.
         */
        transform = (MathTransform1D) inverse.transform.inverse();
        final double min = inverse.transform.transform(inverse.minimum);
        final double max = inverse.transform.transform(inverse.maximum);
        if (min > max) {
            minimum = max;
            maximum = min;
        } else {
            minimum = min;
            maximum = max;
        }
    }

    /**
     * Returns a linear transform with the supplied scale and offset values.
     *
     * @param scale  The scale factor. May be 0 for a constant transform.
     * @param offset The offset value. May be NaN if this method is invoked from a constructor
     *               for initializing {@link #transform} for a qualitative category.
     */
    private static MathTransform1D createLinearTransform(final double scale, final double offset) {
        Matrix matrix = new Matrix(2,2);
        matrix.setElement(0,0, scale);
        matrix.setElement(0,1, offset);
        return (MathTransform1D)MathTransformFactory.getDefault().createAffineTransform(matrix);
    }

    /**
     * Create a linear transform mapping values from <code>sampleValueRange</code>
     * to <code>geophysicsValueRange</code>.
     */
    private static MathTransform1D createLinearTransform(final Range sampleValueRange,
                                                         final Range geophysicsValueRange)
    {
        if (sampleValueRange.isMinIncluded() != geophysicsValueRange.isMinIncluded() ||
            sampleValueRange.isMaxIncluded() != geophysicsValueRange.isMaxIncluded())
        {
            throw new IllegalArgumentException();
        }
        final Class  type      = sampleValueRange.getElementClass();
        final double minSample = doubleValue(type,     sampleValueRange.getMinValue(), 0);
        final double maxSample = doubleValue(type,     sampleValueRange.getMaxValue(), 0);
        final double minValue  = doubleValue(type, geophysicsValueRange.getMinValue(), 0);
        final double maxValue  = doubleValue(type, geophysicsValueRange.getMaxValue(), 0);
        final double scale     = (maxValue-minValue) / (maxSample-minSample);
        final double offset    = minValue - scale*minSample;
        return createLinearTransform(scale, offset);
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
    private static double doubleValue(final Class        type,
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
            } else if (isInteger(type)) {
                value += direction;
            } else {
                throw new IllegalArgumentException(Resources.format(
                          ResourceKeys.ERROR_UNSUPPORTED_DATA_TYPE));
            }
        }
        return value;
    }

    /**
     * Check if <code>type</code> is one of integer types.
     *
     * @param  type The type to test.
     * @return <code>true</code> if <code>type</code> is a class {@link Long},
     *         {@link Integer}, {@link Short} or {@link Byte}.
     */
    static boolean isInteger(final Class type) {
        return Long.class.isAssignableFrom(type) ||
            Integer.class.isAssignableFrom(type) ||
              Short.class.isAssignableFrom(type) ||
               Byte.class.isAssignableFrom(type);
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
            throw new IndexOutOfBoundsException(Integer.toHexString(index));
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
    private static int[] toARGB(final Color[] colors) {
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
     * Returns ARGB values for the specified color. If <code>color</code>
     * is null, a default ARGB code will be returned.
     */
    private static int[] toARGB(Color color, final int sample) {
        if (color==null) {
            color = CYCLE[Math.abs(sample) % CYCLE.length];
        }
        return toARGB(new Color[] {color});
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
     * Returns the range of sample values occurring in this category. Sample values can be
     * transformed into geophysics values using the {@link #getSampleToGeophysics} transform.
     *
     * @return The range of sample values.
     *
     * @see SampleDimension#getMinimumValue()
     * @see SampleDimension#getMaximumValue()
     */
    public Range getRange() {
        return range;
    }

    /**
     * Returns a transform from sample values to geophysics values. If this category
     * is not a quantitative one, then this method returns <code>null</code>.
     */
    public MathTransform1D getSampleToGeophysics() {
        return isQuantitative() ? transform : null;
    }
    
    /**
     * Returns <code>true</code> if this category is quantitative. A quantitative category
     * has a non-null {@link #getSampleToGeophysics() sampleToGeophysics} transform.
     *
     * @return <code>true</code> if this category is quantitative, or
     *         <code>false</code> if this category is qualitative.
     */
    public boolean isQuantitative() {
        return !Double.isNaN(inverse.minimum) && !Double.isNaN(inverse.maximum);
    }
    
    /**
     * Returns a new category for the same range of sample values but
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
        final Category newCategory = new Category(name, newARGB, range, getSampleToGeophysics());
        newCategory.inverse.range = inverse.range; // Share a common instance.
        return newCategory;
    }

    /**
     * If <code>true</code>, returns a category with sample values equals to geophysics values. In
     * any such <cite>geophysics category</cite>, {@link #getSampleToGeophysics sampleToGeophysics}
     * is the identity transform by definition. The following rules hold:
     *
     * <ul>
     *   <li><code>geophysics(true).getSampleToGeophysics()</code> always returns the identity
     *       transform, or <code>null</code> if this category is a qualitative one.</li>
     *   <li><code>geophysics(false)</code> returns the original category. In other words,
     *       it cancel a previous call to <code>geophysics(true)</code>.</li>
     *   <li>In <code>geophysics(b).geophysics(b)</code>, the second call has no effect
     *       if <var>b</var> has the same value.</li>
     *   <li><code>geophysics(false)</code> has no effect if <code>geophysics(true)</code>
     *       has never been invoked. In other words, the default state after {@link Category}
     *       construction is <code>geophysics(false)</code>.</li>
     *   <li><code>geophysics(true).getRange()</code> returns the range of geophysics values, as
     *       transformed by the {@link #getSampleToGeophysics sampleToGeophysics} transform.</li>
     *   <li><code>geophysics(false).getRange()</code> returns the range of original sample values
     *       (usually integers).</li>
     * </ul>
     *
     * @param  toGeophysics <code>true</code> to gets a category with sample matching geophysics
     *         values, or <code>false</code> to get back the original category.
     * @return The category. Never <code>null</code>, but may be <code>this</code>.
     *
     * @see SampleDimension#geophysics
     * @see org.geotools.gc.GridCoverage#geophysics
     */
    public Category geophysics(final boolean toGeophysics) {
        return toGeophysics ? inverse : this;
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
            if (Double.doubleToRawLongBits(minimum)== Double.doubleToRawLongBits(that.minimum) &&
                Double.doubleToRawLongBits(maximum)== Double.doubleToRawLongBits(that.maximum) &&
                Utilities.equals(this.transform, that.transform) &&
                Utilities.equals(this.name,      that.name ) &&
                   Arrays.equals(this.ARGB,      that.ARGB ))
            {
                // Special test for 'range', since 'GeophysicsCategory'
                // computes it only when first needed.
                if (this.range!=null && that.range!=null) {
                    if (!Utilities.equals(this.range, that.range)) {
                        return false;
                    }
                    if (inverse instanceof GeophysicsCategory) {
                        assert inverse.equals(that.inverse);
                    }
                    return true;
                }
                assert (this instanceof GeophysicsCategory);
                return true;
            }
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
        buffer.append("(\"");
        buffer.append(name);
        buffer.append("\":[");
        buffer.append(minimum);
        buffer.append("..");
        buffer.append(maximum); // Inclusive
        buffer.append("])");
        return buffer.toString();
    }

    /**
     * Serialize a single instance of this object.
     * This is an optimisation for speeding up RMI.
     *
     * We keep this method private because we don't need to canonicalize
     * <code>GeophysicsCategory</code> for most serialization/deserialization
     * operations. Canonicalizing {@link Category} is suffisient because
     * if two {@link Category} objects are not equal, then we are are sure
     * that their enclosed <code>GeophysicsCategory</code> are not equal neither.
     */
    private Object writeReplace() throws ObjectStreamException {
        return pool.canonicalize(this);
    }

    /**
     * Canonicalize this category after deserialization.
     * This is an attempt to reduce memory footprint. This
     * method is private for the same reason than <code>writeReplace()</code>.
     */
    private Object readResolve() throws ObjectStreamException {
        return pool.canonicalize(this);
    }
}
