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
 *
 *    This package contains documentation from OpenGIS specifications.
 *    OpenGIS consortium's work is fully acknowledged here.
 */
package org.geotools.gp;

// J2SE and JAI dependencies
import javax.media.jai.Warp;
import java.awt.geom.Rectangle2D;
import java.awt.geom.AffineTransform;

// Geotools dependencies
import org.geotools.gc.GridGeometry;
import org.geotools.ct.MathTransform;
import org.geotools.ct.MathTransform2D;
import org.geotools.ct.TransformException;
import org.geotools.ct.MathTransformFactory;
import org.geotools.ct.NoninvertibleTransformException;
import org.geotools.resources.Utilities;


/**
 * An image warp using {@link MathTransform2D}.
 *
 * @version 1.0
 * @author Martin Desruisseaux
 */
final class WarpTransform extends Warp {
    /**
     * The <strong>inverse</strong> of the transform to apply.
     * This transform maps destination pixels to source pixels.
     */
    private final MathTransform2D inverse;
    
    /**
     * Construct a new <code>WarpTransform</code> with an
     * identity transform. This is a temporary placeholder
     * during the creation of {@link Resampler} object.
     *
     * @param factory The {@link MathTransformFactory} to use.
     */
    public WarpTransform(final MathTransformFactory factory) {
        inverse = factory.createAffineTransform(new AffineTransform());
    }
    
    /**
     * Construct a new <code>WarpTransform</code>.
     *
     * @param  source The source image's {@link GridGeometry}.
     * @param  transform The transformation to apply from source to target image.
     * @param  target The target image's {@link GridGeometry}.
     * @param  factory The {@link MathTransformFactory} to use.
     * @throws NoninvertibleTransformException if a transform can't be inversed.
     */
    public WarpTransform(final GridGeometry         source,
                         final MathTransform2D      transform,
                         final GridGeometry         target,
                         final MathTransformFactory factory)
        throws NoninvertibleTransformException
    {
        final MathTransform step1 = target.getGridToCoordinateSystem2D();
        final MathTransform step2 = transform.inverse();
        final MathTransform step3 = source.getGridToCoordinateSystem2D().inverse();
        inverse = (MathTransform2D) factory.createConcatenatedTransform(step1,
        factory.createConcatenatedTransform(step2, step3));
    }
    
    /**
     * Computes the source pixel positions for a given rectangular
     * destination region, subsampled with an integral period.
     */
    public float[] warpSparseRect(final int xmin,    final int ymin,
                                  final int width,   final int height,
                                  final int periodX, final int periodY, float[] destRect)
    {
        if (periodX<1) throw new IllegalArgumentException(String.valueOf(periodX));
        if (periodY<1) throw new IllegalArgumentException(String.valueOf(periodY));
        
        final int xmax  = xmin+width;
        final int ymax  = ymin+height;
        final int count = ((width+(periodX-1))/periodX) * ((height+(periodY-1))/periodY);
        if (destRect==null) {
            destRect = new float[2*count];
        }
        int index = 0;
        for (int y=ymin; y<ymax; y+=periodY) {
            for (int x=xmin; x<xmax; x+=periodX) {
                destRect[index++] = x;
                destRect[index++] = y;
            }
        }
        try {
            inverse.transform(destRect, 0, destRect, 0, count);
        } catch (TransformException exception) {
            // At least one transformation failed. In current org.geotools.ct.MapProjection
            // implementation, all unprojected points have coordinates (NaN,NaN).
            Utilities.unexpectedException("org.geotools.gcs", "WarpTransform", "warpSparseRect", exception);
        }
        return destRect;
    }
}
