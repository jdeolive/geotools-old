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

// Collections
import java.util.Map;
import java.util.HashMap;

// Images
import java.awt.Color;
import java.awt.Transparency;
import java.awt.color.ColorSpace;
import java.awt.image.ColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.RenderedImage;
import java.awt.image.IndexColorModel;
import java.awt.image.ComponentColorModel;
import javax.media.jai.RasterFactory;

// Other J2SE and JAI dependencies
import java.util.Locale;
import java.util.Arrays;
import java.io.Serializable;
import java.util.AbstractList;
import java.text.NumberFormat;
import java.text.FieldPosition;
import javax.media.jai.util.Range;

// Resources
import org.geotools.units.Unit;
import org.geotools.resources.XMath;
import org.geotools.resources.XArray;
import org.geotools.resources.Utilities;
import org.geotools.resources.ImageUtilities;
import org.geotools.resources.gcs.Resources;
import org.geotools.resources.gcs.ResourceKeys;


/**
 * An immutable list of categories. A category is a range of sample values reserved for a
 * structure or a geophysics parameter.  For example, an image may use some sample values
 * for clouds, lands and ices,  and a range of sample values for sea surface temperature.
 * Such an image may be build of four categories: three qualitative (clouds, lands and ices)
 * and one quantitative (temperature in Celsius degrees). The ability to mix qualitative and
 * quantitative categories in the same sample dimension is an important feature for remote
 * sensing in oceanography. Unfortunately, many commercial GIS software lack this feature
 * since they are mostly designed for terrestrial use.
 * <br><br>
 * For space and performance raisons, images often store samples as integer values. For such
 * images, quantitative categories need an equation converting sample values into geophysics
 * values (e.g. temperature in Celsius degrees).     Each category may have his own equation
 * expressed as a {@link org.geotools.ct.MathTransform1D} object.  <code>CategoryList</code>
 * is responsible for selecting the right category from a sample value, and consequently the
 * right transformation equation.
 *
 * @version $Id: CategoryList.java,v 1.2 2002/07/17 23:30:55 desruisseaux Exp $
 * @author Martin Desruisseaux
 */
final class CategoryList extends AbstractList implements Serializable {
    /**
     * Serial number for interoperability with different versions.
     */
    private static final long serialVersionUID = 5602218759124690745L;

    /**
     * Default category for "No data". Will be used only if no such category was explicitly set.
     */
    private static final Category DEFAULT_BLANK =
            Category.create(Resources.format(ResourceKeys.NODATA), Color.black, 0);

    /**
     * Maximum value for {@link #ndigits}. This is the number of
     * significant digits to allow when formatting a geophysics value.
     */
    private static final int MAX_DIGITS = 6;
    
    /**
     * Liste des catégories constituant cet objet <code>CategoryList</code>.
     * Cette liste doit être en ordre croissant de {@link Category#minSample},
     * c'est-à-dire classée selon le comparateur {@link CategoryComparator#BY_SAMPLES}.
     */
    private final Category[] bySamples;
    
    /**
     * Liste des catégories constituant cet objet <code>CategoryList</code>.
     * Cette liste doit être en ordre croissant de {@link Category#maxSample},
     * c'est-à-dire classée selon le comparateur {@link CategoryComparator#BY_VALUES}.
     */
    private final Category[] byValues;
    
    /**
     * Tableau des valeurs {@link Category#minSample} pour chaque
     * élément du tableau {@link #bySamples}, dans le même ordre.
     */
    private final double[] minSamples;
    
    /**
     * Tableau des valeurs {@link Category#minValue} pour chaque
     * élément du tableau {@link #byValues}, dans le même ordre.
     */
    private final double[] minValues;
    
    /**
     * Unités des mesures géophysiques représentées par les catégories.
     * Ce champ peut être nul s'il ne s'applique pas ou si les unités
     * ne sont pas connues.
     */
    private final Unit unit;
    
    /**
     * Nombre de chiffres significatifs après la virgule.
     * Cette information est utilisée pour les écritures
     * des valeurs géophysiques des catégories.
     */
    private final int ndigits;
    
    /**
     * Locale used for creating {@link #format} last time.
     * May be <code>null</code> if default locale was requested.
     */
    private transient Locale locale;
    
    /**
     * Format à utiliser pour écrire les
     * valeurs géophysiques des thèmes.
     */
    private transient NumberFormat format;
    
    /**
     * Objet temporaire pour {@link NumberFormat}.
     */
    private transient FieldPosition dummy;
    
    /**
     * Modèles de couleurs suggérés pour l'affichage des catégories. Ces modèles de couleurs
     * peuvent être construits à partir des couleurs qui ont été définies dans les différentes
     * catégories du tableau {@link #bySamples}.
     *
     * TODO: We should use some kind of "WeakValueHashMap" instead. Unfortunatly
     *       J2SE 1.4 has no such class, since "WeakHashMap" is not appropriate.
     */
    private transient Map colors;
    
    /**
     * Construct a category list for qualitative categories.
     * This category list will have no unit.
     *
     * @param names  Sequence of category names for the values contained in a sample dimension.
     *               This allows for names to be assigned to numerical values. The first entry
     *               in the sequence relates to a cell value of zero. For example:
     *               [0]="Background", [1]="Water", [2]="Forest", [3]="Urban".
     */
    public CategoryList(final String[] names) {
        this(list(names), null);
    }
    
    /**
     * Construct a category list for qualitative categories.
     * This category list will have no unit.
     *
     * @param names  Sequence of category names for the values contained in a sample dimension.
     *               This allows for names to be assigned to numerical values. The first entry
     *               in the sequence relates to a cell value of zero. For example:
     *               [0]="Background", [1]="Water", [2]="Forest", [3]="Urban".
     * @param colors Color to assign to each category.
     */
    public CategoryList(final String[] names, final Color[] colors) {
        this(list(names, colors), null);
    }
    
    /**
     * Construct a category list for a sample dimension (band) with the specified units.
     *
     * @param  categories The category list.
     * @param  units      The unit information for this category list's.
     *                    May be <code>null</code> if no category has units.
     * @throws IllegalArgumentException if two or more categories have overlapping
     *         sample value range.
     */
    public CategoryList(final Category[] categories, final Unit units)
        throws IllegalArgumentException
    {
        this(categories, units, getFractionDigitCount(categories));
    }
    
    /**
     * Construct a category list for a sample dimension (band) with the specified units.
     *
     * @param  categories The category list.
     * @param  units      The unit information for this category list's.
     *                    May be <code>null</code> if no category has units.
     * @param  ndigits    Number of significant digits after the dot. This is used
     *                    when formatting quantity values. For example, if this
     *                    category list contains a category for temperature and
     *                    <code>ndigits</code> equals 2, then temperature values
     *                    may be formatted as "12.80°C". The exact formatting is
     *                    locale-dependent.
     *
     * @throws IllegalArgumentException if two or more categories have overlapping
     *         sample value range.
     */
    private CategoryList(final Category[] categories, final Unit units, final int ndigits)
        throws IllegalArgumentException
    {
        this.unit       = units;
        this.ndigits    = ndigits;
        this.bySamples  = CategoryComparator.BY_SAMPLES.sort(categories);
        this.byValues   = CategoryComparator.BY_VALUES .sort(bySamples);
        this.minSamples = new double[bySamples.length];
        this.minValues  = new double[byValues .length];
        for (int i=bySamples.length; --i>=0;) {
            minSamples[i] = bySamples[i].minSample;
            minValues [i] = byValues [i].minValue;
        }
        /*
         * Vérifie que les catégories ne se chevauchent pas.
         */
        for (int j=0; j<bySamples.length; j++) {
            final Category categ1 = bySamples[j];
            for (int i=j+1; i<bySamples.length; i++) {
                final Category categ2 = bySamples[i];
                // Use '!' because we do not accept NaN in sample values
                if (!(categ1.minSample>categ2.maxSample || categ1.maxSample<categ2.minSample)) {
                    throw new IllegalArgumentException(overlapMessage(categ1, categ2,
                                                       SampleInterpretation.INDEXED));
                }
                // Don't use '!' because we accept NaN in geophysics values
                if (categ1.minValue<=categ2.maxValue && categ1.maxValue>=categ2.minValue) {
                    throw new IllegalArgumentException(overlapMessage(categ1, categ2,
                                                       SampleInterpretation.GEOPHYSICS));
                }
            }
        }
        assert CategoryComparator.BY_SAMPLES.isSorted(bySamples);
        assert CategoryComparator.BY_VALUES .isSorted(byValues );
    }
    
    /**
     * Construct a list of categories.
     */
    private static Category[] list(final String[] names) {
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
     */
    private static Category[] list(final String[] names, final Color[] colors) {
        if (names.length!=colors.length) {
            throw new IllegalArgumentException(
                    Resources.format(ResourceKeys.ERROR_MISMATCHED_ARRAY_LENGTH));
        }
        final Category[] categories = new Category[names.length];
        for (int i=0; i<categories.length; i++) {
            categories[i] = Category.create(names[i], colors[i], i);
        }
        return categories;
    }

    /**
     * Format an error message saying that two ranges are overlapping.
     */
    private static String overlapMessage(Category categ1, Category categ2,
                                         final SampleInterpretation type)
    {
        final Range range1 = categ1.getRange(type);
        final Range range2 = categ2.getRange(type);
        return Resources.format(ResourceKeys.ERROR_RANGE_OVERLAP_$4,
                                range1.getMinValue(), range1.getMaxValue(),
                                range2.getMinValue(), range2.getMaxValue());
    }
    
    /**
     * Compute the smallest number of fraction digits necessary to resolve
     * all quantitative values.  This method assume that geophysics values
     * in the range {@link Category#minValue} to {@link Category#maxValue}
     * are stored as integer sample values in the range {@link Category#minSample}
     * to {@link Category#maxSample}.
     */
    private static int getFractionDigitCount(final Category[] categories) {
        int ndigits = 0;
        for (int i=0; i<categories.length; i++) {
            final Category category = categories[i];
            final double ln = XMath.log10((category.maxValue  - category.minValue)/
                                          (category.maxSample - category.minSample));
            if (!Double.isNaN(ln)) {
                final int n = -(int)(Math.floor(ln+1E-6));
                if (n>ndigits) {
                    ndigits = Math.min(n, MAX_DIGITS);
                }
            }
        }
        return ndigits;
    }
    
    /**
     * Returns the <code>CategoryList</code>'s name. The default implementation returns
     * the name of what seems to be the "main" category (i.e. the quantitative category
     * with the widest sample range) followed by the geophysics value range.
     *
     * @param  locale The locale, or <code>null</code> for the default one.
     * @return The localized description. If no description was available
     *         in the specified locale, a default locale is used.
     */
    public String getName(final Locale locale) {
        final StringBuffer buffer = new StringBuffer(30);
        final Category   category = getMain();
        if (category != null) {
            buffer.append(category.getName(locale));
        } else {
            buffer.append('(');
            buffer.append(Resources.getResources(locale).getString(ResourceKeys.UNTITLED));
            buffer.append(')');
        }
        buffer.append(' ');
        return String.valueOf(formatRange(buffer, locale));
    }
    
    /**
     * Returns the number of categories in this list.
     */
    public int size() {
        return bySamples.length;
    }
    
    /**
     * Returns the category at the specified index.
     * This index may not be related to sample value.
     *
     * @param  index An index in the range <code>[0..{@link #size()}-1]</code>.
     * @return The {@link Category} at the specified index.
     */
    public Object get(final int index) {
        return bySamples[index];
    }
    
    /**
     * Retourne une catégorie à utiliser pour représenter les données manquantes.
     * Si aucune catégorie ne représente une valeur <code>NaN</code>, alors cette
     * méthode retourne une catégorie arbitraire.
     */
    final Category getBlank() {
        final long blankBits = Double.doubleToRawLongBits(Double.NaN);
        for (int i=0; i<bySamples.length; i++) {
            final Category category = bySamples[i];
            if (Double.doubleToRawLongBits(category.minValue) == blankBits) {
                return category;
            }
        }
        for (int i=0; i<bySamples.length; i++) {
            final Category category = bySamples[i];
            if (Double.isNaN(category.minValue)) {
                return category;
            }
        }
        if (bySamples.length!=0) {
            return bySamples[0];
        }
        return DEFAULT_BLANK;
    }
    
    /**
     * Returns what seems to be the "main" category. The default implementation looks
     * for the quantitative category (if there is one) with the widest sample range.
     */
    private Category getMain() {
        double range = 0;
        Category category = null;
        for (int i=bySamples.length; --i>=0;) {
            final Category candidate = bySamples[i];
            if (candidate!=null && candidate.isQuantitative()) {
                final double candidateRange = candidate.maxSample - candidate.minSample;
                if (candidateRange >= range) {
                    range = candidateRange;
                    category = candidate;
                }
            }
        }
        return category;
    }
    
    /**
     * Returns the unit information for quantitative categories in this list.
     * May returns <code>null</code>  if there is no quantitative categories
     * in this list, or if there is no unit information.
     */
    public Unit getUnits() {
        return unit;
    }
    
    /**
     * Returns the range of values for this category list. The range is bounded by a
     * lower (or minimal) inclusive value and an upper (or maximal) exclusive value.
     * If <code>type</code> is {@link SampleInterpretation#GEOPHYSICS}, then the
     * returned range is expressed in {@link #getUnits} units. Otherwise, it is
     * expressed as sample values.
     *
     * @param  type {@link SampleInterpretation#INDEXED} for the range of sample values, or
     *         {@link SampleInterpretation#GEOPHYSICS} for the range of geophysics values.
     * @return The range of values, or <code>null</code> if this category list has no category.
     *
     * @see Category#getRange
     */
    public Range getRange(final SampleInterpretation type) {
        final boolean indexed = SampleInterpretation.INDEXED.equals(type);
        Range range = null;
        for (int i=0; i<bySamples.length; i++) {
            final Category category = bySamples[i];
            if (indexed || category.isQuantitative()) {
                final Range extent = category.getRange(type);
                if (range != null) {
                    range = range.union(extent);
                } else {
                    range = extent;
                }
            }
        }
        return range;
    }
    
    /**
     * Format a geophysics value. If <code>value</code> is a real number, then the value is
     * formatted with the appropriate number of digits and the units symbol.  Otherwise, if
     * <code>value</code> is <code>NaN</code>, then the category name is returned.
     *
     * @param  value  The geophysics value (may be <code>NaN</code>).
     * @param  locale Locale to use for formatting, or <code>null</code> for the default locale.
     * @param  probableCategory The probable category of <code>value</code>. This argument is just a
     *         hint. If <code>probableCategory</code> is not accurate, this method will work anyway
     *         but may be slower.
     * @return A string representation of the geophysics value.
     */
    final String format(final double value, final Locale locale, final Category probableCategory) {
        if (Double.isNaN(value)) {
            final Category category = getEncoder(value, probableCategory);
            if (category != null) {
                return category.getName(locale);
            }
        }
        return format(value, true, locale, new StringBuffer()).toString();
    }
    
    /**
     * Formatte la valeur spécifiée selon les conventions locales. Le nombre sera
     * écrit avec un nombre de chiffres après la virgule approprié pour cette catégorie.
     * Le symbole des unités sera ajouté après le nombre si <code>writeUnit</code>
     * est <code>true</code>.
     *
     * @param  value Valeur du paramètre géophysique à formatter.
     * @param  writeUnit Indique s'il faut écrire le symbole des unités après le nombre.
     *         Cet argument sera ignoré si aucune unité n'avait été spécifiée au constructeur.
     * @param  locale Conventions locales à utiliser, ou <code>null</code> pour les conventions par
     *         défaut.
     * @param  buffer Le buffer dans lequel écrire la valeur.
     * @return Le buffer <code>buffer</code> dans lequel auront été écrit la valeur et les unités.
     */
    private synchronized StringBuffer format(final double value, final boolean writeUnits,
                                             final Locale locale, StringBuffer buffer)
    {
        if (format==null || !Utilities.equals(this.locale, locale)) {
            this.locale = locale;
            format=(locale!=null) ? NumberFormat.getNumberInstance(locale) :
                                    NumberFormat.getNumberInstance();
            format.setMinimumFractionDigits(ndigits);
            format.setMaximumFractionDigits(ndigits);
            dummy = new FieldPosition(0);
        }
        buffer = format.format(value, buffer, dummy);
        if (writeUnits && unit!=null) {
            final int position = buffer.length();
            buffer.append('\u00A0'); // No-break space
            buffer.append(unit);
            if (buffer.length() == position+1) {
                buffer.setLength(position);
            }
        }
        return buffer;
    }
    
    /**
     * Format the range of geophysics values.
     */
    private StringBuffer formatRange(StringBuffer buffer, final Locale locale) {
        final Range range = getRange(SampleInterpretation.GEOPHYSICS);
        buffer.append('[');
        if (range!=null) {
            buffer=format(((Number)range.getMinValue()).doubleValue(), false, locale, buffer);
            buffer.append("..");
            buffer=format(((Number)range.getMaxValue()).doubleValue(), true,  locale, buffer);
        } else if (unit!=null) {
            buffer.append(unit);
        }
        buffer.append(']');
        return buffer;
    }
    
    /**
     * Retourne la catégorie appropriée pour convertir la valeur du pixel
     * spécifiée en valeur géophysique.  Si aucune catégorie ne convient,
     * alors cette méthode retourne <code>null</code>.
     *
     * @param  sample Valeur à transformer.
     * @param  category Catégorie présumée du pixel, ou <code>null</code>.
     *         Il n'est pas nécessaire que cette information soit exacte,
     *         mais cette méthode sera plus rapide si elle l'est.
     * @return La catégorie du pixel, ou <code>null</code>.
     */
    final Category getDecoder(final double sample, Category category) {
        if (category!=null && sample>=category.minSample && sample<=category.maxSample) {
            return category;
        }
        /*
         * Si la catégorie n'est pas la même que la dernière fois,
         * recherche à quelle autre catégorie pourrait appartenir le pixel.
         */
        int i=Arrays.binarySearch(minSamples, sample);
        if ((i>=0 || (i=~i-1)>=0) && i<bySamples.length) {
            category = bySamples[i];
            assert(minSamples[i] == category.minSample);
            if (sample>=category.minSample && sample<=category.maxSample) {
                return category;
            }
        }
        return null;
    }
    
    /**
     * Retourne la catégorie appropriée pour convertir la valeur géophysique
     * spécifiée en valeur de pixel. Si aucune catégorie ne convient,  alors
     * cette méthode retourne <code>null</code>.
     *
     * @param  value Valeur à transformer.
     * @param  category Catégorie présumée de la valeur, ou <code>null</code>.
     *         Il n'est pas nécessaire que cette information soit exacte, mais
     *         cette méthode sera plus rapide si elle l'est.
     * @return La catégorie de la valeur, ou <code>null</code>.
     */
    final Category getEncoder(final double value, Category category) {
        if (category!=null && ((value>=category.minValue && value<=category.maxValue) ||
                                Double.doubleToRawLongBits(value) ==
                                Double.doubleToRawLongBits(category.minValue)))
        {
            return category;
        }
        /*
         * Si la catégorie n'est pas la même que la dernière fois,
         * recherche à quelle catégorie pourrait appartenir la valeur.
         * Note: Les valeurs 'NaN' sont à la fin du tableau 'values'. Donc:
         *
         * 1) Si 'value' est NaN,  alors 'i' pointera forcément sur une catégorie NaN.
         * 2) Si 'value' est réel, alors 'i' peut pointer sur une des catégories de
         *    valeurs réels ou sur la première catégorie de NaN.
         */
        int i = CategoryComparator.binarySearch(minValues, value); // Special 'binarySearch' for NaN
        if (i>=0) {
            // The value is exactly equals to one of Category.minValue,
            // or is one of NaN values. There is nothing else to do.
            category = byValues[i];
        } else {
            if (Double.isNaN(value)) {
                // The value is NaN, but not one of the registered ones.
                // Consequently, we can't map a category to this value.
                return null;
            }
            assert i==Arrays.binarySearch(minValues, value);
            // 'binarySearch' found the index of "insertion point" (~i). This means that
            // 'value' is lower than 'Category.minValue' at this index.  Consequently, if
            // this value fits in a category's range, it fits in the previous category (~i-1).
            i = ~i-1;
            if (i<0) {
                // If the value is smaller than the smallest Category.minValue, returns
                // the first category (except if there is only NaN categories).
                if (byValues.length==0) {
                    return null;
                }
                category = byValues[0];
                if (Double.isNaN(category.minValue)) {
                    return null;
                }
            } else {
                category = byValues[i];
                assert value >= category.minValue;
                // We found the probable category.  If value is outside
                // any category's range (including this one), it may be
                // closer to the next category than the current one...
                if (value > category.maxValue  &&  i+1 < byValues.length) {
                    final Category upper = byValues[i+1];
                    // assert: if 'upper.minValue' was smaller than 'value',
                    //         it should has been found by 'binarySearch'.
                    //         We use '!' in order to accept NaN values.
                    assert !(upper.minValue <= value);
                    if (upper.minValue-value < value-category.maxValue) {
                        category = upper;
                    }
                }
            }
        }
        // assert: after converting geophysics value to sample
        //         value, it should stay in the same category.
        assert category == getDecoder(category.toSampleValue(value), category) : category;
        return category;
    }
    
    /**
     * Returns a color model for this category list. The default implementation
     * builds up the color model from each category's colors (as returned by
     * {@link Category#getColors}). The returned color model will use data type
     * {@link DataBuffer#TYPE_FLOAT} for geophysical values, or an integer data
     * type for sample values.
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
        /*
         * First, looks if a color model is already available in the cache.
         */
        ColorModel model = null;
        final ColorKey key = new ColorKey(type, visibleBand, numBands);
        if (colors != null) {
            model = (ColorModel) colors.get(key);
            if (model != null) {
                return model;
            }
        }
        /*
         * Now, check argument validity.
         */
        final boolean geophysicsValue;
        if (SampleInterpretation.INDEXED.equals(type)) {
            geophysicsValue = false;
        } else if (SampleInterpretation.GEOPHYSICS.equals(type)) {
            geophysicsValue = true;
        } else {
            throw new IllegalArgumentException(Resources.format(ResourceKeys.ERROR_BAD_PARAMETER_$2,
                                               "type", (type!=null) ? type.getName(null) : null));
        }
        if (visibleBand<0 || visibleBand>=numBands) {
            throw new IllegalArgumentException(Resources.format(
                    ResourceKeys.ERROR_BAD_BAND_NUMBER_$1, new Integer(visibleBand)));
        }
        /*
         * Now, construct the color model.
         */
        if (geophysicsValue) {
            final ColorSpace colors = new ScaledColorSpace(visibleBand, numBands, getRange(type));
            model = RasterFactory.createComponentColorModel(DataBuffer.TYPE_FLOAT,
                                       colors, false, false, Transparency.OPAQUE);
        } else {
            if (numBands!=1) {
                // It would be possible to support 2, 3, 4... bands. But is it
                // really a good idea? This method is used by GridCoverage for
                // creating a displayable image from a geophysics one.  We may
                // ignore extra bands (by subclassing IndexColorModel), but it
                // would involve useless computation every time the "thematic"
                // image is computed since extra-bands are ignored...
                throw new UnsupportedOperationException(String.valueOf(numBands));
            }
            if (bySamples.length==0) {
                // Construct a gray scale palette.
                final byte[] RGB = new byte[256];
                for (int i=0; i<RGB.length; i++) {
                    RGB[i] = (byte)i;
                }
                model = new IndexColorModel(8, RGB.length, RGB, RGB, RGB);
            } else {
                /*
                 * Calcule le nombre de couleurs de la palette
                 * en cherchant l'index le plus élevé des thèmes.
                 */
                assert CategoryComparator.BY_SAMPLES.isSorted(bySamples);
                final int mapSize = (int)Math.round(bySamples[bySamples.length-1].maxSample)+1;
                final int[]  ARGB = new int[mapSize];
                /*
                 * Interpole les codes de couleurs dans la palette. Les couleurs
                 * correspondantes aux plages non-définies par un thème seront transparentes.
                 */
                for (int i=0; i<bySamples.length; i++) {
                    final Category category = bySamples[i];
                    ImageUtilities.expand(category.getColors(), ARGB,
                                          (int)Math.round(category.minSample),
                                          (int)Math.round(category.maxSample)+1);
                }
                model = ImageUtilities.getIndexColorModel(ARGB);
            }
        }
        /*
         * Cache and returns the color model
         */
        if (colors==null) {
            colors = new HashMap();
        }
        colors.put(key, model);
        return model;
    }
    
    /**
     * Returns a hash value for this category list.
     * This value need not remain consistent between
     * different implementations of the same class.
     */
    public int hashCode() {
        int code = 458783261;
        for (int i=0; i<bySamples.length; i++) {
            code = code*37 + bySamples[i].hashCode();
        }
        if (unit!=null) {
            code = code*37 + unit.hashCode();
        }
        return code;
    }
    
    /**
     * Compares the specified object with
     * this category list for equality.
     */
    public boolean equals(final Object object) {
        if (object!=null && object.getClass().equals(getClass())) {
            final CategoryList that = (CategoryList) object;
            if (this.ndigits == that.ndigits             &&
                Utilities.equals(this.unit,   that.unit) &&
                Arrays.equals(this.bySamples, that.bySamples))
            {
                assert Arrays.equals(this.byValues,   that.byValues  ) &&
                       Arrays.equals(this.minSamples, that.minSamples) &&
                       Arrays.equals(this.minValues,  that.minValues );
                return true;
            }
        }
        return false;
    }
    
    /**
     * Returns a string representation of this category list.
     * The returned string is implementation dependent.
     * It is usually provided for debugging purposes only.
     */
    public String toString() {
        final String lineSeparator = System.getProperty("line.separator", "\n");
        StringBuffer buffer = new StringBuffer(Utilities.getShortClassName(this));
        buffer = formatRange(buffer, null);
        buffer.append(lineSeparator);
        /*
         * Ecrit la liste des catégories en dessous.
         */
        final Category main=getMain();
        for (int i=0; i<bySamples.length; i++) {
            buffer.append("   ");
            buffer.append(bySamples[i]==main ? '*' : ' ');
            buffer.append(bySamples[i]);
            buffer.append(lineSeparator);
        }
        return buffer.toString();
    }
    
    /**
     * Returns all categories in this <code>CategoryList</code>.
     */
    public Object[] toArray() {
        return (Category[]) bySamples.clone();
    }

    /**
     * Key for caching {@link ColorModel} objects. Instance of this
     * class are aimed to be used as key in an {@link HashMap} object.
     */
    private static final class ColorKey {
        /**
         * The color model type.
         */
        private final SampleInterpretation type;

        /**
         * The visible band (usually 0).
         */
        private final int visibleBand;

        /**
         * The number of bands (usually 1).
         */
        private final int numBands;

        /**
         * Construct a new <code>ColorKey</code>.
         */
        public ColorKey(final SampleInterpretation type, final int visibleBand, final int numBands) {
            this.type        = type;
            this.visibleBand = visibleBand;
            this.numBands    = numBands;
        }

        /**
         * Returns a hash code for this key.
         */
        public int hashCode() {
            return 962745549 + (numBands*37 + visibleBand)*37 + type.getValue();
        }

        /**
         * Check if two keys are equals.
         */
        public boolean equals(final Object other) {
            if (other instanceof ColorKey) {
                final ColorKey that = (ColorKey) other;
                return this.numBands    == that.numBands    &&
                       this.visibleBand == that.visibleBand &&
                       Utilities.equals(this.type, that.type);
            }
            return false;
        }
    }
}
