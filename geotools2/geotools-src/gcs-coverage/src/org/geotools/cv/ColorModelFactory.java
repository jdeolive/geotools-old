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
package org.geotools.cv;

// J2SE dependencies
import java.util.Map;
import java.util.Arrays;

// Images and colors
import java.awt.Color;
import java.awt.Transparency;
import java.awt.color.ColorSpace;
import java.awt.image.ColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.RenderedImage;
import java.awt.image.IndexColorModel;
import java.awt.image.ComponentColorModel;

// JAI dependencies
import javax.media.jai.RasterFactory;
import javax.media.jai.FloatDoubleColorModel;
import javax.media.jai.ComponentSampleModelJAI;

// Resources
import org.geotools.util.WeakValueHashMap;
import org.geotools.resources.gcs.Resources;
import org.geotools.resources.gcs.ResourceKeys;
import org.geotools.resources.ComponentColorModelJAI;
import org.geotools.resources.ColorUtilities;


/**
 * A factory for {@link ColorModel} objects built from a list of {@link Category} objects.
 * This factory provides only one public static method: {@link #getColorModel}.  Instances
 * of {@link ColorModel} are shared among all callers in the running virtual machine.
 *
 * @version $Id: ColorModelFactory.java,v 1.8 2003/07/22 15:24:53 desruisseaux Exp $
 * @author Martin Desruisseaux
 */
final class ColorModelFactory {
    /**
     * Modèles de couleurs suggérés pour l'affichage des catégories. Ces modèles de couleurs
     * peuvent être construits à partir des couleurs qui ont été définies dans les différentes
     * catégories du tableau {@link #categories}.
     */
    private static final Map colors = new WeakValueHashMap();

    /**
     * The list of categories for the construction of a single instance of a {@link ColorModel}.
     */
    private final Category[] categories;
    
    /**
     * The visible band (usually 0) used for the construction
     * of a single instance of a {@link ColorModel}.
     */
    private final int visibleBand;

    /**
     * The number of bands (usually 1) used for the construction
     * of a single instance of a {@link ColorModel}.
     */
    private final int numBands;

    /**
     * The color model type. One of {@link DataBuffer#TYPE_BYTE}, {@link DataBuffer#TYPE_USHORT},
     * {@link DataBuffer#TYPE_FLOAT} or {@link DataBuffer#TYPE_DOUBLE}.
     *
     * @task TODO: The user may want to set explicitly the number of bits each pixel occupied.
     *             We need to think about an API to allows that.
     */
    private final int type;

    /**
     * Construct a new <code>ColorModelFactory</code>. This object will actually be used
     * as a key in a {@link Map}, so this is not really a <code>ColorModelFactory</code>
     * but a kind of "<code>ColorModelKey</code>" instead. However, since this constructor
     * is private, user doesn't need to know that.
     */
    private ColorModelFactory(final Category[] categories, final int type,
                              final int visibleBand, final int numBands)
    {
        this.categories  = categories;
        this.visibleBand = visibleBand;
        this.numBands    = numBands;
        this.type        = type;
        if (visibleBand<0 || visibleBand>=numBands) {
            throw new IllegalArgumentException(Resources.format(
                    ResourceKeys.ERROR_BAD_BAND_NUMBER_$1, new Integer(visibleBand)));
        }
    }
    
    /**
     * Returns a color model for a category set. This method builds up the color model
     * from each category's colors (as returned by {@link Category#getColors}).
     *
     * @param  categories The set of categories.
     * @param  type The color model type. One of {@link DataBuffer#TYPE_BYTE},
     *         {@link DataBuffer#TYPE_USHORT}, {@link DataBuffer#TYPE_FLOAT} or
     *         {@link DataBuffer#TYPE_DOUBLE}.
     * @param  visibleBand The band to be made visible (usually 0). All other bands, if any
     *         will be ignored.
     * @param  numBands The number of bands for the color model (usually 1). The returned color
     *         model will renderer only the <code>visibleBand</code> and ignore the others, but
     *         the existence of all <code>numBands</code> will be at least tolerated. Supplemental
     *         bands, even invisible, are useful for processing with Java Advanced Imaging.
     * @return The requested color model, suitable for {@link RenderedImage} objects with values
     *         in the <code>{@link CategoryList#getRange}</code> range.
     */
    public static synchronized ColorModel getColorModel(final Category[] categories,
                                                        final int type,
                                                        final int visibleBand,
                                                        final int numBands)
    {
        ColorModelFactory key = new ColorModelFactory(categories, type, visibleBand, numBands);
        ColorModel model = (ColorModel) colors.get(key);
        if (model == null) {
            model = key.getColorModel();
            colors.put(key, model);
        }
        return model;
    }
    
    /**
     * Construct the color model.
     */
    private ColorModel getColorModel() {
        if (type != DataBuffer.TYPE_BYTE &&
            type != DataBuffer.TYPE_USHORT)
        {
            // If the requested type is any type not supported by IndexColorModel,
            // fallback on a generic (but very slow!) color model.
            double min = 0;
            double max = 1;
            if (categories.length != 0) {
                min = categories[0].minimum;
                for (int i=categories.length; --i>=0;) {
                    final double val = categories[i].maximum;
                    if (!Double.isNaN(val)) {
                        max = val;
                        break;
                    }
                }
            }
            final int  transparency = Transparency.OPAQUE;
            final ColorSpace colors = new ScaledColorSpace(visibleBand, numBands, min, max);
            if (false) {
                // This is the J2SE implementation of color model. It should be our preferred one.
                // Unfortunatly, as of JAI 1.1 we have to use JAI implementation instead of J2SE's
                // one because javax.media.jai.iterator.RectIter do not work with J2SE's DataBuffer
                // when the data type is float or double.
                return new ComponentColorModel(colors, false, false, transparency, type);
            }
            if (false) {
                // This is the JAI implementation of color model. This implementation work with
                // JAI's RectIter and should in theory support float and double data buffer.
                // Unfortunatly, it seems to completly ignore our custom ColorSpace. We end
                // up basically with all-black or all-white images.
                return new FloatDoubleColorModel(colors, false, false, transparency, type);
            }
            if (true) {
                // Our patched color model extends J2SE's ComponentColorModel (which work correctly
                // with our custom ColorSpace), but create JAI's SampleModel instead of J2SE's one.
                // It make RectIter happy and display colors correctly.
                return new ComponentColorModelJAI(colors, false, false, transparency, type);
            }
            // This factory is not really different from a direct construction of
            // FloatDoubleColorModel. We provide it here just because we must end
            // with something.
            return RasterFactory.createComponentColorModel(type, colors, false, false, transparency);
        }
        if (numBands==1 && categories.length==0) {
            // Construct a gray scale palette.
            final ColorSpace cs = ColorSpace.getInstance(ColorSpace.CS_GRAY);
            final int[] nBits = {DataBuffer.getDataTypeSize(type)};
            return new ComponentColorModel(cs, nBits, false, true, Transparency.OPAQUE, type);
        }
        /*
         * Calcule le nombre de couleurs de la palette
         * en cherchant l'index le plus élevé des thèmes.
         */
        final int mapSize = (int)Math.round(categories[categories.length-1].maximum)+1;
        final int[]  ARGB = new int[mapSize];
        /*
         * Interpole les codes de couleurs dans la palette. Les couleurs
         * correspondantes aux plages non-définies par un thème seront transparentes.
         */
        for (int i=0; i<categories.length; i++) {
            final Category category = categories[i];
            ColorUtilities.expand(category.getColors(), ARGB,
                                  (int)Math.round(category.minimum),
                                  (int)Math.round(category.maximum)+1);
        }
        return ColorUtilities.getIndexColorModel(ARGB, numBands, visibleBand);
    }

    /**
     * Returns a hash code.
     */
    public int hashCode() {
        int code = 962745549 + (numBands*37 + visibleBand)*37 + categories.length;
        for (int i=0; i<categories.length; i++) {
            code += categories[i].hashCode();
            // Better be independant of categories order.
        }
        return code;
    }

    /**
     * Check this object with an other one for equality.
     */
    public boolean equals(final Object other) {
        if (other == this) {
            return true;
        }
        if (other instanceof ColorModelFactory) {
            final ColorModelFactory that = (ColorModelFactory) other;
            return this.numBands    == that.numBands    &&
                   this.visibleBand == that.visibleBand &&
                   Arrays.equals(this.categories, that.categories);
        }
        return false;
    }
}
