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

// J2SE dependencies
import java.awt.image.RenderedImage;

// Geotools dependencies
import org.geotools.pt.Envelope;
import org.geotools.gc.GridRange;
import org.geotools.gc.GridGeometry;
import org.geotools.gc.GridCoverage;
import org.geotools.ct.MathTransform;
import org.geotools.gc.InvalidGridGeometryException;


/**
 * A set of utilities methods for the Grid Coverage package. Those methods are not really
 * rigorous; must of them should be seen as temporary implementations.
 *
 * @version $Id: GCSUtilities.java,v 1.3 2003/02/18 19:28:38 desruisseaux Exp $
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
     * Cast the specified grid range into an envelope. This is sometime used before to transform
     * the envelope using {@link CTSUtilities#transform(MathTransform, Envelope)}.
     */
    public static Envelope toEnvelope(final GridRange gridRange) {
        final int dimension = gridRange.getDimension();
        final double[] lower = new double[dimension];
        final double[] upper = new double[dimension];
        for (int i=0; i<dimension; i++) {
            lower[i] = gridRange.getLower(i);
            upper[i] = gridRange.getUpper(i);
        }
        return new Envelope(lower, upper);
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

    /**
     * Returns <code>true</code> if the specified grid coverage or any of its source
     * uses the following image.
     */
    public static boolean uses(final GridCoverage coverage, final RenderedImage image) {
        if (coverage != null) {
            if (coverage.getRenderedImage() == image) {
                return true;
            }
            final GridCoverage[] sources = coverage.getSources();
            if (sources != null) {
                for (int i=0; i<sources.length; i++) {
                    if (uses(sources[i], image)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
