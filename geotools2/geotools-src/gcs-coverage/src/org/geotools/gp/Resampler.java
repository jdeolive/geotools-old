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

// J2SE dependencies
import java.awt.Dimension;
import java.awt.RenderingHints;
import java.awt.geom.Rectangle2D;
import java.awt.geom.AffineTransform;
import java.awt.image.RenderedImage;
import java.awt.image.renderable.ParameterBlock;
import java.util.List;
import java.util.Locale;

// Java Advanced Imaging
import javax.media.jai.JAI;
import javax.media.jai.Warp;
import javax.media.jai.RenderedOp;
import javax.media.jai.PlanarImage;
import javax.media.jai.Interpolation;
import javax.media.jai.ParameterList;
import javax.media.jai.ParameterListDescriptor;
import javax.media.jai.ParameterListDescriptorImpl;
import javax.media.jai.InterpolationNearest;

// Geotools (GCS) dependencies
import org.geotools.cv.Category;
import org.geotools.gc.GridRange;
import org.geotools.gc.GridCoverage;
import org.geotools.gc.GridGeometry;
import org.geotools.cv.CategoryList;
import org.geotools.cv.SampleDimension;

// Geotools (CTS) dependencies
import org.geotools.pt.Envelope;
import org.geotools.cs.CoordinateSystem;
import org.geotools.ct.MathTransform;
import org.geotools.ct.MathTransform2D;
import org.geotools.ct.TransformException;
import org.geotools.ct.MathTransformFactory;
import org.geotools.ct.CoordinateTransformation;
import org.geotools.ct.CoordinateTransformationFactory;

// Resources
import org.geotools.resources.CTSUtilities;
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
 * @version 1.00
 * @author OpenGIS (www.opengis.org)
 * @author Martin Desruisseaux
 */
final class Resampler extends GridCoverage {
    /**
     * Small value for catching rounding errors.
     */
    private static final double EPS = 1E-6;
    
    /**
     * Construct a new grid coverage for the resampler operation.
     *
     * @param source       The source for this grid coverage.
     * @param image        The image.
     * @param cs           The coordinate system.
     * @param envelope     The grid coverage cordinates. The two first dimensions describe
     *                     the image location along <var>x</var> and <var>y</var> axis. The
     *                     other dimensions are optional and may be used to locate the image
     *                     on a vertical axis or on the time axis.
     * @param categories   Category lists which allows for the transformation from pixel
     *                     values to real world geophysics value.
     * @param isGeophysics <code>true</code> if pixel's values are already geophysics values, or
     *                     <code>false</code> if transformation described in <code>categories</code>
     *                     must be applied first.
     */
    private Resampler(final GridCoverage       source,
                      final RenderedImage       image,
                      final CoordinateSystem       cs,
                      final Envelope         envelope,
                      final CategoryList[] categories,
                      final boolean      isGeophysics)
    {
        super(source.getName(null), image, cs, envelope, categories, isGeophysics,
              new GridCoverage[] {source}, null);
    }
    
    /**
     * Create a new coverage with a different coordinate reference system.
     *
     * @param  sourceCoverage The source grid coverage.
     * @param  targetCS Coordinate system for the new grid coverage, or <code>null</code>.
     * @param  targetGridGeometry The target grid geometry, or <code>null</code> for default.
     * @param  interpolation The interpolation to use.
     * @param  factory The transformation factory to use.
     * @return The new grid coverage, or <code>sourceCoverage</code> if no resampling was needed.
     * @throws TransformException if the grid coverage can't be reprojected.
     */
    public static GridCoverage reproject(      GridCoverage             sourceCoverage,
                                         final CoordinateSystem               targetCS,
                                         final GridGeometry         targetGridGeometry,
                                         final Interpolation             interpolation,
                                         final CoordinateTransformationFactory factory)
        throws TransformException
    {
        /*
         * If the user request a new grid geometry with the
         * same coordinate system, and if the grid geometry
         * is equivalents to a simple extraction of a sub-area,
         * delegate the work to the 'scale' method.
         */
        if (targetGridGeometry!=null && targetCS==null) {
            final MathTransform2D transform = targetGridGeometry.getGridToCoordinateSystem2D();
            if (transform instanceof AffineTransform) {
                final AffineTransform at = (AffineTransform) transform;
                if ((at.getType() & ~(AffineTransform.TYPE_IDENTITY    |
                AffineTransform.TYPE_TRANSLATION |
                AffineTransform.TYPE_MASK_SCALE))==0) {
                    // NO flip, no rotation, no shear.
                    // TODO: we could make this algorithm slightly
                    //       more general by accepting flips and 90°
                    //       rotation, but we need to change 'scale'
                    //       in order to accept AffineTransform objects.
                    //       In fact, we NEED to do this since this patch
                    //       is wrong: GridCoverage may inverse the 'y' axis.
                    final GridRange range = targetGridGeometry.getGridRange();
                    final Dimension  size = new Dimension(range.getLength(0), range.getLength(1));
                    Rectangle2D    bounds = new Rectangle2D.Double(range.getLower(0), range.getLower(1), size.width, size.height);
                    bounds = XAffineTransform.transform(at, bounds, bounds);
                    return scale(sourceCoverage, bounds, size);
                }
            }
        }
        /*
         * If the source coverage is already a projected one,
         * go up in the chain until the source grid coverage.
         */
        while (sourceCoverage instanceof Resampler) {
            final GridCoverage[] sources = sourceCoverage.getSources();
            if (sources.length!=1) throw new AssertionError(sources.length);
            sourceCoverage = sources[0];
        }
        /*
         * Perform a first argument check. The
         * most trivial cases are handled now.
         */
        final CoordinateSystem sourceCS = sourceCoverage.getCoordinateSystem();
        if (sourceCS==targetCS && targetGridGeometry==null) { // May be both null.
            return sourceCoverage;
        }
        if (sourceCS==null || targetCS==null) {
            throw new CannotReprojectException(Resources.format(ResourceKeys.ERROR_UNSPECIFIED_COORDINATE_SYSTEM));
        }
        if (sourceCS.equivalents(targetCS) && targetGridGeometry==null) {
            return sourceCoverage;
        }
        if (targetGridGeometry!=null) {
            // TODO: Implement the "GridGeometry" argument.
            throw new CannotReprojectException("'GridGeometry' parameter not yet implemented");
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
        final CategoryList[] categories = sourceCoverage.getCategoryLists();
        /*
         * The projection are usually applied on floating-point values, in order
         * to gets maximal precision and to handle correctly the special case of
         * NaN values. However, we can apply the projection on integer values if
         * one of the following conditions is meet:
         *
         *    1) The interpolation type is "nearest neighbor". Since this
         *       is not really an interpolation, the "NaN" issue vanish.
         *    2) The coverage have at most one category, and this category is
         *       quantifiable. In this case, the image should not contains
         *       any NaN value since there is no category for handling NaN.
         *
         * If one of those conditions apply, then we will check if the indexed image
         * is the "source" image  (i.e. the floating-point image is derived from the
         * indexed image, and not the converse). If the indexed image is the source,
         * then we will project this image as an optimization instead of the floating
         * point (also called "geophysics") image.
         */
        boolean geophysics = true;
        if (interpolation instanceof InterpolationNearest || areLinears(categories)) {
            final List sources = sourceCoverage.getRenderedImage(true).getSources();
            if (sources!=null) {
                final RenderedImage indexed = sourceCoverage.getRenderedImage(false);
                if (sources.contains(indexed)) geophysics = false;
            }
        }
        /*
         * Gets the target image as a {@link RenderedOp}.  The source image is
         * initially wrapped into in a "Null" operation. Later, we will change
         * this "Null" operation into a "Warp" operation.  We can't use "Warp"
         * now because we will know the envelope only after creating the GridCoverage.
         * Note: RenderingHints contain mostly indications about tiles layout.
         */
        final PlanarImage sourceImage = PlanarImage.wrapRenderedImage(sourceCoverage.getRenderedImage(geophysics));
        final RenderedOp  targetImage = JAI.create("Null", sourceImage, ImageUtilities.getRenderingHints(sourceImage));
        final GridCoverage targetCoverage;
        /*
         * Gets the math transform.  According our own GridCoverage
         * specification, only the two first ordinates apply to the
         * image. Other ordinates are discarted.  The caller should
         * have make sure that there is no dependency   between the
         * two first ordinates and the other.
         *
         * Once the math transform is know, we compute the target
         * envelope and the warp transform.
         */
        final MathTransformFactory     mathFactory = factory.getMathTransformFactory();
        final CoordinateTransformation transformation;
        final MathTransform            transform;
        final MathTransform2D          transform2D;
        try {
            transformation = factory.createFromCoordinateSystems(sourceCS, targetCS);
            transform      = transformation.getMathTransform();
            transform2D    = (MathTransform2D) mathFactory.createSubMathTransform(0, 2, transform);
        } catch (ClassCastException exception) {
            // This catch clause is here in case the (MathTransform2D) cast failed.
            // It should not happen, except maybe with some implementation outside
            // the Geotools package. Even in the later case, it should be unusual.
            throw new TransformException(Resources.format(
                    ResourceKeys.ERROR_NO_TRANSFORM2D_AVAILABLE), exception);
        }
        /*
         * Gets the source and target envelope. It is difficult to check if the first
         * two dimensions are really independent from other dimensions.   However, if
         * we get the same 2-dimensional envelope no matter if we took in account the
         * extra dimensions or not, then we will assume that projecting the image with
         * a MathTransform2D is safe enough.
         */
        final Envelope sourceEnvelope   = sourceCoverage.getEnvelope();
        final Envelope sourceEnvelope2D = sourceEnvelope.getSubEnvelope(0,2);
        final Envelope targetEnvelope   = CTSUtilities.transform(transform,   sourceEnvelope);
        final Envelope targetEnvelope2D = CTSUtilities.transform(transform2D, sourceEnvelope2D);
        if (!targetEnvelope.getSubEnvelope(0,2).equals(targetEnvelope2D)) {
            throw new TransformException(Resources.format(
                    ResourceKeys.ERROR_NO_TRANSFORM2D_AVAILABLE));
        }
        // TODO: We should do here a special optimization for the case transform2D.isIdentity().
        //       Note that the grid geometry may be different if the 'targetGridGeometry!=null'.
        /*
         * Construct the target grid coverage, and then construct the warp transform.  We
         * had to set the warp transform last because the construction of 'WarpTransform'
         * requires the geometry of the target grid coverage. The trick was to initialize
         * the target image with a null operation, and change the operation here.
         */
        targetCoverage  = new Resampler(sourceCoverage, targetImage, targetCS, targetEnvelope, categories, geophysics);
        final Warp warp = new WarpTransform(sourceCoverage.getGridGeometry(), transform2D,
        targetCoverage.getGridGeometry(), mathFactory);
        final ParameterBlock param = new ParameterBlock().addSource(sourceImage).add(warp).add(interpolation);
        targetImage.setParameterBlock(param); // Must be invoked before setOperationName.
        targetImage.setOperationName("Warp");
        
        final RenderingHints hints = targetImage.getRenderingHints();
        hints.add(ImageUtilities.getRenderingHints(targetImage));
        targetImage.setRenderingHints(hints);
        
        assert sourceCoverage.getCoordinateSystem().equivalents(transformation.getSourceCS());
        assert targetCoverage.getCoordinateSystem().equivalents(transformation.getTargetCS());
        assert targetGridGeometry!=null || targetImage.getBounds().equals(sourceImage.getBounds());
        
        return targetCoverage;
    }
    
    /**
     * Scale and/or crop a grid coverage in order to keep only a subarea.
     * The resulting coverage will use the same coordinate system than the
     * source coverage.
     *
     * @param  sourceCoverage The source grid coverage.
     * @param  area The subarea to extract, in the coordinate system units.
     * @param  size The size to scale image to, or <code>null</code> if the
     *         image should not be scaled.
     * @return The resulting grid coverage.
     */
    public static GridCoverage scale(final GridCoverage sourceCoverage,
                                     Rectangle2D area, final Dimension size)
        throws TransformException
    {
        final MathTransform2D  gridToCS = sourceCoverage.getGridGeometry().getGridToCoordinateSystem2D();
        final RenderedImage sourceImage = sourceCoverage.getRenderedImage(true);
        RenderedImage image=sourceImage;
        /*
         * Get the grid indices for the destination image.  If the destination indices are
         * differents from the source indices (i.e. if the user is requesting a subarea of
         * source coverage), then extract the requested subarea using the "Crop" operation.
         */
        boolean changed=false;
        int tmp;
        int xmin = image.getMinX();
        int ymin = image.getMinY();
        int xmax = image.getWidth()  + xmin;
        int ymax = image.getHeight() + ymin;
        final Rectangle2D bounds = CTSUtilities.transform((MathTransform2D)gridToCS.inverse(), area, null);
        tmp = (int)Math.floor(bounds.getMinX() + EPS); if (tmp>xmin) {xmin=tmp; changed=true;}
        tmp = (int)Math.floor(bounds.getMinY() + EPS); if (tmp>ymin) {ymin=tmp; changed=true;}
        tmp = (int)Math.ceil(bounds.getMaxX() - EPS); if (tmp<xmax) {xmax=tmp; changed=true;}
        tmp = (int)Math.ceil(bounds.getMaxY() - EPS); if (tmp<ymax) {ymax=tmp; changed=true;}
        bounds.setRect(xmin, ymin, xmax-xmin, ymax-ymin);
        if (changed) {
            image = JAI.create("Crop", new ParameterBlock().addSource(image)
                        .add((float)bounds.getX    ()).add((float)bounds.getY())
                        .add((float)bounds.getWidth()).add((float)bounds.getHeight()));
            area = CTSUtilities.transform(gridToCS, bounds, bounds);
        }
        /*
         * If the requested image size is different from current image
         * size, then scale the image using the "Scale" operation.
         */
        if (size!=null && (size.width!=image.getWidth() || size.height!=image.getHeight())) {
            throw new UnsupportedOperationException("Not yet implemented"); // TODO
        }
        /*
         * Returns the resulting grid coverage.
         */
        if (image==sourceImage) {
            return sourceCoverage;
        }
        final Envelope envelope = sourceCoverage.getEnvelope();
        envelope.setRange(0, area.getMinX(), area.getMaxX());
        envelope.setRange(1, area.getMinY(), area.getMaxY());
        return new GridCoverage(sourceCoverage.getName(null),
                                image, sourceCoverage.getCoordinateSystem(),
                                envelope, sourceCoverage.getCategoryLists(), true,
                                new GridCoverage[] {sourceCoverage}, null);
    }
    
    /**
     * Check if the mapping between pixel values and geophysics value
     * is a linear relation for all bands in the specified categories.
     */
    private static boolean areLinears(final CategoryList[] categories) {
        for (int i=categories.length; --i>=0;) {
            final CategoryList list = categories[i];
            if (list==null) {
                // If there is no categories,  we assume that there is
                // no classification. It should be okay to interpolate
                // pixel values.
                continue;
            }
            if (list.size()==1) {
                final Category category = list.get(0);
                if (category.isQuantitative() && category.getClass().equals(Category.class)) {
                    // If there are categories, we require that there is only
                    // one category and this category must be translatable in
                    // numbers using a linear relation.
                    continue;
                }
            }
            return false;
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
     * @version 1.0
     * @author Martin Desruisseaux
     */
    static final class Operation extends org.geotools.gp.Operation {
        /**
         * The coordinate transform factory to use when
         * coordinate transformation are required.
         */
        private final CoordinateTransformationFactory factory;
        
        /**
         * Construct a "Resample" operation.
         */
        public Operation(final CoordinateTransformationFactory factory) {
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
            this.factory = factory;
        }
        
        /**
         * Resample a grid coverage. This method is invoked by
         * {@link GridCoverageProcessor} for the "Resample" operation.
         */
        protected GridCoverage doOperation(final ParameterList parameters) {
            GridCoverage   source = (GridCoverage)     parameters.getObjectParameter("Source");
            Interpolation  interp = toInterpolation   (parameters.getObjectParameter("InterpolationType"));
            CoordinateSystem   cs = (CoordinateSystem) parameters.getObjectParameter("CoordinateSystem");
            GridGeometry gridGeom = (GridGeometry)     parameters.getObjectParameter("GridGeometry");
            try {
                return reproject(source, (cs!=null) ? cs : source.getCoordinateSystem(), gridGeom, interp, factory);
            } catch (TransformException exception) {
                throw new CannotReprojectException(Resources.format(
                        ResourceKeys.ERROR_CANT_REPROJECT_$1,
                        source.getName(null)), exception);
            }
        }
    }
}
