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

// Images and geometry
import javax.media.jai.JAI;
import javax.media.jai.TileCache;
import javax.media.jai.ImageLayout;
import javax.media.jai.PlanarImage;
import javax.media.jai.PointOpImage;
import javax.media.jai.RasterFactory;

import java.awt.image.PixelInterleavedSampleModel;
import java.awt.image.ComponentSampleModel;
import java.awt.image.IndexColorModel;
import java.awt.image.WritableRaster;
import java.awt.image.RenderedImage;
import java.awt.image.SampleModel;
import java.awt.image.ColorModel;
import java.awt.Rectangle;
import java.awt.Dimension;

// Geotools dependencies
import org.geotools.resources.ImageUtilities;


/**
 * Classe de base des images qui représenteront leurs données sous forme
 * de nombre réels ou sous forme d'index de thèmes {@link IndexedTheme}.
 *
 * @version 1.0
 * @author Martin Desruisseaux
 */
abstract class ImageAdapter extends PointOpImage {
    /**
     * Ensemble des catégories qui donnent une
     * signification aux pixels de l'image. La
     * longueur de ce tableau doit correspondre
     * au nombre de bandes de l'image source.
     */
    protected final CategoryList[] categories;
    
    /**
     * Construit une image qui puisera ses données dans l'image spécifiée.
     *
     * @param image      Image source.
     * @param layout     Disposition de l'image de destination.
     * @param categories Ensemble des catégories qui donnent une signification aux pixels de l'image.
     */
    protected ImageAdapter(final RenderedImage image,
                           final ImageLayout   layout,
                           final CategoryList[] categories)
    {
        super(image, layout, JAI.getDefaultInstance().getRenderingHints(), false);
        this.categories = categories;
        final int numBands = image.getSampleModel().getNumBands();
        if (categories.length!=numBands) {
            throw new IllegalArgumentException(String.valueOf(categories.length)+"!="+numBands);
        }
        permitInPlaceOperation();
    }
    
    /**
     * Returns the destination image layout.
     *
     * @param  image The source image.
     * @param  categories Category list.
     * @param  geophysicsValue <code>true</code> if destination will contains geophysics values.
     * @return Layout for the destination image.
     */
    protected static ImageLayout getLayout(final RenderedImage     image,
                                           final CategoryList categories,
                                           final boolean geophysicsValue)
    {
        ImageLayout layout = ImageUtilities.getImageLayout(image);
        ColorModel  colors = categories.getColorModel(geophysicsValue, image.getSampleModel().getNumBands());
        SampleModel  model = colors.createCompatibleSampleModel(image.getWidth(), image.getHeight());
        if (colors instanceof IndexColorModel && model.getClass().equals(ComponentSampleModel.class)) {
            // TODO: IndexColorModel seems to badly choose his sample model. As of JDK 1.4-rc1, it
            //       construct a ComponentSampleModel, which is drawn very slowly to the screen. A
            //       much faster sample model is PixelInterleavedSampleModel, which is the sample
            //       model used by BufferedImage for TYPE_BYTE_INDEXED. Java2D seems to be optimized
            //       for this sample model when used with IndexColorModel.
            final int w = model.getWidth();
            final int h = model.getHeight();
            model = new PixelInterleavedSampleModel(colors.getTransferType(), w, h, 1, w, new int[1]);
        }
        return layout.setSampleModel(model).setColorModel(colors);
    }
    
    /**
     * Retourne l'image qui contient les données sous forme de nombres réels.
     * Cette image sera <code>this</code> ou l'image source de <code>this</code>.
     */
    public abstract PlanarImage getNumeric();
    
    /**
     * Retourne l'image qui contient les données sous forme de valeurs de thèmes.
     * Cette image sera <code>this</code> ou l'image source de <code>this</code>.
     */
    public abstract PlanarImage getThematic();
    
    /**
     * Effectue le calcul d'une tuile de l'image.
     *
     * @param sources  Un tableau de longueur 1 contenant la source.
     * @param dest     La tuile dans laquelle écrire les pixels.
     * @param destRect La région de <code>dest</code> dans laquelle écrire.
     */
    protected abstract void computeRect(final PlanarImage[] sources,
                                        final WritableRaster dest,
                                        final Rectangle destRect);
}
