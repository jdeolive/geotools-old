/*
 *    Geotools2 - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002, Geotools Project Managment Committee (PMC)
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    version 2.1 of the License.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *
 */
package org.geotools.renderer.lite;

import org.geotools.ct.MathTransform2D;
import org.geotools.gc.GridCoverage;
import org.geotools.resources.XAffineTransform;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import javax.media.jai.ImageMIPMap;


/**
 * A helper class for rendering {@link GridCoverage} objects. Still doesn't
 * support grid coverage SLD stylers
 *
 * @author Martin Desruisseaux
 * @author Andrea Aime
 * @version $Id: GridCoverageRenderer.java,v 1.5 2004/03/14 18:44:27 aaime Exp $
 *
 * @task Add support for SLD stylers
 */
final class GridCoverageRenderer {
    /** Tells if we should try an optimisation using pyramidal images. */
    private static final boolean USE_PYRAMID = false;

    /**
     * Decimation factor for image. A value of 0.5 means that each level in the
     * image pyramid will contains an image with half the resolution of
     * previous level. This value is used only if {@link #USE_PYRAMID} is
     * <code>true</code>.
     */
    private static final double DOWN_SAMPLER = 0.5;

    /**
     * Natural logarithm of {@link #DOWN_SAMPLER}. Used only if {@link
     * #USE_PYRAMID} is <code>true</code>.
     */
    private static final double LOG_DOWN_SAMPLER = Math.log(DOWN_SAMPLER);

    /**
     * Minimum size (in pixel) for use of pyramidal image. Images smaller than
     * this size will not use pyramidal images, since it would not give many
     * visible benefict. Used only if {@link #USE_PYRAMID} is
     * <code>true</code>.
     */
    private static final int MIN_SIZE = 256;

    /** The grid coverage to be rendered. */
    private final GridCoverage gridCoverage;

    //private final GridCoverage coverage;

    /**
     * A list of multi-resolution images. Image at level 0 is identical to
     * {@link GridCoverage#getRenderedImage()}.  Other levels contains the
     * image at lower resolution for faster rendering.
     */
    private final ImageMIPMap images;

    /** Maximum amount of level to use for multi-resolution images. */
    private final int maxLevel;

    /**
     * The renderered image that represents the grid coverage according to  the
     * current style setting
     */
    private RenderedImage image;

    /**
     * Creates a new GridCoverageRenderer object.
     *
     * @param gridCoverage DOCUMENT ME!
     */
    public GridCoverageRenderer(GridCoverage gridCoverage) {
        this.gridCoverage = gridCoverage;

        try {
            image = gridCoverage.geophysics(false).getRenderedImage();
        } catch (Exception e) {
            System.out.println("Using geophysics image");
            image = gridCoverage.getRenderedImage();
        }

        if (USE_PYRAMID) {
            AffineTransform at = AffineTransform.getScaleInstance(
                    DOWN_SAMPLER, DOWN_SAMPLER);
            images = new ImageMIPMap(image, at, null);
        } else {
            images = null;
        }

        double maxSize = Math.max(image.getWidth(), image.getHeight());
        int logLevel = (int) (Math.log(MIN_SIZE / maxSize) / LOG_DOWN_SAMPLER);
        maxLevel = Math.max(logLevel, 0);
    }

    /**
     * Paint this grid coverage. The caller must ensure that
     * <code>graphics</code> has an affine transform mapping "real world"
     * coordinates in the coordinate system given by {@link
     * #getCoordinateSystem}.
     *
     * @param graphics the <code>Graphics</code> context in which to paint
     *
     * @throws UnsupportedOperationException if the transformation from grid to
     *         coordinate system in the GridCoverage is not an AffineTransform
     */
    public void paint(final Graphics2D graphics) {
        final MathTransform2D mathTransform;
        mathTransform = gridCoverage.getGridGeometry()
                                    .getGridToCoordinateSystem2D();

        if (!(mathTransform instanceof AffineTransform)) {
            throw new UnsupportedOperationException(
                "Non-affine transformations not yet implemented"); // TODO
        }

        AffineTransform gridToCoordinate = (AffineTransform) mathTransform;

        if (images == null) {
            final AffineTransform transform;
            transform = new AffineTransform(gridToCoordinate);
            transform.translate(-0.5, -0.5); // Map to upper-left corner.

            try {
                graphics.drawRenderedImage(image, transform);
            } catch (Exception e) {
                image = getGoodImage(image);
                graphics.drawRenderedImage(image, transform);
            }
        } else {
            /*
             * Compute the most appropriate level as a function of the required
             * resolution
             */
            AffineTransform transform = graphics.getTransform();
            transform.concatenate(gridToCoordinate);

            double maxScale = Math.max(
                    XAffineTransform.getScaleX0(transform),
                    XAffineTransform.getScaleY0(transform));
            int level = Math.min(
                    maxLevel, (int) (Math.log(maxScale) / LOG_DOWN_SAMPLER));

            if (level < 0) {
                level = 0;
            }

            /*
             * If using an inferior resolution to speed up painting adjust
             * georeferencing
             */
            transform.setTransform(gridToCoordinate);

            if (level != 0) {
                final double scale = Math.pow(DOWN_SAMPLER, -level);
                transform.scale(scale, scale);
            }

            transform.translate(-0.5, -0.5); // Map to upper-left corner.
            graphics.drawRenderedImage(images.getImage(level), transform);
        }
    }

    /**
     * Work around a bug in older JDKs
     *
     * @param img The JAI rendered image
     *
     * @return a buffered image that can be handled by Java2D without problems
     */
    private RenderedImage getGoodImage(RenderedImage img) {
        BufferedImage good = new BufferedImage(
                img.getWidth(), img.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
        Graphics2D g2d = (Graphics2D) good.getGraphics();
        g2d.drawRenderedImage(img, new AffineTransform());

        return good;
    }
}
