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
 */
package org.geotools.io.image;

// J2SE dependencies
import java.awt.color.ColorSpace;

// Geotools dependencies
import org.geotools.resources.Utilities;


/**
 * Color space for images storing pixels as real numbers.
 *
 * @version 1.0
 * @author Martin Desruisseaux
 */
final class ScaledColorSpace extends ColorSpace {
    /**
     * Minimal normalized RGB value.
     */
    private static final float MIN_VALUE = 0f;
    
    /**
     * Maximal normalized RGB value.
     */
    private static final float MAX_VALUE = 1f;
    
    /**
     * Facteur par lequel multiplier les pixels.
     */
    private final float scale;
    
    /**
     * Nombre à aditionner aux pixels après
     * les avoir multiplier par {@link #scale}.
     */
    private final float offset;
    
    /**
     * Construit un modèle de couleurs.
     *
     * @param numComponents Nombre de composante (seule la première sera prise en compte).
     * @param minimum Valeur minimale des nombres réels à décoder.
     * @param maximum Valeur maximale des nombres réels à décoder.
     */
    public ScaledColorSpace(final int numComponents, final float minimum, final float maximum) {
        super(TYPE_GRAY, numComponents);
        scale  = (maximum-minimum)/(MAX_VALUE-MIN_VALUE);
        offset = minimum - MIN_VALUE*scale;
    }
    
    /**
     * Returns a RGB color for a gray scale value.
     */
    public float[] toRGB(final float[] values) {
        float value = (values[0]-offset)/scale;
        if (Float.isNaN(value)) {
            value=MIN_VALUE;
        }
        return new float[] {value, value, value};
    }
    
    /**
     * Returns a real value for the specified RGB color.
     * The RGB color is assumed to be a gray scale value.
     */
    public float[] fromRGB(final float[] RGB) {
        final float[] values = new float[getNumComponents()];
        values[0] = (RGB[0]+RGB[1]+RGB[2])/3*scale + offset;
        return values;
    }
    
    /**
     * Convert a color to the CIEXYZ color space.
     */
    public float[] toCIEXYZ(final float[] values) {
        float value = (values[0]-offset)/scale;
        if (Float.isNaN(value)) {
            value=MIN_VALUE;
        }
        return new float[] {
            value*0.9642f,
            value*1.0000f,
            value*0.8249f
        };
    }
    
    /**
     * Convert a color from the CIEXYZ color space.
     */
    public float[] fromCIEXYZ(final float[] RGB) {
        final float[] values = new float[getNumComponents()];
        values[0] = (RGB[0]/0.9642f + RGB[1] + RGB[2]/0.8249f)/3*scale + offset;
        return values;
    }
    
    /**
     * Returns the minimum value for the specified RGB component.
     */
    public float getMinValue(final int component) {
        return MIN_VALUE*scale + offset;
    }
    
    /**
     * Returns the maximum value for the specified RGB component.
     */
    public float getMaxValue(final int component) {
        return MAX_VALUE*scale + offset;
    }
    
    /**
     * Returns a string representation of this color model.
     */
    public String toString() {
        return Utilities.getShortClassName(this)+'['+getMinValue(0)+", "+getMaxValue(0)+']';
    }
}
