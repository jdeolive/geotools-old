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
 * An immutable list of categories. A category is a range of sample
 * values reserved for a structure or a geophysics parameter.  For example, an image
 * may use some sample values for clouds, lands and ices, and a range of sample values
 * for sea surface temperature. Such an image may be build of four categories: three
 * qualitative (clouds, lands and ices) and one quantitative (temperature in Celsius
 * degrees).  The ability to mix qualitative and quantitative categories in the same
 * sample dimension is an important feature for remote sensing in oceanography.
 * Unfortunately, many commercial GIS software lack this feature since they are mostly
 * designed for terrestrial use.
 * <br><br>
 * For space and performance raisons, images often store samples as integer values.
 * For such images, quantitative categories need an equation converting sample values
 * into geophysics values (e.g. temperature in Celsius degrees). Each category may
 * have his own equation. <code>CategoryList</code> is responsible for selecting the
 * right category from a sample value, and consequently the right transformation equation.
 * <br><br>
 * Note: this class will extends <code>AbstractList&lt;Category&gt;</code> when generic
 *       type will be available (in JDK 1.5).
 *
 * @version 1.0
 * @author Martin Desruisseaux
 */
public class CategoryList /*extends AbstractList<Category>*/ implements Serializable {
    /**
     * Serial number for interoperability with different versions.
     */
    private static final long serialVersionUID = 6238756215239410975L;
    
    /**
     * Liste des catégories constituant cet objet <code>CategoryList</code>.
     * Cette liste doit être en ordre croissant de {@link Category#lower},
     * c'est-à-dire classée selon le comparateur <code>CategoryComparator.BY_INDEX</code>.
     */
    private final Category[] byIndex;
    
    /**
     * Liste des thèmes constituant cet objet <code>IndexedThemeMapper</code>.
     * Cette liste doit être en ordre croissant de <code>Category.minimum</code>,
     * c'est-à-dire classée selon le comparateur <code>CategoryComparator.BY_VALUES</code>.
     */
    private final Category[] byValues;
    
    /**
     * Tableau des index {@link Category#lower} du tableau <code>byIndex</code>.
     */
    private final int[] index;
    
    /**
     * Tableau des index {@link Category#minimum} du tableau <code>byValues</code>.
     */
    private final double[] values;
    
    /**
     * Unités des mesures géophysiques représentées par les catégories.
     * Ce champ peut être nul s'il ne s'applique pas ou si les unités
     * ne sont pas connues.
     */
    private final Unit unit;
    
    /**
     * Nombre de chiffres significatifs après la virgule.
     * Cette information est utilisée pour les écritures
     * des valeurs géophysiques des thèmes.
     */
    private final int ndigits;
    
    /**
     * Locale used for creating {@link #format}.
     * May be <code>null</code> if default locale
     * was requested.
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
     * catégories du tableau {@link #byIndex}.
     */
    private transient ColorModel[] colors;
    
    /**
     * Catégorie utilisée lors du dernier encodage ou décodage d'un pixel.  Avant de rechercher
     * la catégorie appropriée pour un nouveau encodage ou décodage, on vérifiera d'abord si la
     * catégorie désirée n'est pas la même que la dernière fois, c'est-à-dire {@link #lastCategory}.
     */
    private transient Category lastCategory;
    
    /**
     * Construct a category list for qualitative categories.
     * This category list will have no unit.
     *
     * @param names  Sequence of category names for the values contained in a sample dimension.
     *               This allows for names to be assigned to numerical values. The first entry
     *               in the sequence relates to a cell value of zero. For example:
     *               [0]=“Background“, [1]=“Water”, [2]=“Forest”, [3]=“Urban”.
     */
    public CategoryList(final String[] names) {
        this(Category.list(names));
    }
    
    /**
     * Construct a category list for qualitative categories.
     * This category list will have no unit.
     *
     * @param names  Sequence of category names for the values contained in a sample dimension.
     *               This allows for names to be assigned to numerical values. The first entry
     *               in the sequence relates to a cell value of zero. For example:
     *               [0]=“Background“, [1]=“Water”, [2]=“Forest”, [3]=“Urban”.
     * @param colors Color to assign to each category.
     */
    public CategoryList(final String[] names, final Color[] colors) {
        this(Category.list(names, colors));
    }
    
    /**
     * Construct a category list for a sample
     * dimension (band) with no unit.
     *
     * @param  categories The category list.
     *
     * @throws IllegalArgumentException if two category ranges
     *         <code>[{@link Category#lower lower}..{@link Category#upper upper}]</code> overlap.
     */
    public CategoryList(final Category[] categories) throws IllegalArgumentException {
        this(categories, null);
    }
    
    /**
     * Construct a category list for a sample
     * dimension (band) with the specified units.
     *
     * @param  categories The category list.
     * @param  units      The unit information for this category list's.
     *                    May be <code>null</code> if no category has units.
     *
     * @throws IllegalArgumentException If two category ranges
     *         <code>[{@link Category#lower lower}..{@link Category#upper upper}]</code> overlap.
     */
    public CategoryList(final Category[] categories, final Unit units) throws IllegalArgumentException {
        this(categories, units, getFractionDigitCount(categories));
    }
    
    /**
     * Construct a category list for a sample dimension (band) with
     * the specified units.
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
     * @throws IllegalArgumentException If two category ranges
     *         <code>[{@link Category#lower lower}..{@link Category#upper upper}]</code> overlap.
     */
    private CategoryList(final Category[] categories, final Unit units, final int ndigits) throws IllegalArgumentException {
        this.unit     = units;
        this.ndigits  = ndigits;
        this.byIndex  = CategoryComparator.BY_INDEX .sort(categories);
        this.byValues = CategoryComparator.BY_VALUES.sort(byIndex);
        this.index    = new int   [byIndex .length];
        this.values   = new double[byValues.length];
        for (int i=byIndex.length; --i>=0;) {
            index [i] = byIndex [i].lower;
            values[i] = byValues[i].minimum;
        }
        /*
         * Vérifie que les thèmes ne se chevauchent pas.
         */
        for (int j=0; j<byIndex.length; j++) {
            final Category categ = byIndex[j];
            for (int i=j+1; i<byIndex.length; i++) {
                final Category check = byIndex[i];
                if (!(categ.lower>=check.upper || categ.upper<=check.lower)) { // Do not accept NaN
                    throw new IllegalArgumentException(Resources.format(
                            ResourceKeys.ERROR_RANGE_OVERLAP_$4,
                            new Integer(categ.lower), new Integer(categ.upper-1),
                            new Integer(check.lower), new Integer(check.upper-1)));
                }
                if (categ.minimum<check.maximum && categ.maximum>check.minimum) { // Accept NaN
                    throw new IllegalArgumentException(Resources.format(
                            ResourceKeys.ERROR_RANGE_OVERLAP_$4,
                            new Double(categ.minimum), new Double(categ.maximum),
                            new Double(check.minimum), new Double(check.maximum)));
                }
            }
        }
        assert CategoryComparator.BY_INDEX .isSorted(byIndex );
        assert CategoryComparator.BY_VALUES.isSorted(byValues);
    }
    
    /**
     * Compute the smallest number of fraction digits necessary to resolve
     * all quantitative values.  This method assume that geophysics values
     * in the range {@link Category#minimum} to {@link Category#maximum}
     * are stored as integer sample values in the range {@link Category#lower}
     * to {@link Category#upper}.
     */
    private static int getFractionDigitCount(final Category[] categories) {
        int ndigits = 0;
        for (int i=0; i<categories.length; i++) {
            final Category category = categories[i];
            final double ln = XMath.log10((category.maximum - category.minimum)/
                                          (category.upper   - category.lower));
            if (!Double.isNaN(ln)) {
                final int n = -(int)(Math.floor(ln+1E-6));
                if (n>ndigits && n<=6) {
                    ndigits=n;
                }
            }
        }
        return ndigits;
    }
    
    /**
     * Get the <code>CategoryList</code>'s name.
     * This string may be <code>null</code> if no description is present.
     * The default implementation returns the name of what seems to be the
     * "main" category,  i.e. the quantitative category (if there is one)
     * with the widest sample range.
     *
     * @param  locale The locale, or <code>null</code> for the default one.
     * @return The localized description. If no description was available
     *         in the specified locale, a default locale is used.
     */
    public String getName(final Locale locale) {
        final Category category = getMain();
        if (category!=null) {
            final StringBuffer buffer = new StringBuffer(category.getName(locale));
            buffer.append(' ');
            return String.valueOf(formatRange(buffer, locale));
        }
        return null;
    }
    
    /**
     * Returns the number of categories in this list.
     */
    public int size() {
        return byIndex.length;
    }
    
    /**
     * Returns the category at the specified index.
     * This index may not be related to sample value.
     *
     * @param  index An index in the range <code>[0..{@link #size()}-1]</code>.
     * @return The category at the specified index.
     */
    public Category get(final int index) {
        return byIndex[index];
    }
    
    /**
     * Retourne une catégorie à utiliser pour représenter les données manquantes.
     * Si aucune catégorie ne représente une valeur <code>NaN</code>, alors cette
     * méthode retourne une catégorie arbitraire.
     */
    final Category getBlank() {
        for (int i=0; i<byIndex.length; i++) {
            if (Double.doubleToRawLongBits(byIndex[i].minimum) ==
                Double.doubleToRawLongBits(Double.NaN))
            {
                return byIndex[i];
            }
        }
        for (int i=0; i<byIndex.length; i++) {
            if (Double.isNaN(byIndex[i].minimum)) {
                return byIndex[i];
            }
        }
        if (byIndex.length!=0) {
            return byIndex[0];
        }
        return new Category(Resources.format(ResourceKeys.NODATA), Color.black, 0);
    }
    
    /**
     * Returns what seems to be the "main" category. The default implementation looks
     * for the quantitative category (if there is one) with the widest sample range.
     */
    private Category getMain() {
        int range=0;
        Category category=null;
        for (int i=byValues.length; --i>=0;) {
            final Category candidate = byValues[i];
            if (candidate!=null && candidate.isQuantitative()) {
                final int candidateRange = candidate.upper - candidate.lower;
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
     * If <code>geophysicsValues</code> is <code>true</code>, then the returned range
     * is expressed in {@link #getUnits} units. Otherwise, it is expressed as sample
     * index.
     *
     * @param  geophysicsValues <code>true</code> for the range of geophysics values
     *         <code>[{@link Category#minimum}..{@link Category#maximum}]</code>,
     *         or <code>false</code> for the range of sample index
     *         <code>[{@link Category#lower}..{@link Category#upper}]</code>.
     * @return The range of values, or <code>null</code> if this category list
     *         has no category.
     */
    public Range getRange(final boolean geophysicsValues) {
        if (geophysicsValues) {
            assert CategoryComparator.BY_VALUES.isSorted(byValues);
            int max = byValues.length;
            if (max!=0) {
                while (--max!=0 && Double.isNaN(byValues[max].maximum));
                final double minimum = byValues[0  ].minimum;
                final double maximum = byValues[max].maximum;
                assert(minimum <= maximum);
                return new Range(Double.class, new Double(minimum), true,
                                               new Double(maximum), false);
            }
        }  else {
            assert CategoryComparator.BY_INDEX.isSorted(byIndex);
            if (byIndex.length!=0) {
                final int lower = byIndex[0].lower;
                final int upper = byIndex[byIndex.length-1].upper;
                assert(lower < upper);
                return new Range(Integer.class, new Integer(lower), true,
                                                new Integer(upper), false);
            }
        }
        return null;
    }
    
    /**
     * Format a geophysics value. If <code>value</code> is a real number, then the value is
     * formatted with the appropriate number of digits and the units symbol.  Otherwise, if
     * <code>value</code> is <code>NaN</code>, then the category name is returned.
     *
     * @param  value  The geophysics value (may be <code>NaN</code>).
     * @param  locale Locale to use for formatting, or <code>null</code>
     *                for the default locale.
     * @return A string representation of the geophysics value.
     */
    public String format(final double value, final Locale locale) {
        if (Double.isNaN(value)) {
            final Category category = getEncoder(value, lastCategory);
            if (category!=null) {
                lastCategory = category;
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
     * @param  locale Conventions locales à utiliser, ou <code>null</code> pour les conventions par défaut.
     * @param  buffer Le buffer dans lequel écrire la valeur.
     * @return Le buffer <code>buffer</code> dans lequel auront été écrit la valeur et les unités.
     */
    private synchronized StringBuffer format(final double value, final boolean writeUnits, final Locale locale, StringBuffer buffer) {
        if (format==null || !Utilities.equals(this.locale, locale)) {
            this.locale = locale;
            format=(locale!=null) ? NumberFormat.getNumberInstance(locale) :
                                    NumberFormat.getNumberInstance();
            format.setMinimumFractionDigits(ndigits);
            format.setMaximumFractionDigits(ndigits);
            dummy=new FieldPosition(0);
        }
        buffer = format.format(value, buffer, dummy);
        if (writeUnits && unit!=null) {
            final int position = buffer.length();
            buffer.append('\u00A0'); // No-break space
            buffer.append(unit);
            if (buffer.length()==position+1) {
                buffer.setLength(position);
            }
        }
        return buffer;
    }
    
    /**
     * Format the range of geophysics values.
     */
    private StringBuffer formatRange(StringBuffer buffer, final Locale locale) {
        final Range range = getRange(true);
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
     * spécifié en valeur géophysique. Si aucune catégorie ne convient,
     * alors cette méthode retourne <code>null</code>.
     *
     * @param  index Valeur à transformer.
     * @param  category Catégorie présumée du pixel, ou <code>null</code>.
     *         Il n'est pas nécessaire que cette information soit exacte,
     *         mais cette méthode sera plus rapide si elle l'est.
     * @return La catégorie du pixel, ou <code>null</code>.
     */
    final Category getDecoder(final int index, Category category) {
        // Le '!' ci-dessous est un reliquat du temps où on utilisait
        // le type 'float'. Il était important à cause des NaN.
        if (category==null || !(index>=category.lower && index<category.upper)) {
            /*
             * Si la catégorie n'est pas la même que la dernière fois,
             * recherche à quelle autre catégorie pourrait appartenir le pixel.
             */
            int i=Arrays.binarySearch(this.index, index);
            if ((i>=0 || (i=~i-1)>=0) && i<byIndex.length) {
                category=byIndex[i];
                assert(this.index[i] == category.lower);
                if (!(index>=category.lower && index<category.upper)) {
                    return null;
                }
            } else {
                return null;
            }
        }
        return category;
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
        if (category==null ||
        (!(value>=category.minimum && value<category.maximum) && // Le '!' est important à cause des NaN
                Double.doubleToRawLongBits(value) != Double.doubleToRawLongBits(category.minimum)))
        {
            /*
             * Si la catégorie n'est pas la même que la dernière fois,
             * recherche à quelle catégorie pourrait appartenir la valeur.
             * Note: Les valeurs 'NaN' sont à la fin du tableau 'values'. Donc:
             *
             * 1) Si 'value' est NaN,  alors 'i' pointera forcément sur une catégorie NaN.
             * 2) Si 'value' est réel, alors 'i' peut pointer sur une des catégories de
             *    valeurs réels ou sur la première catégorie de NaN.
             */
            int i=binarySearch(values, value); // Special 'binarySearch' for NaN values.
            if (i>=0) {
                // The value is exactly equals to one of Category.minimum,
                // or is one of NaN values. There is nothing else to do.
                category = byValues[i];
            } else {
                if (Double.isNaN(value)) {
                    // The value is NaN, but not one of the registered ones.
                    // Consequently, we can't map a category to this value.
                    return null;
                }
                // 'binarySearch' found the index of "insertion point" (~i). This means that
                // 'value' is lower than 'Category.minimum' at this index.  Consequently, if
                // this value fits in a category's range, it fits in the previous category (~i-1).
                i = ~i-1;
                if (i<0) {
                    // If the value is smaller than the smallest Category.minimum, returns
                    // the first category (except if there is only NaN categories).
                    if (byValues.length==0) {
                        return null;
                    }
                    category = byValues[0];
                    if (Double.isNaN(category.minimum)) {
                        return null;
                    }
                } else {
                    category = byValues[i];
                    assert(value >= category.minimum);
                    // We found the probable category.  If value is outside
                    // any category's range (including this one), it may be
                    // closer to the next category than the current one...
                    if (value >= category.maximum  &&  i+1 < byValues.length) {
                        final Category upper = byValues[i+1];
                        // assert: if 'upper.minimum' was smaller than 'value',
                        //         it should has been found by 'binarySearch'.
                        //         We use '!' in order to accept NaN values.
                        assert !(upper.minimum <= value);
                        if (upper.minimum-value <= value-category.maximum) {
                            category = upper;
                        }
                    }
                }
            }
        }
        // assert: after converting geophysics value to sample
        //         value, it should stay in the same category.
        assert(category==null || category==getDecoder(category.toIndex(value), category)) : category;
        return category;
    }
    
    /**
     * Convert a sample index into a geophysics value.
     *
     * @param  index The sample index.
     * @return The geophysics value, in the units {@link #getUnits}. This
     *         value may be one of the many <code>NaN</code> values if the
     *         sample do not belong to a quantative category. Many
     *         <code>NaN</code> values are possibles, which make it possible
     *         to differenciate among many qualitative categories.
     *
     * @see Category#toValue
     * @see #toValues
     * @see #toIndex
     */
    public double toValue(final int index) {
        final Category category = getDecoder(index, lastCategory);
        if (category!=null) {
            lastCategory = category;
            return category.toValue(index);
        }
        return Double.NaN;
    }
    
    /**
     * Convert a geophysics value into a sample index. <code>value</code> must be
     * in the units {@link #getUnits}. If <code>value</code> is <code>NaN</code>,
     * then this method returns the sample value for one of the qualitative categories.
     * Many different <code>NaN</code> are alowed, which make it possible to differenciate
     * among many qualitative categories.
     *
     * @param  value The geophysics value (may be <code>NaN</code>). If this value is
     *               outside the allowed range of values, it will be clamped to the
     *               minimal or the maximal value.
     * @return The sample value.
     *
     * @see Category#toIndex
     * @see #toIndexed
     * @see #toValue
     */
    public int toIndex(final double value) {
        final Category category = getEncoder(value, lastCategory);
        if (category!=null) {
            lastCategory = category;
            return category.toIndex(value);
        }
        return 0;
    }
    
    /**
     * Returns a view of an image in which all pixels have been transformed into
     * floating-point values as with the {@link #toValue(int)} method. The resulting
     * image usually represents some geophysics parameter in "real world" scientific
     * and engineering units (e.g. temperature in °C).
     *
     * @param image      Image to convert. This image usually store pixel values as integers.
     * @param categories The list of categories to use for transforming pixel values into
     *                   geophysics parameters. This array's length must matches the number
     *                   of bands in <code>image</code>.
     * @return           The converted image. This image store geophysics values as floating-point
     *                   numbers. This method returns <code>null</code> if <code>image</code> was null.
     *
     * @see #toValue
     * @see #toIndexed
     */
    public static RenderedImage toValues(final RenderedImage image, final CategoryList[] categories) {
        return NumericImage.getInstance(image, categories);
    }
    
    /**
     * Returns a view of an image in which all geophysics values have been transformed
     * into indexed pixel as with the {@link #toIndex(double)} method.   The resulting
     * image is more suitable for rendering than the geophysics image (since Java2D do
     * a better job with integer pixels than floating-point pixels).
     *
     * @param image      Image to convert. This image usually represents some geophysics
     *                   parameter in "real world" scientific and engineering units (e.g.
     *                   temperature in °C).
     * @param categories The list of categories to use for transforming floating-point values
     *                   into pixel index. This array's length must matches the number of bands
     *                   in <code>image</code>.
     * @return           The converted image. This image store pixel index as integer.
     *                   This method returns <code>null</code> if <code>image</code> was null.
     *
     * @see #toIndex
     * @see #toValues
     */
    public static RenderedImage toIndexed(final RenderedImage image, final CategoryList[] categories) {
        return ThematicImage.getInstance(image, categories);
    }
    
    /**
     * Returns a color model for this category list. The default implementation
     * may build up the color model from each category's colors (as returned by
     * {@link Category#getColors}). The returned color model will use data type
     * {@link DataBuffer#TYPE_FLOAT} for geophysical values, or an integer data
     * type for sample index.
     *
     * @param  geophysicsValues <code>true</code> to request a color model applicable to geophysics
     *         values <code>[{@link Category#minimum}..{@link Category#maximum}]</code>,
     *         or <code>false</code> to request a color model applicable to sample index
     *         <code>[{@link Category#lower}..{@link Category#upper}]</code>.
     * @return The requested color model.
     */
    public ColorModel getColorModel(final boolean geophysicsValue) {
        return getColorModel(geophysicsValue, 1);
    }
    
    /**
     * Returns a color model for this category list. The default implementation
     * may build up the color model from each category's colors (as returned by
     * {@link Category#getColors}). The returned color model will use data type
     * {@link DataBuffer#TYPE_FLOAT} for geophysical values, or an integer data
     * type for sample index.
     *
     * @param  geophysicsValues <code>true</code> to request a color model applicable to geophysics
     *         values <code>[{@link Category#minimum}..{@link Category#maximum}]</code>,
     *         or <code>false</code> to request a color model applicable to sample index
     *         <code>[{@link Category#lower}..{@link Category#upper}]</code>.
     * @param  numBands The number of bands for the color model.  The returned color model may
     *         take in account only the first band and ignore the others, but the existence of
     *         all <code>numBands</code> will be at least tolerated.  Supplemental bands, even
     *         invisible, are useful for processing with Java Advanced Imaging.
     * @return The requested color model.
     */
    final synchronized ColorModel getColorModel(final boolean geophysicsValue, final int numBands) {
        final int cacheIndex = (numBands*2) + (geophysicsValue ? 0 : 1);
        // Look in the cache for an existing color model.
        if (colors!=null && cacheIndex<colors.length) {
            final ColorModel candidate = colors[cacheIndex];
            if (candidate != null) {
                return candidate;
            }
        }
        final ColorModel model;
        if (geophysicsValue) {
            final ColorSpace colors = new ScaledColorSpace(numBands, getRange(true));
            model = RasterFactory.createComponentColorModel(DataBuffer.TYPE_FLOAT, colors, false, false, Transparency.OPAQUE);
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
            if (byIndex.length==0) {
                // Construct a gray scale palette.
                final byte[] RGB = new byte[256];
                for (int i=0; i<RGB.length; i++) RGB[i] = (byte)i;
                model = new IndexColorModel(8, RGB.length, RGB, RGB, RGB);
            } else {
                /*
                 * Calcule le nombre de couleurs de la palette
                 * en cherchant l'index le plus élevé des thèmes.
                 */
                assert CategoryComparator.BY_INDEX.isSorted(byIndex);
                final int mapSize = Math.round(byIndex[byIndex.length-1].upper);
                final int[]  ARGB = new int[mapSize];
                /*
                 * Interpole les codes de couleurs dans la palette. Les couleurs
                 * correspondantes aux plages non-définies par un thème seront transparentes.
                 */
                for (int i=0; i<byIndex.length; i++) {
                    final Category category = byIndex[i];
                    ImageUtilities.expand(category.getColors(), ARGB, Math.round(category.lower), Math.round(category.upper));
                }
                model = ImageUtilities.getIndexColorModel(ARGB);
            }
        }
        // Cache and returns the color model
        final int minCacheLength = (cacheIndex+2) & ~1;
        if (colors==null) {
            colors=new ColorModel[minCacheLength];
        } else if (colors.length<minCacheLength) {
            colors = (ColorModel[]) XArray.resize(colors, minCacheLength);
        }
        return colors[cacheIndex] = model;
    }
    
    /**
     * Returns a hash value for this category list.
     * This value need not remain consistent between
     * different implementations of the same class.
     */
    public int hashCode() {
        int code = 458783261;
        for (int i=0; i<byIndex.length; i++) {
            code = code*37 + byIndex[i].hashCode();
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
            if (this.ndigits == that.ndigits                 &&
                Utilities.equals(this.unit,    that.unit   ) &&
                Arrays.equals(this.byIndex, that.byIndex))
            {
                assert Arrays.equals(this.byValues, that.byValues) &&
                       Arrays.equals(this.index,    that.index   ) &&
                       Arrays.equals(this.values,   that.values  );
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
        for (int i=0; i<byIndex.length; i++) {
            buffer.append("   ");
            buffer.append(byIndex[i]==main ? '*' : ' ');
            buffer.append(byIndex[i]);
            buffer.append(lineSeparator);
        }
        return buffer.toString();
    }
    
    /**
     * Returns all categories in this <code>CategoryList</code>.
     */
    public Category[] toArray() {
        return (Category[]) byIndex.clone();
    }
    
    /**
     * Effectue une recherche bi-linéaire de la valeur spécifiée. Cette
     * méthode est semblable à {@link Arrays#binarySearch(double[],double)},
     * excepté qu'elle peut distinguer différentes valeurs de NaN.
     */
    private static int binarySearch(final double[] array, final double key) {
        int low  = 0;
        int high = array.length-1;
        final boolean keyIsNaN = Double.isNaN(key);
        while (low <= high) {
            final int mid = (low + high)/2;
            final double midVal = array[mid];
            
            final int cmp;
            if      (midVal < key) cmp = -1; // Neither val is NaN, thisVal is smaller
            else if (midVal > key) cmp = +1; // Neither val is NaN, thisVal is larger
            else {
                /*
                 * The following is an adaptation of evaluator's comments for bug #4471414
                 * (http://developer.java.sun.com/developer/bugParade/bugs/4471414.html).
                 * Extract from evaluator's comment:
                 *
                 *     [This] code is not guaranteed to give the desired results because
                 *     of laxity in IEEE 754 regarding NaN values. There are actually two
                 *     types of NaNs, signaling NaNs and quiet NaNs. Java doesn't support
                 *     the features necessary to reliably distinguish the two.  However,
                 *     the relevant point is that copying a signaling NaN may (or may not,
                 *     at the implementors discretion) yield a quiet NaN -- a NaN with a
                 *     different bit pattern (IEEE 754 6.2).  Therefore, on IEEE 754 compliant
                 *     platforms it may be impossible to find a signaling NaN stored in an
                 *     array since a signaling NaN passed as an argument to binarySearch may
                 *     get replaced by a quiet NaN.
                 */
                final long midRawBits = Double.doubleToRawLongBits(midVal);
                final long keyRawBits = Double.doubleToRawLongBits(key);
                if (midRawBits != keyRawBits) {
                    final boolean midIsNaN = Double.isNaN(midVal);
                    if (keyIsNaN) {
                        // If (mid,key)==(!NaN, NaN): -1.
                        // If two NaN arguments, compare NaN bits.
                        cmp = (!midIsNaN || midRawBits<keyRawBits) ? -1 : +1;
                    } else {
                        // If (mid,key)==(NaN, !NaN): +1.
                        // Otherwise, case for (-0.0, 0.0) and (0.0, -0.0).
                        cmp = (!midIsNaN && midRawBits<keyRawBits) ? -1 : +1;
                    }
                } else {
                    cmp = 0;
                }
            }
            if      (cmp < 0) low  = mid + 1;
            else if (cmp > 0) high = mid - 1;
            else return mid; // key found
        }
        return -(low + 1);  // key not found.
    }
}
