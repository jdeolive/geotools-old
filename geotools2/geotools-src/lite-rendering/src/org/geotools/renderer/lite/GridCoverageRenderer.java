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
package org.geotools.renderer.lite;
import java.awt.*;
import java.awt.image.*;

// JAI dependencies
import javax.media.jai.ImageMIPMap;
import java.awt.geom.*;

// Geotools dependencies
import org.geotools.gc.GridCoverage;
import org.geotools.gc.GridGeometry;
import org.geotools.ct.*;
import org.geotools.resources.XAffineTransform;


/**
 * A helper class for rendering {@link GridCoverage} objects.
 *
 * @version $Id: GridCoverageRenderer.java,v 1.2 2003/05/11 15:21:15 aaime Exp $
 * @author Martin Desruisseaux
 */
final class GridCoverageRenderer {
    /**
     * Tells if we should try an optimisation using pyramidal images.
     * Default value do not use this optimisation, since it doesn't
     * seems to provide the expected performance benefict in JAI 1.0.
     *
     * @task TODO: Test if JAI 1.1 gives better result now.
     */
    private static final boolean USE_PYRAMID = true;
    
    /**
     * Decimation factor for image. A value of 0.5 means that each
     * level in the image pyramid will contains an image with half
     * the resolution of previous level. This value is used only if
     * {@link #USE_PYRAMID} is <code>true</code>.
     */
    private static final double DOWN_SAMPLER = 0.5;
    
    /**
     * Natural logarithm of {@link #DOWN_SAMPLER}. Used
     * only if {@link #USE_PYRAMID} is <code>true</code>.
     */
    private static final double LOG_DOWN_SAMPLER = Math.log(DOWN_SAMPLER);
    
    /**
     * Minimum size (in pixel) for use of pyramidal image. Images smaller
     * than this size will not use pyramidal images, since it would not
     * give many visible benefict. Used only if {@link #USE_PYRAMID} is
     * <code>true</code>.
     */
    private static final int MIN_SIZE = 256;


    /**
     * The grid coverage to render.
     */
    //private final GridCoverage coverage;
    
    /**
     * A list of multi-resolution images. Image at level 0 is identical to
     * {@link GridCoverage#getRenderedImage()}.  Other levels contains the
     * image at lower resolution for faster rendering.
     */
    private final ImageMIPMap images;
    
    /**
     * Maximum amount of level to use for multi-resolution images.
     */
    private final int maxLevel;
    private GridGeometry gridGeometry;
    RenderedImage image;

    /**
     */
    public GridCoverageRenderer(GridCoverage gridCoverage) {
        try {
            image = gridCoverage.geophysics(false).getRenderedImage();
        } catch (Exception e) {
            System.out.println("Using geophysics image");
            image = gridCoverage.getRenderedImage();
        }
        gridGeometry = gridCoverage.getGridGeometry();
        images   = USE_PYRAMID ? new ImageMIPMap(image, AffineTransform.getScaleInstance(DOWN_SAMPLER, DOWN_SAMPLER), null) : null;
        maxLevel = Math.max((int) (Math.log((double)MIN_SIZE/(double)Math.max(image.getWidth(), image.getHeight()))/LOG_DOWN_SAMPLER), 0);
    }
    
    /**
     * Paint this grid coverage. The caller must ensure that <code>graphics</code>
     * has an affine transform mapping "real world" coordinates in the coordinate
     * system given by {@link #getCoordinateSystem}.
     */
    public void paint(final Graphics2D graphics) {
        final MathTransform2D mathTransform = gridGeometry.getGridToCoordinateSystem2D();
        if (!(mathTransform instanceof AffineTransform)) {
            throw new UnsupportedOperationException("Non-affine transformations not yet implemented"); // TODO
        }
        final AffineTransform gridToCoordinate = (AffineTransform) mathTransform;
        if (images==null) {
            final AffineTransform transform = new AffineTransform(gridToCoordinate);
            transform.translate(-0.5, -0.5); // Map to upper-left corner.
            try {
                graphics.drawRenderedImage(image, transform);
            } catch (Exception e) {
                graphics.drawRenderedImage(getGoodImage(image), transform);
            }
        } else {
            /*
             * Calcule quel "niveau" d'image serait le plus approprié.
             * Ce calcul est fait en fonction de la résolution requise.
             */ 
            
            AffineTransform transform=graphics.getTransform();
            transform.concatenate(gridToCoordinate);
            final int level = Math.max(0,
                              Math.min(maxLevel,
                              (int) (Math.log(Math.max(XAffineTransform.getScaleX0(transform),
                              XAffineTransform.getScaleY0(transform)))/LOG_DOWN_SAMPLER)));
            /*
             * Si on utilise une résolution inférieure (pour un
             * affichage plus rapide), alors il faut utiliser un
             * géoréférencement ajusté en conséquence.
             */
            transform.setTransform(gridToCoordinate);
            if (level!=0) {
                final double scale=Math.pow(DOWN_SAMPLER, -level);
                transform.scale(scale, scale);
            }
            transform.translate(-0.5, -0.5); // Map to upper-left corner.
            graphics.drawRenderedImage(images.getImage(level), transform);
        }
    }
    
    private RenderedImage getGoodImage(RenderedImage img) {
        BufferedImage good = new BufferedImage(img.getWidth(), img.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
        Graphics2D g2d = (Graphics2D) good.getGraphics();
        g2d.drawRenderedImage(img, new AffineTransform());
        return good;
    }
}
