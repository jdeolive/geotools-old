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
package org.geotools.cv;

// J2SE dependencies
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.image.ColorModel;
import java.awt.image.SampleModel;
import java.awt.image.RenderedImage;
import java.awt.image.WritableRaster;
import java.awt.image.IndexColorModel;
import java.awt.image.ComponentSampleModel;
import java.awt.image.RasterFormatException;
import java.awt.image.PixelInterleavedSampleModel;
import java.awt.image.renderable.ParameterBlock;

// JAI dependencies
import javax.media.jai.JAI;
import javax.media.jai.TileCache;
import javax.media.jai.ImageLayout;
import javax.media.jai.PlanarImage;
import javax.media.jai.NullOpImage;
import javax.media.jai.PointOpImage;
import javax.media.jai.iterator.RectIter;
import javax.media.jai.iterator.WritableRectIter;
import javax.media.jai.iterator.RectIterFactory;

// Geotools dependencies
import org.geotools.ct.MathTransform1D;
import org.geotools.ct.TransformException;
import org.geotools.resources.DualRectIter;
import org.geotools.resources.ImageUtilities;


/**
 * An image that contains transformed pixels. It may be sample values after their
 * transformation in geophyics values, or the converse.  Images are created using
 * the {@link #getInstance} method, which is invoked by <code>SampleDimension.CRIF</code>.
 * "CRIF" stands for {@link java.awt.image.renderable.ContextualRenderedImageFactory}.
 * The image operation name is "GC_SampleTranscoding".
 *
 * @version $Id: ImageAdapter.java,v 1.8 2002/08/12 10:13:46 desruisseaux Exp $
 * @author Martin Desruisseaux
 */
final class ImageAdapter extends PointOpImage {
    /**
     * Ensemble des catégories qui donnent une
     * signification aux pixels de l'image. La
     * longueur de ce tableau doit correspondre
     * au nombre de bandes de l'image source.
     */
    private final CategoryList[] categories;

    /**
     * Transform an image.
     *
     * @task HACK: This method provides an optimisation for the case of a strictly linear
     *             transformation: it use the JAI's "Rescale" operation, which is hardware
     *             accelerated. Unfortunatly, bug #4726416 prevent us to use this optimisation
     *             here. The optimisation is temporarly disabled, waiting for Sun to fix the
     *             bug.
     *
     * @task TODO: An other possible optimization is to skip "Null" operations.
     *             We could very well imagine the following scenario:
     *
     *             <pre>ImageAdapter  -->  NullOpImage  -->  ImageAdapter</pre>
     *
     *             The <code>NullOpImage</code> between the two <code>ImageAdapter</code> prevents
     *             some optimization, like transforming two inverse <code>ImageAdapter</code> into
     *             an identity operation. R  emoving the <code>NullOpImage</code> would make those
     *             optimization possible.   Unfortunatly, sometime the <code>NullOpImage</code> is
     *             not really "null", but change the <code>IndexColorModel</code>.   Removing this
     *             operation remove also the colormap, which is not what we want. A better solution
     *             need to be find. We should probably use <code>ColormapOpImage</code> instead of
     *             <code>NullOpImage</code> for that.
     *
     * @param  image      The source image, or <code>null</code>.
     * @param  categories The category lists, one for each image's band.
     * @param  jai        The instance of {@link JAI} to use.
     * @return The transformed image.
     */
    public static RenderedImage getInstance(RenderedImage image,
                                            final CategoryList[] categories,
                                            final JAI jai)
    {
        if (image==null) {
            return null;
        }
        /*
         * Slight optimisation: Skip the "Null" operations.
         * Such image may be the result of a "Colormap" operation.
         */
        if (false) {
            // TODO: Current optimization is not appropriate: it loose the ColorModel!
            //       We need a way to perform this optimization while preserving colors.
            while (image instanceof NullOpImage) {
                final NullOpImage op = (NullOpImage) image;
                if (op.getNumSources() != 1) {
                    break;
                }
                image = op.getSourceImage(0);
            }
        }
        /*
         * If this operation is the inverse of a previous operation,
         * returns the source image instead of recomputing the original data.
         */
        if (image instanceof ImageAdapter) {
            final ImageAdapter other = (ImageAdapter) image;
            if (categories.length == other.categories.length) {
                boolean valid = true;
                for (int i=0; i<categories.length; i++) {
                    if (!categories[i].equals(other.categories[i].inverse)) {
                        valid = false;
                        break;
                    }
                }
                if (valid) {
                    return other.getSourceImage(0);
                }
            }
        }
        /*
         * The image must be transcoded. Check if the transcoding operation is only a
         * linear transformation.   If yes, we can use the JAI's "Rescale" operation,
         * which is hardware accelerated.
         */
        try {
            boolean valid = true;
            boolean identity = true;
            final double[] scale  = new double[categories.length];
            final double[] offset = new double[categories.length];
            for (int i=0; i<categories.length; i++) {
                final CategoryList c = categories[i];
                if (c.size()==1) {
                    final MathTransform1D transform = ((Category) c.get(0)).transform;
                    scale[i]  = transform.derivative(Double.NaN);
                    offset[i] = transform.transform(0);
                    if (!Double.isNaN(scale[i]) && !Double.isNaN(offset[i])) {
                        identity &= (scale[i]==1 && offset[i]==0);
                        continue;
                    }
                }
                valid = false;
                break;
            }
            if (valid) {
                if (identity) {
                    return image;
                }
                if (false) { // TODO: Here is the optimisation that we would like to enable.
                    final ParameterBlock param;
                    final RenderingHints hints;
                    param = new ParameterBlock().addSource(image).add(scale).add(offset);
                    hints = new RenderingHints(JAI.KEY_IMAGE_LAYOUT, getLayout(image, categories));
                    return jai.createNS("Rescale", param, hints);
                }
            }
        }
        catch (TransformException exception) {
            // At least one band don't use a linear relation.
            // Ignore the exception and fallback on the general case.
        }
        return new ImageAdapter(image, categories, jai);
    }
    
    /**
     * Construct a new <code>ImageAdapter</code>.
     *
     * @param image      The source image.
     * @param categories The category lists, one for each image's band.
     * @param jai        Instance of JAI to use.
     */
    private ImageAdapter(final RenderedImage image, final CategoryList[] categories, final JAI jai)
    {
        super(image, getLayout(image, categories), jai.getRenderingHints(), false);
        this.categories = categories;
        if (categories.length != image.getSampleModel().getNumBands()) {
            // Should not happen, since SampleDimension$Descriptor has already checked it.
            throw new RasterFormatException(String.valueOf(categories.length));
        }
        permitInPlaceOperation();
    }
    
    /**
     * Returns the destination image layout.
     *
     * @param  image The source image.
     * @param  categories The list of category.
     * @return Layout for the destination image.
     *
     * @task TODO: IndexColorModel seems to badly choose his sample model. As of JDK 1.4-rc1, it
     *             construct a ComponentSampleModel, which is drawn very slowly to the screen. A
     *             much faster sample model is PixelInterleavedSampleModel,  which is the sample
     *             model used by BufferedImage for TYPE_BYTE_INDEXED. We should check if this is
     *             fixed in future J2SE release.
     */
    private static ImageLayout getLayout(final RenderedImage       image,
                                         final CategoryList[] categories)
    {
        final int     band = 0; // The visible band.
        CategoryList categ = categories[band].inverse;
        ImageLayout layout = ImageUtilities.getImageLayout(image);
        ColorModel  colors = categ.getColorModel(band, image.getSampleModel().getNumBands());
        SampleModel  model = colors.createCompatibleSampleModel(image.getWidth(), image.getHeight());
        if (colors instanceof IndexColorModel && model.getClass().equals(ComponentSampleModel.class))
        {
            // TODO: IndexColorModel seems to badly choose his sample model. As of JDK 1.4-rc1, it
            //       construct a ComponentSampleModel, which is drawn very slowly to the screen. A
            //       much faster sample model is PixelInterleavedSampleModel, which is the sample
            //       model used by BufferedImage for TYPE_BYTE_INDEXED. Java2D seems to be optimized
            //       for this sample model when used with IndexColorModel.
            final int w = model.getWidth();
            final int h = model.getHeight();
            model = new PixelInterleavedSampleModel(colors.getTransferType(), w,h,1,w, new int[1]);
        }
        return layout.setSampleModel(model).setColorModel(colors);
    }
    
    /**
     * Compute one of the destination image tile.
     *
     * @task TODO: There is two optimisations we could do here:
     *
     *             1) If source and destination are the same raster, then a single
     *                {@link WritableRectIter} object would be more efficient (the
     *                hard work is to detect if source and destination are the same).
     *             2) If the destination image is a single-banded, non-interleaved
     *                sample model, we could apply the transform directly in the
     *                {@link java.awt.image.DataBuffer}. We can even avoid to copy
     *                sample value if source and destination raster are the same.
     *
     * @param sources  An array of length 1 with source image.
     * @param dest     The destination tile.
     * @param destRect the rectangle within the destination to be written.
     */
    protected void computeRect(final PlanarImage[] sources,
                               final WritableRaster   dest,
                               final Rectangle    destRect)
    {
        final PlanarImage source = sources[0];
        WritableRectIter iterator = RectIterFactory.createWritable(dest, destRect);
        if (true) {
            // TODO: Detect if source and destination rasters are the same. If they are
            //       the same, we should skip this block. Iteration will then be faster.
            iterator = DualRectIter.create(RectIterFactory.create(source, destRect), iterator);
        }
        int band=0;
        if (!iterator.finishedBands()) do {
            categories[band].transform(iterator);
            band++;
        }
        while (!iterator.nextBandDone());
        assert(band == categories.length) : band;
    }
}
