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

// Images
import javax.media.jai.ImageLayout;
import javax.media.jai.PlanarImage;
import javax.media.jai.NullOpImage;
import javax.media.jai.RasterFactory;

import java.awt.image.RenderedImage;
import java.awt.image.WritableRaster;
import java.awt.image.BandedSampleModel;
import java.awt.image.SampleModel;
import java.awt.image.ColorModel;
import java.awt.image.DataBuffer;

// Other J2SE and JAI dependencies
import java.util.Arrays;
import java.awt.Rectangle;
import javax.media.jai.iterator.RectIter;
import javax.media.jai.iterator.RectIterFactory;


/**
 * Image dont les valeurs des pixels correspond aux valeurs réelles d'un paramètre géophysique.
 * Ces valeurs sont calculées en convertissant les valeurs <code>byte</code> d'une autre image
 * en valeur réelles exprimées selon les unités de {@link IndexedThemeMapper#getUnits}. Les données
 * manquantes seront exprimées avec différentes valeurs <code>NaN</code>.
 *
 * @version $Id: NumericImage.java,v 1.2 2002/07/17 23:30:55 desruisseaux Exp $
 * @author Martin Desruisseaux
 */
final class NumericImage extends ImageAdapter {
    /**
     * Convertit une image de valeurs de pixels en image de nombres réels.
     *
     * @param image Image de valeurs de pixels. Les pixels de cette image
     *              doivent correspondre aux thèmes de <code>categories</code>.
     * @return Image de nombres réels. Toutes les valeurs de cette image seront
     *         exprimées selon les unités {@link CategoryList#getUnit}. Les pixels
     *         qui ne correspondent pas au paramètre géophysique auront une valeur <code>NaN</code>.
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
            final ImageAdapter adapter = (ImageAdapter) image;
            if (Arrays.equals(adapter.categories, categories)) {
                return adapter.getNumeric();
            }
        }
        return new NumericImage(image, categories);
    }
    
    /**
     * Construit une image de nombres réelles à partir des
     * valeurs <code>byte</code> de l'image indexée spécifiée.
     *
     * @param image      Image contenant les valeurs indexées.
     * @param categories Ensemble des catégories qui donnent une signification aux pixels de l'image.
     */
    private NumericImage(final RenderedImage image, final CategoryList[] categories) {
        super(image, getLayout(image, categories[0], SampleInterpretation.GEOPHYSICS), categories);
    }
    
    /**
     * Retourne l'image qui contient les données sous forme de nombres réels.
     * Cette image sera <code>this</code>, puisqu'elle représente déjà un
     * décodage des valeurs de pixels.
     */
    public PlanarImage getNumeric() {
        return this;
    }
    
    /**
     * Retourne l'image qui contient les données sous forme de valeurs de thèmes.
     * Cette image sera l'image source de <code>this</code>.
     */
    public PlanarImage getThematic() {
        return getSourceImage(0);
    }
    
    /**
     * Effectue le calcul d'une tuile de l'image. L'image source doit contenir
     * des index correspondant aux thèmes {@link IndexedTheme},  tandis que la
     * tuile de destination aura les valeurs réelles correspondantes.
     *
     * @param sources  Un tableau de longueur 1 contenant la source.
     * @param dest     La tuile dans laquelle écrire les pixels.
     * @param destRect La région de <code>dest</code> dans laquelle écrire.
     */
    protected void computeRect(final PlanarImage[] sources,
                               final WritableRaster   dest,
                               final Rectangle    destRect)
    {
        final RectIter iterator = RectIterFactory.create(sources[0], destRect);
        int band=0;
        if (!iterator.finishedBands()) do {
            final CategoryList categories = this.categories[band];
            final Category blank = categories.getBlank();
            Category category = blank;
            int y=destRect.y;
            iterator.startLines();
            if (!iterator.finishedLines()) do {
                int x=destRect.x;
                iterator.startPixels();
                if (!iterator.finishedPixels()) do {
                    final int sample=iterator.getSample();
                    category = categories.getDecoder(sample, category);
                    if (category==null) category = blank;
                    dest.setSample(x,y,band, category.toGeophysicsValue(sample));
                    x++;
                }
                while (!iterator.nextPixelDone());
                assert(x == destRect.x + destRect.width) : x;
                y++;
            }
            while (!iterator.nextLineDone());
            assert(y == destRect.y + destRect.height) : y;
            band++;
        }
        while (!iterator.nextBandDone());
        assert(band == categories.length) : band;
    }
}
