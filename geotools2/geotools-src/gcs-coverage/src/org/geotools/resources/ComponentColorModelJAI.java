/*
 * Geotools 2 - OpenSource mapping toolkit
 * (C) 2003, Geotools Project Management Committee (PMC)
 * (C) 2002, Institut de Recherche pour le Développement
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
package org.geotools.resources;

// J2SE dependencies
import java.awt.color.ColorSpace;
import java.awt.image.DataBuffer;
import java.awt.image.SampleModel;
import java.awt.image.ComponentColorModel;
import java.awt.image.ComponentSampleModel;

// JAI dependencies
import javax.media.jai.iterator.RectIter;
import javax.media.jai.FloatDoubleColorModel;
import javax.media.jai.ComponentSampleModelJAI;


/**
 * A {@link ComponentColorModel} modified for interoperability with Java Advanced Imaging.
 * JAI 1.1 was designed for use with J2SE 1.3 and is not aware of new features in J2SE 1.4.
 * This lead to the following problems:
 *
 * <ul>
 *   <li>{@link ComponentColorModel} support <code>float</code> and <code>double</code>
 *       datatypes since J2SE 1.4 only. The workaround for J2SE 1.3 is to use the
 *       {@link FloatDoubleColorModel} provided with JAI 1.1.</li>
 *   <li>{@link FloatDoubleColorModel} ignore the new API in {@link ColorSpace}, especially
 *       the <code>getMinValue</code> and <code>getMaxValue</code> methods. Consequently,
 *       rendering of any image using our custom <code>ScaledColorSpace</code> is wrong.</li>
 *   <li>{@link ComponentColorModel} uses {@link java.awt.image.DataBufferFloat} and {@link
 *       java.awt.image.DataBufferDouble}, which are unknown to JAI 1.1. Consequently, trying
 *       to use {@link RectIter} with one of those will throw {@link ClassCastException}.</li>
 * </ul>
 *
 * The work around it to use J2SE's {@link ComponentColorModel} (which work with our custom
 * {@link ColorSpace}) and override its <code>createCompatibleSampleModel</code> in order to
 * returns {@link ComponentSampleModelJAI} instead of {@link ComponentSampleModel} when
 * <code>float</code> or <code>double</code> datatype is requested.
 *
 * @TODO PATCH: Remove this patch when JAI will recognize J2SE 1.4 classes.
 *
 * @version $ID$
 * @author Martin Desruisseaux
 */
public class ComponentColorModelJAI extends ComponentColorModel {
    /**
     * Construct a new color model.
     */
    public ComponentColorModelJAI(final ColorSpace colorSpace,
                                  final int[] bits,
                                  final boolean hasAlpha,
                                  final boolean isAlphaPremultiplied,
                                  final int transparency,
                                  final int transferType)
    {
        super(colorSpace, bits, hasAlpha, isAlphaPremultiplied, transparency, transferType);
    }

    /**
     * Construct a new color model.
     */
    public ComponentColorModelJAI(final ColorSpace colorSpace,
                                  final boolean hasAlpha,
                                  final boolean isAlphaPremultiplied,
                                  final int transparency,
                                  final int transferType)
    {
        super(colorSpace, hasAlpha, isAlphaPremultiplied, transparency, transferType);
    }

    /**
     * Returns a compatible sample model. This implementation is nearly identical
     * to default J2SE's implementation, except that it construct a JAI color model
     * instead of a J2SE one.
     */
    public SampleModel createCompatibleSampleModel(final int w, final int h) {
        switch (transferType) {
            default: {
                return super.createCompatibleSampleModel(w, h);
            }
            case DataBuffer.TYPE_FLOAT:   // fall through
            case DataBuffer.TYPE_DOUBLE: {
                final int numComponents = getNumComponents();
                final int[] bandOffsets = new int[numComponents];
                for (int i=0; i<numComponents; i++) {
                    bandOffsets[i] = i;
                }
                return new ComponentSampleModelJAI(transferType, w, h, numComponents,
                                                   w*numComponents, bandOffsets);
            }
        }
    }
    
    /**
     * Returns the <code>String</code> representation of the contents of
     * this <code>ColorModel</code>object.
     * @return a <code>String</code> representing the contents of this
     * <code>ColorModel</code> object.
     */
    public String toString() {
       return new String("ComponentColorModelJAI: #pixelBits = "+pixel_bits
                         + " numComponents = "+ super.getNumComponents()
                         + " color space = "+ super.getColorSpace()
                         + " transparency = "+ super.getTransparency()
                         + " has alpha = "+ super.hasAlpha()
                         + " isAlphaPre = "+ super.isAlphaPremultiplied()
                         );
    }
}
