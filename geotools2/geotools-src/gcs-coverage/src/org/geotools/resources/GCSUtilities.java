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
import org.geotools.ct.MathTransform1D;
import org.geotools.cv.SampleDimension;
import org.geotools.gc.InvalidGridGeometryException;


/**
 * A set of utilities methods for the Grid Coverage package. Those methods are not really
 * rigorous; must of them should be seen as temporary implementations.
 *
 * @version $Id: GCSUtilities.java,v 1.9 2003/08/03 20:15:04 desruisseaux Exp $
 * @author Martin Desruisseaux
 */
public final class GCSUtilities {
    /**
     * Do not allows instantiation of this class.
     */
    private GCSUtilities() {
    }

    ///////////////////////////////////////////////////////////////////
    ////////                                                   ////////
    ////////        GridGeometry / GridRange / Envelope        ////////
    ////////                                                   ////////
    ///////////////////////////////////////////////////////////////////

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
     * has been transformed using {@link CTSUtilities#transform(MathTransform, Envelope)}. The
     * floating point values are rounded toward the nearest integer.
     * <br><br>
     * <strong>Note about conversion of floating point values to integers:</strong><br>
     * In previous versions, we used {@link Math#floor} and {@link Math#ceil} in order to
     * make sure that the grid range encompass all the envelope (something similar to what
     * <cite>Java2D</cite> does when casting {@link Rectangle2D} to {@link Rectangle}).
     * But it had the undesirable effect of changing image width. For example the range
     * <code>[-0.25  99.75]</code> were changed to <code>[-1  100]</code>, which is not
     * what the {@link javax.media.jai.operator.AffineDescriptor Affine} operation expects
     * for instance. Rounding to nearest integer produces better results. Note that the
     * rounding mode do not alter the significiance of the &quot;Resample&quot; operation,
     * since this operation will respect the &quot;grid to coordinate system&quot; transform
     * no matter what the grid range is.
     */
    public static GridRange toGridRange(final Envelope envelope) {
        final int dimension = envelope.getDimension();
        final int[] lower = new int[dimension];
        final int[] upper = new int[dimension];
        for (int i=0; i<dimension; i++) {
            // See "note about conversion of floating point values to integers" in the JavaDoc.
            lower[i] = (int)Math.round(envelope.getMinimum(i));
            upper[i] = (int)Math.round(envelope.getMaximum(i));
        }
        return new GridRange(lower, upper);
    }




    //////////////////////////////////////////////////////////////////////
    ////////                                                      ////////
    ////////    GridCoverage / SampleDimension / RenderedImage    ////////
    ////////                                                      ////////
    //////////////////////////////////////////////////////////////////////

    /**
     * Returns <code>true</code> if at least one of the specified sample dimensions has a
     * {@linkplain SampleDimension#getSampleToGeophysics sample to geophysics} transform
     * which is not the identity transform.
     */
    public static boolean hasTransform(final SampleDimension[] sampleDimensions) {
        for (int i=sampleDimensions.length; --i>=0;) {
            MathTransform1D tr = sampleDimensions[i].geophysics(false).getSampleToGeophysics();
            if (tr!=null && !tr.isIdentity()) {
                return true;
            }
        }
        return false;
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

    /**
     * Returns the visible band in the specified image. This method fetch the "GC_VisibleBand"
     * property. If this property is undefined, then the visible band default to the first one.
     *
     * @param  image The image for which to fetch the visible band.
     * @return The visible band.
     */
    public static int getVisibleBand(final RenderedImage image) {
        final Object candidate = image.getProperty("GC_VisibleBand");
        if (candidate instanceof Integer) {
            return ((Integer) candidate).intValue();
        }
        return 0;
    }
}
