/*
 * Geotools 2 - OpenSource mapping toolkit
 * (C) 2003, Geotools Project Managment Committee (PMC)
 * (C) 2001, Institut de Recherche pour le Développement
 * (C) 1998, Pêches et Océans Canada
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
package org.geotools.renderer.geom;

// Geometry and graphics
import java.awt.Shape;
import java.awt.Rectangle;
import java.awt.Graphics2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.PathIterator;
import java.awt.geom.AffineTransform;
import java.awt.geom.FlatteningPathIterator;
import java.awt.geom.IllegalPathStateException;
import java.awt.geom.NoninvertibleTransformException;

// Collections
import java.util.List;
import java.util.Arrays;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.Collection;
import java.util.NoSuchElementException;

// Input/Output
import java.io.Writer;
import java.io.PrintWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.OutputStreamWriter;

// Formatting
import java.util.Locale;
import java.text.NumberFormat;
import java.text.FieldPosition;

// Geotools dependencies
import org.geotools.units.Unit;
import org.geotools.cs.Ellipsoid;
import org.geotools.cs.Projection;
import org.geotools.cs.CoordinateSystem;
import org.geotools.cs.CoordinateSystemFactory;
import org.geotools.cs.ProjectedCoordinateSystem;
import org.geotools.cs.GeographicCoordinateSystem;
import org.geotools.ct.CannotCreateTransformException;
import org.geotools.ct.CoordinateTransformation;
import org.geotools.ct.TransformException;
import org.geotools.ct.MathTransform2D;
import org.geotools.ct.MathTransform;

// Miscellaneous
import org.geotools.util.WeakHashSet;
import org.geotools.math.Statistics;
import org.geotools.resources.XMath;
import org.geotools.resources.XArray;
import org.geotools.resources.Arguments;
import org.geotools.resources.Utilities;
import org.geotools.resources.XRectangle2D;
import org.geotools.resources.CTSUtilities;
import org.geotools.resources.renderer.Resources;
import org.geotools.resources.renderer.ResourceKeys;
import org.geotools.renderer.array.PointArray;
import org.geotools.renderer.array.ArrayData;


/**
 * A single polygon. Each <code>Polygon</code> object can have its own {@link CoordinateSystem}
 * object, usually specified at construction time. A set of polygons can be built from an array
 * of (<var>x</var>,<var>y</var>) coordinates or from a geometric shape using one of
 * {@link #getInstances(float[],CoordinateSystem) getInstances(...)} factory methods.
 * <strong>Points given to factory methods should not contain map border.</strong>
 * Border points (orange points in the figure below) are treated specially and must
 * be specified using {@link #appendBorder appendBorder(...)} or
 * {@link #prependBorder prependBorder(...)} methods.
 *
 * <p align="center"><img src="doc-files/borders.png"></p>
 *
 * <TABLE WIDTH="80%" ALIGN="center" CELLPADDING="18" BORDER="4" BGCOLOR="#FFE0B0"><TR><TD>
 * <P ALIGN="justify"><STRONG>This class may change in a future version, hopefully toward
 * ISO-19107. Do not rely on it.</STRONG>
 * </TD></TR></TABLE>
 *
 * @version $Id: Polygon.java,v 1.11 2003/05/19 15:07:19 desruisseaux Exp $
 * @author Martin Desruisseaux
 *
 * @see Isoline
 */
public class Polygon extends GeoShape {
    /**
     * Version number for compatibility with geometries serialized with previous versions
     * of this class.
     */
    private static final long serialVersionUID = 6197907210475790821L;

    /**
     * Small number for comparisons (mostly in assertions).
     * Should be in the range of precision of <code>float</code> type.
     */
    private static final double EPS = 1E-6;

    /**
     * Projection to use for calculations that require a Cartesian coordinate system.
     */
    private static final String CARTESIAN_PROJECTION = "Stereographic";

    /**
     * The enum value for <code>InteriorType == null</code>.
     */
    private static final int UNCLOSED = InteriorType.UNCLOSED;

    /**
     * Last coordinate transformation used for computing {@link #coordinateTransform}.
     * Used in order to avoid the costly call to {@link CoordinateSystemFactory} methods
     * when the same transform is requested many consecutive time, which is a very common
     * situation.
     */
    private static CoordinateTransformation lastCoordinateTransform =
                    getIdentityTransform(GeographicCoordinateSystem.WGS84);

    /**
     * Un des maillons de la chaîne de polylignes, ou
     * <code>null</code> s'il n'y a aucune donnée de
     * mémorisée.
     */
    private Polyline data;

    /**
     * Transformation that allows you to pass from the <code>data</code> point coordinate system
     * to the coordinate system of this polygon. {@link CoordinateTransformation#getSourceCS}
     * absolutely must be the <code>data</code> coordinate system, whilst
     * {@link CoordinateTransformation#getTargetCS} must be the polygon's coordinate system.
     * When this polygon uses the same coordinate system as <code>data</code> (which is normally
     * the case), this field will contain an identity transformation.
     * This field can be null if <code>data</code>'s coordinate system is unknown.
     */
    private CoordinateTransformation coordinateTransform;

    /**
     * Rectangle completely encompassing all <code>data</code>'s points. This
     * rectangle is very useful for quickly spotting features which don't need to be
     * redrawn (for example, when zoomed in on).
     * <strong>The rectangle {@link Rectangle2D} referenced by this field must never be
     * modified</strong>, as it could be shared by several objects {@link Polygon}.
     */
    private transient Rectangle2D dataBounds;

    /**
     * Rectangle completely encompassing the projected coordinates of this polygon.
     * This field is used as a cache for the {@link #getBounds2D()} method to make it
     * quicker.
     *
     * <strong>The {@link Rectangle2D} rectangle referenced by this field should never be
     * modified</strong>, as it could be shared by several objects {@link Polygon}.
     */
    private transient Rectangle2D bounds;

    /**
     * <code>true</code> if {@link #getPathIterator} will return a flattened iterator.
     * In this case, there is no need to wrap it into a {@link FlatteningPathIterator}.
     */
    private transient boolean flattened;

    /**
     * Indicates whether this shape has been closed. If the polygon has been closed, this field
     * will have the value {@link InteriorType#ELEVATION} or {@link InteriorType#DEPRESSION}.
     */
    private byte interiorType = (byte) UNCLOSED;

    /**
     * The resolution to apply at rendering time.
     * The value 0 means that all data should be used.
     */
    private transient float renderingResolution;

    /**
     * Soft reference to a <code>float[]</code> table. This table is used to
     * keep in memory the points that have already been projected or transformed.
     */
    private transient PolygonCache cache;

    /**
     * Constructs a polygon which is initially empty.
     */
    private Polygon(final CoordinateTransformation coordinateTransform) {
        this.coordinateTransform = coordinateTransform;
        if (coordinateTransform != null) {
            CoordinateSystem cs;
            if ((cs=coordinateTransform.getSourceCS()).getDimension() != 2 ||
                (cs=coordinateTransform.getTargetCS()).getDimension() != 2)
            {
                throw new IllegalArgumentException(org.geotools.resources.cts.Resources.format(
                   org.geotools.resources.cts.ResourceKeys.ERROR_CANT_REDUCE_TO_TWO_DIMENSIONS_$1, cs));
            }
        }
        flattened = checkFlattenedShape();
    }

    /**
     * Construct an empty polygon. Use {@link #append} to add points.
     *
     * @param coordinateSystem The coordinate system to use for all
     *        points in this polygon, or <code>null</code> if unknown.
     */
    public Polygon(final CoordinateSystem coordinateSystem) {
        this(getIdentityTransform(getCoordinateSystem2D(coordinateSystem)));
    }

    /**
     * Construct a new polygon with the same data as the specified
     * polygon. The new polygon will have a copy semantic. However,
     * implementation tries to share as much internal data as possible
     * in order to reduce memory footprint.
     */
    public Polygon(final Polygon polygon) {
        super(polygon);
        data                = Polyline.clone(polygon.data);
        coordinateTransform = polygon.coordinateTransform;
        dataBounds          = polygon.dataBounds;
        bounds              = polygon.bounds;
        flattened           = polygon.flattened;
        interiorType        = polygon.interiorType;
    }

    /**
     * Construct a closed polygon with the specified rectangle.
     * The new polygon will be empty if the rectangle was empty
     * or contains at least one <code>NaN</code> value.
     *
     * @param rectangle Rectangle to copy in the new polygon.
     * @param coordinateSystem The rectangle's coordinate system,
     *        or <code>null</code> if unknown.
     */
    Polygon(final PointArray data, final CoordinateSystem coordinateSystem) {
        this(coordinateSystem);
        this.data = new Polyline(data);
    }

    /**
     * Construct a closed polygon with the specified rectangle.
     * The new polygon will be empty if the rectangle was empty
     * or contains at least one <code>NaN</code> value.
     *
     * @param rectangle Rectangle to copy in the new polygon.
     * @param coordinateSystem The rectangle's coordinate system,
     *        or <code>null</code> if unknown.
     */
    public Polygon(final Rectangle2D rectangle, final CoordinateSystem coordinateSystem) {
        this(coordinateSystem);
        if (!rectangle.isEmpty()) {
            final float xmin = (float)rectangle.getMinX();
            final float ymin = (float)rectangle.getMinY();
            final float xmax = (float)rectangle.getMaxX();
            final float ymax = (float)rectangle.getMaxY();
            final Polyline[] polylines = Polyline.getInstances(new float[] {
                xmin,ymin,
                xmax,ymin,
                xmax,ymax,
                xmin,ymax
            });
            if (polylines.length == 1) {
                // length may be 0 or 2 if some points contain NaN
                data = polylines[0];
            }
        }
    }

    /**
     * Constructs polygons from specified (<var>x</var>,<var>y</var>) coordinates.
     * <code>NaN</code> values at the beginning and end of <code>data</code> will be ignored.
     * Those that appear in the middle will separate the feature in a number of polygons.
     *
     * @param  data Coordinates table (may contain NaNs). These data will be copied,
     *         in such a way that any future modifications of <code>data</code> will have no impact
     *         on the polygons created.
     * @param  coordinateSystem <code>data</code> point coordinate system.
     *         This argument can be null if the coordinate system is unknown.
     * @return Polygon table. May have 0 length, but will never be null.
     */
    public static Polygon[] getInstances(final float[] data, final CoordinateSystem coordinateSystem) {
        final Polyline[] polylines = Polyline.getInstances(data);
        final Polygon[]  polygons  = new Polygon[polylines.length];
        final CoordinateTransformation ct = getIdentityTransform(coordinateSystem);
        for (int i=0; i<polygons.length; i++) {
            final Polygon polygon = new Polygon(ct);
            polygon.data      = polylines[i];
            polygon.flattened = polygon.checkFlattenedShape();
            polygons[i]       = polygon;
        }
        return polygons;
    }

    /**
     * Constructs polygons from the specified geometric shape. If <code>shape</code>
     * is already from the <code>Polygon</code> class, it will be returned in a table of length 1.
     * In all other cases, this method can return a table of 0 length, but never returns
     * <code>null</code>.
     *
     * @param  shape Geometric shape to copy in one or more polygons.
     * @param  coordinateSystem <code>shape</code> point coordinate system.
     *         This argument may be null if the coordinate system is unknown.
     * @return Polygon table.  Can have 0 length, but will never be null.
     */
    public static Polygon[] getInstances(final Shape shape, CoordinateSystem coordinateSystem) {
        coordinateSystem = getCoordinateSystem2D(coordinateSystem);
        if (shape instanceof Polygon) {
            return new Polygon[] {(Polygon) shape};
        }
        final CoordinateTransformation ct = getIdentityTransform(coordinateSystem);
        final List               polygons = new ArrayList();
        final PathIterator            pit = shape.getPathIterator(null, getFlatness(shape));
        final float[]              buffer = new float[6];
        float[]                     array = new float[64];
        while (!pit.isDone()) {
            if (pit.currentSegment(array) != PathIterator.SEG_MOVETO) {
                throw new IllegalPathStateException();
            }
            /*
             * Once in this block, the table <code>array</code> already contains
             * the first point at index 0 (for x) and 1 (for y). Now the other points
             * are added so that they correspond to the <code>LINETO</code> instructions.
             */
            int index = 2;
            InteriorType interiorType = null;
      loop: for (pit.next(); !pit.isDone(); pit.next()) {
                switch (pit.currentSegment(buffer)) {
                    case PathIterator.SEG_LINETO: {
                        if (index >= array.length) {
                            array = XArray.resize(array, 2*index);
                        }
                        System.arraycopy(buffer, 0, array, index, 2);
                        index += 2;
                        break;
                    }
                    case PathIterator.SEG_MOVETO: {
                        break loop;
                    }
                    case PathIterator.SEG_CLOSE: {
                        interiorType = InteriorType.FLAT;
                        pit.next();
                        break loop;
                    }
                    default: {
                        throw new IllegalPathStateException();
                    }
                }
            }
            /*
             * Construit les polygones qui correspondent à
             * la forme géométrique qui vient d'être balayée.
             */
            final Polyline[] polylines = Polyline.getInstances(array, 0, index);
            for (int i=0; i<polylines.length; i++) {
                final Polygon polygon = new Polygon(ct);
                polygon.data = polylines[i];
                polygon.flattened = polygon.checkFlattenedShape();
                polygon.close(interiorType);
                polygons.add(polygon);
            }
        }
        return (Polygon[]) polygons.toArray(new Polygon[polygons.size()]);
    }

    /**
     * Returns a suggested value for the <code>flatness</code> argument in
     * {@link Shape#getPathIterator(AffineTransform,double)} for the specified shape.
     */
    static double getFlatness(final Shape shape) {
        final Rectangle2D bounds = shape.getBounds2D();
        return 0.025*Math.max(bounds.getHeight(), bounds.getWidth());
    }

    /**
     * Same as {@link CTSUtilities#getCoordinateSystem2D}, but wraps the {@link TransformException}
     * into an {@link IllegalArgumentException}. Used for constructors only. Other methods still
     * use the method throwing a transform exception.
     */
    private static CoordinateSystem getCoordinateSystem2D(final CoordinateSystem cs)
            throws IllegalArgumentException
    {
        try {
            return CTSUtilities.getCoordinateSystem2D(cs);
        } catch (TransformException exception) {
            throw new IllegalArgumentException(exception.getLocalizedMessage());
        }
    }

    /**
     * Returns the native coordinate system of {@link #data}'s points, or <code>null</code>
     * if unknown.
     */
    private CoordinateSystem getInternalCS() {
        // copy 'coordinateTransform' reference in order to avoid synchronization
        final CoordinateTransformation coordinateTransform = this.coordinateTransform;
        return (coordinateTransform!=null) ? coordinateTransform.getSourceCS() : null;
    }

    /**
     * Returns the polygon's coordinate system, or <code>null</code> if unknown.
     */
    public CoordinateSystem getCoordinateSystem() {
        // copy 'coordinateTransform' reference in order to avoid synchronization
        final CoordinateTransformation coordinateTransform = this.coordinateTransform;
        return (coordinateTransform!=null) ? coordinateTransform.getTargetCS() : null;
    }

    /**
     * Returns the transform which allows you to pass from the {@link #data} point coordinate
     * system to the specified coordinate system.  If at least one of the coordinate systems
     * is unknown, this method returns <code>null</code>.
     *
     * @throws CannotCreateTransformException If the transform cannot be created.
     */
    final CoordinateTransformation getTransformationFromInternalCS(final CoordinateSystem cs)
            throws CannotCreateTransformException
    {
        // copy 'coordinateTransform' reference in order to avoid synchronization
        CoordinateTransformation coordinateTransform = this.coordinateTransform;
        if (cs!=null && coordinateTransform!=null) {
            if (cs.equals(coordinateTransform.getTargetCS(), false)) {
                return coordinateTransform;
            }
            coordinateTransform = lastCoordinateTransform;
            if (cs.equals(coordinateTransform.getTargetCS(), false)) {
                if (coordinateTransform.getSourceCS().equals(getInternalCS(), false)) {
                    return coordinateTransform;
                }
            }
            coordinateTransform=getCoordinateTransformation(coordinateTransform.getSourceCS(), cs);
            lastCoordinateTransform = coordinateTransform;
            return coordinateTransform;
        }
        return null;
    }

    /**
     * Returns a math transform for the specified transformations.
     * If no transformation is available, or if it is the identity
     * transform, then this method returns <code>null</code>. This
     * method accepts null argument.
     */
    static MathTransform2D getMathTransform2D(final CoordinateTransformation transformation) {
        if (transformation != null) {
            final MathTransform transform = transformation.getMathTransform();
            if (!transform.isIdentity()) {
                return (MathTransform2D) transform;
            }
        }
        return null;
    }

    /**
     * Sets the polygon's coordinate system. Calling this method is equivalent
     * to reprojecting all polygon's points from the old coordinate system to the
     * new one.
     *
     * @param  The new coordinate system. A <code>null</code> value resets the
     *         coordinate system given at construction time.
     * @throws TransformException If a transformation failed. In case of failure,
     *         the state of this object will stay unchanged (as if this method has
     *         never been invoked).
     */
    public synchronized void setCoordinateSystem(CoordinateSystem coordinateSystem)
            throws TransformException
    {
        // Do not use 'Polygon.getCoordinateSystem2D', since
        // we want a 'TransformException' in case of failure.
        coordinateSystem = CTSUtilities.getCoordinateSystem2D(coordinateSystem);
        if (coordinateSystem == null) {
            coordinateSystem = getInternalCS();
            // May still null. Its ok.
        }
        final CoordinateTransformation transformCandidate =
                getTransformationFromInternalCS(coordinateSystem);
        /*
         * Compute bounds now. The getBounds2D(...) method scans every point.
         * Consequently, if an exception must be thrown, it will be thrown now.
         */
        bounds = Polyline.getBounds2D(data, (MathTransform2D)transformCandidate.getMathTransform());
        /*
         * Store the new coordinate transform
         * only after projection has succeeded.
         */
        this.coordinateTransform = transformCandidate;
        this.cache = null;
        this.flattened = checkFlattenedShape();
    }

    /**
     * Indicates whether the specified transform is the identity transform.
     * A null transform (<code>null</code>) is considered to be an identity transform.
     */
    private static boolean isIdentity(final CoordinateTransformation coordinateTransform) {
        return coordinateTransform==null || coordinateTransform.getMathTransform().isIdentity();
    }

    /**
     * Test if this polygon is empty. An
     * empty polygon contains no points.
     */
    public synchronized boolean isEmpty() {
        return Polyline.getPointCount(data) == 0;
    }

    /**
     * Return the bounding box of this polygon, including its possible
     * borders. This method uses a cache, such that after a first calling,
     * the following calls should be fairly quick.
     *
     * @return A bounding box of this polygon. Changes to the
     *         fields of this rectangle will not affect the cache.
     */
    public synchronized Rectangle2D getBounds2D() {
        return (Rectangle2D) getCachedBounds().clone();
    }

    /**
     * Returns the smallest bounding box containing {@link #getBounds2D}.
     *
     * @deprecated This method is required by the {@link Shape} interface,
     *             but it doesn't provide enough precision for most cases.
     *             Use {@link #getBounds2D()} instead.
     */
    public synchronized Rectangle getBounds() {
        final Rectangle bounds = new Rectangle();
        bounds.setRect(getCachedBounds()); // 'setRect' effectue l'arrondissement correct.
        return bounds;
    }

    /**
     * Returns a rectangle encompassing all {@link #data}'s points. Because this method returns
     * the rectangle directly from the cache and not a copy, the returned rectangle should
     * never be modified.
     *
     * @return A rectangle encompassing all {@link #data}'s points.
     *         This rectangle may be empty, but will never be null.
     */
    private Rectangle2D getDataBounds() {
        // assert Thread.holdsLock(this);
        // Can't make this assertion, because this method is invoked
        // by {@link #getCachedBounds}. See later for details.

        if (dataBounds == null) {
            dataBounds = getBounds(data, null);
            if (isIdentity(coordinateTransform)) {
                bounds = dataBounds; // Avoid computing the same rectangle twice
            }
        }
        assert equalsEps(getBounds(data, null), dataBounds) : dataBounds;
        return dataBounds;
    }

    /**
     * Return the bounding box of this isoline. This method returns
     * a direct reference to the internally cached bounding box. DO
     * NOT MODIFY!
     */
    final Rectangle2D getCachedBounds() {
        assert Thread.holdsLock(this);
        if (bounds == null) {
            bounds = getBounds(data, coordinateTransform);
            if (isIdentity(coordinateTransform)) {
                dataBounds = bounds; // Avoid computing the same rectangle twice
            }
        }
        assert equalsEps(getBounds(data, coordinateTransform), bounds) : bounds;
        return bounds;
    }

    /**
     * Returns a rectangle encompassing all the points projected in the specified coordinate system.
     * This method will try to return one of the rectangles from the internal cache when appropriate.
     * Because this method can return the rectangle directly from the cache and not a copy, the
     * returned rectangle should never be modified.
     *
     * @param  The coordinate system according to which the points should be projected.
     * @return A rectangle encompassing all the points of this polygon.
     *         This rectangle may be empty, but will never be null.
     * @throws TransformException if a cartographic projection fails.
     */
    private Rectangle2D getCachedBounds(final CoordinateSystem coordinateSystem)
            throws TransformException
    {
        // assert Thread.holdsLock(this);
        // Can't make this assertion, because {@link #intersects(Polygon,boolean)} invokes
        // this method without synchronization on this polygon. In doesn't hurt as long as
        // {@link #intersectsPolygon} and {@link #intersectsEdge} are private methods.

        if (Utilities.equals(getInternalCS(),       coordinateSystem)) return getDataBounds();
        if (Utilities.equals(getCoordinateSystem(), coordinateSystem)) return getCachedBounds();
        Rectangle2D bounds = Polyline.getBounds2D(data, getMathTransform2D(coordinateTransform));
        if (bounds == null) {
            bounds = new Rectangle2D.Float();
        }
        return bounds;
    }

    /**
     * Returns a rectangle encompassing all <code>data</code>'s points.  This method should
     * only be called in a context where it is known that the cartographic projection 
     * should never fail.
     *
     * @param  data One of the links in the chain of point tables (may be null).
     * @param  coordinateTransform Transform to apply on <code>data</code>'s points.
     * @return A rectangle encompassing all <code>data</code>'s points.
     *         This rectangle may be empty, but will never be null.
     */
    private static Rectangle2D getBounds(final Polyline data,
                                         final CoordinateTransformation coordinateTransform)
    {
        Rectangle2D bounds;
        try {
            bounds = Polyline.getBounds2D(data, getMathTransform2D(coordinateTransform));
            if (bounds == null) {
                assert Polyline.getPointCount(data) == 0;
                bounds = new Rectangle2D.Float();
            }
        } catch (TransformException exception) {
            // Should not happen, since {@link #setCoordinateSystem}
            // has already successfully projected every point.
            unexpectedException("getBounds2D", exception);
            bounds = null;
        }
        return bounds;
    }

    /**
     * Check if two rectangles are almost equal (except for an epsilon value).  If one or
     * both arguments are <code>null</code>, then this method does nothing. This method occurs
     * when one rectangle comes from the cache and hasn't been computed yet.  This method is
     * used for assertions only.
     */
    private static boolean equalsEps(final Rectangle2D expected, final Rectangle2D actual) {
        if (expected==null || actual==null) {
            return true;
        }
        final double eps = EPS * XMath.hypot(expected.getCenterX(), expected.getCenterY());
        return Math.abs(expected.getMinX() - actual.getMinX()) <= eps &&
               Math.abs(expected.getMinY() - actual.getMinY()) <= eps &&
               Math.abs(expected.getMaxX() - actual.getMaxX()) <= eps &&
               Math.abs(expected.getMaxY() - actual.getMaxY()) <= eps;
    }

    /**
     * Indicates whether the specified (<var>x</var>,<var>y</var>) coordinate is inside this
     * polygon.  The polygon should have been closed before the call to this method
     * (see {@link #close}), without which this method will always return <code>false</code>.
     *
     * @param  x <var>x</var> coordinate of the point to test.
     * @param  y <var>y</var> coordinate of the point to test.
     * @param  transformation Transform to use for converting {@link #data}'s points,
     *         or <code>null</code> if no transform is required. If a non-null transform
     *         is specified, it should have been obtained by a call to the method 
     *         <code>getTransformationFromInternalCS(targetCS)</code>. All the polygon's points
     *         will then be projected according to the <code>targetCS</code> coordinate system. Where
     *         possible, it is more efficient to calculate only the inverse projection of point
     *         (<var>x</var>,<var>y</var>) and to specify <code>null</code> for this argument.
     * @return <code>true</code> if the point is inside this polygon.
     *
     * @author André Gosselin (original C version)
     * @author Martin Desruisseaux (Java adaptation)
     */
    private boolean contains(final float x, final float y,
                             final CoordinateTransformation transformation)
    {
        assert interiorType!=UNCLOSED : interiorType;
        /*
         * Imagine a straight line starting at point (<var>x</var>,<var>y</var>)
         * and going to infinity to the right of this point (i.e., towards the <var>x</var>
         * positive axis). We count the number of times the polygon intercepts this line.
         * If the number is odd, the point is inside the polygon. The variable <code>nInt</code>
         * will do this counting.
         */
        int   nInt                 = 0;
        int   intSuspended         = 0;
        int   nPointsToRecheck     = 0;
        final Point2D.Float nextPt = new Point2D.Float();
        final Polyline.Iterator it = new Polyline.Iterator(data, getMathTransform2D(transformation));
        float x1                   = Float.NaN;
        float y1                   = Float.NaN;
        /*
         * Extracts a first point.  There will be a problem in the following algorithm if the
         * first point is on the same horizontal line as the point to check.
         * To solve the problem, we look for the first point that isn't on the same horizontal
         * line.
         */
        while (true) {
            final float x0=x1;
            final float y0=y1;
            nPointsToRecheck++;
            if (it.next(nextPt) == null) {
                return false;
            }
            x1 = nextPt.x;
            y1 = nextPt.y;
            if (y1 != y) break;
            /*
             * Checks whether the point falls exactly on the
             * segment (x0,y0)-(x1-y1). If it does,
             * it's not worth going any further.
             */
            if (x0 < x1) {
                if (x>=x0 && x<=x1) return true;
            } else {
                if (x>=x1 && x<=x0) return true;
            }
        }
        /*
         * Sweeps through all the points of the polygon. When the last point is extracted
         * the variable <code>count</code> is adjusted so that only the points that need
         * passing through again are 're-swept'.
         */
        for (int count=-1; count!=0; count--) {
            /*
             * Obtains the following point.  If we have reached the end of the polygon,
             * we reclose the polygon if this has not already been done.
             * If the polygon had already been reclosed, that is the end of the loop.
             */
            final float x0=x1;
            final float y0=y1;
            if (it.next(nextPt) == null) {
                count = nPointsToRecheck+1;
                nPointsToRecheck = 0;
                it.rewind();
                continue;
            }
            x1=nextPt.x;
            y1=nextPt.y;
            /*
             * We now have a right-hand segment going from the coordinates
             * (<var>x0</var>,<var>y0</var>) to (<var>x1</var>,<var>y1</var>).
             * If we realise that the right-hand segment is completely above or completely
             * below the point (<var>x</var>,<var>y</var>), we know that there is no right-hand
             * intersection and we continue the loop.
             */
            if (y0 < y1) {
                if (y<y0 || y>y1) continue;
            } else {
                if (y<y1 || y>y0) continue;
            }
            /*
             * We now know that our segment passes either to the right or the left of our point.
             * We now calculate the coordinate <var>xi</var> where the intersection takes place
             * (with the horizontal right passing through our point).
             */
            final float dy = y1-y0;
            final float xi = x0 + (x1-x0)*(y-y0)/dy;
            if (!Float.isInfinite(xi) && !Float.isNaN(xi)) {
                /*
                 * If the intersection is completely to the left of the point, there is evidently
                 * no intersection to the right and we continue the loop.
                 * Otherwise, if the intersection occurs precisely at the coordinate <var>x</var>
                 * (which is unlikely...), this means our point is exactly on the border of the polygon
                 * and the treatment ends.
                 */
                if (x >  xi) continue;
                if (x == xi) return true;
            } else {
                /*
                 * There is a special treatment if the segment is horizontal. The value
                 * <var>xi</var> isn't valid (we can visualize that as if intersections were 
                 * found all over the right-hand side rather than on a single point). Instead
                 * of performing checks with <var>xi</var>, we will do them with the minimum and
                 * maximum <var>x</var>s of the segment.
                 */
                if (x0 < x1) {
                    if (x >  x1) continue;
                    if (x >= x0) return true;
                } else {
                    if (x >  x0) continue;
                    if (x >= x1) return true;
                }
            }
            /*
             * We now know that there is an intersection on the right.  In principal, it
             * would be sufficient to increment 'nInt'.  However, we should pay particular
             * attention to the case where <var>y</var> is at exactly the same height as one of the
             * extremities of the segment.  Is there an intersection or not?  That depends on
             * whether the following segments continue in the same direction or not.  We adjust
             * a flag, so that the decision to increment 'nInt' or not is taken later in the loop
             * when the other segments have been examined.
             */
            if (x0==x1 && y0==y1) {
                continue;
            }
            if (y==y0 || y==y1) {
                final int sgn=XMath.sgn(dy);
                if (sgn != 0) {
                    if (intSuspended!=0) {
                        if (intSuspended==sgn) nInt++;
                        intSuspended=0;
                    } else {
                        intSuspended=sgn;
                    }
                }
            }
            else nInt++;
        }
        /*
         * If the number of intersections to the right of the point is odd, the point is
         * inside the polygon.  Otherwise, it is outside.
         */
        return (nInt & 1)!=0;
    }

    /**
     * Indicates whether the specified (<var>x</var>,<var>y</var>) coordinate is inside
     * this polygon.  The point's coordinates must be expressed according to the polygon's
     * coordinate system, that is {@link #getCoordinateSystem()}. The polygon must also
     * have been closed before the call to this method (see {@link #close}), if it wasn't
     * this method will always return <code>false</code>.
     */
    public synchronized boolean contains(double x, double y) {
        if (interiorType == UNCLOSED) {
            return false;
        }
        // IMPLEMENTATION NOTE: The polygon's native point array ({@link #data}) and the
        // (x,y) point may use different coordinate systems. For efficiency reasons, the
        // (x,y) point is projected to the "native" polygon's coordinate system instead
        // of projecting all polygon's points. As a result, points very close to the polygon's
        // edge may appear inside (when viewed on screen) while this method returns <code>false</code>,
        // and vice-versa. This is because some projections transform straight lines
        // into curves, but the Polygon class ignores curves and always uses straight
        // lines between any two points.
        if (coordinateTransform!=null) try {
            final MathTransform transform = coordinateTransform.getMathTransform();
            if (!transform.isIdentity()) {
                Point2D point = new Point2D.Double(x,y);
                point = ((MathTransform2D) transform.inverse()).transform(point, point);
                x = point.getX();
                y = point.getY();
            }
        } catch (TransformException exception) {
            // If the projection fails, the point is probably outside the polygon
            // (since all the polygon's points are projectable).
            return false;
        }
        /*
         * First we check whether the rectangle 'dataBounds' contains
         * the point, before calling the costly method 'contains'.
         */
        return getDataBounds().contains(x,y) && contains((float)x, (float)y, null);
    }

    /**
     * Checks whether a point <code>pt</code> is inside this polygon. The point's coordinates
     * must be expressed according to the polygon's coordinate system, that is
     * {@link #getCoordinateSystem()}. The polygon must also have been closed before the call
     * to this method (see {@link #close}), if it wasn't this method will always return
     * <code>false</code>.
     */
    public boolean contains(final Point2D pt) {
        return contains(pt.getX(), pt.getY());
    }

    /**
     * Test if the interior of this contour entirely contains the given rectangle.
     * The rectangle's coordinates must be expressed in this contour's coordinate
     * system (as returned by {@link #getCoordinateSystem}).
     */
    public synchronized boolean contains(final Rectangle2D rect) {
        return containsPolygon(new Polygon(rect, getCoordinateSystem()));
    }

    /**
     * Test if the interior of this polygon
     * entirely contains the given shape.
     */
    public synchronized boolean contains(final Shape shape) {
        if (shape instanceof Polygon) {
            return containsPolygon((Polygon) shape);
        }
        final Polygon[] polygons = getInstances(shape, getCoordinateSystem());
        for (int i=0; i<polygons.length; i++) {
            if (!containsPolygon(polygons[i])) {
                return false;
            }
        }
        return polygons.length!=0;
    }

    /**
     * Test if the interior of this polygon
     * entirely contains the given polygon.
     */
    private boolean containsPolygon(final Polygon shape) {
        /*
         * This method returns <code>true</code> if this polygon contains at least
         * one point of <code>shape</code> and there is no intersection 
         * between <code>shape</code> and <code>this</code>.
         */
        if (interiorType != UNCLOSED) try {
            final CoordinateSystem coordinateSystem = getInternalCS();
            if (getDataBounds().contains(shape.getCachedBounds(coordinateSystem))) {
                final Point2D.Float firstPt = new Point2D.Float();
                final  Line2D.Float segment = new  Line2D.Float();
                final Polyline.Iterator  it = new Polyline.Iterator(shape.data,
                                          shape.getMathTransform2D(
                                          shape.getTransformationFromInternalCS(coordinateSystem)));
                if (it.next(firstPt)!=null && contains(firstPt.x, firstPt.y, null)) {
                    segment.x2 = firstPt.x;
                    segment.y2 = firstPt.y;
                    do if (!it.next(segment)) {
                        if (shape.interiorType==UNCLOSED || isSingular(segment)) {
                            return true;
                        }
                        segment.x2 = firstPt.x;
                        segment.y2 = firstPt.y;
                    } while (!intersects(segment));
                }
            }
        } catch (TransformException exception) {
            // Conservatively returns 'false' if some points from 'shape' can't be projected into
            // {@link #data}'s coordinate system.  This behavior is compliant with the Shape
            // specification. Futhermore, those points are probably outside this polygon since
            // all polygon's points are projectable.
        }
        return false;
    }

    /**
     * Indicates whether or not the points (x1,y1) and (x2,y2)
     * from the specified line are identical.
     */
    private static boolean isSingular(final Line2D.Float segment) {
        return Float.floatToIntBits(segment.x1)==Float.floatToIntBits(segment.x2) &&
               Float.floatToIntBits(segment.y1)==Float.floatToIntBits(segment.y2);
    }

    /**
     * Determines whether the line <code>line</code> intercepts one of this polygon's lines.
     * The polygon will automatically be reclosed if necessary;
     * it is therefore not necessary for the last point to repeat the first.
     *
     * @param  line Line we want to check to see if it intercepts this polygon.
     *         This line absolutely must be expressed according to the native coordinate system
     *         of {@link #array}, i.e. {@link #getInternalCS}.
     * @return <code>true</code> if the line <code>line</code> intercepts this polygon.
     */
    private boolean intersects(final Line2D line) {
        final Point2D.Float firstPt = new Point2D.Float();
        final  Line2D.Float segment = new  Line2D.Float();
        final Polyline.Iterator  it = new Polyline.Iterator(data, null); // Ok even if 'data' is null.
        if (it.next(firstPt) != null) {
            segment.x2 = firstPt.x;
            segment.y2 = firstPt.y;
            do if (!it.next(segment)) {
                if (interiorType==UNCLOSED || isSingular(segment)) {
                    return false;
                }
                segment.x2 = firstPt.x;
                segment.y2 = firstPt.y;
            } while (!segment.intersectsLine(line));
            return true;
        }
        return false;
    }

    /**
     * Tests if the interior of the contour intersects the interior of a specified rectangle.
     * The rectangle's coordinates must be expressed in this contour's coordinate
     * system (as returned by {@link #getCoordinateSystem}).
     */
    public synchronized boolean intersects(final Rectangle2D rect) {
        return intersectsPolygon(new Polygon(rect, getCoordinateSystem()));
    }

    /**
     * Tests if the interior of the contour intersects the interior of a specified shape.
     * The shape's coordinates must be expressed in this contour's coordinate
     * system (as returned by {@link #getCoordinateSystem}).
     */
    public synchronized boolean intersects(final Shape shape) {
        if (shape instanceof Polygon) {
            return intersectsPolygon((Polygon) shape);
        }
        final Polygon[] polygons = getInstances(shape, getCoordinateSystem());
        for (int i=0; i<polygons.length; i++) {
            if (intersectsPolygon(polygons[i])) {
                return true;
            }
        }
        return false;
    }

    /**
     * Test if this polygon intercepts a specified polygon.
     *
     * If this polygon is <em>closed</em> (if it is an island or a lake),
     * this method will return <code>true</code> if at least one point of
     * <code>s</code> lies inside this polygon. If this polygon is not
     * closed, then this method will return the same thing as
     * {@link #intersectsEdge}.
     */
    private boolean intersectsPolygon(final Polygon shape) {
        return intersects(shape, interiorType==UNCLOSED);
    }

    /**
     * Test if the edge of this polygon intercepts the edge of a
     * specified polygon.
     *
     * This should never happen with an error-free bathymery map. However,
     * it could happen if the two polygons don't use the same units. For
     * example, this method may be used to test if an isoline of 15 degrees
     * celsius intercepts an isobath of 30 meters.
     *
     * @param s polygons to test.
     * @return <code>true</code> If an intersection is found.
     */
    final boolean intersectsEdge(final Polygon shape) {
        return intersects(shape, true);
    }

    /**
     * Implémentation of the <code>intersects[Polygon|Edge](Polygon)</code> methods.
     *
     * @param  shape polygons to check.
     * @param  checkEdgeOnly <code>true</code> to only check edges, without bothering with
     *         the inside of this polygon.
     */
    private boolean intersects(final Polygon shape, final boolean checkEdgeOnly) {
        assert Thread.holdsLock(this);
        try {
            final CoordinateSystem coordinateSystem = getInternalCS();
            if (getDataBounds().intersects(shape.getCachedBounds(coordinateSystem))) {
                final Point2D.Float firstPt = new Point2D.Float();
                final  Line2D.Float segment = new  Line2D.Float();
                final Polyline.Iterator  it = new Polyline.Iterator(shape.data,
                                          shape.getMathTransform2D(
                                          shape.getTransformationFromInternalCS(coordinateSystem)));
                if (it.next(firstPt) != null) {
                    if (checkEdgeOnly || !contains(firstPt.x, firstPt.y)) {
                        segment.x2 = firstPt.x;
                        segment.y2 = firstPt.y;
                        do if (!it.next(segment)) {
                            if (interiorType==UNCLOSED || isSingular(segment)) {
                                return false;
                            }
                            segment.x2 = firstPt.x;
                            segment.y2 = firstPt.y;
                        } while (!intersects(segment));
                    }
                    return true;
                }
            }
            return false;
        } catch (TransformException exception) {
            // Conservatively return 'true' if some points from 'shape' can't be projected into
            // {@link #data}'s coordinate system.  This behavior is compliant with the Shape
            // specification.
            return true;
        }
    }

    /**
     * Returns a path iterator for this polygon.
     */
    public synchronized PathIterator getPathIterator(final AffineTransform transform) {
        return new PolygonPathIterator(this, transform);
        // Only polygons internal to Isoline have renderingResolution!=0.
        // Consequently, public polygons never apply decimation, while
        // Isoline's polgyons may apply a decimation for faster rendering
        // when painting through the 'Isoline.paint(...)' method.
    }

    /**
     * Returns a flattened path iterator for this polygon.
     */
    public PathIterator getPathIterator(final AffineTransform transform, final double flatness) {
        if (isFlattenedShape()) {
            return getPathIterator(transform);
        } else {
            return super.getPathIterator(transform, flatness);
        }
    }

    /**
     * Returns <code>true</code> if {@link #getPathIterator} returns a flattened iterator.
     * In this case, there is no need to wrap it into a {@link FlatteningPathIterator}.
     */
    final boolean isFlattenedShape() {
        assert flattened == checkFlattenedShape() : flattened;
        return flattened;
    }

    /**
     * Returns <code>true</code> if {@link #getPathIterator} returns a flattened iterator.
     * In this case, there is no need to wrap it into a {@link FlatteningPathIterator}.
     */
    private boolean checkFlattenedShape() {
        return coordinateTransform==null ||
               coordinateTransform.getMathTransform()==null ||
               !Polyline.hasBorder(data);
    }

    /**
     * Returns the cache for rendering data. This cache
     * is used by the {@link PolygonPathIterator} only.
     */
    final PolygonCache getCache() {
        assert Thread.holdsLock(this);
        if (cache == null) {
            cache = new PolygonCache();
        }
        return cache;
    }

    /**
     * Specifies the resolution to apply when rendering the polygon.
     * This information is used by {@link PolygonPathIterator} objects.
     *
     * @param  resolution Resolution to apply.
     */
    final void setRenderingResolution(final float resolution) {
        /*
         * NOTE: 'setRenderingResolution(0)' is sometimes invoked from
         *       non-synchronized block, immediately after cloning.
         */
        assert resolution==0 || Thread.holdsLock(this);
        if (!Float.isNaN(resolution) && resolution!=renderingResolution) {
            cache = null;
            renderingResolution = resolution;
        }
    }

    /**
     * Returns the rendering resolution. Value 0 means the best available resolution.
     */
    final float getRenderingResolution() {
        return renderingResolution;
    }

    /**
     * Returns an estimation of memory usage in bytes. This method is for information
     * purposes only. The memory really used by two polygons may be lower than the sum
     * of their  <code>getMemoryUsage()</code>  return values, since polylgons try to
     * share their data when possible. Furthermore, this method does not take into account
     * the extra bytes generated by Java Virtual Machine for each objects.
     *
     * @return An <em>estimation</em> of memory usage in bytes.
     */
    final synchronized long getMemoryUsage() {
        return Polyline.getMemoryUsage(data) + 50;
    }

    /**
     * Return the number of points in this polygon.
     */
    public synchronized int getPointCount() {
        return Polyline.getPointCount(data);
    }

    /**
     * Returns all polygon's points. Point coordinates are stored in {@link Point2D}
     * objects using this polygon's coordinate system ({@link #getCoordinateSystem}).
     * This method returns an immutable collection: changes done to <code>Polygon</code>
     * after calling this method will not affect the collection. Despite the fact that
     * this method has a copy semantic, the collection will share many internal structures
     * in such a way that memory consumption should stay low.
     *
     * @return The polygon's points as a collection of {@link Point2D} objects.
     */
    public synchronized Collection getPoints() {
        return new Polyline.Collection(Polyline.clone(data),
                                       getMathTransform2D(coordinateTransform));
    }

    /**
     * Returns an iterator for this polygon's internal points.
     * Points are projected in the specified coordinate system.
     *
     * @param  cs The destination coordinate system, or <code>null</code>
     *            for this polygon's native coordinate system.
     * @return An iterator for points in the specified coordinate system.
     * @throws CannotCreateTransformException if a transformation can't be constructed.
     */
    final Polyline.Iterator iterator(final CoordinateSystem cs)
            throws CannotCreateTransformException
    {
        assert Thread.holdsLock(this);
        return new Polyline.Iterator(data, getMathTransform2D(getTransformationFromInternalCS(cs)));
    }

    /**
     * Stores the value of the first point into the specified point object.
     *
     * @param  point Object in which to store the unprojected coordinate.
     * @return <code>point</code>, or a new {@link Point2D} if <code>point</code> was null.
     * @throws NoSuchElementException If this polygon contains no point.
     *
     * @see #getFirstPoints(Point2D[])
     * @see #getLastPoint(Point2D)
     */
    public synchronized Point2D getFirstPoint(Point2D point) throws NoSuchElementException {
        point = Polyline.getFirstPoint(data, point);
        final MathTransform2D transform = getMathTransform2D(coordinateTransform);
        if (transform!=null) try {
            point = transform.transform(point, point);
        } catch (TransformException exception) {
            // Should not happen, since {@link #setCoordinateSystem}
            // has already successfully projected every points.
            unexpectedException("getFirstPoint", exception);
        }
        assert !Double.isNaN(point.getX()) && !Double.isNaN(point.getY());
        return point;
    }

    /**
     * Stores the value of the last point into the specified point object.
     *
     * @param  point Object in which to store the unprojected coordinate.
     * @return <code>point</code>, or a new {@link Point2D} if <code>point</code> was null.
     * @throws NoSuchElementException If this polygon contains no point.
     *
     * @see #getLastPoints(Point2D[])
     * @see #getFirstPoint(Point2D)
     */
    public synchronized Point2D getLastPoint(Point2D point) throws NoSuchElementException {
        point = Polyline.getLastPoint(data, point);
        final MathTransform2D transform = getMathTransform2D(coordinateTransform);
        if (transform!=null) try {
            point = transform.transform(point, point);
        } catch (TransformException exception) {
            // Should not happen, since {@link #setCoordinateSystem}
            // has already successfully projected every point.
            unexpectedException("getLastPoint", exception);
        }
        assert !Double.isNaN(point.getX()) && !Double.isNaN(point.getY());
        return point;
    }

    /**
     * Stores the values of <code>points.length</code> first points into the specified array.
     *
     * @param points An array to fill with first polygon's points. <code>points[0]</code>
     *               will contains the first point, <code>points[1]</code> the second point,
     *               etc.
     *
     * @throws NoSuchElementException If this polygon doesn't contain enough points.
     */
    public synchronized void getFirstPoints(final Point2D[] points) throws NoSuchElementException {
        Polyline.getFirstPoints(data, points);
        final MathTransform2D transform = getMathTransform2D(coordinateTransform);
        if (transform!=null) try {
            for (int i=0; i<points.length; i++) {
                points[i] = transform.transform(points[i], points[i]);
                assert !Double.isNaN(points[i].getX()) && !Double.isNaN(points[i].getY());
            }
        } catch (TransformException exception) {
            // Should not happen, since {@link #setCoordinateSystem}
            // has already successfully projected every point.
            unexpectedException("getFirstPoints", exception);
        }
        assert points.length==0 || Utilities.equals(getFirstPoint(null), points[0]);
    }

    /**
     * Stores the values of <code>points.length</code> last points into the specified array.
     *
     * @param points An array to fill with last polygon's points.
     *               <code>points[points.length-1]</code> will contains the last point,
     *               <code>points[points.length-2]</code> the point before the last one, etc.
     *
     * @throws NoSuchElementException If this polygon doesn't contain enough points.
     */
    public synchronized void getLastPoints(final Point2D[] points) throws NoSuchElementException {
        Polyline.getLastPoints(data, points);
        final MathTransform2D transform = getMathTransform2D(coordinateTransform);
        if (transform!=null) try {
            for (int i=0; i<points.length; i++) {
                points[i] = transform.transform(points[i], points[i]);
                assert !Double.isNaN(points[i].getX()) && !Double.isNaN(points[i].getY());
            }
        } catch (TransformException exception) {
            // Should not happen, since {@link #setCoordinateSystem}
            // has already successfully projected every point.
            unexpectedException("getLastPoints", exception);
        }
        assert points.length==0 || Utilities.equals(getLastPoint(null), points[points.length-1]);
    }

    /**
     * Adds points to the start of this polygon.  These points will be considered to 
     * form part of the map border, and not considered as points representing
     * a geographic structure.
     *
     * @param  border Coordinates to add as (x,y) number pairs.
     * @param  lower  Index of the first <var>x</var> to add to the border.
     * @param  upper  Index following that of the last <var>y</var> to add to the border.
     * @throws IllegalStateException if this polygon has already been closed.
     * @throws TransformException if <code>border</code> contains points that are invalid
     *         for this polygon's native coordinate system.
     */
    public void prependBorder(final float[] border, final int lower, final int upper)
            throws TransformException
    {
        prependBorder(border, lower, upper, getCoordinateSystem());
    }

    /**
     * Adds points to the end of this polygon.  These points will be considered to
     * form part of the map border, and not considered as points representing 
     * a geographic structure.
     *
     * @param  border Coordinates to add as (x,y) number pairs.
     * @param  lower  Index of the first <var>x</var> to add to the border.
     * @param  upper  Index following that of the last <var>y</var> to add to the border.
     * @throws IllegalStateException if this polygon has already been closed.
     * @throws TransformException if <code>border</code> contains points that are invalid
     *         for this polygon's native coordinate system.
     */
    public void appendBorder(final float[] border, final int lower, final int upper)
            throws TransformException
    {
        appendBorder(border, lower, upper, getCoordinateSystem());
    }

    /**
     * Prepends a border expressed in an arbitrary coordinate system.
     * If <code>cs</code> is null, then the internal CS is assumed.
     */
    final void prependBorder(final float[] border, final int lower, final int upper,
                             final CoordinateSystem cs)
            throws TransformException
    {
        addBorder(border, lower, upper, cs, false);
    }

    /**
     * Appends a border expressed in an arbitrary coordinate system.
     * If <code>cs</code> is null, then the internal CS is assumed.
     */
    final void appendBorder(final float[] border, final int lower, final int upper,
                             final CoordinateSystem cs)
            throws TransformException
    {
        addBorder(border, lower, upper, cs, true);
    }

    /**
     * Implementation of <code>appendBorder(...)</code> and <code>prependBorder(...)</code>.
     *
     * @param cs The border coordinate system, or <code>null</code> for the internal CS.
     * @param append <code>true</code> to carry out the operation <code>appendBorder</code>, or
     *               <code>false</code> to carry out the operation <code>prependBorder</code>.
     */
    private synchronized void addBorder(float[] border, int lower, int upper,
                                        final CoordinateSystem cs, final boolean append)
            throws TransformException
    {
        if (interiorType != UNCLOSED) {
            throw new IllegalStateException(Resources.format(ResourceKeys.ERROR_POLYGON_CLOSED));
        }
        MathTransform2D transform = getMathTransform2D(getTransformationFromInternalCS(cs));
        if (transform != null) {
            final float[] oldBorder = border;
            border = new float[upper-lower];
            transform.inverse().transform(oldBorder, lower, border, 0, border.length);
            lower = 0;
            upper = border.length;
        }
        if (append) {
            data = Polyline.appendBorder(data, border, lower, upper);
        } else {
            data = Polyline.prependBorder(data, border, lower, upper);
        }
        flattened  = checkFlattenedShape();
        dataBounds = null;
        bounds     = null;
        cache      = null;
        // No change to resolution, since it doesn't take border into account.
    }

    /**
     * Adds to the end of this polygon the data of the specified polygon.
     * This method does nothing if <code>toAppend</code> is null.
     *
     * @param  toAppend Polygon to add to the end of <code>this</code>.
     *         The polygon <code>toAppend</code> will not be modified.
     * @throws IllegalStateException    if this polygon has already been closed.
     * @throws IllegalArgumentException if the polygon <code>toAppend</code> has already been closed.
     * @throws TransformException if <code>toAppend</code> contains points that are invalid
     *         for this polygon's native coordinate system.
     */
    public synchronized void append(final Polygon toAppend) throws TransformException {
        if (toAppend == null) {
            return;
        }
        if (!Utilities.equals(getInternalCS(), toAppend.getInternalCS())) {
            throw new UnsupportedOperationException(); // TODO.
        }
        if (interiorType != UNCLOSED || toAppend.interiorType != UNCLOSED) {
            throw new IllegalStateException(Resources.format(ResourceKeys.ERROR_POLYGON_CLOSED));
        }
        data = Polyline.append(data, Polyline.clone(toAppend.data));
        if (dataBounds != null) {
            if (toAppend.dataBounds != null) {
                dataBounds.add(toAppend.dataBounds);
                assert equalsEps(dataBounds, getDataBounds()) : dataBounds;
            } else {
                dataBounds = null;
            }
        }
        bounds    = null;
        cache     = null;
        flattened = checkFlattenedShape();
    }

    /**
     * Reverse point order in this polygon.
     */
    public synchronized void reverse() {
        data = Polyline.reverse(data);
        flattened = checkFlattenedShape();
        cache = null;
    }

    /**
     * Close and freeze this polygon. After closing it,
     * no more points can be added to this polygon.
     *
     * @param type Tells if this polygon is an elevation (e.g. an island in the middle
     *        of the sea) or a depression (e.g. a lake in the middle of a continent).
     *        If this argument doesn't apply, then it can be <code>null</code>.
     *
     * @see #getInteriorType
     */
    public synchronized void close(final InteriorType type) {
        data = Polyline.freeze(data, type!=null, false);
        interiorType = (byte) InteriorType.getValue(type);
        flattened = checkFlattenedShape();
        cache = null;
    }

    /**
     * Returns whether this polygon is closed or not.
     */
    final boolean isClosed() {
        return interiorType != UNCLOSED;
    }

    /**
     * Tells if this polygon's interior is an {@linkplain InteriorType#ELEVATION elevation}
     * or a {@linkplain InteriorType#DEPRESSION depression}.
     *
     * @return This polygon's interior type, or <code>null</code> is it doesn't apply.
     *
     * @see #close
     */
    public InteriorType getInteriorType() {
        return InteriorType.getEnum(interiorType);
    }
    
    /**
     * Returns a polygon with the point of this polygon from <code>lower</code>
     * inclusive to <code>upper</code> exclusive. The returned polygon may not be
     * closed, i.e. {@link #getInteriorType} may return <code>null</code>.
     * If no data are available in the specified range, this method returns
     * <code>null</code>.
     */
    public synchronized Polygon subpoly(final int lower, final int upper) {
        final Polyline sub = Polyline.subpoly(data, lower, upper);
        if (sub == null) {
            return null;
        }
        if (Polyline.equals(sub, data)) {
            return this;
        }
        final Polygon subPoly = new Polygon(coordinateTransform);
        subPoly.data = sub;
        subPoly.flattened = subPoly.checkFlattenedShape();
        assert subPoly.getPointCount() == (upper-lower);
        return subPoly;
    }

    /**
     * Returns a polygon with the point of this polygon from <code>lower</code>
     * inclusive to the end. The returned polygon may not be closed, i.e. {@link
     * #getInteriorType} may return <code>null</code>. If no data are available
     * in the specified range, this method returns <code>null</code>.
     */
    final synchronized Polygon subpoly(final int lower) {
        return subpoly(lower, getPointCount());
    }

    /**
     * Returns the polygon's resolution.  The mean resolution is the mean distance between
     * every pair of consecutive points in this polygon  (ignoring "extra" points used for
     * drawing a border, if there is one). This method tries to express the resolution in
     * linear units (usually meters) no matter whether the coordinate system is actually a
     * {@link ProjectedCoordinateSystem} or a {@link GeographicCoordinateSystem}.
     * More specifically:
     * <ul>
     *   <li>If the coordinate system is a {@linkplain GeographicCoordinateSystem geographic}
     *       one, then the resolution is expressed in units of the underlying
     *       {@linkplain Ellipsoid#getAxisUnit ellipsoid's axis length}.</li>
     *   <li>Otherwise (especially if the coordinate system is a {@linkplain
     *       ProjectedCoordinateSystem projected} one), the resolution is expressed in
     *       {@linkplain ProjectedCoordinateSystem#getUnits units of the coordinate system}.</li>
     * </ul>
     */
    public synchronized Statistics getResolution() {
        try {
            return Polyline.getResolution(data, coordinateTransform);
        } catch (TransformException exception) {
            // Should not happen, since {@link #setCoordinateSystem}
            // has already successfully projected every points.
            unexpectedException("getResolution", exception);
            return null;
        }
    }

    /**
     * Sets the polygon's resolution. This method tries to interpolate new points in such a way
     * that every point is spaced by exactly <code>resolution</code> units (usually meters)
     * from the previous one.
     *
     * @param  resolution Desired resolution, in the same units as {@link #getResolution}.
     * @throws TransformException If some coordinate transformations were needed and failed.
     *         There is no guarantee on contour's state in case of failure.
     */
    public synchronized void setResolution(final double resolution) throws TransformException {
        CoordinateSystem targetCS = getCoordinateSystem();
        if (CTSUtilities.getHeadGeoEllipsoid(targetCS) != null) {
            /*
             * The 'Polyline.setResolution(...)' algorithm requires a cartesian coordinate system.
             * If this polygon's coordinate system is not cartesian, check whether the underlying data
             * used a cartesian CS  (this polygon may be a "view" of the data under another CS).
             * If the underlying data are not cartesian either, create a temporary sterographic
             * projection for computation purposes.
             */
            targetCS = getInternalCS();
            if (targetCS instanceof GeographicCoordinateSystem) {
                final GeographicCoordinateSystem geoCS = (GeographicCoordinateSystem) targetCS;
                final Ellipsoid ellipsoid = geoCS.getHorizontalDatum().getEllipsoid();
                final String         name = "Temporary cartesian";
                final Rectangle2D  bounds = getCachedBounds();
                final Point2D      center = new Point2D.Double(bounds.getCenterX(),
                                                               bounds.getCenterY());
                final Projection projection = new Projection(name, CARTESIAN_PROJECTION,
                                                             ellipsoid, center, null);
                targetCS = new ProjectedCoordinateSystem(name, geoCS, projection);
            }
        }
        Polyline.setResolution(data, getTransformationFromInternalCS(targetCS), resolution);
        clearCache(); // Clear everything in the cache.
    }

    /**
     * Compress this polygon. Compression is destructive, i.e. it may lose data. This method
     * processes in two steps:
     *
     * First, it invokes <code>{@link #setResolution setResolution}(dx&nbsp;+&nbsp;factor*std)</code>
     * where <var>dx</var> is the {@linkplain #getResolution mean resolution} of this
     * polygon and <var>std</var> is the resolution's standard deviation.
     *
     * Second, it replaces absolute positions (left handed image) by relative positions
     * (right handed image), i.e. distances relative to the previous point.  Since all
     * distances are of similar magnitude, distances can be coded in <code>byte</code>
     * primitive type instead of <code>float</code>.
     *
     * <table cellspacing='12'><tr>
     * <td><p align="center"><img src="doc-files/uncompressed.png"></p></td>
     * <td><p align="center"><img src="doc-files/compressed.png"></p></td>
     * </tr></table>
     *
     * @param  factor Facteur contrôlant la baisse de résolution.  Les valeurs élevées
     *         déciment davantage de points, ce qui réduit d'autant la consommation de
     *         mémoire. Ce facteur est généralement positif, mais il peut aussi être 0
     *         où même légèrement négatif.
     * @return A <em>estimation</em> of the compression rate. For example a value of 0.2
     *         means that the new polygon use <em>approximatively</em> 20% less memory.
     *         Warning: this value may be inacurate, for example if the old polygon was
     *         used to shares its data with an other polygon, compressing one polygon
     *         may actually increase memory usage since the two polygons will no longer
     *         share their data.
     * @throws TransformException If an error has come up during a cartographic projection.
     */
    public synchronized float compress(final float factor) throws TransformException {
        final Statistics stats = Polyline.getResolution(data, coordinateTransform);
        if (stats != null) {
            final long  memoryUsage = getMemoryUsage();
            final double resolution = stats.mean() + factor*stats.standardDeviation(false);
            if (resolution > 0) {
                setResolution(resolution);
                data = Polyline.freeze(data, false, true); // Apply the compression algorithm
                return (float) (memoryUsage - getMemoryUsage()) / (float) memoryUsage;
            }
            data = Polyline.freeze(data, false, false); // No compression
        }
        return 0;
    }

    /**
     * Returns a polygon approximately equal to this polygon clipped to the specified bounds.
     * The clip is only approximative in that the resulting polygon may extend outside the clip
     * area. However, it is guaranteed that the resulting polygon contains at least all the interior
     * of the clip area.
     *
     * If this method can't perform the clip, or if it believes that it isn't worth doing a clip,
     * it returns <code>this</code>. If this polygon doesn't intersect the clip area, then this
     * method returns <code>null</code>. Otherwise, a new polygon is created and returned. The new
     * polygon will try to share as much internal data as possible with <code>this</code> in order
     * to keep memory footprint low.
     *
     * @param  clipper An object containing the clip area.
     * @return <code>null</code> if this polygon doesn't intersect the clip, <code>this</code>
     *         if no clip has been performed, or a new clipped polygon otherwise.
     */
    final synchronized Polygon clip(final Clipper clipper) {
        final Rectangle2D clip = clipper.getInternalClip(this);
        final Rectangle2D dataBounds = getDataBounds();
        if (clip.contains(dataBounds)) {
            return this;
        }
        if (!clip.intersects(dataBounds)) {
            return null;
        }
        /*
         * It would appear that the polygon is neither completely inside nor completely
         * outside <code>clip</code>.  It is therefore necessary to resolve to perform
         * a more powerful (and more costly) check.
         */
        final Polygon clipped = clipper.clip(this);
        if (clipped != null) {
            if (Polyline.equals(data, clipped.data)) {
                return this;
            }
        }
        return clipped;
    }

    /**
     * Returns a copy of all coordinates of this polygon. Coordinates are usually
     * (<var>x</var>,<var>y</var>) or (<var>longitude</var>,<var>latitude</var>)
     * pairs, depending on the {@linkplain #getCoordinateSystem coordinate system
     * in use}.
     *
     * @param  The destination array. The coordinates will be filled in {@link ArrayData#array}
     *         from index {@link ArrayData#length}. The array will be expanded if needed, and
     *         {@link ArrayData#length} will be updated with index after the <code>array</code>'s
     *         element filled with the last <var>y</var> ordinates.
     * @param  resolution The minimum distance desired between points.
     */
    final void toArray(final ArrayData dest, float resolution) {
        assert Thread.holdsLock(this);
        try {
            /*
             * If the polygon's coordinate system is geographic, then we must translate
             * the resolution (which is in linear units, usually meters) to angular units.
             * The formula used below is only an approximation (probably not the best one).
             * It estimates the average of latitudinal and longitudinal angles corresponding
             * to the distance 'resolution' in the middle of the polygon's bounds. The average
             * is weighted according to the width/height ratio of the polygon's bounds.
             */
            final CoordinateSystem cs = getCoordinateSystem();
            final Ellipsoid ellipsoid = CTSUtilities.getHeadGeoEllipsoid(cs);
            if (ellipsoid != null) {
                final Unit          unit = cs.getUnits(1);
                final Rectangle2D bounds = getCachedBounds();
                double             width = bounds.getWidth();
                double            height = bounds.getHeight();
                double          latitude = bounds.getCenterY();
                latitude = Unit.RADIAN.convert(latitude, unit);
                final double sin = Math.sin(latitude);
                final double cos = Math.cos(latitude);
                final double normalize = width+height;
                width  /= normalize;
                height /= normalize;
                resolution *= (height + width/cos) * XMath.hypot(sin/ellipsoid.getSemiMajorAxis(),
                                                                 cos/ellipsoid.getSemiMinorAxis());
                // Assume that longitude has the same unit as latitude.
                resolution = (float) unit.convert(resolution, Unit.RADIAN);
            }
            /*
             * Transform the resolution from this polygon's CS to the underlying data CS.
             * TODO: we should use 'MathTransform.derivative' instead, but it is not yet
             *       implemented for most transforms.
             */
            if (coordinateTransform != null) {
                final MathTransform tr = coordinateTransform.getMathTransform();
                if (!tr.isIdentity()) {
                    final Rectangle2D bounds = getCachedBounds();
                    final double  centerX = bounds.getCenterX();
                    final double  centerY = bounds.getCenterY();
                    final double[] coords = new double[] {
                        centerX-resolution, centerY,
                        centerX+resolution, centerY,
                        centerX,            centerY-resolution,
                        centerX,            centerY+resolution
                    };
                    tr.inverse().transform(coords, 0, coords, 0, coords.length/2);
                    resolution = (float) (0.25*(
                                          XMath.hypot(coords[2]-coords[0], coords[3]-coords[1]) +
                                          XMath.hypot(coords[6]-coords[4], coords[7]-coords[5])));
                }
            }
            /*
             * Gets the array and transforms it, if needed.
             */
            Polyline.toArray(data, dest, resolution, getMathTransform2D(coordinateTransform));
        } catch (TransformException exception) {
            // Should not happen, since {@link #setCoordinateSystem}
            // has already successfully projected every point.
            unexpectedException("toArray", exception);
        }
    }

    /**
     * Returns a copy of all coordinates of this polygon. Coordinates are usually
     * (<var>x</var>,<var>y</var>) or (<var>longitude</var>,<var>latitude</var>)
     * pairs, depending on the {@linkplain #getCoordinateSystem coordinate system
     * in use}. This method never returns <code>null</code>, but may return an array
     * of length 0 if no data are available.
     *
     * @param  resolution The minimum distance desired between points, in the same units
     *         as for the {@link #getResolution} method  (i.e. linear units as much as
     *         possible - usually meters - even for geographic coordinate system).
     *         If <code>resolution</code> is greater than 0, then points that are closer
     *         than <code>resolution</code> from previous points will be skipped. This method
     *         is not required to perform precise distance computations.
     * @return The coordinates expressed in this
     *         {@linkplain #getCoordinateSystem polygon's coordinate system}.
     */
    public synchronized float[] toArray(final float resolution) {
        final ArrayData array = new ArrayData(64);
        toArray(array, resolution);
        return XArray.resize(array.array(), array.length());
    }

    /**
     * Returns a hash value for this polygon.
     */
    public synchronized int hashCode() {
        return Polyline.hashCode(data);
    }

    /**
     * Compare the specified object with this polygon for equality.
     */
    public synchronized boolean equals(final Object object) {
        if (object == this) {
            // Slight optimization
            return true;
        }
        if (super.equals(object)) {
            final Polygon that = (Polygon) object;
            return                  this.interiorType    ==   that.interiorType         &&
                   Utilities.equals(this.coordinateTransform, that.coordinateTransform) &&
                    Polyline.equals(this.data,                that.data);
        }
        return false;
    }

    /**
     * Return a clone of this polygon. The clone has a deep copy semantic,
     * i.e. any change to the current polygon (including adding new points)
     * will not affect the clone,  and vice-versa   (any change to the clone
     * will not affect the current polygon). However, the two polygons will
     * share many internal structures in such a way that memory consumption
     * for polygon's clones should be kept low.
     */
    public synchronized Object clone() {
        final Polygon polygon = (Polygon) super.clone();
        polygon.data = Polyline.clone(data); // Take an immutable view of 'data'.
        return polygon;
    }

    /**
     * Clears all information that was kept in an internal cache.
     * This method can be called when we know that this polygon will no longer be used
     * before a particular time. It does not cause the loss of any information but
     * will make the next use of this polygon slower (the time during which the internal
     * caches are reconstructed, after which the polygon will resume its normal speed).
     */
    final synchronized void clearCache() {
        cache      = null;
        bounds     = null;
        dataBounds = null;
        flattened  = checkFlattenedShape();
    }

    /**
     * Invoked during deserialization.
     */
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        flattened = checkFlattenedShape(); // Reasonably fast to compute.
    }

    /**
     * Method called when an unexpected error has occurred.
     *
     * @param  method Name of the method in which the exception has occurred.
     * @param  exception The exception which has occurred.
     * @throws IllegalPathStateException systematically rethrown.
     */
    static void unexpectedException(final String method, final TransformException exception) {
        Polyline.unexpectedException("Polygon", method, exception);
        final IllegalPathStateException e = new IllegalPathStateException(
                                                exception.getLocalizedMessage());
        e.initCause(exception);
        throw e;
    }

    /**
     * Write all point coordinates to the specified stream.
     * This method is useful for debugging purposes.
     *
     * @param  out The destination stream, or <code>null</code> for the standard output.
     * @param  locale Desired locale, or <code>null</code> for a default one.
     * @throws IOException If an error occured while writing to the destination stream.
     */
    public void print(final Writer out, final Locale locale) throws IOException {
        print(new String[]{getName(locale)}, new Collection[]{getPoints()}, out, locale);
    }

    /**
     * Write all point coordinates of many polygons side by side.
     * This method is useful for checking the result of a coordinate
     * transformation; one could write the original and transformed
     * polygons side by side. Note that this method may require unicode
     * support for proper output.
     *
     * @param  polygons The set of polygons. Polygons may have different lengths.
     * @param  out The destination stream, or <code>null</code> for the standard output.
     * @param  locale Desired locale, or <code>null</code> for a default one.
     * @throws IOException If an error occured while writing to the destination stream.
     */
    public static void print(final Polygon[] polygons, final Writer out, final Locale locale)
            throws IOException
    {
        final String[]     titles = new String[polygons.length];
        final Collection[] arrays = new Collection[polygons.length];
        for (int i=0; i<polygons.length; i++) {
            final Polygon polygon = polygons[i];
            titles[i] = polygon.getName(locale);
            arrays[i] = polygon.getPoints();
        }
        print(titles, arrays, out, locale);
    }

    /**
     * Write all points from arbitrary collections side by side.
     * Note that this method may require unicode support for proper output.
     *
     * @param  titles The column's titles. Should have the same length as <code>points</code>.
     * @param  points Array of points collections. Collections may have different sizes.
     * @param  out The destination stream, or <code>null</code> for the standard output.
     * @param  locale Desired locale, or <code>null</code> for a default one.
     * @throws IOException If an error occured while writing to the destination stream.
     */
    public static void print(final String[] titles, final Collection[] points, Writer out, Locale locale)
            throws IOException
    {
        if (locale == null) locale = Locale.getDefault();
        if (out    == null)    out = Arguments.getWriter(System.out);

        final int            width = 8; // Columns width.
        final int        precision = 3; // Significant digits.
        final String     separator = "  \u2502  "; // Vertical bar.
        final String lineSeparator = System.getProperty("line.separator", "\n");
        final NumberFormat  format = NumberFormat.getNumberInstance(locale);
        final FieldPosition  dummy = new FieldPosition(0);
        final StringBuffer  buffer = new StringBuffer();
        format.setMinimumFractionDigits(precision);
        format.setMaximumFractionDigits(precision);
        format.setGroupingUsed(false);

        final Iterator[] iterators = new Iterator[points.length];
        for (int i=0; i<points.length; i++) {
            if (i != 0) {
                out.write(separator);
            }
            int length=0;
            if (titles[i] != null) {
                length=titles[i].length();
                final int spaces = Math.max(width-length/2, 0);
                out.write(Utilities.spaces(spaces));
                out.write(titles[i]);
                length += spaces;
            }
            out.write(Utilities.spaces(1+2*width-length));
            iterators[i]=points[i].iterator();
        }
        out.write(lineSeparator);
        boolean hasNext; do {
            hasNext=false;
            buffer.setLength(0);
            for (int i=0; i<iterators.length; i++) {
                if (i!=0) buffer.append(separator);
                final Iterator   it = iterators[i];
                final boolean hasPt = it.hasNext();
                final Point2D point = (hasPt) ? (Point2D) it.next() : null;
                boolean xy=true; do {
                    final int start = buffer.length();
                    if (point != null) {
                        format.format(xy ? point.getX() : point.getY(), buffer, dummy);
                    }
                    buffer.insert(start, Utilities.spaces(width-(buffer.length()-start)));
                    if (xy) {
                        buffer.append('\u00A0'); // No-break space
                    }
                } while (!(xy = !xy));
                hasNext |= hasPt;
            }
            if (!hasNext) {
                break;
            }
            buffer.append(lineSeparator);
            out.write(buffer.toString());
        } while (hasNext);
    }




    /**
     * This interface defines the method required by any object that
     * would like to be a renderer for polygons in an {@link Isoline}.
     * The {@link #paint} method is invoked by {@link Isoline#paint}.
     *
     * @version $Id: Polygon.java,v 1.11 2003/05/19 15:07:19 desruisseaux Exp $
     * @author Martin Desruisseaux
     *
     * @see Polygon
     * @see Isoline#paint
     * @see org.geotools.renderer.j2d.RenderedIsoline
     */
    public static interface Renderer {
        /**
         * Returns the clip area in units of polygon and isoline's coordinate system (both use
         * the same). This is usually "real world" metres or degrees of latitude/longitude.
         *
         * @see Polygon#getCoordinateSystem
         * @see Isoline#getCoordinateSystem
         */
        public abstract Shape getClip();

        /**
         * Returns the rendering resolution, in units of polygon and isoline's coordinate system.
         * (usually metres or degrees). A larger resolution speeds up rendering, while a smaller
         * resolution draws more precise maps.
         *
         * @param  current The current rendering resolution.
         * @return the <code>current</code> rendering resolution if it is still good enough,
         *         or a new resolution if a change is needed.
         *
         * @see Polygon#getCoordinateSystem
         * @see Isoline#getCoordinateSystem
         */
        public abstract float getRenderingResolution(float current);

        /**
         * Draw or fill a polygon. {@link Isoline#paint} invokes this method with a decimated and/or
         * clipped polygon in argument. This polygon exposes some internal state of {@link Isoline}.
         * <strong>Do not modify it, nor keep a reference to it after this method call</strong>
         * in order to avoid unexpected behaviour.
         *
         * @param polygon The polygon to draw. <strong>Do not modify.</strong>
         */
        public abstract void paint(final Polygon polygon);

        /**
         * Invoked once after a series of polygons have been painted. This method is typically
         * invoked by {@link Isoline#paint} after all isoline's polygons have been painted.
         * Some implementations may choose to release resources here. The arguments provided
         * to this method are for information purposes only.
         *
         * @param rendered The total number of <em>rendered</em> points. This number is
         *        always smaller than {@link Isoline#getPointCount}  since the renderer
         *        may have clipped or decimated data. This is the number of points kept
         *        in the cache.
         * @param recomputed The number of points that have been recomputed (i.e. decompressed,
         *        decimated, projected and transformed). They are points that were not reused
         *        from the cache. This number is always smaller than or equal to
         *        <code>rendered</code>.
         * @param resolution The mean resolution of rendered polygons.
         */
        public abstract void paintCompleted(int rendered, int recomputed, double resolution);
    }
}
