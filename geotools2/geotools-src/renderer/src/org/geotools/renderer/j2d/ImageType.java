/*
 * Geotools 2 - OpenSource mapping toolkit
 * (C) 2003, Geotools Project Managment Committee (PMC)
 * (C) 2003, Institut de Recherche pour le Développement
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
import java.awt.image.VolatileImage;
import java.awt.image.BufferedImage;
import java.io.ObjectStreamException;
import java.util.NoSuchElementException;
import javax.media.jai.EnumeratedParameter;


/**
 * The image type for offscreen buffer. The offscreen buffer may be backed by a
 * {@link VolatileImage} for fast rendering, or by a {@link BufferedImage} for
 * longer persistence.
 *
 * @version $Id: ImageType.java,v 1.3 2003/08/18 16:33:42 desruisseaux Exp $
 * @author Martin Desruisseaux
 *
 * @see Renderer#setOffscreenBuffered
 */
public final class ImageType extends EnumeratedParameter {
    /**
     * Serial number for interoperability with different versions.
     */
    private static final long serialVersionUID = -3858397481670269518L;
    
    /**
     * The enum for layers not backed by any offscreen buffer.
     */
    public static final ImageType NONE = new ImageType("NONE", 0);
    
    /**
     * The enum for offscreen buffer backed by a {@link VolatileImage}.
     *
     * <strong>NOTE: Transparency is not yet implemented in current <code>VolatileImage</code>
     * API</strong>. Consequently, setting offscreen buffering for layers other than the base
     * map will hide previously rendered layers. Transparency in volatile image is scheduled
     * for a future J2SE version. See
     *
     * <A HREF="http://developer.java.sun.com/developer/bugParade/bugs/4881082.html">4881082</A>
     *
     * in Sun's RFE database.
     */
    public static final ImageType VOLATILE = new ImageType("VOLATILE", 1);
    
    /**
     * The enum for offscreen buffer backed by a {@link BufferedImage}.
     * At the difference of {@link #VOLATILE}, buffered image supports
     * transparency. It may be more appropriate for layer above the base map.
     */
    public static final ImageType BUFFERED = new ImageType("BUFFERED", 2);

    /**
     * Image types by value. Used to canonicalize after deserialization.
     */
    private static final ImageType[] ENUMS = {NONE, VOLATILE, BUFFERED};
    static {
        for (int i=0; i<ENUMS.length; i++) {
            if (ENUMS[i].getValue()!=i) {
                throw new AssertionError(ENUMS[i]);
            }
        }
    }
    
    /**
     * Constructs a new enum with the specified value.
     */
    private ImageType(final String name, final int value) {
        super(name, value);
    }
    
    /**
     * Returns the enum for the specified value.
     *
     * @param value The enum value.
     * @return The enum for the specified value.
     * @throws NoSuchElementException if there is no enum for the specified value.
     */
    private static ImageType getEnum(final int value) throws NoSuchElementException {
        if (value>=0 && value<ENUMS.length) return ENUMS[value];
        throw new NoSuchElementException(String.valueOf(value));
    }

    /**
     * Uses a single instance of {@link ImageType} after deserialization.
     * It allows client code to test <code>enum1==enum2</code> instead of
     * <code>enum1.equals(enum2)</code>.
     *
     * @return A single instance of this enum.
     * @throws ObjectStreamException if deserialization failed.
     */
    private Object readResolve() throws ObjectStreamException {
        return getEnum(getValue());
    }
}
