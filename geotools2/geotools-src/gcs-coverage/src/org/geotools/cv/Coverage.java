/*
 * Geotools 2 - OpenSource mapping toolkit
 * (C) 2003, Geotools Project Management Committee (PMC)
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
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.image.ColorModel;
import java.awt.image.SampleModel;
import java.awt.image.RenderedImage;
import java.awt.image.renderable.RenderContext;
import java.awt.image.renderable.RenderableImage;
import java.awt.image.renderable.ParameterBlock;
import javax.media.jai.TiledImage;
import javax.media.jai.iterator.RectIterFactory;
import javax.media.jai.iterator.WritableRectIter;

// Geometry
import java.awt.Shape;
import java.awt.Rectangle;
import java.awt.Dimension;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;

// Miscellaneous
import java.util.Map;
import java.util.Vector;
import java.util.Arrays;
import java.util.Locale;
import java.rmi.RemoteException;
import java.rmi.ServerException; // For Javadoc
import java.rmi.server.UnicastRemoteObject;
import java.lang.ref.WeakReference;

// JAI dependencies
import javax.media.jai.JAI;
import javax.media.jai.PlanarImage;
import javax.media.jai.ImageLayout;
import javax.media.jai.ImageFunction;
import javax.media.jai.PropertySource;
import javax.media.jai.PropertySourceImpl;
import javax.media.jai.util.CaselessStringKey; // For Javadoc
import javax.media.jai.operator.ImageFunctionDescriptor; // For Javadoc

// OpenGIS dependencies
import org.opengis.cv.CV_Coverage;
import org.opengis.pt.PT_Envelope;
import org.opengis.pt.PT_CoordinatePoint;
import org.opengis.cs.CS_CoordinateSystem;
import org.opengis.cv.CV_SampleDimension;
import org.opengis.gc.GC_GridCoverage;

// Geotools dependencies (CTS)
import org.geotools.pt.Matrix;
import org.geotools.pt.Envelope;
import org.geotools.pt.Dimensioned;
import org.geotools.pt.CoordinatePoint;
import org.geotools.cs.CoordinateSystem;
import org.geotools.cs.AxisOrientation;

// Resources
import org.geotools.resources.XArray;
import org.geotools.resources.Utilities;
import org.geotools.resources.ImageUtilities;
import org.geotools.resources.XAffineTransform;
import org.geotools.resources.gcs.ResourceKeys;
import org.geotools.resources.gcs.Resources;


/**
 * Base class of all coverage type. The essential property of coverage is to be able
 * to generate a value for any point within its domain.  How coverage is represented
 * internally is not a concern. For example consider the following different internal
 * representations of coverage:
 *
 *  <ul>
 *    <li>A coverage may be represented by a set of polygons which exhaustively
 *        tile a plane (that is each point on the plane falls in precisely one polygon).
 *        The value returned by the coverage for a point is the value of an attribute of
 *        the polygon that contains the point.</li>
 *    <li>A coverage may be represented by a grid of values.
 *        The value returned by the coverage for a point is that of the grid value
 *        whose location is nearest the point.</li>
 *    <li>Coverage may be represented by a mathematical function.
 *        The value returned by the coverage for a point is just the return value
 *        of the function when supplied the coordinates of the point as arguments.</li>
 *    <li>Coverage may be represented by combination of these.
 *        For example, coverage may be represented by a combination of mathematical
 *        functions valid over a set of polynomials.</LI>
 * </ul>
 *
 * {@linkplain org.geotools.gc.GridCoverage Grid coverages} are typically 2D while other
 * coverages may be 3D or 4D. The dimension of grid coverage may be queried in many ways:
 *
 * <ul>
 *   <li><code>{@link #getCoordinateSystem}.getDimension();</code></li>
 *   <li><code>{@link #getDimensionNames}.length;</code></li>
 *   <li><code>{@link #getDimension};</code></li>
 * </ul>
 *
 * All those methods should returns the same number.   Note that the dimension
 * of grid coverage <strong>is not the same</strong> than the number of sample
 * dimensions (<code>{@link #getSampleDimensions()}.length</code>).  The later
 * may be better understood as the number of bands for 2D grid coverage.
 * A coverage has a corresponding {@link SampleDimension} for each sample
 * dimension in the coverage.
 * <br><br>
 * There is no <code>getMetadataValue(...)</code> method in this implementation.
 * OpenGIS's metadata are called "Properties" in <em>Java Advanced Imaging</em>.
 * Use {@link #getProperty} instead.
 *
 * @version $Id: Coverage.java,v 1.20 2003/08/09 22:13:11 desruisseaux Exp $
 * @author <A HREF="www.opengis.org">OpenGIS</A>
 * @author Martin Desruisseaux
 *
 * @see org.opengis.cv.CV_Coverage
 */
public abstract class Coverage extends PropertySourceImpl implements Dimensioned {
    /**
     * The set of default axis name.
     */
    private static final String[] DIMENSION_NAMES = {"x", "y", "z", "t"};

    /**
     * The sample dimension to make visible by {@link #getRenderableImage}.
     */
    private static final int VISIBLE_BAND = 0;

    /**
     * The coverage name.
     */
    private final String name;
    
    /**
     * The coordinate system, or <code>null</code> if there is none.
     */
    protected final CoordinateSystem coordinateSystem;

    /**
     * OpenGIS object returned by {@link #getProxy}.
     * It may be a hard or a weak reference.
     */
    transient Object proxy;
    
    /**
     * Construct a coverage using the specified coordinate system. If the coordinate system
     * is <code>null</code>, then the subclasses must override {@link #getDimension()}.
     *
     * @param name The coverage name.
     * @param coordinateSystem The coordinate system. This specifies the coordinate
     *        system used when accessing a coverage or grid coverage with the
     *        <code>evaluate(...)</code> methods.
     * @param source The source for this coverage, or <code>null</code> if none.
     *        Source may be (but is not limited to) a {@link PlanarImage} or an
     *        other <code>Coverage</code> object.
     * @param properties The set of properties for this coverage, or <code>null</code> if
     *        there is none. "Properties" in <cite>Java Advanced Imaging</cite> is what
     *        OpenGIS calls "Metadata".  There is no <code>getMetadataValue(...)</code>
     *        method in this implementation. Use {@link #getProperty} instead. Keys may
     *        be {@link String} or {@link CaselessStringKey} objects,  while values may
     *        be any {@link Object}.
     */
    protected Coverage(final String           name,
                       final CoordinateSystem coordinateSystem,
                       final PropertySource   source,
                       final Map              properties)
    {
        super(properties, source);
        this.name             = name;
        this.coordinateSystem = coordinateSystem;
    }
    
    /**
     * Construct a new coverage with the same
     * parameters than the specified coverage.
     */
    protected Coverage(final Coverage coverage) {
        // NOTE: This constructor keep a strong reference to the
        //       source coverage (through 'PropertySourceImpl').
        //       In many cases, it is not a problem since GridCoverage
        //       will retains a strong reference to its source anyway.
        super(null, coverage);
        this.name             = coverage.name;
        this.coordinateSystem = coverage.coordinateSystem;
    }
    
    /**
     * Returns the coverage name, localized for the supplied locale.
     * If the specified locale is not available, returns a name in an
     * arbitrary locale. The default implementation returns the name
     * specified at construction time.
     *
     * @param  locale The desired locale, or <code>null</code> for a default locale.
     * @return The coverage name in the specified locale, or in an arbitrary locale
     *         if the specified localization is not available.
     */
    public String getName(final Locale locale) {
        return name;
    }
    
    /**
     * Returns the coordinate system. This specifies the coordinate system used when
     * accessing a coverage or grid coverage with the <code>evaluate(...)</code> methods.
     * It is also the coordinate system of the coordinates used with the math transform
     * {@link org.geotools.gc.GridGeometry#getGridToCoordinateSystem}. This coordinate
     * system is usually different than the grid coordinate system of the grid. A grid
     * coverage can be accessed (re-projected) with new coordinate system with the
     * {@link org.geotools.gp.GridCoverageProcessor} component.
     * In this case, a new instance of a grid coverage is created.
     * <br><br>
     * Note: If a coverage does not have an associated coordinate system,
     * the returned value will be <code>null</code>.
     * The {@link org.geotools.gc.GridGeometry#getGridToCoordinateSystem
     * GridGeometry.gridToCoordinateSystem}) attribute should also be
     * <code>null</code> if the coordinate system is <code>null</code>.
     *
     * @return The coordinate system, or <code>null</code> if this coverage
     *         does not have an associated coordinate system.
     *
     * @see CV_Coverage#getCoordinateSystem()
     * @see org.geotools.gc.GridGeometry#getGridToCoordinateSystem
     */
    public CoordinateSystem getCoordinateSystem() {
        return coordinateSystem;
    }
    
    /**
     * Returns The bounding box for the coverage domain in coordinate system coordinates.
     * May be null if this coverage has no associated coordinate system.
     * For grid coverages, the grid cells are centered on each grid coordinate.
     * The envelope for a 2-D grid coverage includes the following corner positions.
     *
     * <blockquote><pre>
     * (Minimum row - 0.5, Minimum column - 0.5) for the minimum coordinates
     * (Maximum row - 0.5, Maximum column - 0.5) for the maximum coordinates
     * </pre></blockquote>
     *
     * The default implementation returns the coordinate system envelope if there is one.
     *
     * @return The bounding box for the coverage domain in coordinate system coordinates.
     *
     * @see CV_Coverage#getEnvelope()
     */
    public Envelope getEnvelope() {
        final CoordinateSystem cs = getCoordinateSystem();
        return (cs!=null) ? cs.getDefaultEnvelope() : null;
    }
    
    /**
     * Returns the dimension of the grid coverage. The default implementation
     * returns the dimension of the underlying {@link CoordinateSystem}.
     *
     * @return The number of dimensions of this coverage.
     */
    public int getDimension() {
        return getCoordinateSystem().getDimension();
    }
    
    /**
     * Returns the names of each dimension in this coverage. Typically these names
     * are "x", "y", "z" and "t". The number of items in the sequence is the number
     * of dimensions in the coverage. Grid coverages are typically 2D (<var>x</var>,
     * <var>y</var>) while other coverages may be 3D (<var>x</var>, <var>y</var>,
     * <var>z</var>) or 4D (<var>x</var>, <var>y</var>, <var>z</var>, <var>t</var>).
     * The {@linkplain #getDimension number of dimensions} of the coverage is the
     * number of entries in the list of dimension names.
     *
     * The default implementation ask for {@link CoordinateSystem}
     * axis names, or returns "x", "y"... if this coverage has no coordinate system.
     *
     * @param  locale The desired locale, or <code>null</code> for the default locale.
     * @return The names of each dimension. The array's length is equals to {@link #getDimension}.
     *
     * @see CV_Coverage#getDimensionNames()
     */
    public String[] getDimensionNames(final Locale locale) {
        final CoordinateSystem cs = getCoordinateSystem();
        if (cs != null) {
            final String[] names = new String[cs.getDimension()];
            for (int i=0; i<names.length; i++) {
                names[i] = cs.getAxis(i).getName(locale);
            }
            return names;
        } else {
            final String[] names = (String[]) XArray.resize(DIMENSION_NAMES, getDimension());
            for (int i=DIMENSION_NAMES.length; i<names.length; i++) {
                names[i] = "dim"+(i+1);
            }
            return names;
        }
    }
    
    /**
     * Retrieve sample dimension information for the coverage.
     * For a grid coverage, a sample dimension is a band. The sample dimension information
     * include such things as description, data type of the value (bit, byte, integer...),
     * the no data values, minimum and maximum values and a color table if one is associated
     * with the dimension. A coverage must have at least one sample dimension.
     *
     * @return Sample dimension information for the coverage.
     *
     * @see CV_Coverage#getNumSampleDimensions()
     * @see CV_Coverage#getSampleDimension(int)
     */
    public abstract SampleDimension[] getSampleDimensions();
    
    /**
     * Returns a sequence of boolean values for a given point in the coverage.
     * A value for each sample dimension is included in the sequence. The default interpolation
     * type used when accessing grid values for points which fall between grid cells is
     * nearest neighbor. The coordinate system of the point is the same as the grid
     * coverage {@linkplain #getCoordinateSystem coordinate system}.
     *
     * @param  coord The coordinate point where to evaluate.
     * @param  dest  An array in which to store values, or <code>null</code> to
     *               create a new array. If non-null, this array must be at least
     *               <code>{@link #getSampleDimensions()}.length</code> long.
     * @return The <code>dest</code> array, or a newly created array if <code>dest</code> was null.
     * @throws CannotEvaluateException if the values can't be computed at the specified coordinate.
     *         More specifically, {@link PointOutsideCoverageException} is thrown if the evaluation
     *         failed because the input point has invalid coordinates.
     *
     * @see CV_Coverage#evaluateAsBoolean
     */
    public boolean[] evaluate(final CoordinatePoint coord, boolean[] dest)
            throws CannotEvaluateException
    {
        final double[] result = evaluate(coord, (double[])null);
        if (dest == null) {
            dest = new boolean[result.length];
        }
        for (int i=0; i<result.length; i++) {
            final double value = result[i];
            dest[i] = (!Double.isNaN(value) && value!=0);
        }
        return dest;
    }

    /**
     * Returns a sequence of byte values for a given point in the coverage.
     * A value for each sample dimension is included in the sequence. The default
     * interpolation type used when accessing grid values for points which fall
     * between grid cells is nearest neighbor. The coordinate system of the
     * point is the same as the coverage {@linkplain #getCoordinateSystem
     * coordinate system}.
     *
     * @param  coord The coordinate point where to evaluate.
     * @param  dest  An array in which to store values, or <code>null</code> to
     *               create a new array. If non-null, this array must be at least
     *               <code>{@link #getSampleDimensions()}.length</code> long.
     * @return The <code>dest</code> array, or a newly created array if <code>dest</code> was null.
     * @throws CannotEvaluateException if the values can't be computed at the specified coordinate.
     *         More specifically, {@link PointOutsideCoverageException} is thrown if the evaluation
     *         failed because the input point has invalid coordinates.
     *
     * @see CV_Coverage#evaluateAsInteger
     */
    public byte[] evaluate(final CoordinatePoint coord, byte[] dest)
            throws CannotEvaluateException
    {
        final double[] result = evaluate(coord, (double[])null);
        if (dest == null) {
            dest = new byte[result.length];
        }
        for (int i=0; i<result.length; i++) {
            final double value = Math.rint(result[i]);
            dest[i] = (value < Byte.MIN_VALUE) ? Byte.MIN_VALUE :
                      (value > Byte.MAX_VALUE) ? Byte.MAX_VALUE : (byte) value;
        }
        return dest;
    }

    /**
     * Returns a sequence of integer values for a given point in the coverage.
     * A value for each sample dimension is included in the sequence. The default
     * interpolation type used when accessing grid values for points which fall
     * between grid cells is nearest neighbor. The coordinate system of the
     * point is the same as the coverage {@linkplain #getCoordinateSystem
     * coordinate system}.
     *
     * @param  coord The coordinate point where to evaluate.
     * @param  dest  An array in which to store values, or <code>null</code> to
     *               create a new array. If non-null, this array must be at least
     *               <code>{@link #getSampleDimensions()}.length</code> long.
     * @return The <code>dest</code> array, or a newly created array if <code>dest</code> was null.
     * @throws CannotEvaluateException if the values can't be computed at the specified coordinate.
     *         More specifically, {@link PointOutsideCoverageException} is thrown if the evaluation
     *         failed because the input point has invalid coordinates.
     *
     * @see CV_Coverage#evaluateAsInteger
     */
    public int[] evaluate(final CoordinatePoint coord, int[] dest)
            throws CannotEvaluateException
    {
        final double[] result = evaluate(coord, (double[])null);
        if (dest == null) {
            dest = new int[result.length];
        }
        for (int i=0; i<result.length; i++) {
            final double value = Math.rint(result[i]);
            dest[i] = (value < Integer.MIN_VALUE) ? Integer.MIN_VALUE :
                      (value > Integer.MAX_VALUE) ? Integer.MAX_VALUE : (int) value;
        }
        return dest;
    }

    /**
     * Returns a sequence of float values for a given point in the coverage.
     * A value for each sample dimension is included in the sequence. The default interpolation
     * type used when accessing grid values for points which fall between grid cells is
     * nearest neighbor. The coordinate system of the point is the same as the coverage
     * {@linkplain #getCoordinateSystem coordinate system}.
     *
     * @param  coord The coordinate point where to evaluate.
     * @param  dest  An array in which to store values, or <code>null</code> to
     *               create a new array. If non-null, this array must be at least
     *               <code>{@link #getSampleDimensions()}.length</code> long.
     * @return The <code>dest</code> array, or a newly created array if <code>dest</code> was null.
     * @throws CannotEvaluateException if the values can't be computed at the specified coordinate.
     *         More specifically, {@link PointOutsideCoverageException} is thrown if the evaluation
     *         failed because the input point has invalid coordinates.
     */
    public float[] evaluate(final CoordinatePoint coord, float[] dest)
            throws CannotEvaluateException
    {
        final double[] result = evaluate(coord, (double[])null);
        if (dest == null) {
            dest = new float[result.length];
        }
        for (int i=0; i<result.length; i++) {
            dest[i] = (float)result[i];
        }
        return dest;
    }
    
    /**
     * Returns a sequence of double values for a given point in the coverage.
     * A value for each sample dimension is included in the sequence. The default interpolation
     * type used when accessing grid values for points which fall between grid cells is
     * nearest neighbor. The coordinate system of the point is the same as the grid coverage
     * coordinate system.
     *
     * @param  coord The coordinate point where to evaluate.
     * @param  dest  An array in which to store values, or <code>null</code> to
     *               create a new array. If non-null, this array must be at least
     *               <code>{@link #getSampleDimensions()}.length</code> long.
     * @return The <code>dest</code> array, or a newly created array if <code>dest</code> was null.
     * @throws CannotEvaluateException if the values can't be computed at the specified coordinate.
     *         More specifically, {@link PointOutsideCoverageException} is thrown if the evaluation
     *         failed because the input point has invalid coordinates.
     *
     * @see CV_Coverage#evaluateAsDouble
     */
    public abstract double[] evaluate(CoordinatePoint coord, double[] dest)
            throws CannotEvaluateException;
    
    /**
     * Returns 2D view of this grid coverage as a renderable image.
     * This method allows interoperability with Java2D.
     *
     * @param  xAxis Dimension to use for <var>x</var> axis.
     * @param  yAxis Dimension to use for <var>y</var> axis.
     * @return A 2D view of this grid coverage as a renderable image.
     */
    public RenderableImage getRenderableImage(final int xAxis, final int yAxis) {
        return new Renderable(xAxis, yAxis);
    }




    /////////////////////////////////////////////////////////////////////////
    ////////////////                                         ////////////////
    ////////////////     RenderableImage / ImageFunction     ////////////////
    ////////////////                                         ////////////////
    /////////////////////////////////////////////////////////////////////////
    
    /**
     * Base class for renderable image view of a coverage. Renderable images allow interoperability
     * with <A HREF="http://java.sun.com/products/java-media/2D/">Java2D</A>  for a two-dimensional
     * view of a coverage (which may or may not be a {@linkplain org.geotools.gc.GridCoverage grid
     * coverage}).
     *
     * @version $Id: Coverage.java,v 1.20 2003/08/09 22:13:11 desruisseaux Exp $
     * @author Martin Desruisseaux
     *
     * @see Coverage#getRenderableImage
     */
    protected class Renderable extends PropertySourceImpl implements RenderableImage, ImageFunction
    {
        /**
         * The two dimensional view of the coverage's envelope.
         */
        private final Rectangle2D bounds;

        /**
         * Dimension to use for <var>x</var> axis.
         */
        protected final int xAxis;

        /**
         * Dimension to use for <var>y</var> axis.
         */
        protected final int yAxis;

        /**
         * A coordinate point where to evaluate the function. The point dimension is equals to
         * the {@linkplain Coverage#getDimension coverage's dimension}. The {@linkplain #xAxis
         * x} and {@link #yAxis y} ordinates will be ignored, since they will vary for each pixel
         * to be evaluated. Other ordinates, if any, should be set to a fixed value. For example
         * a coverage may be three-dimensional, where the third dimension is the time axis. In
         * such case, <code>coordinate.ord[2]</code> should be set to the point in time where
         * to evaluate the coverage. By default, all ordinates are initialized to 0. Subclasses
         * should set the desired values in their constructor if needed.
         */
        protected final CoordinatePoint coordinate = new CoordinatePoint(getDimension());

        /**
         * Construct a renderable image.
         *
         * @param  xAxis Dimension to use for <var>x</var> axis.
         * @param  yAxis Dimension to use for <var>y</var> axis.
         */
        public Renderable(final int xAxis, final int yAxis) {
            super(null, Coverage.this);
            this.xAxis = xAxis;
            this.yAxis = yAxis;
            final Envelope envelope = getEnvelope();
            bounds = new Rectangle2D.Double(envelope.getMinimum(xAxis),
                                            envelope.getMinimum(yAxis),
                                            envelope.getLength (xAxis),
                                            envelope.getLength (yAxis));
        }

        /**
         * Returns <code>null</code> to indicate that no source information is available.
         */
        public Vector getSources() {
            return null;
        }

        /**
         * Returns <code>true</code> if successive renderings with the same arguments may
         * produce different results. The default implementation returns <code>false</code>.
         *
         * @see org.geotools.gc.GridCoverage#isDataEditable
         */
        public boolean isDynamic() {
            return false;
        }

        /**
         * Returns <code>false</code> since values are not complex.
         */
        public boolean isComplex() {
            return false;
        }

        /**
         * Gets the width in coverage coordinate space.
         *
         * @see Coverage#getEnvelope
         * @see Coverage#getCoordinateSystem
         */
        public float getWidth() {
            return (float)bounds.getWidth();
        }
        
        /**
         * Gets the height in coverage coordinate space.
         *
         * @see Coverage#getEnvelope
         * @see Coverage#getCoordinateSystem
         */
        public float getHeight() {
            return (float)bounds.getHeight();
        }
        
        /**
         * Gets the minimum <var>X</var> coordinate of the rendering-independent image data.
         * This is the {@linkplain Coverage#getEnvelope coverage's envelope} minimal value
         * for the {@linkplain #xAxis x axis}.
         *
         * @see Coverage#getEnvelope
         * @see Coverage#getCoordinateSystem
         */
        public float getMinX() {
            return (float)bounds.getX();
        }
        
        /**
         * Gets the minimum <var>Y</var> coordinate of the rendering-independent image data.
         * This is the {@linkplain Coverage#getEnvelope coverage's envelope} minimal value
         * for the {@linkplain #yAxis y axis}.
         *
         * @see Coverage#getEnvelope
         * @see Coverage#getCoordinateSystem
         */
        public float getMinY() {
            return (float)bounds.getY();
        }
        
        /**
         * Returns a rendered image with a default width and height in pixels.
         *
         * @return A rendered image containing the rendered data
         */
        public RenderedImage createDefaultRendering() {
            return createScaledRendering(512, 0, null);
        }
        
        /**
         * Creates a rendered image with width <code>width</code> and height <code>height</code>
         * in pixels. If <code>width</code> is 0, it will be computed automatically from
         * <code>height</code>. Conversely, if <code>height</code> is 0, il will be computed
         * automatically from <code>width</code>.
         *
         * The default implementation creates a render context with {@link #createRenderContext}
         * and invokes {@link #createRendering(RenderContext)}.
         *
         * @param  width  The width of rendered image in pixels, or 0.
         * @param  height The height of rendered image in pixels, or 0.
         * @param  hints  Rendering hints, or <code>null</code>.
         * @return A rendered image containing the rendered data
         */
        public RenderedImage createScaledRendering(int width, int height,
                                                   final RenderingHints hints)
        {
            final double boundsWidth  = bounds.getWidth();
            final double boundsHeight = bounds.getHeight();
            if (!(width > 0)) { // Use '!' in order to catch NaN
                if (!(height > 0)) {
                    throw new IllegalArgumentException(Resources.format(
                             ResourceKeys.ERROR_UNSPECIFIED_IMAGE_SIZE));
                }
                width = (int)Math.round(height * (boundsWidth/boundsHeight));
            } else if (!(height > 0)) {
                height = (int)Math.round(width * (boundsHeight/boundsWidth));
            }
            return createRendering(createRenderContext(new Rectangle(0, 0, width, height), hints));
        }
        
        /**
         * Creates a rendered image using a given render context. This method will uses an
         * &quot;{@link ImageFunctionDescriptor ImageFunction}&quot; operation if possible
         * (i.e. if the area of interect is rectangular  and the affine transform contains
         * only translation and scale coefficients).
         *
         * @param  context The render context to use to produce the rendering.
         * @return A rendered image containing the rendered data
         */
        public RenderedImage createRendering(final RenderContext context) {
            final AffineTransform csToGrid = context.getTransform();
            final Shape area = context.getAreaOfInterest();
            final Rectangle gridBounds;
            if (true) {
                /*
                 * Compute the grid bounds for the coverage bounds (or the area of interest).
                 * The default implementation of Rectangle uses Math.floor and Math.ceil for
                 * computing a box which contains fully the Rectangle2D. But in our particular
                 * case, we really want to round toward the nearest integer.
                 */
                final Rectangle2D bounds = XAffineTransform.transform(csToGrid,
                                           (area!=null) ? area.getBounds2D() : this.bounds, null);
                final int xmin = (int)Math.round(bounds.getMinX());
                final int ymin = (int)Math.round(bounds.getMinY());
                final int xmax = (int)Math.round(bounds.getMaxX());
                final int ymax = (int)Math.round(bounds.getMaxY());
                gridBounds = new Rectangle(xmin, ymin, xmax-xmin, ymax-ymin);
            }
            /*
             * Compute some properties of the image to be created.
             */
            final Dimension tileSize = ImageUtilities.toTileSize(gridBounds.getSize());
            final SampleDimension[] sampleDimensions = getSampleDimensions();
            final ColorModel colorModel = sampleDimensions[VISIBLE_BAND].getColorModel(
                                                           VISIBLE_BAND, sampleDimensions.length);
            final SampleModel sampleModel = colorModel.createCompatibleSampleModel(
                                                           tileSize.width, tileSize.height);
            /*
             * If the image can be created using the ImageFunction operation, do it.
             * It allow JAI to defer the computation until a tile is really requested.
             */
            final PlanarImage image;
            if ((area==null || area instanceof Rectangle2D) &&
                csToGrid.getShearX()==0 && csToGrid.getShearY()==0)
            {
                image = JAI.create("ImageFunction",
                       new ParameterBlock()
                                .add(this)                          // The functional description
                                .add(gridBounds.width)                    // The image width
                                .add(gridBounds.height)                   // The image height
                                .add((float)(1/csToGrid.getScaleX()))     // The X scale factor
                                .add((float)(1/csToGrid.getScaleY()))     // The Y scale factor
                                .add((float)   csToGrid.getTranslateX())  // The X translation
                                .add((float)   csToGrid.getTranslateY()), // The Y translation
                       new RenderingHints(JAI.KEY_IMAGE_LAYOUT, new ImageLayout()
                                .setMinX       (gridBounds.x)
                                .setMinY       (gridBounds.y)
                                .setTileWidth  (tileSize.width)
                                .setTileHeight (tileSize.height)
                                .setSampleModel(sampleModel)
                                .setColorModel (colorModel)));
            } else {
                /*
                 * Creates immediately a rendered image using a given render context. This block
                 * is run when the image can't be created with JAI's ImageFunction operator, for
                 * example because the affine transform swap axis or because there is an area of
                 * interest.
                 */
                // Clones the coordinate point in order to allow multi-thread invocation.
                final CoordinatePoint coordinate = new CoordinatePoint(this.coordinate);
                final TiledImage tiled = new TiledImage(gridBounds.x, gridBounds.y,
                                                        gridBounds.width, gridBounds.height,
                                                        0, 0, sampleModel, colorModel);
                final Point2D.Double point2D = new Point2D.Double();
                final int     numBands = tiled.getNumBands();
                final double[] samples = new double[numBands];
                final double[] padNaNs = new double[numBands];
                Arrays.fill(padNaNs, Double.NaN);
                final WritableRectIter iterator = RectIterFactory.createWritable(tiled, gridBounds);
                if (!iterator.finishedLines()) try {
                    int y=gridBounds.y; do {
                        iterator.startPixels();
                        if (!iterator.finishedPixels()) {
                            int x=gridBounds.x; do {
                                point2D.x = x;
                                point2D.y = y;
                                csToGrid.inverseTransform(point2D, point2D);
                                if (area==null || area.contains(point2D)) {
                                    coordinate.ord[xAxis] = point2D.x;
                                    coordinate.ord[yAxis] = point2D.y;
                                    iterator.setPixel(evaluate(coordinate, samples));
                                } else {
                                    iterator.setPixel(padNaNs);
                                }
                                x++;
                            }
                            while (!iterator.nextPixelDone());
                            assert(x == gridBounds.x + gridBounds.width);
                            y++;
                        }
                    }
                    while (!iterator.nextLineDone());
                    assert(y == gridBounds.y + gridBounds.height);
                }
                catch (NoninvertibleTransformException exception) {
                    final IllegalArgumentException e= new IllegalArgumentException("RenderContext");
                    e.initCause(exception);
                    throw e;
                }
                image = tiled;
            }
            /*
             * Add a 'gridToCoordinateSystem' property to the image. This is an important
             * information for constructing a GridCoverage from this image later.
             */
            try {
                image.setProperty("gridToCoordinateSystem", csToGrid.createInverse());
            } catch (NoninvertibleTransformException exception) {
                // Can't add the property. Too bad, the image has been created anyway.
                // Maybe the user know what he is doing...
                Utilities.unexpectedException("org.geotools.cv", "Coverage.Renderable",
                                              "createRendering", exception);
            }
            return image;
        }
        
        /**
         * Initialize a render context with an affine transform that maps the coverage envelope
         * to the specified destination rectangle. The affine transform mays swap axis in order
         * to normalize their order (i.e. make them appear in the (<var>x</var>,<var>y</var>)
         * order), so that the image appears properly oriented when rendered.
         *
         * @param  gridBounds The two-dimensional destination rectangle.
         * @param  hints The rendering hints, or <code>null</code> if none.
         * @return A render context initialized with an affine transform from the coverage
         *         to the grid coordinate system. This transform is the inverse of
         *         {@link org.geotools.gc.GridRange#getGridToCoordinateSystem2D}.
         *
         * @see org.geotools.gc.GridRange#getGridToCoordinateSystem
         */
        protected RenderContext createRenderContext(final Rectangle2D gridBounds,
                                                    final RenderingHints hints)
        {
            final Matrix matrix;
            final Envelope srcEnvelope = new Envelope(bounds);
            final Envelope dstEnvelope = new Envelope(gridBounds);
            final CoordinateSystem  cs = getCoordinateSystem();
            if (cs != null) {
                final AxisOrientation[] axis = new AxisOrientation[] {
                    cs.getAxis(xAxis).orientation,
                    cs.getAxis(yAxis).orientation
                };
                final AxisOrientation[] normalized = (AxisOrientation[]) axis.clone();
                if (true) {
                    // Normalize axis: Is it really a good idea?
                    // We should provide a rendering hint for configuring that.
                    Arrays.sort(normalized);
                    for (int i=normalized.length; --i>=0;) {
                        normalized[i] = normalized[i].absolute();
                    }
                }
                normalized[1] = normalized[1].inverse(); // Image's Y axis is downward.
                matrix = Matrix.createAffineTransform(srcEnvelope, axis, dstEnvelope, normalized);
            } else {
                matrix = Matrix.createAffineTransform(srcEnvelope, dstEnvelope);
            }
            return new RenderContext(matrix.toAffineTransform2D(), hints);
        }

        /**
         * Returns the number of elements per value at each position. This is the maximum
         * value plus 1 allowed in <code>getElements(...)</code> methods invocation. The
         * default implementation returns the number of sample dimensions in the coverage.
         */
        public int getNumElements() {
            return getSampleDimensions().length;
        }

        /**
         * Returns all values of a given element for a specified set of coordinates.
         * This method is automatically invoked at rendering time for populating an
         * image tile, providing that the rendered image is created using the
         * &quot;{@link ImageFunctionDescriptor ImageFunction}&quot; operator
         * and the image type is not <code>double</code>.
         * The default implementation invokes {@link Coverage#evaluate(CoordinatePoint,float[])}
         * recursively.
         */
        public void getElements(final float startX, final float startY,
                                final float deltaX, final float deltaY,
                                final int   countX, final int   countY, final int element,
                                final float[] real, final float[] imag)
        {
            int index = 0;
            float[] buffer = null;
            // Clones the coordinate point in order to allow multi-thread invocation.
            final CoordinatePoint coordinate = new CoordinatePoint(this.coordinate);
            coordinate.ord[1] = startY;
            for (int j=0; j<countY; j++) {
                coordinate.ord[0] = startX;
                for (int i=0; i<countX; i++) {
                    buffer = evaluate(coordinate, buffer);
                    real[index++] = buffer[element];
                    coordinate.ord[0] += deltaX;
                }
                coordinate.ord[1] += deltaY;
            }
        }

        /**
         * Returns all values of a given element for a specified set of coordinates.
         * This method is automatically invoked at rendering time for populating an
         * image tile, providing that the rendered image is created using the
         * &quot;{@link ImageFunctionDescriptor ImageFunction}&quot; operator
         * and the image type is  <code>double</code>.
         * The default implementation invokes {@link Coverage#evaluate(CoordinatePoint,double[])}
         * recursively.
         */
        public void getElements(final double startX, final double startY,
                                final double deltaX, final double deltaY,
                                final int    countX, final int    countY, final int element,
                                final double[] real, final double[] imag)
        {
            int index = 0;
            double[] buffer = null;
            // Clones the coordinate point in order to allow multi-thread invocation.
            final CoordinatePoint coordinate = new CoordinatePoint(this.coordinate);
            coordinate.ord[1] = startY;
            for (int j=0; j<countY; j++) {
                coordinate.ord[0] = startX;
                for (int i=0; i<countX; i++) {
                    buffer = evaluate(coordinate, buffer);
                    real[index++] = buffer[element];
                    coordinate.ord[0] += deltaX;
                }
                coordinate.ord[1] += deltaY;
            }
        }
    }

    /**
     * Provides a hint that a coverage will no longer be accessed from a reference in user space.
     * The results are equivalent to those that occur when the program loses its last reference to
     * this coverage, the garbage collector discovers this, and finalize is called. This can be
     * used as a hint in situations where waiting for garbage collection would be overly
     * conservative. The results of referencing a coverage after a call to <code>dispose()</code>
     * are undefined.
     *
     * @see PlanarImage#dispose
     */
    public void dispose() {
        // To be overriden by subclasses.
        // Note: implementing this method in GridCoverage is tricky. We must ensure that:
        //       1) The PlanarImage is not used by somebody else (i.e. is not a user supplied
        //          image, or the user didn't got a reference with getRenderedImage()).
        //       2) If the image is the result of a GridCoverageProcessor operation, it must
        //          removes itself from the WeakValueHashMap.
    }
    
    /**
     * Returns a string représentation of this coverage. This string is
     * for debugging purpose only and may change in future version.
     */
    public String toString() {
        final Locale locale = null;
        final StringBuffer buffer=new StringBuffer(Utilities.getShortClassName(this));
        buffer.append("[\"");
        buffer.append(getName(locale));
        buffer.append('"');
        final Envelope envelope = getEnvelope();
        if (envelope != null) {
            buffer.append(", ");
            buffer.append(envelope);
        }
        final CoordinateSystem cs = getCoordinateSystem();
        if (cs != null) {
            buffer.append(", ");
            buffer.append(Utilities.getShortClassName(cs));
            buffer.append("[\"");
            buffer.append(cs.getName(locale));
            buffer.append("\"]");
        }
        buffer.append(']');
        return buffer.toString();
    }




    /////////////////////////////////////////////////////////////////////////
    ////////////////                                         ////////////////
    ////////////////             OPENGIS ADAPTER             ////////////////
    ////////////////                                         ////////////////
    /////////////////////////////////////////////////////////////////////////

    /**
     * Export a Geotools {@link Coverage} as an OpenGIS {@link CV_Coverage} object.
     * This class is suitable for RMI use. User should not create instance of this
     * class directly. The method {@link Adapters#export(Coverage)} should be used
     * instead.
     *
     * @version $Id: Coverage.java,v 1.20 2003/08/09 22:13:11 desruisseaux Exp $
     * @author Martin Desruisseaux
     */
    protected class Export extends UnicastRemoteObject implements CV_Coverage, PropertySource {
        /**
         * The originating adapter.
         */
        protected final Adapters adapters;

        /**
         * Constructs a remote object. This object is automatically added to the cache in
         * the enclosing {@link Coverage} object. The cached <code>Export</code> instance
         * can be queried with {@link Adapters#export(Coverage)}.
         *
         * @param  adapters The originating adapter.
         * @throws RemoteException if this object can't be exported through RMI.
         */
        protected Export(final Adapters adapters) throws RemoteException {
            super(); // TODO: Fetch the port number from the adapter.
            this.adapters = adapters;
            proxy = new WeakReference(this);
        }

        /**
         * Returns the underlying implementation. Note: This method is not available or remote
         * machine, since we don't implement {@link org.geotools.resources.RemoteProxy}.  This
         * is because {@link Coverage} may not be serializable. For example,
         * {@link org.geotools.gc.GridCoverage} will usually contains a non-serializable
         * {@link java.awt.image.RenderedImage}.
         */
        final Coverage getImplementation() {
            return Coverage.this;
        }

        /**
         * Returns the names of each dimension in the coverage.
         * The default implementation invokes {@link Coverage#getDimensionNames}.
         *
         * @return the names of each dimension in the coverage.
         * @throws RemoteException if a remote call failed. More specifically, the exception will
         *         be an instance of {@link ServerException} if an error occurs on the server side.
         */
        public String[] getDimensionNames() throws RemoteException {
            return Coverage.this.getDimensionNames(null);
        }

        /**
         * Returns The number of sample dimensions in the coverage.
         * The default implementation invokes {@link Coverage#getSampleDimensions}.
         *
         * @return the number of sample dimensions in the coverage.
         * @throws RemoteException if a remote call failed. More specifically, the exception will
         *         be an instance of {@link ServerException} if an error occurs on the server side.
         */
        public int getNumSampleDimensions() throws RemoteException {
            return Coverage.this.getSampleDimensions().length;
        }

        /**
         * Retrieve sample dimension information for the coverage.
         * The default implementation invokes {@link Coverage#getSampleDimensions}.
         *
         * @throws RemoteException if a remote call failed. More specifically, the exception will
         *         be an instance of {@link ServerException} if an error occurs on the server side.
         */
        public CV_SampleDimension getSampleDimension(int index) throws RemoteException {
            return adapters.export(Coverage.this.getSampleDimensions()[index]);
        }

        /**
         * Returns the number of grid coverages which the grid coverage was derived from.
         * The default implementation returns <code>0</code>.
         *
         * @return the number of grid coverages which this coverage was derived from.
         * @throws RemoteException if a remote call failed. More specifically, the exception will
         *         be an instance of {@link ServerException} if an error occurs on the server side.
         */
        public int getNumSources() throws RemoteException {
            return 0;
        }

        /**
         * Returns the source data for a grid coverage.
         * The default implementation throws an {@link ArrayIndexOutOfBoundsException},
         * since {@link #getNumSources} returned 0.
         *
         * @param  sourceDataIndex Source grid coverage index. Indexes start at 0.
         * @return the source data for a grid coverage.
         * @throws RemoteException if a remote call failed. More specifically, the exception will
         *         be an instance of {@link ServerException} if an error occurs on the server side.
         */
        public GC_GridCoverage getSource(int sourceDataIndex) throws RemoteException {
            throw new ArrayIndexOutOfBoundsException(sourceDataIndex);
        }

        /**
         * Returns the list of metadata keywords for a coverage.
         * The default implementation invokes {@link #getPropertyNames}.
         *
         * @return the list of metadata keywords for a coverage.
         * @throws RemoteException if a remote call failed. More specifically, the exception will
         *         be an instance of {@link ServerException} if an error occurs on the server side.
         */
        public String[] getMetadataNames() throws RemoteException {
            return getPropertyNames();
        }

        /**
         * Retrieve the metadata value for a given metadata name.
         * The default implementation invokes {@link #getProperty}
         * and replace {@link java.awt.Image#UndefinedProperty} by
         * <code>null</code>.
         *
         * @param  name Metadata keyword for which to retrieve data.
         * @return the metadata value for a given metadata name.
         * @throws RemoteException if a remote call failed. More specifically, the exception will
         *         be an instance of {@link ServerException} if an error occurs on the server side.
         */
        public String getMetadataValue(String name) throws RemoteException {
            final Object value = getProperty(name);
            return (value!=null && value!=Image.UndefinedProperty) ? value.toString() : null;
        }

        /**
         * Returns an array of {@link String}s recognized as names by this property source.
         * The default implementation invokes {@link Coverage#getPropertyNames()}.
         *
         * @return an array of strings giving the valid property names, or <code>null</code>.
         */
        public String[] getPropertyNames() {
            return Coverage.this.getPropertyNames();
        }

        /**
         * Returns an array of {@link String}s recognized as names by this property source
         * that begin with the supplied prefix. The default implementation invokes {@link
         * Coverage#getPropertyNames(String)}.
         *
         * @return an array of strings giving the valid property names.
         */
        public String[] getPropertyNames(final String prefix) {
            return Coverage.this.getPropertyNames(prefix);
        }

        /**
         * Returns the class expected to be returned by a request for the property with
         * the specified name. If this information is unavailable, <code>null</code> will
         * be returned.
         *
         * @param  name the name of the property.
         * @return The class expected to be return by a request for the value
         *         of this property, or <code>null</code>.
         */
        public Class getPropertyClass(final String name) {
            return Coverage.this.getPropertyClass(name);
        }

        /**
         * Returns the value of a property. If the property name is not recognized,
         * then {@link java.awt.Image#UndefinedProperty} will be returned.
         *
         * @param  name the name of the property.
         * @return the value of the property, or the value
         *         {@link java.awt.Image#UndefinedProperty}.
         */
        public Object getProperty(final String name) {
            return Coverage.this.getProperty(name);
        }

        /**
         * Returns coordinate system used when accessing a coverage or grid coverage
         * with the <code>evaluate</code> methods. The default implementation invokes
         * {@link Coverage#getCoordinateSystem}.
         *
         * @return the coordinate system used when accessing a coverage or
         *         grid coverage with the <code>evaluate</code> methods.
         * @throws RemoteException if a remote call failed. More specifically, the exception will
         *         be an instance of {@link ServerException} if an error occurs on the server side.
         */
        public CS_CoordinateSystem getCoordinateSystem() throws RemoteException {
            return adapters.CTS.export(Coverage.this.getCoordinateSystem());
        }

        /**
         * Returns the bounding box for the coverage domain in coordinate system coordinates.
         * The default implementation invokes {@link Coverage#getEnvelope}.
         *
         * @return the bounding box for the coverage domain in coordinate system coordinates.
         * @throws RemoteException if a remote call failed. More specifically, the exception will
         *         be an instance of {@link ServerException} if an error occurs on the server side.
         */
        public PT_Envelope getEnvelope() throws RemoteException {
            return adapters.CTS.export(Coverage.this.getEnvelope());
        }

        /**
         * Return the value vector for a given point in the coverage.
         * The default implementation invokes one of other <code>CV_Coverage</code> method
         * (for example {@link #evaluateAsDouble}) according the underlying data type.
         *
         * @param  point point at which to find the grid values.
         * @return the value vector for a given point in the coverage.
         * @throws RemoteException if a remote call failed. More specifically, the exception will
         *         be an instance of {@link ServerException} if an error occurs on the server side.
         *
         * @task TODO: Check the underlying data type.
         */
        public Object evaluate(PT_CoordinatePoint point) throws RemoteException {
            return evaluateAsDouble(point);
        }

        /**
         * Return a sequence of Boolean values for a given point in the coverage.
         * The default implementation invokes {@link Coverage#evaluate(CoordinatePoint, boolean[])}.
         *
         * @param  point point at which to find the coverage values.
         * @return a sequence of boolean values for a given point in the coverage.
         * @throws RemoteException if a remote call failed. More specifically, the exception will
         *         be an instance of {@link ServerException} if an error occurs on the server side.
         */
        public boolean[] evaluateAsBoolean(PT_CoordinatePoint point) throws RemoteException {
            return Coverage.this.evaluate(adapters.CTS.wrap(point), (boolean[]) null);
        }

        /**
         * Return a sequence of unsigned byte values for a given point in the coverage.
         * The default implementation invokes {@link Coverage#evaluate(CoordinatePoint, byte[])}.
         *
         * @param  point point at which to find the coverage values.
         * @return a sequence of unsigned byte values for a given point in the coverage.
         * @throws RemoteException if a remote call failed. More specifically, the exception will
         *         be an instance of {@link ServerException} if an error occurs on the server side.
         */
        public byte[] evaluateAsByte(PT_CoordinatePoint point) throws RemoteException {
            return Coverage.this.evaluate(adapters.CTS.wrap(point), (byte[]) null);
        }

        /**
         * Return a sequence of integer values for a given point in the coverage.
         * The default implementation invokes {@link Coverage#evaluate(CoordinatePoint, int[])}.
         *
         * @param  point point at which to find the grid values.
         * @return a sequence of integer values for a given point in the coverage.
         * @throws RemoteException if a remote call failed. More specifically, the exception will
         *         be an instance of {@link ServerException} if an error occurs on the server side.
         */
        public int[] evaluateAsInteger(PT_CoordinatePoint point) throws RemoteException {
            return Coverage.this.evaluate(adapters.CTS.wrap(point), (int[]) null);
        }

        /**
         * Return a sequence of double values for a given point in the coverage.
         * The default implementation invokes {@link Coverage#evaluate(CoordinatePoint, double[])}.
         *
         * @param  point point at which to find the grid values.
         * @return a sequence of double values for a given point in the coverage.
         * @throws RemoteException if a remote call failed. More specifically, the exception will
         *         be an instance of {@link ServerException} if an error occurs on the server side.
         */
        public double[] evaluateAsDouble(PT_CoordinatePoint point) throws RemoteException {
            return Coverage.this.evaluate(adapters.CTS.wrap(point), (double[]) null);
        }
    }
}
