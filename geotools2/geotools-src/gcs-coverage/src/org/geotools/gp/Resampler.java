/*
 * Geotools 2 - OpenSource mapping toolkit
 * (C) 2003, Geotools Project Management Committee (PMC)
 * (C) 2002, Institut de Recherche pour le Développement
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

// J2SE dependencies
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.Rectangle2D;
import java.awt.geom.AffineTransform;
import java.awt.image.RenderedImage;
import java.awt.image.renderable.ParameterBlock;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.Locale;
import java.util.List;

// Java Advanced Imaging
import javax.media.jai.JAI;
import javax.media.jai.Warp;
import javax.media.jai.RenderedOp;
import javax.media.jai.PlanarImage;
import javax.media.jai.ImageLayout;
import javax.media.jai.Interpolation;
import javax.media.jai.ParameterList;
import javax.media.jai.ParameterListDescriptor;
import javax.media.jai.ParameterListDescriptorImpl;
import javax.media.jai.InterpolationNearest;
import javax.media.jai.IntegerSequence;

// Geotools (GCS) dependencies
import org.geotools.cv.Category;
import org.geotools.gc.GridRange;
import org.geotools.gc.GridCoverage;
import org.geotools.gc.GridGeometry;
import org.geotools.cv.SampleDimension;
import org.geotools.gc.InvalidGridGeometryException;

// Geotools (CTS) dependencies
import org.geotools.pt.Matrix;
import org.geotools.pt.Envelope;
import org.geotools.cs.CoordinateSystem;
import org.geotools.cs.FactoryException;
import org.geotools.ct.MathTransform;
import org.geotools.ct.MathTransform2D;
import org.geotools.ct.TransformException;
import org.geotools.ct.MathTransformFactory;
import org.geotools.ct.CoordinateTransformation;
import org.geotools.ct.CoordinateTransformationFactory;

// Resources
import org.geotools.util.NumberRange;
import org.geotools.resources.JAIUtilities;
import org.geotools.resources.CTSUtilities;
import org.geotools.resources.GCSUtilities;
import org.geotools.resources.ImageUtilities;
import org.geotools.resources.gcs.Resources;
import org.geotools.resources.gcs.ResourceKeys;
import org.geotools.resources.XAffineTransform;


/**
 * Resample a grid coverage using a different grid geometry.
 * This operation provides the following functionality:<br>
 * <br>
 * <strong>Resampling</strong><br>
 * The grid coverage can be resampled at a different cell resolution. Some implementations
 * may be able to do resampling efficiently at any resolution. This can be determined from
 * the {@link GridCoverageProcessor} metadata <code>HasArbitraryResolutions</code> keyword.
 * Also a non-rectilinear grid coverage can be accessed as rectilinear grid coverage with
 * this operation.<br>
 * <br>
 * <strong>Reprojecting</strong><br>
 * The new grid geometry can have a different coordinate system than the underlying grid
 * geometry. For example, a grid coverage can be reprojected from a geodetic coordinate
 * system to Universal Transverse Mercator coordinate system.<br>
 * <br>
 * <strong>Subsetting</strong><br>
 * A subset of a grid can be viewed as a separate coverage by using this operation with a
 * grid geometry which as the same geoferencing and a region. Grid range in the grid geometry
 * defines the region to subset in the grid coverage.<br>
 *
 * @version $Id: Resampler.java,v 1.18 2003/08/01 10:41:53 desruisseaux Exp $
 * @author Martin Desruisseaux
 */
final class Resampler extends GridCoverage {
    /**
     * Disable the native acceleration for the "Affine" operation. In JAI 1.1.2, the "Affine"
     * operation on TYPE_FLOAT datatype with INTERP_BILINEAR interpolation cause an exception
     * in the native code of medialib, which halt the Java Virtual Machine. Using the pure Java
     * implementation instead resolve the problem.
     *
     * @task TODO: Remove this hack when Sun will fix the medialib bug.
     */
    static {
        ImageUtilities.allowNativeAcceleration("Affine", false);
    }

    /**
     * Construct a new grid coverage for the specified grid geometry.
     *
     * @param source       The source for this grid coverage.
     * @param image        The image.
     * @param cs           The coordinate system.
     * @param envelope     The grid geometry.
     */
    private Resampler(final GridCoverage   source,
                      final RenderedImage   image,
                      final CoordinateSystem   cs,
                      final GridGeometry geometry)
    {
        /*
         * If the grid geometry has more than 2 dimensions, then the current implementation
         * of GridCoverage assumes that grid range for all dimensions above 2 is [n..n+1].
         * TODO: Should we accept the grid geometry as is? (open question...)
         */
        super(source.getName(null), image, cs,
              geometry.getGridToCoordinateSystem(),
              source.getSampleDimensions(),
              new GridCoverage[]{source}, null);
    }
    
    /**
     * Construct a new grid coverage for the specified envelope.
     *
     * @param source       The source for this grid coverage.
     * @param image        The image.
     * @param cs           The coordinate system.
     * @param envelope     The grid coverage cordinates. The two first dimensions describe
     *                     the image location along <var>x</var> and <var>y</var> axis. The
     *                     other dimensions are optional and may be used to locate the image
     *                     on a vertical axis or on the time axis.
     */
    private Resampler(final GridCoverage       source,
                      final RenderedImage       image,
                      final CoordinateSystem       cs,
                      final Envelope         envelope)
    {
        super(source.getName(null),
              image, cs, envelope,
              source.getSampleDimensions(),
              new GridCoverage[]{source}, null);
    }
    
    /**
     * Create a new coverage with a different coordinate reference system.
     *
     * @param  sourceCoverage The source grid coverage.
     * @param  targetCS Coordinate system for the new grid coverage, or <code>null</code>.
     * @param  targetGG The target grid geometry, or <code>null</code> for default.
     * @param  interpolation The interpolation to use.
     * @param  The rendering hingts. This is usually provided by a {@link GridCoverageProcessor}.
     *         This method will looks for {@link Hints#COORDINATE_TRANSFORMATION_FACTORY}
     *         and {@link Hints#JAI_INSTANCE} keys.
     * @return The new grid coverage, or <code>sourceCoverage</code> if no resampling was needed.
     * @throws FactoryException is a transformation step can't be created.
     * @throws TransformException if a transformation failed.
     */
    public static GridCoverage reproject(      GridCoverage sourceCoverage,
                                         final CoordinateSystem   targetCS,
                                               GridGeometry       targetGG,
                                         final Interpolation interpolation,
                                         final RenderingHints        hints)
        throws FactoryException, TransformException
    {
        /*
         * Gets the {@link JAI} instance to use from the rendering hints.
         */
        Object property = (hints!=null) ? hints.get(Hints.JAI_INSTANCE) : null;
        final JAI processor;
        if (property instanceof JAI) {
            processor = (JAI) property;
        } else {
            processor = JAI.getDefaultInstance();
        }
        /*
         * Gets the {@link CoordinateTransformationFactory} to use from the rendering hints.
         */
        property = (hints!=null) ? hints.get(Hints.COORDINATE_TRANSFORMATION_FACTORY) : null;
        final CoordinateTransformationFactory factory;
        if (property instanceof CoordinateTransformationFactory) {
            factory = (CoordinateTransformationFactory) property;
        } else {
            factory = CoordinateTransformationFactory.getDefault();
        }
        final MathTransformFactory mtFactory = factory.getMathTransformFactory();
        /*
         * If the source coverage is already the result of a "Resample" operation,
         * go up in the chain and check if a previously computed image could fits.
         */
        GridGeometry     sourceGG; boolean sameGG;
        CoordinateSystem sourceCS; boolean sameCS;
        while (true) {
            sourceGG = sourceCoverage.getGridGeometry();
            sourceCS = sourceCoverage.getCoordinateSystem();
            sameGG   = (targetGG==null || equivalent(targetGG, sourceGG));
            sameCS   = (targetCS==null || targetCS.equals(sourceCS, false));
            if (sameGG && sameCS) {
                return sourceCoverage;
            }
            if (sourceCoverage instanceof Resampler) {
                final GridCoverage[] sources = sourceCoverage.getSources();
                if (sources.length != 1) {
                    // Should not happen, but test anyway.
                    throw new AssertionError(sources.length);
                }
                sourceCoverage = sources[0];
                continue;
            }
            break;
        }
        /*
         * We use the next two lines mostly as an argument check. If a coordinate system
         * can't be reduced to a two-dimensional one,   then an IllegalArgumentException
         * will be thrown.  Note that a less rigourous check is performed later (compare
         * envelopes), so we could comment out this block in order to accept a wider
         * range of (possibly incorrect) coordinate systems.
         */
        if (true) {
            CTSUtilities.getCoordinateSystem2D(sourceCS);
            CTSUtilities.getCoordinateSystem2D(targetCS);
        }
        /*
         * The projection are usually applied on floating-point values, in order
         * to gets maximal precision and to handle correctly the special case of
         * NaN values. However, we can apply the projection on integer values if
         * the interpolation type is "nearest neighbor", since this is not really
         * an interpolation.
         *
         * If this condition is meets, then we verify if an "integer version" of the image
         * is available as a source of the source coverage (i.e. the floating-point image
         * is derived from the integer image, not the converse).
         */
        Boolean targetGeophysics = null;
        if (interpolation instanceof InterpolationNearest) {
            final GridCoverage candidate = sourceCoverage.geophysics(false);
            if (candidate != sourceCoverage) {
                final List sources = sourceCoverage.getRenderedImage().getSources();
                if (sources != null) {
                    if (sources.contains(candidate.getRenderedImage())) {
                        sourceCoverage   = candidate;
                        targetGeophysics = Boolean.TRUE;
                    }
                }
            }
        }
        /*
         * Gets the target image as a {@link RenderedOp}.  The source image is
         * initially wrapped into in a "Null" operation. Later, we will change
         * this "Null" operation into a "Warp" operation.  We can't use "Warp"
         * now because we will know the envelope only after creating the GridCoverage.
         * Note: RenderingHints contain mostly indications about tiles layout.
         */
        final PlanarImage sourceImage = PlanarImage.wrapRenderedImage(sourceCoverage.getRenderedImage());
        final ParameterBlock paramBlk = new ParameterBlock().addSource(sourceImage);
        RenderingHints    targetHints = ImageUtilities.getRenderingHints(sourceImage);
        if (targetHints == null) {
            targetHints = hints;
        } else if (hints != null) {
            targetHints.add(hints);
        }
        final RenderedOp targetImage = processor.createNS("Null", paramBlk, targetHints);
        GridCoverage  targetCoverage = null;
        /*
         * Compute the INVERSE of the math transform from [Source Grid] to [Target Grid].
         * The transform will be computed using the inverse of the following paths:
         *
         *      Source Grid --> Source CS --> Target CS --> Target Grid
         *
         * If source and target CS are equals, a shorter path is used. This special case
         * is needed because 'targetCS' may be null (which means "same CS than source").
         * Note that 'sourceCS' may be null as well.
         *
         *      Source Grid --> Common CS --> Target Grid
         */
        final MathTransform transform, step1, step2, step3, step2x;
        if (sameCS) {
            step2x = null;
            step2  = MathTransform2D.IDENTITY;
            if (!GCSUtilities.hasTransform(targetGG)) {
                // TargetGG should not be null, otherwise the code above should
                // have already detected that this resample is not doing anything.
                targetGG = new GridGeometry(targetGG.getGridRange(),
                                            sourceGG.getGridToCoordinateSystem());
            }
        } else {
            if (sourceCS==null || targetCS==null) {
                throw new CannotReprojectException(Resources.format(
                        ResourceKeys.ERROR_UNSPECIFIED_COORDINATE_SYSTEM));
            }
            final MathTransform step2r;
            step2x = factory.createFromCoordinateSystems(sourceCS, targetCS).getMathTransform();
            step2r = getMathTransform2D(step2x, mtFactory);
            /*
             * Gets the source and target envelope. It is difficult to check if the first
             * two dimensions are really independent from other dimensions.   However, if
             * we get the same 2-dimensional envelope no matter if we took in account the
             * extra dimensions or not, then we will assume that projecting the image with
             * a MathTransform2D is safe enough.
             */
            final Envelope sourceEnvelope   = sourceCoverage.getEnvelope();
            final Envelope sourceEnvelope2D = sourceEnvelope.getSubEnvelope(0,2);
            final Envelope targetEnvelope   = CTSUtilities.transform(step2x, sourceEnvelope  );
            final Envelope targetEnvelope2D = CTSUtilities.transform(step2r, sourceEnvelope2D);
            if (!targetEnvelope.getSubEnvelope(0,2).equals(targetEnvelope2D)) {
                throw new TransformException(Resources.format(
                        ResourceKeys.ERROR_NO_TRANSFORM2D_AVAILABLE));
            }
            /*
             * If the target GridGeometry is incomplete, provides default
             * values for the missing fields. Three cases may occur:
             *
             * - User provided no GridGeometry at all. Then, constructs an image of the same size
             *   than the source image and set an envelope big enough to contains the projected
             *   coordinates. The transform will derivate from the grid range and the envelope.
             *
             * - User provided only a grid range.  Then, set an envelope big enough to contains
             *   the projected coordinates. The transform will derivate from the grid range and
             *   the envelope.
             *
             * - User provided only a "grid to coordinate system" transform. Then, transform the
             *   projected envelope to "grid units" using the specified transform,  and create a
             *   grid range big enough to hold the result.
             */
            if (targetGG == null) {
                targetCoverage=new Resampler(sourceCoverage, targetImage, targetCS, targetEnvelope);
                targetGG = targetCoverage.getGridGeometry();
            }
            else if (!GCSUtilities.hasTransform(targetGG)) {
                targetGG = new GridGeometry(targetGG.getGridRange(), targetEnvelope, null);
            }
            else if (!GCSUtilities.hasGridRange(targetGG)) {
                final MathTransform step3x = targetGG.getGridToCoordinateSystem();
                final Envelope gridRange = CTSUtilities.transform(step3x.inverse(), targetEnvelope);
                for (int i=gridRange.getDimension(); --i>=0;) {
                    // According OpenGIS specification, GridGeometry maps pixel's center. But
                    // the bounding box was for all pixels, not pixel's centers. Offset by
                    // 0.5 (use +0.5 for maximum too, not -0.5, since maximum is exclusive).
                    gridRange.setRange(i, gridRange.getMinimum(i)+0.5, gridRange.getMaximum(i)+0.5);
                }
                targetGG = new GridGeometry(GCSUtilities.toGridRange(gridRange), step3x);
            }
            step2 = step2r.inverse();
        }
        /*
         * Complete the transformation from [Target Grid] to [Source Grid]. If the target grid
         * range was not explicitely specified, a grid range will be automatically computed in
         * such a way that it will map to the same envelope (at least approximatively). We use
         * the inverse of 'transform' for this purpose, except that the transform to be used for
         * grid range may have more than 2 dimensions.
         */
        step1 = targetGG.getGridToCoordinateSystem2D();
        step3 = sourceGG.getGridToCoordinateSystem2D().inverse();
        if (step1.equals(step3.inverse())) {
            transform = step2;
        } else {
            transform = mtFactory.createConcatenatedTransform(
                        mtFactory.createConcatenatedTransform(step1, step2), step3);
        }
        if (!(transform instanceof MathTransform2D)) {
            // Should not happen with Geotools implementations. May happen
            // with some external implementations, but should stay unusual.
            throw new TransformException(Resources.format(
                                         ResourceKeys.ERROR_NO_TRANSFORM2D_AVAILABLE));
        }
        if (!GCSUtilities.hasGridRange(targetGG)) {
            final MathTransform xtr;
            final MathTransform step1x = targetGG.getGridToCoordinateSystem();
            final MathTransform step3x = sourceGG.getGridToCoordinateSystem().inverse();
            if (step1x.equals(step3x.inverse())) {
                xtr = step2x;
            } else if (step2x==null) {
                xtr = mtFactory.createConcatenatedTransform(step1x, step3x);
            } else {
                xtr = mtFactory.createConcatenatedTransform(
                      mtFactory.createConcatenatedTransform(step1x, step2x), step3x);
            }
            if (xtr != null) {
                assert getMathTransform2D(xtr, mtFactory).equals(transform) : xtr;
                Envelope envelope = GCSUtilities.toEnvelope(sourceGG.getGridRange());
                envelope = CTSUtilities.transform(xtr.inverse(), envelope);
                targetGG = new GridGeometry(GCSUtilities.toGridRange(envelope), step1x);
            } else {
                assert transform.isIdentity() : transform;
                targetGG = sourceGG;
            }
        }
        /*
         * If the target coverage has not been created yet, change the image bounding box in
         * order to matches the grid range. If the target coverage has already been created,
         * just make sure that the image bounding box will stay constant even when we will
         * change the "Null" operation into a "Warp" one later.
         */
        if (true) {
            final GridRange gridRange =
                (targetCoverage!=null ? targetCoverage.getGridGeometry() : targetGG).getGridRange();
            int layoutMask = 0;
            ImageLayout layout;
            if (hints != null) {
                layout = (ImageLayout)hints.get(JAI.KEY_IMAGE_LAYOUT);
                if (layout != null) {
                    layoutMask = layout.getValidMask();
                }
            }
            layout = (ImageLayout)targetImage.getRenderingHint(JAI.KEY_IMAGE_LAYOUT);
            if (layout != null) {
                layout = (ImageLayout) layout.clone();
            } else {
                layout = new ImageLayout();
            }
            if (0==(layoutMask & (ImageLayout.MIN_X_MASK |
                                  ImageLayout.MIN_Y_MASK |
                                  ImageLayout.WIDTH_MASK |
                                  ImageLayout.HEIGHT_MASK)))
            {
                layout.setMinX  (gridRange.getLower (0));
                layout.setMinY  (gridRange.getLower (1));
                layout.setWidth (gridRange.getLength(0));
                layout.setHeight(gridRange.getLength(1));
                targetImage.setRenderingHint(JAI.KEY_IMAGE_LAYOUT, layout);
            }
            if (0==(layoutMask & (ImageLayout.TILE_WIDTH_MASK |
                                  ImageLayout.TILE_HEIGHT_MASK |
                                  ImageLayout.TILE_GRID_X_OFFSET_MASK |
                                  ImageLayout.TILE_GRID_Y_OFFSET_MASK)))
            {
                layout.setTileGridXOffset(layout.getMinX(targetImage));
                layout.setTileGridYOffset(layout.getMinY(targetImage));
                final int width  = layout.getWidth (targetImage);
                final int height = layout.getHeight(targetImage);
                if (layout.getTileWidth (targetImage) > width ) layout.setTileWidth (width);
                if (layout.getTileHeight(targetImage) > height) layout.setTileHeight(height);
                targetImage.setRenderingHint(JAI.KEY_IMAGE_LAYOUT, layout);
            }
        }
        /*
         * If the user request a new grid geometry with the same coordinate system, and if
         * the grid geometry is equivalents to a simple extraction of a sub-area, delegate
         * the work to a "Crop" operation.
         */
        if (transform.isIdentity()) {
            final GridRange sourceGR = sourceGG.getGridRange();
            final GridRange targetGR = targetGG.getGridRange();
            final int xmin = targetGR.getLower(0);
            final int xmax = targetGR.getUpper(0);
            final int ymin = targetGR.getLower(1);
            final int ymax = targetGR.getUpper(1);
            if (xmin >= sourceGR.getLower(0) &&
                xmax <= sourceGR.getUpper(0) &&
                ymin >= sourceGR.getLower(1) &&
                ymax <= sourceGR.getUpper(1))
            {
                paramBlk.add((float) (xmin));
                paramBlk.add((float) (ymin));
                paramBlk.add((float) (xmax-xmin));
                paramBlk.add((float) (ymax-ymin));
                targetImage.setParameterBlock(paramBlk);
                targetImage.setOperationName("Crop");
            }
        }
        /*
         * Get the sample value to use for background. We will try to fetch this value from one
         * of "no data" categories. For geophysics image, it is usually NaN. For non-geophysics
         * image, it is usually 0.
         */
        final SampleDimension[] sampleDimensions = sourceCoverage.getSampleDimensions();
        final double[] background = new double[sampleDimensions.length];
        for (int i=0; i<background.length; i++) {
            final NumberRange range = sampleDimensions[i].getBackground().getRange();
            final double min = range.getMinimum();
            final double max = range.getMaximum();
            if (range.isMinIncluded()) {
                background[i] = min;
            } else if (range.isMaxIncluded()) {
                background[i] = max;
            } else {
                background[i] = 0.5*(min+max);
            }
        }
        /*
         * Special case for the affine transform. Try to use the JAI "Affine" operation instead of
         * the more general "Warp" one. JAI provides native acceleration for the affine operation.
         * NOTE: "Affine", "Scale", "Translate", "Rotate" and similar operations ignore the 'xmin',
         * 'ymin', 'width' and 'height' image layout. Consequently, we can't use this operation if
         * the user provided explicitely a grid geometry. We use it only for automatically generated
         * geometry (the 'if (targetGG==null)' case above), in which case the target coverage may
         * need to be replaced.
         *
         * Note: a yet cheaper approach is to just update the 'gridToCoordinateSystem' value. We
         *       are allowed to do that since it is an automatically computed one. Then, returns
         *       a grid coverage wrapping the SOURCE image with the updated grid geometry.
         */
        if (targetImage.getOperationName().equalsIgnoreCase("Null")) {
            if (transform instanceof AffineTransform) {
                if (targetCoverage != null) {
                    // Cheapest approach: just update 'gridToCoordinateSystem'.
                    MathTransform mtr = targetGG.getGridToCoordinateSystem();
                    mtr = mtFactory.createConcatenatedTransform(transform.inverse(), mtr);
                    targetGG = new GridGeometry(sourceGG.getGridRange(), mtr);
                    return new Resampler(sourceCoverage, sourceImage, targetCS, targetGG);
                }
                // More general approach: apply the affine transform.
                final AffineTransform affine = (AffineTransform) transform.inverse();
                paramBlk.add(affine).add(interpolation).add(background);
                targetImage.setParameterBlock(paramBlk);
                targetImage.setOperationName("Affine");
                Rectangle targetBounds = targetGG.getGridRange().getSubGridRange(0,2).toRectangle();
                if (!targetBounds.equals(targetImage.getBounds())) {
                    if (targetCoverage == null) {
                        // "Affine" computed its own image bounds, which doesn't
                        // matches the requested bounds. Unroll the operation...
                        paramBlk.removeParameters();
                        targetImage.setOperationName("Null");
                        targetImage.setParameterBlock(paramBlk);
                    } else {
                        // ...or update the GridCoverage if we are allowed to do so.
                        targetCoverage = new Resampler(sourceCoverage, targetImage, targetCS, 
                                                       targetCoverage.getEnvelope());
                        targetGG = targetCoverage.getGridGeometry();
                    }
                }
            }
        }
        if (targetImage.getOperationName().equalsIgnoreCase("Null")) {
            /*
             * General case: construct the warp transform.  We had to set the warp transform
             * last because the construction of 'WarpTransform' requires the geometry of the
             * target grid coverage. The trick was to initialize the target image with a null
             * operation, and change the operation here.
             */
            paramBlk.add(new WarpTransform(sourceCoverage.getName(null), (MathTransform2D)transform));
            paramBlk.add(interpolation).add(background);
            targetImage.setParameterBlock(paramBlk); // Must be invoked before setOperationName
            targetImage.setOperationName("Warp");
        }
        /*
         * The "Warp" operation with JAI 1.1.2-rc sometime returns an image with a bounding
         * box different from what we expected. It was not the case with JAI 1.1.1 (is it a
         * regression?).  As a safety, we check the bounding box in any case. If it doesn't
         * matches, then we will reconstruct the target coverage with a new grid geometry.
         */
        if (targetGG != null) {
            final GridRange targetGR = targetGG.getGridRange();
            final int[] lower = targetGR.getLowers();
            final int[] upper = targetGR.getUppers();
            lower[0] = targetImage.getMinX();
            lower[1] = targetImage.getMinY();
            upper[0] = targetImage.getWidth()  + lower[0];
            upper[1] = targetImage.getHeight() + lower[1];
            final GridRange actualGR = new GridRange(lower, upper);
            if (!targetGR.equals(actualGR)) {
                MathTransform gridToCoordinateSystem = targetGG.getGridToCoordinateSystem();
                if (false) {
                    // With JAI 1.1.2-rc, the bounding box seems wrong but the transform seems
                    // right. Set to 'true' if the transform need an adjustement as well.
                    gridToCoordinateSystem =
                          mtFactory.createConcatenatedTransform(
                          mtFactory.createAffineTransform(
                             Matrix.createAffineTransform(GCSUtilities.toEnvelope(actualGR),
                                                          GCSUtilities.toEnvelope(targetGR))),
                                                          gridToCoordinateSystem);
                }
                targetGG = new GridGeometry(actualGR, gridToCoordinateSystem);
                targetCoverage = null;
            }
        }
        /*
         * Construct the final grid coverage.
         */
        if (targetCoverage == null) {
            targetCoverage = new Resampler(sourceCoverage, targetImage, targetCS, targetGG);
        }
        if (targetGeophysics != null) {
            targetCoverage = targetCoverage.geophysics(targetGeophysics.booleanValue());
        }
        assert targetCoverage.getCoordinateSystem().equals(targetCS!=null ? targetCS : sourceCS, false);
        assert targetGG!=null || targetImage.getBounds().equals(sourceImage.getBounds());
        assert targetCoverage.getGridGeometry().getGridRange().getSubGridRange(0,2).toRectangle()
                             .equals(targetImage.getBounds()) : targetGG;
        return targetCoverage;
    }

    /**
     * Returns the math transform for the two first dimensions of the specified transform.
     * This method is usefull for extracting the transformation caused by the head CS in
     * a {@link org.geotools.cs.CompoundCoordinateSystem}, assuming that this head CS is
     * a {@link org.geotools.cs.HorizontalCoordinateSystem}.
     *
     * @param  transform The transform.
     * @param  mtFactory The factory to use for extracting the sub-transform.
     * @return The {@link MathTransform2D} part of <code>transform</code>.
     * @throws FactoryException if <code>transform</code> is not separable.
     */
    private static MathTransform2D getMathTransform2D(MathTransform transform,
                                                final MathTransformFactory mtFactory)
            throws FactoryException
    {
        final IntegerSequence  inputDimensions = JAIUtilities.createSequence(0, 1);
        final IntegerSequence outputDimensions = new IntegerSequence();
        transform = mtFactory.createSubTransform(transform, inputDimensions, outputDimensions);
        if (JAIUtilities.containsAll(outputDimensions, 0, 2)) {
            transform = mtFactory.createFilterTransform(transform, inputDimensions);
            return (MathTransform2D) transform;
        }
        throw new FactoryException(Resources.format(ResourceKeys.ERROR_NO_TRANSFORM2D_AVAILABLE));
    }

    /**
     * Check if two geometries are equal, ignoring unspecified fields. If one or both geometries
     * has no &quot;gridToCoordinateSystem&quot; transform, then this properties is not taken in
     * account. Same apply for the grid range.
     * 
     *
     * @param  range1 The first range.
     * @param  range2 The second range.
     * @return <code>true</code> if the two geometries are equal, ignoring unspecified fields.
     */
    private static boolean equivalent(final GridGeometry geom1, final GridGeometry geom2) {
        if (geom1.equals(geom2)) {
            return true;
        }
        try {
            if (!geom1.getGridRange().equals(geom2.getGridRange())) {
                return false;
            }
        } catch (InvalidGridGeometryException exception) {
            // One geometry doesn't have a grid range.
            // Do not compare this properties.
        }
        try {
            if (!geom1.getGridToCoordinateSystem().equals(geom2.getGridToCoordinateSystem())) {
                return false;
            }
        } catch (InvalidGridGeometryException exception) {
            // One geometry doesn't have a transform.
            // Do not compare this properties.
        }
        return true;
    }
    
    /**
     * Returns the coverage name, localized for the supplied locale.
     * Default implementation fallback to the first source coverage.
     */
    public String getName(final Locale locale) {
        final GridCoverage[] sources = getSources();
        if (sources!=null && sources.length!=0) {
            return sources[0].getName(locale);
        }
        return super.getName(locale);
    }
    
    
    
    
    /**
     * The "Resample" operation. See package description for more details.
     *
     * @version $Id: Resampler.java,v 1.18 2003/08/01 10:41:53 desruisseaux Exp $
     * @author Martin Desruisseaux
     */
    static final class Operation extends org.geotools.gp.Operation {
        /**
         * Construct a "Resample" operation.
         */
        public Operation() {
            super("Resample", new ParameterListDescriptorImpl(
                  null,         // the object to be reflected upon for enumerated values.
                  new String[]  // the names of each parameter.
                  {
                      "Source",
                      "InterpolationType",
                      "CoordinateSystem",
                      "GridGeometry"
                  },
                  new Class[]   // the class of each parameter.
                  {
                      GridCoverage.class,
                      Object.class,
                      CoordinateSystem.class,
                      GridGeometry.class
                  },
                  new Object[] // The default values for each parameter.
                  {
                      ParameterListDescriptor.NO_PARAMETER_DEFAULT,
                      "NearestNeighbor",
                      null, // Same as source grid coverage
                      null  // Automatic
                  },
                  null // Defines the valid values for each parameter.
                ));
        }
        
        /**
         * Resample a grid coverage. This method is invoked by
         * {@link GridCoverageProcessor} for the "Resample" operation.
         */
        protected GridCoverage doOperation(final ParameterList  parameters,
                                           final RenderingHints hints)
        {
            GridCoverage   source = (GridCoverage)     parameters.getObjectParameter("Source");
            Interpolation  interp = toInterpolation   (parameters.getObjectParameter("InterpolationType"));
            CoordinateSystem   cs = (CoordinateSystem) parameters.getObjectParameter("CoordinateSystem");
            GridGeometry gridGeom = (GridGeometry)     parameters.getObjectParameter("GridGeometry");
            GridCoverage coverage; // The result to be computed below.
            if (cs == null) {
                cs = source.getCoordinateSystem();
            }
            try {
                coverage = reproject(source, cs, gridGeom, interp, hints);
            } catch (FactoryException exception) {
                throw new CannotReprojectException(Resources.format(
                        ResourceKeys.ERROR_CANT_REPROJECT_$1,
                        source.getName(null)), exception);
            } catch (TransformException exception) {
                throw new CannotReprojectException(Resources.format(
                        ResourceKeys.ERROR_CANT_REPROJECT_$1,
                        source.getName(null)), exception);
            }
            /*
             * Check if we have been able to respect the user request. We may have failed to
             * respect the user specified grid geometry because JAI may not have honored the
             * 'minX', 'minY', 'width' and 'height' image layout setting  (see documentation
             * for AffineDescriptor).  Of course, we may have failed because of a bug in our
             * implementation as well.
             */
            if (gridGeom != null) {
                boolean mismatche = false;
                final GridGeometry actualGeom = coverage.getGridGeometry();
                if (GCSUtilities.hasGridRange(gridGeom)) {
                    mismatche |= !gridGeom.getGridRange().equals(
                                actualGeom.getGridRange());
                }
                if (GCSUtilities.hasTransform(gridGeom)) {
                    mismatche |= !gridGeom.getGridToCoordinateSystem().equals(
                                actualGeom.getGridToCoordinateSystem());
                }
                if (mismatche) {
                    final Locale locale = Locale.getDefault();
                    final LogRecord record = Resources.getResources(locale).getLogRecord(Level.WARNING,
                             ResourceKeys.WARNING_ADJUSTED_GRID_GEOMETRY_$1, coverage.getName(locale));
                    record.setSourceClassName("GridCoverageProcessor");
                    record.setSourceMethodName("doOperation(\"Resample\")");
                    Logger.getLogger("org.geotools.gp").log(record);
                }
            }
            return coverage;
        }
    }
}
