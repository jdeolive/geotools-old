/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2003-2006, GeoTools Project Managment Committee (PMC)
 *    (C) 2003, Institut de Recherche pour le Développement
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    version 2.1 of the License.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
package org.geotools.display.canvas;

// J2SE and JAI dependencies
import java.util.List;
import java.util.ArrayList;
import java.awt.image.VolatileImage;
import java.awt.image.BufferedImage;

// OpenGIS dependencies
import org.opengis.util.CodeList;


/**
 * The image type for offscreen buffer. The offscreen buffer may be backed by a
 * {@link VolatileImage} for fast rendering, or by a {@link BufferedImage} for
 * longer persistence.
 *
 * @since 2.3
 * @source $URL$
 * @version $Id$
 * @author Martin Desruisseaux
 *
 * @see BufferedCanvas2D#setOffscreenBuffered
 */
public final class ImageType extends CodeList<ImageType> {
    /**
     * Serial number for interoperability with different versions.
     */
    private static final long serialVersionUID = 5596401799358607369L;

    /**
     * List of all enumerations of this type.
     * Must be declared before any enum declaration.
     */
    private static final List<ImageType> VALUES = new ArrayList<ImageType>(3);

    /**
     * The enum for layers not backed by any offscreen buffer.
     */
    public static final ImageType NONE = new ImageType("NONE");
    
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
     *
     * @todo Update this doc when we will be allowed to compile for J2SE 1.5, since transparency
     *       is supported there.
     */
    public static final ImageType VOLATILE = new ImageType("VOLATILE");
    
    /**
     * The enum for offscreen buffer backed by a {@link BufferedImage}.
     * At the difference of {@link #VOLATILE}, buffered image supports
     * transparency. It may be more appropriate for layer above the base map.
     */
    public static final ImageType BUFFERED = new ImageType("BUFFERED");
    
    /**
     * Constructs an enum with the given name. The new enum is
     * automatically added to the list returned by {@link #values}.
     *
     * @param name The enum name. This name must not be in use by an other enum of this type.
     */
    private ImageType(final String name) {
        super(name, VALUES);
    }

    /**
     * Returns the list of {@code ImageType}s.
     */
    public static ImageType[] values() {
        synchronized (VALUES) {
            return VALUES.toArray(new ImageType[VALUES.size()]);
        }
    }

    /**
     * Returns the list of enumerations of the same kind than this enum.
     */
    public ImageType[] family() {
        return values();
    }
}
