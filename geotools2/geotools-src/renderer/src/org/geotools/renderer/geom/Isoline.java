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
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.PathIterator;
import java.awt.geom.AffineTransform;
import java.awt.geom.FlatteningPathIterator;

// Collections and utils
import java.util.Set;
import java.util.List;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.AbstractSet;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.NoSuchElementException;
import java.util.Locale;
import java.io.IOException;
import java.io.ObjectInputStream;

// Geotools dependencies
import org.geotools.cs.Ellipsoid;
import org.geotools.cs.CoordinateSystem;
import org.geotools.ct.TransformException;
import org.geotools.cs.ProjectedCoordinateSystem;
import org.geotools.cs.GeographicCoordinateSystem;
import org.geotools.util.ProgressListener;
import org.geotools.math.Statistics;
import org.geotools.resources.XArray;
import org.geotools.resources.Utilities;
import org.geotools.resources.renderer.Resources;
import org.geotools.resources.renderer.ResourceKeys;


/**
 * An isoline built from a set of polygons. An isoline is initially built with a {@linkplain
 * #value} (for example "50" for the 50 meters isobath) and a {@linkplain CoordinateSystem
 * coordinate system}. An arbitrary amount of {@linkplain Polygon polygons} can be added after
 * construction using {@link #add(Polygon)} or {@link #add(float[])}. If polygons are broken
 * in many pieces (as in the figure below), the
 * {@link #assemble(Isoline[],float[],Shape,ProgressListener) assemble(...)} method may help
 * assemble them before rendering.
 *
 * <p align="center"><img src="doc-files/splitted.png"></p>
 *
 * Note: this class has a natural ordering that is inconsistent with equals.
 * The {@link #compareTo} method compares only the isoline's values, while
 * {@link #equals} also compares all polygon points. The natural ordering
 * for <code>Isoline</code> is convenient for sorting isolines in increasing
 * order of altitude.
 *
 * <br><br>
 * <TABLE WIDTH="80%" ALIGN="center" CELLPADDING="18" BORDER="4" BGCOLOR="#FFE0B0"><TR><TD>
 * <P ALIGN="justify"><STRONG>This class may change in a future version, hopefully toward
 * ISO-19107. Do not rely on it.</STRONG>
 * </TD></TR></TABLE>
 *
 * @version $Id: Isoline.java,v 1.11 2003/05/13 11:00:46 desruisseaux Exp $
 * @author Martin Desruisseaux
 *
 * @see Polygon
 */
public class Isoline extends GeoShape implements Comparable {
    /**
     * Version number for compatibility with bathymetries
     * registered under previous versions.
     */
    private static final long serialVersionUID = -2560639903583552721L;

    /**
     * The value for this isoline. In the case
     * of isobath, the value is the altitude.
     */
    public final float value;

    /**
     * Coordinate system.
     */
    private CoordinateSystem coordinateSystem;

    /**
     * Collection of polygons making up this isoline. The elements of this
     * table can be sorted to improve the quality of display when they are
     * drawn from last to first.
     *
     * This array contains cloned polygons (reminder: cloned polgons still share their data).
     * The <code>Isoline</code> API should never expose its internal polygons in any way.
     * If a polygon is to be returned, it must be cloned again. Cloning polygons allows us to
     * protect their state, especially the <code>Polygon.setRenderingResolution(...)</code>
     * value which is implementation details and should be hidden from the user. The only
     * exceptions to this rule are calls to {@link Polygon.Renderer#paint}, which do not
     * clone polygons for performance reasons.
     */
    private Polygon[] polygons;

    /**
     * Number of valid elements in <code>polygons</code>.
     */
    private int polygonCount;

    /**
     * Indicates whether the polygons contained in the table <code>polygons</code>
     * have been sorted. If this is not the case, the sorting should be 
     * done before drawing the polygons.
     */
    private boolean sorted;

    /**
     * <code>true</code> if {@link #getPathIterator} returns a flattened iterator.
     * In this case, there is no need to wrap it into a {@link FlatteningPathIterator}.
     */
    private transient boolean flattened;

    /**
     * Rectangle completely enclosing this isoline. This rectangle is 
     * calculated just once and kept in an internal cache to accelerate
     * certain checks.
     */
    private transient Rectangle2D bounds;

    /**
     * The statistics about resolution, or <code>null</code> if none.
     * This object is computed when first requested and cached for subsequent uses.
     * It is also serialized if available, since it is somewhat heavy to compute.
     */
    private Statistics resolution;

    /**
     * Construct an initially empty isoline using the {@linkplain GeographicCoordinateSystem#WGS84
     * default} geographic coordinate system.
     * Polygons can be added using one of the <code>add(...)</code> methods.
     *
     * @param value The value for this isoline. In the case
     *        of isobath, the value is the altitude.
     *
     * @see #add(float[])
     * @see #add(Shape)
     * @see #add(Polygon)
     * @see #add(Isoline)
     */
    public Isoline(final float value) {
        this(value, GeographicCoordinateSystem.WGS84);
    }

    /**
     * Construct an initially empty isoline.
     * Polygons can be added using one of the <code>add(...)</code> methods.
     *
     * @param value The value for this isoline. In the case
     *        of isobath, the value is the altitude.
     * @param coordinateSystem The coordinate system to use for all
     *        points in this isoline, or <code>null</code> if unknown.
     *
     * @see #add(float[])
     * @see #add(Shape)
     * @see #add(Polygon)
     * @see #add(Isoline)
     */
    public Isoline(final float value, final CoordinateSystem coordinateSystem) {
        this.value = value;
        this.coordinateSystem = coordinateSystem;
    }

    /**
     * Construct an isoline with the same data as
     * the specified isoline. The new isoline will
     * have a copy semantic.
     */
    public Isoline(final Isoline isoline) {
        this.value            = isoline.value;
        this.coordinateSystem = isoline.coordinateSystem;
        this.polygonCount     = isoline.polygonCount;
        this.sorted           = isoline.sorted;
        this.bounds           = isoline.bounds;
        this.polygons         = new Polygon[polygonCount];
        for (int i=0; i<polygonCount; i++) {
            polygons[i] = (Polygon) isoline.polygons[i].clone();
        }
        flattened = checkFlattenedShape();
    }

    /**
     * Returns the isoline's coordinate system, or <code>null</code> if unknown.
     */
    public synchronized CoordinateSystem getCoordinateSystem() {
        return coordinateSystem;
    }

    /**
     * Set the isoline's coordinate system. Calling this method is equivalent
     * to reprojecting all polygons from the old coordinate system to the new one.
     *
     * @param  The new coordinate system. A <code>null</code> value resets the
     *         coordinate system given at construction time.
     * @throws TransformException If a transformation failed. In case of failure,
     *         the state of this object will remain unchanged (as if this method has
     *         never been invoked).
     */
    public synchronized void setCoordinateSystem(final CoordinateSystem coordinateSystem)
            throws TransformException
    {
        final CoordinateSystem oldCoordinateSystem = this.coordinateSystem;
        if (Utilities.equals(oldCoordinateSystem, coordinateSystem)) return;
        bounds     = null;
        resolution = null;
        int i=polygonCount;
        try {
            while (--i>=0) {
                polygons[i].setCoordinateSystem(coordinateSystem);
            }
            this.coordinateSystem = coordinateSystem; // Do it last.
        } catch (TransformException exception) {
            /*
             * If a map projection failed, reset
             * to the original coordinate system.
             */
            while (++i < polygonCount) {
                try {
                    polygons[i].setCoordinateSystem(oldCoordinateSystem);
                } catch (TransformException unexpected) {
                    // Should not happen, since the old coordinate system is supposed to be ok.
                    Polygon.unexpectedException("setCoordinateSystem", unexpected);
                }
            }
            throw exception;
        }
        flattened = checkFlattenedShape();
    }

    /**
     * Determines whetever the isoline is empty.
     */
    public synchronized boolean isEmpty() {
        for (int i=polygonCount; --i>=0;) {
            if (!polygons[i].isEmpty()) {
                return false;
            }
        }
        return true;
    }

    /**
     * Return the bounding box of this isoline. This method returns
     * a direct reference to the internally cached bounding box. DO
     * NOT MODIFY!
     */
    final Rectangle2D getCachedBounds() {
        assert Thread.holdsLock(this);
        if (bounds == null) {
            for (int i=polygonCount; --i>=0;) {
                final Polygon polygon = polygons[i];
                if (!polygon.isEmpty()) {
                    final Rectangle2D polygonBounds=polygon.getBounds2D();
                    if (bounds == null) {
                        bounds = polygonBounds;
                    } else {
                        bounds.add(polygonBounds);
                    }
                }
            }
            if (bounds == null) {
                bounds = new Rectangle2D.Float();
            }
        }
        return bounds;
    }

    /**
     * Return the bounding box of this isoline, including its possible
     * borders. This method uses a cache, such that after a first call,
     * the following calls should be fairly quick.
     *
     * @return A bounding box of this isoline. Changes to the
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
        final Rectangle rect = new Rectangle();
        rect.setRect(getCachedBounds()); // Perform the appropriate rounding.
        return rect;
    }

    /**
     * Indicates whether the specified (<var>x</var>,<var>y</var>) point is inside this isoline.
     * The point coordinates must be expressed in the isoline's coordinate system, that is
     * {@link #getCoordinateSystem()}. This method looks for the smallest polygon that contains the
     * specified polygon and returns <code>true</code> if this point is an elevation (for example
     * an island) or <code>false</code> if it is a depression (for example a lake).
     */
    public synchronized boolean contains(final double x, final double y) {
        if (getCachedBounds().contains(x,y)) {
            if (!sorted) {
                sort();
            }
            for (int i=0; i<polygonCount; i++) {
                final Polygon polygon = polygons[i];
                if (polygon.contains(x,y)) {
                    final InteriorType interiorType = polygon.getInteriorType();
                    if (InteriorType.ELEVATION.equals(interiorType)) {
                        return true;
                    }
                    if (InteriorType.DEPRESSION.equals(interiorType)) {
                        return false;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Indicates whether the specified point is inside this isoline.  The point coordinates
     * must be expressed in the isoline's coordinate system, that is {@link #getCoordinateSystem()}.
     * This method looks for the smallest polygon that contains the specified point and returns
     * <code>true</code> if this polygon is an elevation (for example, an island) or
     * <code>false</code> if it is a depression (for example, a lake).
     */
    public synchronized boolean contains(final Point2D point) {
        if (getCachedBounds().contains(point)) {
            if (!sorted) {
                sort();
            }
            for (int i=0; i<polygonCount; i++) {
                final Polygon polygon = polygons[i];
                if (polygon.contains(point)) {
                    final InteriorType interiorType = polygon.getInteriorType();
                    if (InteriorType.ELEVATION.equals(interiorType)) {
                        return true;
                    }
                    if (InteriorType.DEPRESSION.equals(interiorType)) {
                        return false;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Checks whether the specified rectangle is entirely contained within this isoline.
     * The rectangle's coordinates should be expressed in the isoline's coordinate system,
     * that is {link #getCoordinateSystem()}. This method looks for the smallest polygon that
     * contains the specified rectangle and returns <code>true</code> if this polygon is an
     * elevation (for example, an island) or <code>false</code> if it is a depression
     * (for example, a lake).
     */
    public synchronized boolean contains(final Rectangle2D rect) {
        if (getCachedBounds().contains(rect)) {
            if (!sorted) {
                sort();
            }
            for (int i=0; i<polygonCount; i++) {
                final Polygon polygon = polygons[i];
                final InteriorType interiorType = polygon.getInteriorType();
                if (InteriorType.ELEVATION.equals(interiorType)) {
                    if (polygon.contains(rect)) {
                        return true;
                    }
                } else if (InteriorType.DEPRESSION.equals(interiorType)) {
                    if (polygon.intersects(rect)) {
                        return false;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Checks whether the specified shape is entirely contained within this isoline.
     * The shape's coordinates must be expressed in the isoline's coordinate system, 
     * that is {@link #getCoordinateSystem()}. This method looks for the smallest polygon
     * that contains the specified shape and returns <code>true</code> if this polygon is
     * an elevation (for example, an island) or <code>false</code> if it is a depression
     * (for example, a lake).
     */
    public synchronized boolean contains(final Shape shape) {
        if (getCachedBounds().contains(shape.getBounds2D())) {
            if (!sorted) {
                sort();
            }
            for (int i=0; i<polygonCount; i++) {
                final Polygon polygon = polygons[i];
                final InteriorType interiorType = polygon.getInteriorType();
                if (InteriorType.ELEVATION.equals(interiorType)) {
                    if (polygon.contains(shape)) {
                        return true;
                    }
                } else if (InteriorType.DEPRESSION.equals(interiorType)) {
                    if (polygon.intersects(shape)) {
                        return false;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Tests whether the specified rectangle intersects
     * the interior of a polygon of this isoline.
     */
    public synchronized boolean intersects(final Rectangle2D rect) {
        if (getCachedBounds().intersects(rect)) {
            if (!sorted) {
                sort();
            }
            for (int i=0; i<polygonCount; i++) {
                final Polygon polygon = polygons[i];
                final InteriorType interiorType = polygon.getInteriorType();
                if (InteriorType.ELEVATION.equals(interiorType)) {
                    if (polygon.intersects(rect)) {
                        return true;
                    }
                } else if (InteriorType.DEPRESSION.equals(interiorType)) {
                    if (polygon.contains(rect)) {
                        return false;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Tests whether the specified shape intersects
     * the interior of a polygon of this isoline.
     */
    public synchronized boolean intersects(final Shape shape) {
        if (getCachedBounds().intersects(shape.getBounds2D())) {
            if (!sorted) {
                sort();
            }
            for (int i=0; i<polygonCount; i++) {
                final Polygon polygon = polygons[i];
                final InteriorType interiorType = polygon.getInteriorType();
                if (InteriorType.ELEVATION.equals(interiorType)) {
                    if (polygon.intersects(shape)) {
                        return true;
                    }
                } else if (InteriorType.DEPRESSION.equals(interiorType)) {
                    if (polygon.contains(shape)) {
                        return false;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Paints this isoline using the specified {@link Polygon.Renderer}.
     * This method is faster than <code>graphics.draw(this)</code> since
     * it reuses internal cache when possible.
     *
     * @param  renderer The destination renderer. The {@link Polygon.Renderer#paint}
     *         method will be invoked for each polygon to render.
     */
    public synchronized void paint(final Polygon.Renderer renderer) {
        int rendered=0, recomputed=0;
        double meanResolution = 0;
        final Shape clip = renderer.getClip();
        if (clip.intersects(getCachedBounds())) {
            if (!sorted) {
                sort();
            }
            for (int i=polygonCount; --i>=0;) {
                final Polygon polygon = polygons[i];
                synchronized (polygon) {
                    if (clip.intersects(polygon.getCachedBounds())) {
                        /*
                         * Compute the rendering resolution and paint the polygon.
                         */
                        float resolution = polygon.getRenderingResolution();
                        resolution = renderer.getRenderingResolution(resolution);
                        polygon.setRenderingResolution(resolution);
                        renderer.paint(polygon);
                        /*
                         * Get statistical data for monitoring the cache performance.
                         */
                        final PolygonCache cache = polygon.getCache();
                        final int numPts = cache.getPointCount();
                        rendered += numPts;
                        if (cache.recomputed()) {
                            recomputed += numPts;
                        }
                        meanResolution += resolution * numPts;
                    }
                }
            }
        }
        meanResolution /= rendered;
        renderer.paintCompleted(rendered, recomputed, meanResolution);
    }

    /**
     * Returns a path iterator for this isoline.
     */
    public synchronized PathIterator getPathIterator(final AffineTransform transform) {
        return new PolygonPathIterator(getPolygonList(true).iterator(), transform);
    }

    /**
     * Returns a flattened path iterator for this isoline.
     */
    public PathIterator getPathIterator(final AffineTransform transform, final double flatness) {
        assert flattened == checkFlattenedShape() : flattened;
        if (flattened) {
            return getPathIterator(transform);
        } else {
            return super.getPathIterator(transform, flatness);
        }
    }

    /**
     * Returns <code>true</code> if {@link #getPathIterator} returns a flattened iterator.
     * In this case, there is no need to wrap it into a {@link FlatteningPathIterator}.
     */
    private boolean checkFlattenedShape() {
        for (int i=polygonCount; --i>=0;) {
            if (!polygons[i].isFlattenedShape()) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns the number of points describing this isobath.
     */
    public synchronized int getPointCount() {
        int total=0;
        for (int i=polygonCount; --i>=0;) {
            total += polygons[i].getPointCount();
        }
        return total;
    }

    /**
     * Returns an estimation of memory usage in bytes. This method is for information
     * purposes only. The memory really used by two isolines may be lower than the sum
     * of their  <code>getMemoryUsage()</code>  return values,  since isolines try to
     * share their data when possible. Furthermore, this method does not take into account
     * the extra bytes generated by Java Virtual Machine for each object.
     *
     * @return An <em>estimation</em> of memory usage in bytes.
     */
    final synchronized long getMemoryUsage() {
        long total = 48;
        if (polygons != null) {
            total += 4*polygons.length;
        }
        for (int i=polygonCount; --i>=0;) {
            total += polygons[i].getMemoryUsage();
        }
        return total;
    }

    /**
     * Returns the set of {@link Polygon} objects in this isoline. If possible,
     * the set's iterator will return continents last and small lakes within the
     * continents first. Therefore, rendering done in a reverse order should yield
     * good results.
     * <br><br>
     * The order in which the iterator returns {@link Polygon} objects make it
     * convenient to find the smallest island or lake that a given point contains.
     * For example:
     *
     * <blockquote><pre>
     * &nbsp;public Polygon getSmallestIslandAt(double x, double y) {
     * &nbsp;    final Iterator it = isobath.getPolygons().iterator();
     * &nbsp;    while (it.hasNext()) {
     * &nbsp;        final Polygon île=(Polygon) it.next();
     * &nbsp;        if (île.contains(x,y)) {
     * &nbsp;            return île;
     * &nbsp;        }
     * &nbsp;    }
     * &nbsp;    return null;
     * &nbsp;}
     * </pre></blockquote>
     *
     * @return A set of {@link Polygon} objects.
     */
    public synchronized Set getPolygons() {
        return new LinkedHashSet(getPolygonList(false));
    }

    /**
     * Returns the set of polygons as a list. This method is faster than
     * {@link #getPolygons} and is optimized for {@link #getPathIterator}.
     *
     * @param  reverse <code>true</code> for reversing order (i.e. returning
     *         big continents first, and small lakes or islands last). This
     *         reverse order is appropriate for rendering, while the "normal"
     *         order is more appropriate for searching a polygon.
     * @return The set of polygons. This set will be ordered if possible.
     */
    private List getPolygonList(final boolean reverse) {
        assert Thread.holdsLock(this);
        if (!sorted) {
            sort();
        }
        final List list = new ArrayList(polygonCount);
        for (int i=polygonCount; --i>=0;) {
            final Polygon polygon = (Polygon) polygons[i].clone();
            polygon.setRenderingResolution(0);
            list.add(polygon);
        }
        if (!reverse) {
            // Elements were inserted in reverse order.
            // (reminder: this method is optimized for getPathIterator)
            Collections.reverse(list);
        }
        return list;
    }

    /**
     * Returns the set of polygons containing the specified point.
     *
     * @param  point A coordinate expressed according to {@link #getCoordinateSystem}.
     * @return The set of polygons under the specified point.
     */
    public synchronized Set getPolygons(final Point2D point) {
        if (getCachedBounds().contains(point)) {
            if (!sorted) {
                sort();
            }
            final Polygon[] copy = new Polygon[polygonCount];
            System.arraycopy(polygons, 0, copy, 0, polygonCount);
            return new FilteredSet(copy, point, null, null);
        }
        return Collections.EMPTY_SET;
    }

    /**
     * Returns the set of polygons containing or intersecting the specified shape.
     *
     * @param  shape A shape with coordinates expressed according to {@link #getCoordinateSystem}.
     * @param  intersects <code>false</code> to search for polygons containing <code>shape</code>,
     *         or <code>true</code> to search for polygons intercepting <code>shape</code>.
     * @return The set of polygons containing or intersecting the specified shape.
     */
    public synchronized Set getPolygons(final Shape shape, final boolean intersects) {
        if (shape.intersects(getCachedBounds())) {
            if (!sorted) {
                sort();
            }
            final Polygon[] copy = new Polygon[polygonCount];
            System.arraycopy(polygons, 0, copy, 0, polygonCount);
            if (intersects) {
                return new FilteredSet(copy, null, null, shape);
            } else {
                return new FilteredSet(copy, null, shape, null);
            }
        }
        return Collections.EMPTY_SET;
    }

    /**
     * Returns the polygon's name at the specified location.
     *
     * @param  point Coordinates in this isoline's coordinate system
     *         (as returned by {@link #getCoordinateSystem}).
     * @param  locale The desired locale for the polygon name.
     * @return The polygon name at the given location,
     *         or <code>null</code> if there is none.
     */
    public synchronized String getPolygonName(final Point2D point, final Locale locale) {
        if (getCachedBounds().contains(point)) {
            if (!sorted) {
                sort();
            }
            for (int i=0; i<polygonCount; i++) {
                final Polygon polygon = polygons[i];
                final String name = polygon.getName(locale);
                if (name!=null && polygon.contains(point)) {
                    return name;
                }
            }
        }
        return null;
    }

    /**
     * Adds points to this isobath. The data must be written in (<var>x</var>,<var>y</var>) pairs
     * in this isoline's coordinate system.
     * ({@link #getCoordinateSystem}). <code>NaN</code>s will be considered to be holes;
     * no line will join the points between two <code>NaN</code>s.
     *
     * @param  data Coordinate table (may contain NaNs). These data will be copied in such a way
     *         that any future modification of <code>data</code> will have no impact on the
     *         polygons created.
     */
    public synchronized void add(final float[] array) {
        final Polygon[] toAdd = Polygon.getInstances(array, coordinateSystem);
        for (int i=0; i<toAdd.length; i++) {
            if (!toAdd[i].isEmpty()) {
                addImpl(toAdd[i]);
            }
        }
    }

    /**
     * Add polygons from the specified shape. Shape's coordinates
     * must be express in this isoline's coordinate system.
     *
     * @param  shape The shape to add.
     * @throws IllegalArgumentException if the specified shape can't be added. This error may
     *         occur if <code>shape</code> is an instance of {@link Polygon} or {@link Isoline}
     *         and uses an incompatible coordinate system and/or {@linkplain #value}.
     */
    public synchronized void add(final Shape shape) throws IllegalArgumentException {
        try {
            if (shape instanceof Polygon) {
                add((Polygon) shape);
                return;
            }
            if (shape instanceof Isoline) {
                add((Isoline) shape);
                return;
            }
        } catch (TransformException exception) {
            // TODO: localize this message, if it is worth it.
            final IllegalArgumentException e = new IllegalArgumentException("Incompatible CS");
            e.initCause(exception);
            throw e;
        }
        final Polygon[] toAdd = Polygon.getInstances(shape, coordinateSystem);
        for (int i=0; i<toAdd.length; i++) {
            if (!toAdd[i].isEmpty()) {
                addImpl(toAdd[i]);
            }
        }
    }

    /**
     * Add all polygons from the specified isoline. Both isolines must have
     * the same {@linkplain #value}.
     *
     * @param  toAdd Isoline to add.
     * @throws IllegalArgumentException if both isolines don't
     *         have the same {@linkplain #value}.
     * @throws TransformException if the specified isoline can't
     *         be transformed in this isoline's coordinate system.
     */
    public synchronized void add(final Isoline toAdd) throws TransformException {
        if (toAdd != null) {
            if (Float.floatToIntBits(toAdd.value) != Float.floatToIntBits(value)) {
                // TODO: localize this message, if it is worth it.
                throw new IllegalArgumentException("Incompatible values");
            }
            final Polygon[] polyToAdd = (Polygon[]) toAdd.polygons.clone();
            for (int i=0; i<polyToAdd.length; i++) {
                add(polyToAdd[i]);
            }
        }
    }

    /**
     * Add a polygon to this isoline.
     *
     * @param  toAdd Polygon to add.
     * @throws TransformException if the specified polygon can't
     *         be transformed in this isoline's coordinate system.
     */
    public synchronized void add(Polygon toAdd) throws TransformException {
        if (toAdd != null) {
            toAdd = (Polygon) toAdd.clone();
            if (coordinateSystem != null) {
                toAdd.setCoordinateSystem(coordinateSystem);
            } else {
                coordinateSystem = toAdd.getCoordinateSystem();
                if (coordinateSystem!=null) {
                    setCoordinateSystem(coordinateSystem);
                }
            }
            addImpl(toAdd);
        }
    }

    /**
     * Add a polylgon to this isoline. This method does not clone
     * the polygon and doesn't set the coordinate system.
     */
    private void addImpl(final Polygon toAdd) {
        assert Thread.holdsLock(this);
        if (polygons == null) {
            polygons = new Polygon[16];
        }
        if (polygonCount >= polygons.length) {
            polygons = (Polygon[])XArray.resize(polygons, polygonCount+Math.min(polygonCount, 256));
        }
        polygons[polygonCount++] = toAdd;
        sorted = false;
        bounds = null;
        flattened = checkFlattenedShape();
    }

    /**
     * Remove a polylgon from this isobath.
     *
     * @param toRemove The polygon to remove.
     * @return <code>true</code> if the polygon has been removed.
     */
    public synchronized boolean remove(final Polygon toRemove) {
        boolean removed = false;
        for (int i=polygonCount; --i>=0;) {
            if (polygons[i].equals(toRemove)) {
                remove(i);
                removed = true;
            }
        }
        return removed;
        // No change to sorting order.
    }

    /**
     * Remove the polygon at the specified index.
     */
    private void remove(final int index) {
        assert Thread.holdsLock(this);
        bounds = null;
        System.arraycopy(polygons, index+1, polygons, index, polygonCount-(index+1));
        polygons[--polygonCount] = null;
        flattened = checkFlattenedShape();
    }

    /**
     * Remove all polygons from this isoline.
     */
    public synchronized void removeAll() {
        polygons = null;
        polygonCount = 0;
        clearCache();
    }

    /**
     * Returns the isoline's resolution.  The mean resolution is the mean distance between
     * every pair of consecutive points in this isoline  (ignoring "extra" points used for
     * drawing a border, if there is one). This method tries to express the resolution in
     * linear units (usually meters) no matter whether the coordinate systems is actually a
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
     *
     * @see Polygon#getResolution
     */
    public synchronized Statistics getResolution() {
        if (resolution == null) {
            for (int i=polygonCount; --i>=0;) {
                final Statistics toAdd = polygons[i].getResolution();
                if (resolution == null) {
                    resolution = toAdd;
                } else {
                    resolution.add(toAdd);
                }
            }
        }
        return (Statistics) resolution.clone();
    }

    /**
     * Set the isoline's resolution. This method tries to interpolate new points in such a way
     * that every point is spaced by exactly <code>resolution</code> units (usually meters)
     * from the previous one.
     *
     * @param  resolution Desired resolution, in the same units as {@link #getResolution}.
     * @throws TransformException If some coordinate transformations were needed and failed.
     *         There is no guarantee on contour's state in case of failure.
     *
     * @see Polygon#setResolution
     */
    public synchronized void setResolution(final double resolution) throws TransformException {
        bounds = null;
        for (int i=polygonCount; --i>=0;) {
            final Polygon polygon = polygons[i];
            polygon.setResolution(resolution);
            if (polygon.isEmpty()) {
                remove(i);
            }
        }
    }

    /**
     * Compress all polygons in this isoline. This method invokes {@link Polygon#compress}
     * for each polygon.
     *
     * @param  factor Facteur contrôlant la baisse de résolution.  Les valeurs élevées
     *         déciment davantage de points, ce qui réduit d'autant la consommation de
     *         mémoire. Ce facteur est généralement positif, mais il peut aussi être 0
     *         ou même légèrement négatif.
     * @return A <em>estimation</em> of the compression rate. For example a value of 0.2
     *         means that the new polygon uses <em>approximately</em> 20% less memory.
     *         Warning: this value may be inaccurate, for example if the old polygon was
     *         used to share its data with an other polygon, compressing one polygon
     *         may actually increase memory usage since the two polygons will no longer
     *         share their data.
     * @throws TransformException If an error has occurred during a cartographic projection.
     *
     * @see Polygon#compress
     */
    public synchronized float compress(final float factor) throws TransformException {
        polygons = (Polygon[]) XArray.resize(polygons, polygonCount);
        bounds   = null;
        final long memoryUsage = getMemoryUsage();
        for (int i=polygonCount; --i>=0;) {
            final Polygon polygon = polygons[i];
            polygon.compress(factor);
            if (polygon.isEmpty()) {
                remove(i);
            }
        }
        clearCache();
        return (float) (memoryUsage - getMemoryUsage()) / (float) memoryUsage;
        // No change to sorting order.
    }

    /**
     * Returns an isoline approximately equal to this isoline clipped to the specified bounds.
     * The clip is only approximate in that the resulting isoline may extend outside the clip
     * area. However, it is guaranteed that the resulting isoline contains at least all the interior
     * of the clip area.
     *
     * If this method can't perform the clip, or if it believes that it isn't worth doing a clip,
     * it returns <code>this</code>. If this isoline doesn't intersect the clip area, then this
     * method returns <code>null</code>. Otherwise, a new isoline is created and returned. The new
     * isoline will try to share as much internal data as possible with <code>this</code> in order
     * to keep memory footprint low.
     *
     * @param  clip The clipping area in this {@linkplain #getCoordinateSystem isoline's coordinate
               system}.
     * @return <code>null</code> if this isoline doesn't intersect the clip, <code>this</code>
     *         if no clip has been performed, or a new clipped isoline otherwise.
     */
    public synchronized Isoline clip(final Rectangle2D clip) {
        final Clipper        clipper = new Clipper(clip, coordinateSystem);
        final Polygon[] clipPolygons = new Polygon[polygonCount];
        int         clipPolygonCount = 0;
        boolean              changed = false;
        /*
         * Clip all polygons, discarding polygons outside the clip.
         */
        for (int i=0; i<polygonCount; i++) {
            final Polygon toClip  = polygons[i];
            final Polygon clipped = toClip.clip(clipper);
            if (clipped!=null && !clipped.isEmpty()) {
                clipPolygons[clipPolygonCount++] = clipped;
                if (toClip != clipped) {
                    changed = true;
                }
            } else {
                changed = true;
            }
        }
        if (changed) {
             final Isoline isoline = new Isoline(value, coordinateSystem);
             isoline.polygons      = (Polygon[]) XArray.resize(clipPolygons, clipPolygonCount);
             isoline.polygonCount  = clipPolygonCount;
             isoline.setName(super.getName(null));
             if (coordinateSystem.equals(clipper.mapCS, false)) {
                isoline.bounds = bounds.createIntersection(clipper.mapClip);
                // Note: Bounds computed above may be bigger than the bounds usually computed
                //       by 'getBounds2D()'.  However, these bigger bounds conform to Shape
                //       specification and are also desirable.  If the bounds were smaller than
                //       the clip, the rendering code would wrongly believe that the clipped
                //       isoline is inappropriate for the clipping area. It would slow down the
                //       rendering, but would not affect the visual result.
             }
             return isoline;
        } else {
            return this;
        }
    }

    /**
     * Returns a hash value for this isoline.
     */
    public synchronized int hashCode() {
        int code = (int)serialVersionUID;
        for (int i=0; i<polygonCount; i++) {
            // Must be insensitive to order.
            code += polygons[i].hashCode();
        }
        return code;
    }

    /**
     * Compare this isoline with the specified isoline.  Note that this method is
     * inconsistent with <code>equals</code>. <code>compareTo</code> compares only
     * isoline values, while <code>equals</code> compares all polygon points.   The
     * natural ordering for <code>Isoline</code> is convenient for sorting isolines
     * in increasing order of altitude.
     *
     * @param  iso The isoline to compare value with.
     * @return <ul>
     *           <li>+1 if this isoline's value is greater than the specified isoline value.</li>
     *           <li>-1 if this isoline's value is less than the specified isoline value.</li>
     *           <li>0 if both isolines have the same value.</li>
     *         </ul>
     */
    public int compareTo(final Object iso) {
        return Float.compare(value, ((Isoline) iso).value);
    }

    /**
     * Compares the specified object with this isoline for equality.
     * This methods checks for isoline values ({@link #value})  and
     * all polygon points. This is different from {@link #compareTo},
     * which compares only isoline values.
     */
    public synchronized boolean equals(final Object object) {
        if (object==this) {
            // Slight optimization
            return true;
        }
        if (super.equals(object)) {
            final Isoline that = (Isoline) object;
            if (Float.floatToIntBits(this.value) == Float.floatToIntBits(that.value) &&
                this.polygonCount == that.polygonCount)
            {
                // Compare ignoring order. Note: we don't call any synchronized
                // methods on 'that' in order to avoid deadlock.
                return getPolygons().containsAll(that.getPolygonList(true));
            }
        }
        return false;
    }

    /**
     * Return a copy of this isoline. The clone has a deep copy semantic,
     * but will share many internal arrays with the original isoline.
     *
     * @see Polygon#clone
     */
    public synchronized Object clone() {
        // Note: we can't use the 'Isoline(Isoline)' constructor,
        //       because the user way have subclassed this isoline.
        final Isoline isoline = (Isoline) super.clone();
        isoline.polygons = new Polygon[polygonCount];
        for (int i=isoline.polygons.length; --i>=0;) {
            isoline.polygons[i] = (Polygon) polygons[i].clone();
        }
        return isoline;
    }

    /**
     * Deletes all the information that was kept in an internal cache. This method can be
     * called when we know that this isoline will no longer be used before a particular time.
     * It does not cause the loss of any information, but will make subsequent uses of this
     * isoline slower (the time the internal caches take to be reconstructed, after which the
     * isoline will resume its normal speed).
     */
    final void clearCache() {
        bounds     = null;
        resolution = null;
        for (int i=polygonCount; --i>=0;) {
            polygons[i].clearCache();
        }
        flattened = checkFlattenedShape();
    }

    /**
     * Invoked during deserialization.
     */
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        flattened = checkFlattenedShape(); // Reasonably fast to compute.
    }

    /**
     * Sorts the polygons in order to make small islands or lakes appear
     * first and big continents last.
     *
     * @task TODO: Not yet implemented.
     */
    private void sort() {
        // TODO
    }

    /**
     * Assemble all polygons in the specified set of isolines. This method assembles polylines in
     * order to create closed {@linkplain Polygon polygons}. It analyses all available polylines
     * and merges together the polylines that look like parts of the same polygons. This class can
     * also complete the polygons that were cut by the map border.
     *
     * This method is useful in the context of isolines digitized from many consecutive maps
     * (for example the GEBCO digital atlas).  It is not possible to fill polygons with Java2D
     * if the polygons are broken in many pieces. Running this method <strong>once</strong> for
     * a given set of isolines before renderering helps to repair them. The algorithm is:
     *
     * <ol>
     *   <li>A list of all possible pairs of polylines is built.</li>
     *   <li>For any pair of polylines, the shortest distance between their extremities is
     *       computed. All combinations between the beginning and the end of a polyline with
     *       the beginning or end of the other polyline are taken into account.</li>
     *   <li>The pair with the shortest distance are identified. When the shortest distance
     *       from one polyline's extremity is the other extremity of the same polyline, then
     *       the polyline is identified as a closed polygon (e.g. an island or a lake).
     *       Otherwise, the closest polylines are merged together.</li>
     *   <li>The loop is reexecuted from step 1 until no more polylines have been merged.</li>
     * </ol>
     *
     * @param  isolines Isolines to assemble. Isolines are updated in place.
     * @param  toComplete {@link Isoline#value} of isoline to complete with map border.
     *         Usually, only the coast line is completed (<code>value==0</code>).
     * @param  The bounded shape of the map, or <code>null</code> for assuming a rectangular
     *         map inferred from the <code>isolines</code>. This is the bounding shape of the
     *         software that created isoline data, not an arbitrary clip that the application
     *         would like.
     * @param  progress An optional progress listener (<code>null</code> in none).
     * @throws TransformException if a transformation was required and failed.
     */
    public static void assemble(final Isoline[]        isolines,
                                final float[]          toComplete,
                                final Shape            mapBounds,
                                final ProgressListener progress)
            throws TransformException
    {
        PolygonAssembler.assemble((Isoline[])isolines.clone(),
                                  (float[])toComplete.clone(),
                                  mapBounds, progress);
        for (int i=0; i<isolines.length; i++) {
            isolines[i].clearCache();
        }
    }

    /**
     * Assemble all polygons in the specified set of isolines. This is a convenience method for
     * <code>{@link #assemble(Isoline[],float[],Shape,ProgressListener) assemble}(isolines,
     * new float[]{-0f,0f}, null, progress)</code>.
     *
     * @param  isolines Isolines to assemble. Isolines are updated in place.
     * @param  progress An optional progress listener (<code>null</code> in none).
     * @throws TransformException if a transformation was required and failed.
     */
    public static void assemble(final Isoline[]        isolines,
                                final ProgressListener progress)
            throws TransformException
    {
        assemble(isolines, new float[]{-0f,0f}, null, progress);
    }



    /**
     * The set of polygons under a point. The check of inclusion
     * or intersection will be performed only when needed.
     *
     * @version $Id: Isoline.java,v 1.11 2003/05/13 11:00:46 desruisseaux Exp $
     * @author Martin Desruisseaux
     */
    private static final class FilteredSet extends AbstractSet {
        /**
         * The polygons to check. This array must be a copy of
         * {@link Isoline#polygons}. It will be changed during
         * iteration: polygons that do not obey the condition
         * will be set to <code>null</code>.
         */
        final Polygon[] polygons;

        /**
         * The point to check for inclusion, or <code>null</code> if none.
         */
        private final Point2D point;

        /**
         * The shape to check for inclusion, or <code>null</code> if none.
         */
        private final Shape contains;

        /**
         * The shape to check for intersection, or <code>null</code> if none.
         */
        private final Shape intersects;

        /**
         * Index of the next polygon to check. All polygons
         * before this index are considered valid.
         */
        private int upper;

        /**
         * Construct a filtered set.
         *
         * @param polygons The polygon array. This array <strong>must be a copy</strong>
         *                 of {@link Isoline#polygons}. It must not be the original!
         */
        public FilteredSet(final Polygon[] polygons, final Point2D point,
                           final Shape contains, final Shape intersects)
        {
            this.polygons   = polygons;
            this.point      = point;
            this.contains   = contains;
            this.intersects = intersects;
        }

        /**
         * Returns the index of the next valid polygon starting at of after the specified
         * index. If there is no polygon left, it returns a number greater than or equal to
         * <code>polygons.length</code>. This method should be invoked with increasing
         * value of <code>from</code> only (values in random order are not supported).
         */
        final int next(int from) {
            while (from < polygons.length) {
                Polygon polygon = polygons[from];
                if (polygon != null) {
                    if (from >= upper) {
                        // This polygon has not been
                        // checked yet for validity.
                        upper = from+1;
                        if ((     point!=null && !polygon.contains  (point   )) ||
                            (  contains!=null && !polygon.contains  (contains)) ||
                            (intersects!=null && !polygon.intersects(intersects)))
                        {
                            polygons[from] = null;
                            continue;
                        }
                        polygon = (Polygon) polygon.clone();
                        polygon.setRenderingResolution(0);
                        polygons[from] = polygon;
                    }
                    break;
                }
            }
            return from;
        }
        
        /**
         * Returns the number of elements in this collection.
         */
        public int size() {
            int count = 0;
            for (int i=next(0); i<polygons.length; i=next(i+1)) {
                count++;
            }
            return count;
        }

        /**
         * Returns an iterator over the elements in this collection.
         */
        public Iterator iterator() {
            return new Iterator() {
                /** Index of the next valid polygon. */
                private int index = FilteredSet.this.next(0);

                /** Check if there are more polygons. */
                public boolean hasNext() {
                    return index < polygons.length;
                }

                /** Returns the next polygon. */
                public Object next() {
                    if (index < polygons.length) {
                        final Polygon next = polygons[index];
                        index = FilteredSet.this.next(index+1);
                        return next;
                    } else {
                        throw new NoSuchElementException();
                    }
                }

                /** Unsupported operation. */
                public void remove() {
                    throw new UnsupportedOperationException();
                }
            };
        }
    }
}
