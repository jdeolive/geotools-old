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
 */
package org.geotools.renderer.geom;

// J2SE dependencies
import java.util.Locale;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

// Geotools dependencies
import org.geotools.resources.XRectangle2D;


/**
 * Immutable version of a serializable, high-performance double-precision rectangle.
 *
 * @version $Id: UnmodifiableRectangle.java,v 1.1 2003/05/28 18:06:27 desruisseaux Exp $
 * @author Martin Desruisseaux
 */
final class UnmodifiableRectangle extends XRectangle2D {
    /**
     * Serial number for interoperability with different versions.
     */
    private static final long serialVersionUID = -8196023373680425093L;

    /**
     * Construct a rectangle with the same coordinates than the supplied rectangle.
     *
     * @param rect The rectangle. Use {@link #INFINITY} for initializing
     *             this <code>XRectangle2D</code> with infinite bounds.
     */
    public UnmodifiableRectangle(final Rectangle2D rect) {
        if (rect != null) {
            super.setRect(rect);
        }
    }

    /**
     * Throws {@link UnmodifiableGeometryException}.
     *
     * @deprecated Should never been invoked, since this rectangle is immutable.
     */
    public void setRect(final double x, final double y, final double width, final double height) {
        throw new UnmodifiableGeometryException((Locale)null);
    }

    /**
     * Throws {@link UnmodifiableGeometryException}.
     *
     * @deprecated Should never been invoked, since this rectangle is immutable.
     */
    public void setRect(final Rectangle2D r) {
        throw new UnmodifiableGeometryException((Locale)null);
    }

    /**
     * Throws {@link UnmodifiableGeometryException}.
     *
     * @deprecated Should never been invoked, since this rectangle is immutable.
     */
    public void add(final double x, final double y) {
        throw new UnmodifiableGeometryException((Locale)null);
    }

    /**
     * Throws {@link UnmodifiableGeometryException}.
     *
     * @deprecated Should never been invoked, since this rectangle is immutable.
     */
    public void add(final Rectangle2D rect) {
        throw new UnmodifiableGeometryException((Locale)null);
    }

    /**
     * Returns a mutable version of this rectangle.
     */
    public Object clone() {
        return new XRectangle2D(this);
    }
}
