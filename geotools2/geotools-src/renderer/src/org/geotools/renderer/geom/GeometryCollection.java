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
import java.util.Locale;
import java.util.Date;
import java.util.Set;
import java.util.List;
import java.util.Arrays;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.AbstractCollection;
import java.util.NoSuchElementException;
import java.text.NumberFormat;
import java.text.DateFormat;
import java.text.Format;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

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
 * A collection of geometry shapes. Included geometries may be {@link Polyline}s, {@link Polygon}s
 * or others {@link GeometryCollection}. Regrouping related polygons in a single collection
 * help to speed up the rendering. Polygons can be regrouped on a spatial basis (European polygons,
 * African polygons, etc.) or on a value basis (50 meters isobath, 100 meters isobath, etc.).
 * <br><br>
 * A <code>GeometryCollection</code> is initially built with a {@linkplain CoordinateSystem
 * coordinate system}. An arbitrary amount of {@linkplain Geometry geometries} can be added after
 * construction using {@link #add(Geometry)} or {@link #add(float[])}. Geometries will be rendered
 * in the order they were added. If polygons are broken in many pieces, then the
 * {@link #assemble(Shape,float[],ProgressListener) assemble(...)} method may help
 * to assemble them before rendering.
 * <br><br>
 * <strong>Note:</strong> this class has a natural ordering that is inconsistent with equals.
 * The {@link #compareTo} method compares only the collection's {@linkplain #setValue(Comparable)
 * value}, while {@link #equals} compares also all coordinate points. The natural ordering for
 * <code>GeometryCollection</code> is convenient for sorting collections in alphabetical order
 * or isobaths in increasing order of altitude.
 *
 * @version $Id: GeometryCollection.java,v 1.1 2003/05/27 18:22:43 desruisseaux Exp $
 * @author Martin Desruisseaux
 *
 * @task TODO: Add a 'getTree(boolean)' method returning a TreeNode. Would be usefull for debugging.
 *             Node contains GeometryCollection only if boolean argument is false, GeometryCollection
 *             and Polygons if true (not Polylines). Node.toString returns Geometry.getName().
 *
 * @see Polyline
 * @see Polygon
 */
public class GeometryCollection extends Geometry implements Comparable {
    /**
     * Version number for compatibility with geometry created with previous versions.
     */
    private static final long serialVersionUID = -6069518397739467506L;

    /**
     * A common value for {@link #value}. Declared in order to reduce
     * the amount of identical {@link Float} objects to be created.
     */
    private static final Float ZERO = new Float(0);

    /**
     * The value or name for this collection, or <code>null</code> if none.
     * For isobaths, the value is the altitude as a {@link Float} object.
     */
    private Comparable value;

    /**
     * Coordinate system.
     */
    private CoordinateSystem coordinateSystem;

    /**
     * Collection of geometries making up this <code>GeometryCollection</code> object.
     * Geometries will be rendered in the order they were added.
     */
    private Geometry[] geometries;

    /**
     * Number of valid elements in <code>geometries</code>.
     */
    private int count;

    /**
     * Immutable list of {@link Polyline} objects. Cached for faster rendering.
     */
    private transient List polylines;

    /**
     * Rectangle completely enclosing this collection. This rectangle is 
     * calculated just once and kept in an internal cache to accelerate
     * certain checks.
     */
    private transient Rectangle2D bounds;

    /**
     * <code>true</code> if {@link #getPathIterator} returns a flattened iterator.
     * In this case, there is no need to wrap it into a {@link FlatteningPathIterator}.
     */
    private transient boolean flattened;

    /**
     * The statistics about resolution, or <code>null</code> if none.
     * This object is computed when first requested and cached for subsequent uses.
     * It is also serialized if available, since it is somewhat heavy to compute.
     */
    private Statistics resolution;

    /**
     * Construct an initially empty collection using the
     * {@linkplain GeographicCoordinateSystem#WGS84 WGS84} coordinate system.
     * Polygons can be added using one of the <code>add(...)</code> methods.
     *
     * @see #add(float[])
     * @see #add(Shape)
     * @see #add(Geometry)
     */
    public GeometryCollection() {
        this(GeographicCoordinateSystem.WGS84);
    }

    /**
     * Construct an initially empty collection.
     * Polygons can be added using one of the <code>add(...)</code> methods.
     *
     * @param coordinateSystem The coordinate system to use for all
     *        points in this collection, or <code>null</code> if unknown.
     *
     * @see #add(float[])
     * @see #add(Shape)
     * @see #add(Geometry)
     */
    public GeometryCollection(final CoordinateSystem coordinateSystem) {
        this.coordinateSystem = coordinateSystem;
    }

    /**
     * Construct an collection with the same data as the specified collection.
     * The new collection will have a copy semantic, but the underlying arrays
     * of (<var>x</var>,<var>y</var>) points will be shared.
     */
    public GeometryCollection(final GeometryCollection geometry) {
        this.coordinateSystem = geometry.coordinateSystem;
        this.value            = geometry.value;
        this.count            = geometry.count;
        this.bounds           = geometry.bounds;
        this.polylines        = geometry.polylines;
        this.geometries       = new Geometry[count];
        for (int i=0; i<count; i++) {
            geometries[i] = (Geometry) geometry.geometries[i].clone();
        }
        flattened = checkFlattenedShape();
    }

    /**
     * Returns the localized name for this geometry, or <code>null</code> if none.
     *
     * @param  locale The desired locale. If no name is available
     *         for this locale, a default locale will be used.
     * @return The geometry's name, localized if possible.
     *
     * @task TODO: We should find a way to avoid the creation of Format object at each
     *             invocation.
     */
    public String getName(final Locale locale) {
        final Comparable value = this.value; // Avoid the need for synchronisation.
        if (locale != null) {
            if (value instanceof Number) {
                return NumberFormat.getInstance(locale).format(value);
            }
            if (value instanceof Date) {
                return DateFormat.getDateTimeInstance(DateFormat.LONG,
                                                      DateFormat.SHORT, locale).format(value);
            }
        }
        return (value!=null) ? value.toString() : null;
    }

    /**
     * Returns the value for this collection, or <code>NaN</code> if none.
     * If this collection is an isobath, then the value is typically the isobath altitude.
     */
    public float getValue() {
        return (value instanceof Number) ? ((Number) value).floatValue() : Float.NaN;
    }

    /**
     * Set the value for this geometry. If this geometry is an isobath,
     * then the value is typically the isobath altitude.
     */
    public void setValue(final float value) {
        setValue(value==0 ? ZERO : new Float(value));
    }

    /**
     * Set the value for this geometry. It may be a {@link String} (for example "Africa"),
     * or a value as a {@link Number} object (for example <code>{@link Float}(-50)</code>
     * for the -50 meters isobath). There is two advantages in using <code>Number</code>
     * instead of <code>String</code> for values:
     * <ul>
     *   <li>Better ordering with {@link #compareTo}.</li>
     *   <li>Locale-dependent formatting with {@link #getName}.</li>
     * </ul>
     *
     * @param value The value of value for this geometry.
     */
    public void setValue(final Comparable value) {
        this.value = ZERO.equals(value) ? ZERO : value;
    }

    /**
     * Returns the geometry's coordinate system, or <code>null</code> if unknown.
     */
    public synchronized CoordinateSystem getCoordinateSystem() {
        return coordinateSystem;
    }

    /**
     * Set the geometry's coordinate system. Calling this method is equivalent
     * to reprojecting all geometries from the old coordinate system to the new one.
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
        if (Utilities.equals(oldCoordinateSystem, coordinateSystem)) {
            return;
        }
        bounds     = null;
        resolution = null;
        polylines  = null;
        int i=count;
        try {
            while (--i>=0) {
                geometries[i].setCoordinateSystem(coordinateSystem);
            }
            this.coordinateSystem = coordinateSystem; // Do it last.
        } catch (TransformException exception) {
            /*
             * If a map projection failed, reset
             * to the original coordinate system.
             */
            while (++i < count) {
                try {
                    geometries[i].setCoordinateSystem(oldCoordinateSystem);
                } catch (TransformException unexpected) {
                    // Should not happen, since the old coordinate system is supposed to be ok.
                    Polyline.unexpectedException("setCoordinateSystem", unexpected);
                }
            }
            throw exception;
        }
        flattened = checkFlattenedShape();
    }




    ///////////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////
    ////////////                                                                       ////////////
    ////////////          M O D I F I E R S :   add / remove   M E T H O D S           ////////////
    ////////////                                                                       ////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Adds points to this collection. The points must be stored as (<var>x</var>,<var>y</var>)
     * pairs in this {@link #getCoordinateSystem geometry's coordinate system}. <code>NaN</code>
     * values will be considered as disjoint lines.
     *
     * @param  data Coordinate array (may contain NaNs). These data will be copied. Consequently,
     *         any modification on <code>data</code> will have no impact on the geometries created
     *         by this method.
     * @param  lower Index of the first <var>x</var> ordinate to add to the polyline.
     * @param  upper Index after of the last <var>y</var> ordinate to add to the polyline.
     */
    public synchronized void add(final float[] array, final int lower, final int upper) {
        final Polyline[] toAdd = Polyline.getInstances(array, lower, upper, coordinateSystem);
        for (int i=0; i<toAdd.length; i++) {
            addImpl(toAdd[i]);
        }
    }

    /**
     * Add geometries from the specified shape. Shape's coordinates must be
     * express in this {@link #getCoordinateSystem geometry's coordinate system}.
     *
     * @param  shape The shape to add.
     * @throws IllegalArgumentException if the specified shape can't be added. This error may
     *         occur if <code>shape</code> is an instance of {@link Geometry} and uses an
     *         incompatible coordinate system.
     */
    public synchronized void add(final Shape shape) throws IllegalArgumentException {
        if (shape instanceof Geometry) try {
            add((Geometry) shape);
            return;
        } catch (TransformException exception) {
            // TODO: localize this message, if it is worth it.
            final IllegalArgumentException e = new IllegalArgumentException("Incompatible CS");
            e.initCause(exception);
            throw e;
        }
        final Polyline[] toAdd = Polyline.getInstances(shape, coordinateSystem);
        for (int i=0; i<toAdd.length; i++) {
            addImpl(toAdd[i]);
        }
    }

    /**
     * Add a geometry to this collection.
     *
     * @param  toAdd Geometry to add.
     * @throws TransformException if the specified geometry can't
     *         be transformed in this collection coordinate system.
     */
    public synchronized void add(Geometry toAdd) throws TransformException {
        if (toAdd != null) {
            toAdd = (Geometry) toAdd.clone();
            if (coordinateSystem != null) {
                toAdd.setCoordinateSystem(coordinateSystem);
            } else {
                coordinateSystem = toAdd.getCoordinateSystem();
                if (coordinateSystem != null) {
                    setCoordinateSystem(coordinateSystem);
                }
            }
            addImpl(toAdd);
        }
    }

    /**
     * Add a geometry to this collection. This method does not clone
     * the geometry and doesn't set the coordinate system.
     */
    private void addImpl(final Geometry toAdd) {
        assert Thread.holdsLock(this);
        if (!toAdd.isEmpty()) {
            if (geometries == null) {
                geometries = new Geometry[16];
            }
            if (count >= geometries.length) {
                geometries = (Geometry[])XArray.resize(geometries, count+Math.min(count, 256));
            }
            geometries[count++] = toAdd;
            bounds    = null;
            polylines = null;
            if (flattened) {
                // May changes from 'true' to 'false'.
                flattened = checkFlattenedShape();
            }
        }
    }

    /**
     * Removes a geometry from this collection.
     *
     * @param toRemove The geometry to remove.
     * @return <code>true</code> if the geometry has been removed.
     */
    public synchronized boolean remove(final Geometry toRemove) {
        boolean removed = false;
        for (int i=count; --i>=0;) {
            if (geometries[i].equals(toRemove)) {
                remove(i);
                removed = true;
            }
        }
        return removed;
        // No change to sorting order.
    }

    /**
     * Remove the geometry at the specified index.
     */
    private void remove(final int index) {
        assert Thread.holdsLock(this);
        bounds    = null;
        polylines = null;
        System.arraycopy(geometries, index+1, geometries, index, count-(index+1));
        geometries[--count] = null;
        if (!flattened) {
            // May changes from 'false' to 'true'.
            flattened = checkFlattenedShape();
        }
    }

    /**
     * Remove all geometries from this geometry.
     */
    public synchronized void removeAll() {
        geometries = null;
        count = 0;
        clearCache();
    }




    ///////////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////
    ////////////                                                                       ////////////
    ////////////               A S S E M B L A G E   /   C L I P P I N G               ////////////
    ////////////                                                                       ////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Assemble all {@linkplain Polyline polylines} in order to create closed {@linkplain Polygon
     * polygons} for proper rendering. This method analyses all available polylines and merges
     * together the polylines that look like parts of the same polygons. It can also complete the
     * polygons that were cut by the map border.
     *
     * This method is useful in the context of geometries digitalized from many consecutive
     * maps (for example the GEBCO digital atlas). It is not possible to fill polygons with
     * <A HREF="http://java.sun.com/products/java-media/2D/">Java2D</A> if the polygons are
     * broken in many pieces, as in the figure below.
     *
     * <p align="center"><img src="doc-files/splitted.png"></p>
     *
     * <P>Running this method <strong>once</strong> for a given collection of geometries before
     * renderering helps to repair them. The algorithm is:</P>
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
     * This method will produces better results if this collection contains other
     * <code>GeometryCollection</code> objects (one for each isobath) with {@linkplain #setValue
     * value set} to the bathymetric value (for example <code>-50</code> for the -50 meters
     * isobath). The <code>toComplete</code> argument tells which isobaths to complete with
     * the map border provided by the <code>mapBounds</code> argument.
     *
     * @param  The bounded shape of the map, or <code>null</code> for assuming a rectangular
     *         map inferred from this geometry. This is the bounding shape of the software that
     *         created the polylines, not an arbitrary clip that the application would like.
     * @param  toComplete {@link #setName value} of collections to complete with map border,
     *         or <code>null</code> if none.
     * @param  progress An optional progress listener (<code>null</code> in none). This is an
     *         optional but recommanded argument, since the computation may be very long.
     * @throws TransformException if a transformation was required and failed.
     */
    public synchronized void assemble(Shape mapBounds, float[] toComplete,
                                      final ProgressListener progress)
            throws TransformException
    {
        if (mapBounds == null) {
            mapBounds = getBounds2D();
        }
        if (toComplete == null) {
            toComplete = new float[0];
        }
        final PolygonAssembler assembler = new PolygonAssembler(mapBounds, progress);
        assembler.assemble(this, toComplete);
    }

    /**
     * Assemble all {@linkplain Polyline polylines} with default setting. This convenience
     * method will complete the map border only for the 0 meters isobath.
     *
     * @param  progress An optional progress listener (<code>null</code> in none). This is an
     *         optional but recommanded argument, since the computation may be very long.
     * @throws TransformException if a transformation was required and failed.
     */
    public void assemble(final ProgressListener progress) throws TransformException {
        assemble(null, new float[]{-0f,0f}, progress);
    }

    /**
     * Returns an geometry approximately equal to this geometry clipped to the specified bounds.
     * The clip is only approximate in that the resulting geometry may extend outside the clip
     * area. However, it is guaranteed that the resulting geometry contains at least all the interior
     * of the clip area.
     *
     * If this method can't perform the clip, or if it believes that it isn't worth doing a clip,
     * it returns <code>this</code>. If this geometry doesn't intersect the clip area, then this
     * method returns <code>null</code>. Otherwise, a new geometry is created and returned. The new
     * geometry will try to share as much internal data as possible with <code>this</code> in order
     * to keep memory footprint low.
     *
     * @param  clip The clipping area.
     * @return <code>null</code> if this geometry doesn't intersect the clip, <code>this</code>
     *         if no clip has been performed, or a new clipped geometry otherwise.
     */
    public synchronized Geometry clip(final Clipper clipper) {
        Geometry[] clips = new Geometry[count];
        int    clipCount = 0;
        boolean  changed = false;
        /*
         * Clip all geometries, discarding geometries outside the clip.
         */
        for (int i=0; i<count; i++) {
            final Geometry toClip  = geometries[i];
            final Geometry clipped = toClip.clip(clipper);
            if (clipped!=null && !clipped.isEmpty()) {
                clips[clipCount++] = clipped;
                if (toClip != clipped) {
                    changed = true;
                }
            } else {
                changed = true;
            }
        }
        if (clipCount == 1) {
            return clips[0];
        }
        if (!changed) {
            return this;
        }
        final GeometryCollection geometry = new GeometryCollection(coordinateSystem);
        geometry.geometries = (Geometry[]) XArray.resize(clips, clipCount);
        geometry.count      = clipCount;
        geometry.value      = this.value;
        if (coordinateSystem.equals(clipper.mapCS, false)) {
            geometry.bounds = bounds.createIntersection(clipper.mapClip);
            // Note: Bounds computed above may be bigger than the bounds usually computed
            //       by 'getBounds2D()'.  However, these bigger bounds conform to Shape
            //       specification and are also desirable.  If the bounds were smaller than
            //       the clip, the rendering code would wrongly believe that the clipped
            //       geometry is inappropriate for the clipping area. It would slow down the
            //       rendering, but would not affect the visual result.
        }
        return geometry;
    }




    ///////////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////
    ////////////                                                                       ////////////
    ////////////         A C C E S S O R S :   'getGeometries'   M E T H O D S         ////////////
    ////////////                                                                       ////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Remove all {@link GeometryCollection} from this collection and returns
     * them in a separated list. This method is used by {@link PolygonAssembler}.
     */
    final List extractCollections() {
        assert Thread.holdsLock(this);
        int newCount = 0;
        final List collections = new ArrayList();
        for (int i=0; i<count; i++) {
            final Geometry geometry = geometries[i];
            if (geometry instanceof GeometryCollection) {
                collections.add(geometry);
            } else {
                geometries[newCount++] = geometry;
            }
        }
        count = newCount;
        trimToSize();
        return collections;
    }

    /**
     * Returns the list of polylines. <code>GeometryCollection</code>
     * objects are expanded into their {@link Polyline} elements. This
     * method is used by {@link #getPathIterator}.
     *
     * @param  list The list to fill.
     */
    private void getPolylines(final List list) {
        assert Thread.holdsLock(this);
        for (int i=count; --i>=0;) {
            final Geometry geometry = geometries[i];
            if (geometry instanceof Polyline) {
                list.add((Polyline) geometry);
            } else if (geometry instanceof GeometryCollection) {
                ((GeometryCollection) geometry).getPolylines(list);
            }
        }
    }

    /**
     * Returns the list of polylines in this collection and in all children
     * <code>GeometryCollection</code> instances. This list is usefull for
     * rendering.
     */
    public synchronized List getPolylines() {
        if (polylines == null) {
            final ArrayList list = new ArrayList(count);
            getPolylines(list);
            list.trimToSize();
            polylines = Collections.unmodifiableList(list);
        }
        return polylines;
    }

    /**
     * Returns the collection of {@link Geometry} objects. The collection will contains geometries
     * in the reverse order, i.e. geometries {@linkplain #add(Geometry) added} last will be
     * returned first. This convention make it easier to find the smallest visible feature
     * (for example an island draw in a lake) which contains a given point. For example:
     *
     * <blockquote><pre>
     * &nbsp;public Geometry getSmallestIslandAt(double x, double y) {
     * &nbsp;    final Iterator it = collection.getGeometries().iterator();
     * &nbsp;    while (it.hasNext()) {
     * &nbsp;        final Geometry island=(Geometry) it.next();
     * &nbsp;        if (island.contains(x,y)) {
     * &nbsp;            return island;
     * &nbsp;        }
     * &nbsp;    }
     * &nbsp;    return null;
     * &nbsp;}
     * </pre></blockquote>
     *
     * @return A collection of {@link Geometry} objects.
     */
    public synchronized Collection getGeometries() {
        // The 'Filtered' implementation uses a lazy iterator which
        // will clone the geometries only when first required.
        return new Filtered(this);
    }

    /**
     * Returns the collection of geometries containing the specified point.
     * The collection will contains geometries in the reverse order, i.e.
     * geometries {@linkplain #add(Geometry) added} last will be returned first.
     *
     * @param  point The coordinates to look at in this
     *               {@linkplain #getCoordinateSystem geometry's coordinate system}.
     * @return The collection of geometries under the specified point.
     */
    public synchronized Collection getGeometries(final Point2D point) {
        if (getCachedBounds().contains(point)) {
            return new Filtered(this) {
                protected boolean accept(final Geometry geometry) {
                    return geometry.contains(point);
                }
            };
        }
        return Collections.EMPTY_SET;
    }

    /**
     * Returns the collection of geometries containing the specified shape.
     * The collection will contains geometries in the reverse order, i.e.
     * geometries {@linkplain #add(Geometry) added} last will be returned first.
     *
     * @param  shape A shape with coordinates expressed according to {@link #getCoordinateSystem}.
     * @return The collection of geometries containing the specified shape.
     */
    public Collection getGeometriesContaining(final Shape shape) {
        if (shape.intersects(getCachedBounds())) {
            return new Filtered(this) {
                protected boolean accept(final Geometry geometry) {
                    return geometry.contains(shape);
                }
            };
        }
        return Collections.EMPTY_SET;
    }

    /**
     * Returns the collection of geometries intersecting the specified shape.
     * The collection will contains geometries in the reverse order, i.e.
     * geometries {@linkplain #add(Geometry) added} last will be returned first.
     *
     * @param  shape A shape with coordinates expressed according to {@link #getCoordinateSystem}.
     * @return The collection of geometries intersecting the specified shape.
     */
    public Collection getGeometriesIntersecting(final Shape shape) {
        if (shape.intersects(getCachedBounds())) {
            return new Filtered(this) {
                protected boolean accept(final Geometry geometry) {
                    return geometry.intersects(shape);
                }
            };
        }
        return Collections.EMPTY_SET;
    }

    /**
     * Returns the name of the smallest {@linkplain Polygon polygon} at the given location.
     * This method is usefull for formatting tooltip text when the mouse cursor moves over
     * the map.
     *
     * @param  point The coordinates to look at in this
     *               {@linkplain #getCoordinateSystem geometry's coordinate system}.
     * @param  locale The desired locale for the geometry name.
     * @return The geometry name at the given location, or <code>null</code> if there is none.
     */
    public synchronized String getPolygonName(final Point2D point, final Locale locale) {
        if (getCachedBounds().contains(point)) {
            String name;
            for (int i=0; i<count; i++) {
                final Geometry polygon = geometries[i];
                if (polygon instanceof GeometryCollection) {
                    name = ((GeometryCollection) polygon).getPolygonName(point, locale);
                    if (name != null) {
                        return name;
                    }
                } else {
                    name = polygon.getName(locale);
                    if (name!=null && polygon.contains(point)) {
                        return name;
                    }
                }
            }
        }
        return null;
    }




    ///////////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////
    ////////////                                                                       ////////////
    ////////////                S H A P E   I M P L E M E N T A T I O N                ////////////
    ////////////            getBounds2D() / contains(...) / intersects(...)            ////////////
    ////////////                                                                       ////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Determines whetever the collection is empty.
     */
    public synchronized boolean isEmpty() {
        for (int i=count; --i>=0;) {
            if (!geometries[i].isEmpty()) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns an estimation of memory usage in bytes. This method is for information
     * purposes only. The memory really used by two geometries may be lower than the sum
     * of their  <code>getMemoryUsage()</code>  return values,  since geometries try to
     * share their data when possible. Furthermore, this method does not take into account
     * the extra bytes generated by Java Virtual Machine for each object.
     *
     * @return An <em>estimation</em> of memory usage in bytes.
     */
    final synchronized long getMemoryUsage() {
        long total = 48;
        if (geometries != null) {
            total += 4*geometries.length;
        }
        for (int i=count; --i>=0;) {
            total += geometries[i].getMemoryUsage();
        }
        return total;
    }

    /**
     * Returns the number of points in this geometry.
     */
    public synchronized int getPointCount() {
        int n = 0;
        for (int i=count; --i>=0;) {
            n += geometries[i].getPointCount();
        }
        return n;
    }

    /**
     * Return the bounding box of this geometry. This method returns
     * a direct reference to the internally cached bounding box. DO
     * NOT MODIFY!
     */
    private Rectangle2D getCachedBounds() {
        assert Thread.holdsLock(this);
        if (bounds == null) {
            for (int i=count; --i>=0;) {
                final Geometry polygon = geometries[i];
                if (!polygon.isEmpty()) {
                    final Rectangle2D polygonBounds = polygon.getBounds2D();
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
     * Return the bounding box of this geometry, including its possible
     * borders. This method uses a cache, such that after a first call,
     * the following calls should be fairly quick.
     *
     * @return A bounding box of this geometry. Changes to this rectangle
     *         will not affect the cache.
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
     * Indicates whether the specified (<var>x</var>,<var>y</var>) point is inside this geometry.
     * The point coordinates must be expressed in the geometry's coordinate system, that is
     * {@link #getCoordinateSystem()}.
     */
    public synchronized boolean contains(final double x, final double y) {
        if (getCachedBounds().contains(x,y)) {
            for (int i=0; i<count; i++) {
                if (geometries[i].contains(x,y)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Indicates whether the specified point is inside this geometry.
     * The point coordinates must be expressed in the geometry's coordinate system,
     * that is {@link #getCoordinateSystem()}.
     */
    public synchronized boolean contains(final Point2D point) {
        if (getCachedBounds().contains(point)) {
            for (int i=0; i<count; i++) {
                if (geometries[i].contains(point)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Checks whether the specified rectangle is entirely contained within this geometry.
     * The rectangle's coordinates should be expressed in the geometry's coordinate system,
     * that is {link #getCoordinateSystem()}.
     */
    public synchronized boolean contains(final Rectangle2D rect) {
        if (getCachedBounds().contains(rect)) {
            final Polygon shape = new Polygon(rect, getCoordinateSystem());
            for (int i=0; i<count; i++) {
                if (geometries[i].contains(shape)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Checks whether the specified shape is entirely contained within this geometry.
     * The shape's coordinates must be expressed in the geometry's coordinate system, 
     * that is {@link #getCoordinateSystem()}.
     */
    public synchronized boolean contains(final Shape shape) {
        if (getCachedBounds().contains(shape.getBounds2D())) {
            for (int i=0; i<count; i++) {
                if (geometries[i].contains(shape)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Tests whether the specified rectangle intersects the interior of this geometry.
     */
    public synchronized boolean intersects(final Rectangle2D rect) {
        if (getCachedBounds().intersects(rect)) {
            final Polygon shape = new Polygon(rect, getCoordinateSystem());
            for (int i=0; i<count; i++) {
                if (geometries[i].intersects(shape)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Tests whether the specified shape intersects the interior of this geometry.
     */
    public synchronized boolean intersects(final Shape shape) {
        if (getCachedBounds().intersects(shape.getBounds2D())) {
            for (int i=0; i<count; i++) {
                if (geometries[i].intersects(shape)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Test if the {@linkplain #getBounds2D bounding box} of this geometry intersects the
     * interior of the specified shape. This method is less precise but faster than invoking
     * {@link #intersects(Shape)}.
     */
    public synchronized boolean boundsIntersects(final Shape shape) {
        return shape.intersects(getCachedBounds());
    }




    ///////////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////
    ////////////                                                                       ////////////
    ////////////    C O M P R E S S I O N   /   R E S O L U T I O N   S E T T I N G    ////////////
    ////////////                                                                       ////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Trim the {@link #geometries} array to its minimal size.
     */
    private void trimToSize() {
        geometries = (Geometry[]) XArray.resize(geometries, count);
    }

    /**
     * Compress all geometries in this collection. The <code>level</code> argument specify the
     * algorithm, which may be desctructive (i.e. data may loose precision). For example, the
     * compression may replaces direct positions by relative positions, as in the figures below:
     *
     *       <table cellspacing='12'><tr>
     *       <td><p align="center"><img src="doc-files/uncompressed.png"></p></td>
     *       <td><p align="center"><img src="doc-files/compressed.png"></p></td>
     *       </tr></table>
     *
     * @param  level The compression level (or algorithm) to use. See the {@link CompressionLevel}
     *         javadoc for an explanation of available algorithms.
     * @return A <em>estimation</em> of the compression rate. For example a value of 0.2
     *         means that the new polygon uses <em>approximately</em> 20% less memory.
     * @throws TransformException If an error has occurred during a cartographic projection.
     */
    public synchronized float compress(final CompressionLevel level) throws TransformException {
        bounds    = null;
        polylines = null;
        int newCount = 0;
        final long memoryUsage = getMemoryUsage();
        for (int i=0; i<count; i++) {
            final Geometry polygon = geometries[i];
            polygon.compress(level);
            if (!polygon.isEmpty()) {
                geometries[newCount++] = polygon;
            }
        }
        count = newCount;
        trimToSize();
        clearCache();
        return (float) (memoryUsage - getMemoryUsage()) / (float) memoryUsage;
    }

    /**
     * Returns the geometry's resolution. The mean resolution is the mean distance between
     * every pair of consecutive points in this geometry. This method tries to express the
     * resolution in linear units (usually meters) no matter whether the coordinate systems
     * is actually a {@linkplain ProjectedCoordinateSystem projected} or a
     * {@linkplain GeographicCoordinateSystem geographic} one.
     */
    public synchronized Statistics getResolution() {
        if (resolution == null) {
            for (int i=count; --i>=0;) {
                final Statistics toAdd = geometries[i].getResolution();
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
     * Set the geometry's resolution. This method tries to interpolate new points in such a way
     * that every point is spaced by exactly <code>resolution</code> units (usually meters)
     * from the previous one.
     *
     * @param  resolution Desired resolution, in the same units as {@link #getResolution}.
     * @throws TransformException If some coordinate transformations were needed and failed.
     *         There is no guarantee on contour's state in case of failure.
     */
    public synchronized void setResolution(final double resolution) throws TransformException {
        bounds    = null;
        polylines = null;
        for (int i=count; --i>=0;) {
            final Geometry polygon = geometries[i];
            polygon.setResolution(resolution);
            if (polygon.isEmpty()) {
                remove(i);
            }
        }
    }

    /**
     * Returns the rendering resolution. This is the spatial resolution used by
     * {@link PathIterator} only; it has no effect on the underyling data.
     *
     * @return The rendering resolution in units of this geometry's
     *         {@linkplain #getCoordinateSystem coordinate system} (linear or angular units),
     *         or 0 if the finest available resolution should be used.
     */
    public synchronized float getRenderingResolution() {
        float resolution = 0;
        for (int i=count; --i>=0;) {
            final float candidate = geometries[i].getRenderingResolution();
            if (candidate > resolution) {
                resolution = candidate;
            }
        }
        return resolution;
    }

    /**
     * Hints this geometry that the specified resolution is sufficient for rendering.
     * Value 0 ask for the best available resolution. If a value greater than 0 is provided,
     * then the {@link PathIterator} will skip as many points as it can while preserving a
     * distance equals or smaller than <code>resolution</code> between two consecutive points.
     *
     * @param resolution The resolution to use at rendering time, in units of this geometry's
     *        {@linkplain #getCoordinateSystem coordinate system} (linear or angular units).
     */
    public synchronized void setRenderingResolution(float resolution) {
        for (int i=count; --i>=0;) {
            geometries[i].setRenderingResolution(resolution);
        }
    }




    ///////////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////
    ////////////                                                                       ////////////
    ////////////       P A T H   I T E R A T O R   /   M I S C E L L A N E O U S       ////////////
    ////////////                                                                       ////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Returns a path iterator for this geometry.
     */
    public synchronized PathIterator getPathIterator(final AffineTransform transform) {
        return new PolygonPathIterator(null, getPolylines().iterator(), transform);
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
        for (int i=count; --i>=0;) {
            if (!geometries[i].isFlattenedShape()) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns a hash value for this geometry.
     */
    public synchronized int hashCode() {
        int code = 0;//(int)serialVersionUID;
        for (int i=0; i<count; i++) {
            // Must be insensitive to order.
            code += geometries[i].hashCode();
        }
        return code;
    }

    /**
     * Compare this geometry with the specified object for order. Note that this method is
     * inconsistent with <code>equals</code>. The method <code>compareTo</code> compares only the
     * {@linkplain #setValue(Comparable) value}, while <code>equals</code> compares all coordinate
     * points. The natural ordering for <code>GeometryCollection</code> is convenient for sorting
     * geometries in alphabetical order or isobaths in increasing order of altitude. Geometries
     * without value are sorted last.
     *
     * @param  object The geometry to compare value with.
     * @return <ul>
     *    <li>+1 if this geometry's value is greater than the value for the specified geometry.</li>
     *    <li>-1 if this geometry's value is less than the value for the specified geometry.</li>
     *    <li> 0 if both geometries have the same value or value.</li>
     *  </ul>
     */
    public int compareTo(final Object object) {
        final GeometryCollection that = (GeometryCollection) object;
        if (this.value == that.value) return 0;
        if (this.value == null)       return +1;
        if (that.value == null)       return -1;
        try {
            return value.compareTo(that.value);
        } catch (ClassCastException exception) {
            // Values not comparable. Check for numbers.
            if (this.value instanceof Number && that.value instanceof Number) {
                return Double.compare(((Number)this.value).doubleValue(),
                                      ((Number)that.value).doubleValue());
            }
            // Compares their string representation instead.
            final String name1 = this.value.toString().trim();
            final String name2 = that.value.toString().trim();
            return name1.compareTo(name2);
        }
    }

    /**
     * Compares the specified object with this geometry for equality.
     * This methods checks and all coordinate points.
     */
    public synchronized boolean equals(final Object object) {
        if (object==this) {
            // Slight optimization
            return true;
        }
        if (object!=null && object.getClass().equals(getClass())) {
            final GeometryCollection that = (GeometryCollection) object;
            if (this.count==that.count && Utilities.equals(this.value, that.value)) {
                for (int i=count; --i>=0;) {
                    if (Utilities.equals(this.geometries[i], that.geometries[i])) {
                        return false;
                    }
                }
                return true;
            }
        }
        return false;
    }

    /**
     * Return a copy of this geometry. The clone has a deep copy semantic,
     * but will share many internal arrays with the original geometry.
     */
    public synchronized Object clone() {
        // Note: we can't use the 'GeometryCollection(GeometryCollection)' constructor,
        //       because the user way have subclassed this geometry.
        final GeometryCollection geometry = (GeometryCollection) super.clone();
        geometry.geometries = new Geometry[count];
        for (int i=geometry.geometries.length; --i>=0;) {
            geometry.geometries[i] = (Geometry) geometries[i].clone();
        }
        return geometry;
    }

    /**
     * Deletes all the information that was kept in an internal cache. This method can be
     * called when we know that this geometry will no longer be used before a particular time.
     * It does not cause the loss of any information, but will make subsequent uses of this
     * geometry slower (the time the internal caches take to be reconstructed, after which the
     * geometry will resume its normal speed).
     */
    final synchronized void clearCache() {
        bounds     = null;
        resolution = null;
        polylines  = null;
        for (int i=count; --i>=0;) {
            geometries[i].clearCache();
        }
        flattened = checkFlattenedShape();
        super.clearCache();
    }

    /**
     * Invoked during deserialization.
     */
    private void readObject(final ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        flattened = checkFlattenedShape(); // Reasonably fast to compute.
    }

    /**
     * Invoked during serialization.
     */
    private synchronized void writeObject(final ObjectOutputStream out) throws IOException {
        trimToSize();
        out.defaultWriteObject();
    }



    /**
     * The collection of geometries meeting a condition.
     * The check for inclusion or intersection will be performed only when first needed.
     *
     * @version $Id: GeometryCollection.java,v 1.1 2003/05/27 18:22:43 desruisseaux Exp $
     * @author Martin Desruisseaux
     */
    private static class Filtered extends AbstractCollection {
        /**
         * The geometries to check. This array <strong>must</strong> be a copy of
         * {@link GeometryCollection#geometries}. It will be changed during iteration:
         * geometries that do not obey the condition will be set to <code>null</code>.
         */
        private Geometry[] geometries;

        /**
         * Index of the next geometry to check. All geometries
         * before this index are considered valid.
         */
        private int upper;

        /**
         * Construct a filtered collection.
         *
         * @param source The source.
         */
        public Filtered(final GeometryCollection source) {
            geometries = new Geometry[source.count];
            for (int i=source.count,j=0; --i>=0; j++) {
                geometries[j] = source.geometries[i];
            }
        }

        /**
         * Returns <code>true</code> if this collection should accept the given geometry.
         * The default implementation returns always <code>true</code>.
         */
        protected boolean accept(final Geometry geometry) {
            return true;
        }

        /**
         * Returns the index of the next valid polygon starting at of after the specified
         * index. If there is no polygon left, it returns a number greater than or equal to
         * <code>geometries.length</code>. This method should be invoked with increasing
         * value of <code>from</code> only (values in random order are not supported).
         */
        private int next(int from) {
            while (from < geometries.length) {
                Geometry polygon = geometries[from];
                if (polygon != null) {
                    if (from >= upper) {
                        // This polygon has not been checked yet for validity.
                        upper = from+1;
                        if (!accept(polygon)) {
                            geometries[from] = null;
                            continue;
                        }
                        polygon = (Geometry) polygon.clone();
                        polygon.setRenderingResolution(0);
                        geometries[from] = polygon;
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
            int n = 0;
            for (int i=next(0); i<geometries.length; i=next(i+1)) {
                n++;
            }
            return n;
        }

        /**
         * Returns an iterator over the elements in this collection.
         */
        public Iterator iterator() {
            return new Iterator() {
                /** Index of the next valid polygon. */
                private int index = Filtered.this.next(0);

                /** Check if there are more geometries. */
                public boolean hasNext() {
                    return index < geometries.length;
                }

                /** Returns the next polygon. */
                public Object next() {
                    if (index < geometries.length) {
                        final Geometry next = geometries[index];
                        index = Filtered.this.next(index+1);
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
