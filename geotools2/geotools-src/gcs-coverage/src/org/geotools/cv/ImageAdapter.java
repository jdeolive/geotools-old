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
import java.awt.image.ColorModel;
import java.awt.image.SampleModel;
import java.awt.image.RenderedImage;
import java.awt.image.WritableRaster;
import java.awt.image.IndexColorModel;
import java.awt.image.ComponentSampleModel;
import java.awt.image.RasterFormatException;
import java.awt.image.PixelInterleavedSampleModel;

// JAI dependencies
import javax.media.jai.JAI;
import javax.media.jai.TileCache;
import javax.media.jai.ImageLayout;
import javax.media.jai.PlanarImage;
import javax.media.jai.NullOpImage;
import javax.media.jai.PointOpImage;
import javax.media.jai.RasterFactory;
import javax.media.jai.iterator.RectIter;
import javax.media.jai.iterator.WritableRectIter;
import javax.media.jai.iterator.RectIterFactory;

// Geotools dependencies
import org.geotools.resources.DualRectIter;
import org.geotools.resources.ImageUtilities;


/**
 * An image that contains transformed pixels. It may be sample values after their
 * transformation in geophyics values, or the converse.
 *
 * @version $Id: ImageAdapter.java,v 1.3 2002/07/23 17:53:36 desruisseaux Exp $
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
     * @param  image      The source image, or <code>null</code>.
     * @param  categories The category lists, one for each image's band.
     * @return The transformed image.
     */
    public static RenderedImage getInstance(RenderedImage image, final CategoryList[] categories) {
        if (image==null) {
            return null;
        }
        while (image instanceof NullOpImage) {
            // Optimization for images that doesn't change
            // pixel value. Such an image may be the result
            // of a "Colormap" operation.
            final NullOpImage op = (NullOpImage) image;
            if (op.getNumSources() != 1) {
                break;
            }
            image = op.getSourceImage(0);
        }
        if (image instanceof ImageAdapter) {
            final ImageAdapter other = (ImageAdapter) image;
            if (categories.length == other.categories.length) {
                for (int i=0; i<categories.length; i++) {
                    if (!categories[i].equals(other.categories[i].inverse())) {
                        // If enter here, we are not undoing a previous ImageAdapter.
                        return new ImageAdapter(image, categories);
                    }
                }
                return other.getSourceImage(0);
            }
        }
        return new ImageAdapter(image, categories);
    }
    
    /**
     * Construct a new <code>ImageAdapter</code>.
     *
     * @param image      The source image.
     * @param categories The category lists, one for each image's band.
     */
    private ImageAdapter(final RenderedImage image,
                         final CategoryList[] categories)
    {
        super(image, getLayout(image, (CategoryList) categories[0].inverse()),
              JAI.getDefaultInstance().getRenderingHints(), false);
        this.categories = categories;
        final int numBands = image.getSampleModel().getNumBands();
        if (categories.length != numBands) {
            throw new RasterFormatException(String.valueOf(categories.length)+"!="+numBands);
        }
        permitInPlaceOperation();
    }
    
    /**
     * Returns the destination image layout.
     *
     * @param  image The source image.
     * @param  categories The destination category list.
     * @return Layout for the destination image.
     *
     * @task TODO: IndexColorModel seems to badly choose his sample model. As of JDK 1.4-rc1, it
     *             construct a ComponentSampleModel, which is drawn very slowly to the screen. A
     *             much faster sample model is PixelInterleavedSampleModel,  which is the sample
     *             model used by BufferedImage for TYPE_BYTE_INDEXED. We should check if this is
     *             fixed in future J2SE release.
     */
    private static ImageLayout getLayout(final RenderedImage     image,
                                         final CategoryList categories)
    {
        ImageLayout layout = ImageUtilities.getImageLayout(image);
        ColorModel  colors = categories.getColorModel(0, image.getSampleModel().getNumBands());
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
     * Effectue le calcul d'une tuile de l'image.
     *
     * @param sources  Un tableau de longueur 1 contenant la source.
     * @param dest     La tuile dans laquelle écrire les pixels.
     * @param destRect La région de <code>dest</code> dans laquelle écrire.
     */
    protected void computeRect(final PlanarImage[] sources,
                               final WritableRaster   dest,
                               final Rectangle    destRect)
    {
        final WritableRectIter iterator = DualRectIter.create(
                RectIterFactory.create(sources[0],   destRect),
                RectIterFactory.createWritable(dest, destRect));
        int band=0;
        if (!iterator.finishedBands()) do {
            categories[band].transform(iterator);
            band++;
        }
        while (!iterator.nextBandDone());
        assert(band == categories.length) : band;
    }
}
