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
import java.awt.Rectangle;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.RasterFormatException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.LogRecord;

// Geotools dependencies
import org.geotools.ct.MathTransform2D;
import org.geotools.ct.TransformException;
import org.geotools.ct.proj.PointOutsideEnvelopeException;
import org.geotools.resources.Utilities;
import org.geotools.resources.CTSUtilities;
import org.geotools.resources.gcs.Resources;
import org.geotools.resources.gcs.ResourceKeys;


/**
 * An image warp using {@link MathTransform2D}.
 *
 * @version $Id: WarpTransform.java,v 1.5 2003/05/12 21:29:31 desruisseaux Exp $
 * @author Martin Desruisseaux
 */
final class WarpTransform extends Warp {
    /**
     * The coverage name. Used for formatting error message.
     */
    private final String name;

    /**
     * The <strong>inverse</strong> of the transform to apply.
     * This transform maps destination pixels to source pixels.
     */
    private final MathTransform2D inverse;
    
    /**
     * Construct a new <code>WarpTransform</code>.
     *
     * @param name    The coverage name. Used for formatting error message.
     * @param inverse The <strong>inverse</strong> of the transformation
     *                to apply from source to target image.
     */
    public WarpTransform(final String name, final MathTransform2D inverse) {
        this.name    = name;
        this.inverse = inverse;
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
            // At least one transformation failed. In Geotools MapProjection
            // implementation, unprojected coordinates are set to (NaN,NaN).
            RasterFormatException e = new RasterFormatException(Resources.format(
                            ResourceKeys.ERROR_CANT_REPROJECT_$1, name));
            e.initCause(exception);
            throw e;
        }
        return destRect;
    }

    /**
     * Computes a rectangle that is guaranteed to enclose the region of the source
     * that is required in order to produce a given rectangular output region.
     */
    public Rectangle mapDestRect(final Rectangle destRect) {
        try {
            // According OpenGIS specification, GridGeometry maps pixel's center. But
            // the bounding box is for all pixels, not pixel's centers. Offset by
            // -0.5 (use -0.5 for maximum too, not +0.5, since maximum is exclusive).
            Rectangle2D bounds = new Rectangle2D.Double(
                    destRect.x-0.5, destRect.y-0.5, destRect.width, destRect.height);
            // TODO: This rectangle may be approximative. We should improve the algorithm.
            bounds = CTSUtilities.transform(inverse, bounds, bounds);
            return bounds.getBounds();
        } catch (TransformException exception) {
            IllegalArgumentException e = new IllegalArgumentException(Resources.format(
                            ResourceKeys.ERROR_BAD_PARAMETER_$2, "destRect", destRect));
            e.initCause(exception);
            throw e;
        }
    }

    /**
     * Computes a rectangle that is guaranteed to enclose the region of the destination
     * that can potentially be affected by the pixels of a rectangle of a given source.
     */
    public Rectangle mapSourceRect(final Rectangle sourceRect) {
        try {
            // According OpenGIS specification, GridGeometry maps pixel's center. But
            // the bounding box is for all pixels, not pixel's centers. Offset by
            // -0.5 (use -0.5 for maximum too, not +0.5, since maximum is exclusive).
            Rectangle2D bounds = new Rectangle2D.Double(
                    sourceRect.x-0.5, sourceRect.y-0.5, sourceRect.width, sourceRect.height);
            // TODO: This rectangle may be approximative. We should improve the algorithm.
            bounds = CTSUtilities.transform((MathTransform2D)inverse.inverse(), bounds, bounds);
            return bounds.getBounds();
        } catch (TransformException exception) {
            IllegalArgumentException e = new IllegalArgumentException(Resources.format(
                            ResourceKeys.ERROR_BAD_PARAMETER_$2, "sourceRect", sourceRect));
            e.initCause(exception);
            throw e;
        }
    }

    /**
     * Computes the source point corresponding to the supplied point.
     *
     * @param destPt The position in destination image coordinates
     *               to map to source image coordinates.
     */
    public Point2D mapDestPoint(final Point2D destPt) {
        try {
            return inverse.transform(destPt, null);
        } catch (TransformException exception) {
            IllegalArgumentException e = new IllegalArgumentException(Resources.format(
                            ResourceKeys.ERROR_BAD_PARAMETER_$2, "destPt", destPt));
            e.initCause(exception);
            throw e;
        }
    }

    /**
     * Computes the destination point corresponding to the supplied point.
     *
     * @param sourcePt The position in source image coordinates
     *                 to map to destination image coordinates.
     */
    public Point2D mapSourcePoint(final Point2D sourcePt) {
        try {
            return ((MathTransform2D)inverse.inverse()).transform(sourcePt, null);
        } catch (TransformException exception) {
            IllegalArgumentException e = new IllegalArgumentException(Resources.format(
                            ResourceKeys.ERROR_BAD_PARAMETER_$2, "sourcePt", sourcePt));
            e.initCause(exception);
            throw e;
        }
    }
}
