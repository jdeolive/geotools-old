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
package org.geotools.gc;

// Images
import java.awt.image.Raster;
import java.awt.image.DataBuffer;
import java.awt.image.RenderedImage;
import java.awt.image.WritableRenderedImage;
import java.awt.image.renderable.ParameterBlock;

// Java Advanced Imaging
import javax.media.jai.JAI;
import javax.media.jai.RenderedOp;
import javax.media.jai.PlanarImage;
import javax.media.jai.ImageFunction;
import javax.media.jai.Interpolation;
import javax.media.jai.InterpolationNearest;
import javax.media.jai.util.CaselessStringKey;
import javax.media.jai.remote.SerializableRenderedImage;

// Geometry
import java.awt.Point;
import java.awt.Shape;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.geom.Point2D;
import java.awt.geom.Dimension2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.AffineTransform;
import org.geotools.resources.XAffineTransform;
import org.geotools.resources.XDimension2D;

// Collections
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

// Events
import java.awt.event.WindowEvent;
import java.awt.event.WindowAdapter;

// Miscellaneous
import java.util.Date;
import java.util.Arrays;
import java.text.DateFormat;
import java.text.FieldPosition;
import java.rmi.RemoteException;
import java.rmi.ServerException;
import java.rmi.server.RemoteObject;
import java.io.NotSerializableException;
import java.io.StringWriter;
import java.io.IOException;

// OpenGIS dependencies
import org.opengis.gc.GC_GridRange;
import org.opengis.gc.GC_GridPacking;
import org.opengis.gc.GC_GridGeometry;
import org.opengis.gc.GC_GridCoverage;

// Geotools dependencies
import org.geotools.pt.Envelope;
import org.geotools.pt.CoordinatePoint;
import org.geotools.pt.MismatchedDimensionException;
import org.geotools.cs.CoordinateSystem;
import org.geotools.cs.AxisOrientation;
import org.geotools.ct.MathTransform;
import org.geotools.ct.MathTransform2D;
import org.geotools.ct.TransformException;
import org.geotools.cv.Coverage;
import org.geotools.cv.Category;
import org.geotools.cv.SampleDimension;
import org.geotools.cv.CannotEvaluateException;
import org.geotools.cv.PointOutsideCoverageException;

// Resources
import org.geotools.io.LineWriter;
import org.geotools.util.WeakHashSet;
import org.geotools.resources.XArray;
import org.geotools.resources.Utilities;
import org.geotools.resources.CTSUtilities;
import org.geotools.resources.GCSUtilities;
import org.geotools.resources.gcs.Resources;
import org.geotools.resources.gcs.ResourceKeys;


/**
 * Basic access to grid data values. Grid coverages are backed by
 * {@link RenderedImage}. Each band in an image is represented as
 * a sample dimension.
 * <br><br>
 * Grid coverages are usually two-dimensional. However, their envelope may
 * have more than two dimensions.  For example, a remote sensing image may
 * be valid only over some time range (the time of satellite pass over the
 * observed area). Envelope for such grid coverage may have three dimensions:
 * the two usual ones (horizontal extends along <var>x</var> and <var>y</var>),
 * and a third one for start time and end time (time extends along <var>t</var>).
 *
 * @version $Id: GridCoverage.java,v 1.11 2003/02/14 23:38:13 desruisseaux Exp $
 * @author <A HREF="www.opengis.org">OpenGIS</A>
 * @author Martin Desruisseaux
 *
 * @see GC_GridCoverage
 */
public class GridCoverage extends Coverage {
    /**
     * Axis orientation of image's coordinate systems. In most images, <var>x</var> values are
     * increasing toward the right (<code>EAST</code>)  and <var>y</var> values are increasing
     * toward the bottom (<code>SOUTH</code>). This is different to many geographic coordinate
     * systems, which have <var>y</var> values increasing <code>NORTH</code>. The grid coverage
     * constructor will compare the geographic axis orientations to this
     * <code>IMAGE_ORIENTATION</code> and inverse the <var>y</var> axis if necessary. The axis
     * inversions are handle by {@link GridGeometry#getGridToCoordinateSystem()}.
     */
    private static final AxisOrientation[] IMAGE_ORIENTATION = {
        AxisOrientation.EAST,
        AxisOrientation.SOUTH
    };
    
    /**
     * Pool of created object. Objects in this pool must be immutable.
     * Those objects will be shared among many grid coverages.
     */
    private static final WeakHashSet pool = new WeakHashSet();
    
    /**
     * An empty list of grid coverage.
     */
    private static final GridCoverage[] EMPTY_LIST = new GridCoverage[0];
    
    /**
     * Sources grid coverage.
     */
    private final GridCoverage[] sources;

    /**
     * A grid coverage using the sample dimensions <code>SampleDimension.inverse</code>.
     * This object is constructed and returned by {@link #geophysics}. Constructed when
     * first needed. May also appears also in the <code>sources</code> list.
     */
    private GridCoverage inverse;
    
    /**
     * The raster data.
     */
    protected final PlanarImage image;
    
    /**
     * The grid geometry.
     */
    protected final GridGeometry gridGeometry;
    
    /**
     * The image's envelope. This envelope must have at least two
     * dimensions. It may have more dimensions if the image have
     * some extend in other dimensions (for example a depth, or
     * a start and end time).
     */
    private final Envelope envelope;
    
    /**
     * List of sample dimension information for the grid coverage.
     * For a grid coverage, a sample dimension is a band. The sample dimension information
     * include such things as description, data type of the value (bit, byte, integer...),
     * the no data values, minimum and maximum values and a color table if one is associated
     * with the dimension. A coverage must have at least one sample dimension.
     */
    private final SampleDimension[] sampleDimensions;

    /**
     * <code>true</code> is all sample in the image are geophysics values.
     */
    private final boolean isGeophysics;
    
    /**
     * Construct a new grid coverage with the same parameter than the specified
     * coverage. This constructor is useful when creating a coverage with
     * identical data, but in which some method has been overriden in order to
     * process data differently (e.g. interpolating them).
     *
     * @param coverage The source grid coverage.
     */
    protected GridCoverage(final GridCoverage coverage) {
        super(coverage);
        image            = coverage.image;
        gridGeometry     = coverage.gridGeometry;
        envelope         = coverage.envelope;
        sampleDimensions = coverage.sampleDimensions;
        isGeophysics     = coverage.isGeophysics;
        sources          = new GridCoverage[] {coverage};
    }
    
    /**
     * Construt a grid coverage from an image function.
     *
     * @param name         The grid coverage name.
     * @param function     The image function.
     * @param cs           The coordinate system. This specifies the coordinate system used
     *                     when accessing a grid coverage with the "evaluate" methods.  The
     *                     number of dimensions must matches the number of dimensions for
     *                     the grid range in <code>gridGeometry</code>.
     * @param gridGeometry The grid geometry. The grid range must contains the expected
     *                     image size (width and height).
     * @param bands        Sample dimensions for each image band, or <code>null</code> for
     *                     default sample dimensions. If non-null, then this array's length
     *                     must matches the number of bands in <code>image</code>.
     * @param properties The set of properties for this coverage, or <code>null</code>
     *        if there is none. "Properties" in <em>Java Advanced Imaging</em> is what
     *        OpenGIS calls "Metadata".  There is no <code>getMetadataValue(...)</code>
     *        method in this implementation. Use {@link #getProperty} instead. Keys may
     *        be {@link String} or {@link CaselessStringKey} objects,  while values may
     *        be any {@link Object}.
     *
     * @throws MismatchedDimensionException If the grid range's dimension
     *         is not the same than the coordinate system's dimension.
     */
    public GridCoverage(final String             name, final ImageFunction    function,
                        final CoordinateSystem     cs, final GridGeometry gridGeometry,
                        final SampleDimension[] bands, final Map properties)
        throws MismatchedDimensionException
    {
        this(name, getImage(function, gridGeometry),
             cs, gridGeometry, null, bands, null, properties);
    }
    
    /**
     * Create an image from an image function.  Translation and scale
     * factors are fetched from the grid geometry, which must have an
     * affine transform.
     *
     * @task TODO: We could support shear in affine transform.
     */
    private static PlanarImage getImage(final ImageFunction function,
                                        final GridGeometry gridGeometry)
    {
        final MathTransform transform = gridGeometry.getGridToCoordinateSystem2D();
        if (!(transform instanceof AffineTransform)) {
            throw new IllegalArgumentException(org.geotools.resources.cts.Resources.format(
                    org.geotools.resources.cts.ResourceKeys.ERROR_NOT_AN_AFFINE_TRANSFORM));
        }
        final AffineTransform at = (AffineTransform) transform;
        if (at.getShearX()!=0 || at.getShearY()!=0) {
            // TODO: We may support that in a future version.
            //       1) Create a copy with shear[X/Y] set to 0. Use the copy.
            //       2) Compute the residu with createInverse() and concatenate().
            //       3) Apply the residu with JAI.create("Affine").
            throw new IllegalArgumentException("Shear and rotation not supported");
        }
        final double xScale =  at.getScaleX();
        final double yScale =  at.getScaleY();
        final double xTrans = -at.getTranslateX()/xScale;
        final double yTrans = -at.getTranslateY()/yScale;
        final GridRange      range = gridGeometry.getGridRange();
        final ParameterBlock param = new ParameterBlock().add(function)
                                                         .add(range.getLength(0)) // width
                                                         .add(range.getLength(1)) // height
                                                         .add((float) xScale)
                                                         .add((float) yScale)
                                                         .add((float) xTrans)
                                                         .add((float) yTrans);
        return JAI.create("ImageFunction", param);
    }
    
    /**
     * Construct a grid coverage with the specified envelope.
     * Pixels will not be classified in any category.
     *
     * @param name         The grid coverage name.
     * @param image        The image.
     * @param cs           The coordinate system. This specifies the coordinate system used
     *                     when accessing a grid coverage with the "evaluate" methods.  The
     *                     number of dimensions must matches the number of dimensions for
     *                     <code>envelope</code>.
     * @param envelope     The grid coverage cordinates. This envelope must have at least two
     *                     dimensions.   The two first dimensions describe the image location
     *                     along <var>x</var> and <var>y</var> axis. The other dimensions are
     *                     optional and may be used to locate the image on a vertical axis or
     *                     on the time axis.
     *
     * @throws MismatchedDimensionException If the envelope's dimension
     *         is not the same than the coordinate system's dimension.
     */
    public GridCoverage(final String         name, final RenderedImage  image,
                        final CoordinateSystem cs, final Envelope    envelope)
        throws MismatchedDimensionException
    {
        this(name, image, cs, envelope, null, null, null);
    }
    
    /**
     * Construct a grid coverage with the specified envelope and sample dimensions.
     *
     * @param name         The grid coverage name.
     * @param image        The image.
     * @param cs           The coordinate system. This specifies the coordinate system used
     *                     when accessing a grid coverage with the "evaluate" methods.  The
     *                     number of dimensions must matches the number of dimensions for
     *                     <code>envelope</code>.
     * @param envelope     The grid coverage cordinates. This envelope must have at least two
     *                     dimensions.   The two first dimensions describe the image location
     *                     along <var>x</var> and <var>y</var> axis. The other dimensions are
     *                     optional and may be used to locate the image on a vertical axis or
     *                     on the time axis.
     * @param sampleDim    Sample dimensions for each image band, or <code>null</code> for
     *                     default sample dimensions. If non-null, then this array's length
     *                     must matches the number of bands in <code>image</code>.
     * @param sources      The sources for this grid coverage, or <code>null</code> if none.
     * @param properties The set of properties for this coverage, or <code>null</code>
     *        if there is none. "Properties" in <em>Java Advanced Imaging</em> is what
     *        OpenGIS calls "Metadata".  There is no <code>getMetadataValue(...)</code>
     *        method in this implementation. Use {@link #getProperty} instead. Keys may
     *        be {@link String} or {@link CaselessStringKey} objects,  while values may
     *        be any {@link Object}.
     *
     * @throws MismatchedDimensionException If the envelope's dimension
     *         is not the same than the coordinate system's dimension.
     * @param  IllegalArgumentException if the number of bands differs
     *         from the number of sample dimensions.
     */
    public GridCoverage(final String             name, final RenderedImage    image,
                        final CoordinateSystem     cs, final Envelope      envelope,
                        final SampleDimension[] bands, final GridCoverage[] sources,
                        final Map properties)
        throws MismatchedDimensionException
    {
        this(name, PlanarImage.wrapRenderedImage(image), cs, null,
             (Envelope)envelope.clone(), bands, sources, properties);
    }
    
    /**
     * Construct a grid coverage with the specified transform and sample dimension.
     *
     * @param name         The grid coverage name.
     * @param image        The image.
     * @param cs           The coordinate system. This specifies the coordinate system used
     *                     when accessing a grid coverage with the "evaluate" methods.  The
     *                     number of dimensions must matches the number of dimensions for
     *                     <code>gridToCS</code>.
     * @param gridToCS     The math transform from grid to coordinate system.
     * @param sampleDim    Sample dimensions for each image band, or <code>null</code> for
     *                     default sample dimensions. If non-null, then this array's length
     *                     must matches the number of bands in <code>image</code>.
     * @param sources      The sources for this grid coverage, or <code>null</code> if none.
     * @param properties The set of properties for this coverage, or <code>null</code>
     *        if there is none. "Properties" in <em>Java Advanced Imaging</em> is what
     *        OpenGIS calls "Metadata".  There is no <code>getMetadataValue(...)</code>
     *        method in this implementation. Use {@link #getProperty} instead. Keys may
     *        be {@link String} or {@link CaselessStringKey} objects,  while values may
     *        be any {@link Object}.
     *
     * @throws MismatchedDimensionException If the transform's dimension
     *         is not the same than the coordinate system's dimension.
     * @param  IllegalArgumentException if the number of bands differs
     *         from the number of sample dimensions.
     */
    public GridCoverage(final String             name, final RenderedImage    image,
                        final CoordinateSystem     cs, final MathTransform gridToCS,
                        final SampleDimension[] bands, final GridCoverage[] sources,
                        final Map properties)
        throws MismatchedDimensionException
    {
        this(name, PlanarImage.wrapRenderedImage(image), cs,
             new GridGeometry(null, gridToCS),
             null, bands, sources, properties);
    }
    
    /**
     * Construct a grid coverage. This private constructor expect an envelope
     * (<code>envelope</code>) or a grid geometry (<code>gridGeometry</code>).
     * <strong>One and only one of those argument</strong> should be non-null.
     * The null arguments will be computed from the non-null argument.
     */
    private GridCoverage(final String               name,
                         final PlanarImage         image,
                         final CoordinateSystem       cs,
                               GridGeometry gridGeometry, // ONE and  only  one of those
                               Envelope         envelope, // two arguments should be non-null.
                         final SampleDimension[] sdBands,
                         final GridCoverage[]    sources,
                         final Map            properties)
        throws MismatchedDimensionException
    {
        super(name, cs, image, properties);
        if ((gridGeometry==null) == (envelope==null)) {
            // Should not happen
            throw new AssertionError();
        }
        if (sources != null) {
            this.sources = (GridCoverage[]) sources.clone();
        } else {
            this.sources = EMPTY_LIST;
        }
        this.image = image;
        /*
         * Check sample dimensions. The number of SampleDimensions must matches the
         * number of image's bands (this is checked by GridSampleDimension.create).
         */
        sampleDimensions = new SampleDimension[image.getSampleModel().getNumBands()];
        isGeophysics     = GridSampleDimension.create(image, sdBands, sampleDimensions);
        /*
         * Constructs the grid range and the envelope if they were not explicitly provided.
         * The envelope computation (if needed)  requires  a valid 'gridToCoordinateSystem'
         * transform in the GridGeometry.  Otherwise, no transform are required.  The range
         * will be inferred from the image size, if needed. In any cases, the envelope must
         * be non-empty and its dimension must matches the coordinate system's dimension. A
         * pool of shared envelopes will be used in order to recycle existing envelopes.
         */
        final GridRange gridRange;
        if (GCSUtilities.hasGridRange(gridGeometry)) {
            gridRange = gridGeometry.getGridRange();
        } else {
            gridRange = new GridRange(image, cs.getDimension());
            if (GCSUtilities.hasTransform(gridGeometry)) {
                gridGeometry=new GridGeometry(gridRange, gridGeometry.getGridToCoordinateSystem());
            }
        }
        // Check the GridRange...
        if (true) {
            final String error = checkConsistency(image, gridRange);
            if (error != null) {
                throw new IllegalArgumentException(error);
            }
        }
        // Check the Envelope...
        if (envelope == null) {
            envelope = gridGeometry.getEnvelope();
        }
        final int dimension = envelope.getDimension();
        if (envelope.isEmpty() || dimension<2) {
            throw new IllegalArgumentException(Resources.format(ResourceKeys.ERROR_EMPTY_ENVELOPE));
        }
        if (dimension != cs.getDimension()) {
            throw new MismatchedDimensionException(cs, envelope);
        }
        this.envelope = (Envelope)pool.canonicalize(envelope);
        /*
         * Compute the grid geometry. The math transform will be computed from the envelope.
         * A pool of shared grid geometries will be used in order to recycle existing objects.
         *
         * Note: Should we invert some axis? For example, the 'y' axis is often inversed
         *       (since image use a downward 'y' axis). If all source grid coverages use
         *       the same axis orientations, we will reuse those orientations. Otherwise,
         *       we will use default orientations where only the 'y' axis is inversed.
         */
        if (gridGeometry == null) {
            boolean[] inverse = null;
            if (sources != null) {
                for (int i=0; i<sources.length; i++) {
                    boolean check[] = sources[i].gridGeometry.areAxisInverted();
                    check = XArray.resize(check, dimension);
                    if (inverse!=null) {
                        if (!Arrays.equals(check, inverse)) {
                            inverse = null;
                            break;
                        }
                    } else {
                        inverse = check;
                    }
                }
            }
            if (inverse == null) {
                inverse = new boolean[dimension];
                for (int i=Math.min(IMAGE_ORIENTATION.length, dimension); --i>=0;) {
                    final AxisOrientation toInverse = IMAGE_ORIENTATION[i].inverse();
                    inverse[i] = toInverse.equals(cs.getAxis(1).orientation);
                }
            }
            gridGeometry = new GridGeometry(gridRange, envelope, inverse);
        }
        this.gridGeometry = (GridGeometry)pool.canonicalize(gridGeometry);
    }

    /**
     * Check if the boundix box of the specified image is consistents with the specified
     * grid range. If an inconsistency has been found, then an error string is returned.
     * This string will be typically used as a message in an exception to be thrown.
     * <br><br>
     * Note that a succesful check at construction time may fails later if the image is part
     * of a JAI chain (i.e. is a {@link RenderedOp}) and its bounds has been edited (i.e the
     * image node as been re-rendered). Since <code>GridCoverage</code> are immutable by design,
     * we are not allowed to propagate the image change here. The {@link #getGridGeometry} method
     * will thrown an {@link IllegalStateException} in this case.
     */
    private static String checkConsistency(final RenderedImage image, final GridRange range) {
        for (int i=0; i<=1; i++) {
            final int min, length;
            final Object label;
            switch (i) {
                case 0:  min=image.getMinX(); length=image.getWidth();  label="\"X\"";        break;
                case 1:  min=image.getMinY(); length=image.getHeight(); label="\"Y\"";        break;
                default: min=0;               length=1;                 label=new Integer(i); break;
            }
            if (range.getLower(i)!=min || range.getLength(i)!=length) {
                return Resources.format(ResourceKeys.ERROR_BAD_GRID_RANGE_$1, label);
            }
        }
        return null;
    }
    
    /**
     * Returns <code>true</code> if grid data can be edited. The default
     * implementation returns <code>true</code>  if  {@link #image} is an
     * instance of {@link WritableRenderedImage}.
     *
     * @see GC_GridCoverage#isDataEditable
     */
    public boolean isDataEditable() {
        return (image instanceof WritableRenderedImage);
    }
    
    /**
     * Returns the source data for a grid coverage. If the <code>GridCoverage</code>
     * was produced from an underlying dataset, the returned list is an empty list.
     * If the <code>GridCoverage</code> was produced using
     * {@link org.geotools.gp.GridCoverageProcessor} then it should return the source
     * grid coverage of the one used as input to <code>GridCoverageProcessor</code>.
     * In general the <code>getSource()</code> method is intended to return the original
     * <code>GridCoverage</code> on which it depends. This is intended to allow applications
     * to establish what <code>GridCoverage</code>s will be affected when others are updated,
     * as well as to trace back to the "raw data".
     */
    public GridCoverage[] getSources() {
        return (GridCoverage[]) sources.clone();
    }
    
    /**
     * Returns information for the grid coverage geometry. Grid geometry
     * includes the valid range of grid coordinates and the georeferencing.
     *
     * @see GC_GridCoverage#getGridGeometry
     */
    public GridGeometry getGridGeometry() {
        final String error = checkConsistency(image, gridGeometry.getGridRange());
        if (error != null) {
            throw new IllegalStateException(error);
        }
        return gridGeometry;
    }
    
    /**
     * Returns The bounding box for the coverage domain in coordinate
     * system coordinates.
     */
    public Envelope getEnvelope() {
        return (Envelope) envelope.clone();
    }
    
    /**
     * Retrieve sample dimension information for the coverage.
     * For a grid coverage, a sample dimension is a band. The sample dimension information
     * include such things as description, data type of the value (bit, byte, integer...),
     * the no data values, minimum and maximum values and a color table if one is associated
     * with the dimension. A coverage must have at least one sample dimension.
     */
    public SampleDimension[] getSampleDimensions() {
        return (SampleDimension[]) sampleDimensions.clone();
    }

    /**
     * Returns the interpolation used for all <code>evaluate(...)</code> methods.
     * The default implementation returns {@link InterpolationNearest}.
     *
     * @return The interpolation.
     */
    public Interpolation getInterpolation() {
        return Interpolation.getInstance(Interpolation.INTERP_NEAREST);
    }
    
    /**
     * Returns a sequence of integer values for a given point in the coverage.
     *
     * @param  coord The coordinate point where to evaluate.
     * @param  dest  An array in which to store values, or <code>null</code>.
     * @return An array containing values.
     * @throws CannotEvaluateException if the values can't be computed at the specified coordinate.
     *         More specifically, {@link PointOutsideCoverageException} is thrown if the evaluation
     *         failed because the input point has invalid coordinates.
     */
    public int[] evaluate(final CoordinatePoint coord, final int[] dest)
        throws CannotEvaluateException
    {
        return evaluate(new Point2D.Double(coord.ord[0], coord.ord[1]), dest);
    }
    
    /**
     * Returns a sequence of float values for a given point in the coverage.
     *
     * @param  coord The coordinate point where to evaluate.
     * @param  dest  An array in which to store values, or <code>null</code>.
     * @return An array containing values.
     * @throws CannotEvaluateException if the values can't be computed at the specified coordinate.
     *         More specifically, {@link PointOutsideCoverageException} is thrown if the evaluation
     *         failed because the input point has invalid coordinates.
     */
    public float[] evaluate(final CoordinatePoint coord, final float[] dest)
        throws CannotEvaluateException
    {
        return evaluate(new Point2D.Double(coord.ord[0], coord.ord[1]), dest);
    }
    
    /**
     * Returns a sequence of double values for a given point in the coverage.
     *
     * @param  coord The coordinate point where to evaluate.
     * @param  dest  An array in which to store values, or <code>null</code>.
     * @return An array containing values.
     * @throws CannotEvaluateException if the values can't be computed at the specified coordinate.
     *         More specifically, {@link PointOutsideCoverageException} is thrown if the evaluation
     *         failed because the input point has invalid coordinates.
     */
    public double[] evaluate(final CoordinatePoint coord, final double[] dest)
        throws CannotEvaluateException
    {
        return evaluate(new Point2D.Double(coord.ord[0], coord.ord[1]), dest);
    }
    
    /**
     * Returns a sequence of integer values for a given two-dimensional point in the coverage.
     *
     * @param  coord The coordinate point where to evaluate.
     * @param  dest  An array in which to store values, or <code>null</code>.
     * @return An array containing values.
     * @throws CannotEvaluateException if the values can't be computed at the specified coordinate.
     *         More specifically, {@link PointOutsideCoverageException} is thrown if the evaluation
     *         failed because the input point has invalid coordinates.
     */
    public int[] evaluate(final Point2D coord, final int[] dest)
        throws CannotEvaluateException
    {
        final Point2D pixel = gridGeometry.inverseTransform(coord);
        final double fx = pixel.getX();
        final double fy = pixel.getY();
        if (!Double.isNaN(fx) && !Double.isNaN(fy)) {
            final int x = (int)Math.round(fx);
            final int y = (int)Math.round(fy);
            if (image.getBounds().contains(x,y)) { // getBounds() returns a cached instance.
                return image.getTile(image.XToTileX(x), image.YToTileY(y)).getPixel(x, y, dest);
            }
        }
        throw new PointOutsideCoverageException(coord);
    }
    
    /**
     * Returns a sequence of float values for a given two-dimensional point in the coverage.
     *
     * @param  coord The coordinate point where to evaluate.
     * @param  dest  An array in which to store values, or <code>null</code>.
     * @return An array containing values.
     * @throws CannotEvaluateException if the values can't be computed at the specified coordinate.
     *         More specifically, {@link PointOutsideCoverageException} is thrown if the evaluation
     *         failed because the input point has invalid coordinates.
     */
    public float[] evaluate(final Point2D coord, final float[] dest)
        throws CannotEvaluateException
    {
        final Point2D pixel = gridGeometry.inverseTransform(coord);
        final double fx = pixel.getX();
        final double fy = pixel.getY();
        if (!Double.isNaN(fx) && !Double.isNaN(fy)) {
            final int x = (int)Math.round(fx);
            final int y = (int)Math.round(fy);
            if (image.getBounds().contains(x,y)) { // getBounds() returns a cached instance.
                return image.getTile(image.XToTileX(x), image.YToTileY(y)).getPixel(x, y, dest);
            }
        }
        throw new PointOutsideCoverageException(coord);
    }
    
    /**
     * Returns a sequence of double values for a given two-dimensional point in the coverage.
     *
     * @param  coord The coordinate point where to evaluate.
     * @param  dest  An array in which to store values, or <code>null</code>.
     * @return An array containing values.
     * @throws CannotEvaluateException if the values can't be computed at the specified coordinate.
     *         More specifically, {@link PointOutsideCoverageException} is thrown if the evaluation
     *         failed because the input point has invalid coordinates.
     */
    public double[] evaluate(final Point2D coord, final double[] dest)
        throws CannotEvaluateException
    {
        final Point2D pixel = gridGeometry.inverseTransform(coord);
        final double fx = pixel.getX();
        final double fy = pixel.getY();
        if (!Double.isNaN(fx) && !Double.isNaN(fy)) {
            final int x = (int)Math.round(fx);
            final int y = (int)Math.round(fy);
            if (image.getBounds().contains(x,y)) { // getBounds() returns a cached instance.
                return image.getTile(image.XToTileX(x), image.YToTileY(y)).getPixel(x, y, dest);
            }
        }
        throw new PointOutsideCoverageException(coord);
    }
    
    /**
     * Returns a debug string for the specified coordinate.   This method produces a
     * string with pixel coordinates and pixel values for all bands (with geophysics
     * values or category name in parenthesis). Example for a 1-banded image:
     *
     * <blockquote><pre>(1171,1566)=[196 (29.6 °C)]</pre></blockquote>
     *
     * @param  coord The coordinate point where to evaluate.
     * @return A string with pixel coordinates and pixel values at the specified location,
     *         or <code>null</code> if <code>coord</code> is outside coverage.
     */
    public synchronized String getDebugString(final CoordinatePoint coord) {
        Point2D pixel = new Point2D.Double(coord.ord[0], coord.ord[1]);
        pixel         = gridGeometry.inverseTransform(pixel);
        final int   x = (int)Math.round(pixel.getX());
        final int   y = (int)Math.round(pixel.getY());
        if (image.getBounds().contains(x,y)) { // getBounds() returns a cached instance.
            final int  numBands = image.getNumBands();
            final Raster raster = image.getTile(image.XToTileX(x), image.YToTileY(y));
            final int  datatype = image.getSampleModel().getDataType();
            final StringBuffer  buffer = new StringBuffer();
            buffer.append('(');
            buffer.append(x);
            buffer.append(',');
            buffer.append(y);
            buffer.append(")=[");
            for (int band=0; band<numBands; band++) {
                if (band!=0) {
                    buffer.append(";\u00A0");
                }
                final double sample = raster.getSampleDouble(x, y, band);
                switch (datatype) {
                    case DataBuffer.TYPE_DOUBLE: buffer.append((double)sample); break;
                    case DataBuffer.TYPE_FLOAT : buffer.append( (float)sample); break;
                    default                    : buffer.append(   (int)sample); break;
                }
                final String formatted = sampleDimensions[band].getLabel(sample, null);
                if (formatted != null) {
                    buffer.append("\u00A0(");
                    buffer.append(formatted);
                    buffer.append(')');
                }
            }
            buffer.append(']');
            return buffer.toString();
        }
        return null;
    }
    
    /**
     * Return a sequence of strongly typed values for a block.
     * A value for each sample dimension will be returned. The return value is an
     * <CODE>N+1</CODE> dimensional array, with dimensions. For 2 dimensional
     * grid coverages, this array will be accessed as (sample dimension, column,
     * row). The index values will be based from 0. The indices in the returned
     * <CODE>N</CODE> dimensional array will need to be offset by grid range
     * minimum coordinates to get equivalent grid coordinates.
     */
    //  public abstract DoubleMultiArray getDataBlockAsDouble(final GridRange range)
    //  {
    // TODO: Waiting for multiarray package (JSR-083)!
    //       Same for setDataBlock*
    //  }

    /**
     * Returns grid data as a rendered image.
     */
    public RenderedImage getRenderedImage() {
        return image;
    }
    
    /**
     * Hints that the given area may be needed in the near future. Some implementations
     * may spawn a thread or threads to compute the tiles while others may ignore the hint.
     *
     * @param area A rectangle indicating which geographic area to prefetch.
     *             This area's coordinates must be expressed according the
     *             grid coverage's coordinate system, as given by
     *             {@link #getCoordinateSystem}.
     */
    public void prefetch(final Rectangle2D area) {
        final Point[] tileIndices=image.getTileIndices(gridGeometry.inverseTransform(area));
        if (tileIndices!=null) {
            image.prefetchTiles(tileIndices);
        }
    }
    
    /**
     * Check if all numbers in <code>bands</code> are increasing from 0 to
     * <code>bands.length-1</code>. This internal method is used by {@link #geophysics}.
     */
    private static boolean isIncreasing(final int[] bands) {
        for (int i=0; i<bands.length; i++) {
            if (bands[i]!=i) {
                return false;
            }
        }
        return true;
    }

    /**
     * If <code>true</code>, returns a <code>GridCoverage</code> with sample values
     * equals to geophysics values. In any such <cite>geophysics grid coverage</cite>,
     * {@link SampleDimension#getSampleToGeophysics sampleToGeophysics} is the identity
     * transform for all bands. The following rules hold:
     *
     * <ul>
     *   <li><code>geophysics(true).evaluate(...)</code> returns directly the geophysics
     *       values (no transformation needed).</li>
     *   <li><code>geophysics(false)</code> returns the original grid coverage. In other words,
     *       it cancel a previous call to <code>geophysics(true)</code>.</li>
     *   <li>In <code>geophysics(b).geophysics(b)</code>, the second call has no effect
     *       if <var>b</var> has the same value.</li>
     * </ul>
     *
     * @param  toGeophysics <code>true</code> to gets a grid coverage wrapping geophysics
     *         values, or <code>false</code> to get back the original grid coverage.  The
     *         original grid coverage usually store sample as integers, which is faster
     *         to display.
     * @return The grid coverage. Never <code>null</code>, but may be <code>this</code>.
     *
     * @see SampleDimension#geophysics
     * @see Category#geophysics
     */
    public GridCoverage geophysics(final boolean toGeophysics) {
        if (toGeophysics == isGeophysics) {
            return this;
        }
        if (inverse == null) {
            PlanarImage       selectedImage = image;
            SampleDimension[] selectedBands = sampleDimensions;
            if (!toGeophysics) {
                /*
                 * HACK: If we are going to transform a geophysics image into a "normal" one, we
                 *       need to keep only one band.  This is because the "normal" image usually
                 *       has an IndexColorModel, which can have only one band.  We should try to
                 *       avoid this hack in some future version.
                 */
                final int    band  = 0; // TODO: make available as a parameter.
                final int[]  bands = new int[]{band};
                final int numBands = selectedImage.getSampleModel().getNumBands();
                if (bands.length!=numBands || !isIncreasing(bands)) {
                    ParameterBlock param = new ParameterBlock().addSource(selectedImage).add(bands);
                    selectedImage = JAI.create("BandSelect", param);
                }
                selectedBands = new SampleDimension[bands.length];
                for (int i=0; i<bands.length; i++) {
                    selectedBands[i] = sampleDimensions[bands[i]];
                }
            }
            /*
             * Transcode the image sample values. The "GC_SampleTranscoding" is registered
             * in the org.geotools.cv package in the SampleDimension class.
             */
            ParameterBlock param = new ParameterBlock().addSource(selectedImage).add(selectedBands);
            selectedImage = JAI.create("GC_SampleTranscoding", param).getRendering();
            if (selectedImage == image) {
                inverse = this;
            } else {
                if (selectedBands == sampleDimensions) {
                    selectedBands = (SampleDimension[]) selectedBands.clone();
                }
                for (int i=0; i<selectedBands.length; i++) {
                    selectedBands[i] = selectedBands[i].geophysics(toGeophysics);
                }
                inverse = new GridCoverage(getName(null), selectedImage,
                                           coordinateSystem, getGridGeometry(), null,
                                           selectedBands, new GridCoverage[]{this}, null);
                inverse = interpolate(inverse);
                inverse.inverse = this;
            }
        }
        return inverse;
    }

    /**
     * Apply to the specified grid coverage the same interpolation than this
     * grid coverage. This method is invoked internally by {@link #geophysics}.
     *
     * @param  coverage The coverage for which to apply the interpolation.
     * @return A coverage with the same data than <code>coverage</code> but
     *         the same interpolation than <code>this</code>.
     */
    protected GridCoverage interpolate(final GridCoverage coverage) {
        // This method is overriden by org.geotools.gp.Interpolator
        return coverage;
    }
    
    /**
     * Returns a string représentation of this coverage. This string is
     * for debugging purpose only and may change in future version.
     */
    public String toString() {
        final String lineSeparator = System.getProperty("line.separator", "\n");
        final StringWriter  buffer = new StringWriter();
        buffer.write(super.toString());
        final LineWriter filter = new LineWriter(buffer, lineSeparator+"    ");
        try {
            filter.write(lineSeparator);
            for (int i=0; i<sampleDimensions.length; i++) {
                filter.write(sampleDimensions[i].toString());
            }
            filter.flush();
        } catch (IOException exception) {
            // Should not happen
            throw new AssertionError(exception);
        }
        return buffer.toString();
    }




    /////////////////////////////////////////////////////////////////////////
    ////////////////                                         ////////////////
    ////////////////             OPENGIS ADAPTER             ////////////////
    ////////////////                                         ////////////////
    /////////////////////////////////////////////////////////////////////////

    /**
     * Interface for OpenGIS's {@link GC_GridCoverage} capable to produce a
     * {@link RenderedImage}. This interface can be used in order to export
     * some {@link GridCoverage} functionalities over the network using RMI
     * (<cite>Remote Method Invocation</cite>).  Socket connection are used
     * for sending the rendered image through the network.
     *
     * @version $Id: GridCoverage.java,v 1.11 2003/02/14 23:38:13 desruisseaux Exp $
     * @author Martin Desruisseaux
     */
    public static interface Remote extends GC_GridCoverage {
        /**
         * Returns the underlying {@link RenderedImage} for this {@link GC_GridCoverage}.
         * This method usually returns an instance of {@link SerializableRenderedImage},
         * which is appropriate for use through a RMI API.
         *
         * @throws RemoteException if the remote connection failed.
         * @throws NotSerializableException if the image is not serializable.
         */
        public abstract RenderedImage getRenderedImage() throws IOException;

        /**
         * Returns the interpolation used for <code>evaluate(...)</code> methods.
         *
         * @return The interpolation.
         * @throws RemoteException if the remote connection failed.
         */
        public abstract Interpolation getInterpolation() throws RemoteException;
    }

    /**
     * Export a Geotools {@link GridCoverage} as an OpenGIS {@link GC_GridCoverage}
     * object. This class is suitable for RMI use. User should not create instance
     * of this class directly. The method {@link Adapters#export(GridCoverage)} should
     * be used instead.
     *
     * @version $Id: GridCoverage.java,v 1.11 2003/02/14 23:38:13 desruisseaux Exp $
     * @author Martin Desruisseaux
     */
    protected class Export extends Coverage.Export implements GC_GridCoverage, Remote {
        /**
         * The serialized {@link RenderedImage}, or <code>null</code> if this image
         * is not yet serialized.
         */
        private SerializableRenderedImage serialized;

        /**
         * Constructs a remote object.  This object is automatically added to the cache
         * in the enclosing {@link GridCoverage} object. The cached <code>Export</code>
         * instance can be queried with {@link Adapters#export(GridCoverage)}.
         *
         * @param adapters The originating adapter.
         */
        protected Export(final Adapters adapters) {
            super(adapters);
        }

        /**
         * Returns the number of grid coverages which the grid coverage was derived from.
         * The default implementation invokes {@link GridCoverage#getSources}.
         *
         * @throws RemoteException if a remote call failed. More specifically, the exception will
         *         be an instance of {@link ServerException} if an error occurs on the server side.
         */
        public int getNumSources() throws RemoteException {
            return GridCoverage.this.getSources().length;
        }

        /**
         * Returns the source data for a grid coverage.
         * The default implementation invokes {@link GridCoverage#getSources}.
         *
         * @throws RemoteException if a remote call failed. More specifically, the exception will
         *         be an instance of {@link ServerException} if an error occurs on the server side.
         */
        public GC_GridCoverage getSource(int sourceDataIndex) throws RemoteException {
            return ((Adapters) adapters).export(GridCoverage.this.getSources()[sourceDataIndex]);
        }

        /**
         * Returns <code>true</code> if grid data can be edited.
         * The default implementation invokes {@link GridCoverage#isDataEditable}.
         *
         * @throws RemoteException if a remote call failed. More specifically, the exception will
         *         be an instance of {@link ServerException} if an error occurs on the server side.
         */
        public boolean isDataEditable() throws RemoteException {
            return GridCoverage.this.isDataEditable();
        }

        /**
         * Returns information for the packing of grid coverage values.
         *
         * @throws RemoteException if a remote call failed. More specifically, the exception will
         *         be an instance of {@link ServerException} if an error occurs on the server side.
         */
        public GC_GridPacking getGridPacking() throws RemoteException {
            throw new UnsupportedOperationException("Not yet implemented");
        }

        /**
         * Returns information for the grid coverage geometry.
         * The default implementation invokes {@link GridCoverage#getGridGeometry}.
         *
         * @throws RemoteException if a remote call failed. More specifically, the exception will
         *         be an instance of {@link ServerException} if an error occurs on the server side.
         */
        public GC_GridGeometry getGridGeometry() throws RemoteException {
            return ((Adapters) adapters).export(GridCoverage.this.getGridGeometry());
        }

        /**
         * Return the grid geometry for an overview.
         * The default implementation throws an {@link ArrayIndexOutOfBoundsException},
         * since {@link #getNumOverviews} returned 0.
         *
         * @throws RemoteException if a remote call failed. More specifically, the exception will
         *         be an instance of {@link ServerException} if an error occurs on the server side.
         */
        public GC_GridGeometry getOverviewGridGeometry(int overviewIndex) throws RemoteException {
            throw new ArrayIndexOutOfBoundsException(overviewIndex);
        }

        /**
         * Returns the number of predetermined overviews for the grid.
         * The default implementation returns 0, since this feature is not yet
         * implemented. It may be implemented in a future version.
         *
         * @throws RemoteException if a remote call failed. More specifically, the exception will
         *         be an instance of {@link ServerException} if an error occurs on the server side.
         */
        public int getNumOverviews() throws RemoteException {
            return 0;
        }

        /**
         * Returns a pre-calculated overview for a grid coverage.
         * The default implementation throws an {@link ArrayIndexOutOfBoundsException},
         * since {@link #getNumOverviews} returned 0.
         *
         * @throws RemoteException if a remote call failed. More specifically, the exception will
         *         be an instance of {@link ServerException} if an error occurs on the server side.
         */
        public GC_GridCoverage getOverview(int overviewIndex) throws RemoteException {
            throw new ArrayIndexOutOfBoundsException(overviewIndex);
        }

        /**
         * Returns the optimal size to use for each dimension when accessing grid values.
         * The default implementation returns the image's tiles size.
         *
         * @throws RemoteException if a remote call failed. More specifically, the exception will
         *         be an instance of {@link ServerException} if an error occurs on the server side.
         */
        public int[] getOptimalDataBlockSizes() throws RemoteException {
            final int[] size = new int[getDimension()];
            switch (size.length) {
                default: Arrays.fill(size, 1);            // Fall through
                case 2:  size[1] = image.getTileHeight(); // Fall through
                case 1:  size[0] = image.getTileWidth();  // Fall through
                case 0:  break;
            }
            return size;
        }

        /**
         * Return a sequence of boolean values for a block.
         *
         * @throws RemoteException if a remote call failed. More specifically, the exception will
         *         be an instance of {@link ServerException} if an error occurs on the server side.
         */
        public boolean[] getDataBlockAsBoolean(GC_GridRange gridRange) throws RemoteException {
            throw new UnsupportedOperationException("Not yet implemented");
        }

        /**
         * Return a sequence of byte values for a block.
         *
         * @throws RemoteException if a remote call failed. More specifically, the exception will
         *         be an instance of {@link ServerException} if an error occurs on the server side.
         */
        public byte[] getDataBlockAsByte(GC_GridRange gridRange) throws RemoteException {
            throw new UnsupportedOperationException("Not yet implemented");
        }

        /**
         * Return a sequence of int values for a block.
         *
         * @throws RemoteException if a remote call failed. More specifically, the exception will
         *         be an instance of {@link ServerException} if an error occurs on the server side.
         */
        public int[] getDataBlockAsInteger(GC_GridRange gridRange) throws RemoteException {
            throw new UnsupportedOperationException("Not yet implemented");
        }

        /**
         * Return a sequence of double values for a block.
         *
         * @throws RemoteException if a remote call failed. More specifically, the exception will
         *         be an instance of {@link ServerException} if an error occurs on the server side.
         */
        public double[] getValueBlockAsDouble(GC_GridRange gridRange) throws RemoteException {
            throw new UnsupportedOperationException("Not yet implemented");
        }

        /**
         * Return a block of grid coverage data for all sample dimensions.
         *
         * @throws RemoteException if a remote call failed. More specifically, the exception will
         *         be an instance of {@link ServerException} if an error occurs on the server side.
         */
        public byte[] getPackedDataBlock(GC_GridRange gridRange) throws RemoteException {
            throw new UnsupportedOperationException("Not yet implemented");
        }

        /**
         * Set a block of boolean values for all sample dimensions.
         *
         * @throws RemoteException if a remote call failed. More specifically, the exception will
         *         be an instance of {@link ServerException} if an error occurs on the server side.
         */
        public void setDataBlockAsBoolean(GC_GridRange gridRange, boolean[] values) throws RemoteException {
            throw new UnsupportedOperationException("Not yet implemented");
        }

        /**
         * Set a block of byte values for all sample dimensions.
         *
         * @throws RemoteException if a remote call failed. More specifically, the exception will
         *         be an instance of {@link ServerException} if an error occurs on the server side.
         */
        public void setDataBlockAsByte(GC_GridRange gridRange, byte[] values) throws RemoteException {
            throw new UnsupportedOperationException("Not yet implemented");
        }

        /**
         * Set a block of bint values for all sample dimensions.
         *
         * @throws RemoteException if a remote call failed. More specifically, the exception will
         *         be an instance of {@link ServerException} if an error occurs on the server side.
         */
        public void setDataBlockAsInteger(GC_GridRange gridRange, int[] values) throws RemoteException {
            throw new UnsupportedOperationException("Not yet implemented");
        }

        /**
         * Set a block of double values for all sample dimensions.
         *
         * @throws RemoteException if a remote call failed. More specifically, the exception will
         *         be an instance of {@link ServerException} if an error occurs on the server side.
         */
        public void setDataBlockAsDouble(GC_GridRange gridRange, double[] values) throws RemoteException {
            throw new UnsupportedOperationException("Not yet implemented");
        }

        /**
         * Set a block of grid coverage data for all sample dimensions.
         *
         * @throws RemoteException if a remote call failed. More specifically, the exception will
         *         be an instance of {@link ServerException} if an error occurs on the server side.
         */
        public void setPackedDataBlock(GC_GridRange gridRange, byte [] values) throws RemoteException {
            throw new UnsupportedOperationException("Not yet implemented");
        }

        /**
         * Returns the underlying {@link RenderedImage} for this {@link GC_GridCoverage}.
         * This method usually returns an instance of {@link SerializableRenderedImage},
         * which is appropriate for use through a RMI API.
         *
         * @throws RemoteException if the remote connection failed.
         * @throws NotSerializableException if the image is not serializable.
         */
        public synchronized RenderedImage getRenderedImage() throws IOException {
            if (serialized == null) {
                serialized = new SerializableRenderedImage(GridCoverage.this.getRenderedImage(),
                                                           false, null, "gzip", null, null);
            }
            return serialized;
        }

        /**
         * Returns the interpolation used for <code>evaluate(...)</code> methods.
         *
         * @return The interpolation.
         * @throws RemoteException if the remote connection failed.
         */
        public Interpolation getInterpolation() throws RemoteException {
            return GridCoverage.this.getInterpolation();
        }
    }
}
