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

// Color model
import java.awt.color.ColorSpace;
import java.awt.image.ColorModel;
import java.awt.image.IndexColorModel;

// Other J2SE and JAI dependencies
import java.util.Locale;
import java.io.ObjectStreamException;
import java.util.NoSuchElementException;
import javax.media.jai.EnumeratedParameter;

// OpenGIS dependencies
import org.opengis.cv.CV_ColorInterpretation;

// Resources
import org.geotools.resources.gcs.Resources;
import org.geotools.resources.gcs.ResourceKeys;


/**
 * Enumeration class specifing the
 * mapping of a band to a color model component.
 *
 * @version 1.00
 * @author OpenGIS (www.opengis.org)
 * @author Martin Desruisseaux
 *
 * @see org.opengis.cv.CV_ColorInterpretation
 */
public final class ColorInterpretation extends EnumeratedParameter {
    /**
     * Serial number for interoperability with different versions.
     */
    private static final long serialVersionUID = -880153999326204504L;
    
    /**
     * Band is not associated with a color model component.
     * @see CV_ColorInterpretation#CV_Undefined
     */
    public static final ColorInterpretation UNDEFINED = new ColorInterpretation("UNDEFINED",
                        CV_ColorInterpretation.CV_Undefined, ResourceKeys.UNDEFINED);
    
    /**
     * Band is an index into a lookup table.
     * @see CV_ColorInterpretation#CV_GrayIndex
     */
    public static final ColorInterpretation GRAY_INDEX = new ColorInterpretation("GRAY_INDEX",
                        CV_ColorInterpretation.CV_GrayIndex, ResourceKeys.GRAY);
    
    /**
     * Band is a color index into a color table.
     * @see CV_ColorInterpretation#CV_PaletteIndex
     */
    public static final ColorInterpretation PALETTE_INDEX = new ColorInterpretation("PALETTE_INDEX",
                        CV_ColorInterpretation.CV_PaletteIndex, ResourceKeys.PALETTE);
    
    /**
     * Bands correspond to RGB color model components.
     * Alpha band may or may not be present.
     * @see CV_ColorInterpretation#CV_RedBand
     */
    public static final ColorInterpretation RED_BAND = new ColorInterpretation("RED_BAND",
                        CV_ColorInterpretation.CV_RedBand, ResourceKeys.RED);
    
    /**
     * Bands correspond to RGB color model components.
     * Alpha band may or may not be present.
     * @see CV_ColorInterpretation#CV_GreenBand
     */
    public static final ColorInterpretation GREEN_BAND = new ColorInterpretation("GREEN_BAND",
                        CV_ColorInterpretation.CV_GreenBand, ResourceKeys.GREEN);
    
    /**
     * Bands correspond to RGB color model components.
     * Alpha band may or may not be present.
     * @see CV_ColorInterpretation#CV_BlueBand
     */
    public static final ColorInterpretation BLUE_BAND = new ColorInterpretation("BLUE_BAND",
                        CV_ColorInterpretation.CV_BlueBand, ResourceKeys.BLUE);
    
    /**
     * Bands correspond to RGB color model components.
     * @see CV_ColorInterpretation#CV_AlphaBand
     */
    public static final ColorInterpretation ALPHA_BAND = new ColorInterpretation("ALPHA_BAND",
                        CV_ColorInterpretation.CV_AlphaBand, ResourceKeys.TRANSPARENCY);
    
    /**
     * Bands correspond to HSL color model.
     * @see CV_ColorInterpretation#CV_HueBand
     */
    public static final ColorInterpretation HUE_BAND = new ColorInterpretation("HUE_BAND",
                        CV_ColorInterpretation.CV_HueBand, ResourceKeys.HUE);
    
    /**
     * Bands correspond to HSL color model.
     * @see CV_ColorInterpretation#CV_SaturationBand
     */
    public static final ColorInterpretation SATURATION_BAND = new ColorInterpretation("SATURATION_BAND",
                        CV_ColorInterpretation.CV_SaturationBand, ResourceKeys.SATURATION);
    
    /**
     * Bands correspond to HSL color model.
     * @see CV_ColorInterpretation#CV_LightnessBand
     */
    public static final ColorInterpretation LIGHTNESS_BAND = new ColorInterpretation("LIGHTNESS_BAND",
                        CV_ColorInterpretation.CV_LightnessBand, ResourceKeys.LIGHTNESS);
    
    /**
     * Bands correspond to CMYK color model.
     * @see CV_ColorInterpretation#CV_CyanBand
     */
    public static final ColorInterpretation CYAN_BAND = new ColorInterpretation("CYAN_BAND",
                        CV_ColorInterpretation.CV_CyanBand, ResourceKeys.CYAN);
    /**
     * Bands correspond to CMYK color model.
     * @see CV_ColorInterpretation#CV_MagentaBand
     */
    public static final ColorInterpretation MAGENTA_BAND = new ColorInterpretation("MAGENTA_BAND",
                        CV_ColorInterpretation.CV_MagentaBand, ResourceKeys.MAGENTA);
    
    /**
     * Bands correspond to CMYK color model.
     * @see CV_ColorInterpretation#CV_YellowBand
     */
    public static final ColorInterpretation YELLOW_BAND = new ColorInterpretation("YELLOW_BAND",
                        CV_ColorInterpretation.CV_YellowBand, ResourceKeys.YELLOW);
    
    /**
     * Bands correspond to CMYK color model.
     * @see CV_ColorInterpretation#CV_BlackBand
     */
    public static final ColorInterpretation BLACK_BAND = new ColorInterpretation("BLACK_BAND",
                        CV_ColorInterpretation.CV_BlackBand, ResourceKeys.BLACK);
    
    /**
     * Color interpretation by value. Used to
     * canonicalize after deserialization.
     */
    private static final ColorInterpretation[] ENUMS = {
        UNDEFINED, GRAY_INDEX,      PALETTE_INDEX,
        RED_BAND,  GREEN_BAND,      BLUE_BAND,      ALPHA_BAND,
        HUE_BAND,  SATURATION_BAND, LIGHTNESS_BAND,
        CYAN_BAND, MAGENTA_BAND,    YELLOW_BAND,    BLACK_BAND
    };
    static {
        for (int i=0; i<ENUMS.length; i++) {
            if (ENUMS[i].getValue()!=i) {
                throw new AssertionError(ENUMS[i]);
            }
        }
    }
    
    /**
     * Resource key, used for building localized name. This key doesn't need to
     * be serialized, since {@link #readResolve} canonicalize enums according their
     * {@link #value}. Furthermore, its value is implementation-dependent (which is
     * an other raison why it should not be serialized).
     */
    private transient final int key;
    
    /**
     * Construct a new enum with the specified value.
     */
    private ColorInterpretation(final String name, final int value, final int key) {
        super(name, value);
        this.key = key;
    }
    
    /**
     * Return the enum for the specified value.
     * This method is provided for compatibility with
     * {@link org.opengis.cv.CV_ColorInterpretation}.
     *
     * @param  value The enum value.
     * @return The enum for the specified value.
     * @throws NoSuchElementException if there is no enum for the specified value.
     */
    public static ColorInterpretation getEnum(final int value) throws NoSuchElementException {
        if (value>=0 && value<ENUMS.length) return ENUMS[value];
        throw new NoSuchElementException(String.valueOf(value));
    }
    
    /**
     * Return the enum for the specified color model and band number.
     *
     * @param  model The color model.
     * @param  band  The band to query.
     * @return The enum for the specified color model and band number.
     * @throws IllegalArgumentException if the band number is not in the valid range.
     */
    public static ColorInterpretation getEnum(final ColorModel model, final int band) throws IllegalArgumentException {
        if (band<0 || band>=model.getNumComponents()) {
            throw new IllegalArgumentException(Resources.format(ResourceKeys.ERROR_BAD_BAND_NUMBER_$1, new Integer(band)));
        }
        if (model instanceof IndexColorModel) {
            return PALETTE_INDEX;
        }
        switch (model.getColorSpace().getType()) {
            case ColorSpace.TYPE_GRAY: {
                switch (band) {
                    case  0: return GRAY_INDEX;
                    default: return UNDEFINED;
                }
            }
            case ColorSpace.TYPE_RGB: {
                switch (band) {
                    case  0: return RED_BAND;
                    case  1: return GREEN_BAND;
                    case  2: return BLUE_BAND;
                    case  3: return ALPHA_BAND;
                    default: return UNDEFINED;
                }
            }
            case ColorSpace.TYPE_HSV: {
                switch (band) {
                    case  0: return HUE_BAND;
                    case  1: return SATURATION_BAND;
                    case  2: return LIGHTNESS_BAND;
                    default: return UNDEFINED;
                }
            }
            case ColorSpace.TYPE_CMY:
            case ColorSpace.TYPE_CMYK: {
                switch (band) {
                    case  0: return CYAN_BAND;
                    case  1: return MAGENTA_BAND;
                    case  2: return YELLOW_BAND;
                    case  3: return BLACK_BAND;
                    default: return UNDEFINED;
                }
            }
            default: return UNDEFINED;
        }
    }
    
    /**
     * Returns this enum's name in the specified locale.
     * If no name is available for the specified locale,
     * a default one will be used.
     *
     * @param  locale The locale, or <code>null</code> for the default locale.
     * @return Enum's name in the specified locale.
     */
    public String getName(final Locale locale) {
        return Resources.getResources(locale).getString(key);
    }
    
    /**
     * Use a single instance of {@link ColorInterpretation} after deserialization.
     * It allow client code to test <code>enum1==enum2</code> instead of
     * <code>enum1.equals(enum2)</code>.
     *
     * @return A single instance of this enum.
     * @throws ObjectStreamException is deserialization failed.
     */
    private Object readResolve() throws ObjectStreamException {
        final int value = getValue();
        if (value>=0 && value<ENUMS.length) return ENUMS[value]; // Canonicalize
        else return ENUMS[0]; // Collapse unknow value to a single canonical one
    }
}
