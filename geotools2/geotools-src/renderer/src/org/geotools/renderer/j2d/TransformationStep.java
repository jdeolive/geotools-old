/*
 * Geotools - OpenSource mapping toolkit
 * (C) 2001, Institut de Recherche pour le Développement
 * (C) 1998, Pêches et Océans Canada
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
package org.geotools.renderer.j2d;

// J2SE and JAI dependencies
import java.awt.geom.AffineTransform;
import java.io.ObjectStreamException;
import java.util.NoSuchElementException;
import javax.media.jai.EnumeratedParameter;


/**
 * Enumeration class specifing the transformation step. Transformation steps are
 * {@link AffineTransform} performing transformations between three coordinate
 * systems:
 * <ul>
 *   <li><strong>Logical coordinates</strong>, usually "real world" meters.</li>
 *   <li><strong>Point coordinates</strong>, usually 1/72 of inch on the rendering
 *       device (screen or printer).</li>
 *   <li><strong>Pixel (or device) coordinates</strong> which are device-dependent.</li>
 * </ul>
 *
 * @version $Id: TransformationStep.java,v 1.1 2003/01/20 00:06:35 desruisseaux Exp $
 * @author Martin Desruisseaux
 *
 * @see RenderingContext#getAffineTransform
 */
public final class TransformationStep extends EnumeratedParameter {
    /**
     * Serial number for interoperability with different versions.
     */
    private static final long serialVersionUID = -3433597537353834111L;
    
    /**
     * Constante désignant la transformation affine qui permet de
     * passer des coordonnées logiques vers des unités de points.
     */
    public static final TransformationStep WORLD_TO_POINT = new TransformationStep("WORLD_TO_POINT", 0);
    
    /**
     * Constante désignant la transformation affine qui permet de
     * passer des des unités de points vers des coordonnées pixels.
     */
    public static final TransformationStep POINT_TO_PIXEL = new TransformationStep("POINT_TO_PIXEL", 1);
    
    /**
     * Transformation steps type by value. Used
     * to canonicalize after deserialization.
     */
    private static final TransformationStep[] ENUMS = {
        WORLD_TO_POINT, POINT_TO_PIXEL
    };
    static {
        for (int i=0; i<ENUMS.length; i++) {
            if (ENUMS[i].getValue() != i) {
                throw new AssertionError(ENUMS[i]);
            }
        }
    }
    
    /**
     * Construct a new enum with the specified value.
     */
    private TransformationStep(final String name, final int value) {
        super(name, value);
    }
    
    /**
     * Return the enum for the specified value.
     *
     * @param  value The enum value.
     * @return The enum for the specified value.
     * @throws NoSuchElementException if there is no enum for the specified value.
     */
    static TransformationStep getEnum(final int value) throws NoSuchElementException {
        if (value>=0 && value<ENUMS.length) return ENUMS[value];
        throw new NoSuchElementException(String.valueOf(value));
    }
    
    /**
     * Use a single instance of {@link TransformationStep} after deserialization.
     * It allow client code to test <code>enum1==enum2</code> instead of
     * <code>enum1.equals(enum2)</code>.
     *
     * @return A single instance of this enum.
     * @throws ObjectStreamException is deserialization failed.
     */
    private Object readResolve() throws ObjectStreamException {
        return getEnum(getValue());
    }
}
