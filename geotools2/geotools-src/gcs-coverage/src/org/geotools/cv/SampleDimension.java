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
import java.util.List;
import java.util.Locale;
import java.util.Arrays;
import java.io.Serializable;
import java.awt.RenderingHints;
import java.awt.image.DataBuffer; // For JavaDoc
import java.awt.image.ColorModel;
import java.awt.image.RenderedImage;
import java.awt.image.IndexColorModel;
import java.awt.image.renderable.ParameterBlock;
import java.awt.image.renderable.RenderedImageFactory;

// RMI and weak references
import java.rmi.RemoteException;
import java.rmi.server.RemoteObject;
import java.lang.ref.WeakReference;
import java.lang.ref.Reference;

// JAI dependencies
import javax.media.jai.JAI;
import javax.media.jai.CRIFImpl;
import javax.media.jai.util.Range;
import javax.media.jai.OperationRegistry;
import javax.media.jai.OperationDescriptorImpl;
import javax.media.jai.registry.RenderedRegistryMode;

// OpenGIS dependencies
import org.opengis.cs.CS_Unit;
import org.opengis.cv.CV_SampleDimension;
import org.opengis.cv.CV_SampleDimensionType;
import org.opengis.cv.CV_ColorInterpretation;
import org.opengis.cv.CV_PaletteInterpretation;

// Geotools dependencies
import org.geotools.ct.MathTransform1D;
import org.geotools.ct.TransformException;

// Resources
import org.geotools.units.Unit;
import org.geotools.resources.XArray;
import org.geotools.resources.Utilities;
import org.geotools.resources.RemoteProxy;
import org.geotools.resources.gcs.Resources;
import org.geotools.resources.gcs.ResourceKeys;


/**
 * Describes the data values for a coverage. For a grid coverage a sample dimension is a band.
 * Sample values in a band may be organized in categories.  This <code>SampleDimension</code>
 * implementation is capable to differenciate <em>qualitative</em> and <em>quantitative</em>
 * categories. For example an image of sea surface temperature (SST) could very well defines
 * the following categories:
 *
 * <blockquote>
 *   [0]       : no data
 *   [1]       : cloud
 *   [2]       : land
 *   [10..210] : temperature to be converted into Celsius degrees through a linear equation
 * </blockquote>
 *
 * In this example, sample values in range <code>[10..210]</code> defines a quantitative category,
 * while all others categories are qualitative. The difference between those two kinds of category
 * is that the {@link Category#getSampleToGeophysics} method returns a non-null transform if and
 * only if the category is quantitative.
 *
 * @version $Id: SampleDimension.java,v 1.15 2003/01/15 21:47:19 desruisseaux Exp $
 * @author <A HREF="www.opengis.org">OpenGIS</A>
 * @author Martin Desruisseaux
 *
 * @see org.opengis.cv.CV_SampleDimension
 */
public class SampleDimension implements Serializable {
    /**
     * Serial number for interoperability with different versions.
     */
    private static final long serialVersionUID = 6026936545776852758L;

    /**
     * A sample dimension wrapping the list of categories <code>CategoryList.inverse</code>.
     * This object is constructed and returned by {@link #geophysics}. Constructed when first
     * needed, but serialized anyway because it may be a user-supplied object.
     */
    private SampleDimension inverse;

    /**
     * The category list for this sample dimension,
     * or <code>null</code> if this sample dimension
     * has no category.
     */
    private final CategoryList categories;

    /**
     * <code>true</code> if all categories in this sample dimension have been already scaled
     * to geophysics ranges. If <code>true</code>, then the {@link #getSampleToGeophysics()}
     * method should returns an identity transform. Note that the opposite do not always hold:
     * an identity transform doesn't means that all categories are geophysics. For example,
     * some qualitative categories may map to some values differents than <code>NaN</code>.
     * <br><br>
     * Assertions:
     *  <ul>
     *    <li><code>isGeophysics</code> == <code>categories.isScaled(true)</code>.</li>
     *    <li><code>isGeophysics</code> != <code>categories.isScaled(false)</code>, except
     *        if <code>categories.geophysics(true) == categories.geophysics(false)</code></li>
     * </ul>
     */
    private final boolean isGeophysics;

    /**
     * <code>true</code> if this sample dimension has at least one qualitative category.
     * An arbitrary number of qualitative categories is allowed, providing their sample
     * value ranges do not overlap. A sample dimension can have both qualitative and
     * quantitative categories.
     */
    private final boolean hasQualitative;

    /**
     * <code>true</code> if this sample dimension has at least one quantitative category.
     * An arbitrary number of quantitative categories is allowed, providing their sample
     * value ranges do not overlap.
     * <br><br>
     * If <code>sampleToGeophysics</code> is non-null, then <code>hasQuantitative</code>
     * <strong>must</strong> be true.  However, the opposite do not hold in all cases: a
     * <code>true</code> value doesn't means that <code>sampleToGeophysics</code> should
     * be non-null.
     */
    private final boolean hasQuantitative;

    /**
     * The {@link Category#getSampleToGeophysics sampleToGeophysics} transform used by every
     * quantitative {@link Category}, or <code>null</code>. This field may be null for two
     * reasons:
     *
     * <ul>
     *   <li>There is no quantitative category in this sample dimension.</li>
     *   <li>There is more than one quantitative category, and all of them
     *       don't use the same {@link Category#getSampleToGeophysics
     *       sampleToGeophysics} transform.</li>
     * </ul>
     *
     * This field is used by {@link #getOffset} and {@link #getScale}. The
     * {@link #getSampleToGeophysics} method may also returns directly this
     * value in some conditions.
     */
    private final MathTransform1D sampleToGeophysics;

    /**
     * OpenGIS object returned by {@link #toOpenGIS}.
     * It may be a hard or a weak reference.
     */
    private transient Object proxy;

    /**
     * Construct a sample dimension with no category.
     */
    public SampleDimension() {
        this((CategoryList) null);
    }
    
    /**
     * Construct a sample dimension with a set of qualitative categories only.
     * This sample dimension will have no unit and a default set of colors.
     *
     * @param names  Sequence of category names for the values contained in a sample dimension.
     *               This allows for names to be assigned to numerical values. The first entry
     *               in the sequence relates to a cell value of zero. For example:
     *               [0]="Background", [1]="Water", [2]="Forest", [3]="Urban".
     */
    public SampleDimension(final String[] names) {
        // TODO: 'list(...)' should be inlined there if only Sun was to fix RFE #4093999
        //       ("Relax constraint on placement of this()/super() call in constructors").
        this(list(names));
    }
    
    /**
     * Construct a sample dimension with a set of qualitative categories only.
     * This sample dimension will have no unit.
     *
     * @param names  Sequence of category names for the values contained in a sample dimension.
     *               This allows for names to be assigned to numerical values. The first entry
     *               in the sequence relates to a cell value of zero. For example:
     *               [0]="Background", [1]="Water", [2]="Forest", [3]="Urban".
     * @param colors Color to assign to each category. This array must have the same
     *               length than <code>names</code>.
     */
    public SampleDimension(final String[] names, final Color[] colors) {
        // TODO: 'list(...)' should be inlined there if only Sun was to fix RFE #4093999
        //       ("Relax constraint on placement of this()/super() call in constructors").
        this(list(names, colors));
    }
    
    /**
     * Construct a sample dimension with an arbitrary set of categories, which may be both
     * quantitative and qualitative.  It is possible to specify more than one quantitative
     * categories, providing that their sample value ranges do not overlap.   Quantitative
     * categories can map sample values to geophysics values using arbitrary relation (not
     * necessarly linear).
     *
     * @param  categories The list of categories.
     * @param  units      The unit information for this sample dimension.
     *                    May be <code>null</code> if no category has units.
     *                    This unit apply to values obtained after the
     *                    {@link #getSampleToGeophysics sampleToGeophysics} transformation.
     * @throws IllegalArgumentException if <code>categories</code> contains incompatible
     *         categories. If may be the case for example if two or more categories have
     *         overlapping ranges of sample values.
     */
    public SampleDimension(Category[] categories, Unit units) throws IllegalArgumentException {
        // TODO: 'list(...)' should be inlined there if only Sun was to fix RFE #4093999
        //       ("Relax constraint on placement of this()/super() call in constructors").
        this(list(categories, units));
    }

    /**
     * Construct a sample dimension with the specified list of categories.
     *
     * @param list The list of categories, or <code>null</code>.
     */
    private SampleDimension(final CategoryList list) {
        MathTransform1D main = null;
        boolean  isMainValid = true;
        boolean  qualitative = false;
        if (list != null) {
            for (int i=list.size(); --i>=0;) {
                final MathTransform1D candidate = ((Category)list.get(i)).getSampleToGeophysics();
                if (candidate == null) {
                    qualitative = true;
                    continue;
                }
                if (main != null) {
                    isMainValid &= main.equals(candidate);
                }
                main = candidate;
            }
            this.isGeophysics = list.isScaled(true);
        } else {
            this.isGeophysics = false;
        }
        this.categories         = list;
        this.hasQualitative     = qualitative;
        this.hasQuantitative    = (main != null);
        this.sampleToGeophysics = isMainValid ? main : null;
    }

    /**
     * Construct a new sample dimension with the same categories and
     * units than the specified sample dimension.
     *
     * @param other The other sample dimension, or <code>null</code>.
     */
    protected SampleDimension(final SampleDimension other) {
        if (other != null) {
            inverse            = other.inverse;
            categories         = other.categories;
            isGeophysics       = other.isGeophysics;
            hasQualitative     = other.hasQualitative;
            hasQuantitative    = other.hasQuantitative;
            sampleToGeophysics = other.sampleToGeophysics;
        } else {
            // 'inverse' will be set when needed.
            categories         = null;
            isGeophysics       = false;
            hasQualitative     = false;
            hasQuantitative    = false;
            sampleToGeophysics = null;
        }
    }
    
    /**
     * Construct a list of categories.
     * Used by constructors only.
     */
    private static CategoryList list(final String[] names) {
        final Color[] colors = new Color[names.length];
        final double scale = 255.0/colors.length;
        for (int i=0; i<colors.length; i++) {
            final int r = (int)Math.round(scale*i);
            colors[i] = new Color(r,r,r);
        }
        return list(names, colors);
    }
    
    /**
     * Construct a list of categories.
     * Used by constructors only.
     */
    private static CategoryList list(final String[] names, final Color[] colors) {
        if (names.length != colors.length) {
            throw new IllegalArgumentException(
                    Resources.format(ResourceKeys.ERROR_MISMATCHED_ARRAY_LENGTH));
        }
        final Category[] categories = new Category[names.length];
        for (int i=0; i<categories.length; i++) {
            categories[i] = new Category(names[i], colors[i], i);
        }
        return list(categories, null);
    }

    /**
     * Construct a list of categories.
     * Used by constructors only.
     */
    private static CategoryList list(final Category[] categories, final Unit units) {
        if (categories == null) {
            return null;
        }
        CategoryList list = new CategoryList(categories, units);
        list = (CategoryList) Category.pool.canonicalize(list);
        if (CategoryList.isScaled(categories, false)) return list;
        if (CategoryList.isScaled(categories, true )) return list.inverse;
        throw new IllegalArgumentException(Resources.format(ResourceKeys.ERROR_MIXED_CATEGORIES));
    }

    /**
     * Returns a code value indicating grid value data type.
     * This will also indicate the number of bits for the data type.
     *
     * @return a code value indicating grid value data type.
     */
    public SampleDimensionType getSampleDimensionType() {
        final Range range = getRange();
        if (range == null) {
            return SampleDimensionType.FLOAT;
        }
        return SampleDimensionType.getEnum(range);
    }
    
    /**
     * Get the sample dimension title or description.
     * This string may be <code>null</code> if no description is present.
     *
     * @param  locale The locale, or <code>null</code> for the default one.
     * @return The localized description, or <code>null</code> if none.
     *         If no description was available in the specified locale,
     *         then a default locale is used.
     *
     * @see CV_SampleDimension#getDescription()
     */
    public String getDescription(final Locale locale) {
        return (categories!=null) ? categories.getName(locale) : null;
    }

    /**
     * Returns a sequence of category names for the values contained in this sample dimension.
     * This allows for names to be assigned to numerical values. The first entry in the sequence
     * relates to a cell value of zero. For example:
     *
     *  <blockquote><pre>
     *    [0] Background
     *    [1] Water
     *    [2] Forest
     *    [3] Urban
     *  </pre></blockquote>
     *
     * @param  locale The locale for category names, or <code>null</code> for a default locale.
     * @return The sequence of category names for the values contained in this sample dimension,
     *         or <code>null</code> if there is no category in this sample dimension.
     * @throws IllegalStateException if a sequence can't be mapped because some category use
     *         negative or non-integer sample values.
     *
     * @see CV_SampleDimension#getCategoryNames()
     * @see #getCategories
     * @see #getCategory
     */
    public String[] getCategoryNames(final Locale locale) throws IllegalStateException {
        if (categories == null) {
            return null;
        }
        if (categories.isEmpty()) {
            return new String[0];
        }
        String[] names = null;
        for (int i=categories.size(); --i>=0;) {
            final Category category = (Category) categories.get(i);
            final int lower = (int) category.minimum;
            final int upper = (int) category.maximum;
            if (lower!=category.minimum || lower<0 ||
                upper!=category.maximum || upper<0)
            {
                final Resources resources = Resources.getResources(locale);
                throw new IllegalStateException(Resources.format(
                        ResourceKeys.ERROR_NON_INTEGER_CATEGORY));
            }
            if (names == null) {
                names = new String[upper+1];
            }
            Arrays.fill(names, lower, upper+1, category.getName(locale));
        }
        return names;
    }
    
    /**
     * Returns all categories in this sample dimension. Note that a {@link Category} object may
     * apply to an arbitrary range of sample values.    Consequently, the first element in this
     * collection may not be directly related to the sample value <code>0</code>.
     *
     * @return The list of categories in this sample dimension, or <code>null</code> if none.
     *
     * @see #getCategoryNames
     * @see #getCategory
     */
    public List getCategories() {
        return categories;
    }
    
    /**
     * Returns the category for the specified sample value. If this method can't maps
     * a category to the specified value, then it returns <code>null</code>.
     *
     * @param  sample The value (can be one of <code>NaN</code> values).
     * @return The category for the supplied value, or <code>null</code> if none.
     *
     * @see #getCategories
     * @see #getCategoryNames
     */
    public Category getCategory(final double sample) {
        return (categories!=null) ? categories.getCategory(sample) : null;
    }
    
    /**
     * Returns a string representation of a sample value. This method try to returns
     * a representation of the geophysics value; the transformation is automatically
     * applied when necessary. More specifically:
     *
     * <ul>
     *   <li>If <code>value</code> maps a qualitative category, then the
     *       category name is returned as of {@link Category#getName(Locale)}.</li>
     *
     *   <li>Otherwise, if <code>value</code> maps a quantitative category, then the value is
     *       transformed into a geophysics value as with the {@link #getSampleToGeophysics()
     *       sampleToGeophysics} transform, the result is formatted as a number and the unit
     *       symbol is appened.</li>
     * </ul>
     *
     * @param  value  The sample value (can be one of <code>NaN</code> values).
     * @param  locale Locale to use for formatting, or <code>null</code> for the default locale.
     * @return A string representation of the geophysics value, or <code>null</code> if there is
     *         none.
     *
     * @task REVISIT: What should we do when the value can't be formatted?
     *                <code>SampleDimension</code> returns <code>null</code> if there is no
     *                category or if an exception is thrown, but <code>CategoryList</code>
     *                returns "Untitled" if the value is an unknow NaN, and try to format
     *                the number anyway in other cases.
     */
    public String getLabel(final double value, final Locale locale) {
        if (categories != null) {
            if (isGeophysics) {
                return categories.format(value, locale);
            } else try {
                return categories.inverse.format(categories.transform(value), locale);
            } catch (TransformException exception) {
                // Value probably don't match a category. Ignore...
            }
        }
        return null;
    }

    /**
     * Returns the values to indicate "no data" for this sample dimension.  The default
     * implementation deduces the "no data" values from the list of categories supplied
     * at construction time. The rules are:
     *
     * <ul>
     *   <li>If {@link #getSampleToGeophysics} returns <code>null</code>, then
     *       <code>getNoDataValue()</code> returns <code>null</code> as well.
     *       This means that this sample dimension contains no category or contains
     *       only qualitative categories (e.g. a band from a classified image).</li>
     *
     *   <li>If {@link #getSampleToGeophysics} returns an identity transform,
     *       then <code>getNoDataValue()</code> returns <code>null</code>.
     *       This means that sample value in this sample dimension are already
     *       expressed in geophysics values and that all "no data" values (if any)
     *       have already been converted into <code>NaN</code> values.</li>
     *
     *   <li>Otherwise, if there is at least one quantitative category, returns the sample values
     *       of all non-quantitative categories. For example if "Temperature" is a quantitative
     *       category and "Land" and "Cloud" are two qualitative categories, then sample values
     *       for "Land" and "Cloud" will be considered as "no data" values. "No data" values
     *       that are already <code>NaN</code> will be ignored.</li>
     * </ul>
     *
     * Together with {@link #getOffset()} and {@link #getScale()}, this method provides a limited
     * way to transform sample values into geophysics values. However, the recommended way is to
     * use the {@link #getSampleToGeophysics sampleToGeophysics} transform instead, which is more
     * general and take care of converting automatically "no data" values into <code>NaN</code>.
     *
     * @return The values to indicate no data values for this sample dimension,
     *         or <code>null</code> if not applicable.
     * @throws IllegalStateException if some qualitative categories use a range of
     *         non-integer values.
     *
     * @see CV_SampleDimension#getNoDataValue()
     * @see #getSampleToGeophysics
     */
    public double[] getNoDataValue() throws IllegalStateException {
        if (!hasQuantitative) {
            return null;
        }
        int count = 0;
        double[] padValues = null;
        final int size = categories.size();
        for (int i=0; i<size; i++) {
            final Category category = (Category) categories.get(i);
            if (!category.isQuantitative()) {
                final double min = category.minimum;
                final double max = category.maximum;
                if (!Double.isNaN(min) || !Double.isNaN(max)) {
                    if (padValues == null) {
                        padValues = new double[size-i];
                    }
                    if (count >= padValues.length) {
                        padValues = XArray.resize(padValues, count*2);
                    }
                    padValues[count++] = min;
                    /*
                     * The "no data" value has been extracted. Now, check if we have a range
                     * of "no data" values instead of a single one for this category.  If we
                     * have a single value, it can be of any type. But if we have a range,
                     * then it must be a range of integers (otherwise we can't expand it).
                     */
                    if (max != min) {
                        int lower = (int) min;
                        int upper = (int) max;
                        if (lower!=min || upper!=max ||
                            !Category.isInteger(category.getRange().getElementClass()))
                        {
                            throw new IllegalStateException(Resources.format(
                                    ResourceKeys.ERROR_NON_INTEGER_CATEGORY));
                        }
                        final int requiredLength = count + (upper-lower);
                        if (requiredLength > padValues.length) {
                            padValues = XArray.resize(padValues, requiredLength*2);
                        }
                        while (++lower <= upper) {
                            padValues[count++] = lower;
                        }
                    }
                }
            }
        }
        if (padValues != null) {
            padValues = XArray.resize(padValues, count);
        }
        return padValues;
    }
    
    /**
     * Returns the minimum value occurring in this sample dimension.
     * The default implementation fetch this value from the categories supplied at
     * construction time. If the minimum value can't be computed, then this method
     * returns {@link Double#NEGATIVE_INFINITY}.
     *
     * @see CV_SampleDimension#getMinimumValue()
     * @see #getRange
     */
    public double getMinimumValue() {
        if (categories!=null && !categories.isEmpty()) {
            final double value = ((Category) categories.get(0)).minimum;
            if (!Double.isNaN(value)) {
                return value;
            }
        }
        return Double.NEGATIVE_INFINITY;
    }
    
    /**
     * Returns the maximum value occurring in this sample dimension.
     * The default implementation fetch this value from the categories supplied at
     * construction time. If the maximum value can't be computed, then this method
     * returns {@link Double#POSITIVE_INFINITY}.
     *
     * @see CV_SampleDimension#getMaximumValue()
     * @see #getRange
     */
    public double getMaximumValue() {
        if (categories!=null) {
            for (int i=categories.size(); --i>=0;) {
                final double value = ((Category) categories.get(i)).maximum;
                if (!Double.isNaN(value)) {
                    return value;
                }
            }
        }
        return Double.POSITIVE_INFINITY;
    }
    
    /**
     * Returns the range of values in this sample dimension. This is the union of the range of
     * values of every categories, excluding <code>NaN</code> values. A {@link Range} object
     * gives more informations than {@link #getMinimumValue} and {@link #getMaximumValue} methods
     * since it contains also the data type (integer, float, etc.) and inclusion/exclusion
     * informations.
     *
     * @return The range of values. May be <code>null</code> if this sample dimension has no
     *         quantitative category.
     *
     * @see Category#getRange
     * @see #getMinimumValue
     * @see #getMaximumValue
     *
     * @task TODO: We should do a better job in CategoryList.getRange() when selecting
     *       the appropriate data type. SampleDimensionType.getEnum(Range) may be of
     *       some help.
     */
    public Range getRange() {
        return (categories!=null) ? categories.getRange() : null;
    }
    
    /**
     * Returns the unit information for this sample dimension.
     * May returns <code>null</code> if this dimension has no units.
     * This unit apply to values obtained after the {@link #getSampleToGeophysics
     * sampleToGeophysics} transformation.
     *
     * @see CV_SampleDimension#getUnits()
     * @see #getSampleToGeophysics
     */
    public Unit getUnits() {
        return (categories!=null) ? categories.geophysics(true).getUnits() : null;
    }

    /**
     * Returns the value to add to grid values for this sample dimension.
     * This attribute is typically used when the sample dimension represents
     * elevation data. The transformation equation is:
     *
     * <blockquote><pre>offset + scale*sample</pre></blockquote>
     *
     * Together with {@link #getScale()} and {@link #getNoDataValue()}, this method provides a
     * limited way to transform sample values into geophysics values. However, the recommended
     * way is to use the {@link #getSampleToGeophysics sampleToGeophysics} transform instead,
     * which is more general and take care of converting automatically "no data" values into
     * <code>NaN</code>.
     *
     * @return the offset to add to grid values.
     * @throws IllegalStateException if the transform from sample to geophysics values
     *         is not a linear relation.
     *
     * @see CV_SampleDimension#getOffset()
     * @see #getSampleToGeophysics
     */
    public double getOffset() throws IllegalStateException {
        return getCoefficient(0);
    }

    /**
     * Returns the value which is multiplied to grid values for this sample dimension.
     * This attribute is typically used when the sample dimension represents elevation
     * data. The transformation equation is:
     *
     * <blockquote><pre>offset + scale*sample</pre></blockquote>
     *
     * Together with {@link #getOffset()} and {@link #getNoDataValue()}, this method provides a
     * limited way to transform sample values into geophysics values. However, the recommended
     * way is to use the {@link #getSampleToGeophysics sampleToGeophysics} transform instead,
     * which is more general and take care of converting automatically "no data" values into
     * <code>NaN</code>.
     *
     * @return the scale to multiply to grid value.
     * @throws IllegalStateException if the transform from sample to geophysics values
     *         is not a linear relation.
     *
     * @see CV_SampleDimension#getScale()
     * @see #getSampleToGeophysics
     */
    public double getScale() {
        return getCoefficient(1);
    }

    /**
     * Returns a coefficient of the linear transform from sample to geophysics values.
     *
     * @param  order The coefficient order (0 for the offset, or 1 for the scale factor,
     *         2 if we were going to implement quadratic relation, 3 for cubic, etc.).
     * @return The coefficient.
     * @throws IllegalStateException if the transform from sample to geophysics values
     *         is not a linear relation.
     */
    private double getCoefficient(final int order) throws IllegalStateException {
        if (!hasQuantitative) {
            // Default value for "offset" is 0; default value for "scale" is 1.
            // This is equal to the order if 0 <= order <= 1.
            return order;
        }
        Exception cause = null;
        if (sampleToGeophysics != null) try {
            final double value;
            switch (order) {
                case 0:  value = sampleToGeophysics.transform(0); break;
                case 1:  value = sampleToGeophysics.derivative(Double.NaN); break;
                default: throw new AssertionError(order); // Should not happen
            }
            if (!Double.isNaN(value)) {
                return value;
            }
        } catch (TransformException exception) {
            cause = exception;
        }
        IllegalStateException exception = new IllegalStateException(Resources.format(
                                              ResourceKeys.ERROR_NON_LINEAR_RELATION));
        exception.initCause(cause);
        throw exception;
    }

    /**
     * Returns a transform from sample values to geophysics values. If this sample dimension
     * has no category, then this method returns <code>null</code>. If all sample values are
     * already geophysics values (including <code>NaN</code> for "no data" values), then this
     * method returns an identity transform. Otherwise, this method returns a transform expecting
     * sample values as input and computing geophysics value as output. This transform will take
     * care of converting all "{@linkplain #getNoDataValue() no data values}" into
     * <code>NaN</code> values.
     * The <code>sampleToGeophysics.{@linkplain MathTransform1D#inverse() inverse()}</code>
     * transform is capable to differenciate <code>NaN</code> values to get back the original
     * sample value.
     *
     * @return The transform from sample to geophysics values, or <code>null</code> if this
     *         sample dimension do not defines any transform (which is not the same that
     *         defining an identity transform).
     */
    public MathTransform1D getSampleToGeophysics() {
        if (isGeophysics) {
            return GeophysicsCategory.IDENTITY;
        }
        if (!hasQualitative && sampleToGeophysics!=null) {
            // If there is only quantitative categories and they all use the same transform,
            // then we don't need the indirection level provided by CategoryList.
            return sampleToGeophysics;
        }
        // CategoryList is a MathTransform1D.
        return categories;
    }

    /**
     * If <code>true</code>, returns a <code>SampleDimension</code> with sample values
     * equals to geophysics values. In any such <cite>geophysics sample dimension</cite>,
     * {@link #getSampleToGeophysics sampleToGeophysics} is the identity transform by
     * definition. The following rules hold:
     *
     * <ul>
     *   <li><code>geophysics(true).getSampleToGeophysics()</code> always returns the identity
     *       transform.</li>
     *   <li><code>geophysics(false)</code> returns the original sample dimension. In other words,
     *       it cancel a previous call to <code>geophysics(true)</code>.</li>
     *   <li>In <code>geophysics(b).geophysics(b)</code>, the second call has no effect
     *       if <var>b</var> has the same value.</li>
     *   <li><code>geophysics(true).getRange()</code> returns the range of geophysics values, as
     *       transformed by the {@link #getSampleToGeophysics sampleToGeophysics} transform.</li>
     *   <li><code>geophysics(false).getRange()</code> returns the range of original sample values
     *       (usually integers).</li>
     * </ul>
     *
     * @param  toGeophysics <code>true</code> to gets a sample dimension with an identity
     *         transform, or <code>false</code> to get back the original sample dimension.
     * @return The sample dimension. Never <code>null</code>, but may be <code>this</code>.
     *
     * @see Category#geophysics
     * @see org.geotools.gc.GridCoverage#geophysics
     */
    public SampleDimension geophysics(final boolean toGeophysics) {
        if (toGeophysics == isGeophysics) {
            return this;
        }
        if (inverse == null) {
            if (categories != null) {
                inverse = new SampleDimension(categories.inverse);
                inverse.inverse = this;
            } else {
                /*
                 * If there is no categories, then there is no real difference between
                 * "geophysics" and "indexed" sample dimensions.  Both kinds of sample
                 * dimensions would be identical objects, so we are better to just
                 * returns 'this'.
                 */
                inverse = this;
            }
        }
        return inverse;
    }
    
    // NOTE: "getPaletteInterpretation()" is not available in Geotools since
    //       palette are backed by IndexColorModel, which support only RGB.
    
    /**
     * Returns the color interpretation of the sample dimension.
     * A sample dimension can be an index into a color palette or be a color model
     * component. If the sample dimension is not assigned a color interpretation
     * the value is {@link ColorInterpretation#UNDEFINED}.
     *
     * @see CV_SampleDimension#getColorInterpretation()
     *
     * @task TODO: the 'band' and 'numBands' parameters should be specified at construction time.
     */
    public ColorInterpretation getColorInterpretation() {
        final int band     = 0;
        final int numBands = 1;
        return ColorInterpretation.getEnum(getColorModel(band, numBands), band);
    }
    
    /**
     * Returns a color model for this sample dimension. The default implementation builds up the
     * color model using each category's colors (as returned by {@link Category#getColors}). The
     * returned color model will typically use data type {@link DataBuffer#TYPE_FLOAT} if this
     * sample model is for geophysical values, or an integer data type otherwise.
     *
     * @param  visibleBand The band to be made visible (usually 0). All other bands, if any
     *         will be ignored.
     * @param  numBands The number of bands for the color model (usually 1). The returned color
     *         model will renderer only the <code>visibleBand</code> and ignore the others, but
     *         the existence of all <code>numBands</code> will be at least tolerated. Supplemental
     *         bands, even invisible, are useful for processing with Java Advanced Imaging.
     * @return The requested color model, suitable for {@link RenderedImage} objects with values
     *         in the <code>{@link #getRange}</code> range. May be <code>null</code> if this
     *         sample dimension has no category.
     *
     * @task TODO: the 'band' and 'numBands' parameters should be specified at construction time.
     */
    public ColorModel getColorModel(final int visibleBand, final int numBands) {
        if (categories != null) {
            return categories.getColorModel(visibleBand, numBands);
        }
        return null;
    }
    
    /**
     * Returns a hash value for this sample dimension.
     * This value need not remain consistent between
     * different implementations of the same class.
     */
    public int hashCode() {
        return (categories!=null) ? categories.hashCode() : 23491;
    }
    
    /**
     * Compares the specified object with this sample dimension for equality.
     */
    public boolean equals(final Object object) {
        if (object == this) {
            // Slight optimization
            return true;
        }
        if (object instanceof SampleDimension) {
            final SampleDimension that = (SampleDimension) object;
            return Utilities.equals(this.categories, that.categories);
            // Since everything is deduced from CategoryList, two sample dimensions
            // should be equal if they have the same list of categories.
        }
        return false;
    }
    
    /**
     * Returns a string representation of this sample dimension.
     * This string is for debugging purpose only and may change
     * in future version. The default implementation format the
     * sample value range, then the list of categories. A "*"
     * mark is put in front of what seems the "main" category.
     */
    public String toString() {
        if (categories != null) {
            return categories.toString(this);
        } else {
            return Utilities.getShortClassName(this);
        }
    }




    //////////////////////////////////////////////////////////////////////////////////////
    ////////                                                                      ////////
    ////////        REGISTRATION OF "GC_SampleTranscoding" IMAGE OPERATION        ////////
    ////////                                                                      ////////
    //////////////////////////////////////////////////////////////////////////////////////
    /**
     * The operation descriptor for the "GC_SampleTranscoding" operation. This operation can
     * apply the {@link SampleDimension#getSampleToGeophysics sampleToGeophysics}  transform
     * on all pixels in all bands of an image. The transformations are supplied as a list of
     * {@link SampleDimension}s, one for each band. The supplied <code>SampleDimension</code>s
     * objects describe the categories in the <strong>source</strong> image. The target image
     * will matches sample dimension
     *
     *     <code>{@link SampleDimension#geophysics geophysics}(!isGeophysics)</code>,
     *
     * where <code>isGeophysics</code> is the previous state of the sample dimension.
     */
    private static final class Descriptor extends OperationDescriptorImpl {
        /**
         * Construct the descriptor.
         */
        public Descriptor() {
            super(new String[][]{{"GlobalName",  "GC_SampleTranscoding"},
                                 {"LocalName",   "GC_SampleTranscoding"},
                                 {"Vendor",      "geotools.org"},
                                 {"Description", "Transformation from sample to geophysics values"},
                                 {"DocURL",      "http://modules.geotools.org/gcs-coverage"},
                                 {"Version",     "1.0"}},
                  new String[]   {RenderedRegistryMode.MODE_NAME}, 1,
                  new String[]   {"sampleDimensions"},      // Argument names
                  new Class []   {SampleDimension[].class}, // Argument classes
                  new Object[]   {NO_PARAMETER_DEFAULT},    // Default values for parameters,
                  null // No restriction on valid parameter values.
            );
        }

        /**
         * Returne <code>true</code> if the parameters are valids. This implementation check
         * that the number of bands in the source image is equals to the number of supplied
         * sample dimensions, and that all sample dimensions has categories.
         */
        protected boolean validateParameters(final String modeName,
                                             final ParameterBlock args,
                                             final StringBuffer msg)
        {
            if (!super.validateParameters(modeName, args, msg)) {
                return false;
            }
            final RenderedImage  source = (RenderedImage)     args.getSource(0);
            final SampleDimension[] dim = (SampleDimension[]) args.getObjectParameter(0);
            final int numBands = source.getSampleModel().getNumBands();
            if (numBands != dim.length) {
                msg.append(Resources.format(ResourceKeys.ERROR_NUMBER_OF_BANDS_MISMATCH_$2,
                                            new Integer(numBands), new Integer(dim.length)));
                return false;
            }
            for (int i=0; i<numBands; i++) {
                if (dim[i].categories == null) {
                    msg.append(Resources.format(ResourceKeys.ERROR_BAD_PARAMETER_$2,
                                                "sampleDimensions["+i+"].categories", null));
                    return false;
                }
            }
            return true;
        }
    }

    /**
     * The {@link RenderedImageFactory} for the "GC_SampleTranscoding" operation.
     */
    private static final class CRIF extends CRIFImpl {
        /**
         * Creates a {@link RenderedImage} representing the results of an imaging
         * operation for a given {@link ParameterBlock} and {@link RenderingHints}.
         */
        public RenderedImage create(final ParameterBlock paramBlock,
                                    final RenderingHints renderHints)
        {
            final RenderedImage  source = (RenderedImage) paramBlock.getSource(0);
            final SampleDimension[] dim = (SampleDimension[]) paramBlock.getObjectParameter(0);
            final CategoryList[]   list = new CategoryList[dim.length];
            for (int i=0; i<list.length; i++) {
                list[i] = dim[i].categories;
            }
            return ImageAdapter.getInstance(source, list, JAI.getDefaultInstance());
        }
    }

    /**
     * Register the "GC_SampleTranscoding" image operation.
     * Registration is done when the class is first loaded.
     *
     * @task REVISIT: This static initializer will imply immediate class loading of a lot of
     *                JAI dependencies.  This is a pretty high overhead if JAI is not wanted
     *                right now. The correct approach is to declare the image operation into
     *                the <code>META-INF/registryFile.jai</code> file, which is automatically
     *                parsed during JAI initialization. Unfortunatly, it can't access private
     *                classes and we don't want to make our registration classes public. We
     *                can't move our registration classes into a hidden "resources" package
     *                neither because we need package-private access to <code>CategoryList</code>.
     *                For now, we assume that people using the GCS package probably want to work
     *                with {@link org.geotools.gc.GridCoverage}, which make extensive use of JAI.
     *                Peoples just working with {@link org.geotools.cv.Coverage} are stuck with
     *                the overhead. Note that we register the image operation here because the
     *                only operation's argument is of type <code>SampleDimension[]</code>.
     *                Consequently, the image operation may be invoked at any time after class
     *                loading of {@link SampleDimension}.
     *                <br><br>
     *                Additional note: moving the initialization into the
     *                <code>META-INF/registryFile.jai</code> file may not be the best idea neithter,
     *                since peoples using JAI without the GCS module may be stuck with the overhead
     *                of loading GCS classes.
     */
    static {
        final OperationRegistry registry = JAI.getDefaultInstance().getOperationRegistry();
        registry.registerDescriptor(new Descriptor());
        registry.registerFactory(RenderedRegistryMode.MODE_NAME, "GC_SampleTranscoding",
                                 "geotools.org", new CRIF());
    }




    /////////////////////////////////////////////////////////////////////////
    ////////////////                                         ////////////////
    ////////////////             OPENGIS ADAPTER             ////////////////
    ////////////////                                         ////////////////
    /////////////////////////////////////////////////////////////////////////

    /**
     * Returns an OpenGIS interface for this sample dimension. This method first
     * looks in the cache. If no interface was previously cached, then this
     * method creates a new adapter and caches the result.
     *
     * @param  adapters The originating {@link Adapters}.
     * @return The OpenGIS interface. The returned type is a generic {@link Object}
     *         in order to avoid premature class loading of OpenGIS interface.
     */
    final synchronized Object toOpenGIS(final Object adapters) {
        if (proxy != null) {
            if (proxy instanceof Reference) {
                final Object ref = ((Reference) proxy).get();
                if (ref != null) {
                    return ref;
                }
            } else {
                return proxy;
            }
        }
        final Object opengis = new Export(adapters);
        proxy = new WeakReference(opengis);
        return opengis;
    }

    /**
     * Wraps a {@link SampleDimension} object for use with OpenGIS.  This wrapper is a
     * good place to check for non-implemented OpenGIS methods (just check for methods
     * throwing {@link UnsupportedOperationException}). This class is suitable for RMI
     * use.
     */
    final class Export extends RemoteObject implements CV_SampleDimension, RemoteProxy {
        /**
         * The originating adapter.
         */
        private final Adapters adapters;

        /**
         * Constructs a remote object.
         */
        protected Export(final Object adapters) {
            this.adapters = (Adapters)adapters;
        }

        /**
         * Returns the underlying implementation.
         */
        public final Serializable getImplementation() throws RemoteException {
            return SampleDimension.this;
        }

        /**
         * Sample dimension title or description.
         */
        public String getDescription() throws RemoteException {
            return SampleDimension.this.getDescription(null);
        }

        /**
         * A code value indicating grid value data type.
         *
         * @task TODO: We should get this information by inspecting
         *             the image's underlying {@link SampleModel}.
         */
        public CV_SampleDimensionType getSampleDimensionType() throws RemoteException {
            return adapters.export(SampleDimension.this.getSampleDimensionType());
        }

        /**
         * Sequence of category names for the values contained in a sample dimension.
         */
        public String[] getCategoryNames() throws RemoteException {
            return SampleDimension.this.getCategoryNames(null);
        }

        /**
         * Color interpretation of the sample dimension.
         */
        public CV_ColorInterpretation getColorInterpretation() throws RemoteException {
            return adapters.export(SampleDimension.this.getColorInterpretation());
        }

        /**
         * Indicates the type of color palette entry for sample dimensions which have a palette.
         */
        public CV_PaletteInterpretation getPaletteInterpretation() throws RemoteException {
            return new CV_PaletteInterpretation(CV_PaletteInterpretation.CV_RGB);
        }

        /**
         * Color palette associated with the sample dimension.
         *
         * @task TODO: Should invokes a no-args method for 'getColorModel()'. Can be done when
         *             'band' and 'numBands' parameters will be moved into SampleDimension's
         *             constructor.
         */
        public int[][] getPalette() throws RemoteException {
            final ColorModel model = getColorModel(0, 1);
            if (model instanceof IndexColorModel) {
                final IndexColorModel index = (IndexColorModel) model;
                final int[][] palette = new int[index.getMapSize()][];
                final boolean hasAlpha = index.hasAlpha();
                for (int i=0; i<palette.length; i++) {
                    final int[] RGB = palette[i] = new int[hasAlpha ? 4 : 3];
                    RGB[0] = index.getRed  (i);
                    RGB[1] = index.getGreen(i);
                    RGB[2] = index.getBlue (i);
                    if (hasAlpha) {
                        RGB[3] = index.getAlpha(i);
                    }
                }
                return palette;
            } else {
                throw new UnsupportedOperationException("Not yet implemented");
            }
        }

        /**
         * Values to indicate no data values for the sample dimension.
         */
        public double[] getNoDataValue() throws RemoteException {
            return SampleDimension.this.getNoDataValue();
        }

        /**
         * The minimum value occurring in the sample dimension.
         */
        public double getMinimumValue() throws RemoteException {
            return SampleDimension.this.getMinimumValue();
        }

        /**
         * The maximum value occurring in the sample dimension.
         */
        public double getMaximumValue() throws RemoteException {
            return SampleDimension.this.getMaximumValue();
        }

        /**
         * The unit information for this sample dimension.
         */
        public CS_Unit getUnits() throws RemoteException {
            return adapters.CT.export(SampleDimension.this.getUnits());
        }

        /**
         * Offset is the value to add to grid values for this sample dimension.
         */
        public double getOffset() throws RemoteException {
            return SampleDimension.this.getOffset();
        }

        /**
         * Scale is the value which is multiplied to grid values for this sample dimension.
         */
        public double getScale() throws RemoteException {
            return SampleDimension.this.getScale();
        }

        /**
         * The list of metadata keywords for a sample dimension.
         */
        public String[] getMetaDataNames() throws RemoteException {
            return new String[0];
        }

        /**
         * Retrieve the metadata value for a given metadata name.
         */
        public String getMetadataValue(String name) throws RemoteException {
            throw new UnsupportedOperationException("Not yet implemented");
        }
    }
}
