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
package org.geotools.gp.jai;

// J2SE dependencies
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.image.RenderedImage;
import java.awt.image.WritableRaster;
import java.awt.image.renderable.ParameterBlock;

// JAI dependencies
import javax.media.jai.JAI;
import javax.media.jai.ImageLayout;
import javax.media.jai.PlanarImage;
import javax.media.jai.PointOpImage;
import javax.media.jai.iterator.RectIter;
import javax.media.jai.iterator.RectIterFactory;
import javax.media.jai.iterator.WritableRectIter;


/**
 * Linear combinaison of two images (<CODE>src0</CODE> and <CODE>src1</CODE>). The
 * parameters <CODE>weight0</CODE> and <CODE>weight1</CODE> indicate the weight of
 * source images <CODE>src0</CODE> and <CODE>src1</CODE>. If we consider pixel at
 * coordinate (<var>x</var>,<var>y</var>), its value is determinate by the pseudo-code:
 *
 * <blockquote><pre>
 * value = src0[x][y]*weight0 + src1[x][y]*weight1 + offset
 * </pre></blockquote>
 *
 * @version $Id: Combine.java,v 1.1 2003/07/11 16:57:48 desruisseaux Exp $
 * @author Remi Eve
 */
public class Combine extends PointOpImage {
    /**
     * The operation name.
     */
    public static final String OPERATION_NAME = "org.geotools.Combine";
    
    /**
     * The weight of image <code>src0</code> for each bands.
     */
    private final double[] weights0;
    
    /**
     * The weight of image <code>src1</code> for each bands.
     */
    private final double[] weights1;

    /**
     * The offset for each bands.
     */
    private final double[] offsets;
    
    /**
     * Construct a new instance of <code>CoverageCombine</code>.
     *
     * @param src0     First source image.
     * @param src1     Second source image.
     * @param weights0 The weight of image src0 for each bands.
     * @param weights1 The weight of image src1 for each bands.
     * @param offsets  The offsets for each bands.
     * @param hints    The rendering hints.
     */
    public Combine(final RenderedImage   src0,
                   final RenderedImage   src1,
                   final double[]    weights0,
                   final double[]    weights1,
                   final double[]     offsets,
                   final RenderingHints hints)
    {
        super(src0, src1, (ImageLayout)hints.get(JAI.KEY_IMAGE_LAYOUT), hints, false);
        permitInPlaceOperation();
        this.weights0 = weights0;
        this.weights1 = weights1;
        this.offsets  = offsets;
    }
    
    /**
     * Compute the interpolation between two tiles.
     *
     * @param sources   An array of PlanarImage sources.
     * @param dest      A WritableRaster to be filled in.
     * @param destRect  The Rectangle within the destination to be written.
     */
    public void computeRect(final PlanarImage[]  sources,
                            final WritableRaster dest,
                            final Rectangle      destRect)
    {
        int band = 0;
        final         RectIter iSrc0   = RectIterFactory.create(sources[0],   destRect);
        final         RectIter iSrc1   = RectIterFactory.create(sources[1],   destRect);
        final WritableRectIter iTarget = RectIterFactory.createWritable(dest, destRect);
        iSrc0  .startBands();
        iSrc1  .startBands();
        iTarget.startBands();
        if (!iTarget.finishedBands() &&
            !iSrc0  .finishedBands() &&
            !iSrc1  .finishedBands())
        {
            final double weight0 = weights0[Math.min(band, weights0.length-1)];
            final double weight1 = weights1[Math.min(band, weights1.length-1)];
            final double  offset =  offsets[Math.min(band,  offsets.length-1)];
            do {
                iSrc0  .startLines();
                iSrc1  .startLines();
                iTarget.startLines();
                if (!iTarget.finishedLines() &&
                    !iSrc0  .finishedLines() &&
                    !iSrc1  .finishedLines())
                {
                    do {
                        iSrc0  .startPixels();
                        iSrc1  .startPixels();
                        iTarget.startPixels();
                        if (!iTarget.finishedPixels() &&
                            !iSrc0  .finishedPixels() &&
                            !iSrc1  .finishedPixels())
                        {
                            do {
                                iTarget.setSample(iSrc0.getSampleDouble() * weight0 +
                                                  iSrc1.getSampleDouble() * weight1 + offset);
                            } while (!iSrc0.nextPixelDone() &&
                                     !iSrc1.nextPixelDone() &&
                                   !iTarget.nextPixelDone());
                        }
                    } while (!iSrc0.nextLineDone() &&
                             !iSrc1.nextLineDone() &&
                           !iTarget.nextLineDone());
                }
                band++;
            } while (!iSrc0.nextBandDone() &&
                     !iSrc1.nextBandDone() &&
                   !iTarget.nextBandDone());
        }
    }
}
