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
package org.geotools.renderer.geom;

// J2SE dependencies
import java.util.Locale;

// Geotools dependencies
import org.geotools.resources.renderer.Resources;
import org.geotools.resources.renderer.ResourceKeys;


/**
 * Throws when an attempt is made to modify a geometry, but this geometry is part of an other
 * geometry. For example the geometry may be a hole in a {@link Polygon}, or a geometry in a
 * {@link GeometryCollection}. Attempt to change those geometries may corrupt the container,
 * which is why they are not allowed. This exception may be thrown by the following methods:
 * <br><br>
 * <ul>
 *   <li>{@link Geometry#setCoordinateSystem}</li>
 *   <li>{@link Geometry#setResolution}</li>
 *   <li>{@link Geometry#compress}</li>
 *   <li>{@link Polyline#append}</li>
 *   <li>{@link Polyline#appendBorder}</li>
 *   <li>{@link Polyline#prependBorder}</li>
 *   <li>{@link Polygon#addHole}</li>
 *   <li>{@link GeometryCollection#add(Geometry)}</li>
 *   <li>{@link GeometryCollection#remove(Geometry)}</li>
 * </ul>
 * <br><br>
 * If this exception is thrown, the workaround is to {@linkplain Geometry#clone clone}
 * the geometry before to invokes one of the above methods.
 *
 * @version $Id: UnmodifiableGeometryException.java,v 1.1 2003/05/28 18:06:27 desruisseaux Exp $
 * @author Martin Desruisseaux
 */
public class UnmodifiableGeometryException extends IllegalStateException {
    /**
     * Serial number for compatibility with different versions.
     */
    private static final long serialVersionUID = 3256180011529483892L;

    /**
     * Construct an exception with an empty message.
     */
    public UnmodifiableGeometryException() {
        super();
    }

    /**
     * Construct an exception with the specified message.
     */
    public UnmodifiableGeometryException(final String message) {
        super(message);
    }

    /**
     * Construct an exception with a &quot;Unmodifiable geometry&quot; message
     * in the given locale.
     *
     * @param locale The locale, or <code>null</code> for a default one.
     */
    public UnmodifiableGeometryException(final Locale locale) {
        super(Resources.getResources(locale).getString(ResourceKeys.ERROR_UNMODIFIABLE_GEOMETRY));
    }
}
