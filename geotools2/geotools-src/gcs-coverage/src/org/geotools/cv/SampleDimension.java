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
import java.util.Locale;
import java.util.Arrays;
import java.io.Serializable;
import java.awt.image.ColorModel;
import java.awt.image.RenderedImage;

// JAI dependencies
import javax.media.jai.util.Range;

// OpenGIS dependencies
import org.opengis.cv.CV_SampleDimension;

// Geotools dependencies
import org.geotools.ct.MathTransform1D;
import org.geotools.ct.TransformException;

// Resources
import org.geotools.units.Unit;
import org.geotools.resources.XArray;
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
 * is that the {@link Category#getSampleToGeophysics} method  returns a non-null value if and only
 * if the category is quantitative.
 *
 * @version $Id: SampleDimension.java,v 1.3 2002/07/17 23:30:55 desruisseaux Exp $
 * @author OpenGIS (www.opengis.org)
 * @author Martin Desruisseaux
 *
 * @see org.opengis.cv.CV_SampleDimension
 */
public class SampleDimension implements Serializable {
    /**
     * Serial number for interoperability with different versions.
     */
//    private static final long serialVersionUID = 5602218759124690745L;

    /**
     * The category list for this sample dimension,
     * or <code>null</code> if this sample dimension
     * has no category.
     */
    private final CategoryList categories;

    /**
     * <code>true</code> if this sample dimension has at least one quantitative category.
     * An arbitrary number of quantitative categories is allowed, providing their sample
     * value ranges do not overlap.
     */
    private final boolean isQuantitative;

    /**
     * If there is one and only one quantitative category, the
     * {@link Category#getSampleToGeophysics} object for this
     * category. Otherwise, <code>null</code>.
     */
    private final MathTransform1D sampleToGeophysics;
    
    /**
     * Catégorie utilisée lors du dernier encodage ou décodage d'un pixel.  Avant de rechercher
     * la catégorie appropriée pour un nouveau encodage ou décodage, on vérifiera d'abord si la
     * catégorie désirée n'est pas la même que la dernière fois, c'est-à-dire {@link #lastCategory}.
     */
    private transient Category lastCategory;

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
        this(new CategoryList(names));
    }
    
    /**
     * Construct a sample dimension with a set of qualitative categories only.
     * This sample dimension will have no unit.
     *
     * @param names  Sequence of category names for the values contained in a sample dimension.
     *               This allows for names to be assigned to numerical values. The first entry
     *               in the sequence relates to a cell value of zero. For example:
     *               [0]="Background", [1]="Water", [2]="Forest", [3]="Urban".
     * @param colors Color to assign to each category. This array length must be the same
     *               than <code>names</code>'s length.
     */
    public SampleDimension(final String[] names, final Color[] colors) {
        this(new CategoryList(names, colors));
    }
    
    /**
     * Construct a sample dimension with an arbitrary set of categories,
     * which may be both quantitative and qualitative.
     *
     * @param  categories The category list.
     * @param  units      The unit information for this sample dimension.
     *                    May be <code>null</code> if no category has units.
     * @throws IllegalArgumentException if two or more categories have overlapping
     *         sample value range.
     */
    public SampleDimension(final Category[] categories, Unit units) throws IllegalArgumentException
    {
        this(new CategoryList(categories, units));
    }

    /**
     * Construct a new sample dimension with the same categories and
     * units than the specified sample dimension.
     */
    public SampleDimension(final SampleDimension other) {
        categories         = other.categories;
        sampleToGeophysics = other.sampleToGeophysics;
        isQuantitative     = other.isQuantitative;
        lastCategory       = other.lastCategory;
    }
    
    /**
     * Construct a sample dimension with a set of categories.
     *
     * @param categories The category list for this sample dimension, or
     *        <code>null</code> if this sample dimension has no category.
     */
    private SampleDimension(final CategoryList categories) {
        this.categories = categories;
        boolean isQuantitative = false;
        Category main = null;
        if (categories != null) {
            for (int i=categories.size(); --i>=0;) {
                final Category candidate = (Category) categories.get(i);
                if (candidate.isQuantitative()) {
                    isQuantitative = true;
                    if (main != null) {
                        main = null;
                        break;
                    }
                    main = candidate;
                }
            }
        }
        this.isQuantitative = isQuantitative;
        sampleToGeophysics = (main!=null) ? main.getSampleToGeophysics() : null;
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
     * Returns all categories in this sample dimension. Note that a {@link Category} object may
     * apply to an arbitrary range of sample values. Consequently, element <code>[0]</code> (for
     * example) in the returned array may not be directly related to the sample value
     * <code>0</code>.
     *
     * @return The categories in this sample dimension, or <code>null</code> if none.
     */
    public Category[] getCategories() {
        return (categories!=null) ? (Category[]) categories.toArray() : null;
    }

    /**
     * Returns a sequence of category names for the values contained in this sample dimension.
     * This allows for names to be assigned to numerical values. The first entry in the sequence
     * relates to a cell value of zero. For grid coverages, category names are only valid for a
     * classified grid data.
     *
     * For example:
     *  <blockquote><pre>
     *    [0] Background
     *    [1] Water
     *    [2] Forest
     *    [3] Urban
     *  </pre></blockquote>
     *
     * @param  locale The locale for category names, or <code>null</code> for a default locale.
     * @return The sequence of category names for the values contained in a sample dimension,
     *         or <code>null</code> if there is no category in this sample dimension.
     * @throws IllegalStateException if a sequence can't be mapped because some category use
     *         negative of non-integer sample values.
     *
     * @see CV_SampleDimension#getCategoryNames()
     */
    public String[] getCategoryNames(final Locale locale) throws IllegalStateException {
        if (categories == null) {
            return null;
        }
        final int categoryCount = categories.size();
        if (categoryCount == 0) {
            return new String[0];
        }
        final int length=Math.max((int)((Category)categories.get(categoryCount-1)).maxSample + 1,0);
        final String[] names = new String[length];
        for (int i=0; i<categoryCount; i++) {
            final Category category = (Category) categories.get(i);
            final int lower = (int) category.minSample;
            final int upper = (int) category.maxSample;
            if (lower!=category.minSample || lower<0 ||
                upper!=category.maxSample || upper<0)
            {
                final Resources resources = Resources.getResources(locale);
                throw new IllegalStateException("Some categories use non-integer sample values");
                // TODO: localize this message.
            }
            Arrays.fill(names, lower, upper+1, category.getName(locale));
        }
        return names;
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
     */
    public ColorInterpretation getColorInterpretation() {
        return ColorInterpretation.UNDEFINED;
    }

    /**
     * Returns the values to indicate "no data" for this sample dimension.
     * The default implementation fetch those values from the categories
     * supplied at construction time.
     * <br><br>
     * Together with {@link #getOffset()} and {@link #getScale()}, this method provides a limited
     * way to transform sample values into geophysics values. However, the recommended way is to
     * use {@link #getSampleToGeophysics} instead, which is more general and take cares of
     * "no data" values.
     *
     * @return The values to indicate no data values for this sample dimension,
     *         or <code>null</code> if not applicable.
     * @throws IllegalStateException if some qualitative categories use a range of
     *         non-integer values.
     */
    public double[] getNoDataValue() throws IllegalStateException {
        if (!isQuantitative) {
            return null;
        }
        int count = 0;
        final int categoryCount = categories.size();
        double[] padValues = new double[categoryCount];
        for (int i=0; i<categoryCount; i++) {
            final Category category = (Category) categories.get(i);
            if (!category.isQuantitative()) {
                padValues[count++] = category.minSample;
                if (category.maxSample != category.minSample) {
                    int lower = (int) category.minSample;
                    int upper = (int) category.maxSample;
                    if (lower!=category.minSample || upper!=category.maxSample) {
                        throw new IllegalStateException("Some categories use non-integer sample values");
                        // TODO: localize this message.
                    }
                    padValues = XArray.resize(padValues, padValues.length + (upper-lower));
                    while (++lower <= upper) {
                        padValues[count++] = lower;
                    }
                }
            }
        }
        return XArray.resize(padValues, count);
    }
    
    /**
     * Returns the minimum value occurring in this sample dimension.
     * The default implementation fetch this value from the categories supplied at
     * construction time. If the minimum value can't be computed, then this method
     * returns {@link Double#NEGATIVE_INFINITY}.
     *
     * @see CV_SampleDimension#getMinimumValue()
     */
    public double getMinimumValue() {
        if (categories!=null && categories.size()!=0) {
            return ((Category) categories.get(0)).minSample;
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
     */
    public double getMaximumValue() {
        if (categories!=null) {
            for (int i=categories.size(); --i>=0;) {
                final double value = ((Category) categories.get(i)).maxSample;
                if (!Double.isNaN(value)) {
                    return value;
                }
            }
        }
        return Double.POSITIVE_INFINITY;
    }
    
    /**
     * Returns the unit information for this sample dimension.
     * May returns <code>null</code> if this dimension has no units.
     *
     * @see CV_SampleDimension#getUnits()
     */
    public Unit getUnits() {
        return (categories!=null) ? categories.getUnits() : null;
    }

    /**
     * Returns the value to add to grid values for this sample dimension.
     * This attribute is typically used when the sample dimension represents
     * elevation data. The transformation equation is:
     *
     * <blockquote><pre>offset + scale*sample</pre></blockquote>
     *
     * Together with {@link #getSample()} and {@link #getNoDataValue()}, this method provides a
     * limited way to transform sample values into geophysics values. However, the recommended
     * way is to use {@link #getSampleToGeophysics} instead, which is more general and take cares
     * of "no data" values.
     *
     * @return the offset to add to grid values.
     * @throws IllegalStateException if the transform from sample to geophysics values
     *         is not a linear relation.
     *
     * @see CV_SampleDimension#getOffset()
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
     * way is to use {@link #getSampleToGeophysics} instead, which is more general and take cares
     * of "no data" values.
     *
     * @return the scale to multiply to grid value.
     * @throws IllegalStateException if the transform from sample to geophysics values
     *         is not a linear relation.
     *
     * @see CV_SampleDimension#getScale()
     */
    public double getScale() {
        return getCoefficient(1);
    }

    /**
     * Returns a coefficient of the linear transform from sample to geophysics values.
     *
     * @param  order 0 for the offset, or 1 for the scale factor.
     * @return The coefficient.
     * @throws IllegalStateException if the transform from sample to geophysics values
     *         is not a linear relation.
     */
    private double getCoefficient(final int order) throws IllegalStateException {
        if (!isQuantitative) {
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
        // TODO: localize.
        IllegalStateException exception = new IllegalStateException("Not a linear relation");
        exception.initCause(cause);
        throw exception;
    }
    
    /**
     * Convert a sample value into a geophysics value.
     *
     * @param  sample The sample value.
     * @return The geophysics value, in the units {@link #getUnits}. This
     *         value may be one of the many <code>NaN</code> values if the
     *         sample do not belong to a quantitative category. Many
     *         <code>NaN</code> values are possibles, which make it possible
     *         to differenciate among many qualitative categories.
     */
    public final double toGeophysicsValue(final double sample) {
        if (categories != null) {
            final Category category = categories.getDecoder(sample, lastCategory);
            if (category != null) {
                lastCategory = category;
                return category.toGeophysicsValue(sample);
            }
        }
        return Double.NaN;
    }
    
    /**
     * Convert a geophysics value into a sample value. <code>value</code> must be
     * in the units {@link #getUnits}. If <code>value</code> is <code>NaN</code>,
     * then this method returns the sample value for one of the qualitative categories.
     * Many different <code>NaN</code> are alowed, which make it possible to differenciate
     * among many qualitative categories.
     *
     * @param  value The geophysics value (may be <code>NaN</code>). If this value is
     *               outside the allowed range of values, it will be clamped to the
     *               minimal or the maximal value.
     * @return The sample value.
     */
    public final double toSampleValue(final double value) {
        if (categories != null) {
            final Category category = categories.getEncoder(value, lastCategory);
            if (category!=null) {
                lastCategory = category;
                return category.toSampleValue(value);
            }
        }
        return 0;
    }
    
    /**
     * Returns a view of an image in which all pixels have been transformed into
     * floating-point values as with the {@link #toGeophysicsValue} method. The resulting
     * image usually represents some geophysics parameter in "real world" scientific
     * and engineering units (e.g. temperature in °C).
     *
     * @param image Image to convert. This image usually store pixel values as integers.
     * @param bands The list of sample dimension to use for transforming pixel values into
     *              geophysics parameters. This array's length must matches the number
     *              of bands in <code>image</code>.
     * @return      The converted image. This image store geophysics values as floating-point
     *              numbers. This method returns <code>null</code> if <code>image</code> was null.
     */
    public static RenderedImage toGeophysicsValues(final RenderedImage     image,
                                                   final SampleDimension[] bands)
    {
        return NumericImage.getInstance(image, getCategories(bands));
    }
    
    /**
     * Returns a view of an image in which all geophysics values have been transformed
     * into indexed pixel as with the {@link #toSampleValue} method. The resulting
     * image is more suitable for rendering than the geophysics image (since Java2D
     * do a better job with integer pixels than floating-point pixels).
     *
     * @param image  Image to convert. This image usually represents some geophysics
     *               parameter in "real world" scientific and engineering units (e.g.
     *               temperature in °C).
     * @param bands  The list of sample dimension to use for transforming floating-point values
     *               into sample values. This array's length must matches the number of
     *               bands in <code>image</code>.
     * @return       The converted image. This image store sample values as integer.
     *               This method returns <code>null</code> if <code>image</code> was null.
     */
    public static RenderedImage toSampleValues(final RenderedImage     image,
                                               final SampleDimension[] bands)
    {
        return ThematicImage.getInstance(image, getCategories(bands));
    }

    /**
     * Convert an array of {@link SampleDimension} into an array of {@link CategoryList}.
     */
    private static CategoryList[] getCategories(final SampleDimension[] bands) {
        final CategoryList[] categories = new CategoryList[bands.length];
        for (int i=0; i<categories.length; i++) {
            categories[i] = bands[i].categories;
        }
        return categories;
    }
    
    /**
     * Format a geophysics value. If <code>value</code> is a real number, then the value is
     * formatted with the appropriate number of digits and the units symbol.  Otherwise, if
     * <code>value</code> is <code>NaN</code>, then the category name is returned.
     *
     * @param  value  The geophysics value (may be <code>NaN</code>).
     * @param  locale Locale to use for formatting, or <code>null</code> for the default locale.
     * @return A string representation of the geophysics value.
     */
    public String format(final double value, final Locale locale) {
        // TODO: check for categories==null
        return categories.format(value, locale, lastCategory);
    }
    
    /**
     * Returns a color model for this sample dimension. The default implementation builds up the
     * color model using each category's colors (as returned by {@link Category#getColors}). The
     * returned color model will use data type {@link DataBuffer#TYPE_FLOAT} for geophysical
     * values, or an integer data type for sample values.
     *
     * @param  type {@link SampleInterpretation#GEOPHYSICS} to request a color model applicable
     *         to geophysics,  or {@link SampleInterpretation#INDEXED} to request a color model
     *         applicable to sample values.
     * @param  visibleBand The band to be made visible (usually 0). All other bands, if any
     *         will be ignored.
     * @param  numBands The number of bands for the color model (usually 1). The returned color
     *         model will renderer only the <code>visibleBand</code> and ignore the others, but
     *         the existence of all <code>numBands</code> will be at least tolerated. Supplemental
     *         bands, even invisible, are useful for processing with Java Advanced Imaging.
     * @return The requested color model, suitable for {@link RenderedImage} objects with values
     *         in the <code>{@link #getRange getRange}(type)</code> range.
     */
    public final synchronized ColorModel getColorModel(final SampleInterpretation type,
                                                       final int visibleBand, final int numBands)
    {
        // TODO: check for categories==null
        return categories.getColorModel(type, visibleBand, numBands);
    }
}
