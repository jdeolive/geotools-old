/*
 * Geotools 2 - OpenSource mapping toolkit
 * (C) 2003, Geotools Project Managment Committee (PMC)
 * (C) 2003, 2ie Technologie
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
package org.geotools.gp.jai;

// J2SE dependencies
import java.util.Map;
import java.awt.Rectangle;
import java.awt.image.RenderedImage;
import java.awt.image.WritableRaster;

// JAI dependencies
import javax.media.jai.AreaOpImage;
import javax.media.jai.ImageLayout;
import javax.media.jai.PlanarImage;
import javax.media.jai.iterator.RandomIter;
import javax.media.jai.iterator.RandomIterFactory;

// Geotools dependencies
import org.geotools.resources.XMath;


/**
 * Replace {@link Double#NaN} values by the weighted average of neighbors values.
 * This operation use a box of <code>size</code>&times<code>size</code> pixels
 * centered on each <code>NaN</code> value. The weighted average is then computed,
 * ignoring all <code>NaN</code> values. If the number of valid values is greater
 * than <code>validityThreshold</code>, then the center <code>NaN</code> is replaced
 * by the computed average. Otherwise, the <code>NaN</code> value is left unchanged.
 * 
 * @version $Id: NodataFilter.java,v 1.1 2003/07/18 13:49:56 desruisseaux Exp $
 * @author Lionel Flahaut
 * @author Martin Desruisseaux
 */
public class NodataFilter extends AreaOpImage {
    /**
     * Shared instance of {@link #distances} for the common case where <code>padding==1</code>.
     */
    private static double[] sharedDistances;

    /**
     * Pre-computed distances. Used in order to avoid a huge amount of calls to
     * {@link Math#sqrt} in {@link #computeRect}.
     */
    private final double[] distances;

    /**
     * The minimal number of valid neighbors required in order to consider the average as valid.
     */
    private final int validityThreshold;

    /**
     * Construct a new operation.
     *
     * @param source   The source image.
     * @param layout   The image layout.
     * @param map      The image properties and rendering hints.
     * @param padding  The number of source above, below, to the left and to the right of central
     *                 <code>NaN</code> pixel. The full box size is <code>padding</code>&times;2.
     * @param validityThreshold The minimal number of valid neighbors required in order to consider
     *                the average as valid.
     */
    protected NodataFilter(final RenderedImage source,
                           final ImageLayout   layout,
                           final Map           map,
                           final int           padding,
                           final int validityThreshold)
    {
        super(source, layout, map, false, null, padding, padding, padding, padding);
        this.validityThreshold = validityThreshold;
        /*
         * Compute the array of distances once for ever. For the special case where
         * <code>padding</code> equals 1, we will try to reuse the same array for
         * all <code>NodataFilter</code> instances.
         */
        if (padding==1 && sharedDistances!=null) {
            distances = sharedDistances;
        } else {
            distances = new double[(leftPadding+rightPadding+1) * (topPadding+bottomPadding+1)];
            int index = 0;
            for (int dy=-topPadding; dy<=bottomPadding; dy++) {
                for (int dx=-leftPadding; dx<=rightPadding; dx++) {
                    distances[index++] = Math.sqrt(dx*dx + dy*dy);
                }
            }
            assert index == distances.length;
            if (padding == 1) {
                sharedDistances = distances;
            }
        }
    }

    /**
     * Compute a rectangle of outputs.
     */
    protected void computeRect(final PlanarImage[] sources,
                               final WritableRaster   dest,
                               final Rectangle    destRect)
    {
        assert sources.length == 1;
        final PlanarImage source = sources[0];
        Rectangle sourceRect = mapDestRect(destRect, 0);
        sourceRect = sourceRect.intersection(source.getBounds());
        final RandomIter iter = RandomIterFactory.create(source, sourceRect);
        final int minX = destRect.x;                 // Minimum inclusive
        final int minY = destRect.y;                 // Minimum inclusive
        final int maxX = destRect.width  + minX;     // Maximum exclusive
        final int maxY = destRect.height + minY;     // Maximum exclusive
        final int hPad = leftPadding+rightPadding+1; // Horizontal padding
        for (int band=source.getNumBands(); --band>=0;) {
            for (int y=minY; y<maxY; y++) {
                final int minScanY = Math.max(minY, y -    topPadding   ); // Inclusive
                final int maxScanY = Math.min(maxY, y + bottomPadding +1); // Exclusive
                final int minScanI = (minScanY - (y-topPadding)) * hPad;
                assert minScanI>=0 && minScanI<=distances.length : minScanI;
                for (int x=minX; x<maxX; x++) {
                    final double current = iter.getSampleDouble(x, y, band);
                    if (!Double.isNaN(current)) {
                        /*
                         * Pixel is already valid: no operation here.
                         */
                        dest.setSample(x, y, band, current);
                        continue;
                    }
                    /*
                     * Compute the average and set the value if the amount of
                     * valid pixels is at least equals to the threshold amount.
                     */
                    int       count       = 0; // Number of valid values.
                    double    sumValue    = 0; // Weighted sum of values.
                    double    sumDistance = 0; // Sum of distances of valid values.
                    final int minScanX    = Math.max(minX, x -  leftPadding   ); // Inclusive
                    final int maxScanX    = Math.min(maxX, x + rightPadding +1); // Exclusive
                    final int lineOffset  = hPad - (maxScanX-minScanX);
                    int index = minScanI + (minScanX - (x-leftPadding));
                    for (int sy=minScanY; sy<maxScanY; sy++) {
                        for (int sx=minScanX; sx<maxScanX; sx++) {
                            final double scan = iter.getSampleDouble(sx, sy, band);
                            if (!Double.isNaN(scan)) {
                                final double distance = distances[index];
                                assert (Math.abs(distance-XMath.hypot(sx-x, sy-y)) < 1E-6) &&
                                       (distance > 0) : distance;
                                sumValue    += distance*scan;
                                sumDistance += distance;
                                count++;
                            }
                            index++;
                        }
                        index += lineOffset;
                    }
                    dest.setSample(x, y, band,
                                   (count>=validityThreshold) ? sumValue/sumDistance : current);
                }
            }
        }
        iter.done();
    }
}
