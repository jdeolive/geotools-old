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
 */
package org.geotools.resources;

// Geotools dependencies
import org.geotools.pt.Envelope;
import org.geotools.gc.GridRange;
import org.geotools.gc.GridGeometry;
import org.geotools.ct.MathTransform;
import org.geotools.gc.InvalidGridGeometryException;


/**
 * A set of utilities methods for the Grid Coverage package. Those methods are not really
 * rigorous; must of them should be seen as temporary implementations.
 *
 * @version $Id: GCSUtilities.java,v 1.1 2003/02/13 22:59:51 desruisseaux Exp $
 * @author Martin Desruisseaux
 */
public final class GCSUtilities {
    /**
     * Do not allows instantiation of this class.
     */
    private GCSUtilities() {
    }

    /**
     * Returns <code>true</code> if the specified geometry has a valid grid range.
     */
    public static boolean hasGridRange(final GridGeometry geometry) {
        if (geometry != null) try {
            geometry.getGridRange();
            return true;
        } catch (InvalidGridGeometryException exception) {
            // Ignore.
        }
        return false;
    }

    /**
     * Returns <code>true</code> if the specified geometry
     * has a valid "grid to coordinate system" transform.
     */
    public static boolean hasTransform(final GridGeometry geometry) {
        if (geometry != null) try {
            geometry.getGridToCoordinateSystem();
            return true;
        } catch (InvalidGridGeometryException exception) {
            // Ignore.
        }
        return false;
    }

    /**
     * Cast the specified envelope into a grid range. This is sometime used after the envelope
     * has been transformed using {@link CTSUtilities#transform(MathTransform, Envelope)}.
     */
    public static GridRange toGridRange(final Envelope envelope) {
        final int dimension = envelope.getDimension();
        final int[] lower = new int[dimension];
        final int[] upper = new int[dimension];
        for (int i=0; i<dimension; i++) {
            lower[i] = (int)Math.floor(envelope.getMinimum(i));
            upper[i] = (int)Math.ceil (envelope.getMaximum(i));
        }
        return new GridRange(lower, upper);
    }
}
