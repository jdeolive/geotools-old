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
package org.geotools.gui.swing.event;

// Dependencies
import java.util.EventObject;
import java.awt.geom.AffineTransform;


/**
 * An event which indicates that a zoom occurred in a component.
 * This event is usually fired by {@link org.geotools.gui.swing.ZoomPane}.
 *
 * @version $Id: ZoomChangeEvent.java,v 1.5 2003/05/13 11:01:39 desruisseaux Exp $
 * @author Martin Desruisseaux
 */
public class ZoomChangeEvent extends EventObject {
    /**
     * An affine transform indicating the zoom change.
     * If <code>oldZoom</code> and <code>newZoom</code> are the affine
     * transform before and after the change respectively, then the
     * following relation must hold (within the limits of rounding error):
     *
     * <code>newZoom=oldZoom.{@link AffineTransform#concatenate concatenate}
     * (change)</code>
     */
    private final AffineTransform change;

    /**
     * Constructs a new event.
     *
     * @param source The event source
     *               (usually a {@link org.geotools.gui.swing.ZoomPane}).
     * @param change An affine transform indicating the zoom change.
     *               If <code>oldZoom</code> and <code>newZoom</code> are the
     *               affine transform before and after the change respectively,
     *               then the following relation must hold (within the limits
     *               of rounding error):
     *
     * <code>newZoom=oldZoom.{@link AffineTransform#concatenate concatenate}
     * (change)</code>
     */
    public ZoomChangeEvent(final Object source, final AffineTransform change) {
        super(source);
        this.change = change;
    }

    /**
     * Returns the affine transform indicating the zoom change.
     * Note: for performance reasons, this method does not clone
     * the returned transform. Do not change!
     */
    public AffineTransform getChange() {
        return change;
    }
}
