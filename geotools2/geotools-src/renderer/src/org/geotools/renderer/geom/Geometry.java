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

// Java2D Geometry
import java.awt.Shape;
import java.awt.Rectangle;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.PathIterator;
import java.awt.geom.AffineTransform;
import java.awt.geom.FlatteningPathIterator;

// Formatting and logging
import java.text.Format;
import java.text.NumberFormat;
import java.text.FieldPosition;
import java.util.Collection;
import java.util.logging.Logger;

// Miscellaneous
import java.util.Map;
import java.util.Locale;
import java.io.Serializable;

// Geotools dependencies
import org.geotools.units.Unit; // For Javadoc
import org.geotools.pt.Latitude;
import org.geotools.pt.Longitude;
import org.geotools.pt.AngleFormat;
import org.geotools.cs.CoordinateSystem;
import org.geotools.cs.LocalCoordinateSystem;
import org.geotools.cs.GeographicCoordinateSystem;
import org.geotools.ct.CoordinateTransformationFactory;
import org.geotools.ct.CannotCreateTransformException;
import org.geotools.ct.CoordinateTransformation;
import org.geotools.ct.TransformException;
import org.geotools.feature.Feature; // For Javadoc
import org.geotools.renderer.style.Style;
import org.geotools.resources.XRectangle2D;
import org.geotools.resources.Utilities;
import org.geotools.math.Statistics;
import org.geotools.util.Cloneable;


/**
 * Base class for {@linkplain Shape geometric shape} to be rendered in a given
 * {@link CoordinateSystem coordinate system}.  Those classes are not designed
 * for spatial analysis or topology: they are <strong>not</strong> a replacement
 * for <A HREF="http://www.vividsolutions.com/JTS/jts_frame.htm">JTS</A>, neither
 * an implementation of ISO-19107. They are rather a wrapper around arbitrary source
 * of (<var>x</var>,<var>y</var>) coordinates to be rendered. With the rendering goal
 * in mind, this class implements the {@link Shape} interface for interoperability with
 * <A HREF="http://java.sun.com/products/java-media/2D/">Java2D</A>. But it provides also
 * some more capabilities. For example, <code>contains</code> and <code>intersects</code>
 * methods accepts arbitrary shapes instead of rectangle only. <code>Geometry</code> objects
 * can have arbitrary two-dimensional coordinate system, which can be
 * {@linkplain #setCoordinateSystem changed at any time} (i.e. the geometry can be reprojected).
 * {@linkplain #setRenderingResolution Decimation} can be applied at rendering time. Futhermore,
 * <code>Geometry</code>s can {@linkplain #compress compress} and share their internal data in
 * order to reduce memory footprint.
 *
 * @version $Id: Geometry.java,v 1.15 2004/05/10 22:21:57 desruisseaux Exp $
 * @author Martin Desruisseaux
 */
public abstract class Geometry implements Shape, Cloneable, Serializable {
    /**
     * Serial number for compatibility with previous versions.
     */
    private static final long serialVersionUID = -1274472236517648668L;

    /**
     * The logger for the renderer module.
     */
    static final Logger LOGGER = Logger.getLogger("org.geotools.renderer.geom");

    /**
     * The default coordinate system for all geometries. This is the coordinate
     * system used if no CS were explicitly specified at construction time. The
     * default implementation uses a two-dimensional cartesian coordinate system
     * with {@linkplain AxisInfo#X x},{@linkplain AxisInfo#Y y} axis in
     * {@linkplain Unit#METRE metres}. This coordinate system is treated specially
     * by the default {@linkplain org.geotools.ct.CoordinateTransformationFactory
     * coordinate transformation factory} with loose transformation rules: if no
     * transformation path were found, then the transformation from this CS to any
     * CS with a compatible number of dimensions is assumed to be the identity transform.
     *
     * @see LocalCoordinateSystem#PROMISCUOUS
     * @see LocalCoordinateSystem#CARTESIAN
     * @see GeographicCoordinateSystem#WGS84
     */
    public static final CoordinateSystem DEFAULT_COORDINATE_SYSTEM = LocalCoordinateSystem.PROMISCUOUS;

    /**
     * A user object. This is often the {@linkplain Feature feature} where this geometry
     * come from, or an ID for this feature as a {@linkplain String character string}.
     * This information is stored here in order to allow faster retrieval of the
     * {@linkplain Feature feature} under the mouse cursor.
     */
    private Object userObject;

    /**
     * The resolved style for this geometry, or <code>null</code> if none.
     */
    private Style style;
    
    /**
     * A generic identifier for this geometry.
     */
    private String ID;

    /**
     * Construct an empty geographic shape.
     */
    public Geometry() {
    }

    /**
     * Construct a geographic shape with the same data than the specified geometry.
     *
     * @param geometry The geometry to copy data from.
     */
    protected Geometry(final Geometry geometry) {
        this.style = geometry.style;
    }

    /**
     * Returns the localized name for this geometry, or <code>null</code> if none.
     * The default implementation returns always <code>null</code>.
     *
     * @param  locale The desired locale. If no name is available
     *         for this locale, a default locale will be used.
     * @return The geometry's name, localized if possible.
     */
    public String getName(final Locale locale) {
        return null;
    }
    
    /**
     * Returns the geometry ID. The ID is string that identifies the current
     * geometry and its application specific, that is, the renderer won't use it
     * in any special way.
     *
     * @return The geometry ID, or <code>null</code> if none.
     */
    public String getID() {
        return ID;
    }

    /**
     * Sets the geometry ID.
     *
     * @param The geometry ID.
     */
    public void setID(final String ID) {
        this.ID = ID;
    }

    /**
     * Returns the user object attached to this geometry. This is often the
     * {@linkplain Feature feature} where this geometry come from, or an ID
     * for this feature as a {@linkplain String character string}.
     */
    public Object getUserObject() {
    	return userObject;
    }

    /**
     * Set the user object for this geometry. This is often the {@linkplain Feature feature}
     * where this geometry come from, or an ID for this geometry as a {@linkplain String
     * character string}. The renderer will not use this information in any way. It is stored
     * here mostly for faster retrieval of the {@linkplain Feature feature} under the mouse cursor.
     */
    public void setUserObject(final Object userObject) {
    	this.userObject = userObject;
    }

    /**
     * Returns the style attached to this geometry, or <code>null</code> if none.
     */
    public Style getStyle() {
        return style;
    }

    /**
     * Set the style attached to this geometry.
     *
     * @param style The new style for this geometry, or <code>null</code> if none.
     */
    public void setStyle(final Style style) {
        this.style = style;
    }

    /**
     * Returns the geometry's coordinate system, or <code>null</code> if unknow.
     */
    public abstract CoordinateSystem getCoordinateSystem();

    /**
     * Set the geometry's coordinate system. Calling this method is equivalents
     * to reproject all geometry's points from the old coordinate system to the
     * new one.
     *
     * @param  coordinateSystem The new coordinate system. A <code>null</code> value reset
     *         the default coordinate system (usually the one that best fits internal data).
     * @throws TransformException If a transformation failed. In case of failure,
     *         the state of this object will stay unchanged, as if this method has
     *         never been invoked.
     * @throws UnmodifiableGeometryException if modifying this geometry would corrupt a container.
     *         To avoid this exception, {@linkplain #clone clone} this geometry before to modify it.
     */
    public abstract void setCoordinateSystem(final CoordinateSystem coordinateSystem)
            throws TransformException, UnmodifiableGeometryException;

    /**
     * Check if two coordinate system are equivalents, ignoring attributes like the CS name.
     */
    static boolean equivalents(final CoordinateSystem cs1, final CoordinateSystem cs2) {
        if (cs1 == cs2) return true;
        return cs1!=null && cs1.equals(cs2, false);
    }

    /**
     * Construct a transform from two coordinate systems.
     *
     * @param  sourceCS The source coordinate system.
     * @param  targetCS The target coordinate system.
     * @return A transformation from <code>sourceCS</code> to <code>targetCS</code>.
     */
    static CoordinateTransformation getCoordinateTransformation(final CoordinateSystem sourceCS,
                                                                final CoordinateSystem targetCS)
            throws CannotCreateTransformException
    {
        return CoordinateTransformationFactory.getDefault()
                    .createFromCoordinateSystems(sourceCS, targetCS);
    }

    /**
     * Retourne une transformation identitée pour le système de coordonnées
     * spécifié, ou <code>null</code> si <code>coordinateSystem</code> est nul.
     *
     * @param  coordinateSystem The coordinate system, or <code>null</code>.
     * @return An identity transformation from and to <code>coordinateSystem</code>,
     *         or <code>null</code>.
     */
    static CoordinateTransformation getIdentityTransform(final CoordinateSystem coordinateSystem) {
        if (coordinateSystem != null) try {
            return getCoordinateTransformation(coordinateSystem, coordinateSystem);
        } catch (CannotCreateTransformException exception) {
            // Should not happen; we are just asking for an identity transform!
            Utilities.unexpectedException("org.geotools.renderer.geom", "Geometry",
                                          "getIdentityTransform", exception);
        }
        return null;
    }

    /**
     * Determines whetever this geometry is empty.
     */
    public boolean isEmpty() {
        // To be overriden by subclasses with a more efficient implementation.
        return getPointCount() == 0;
    }

    /**
     * Add to the specified collection all non-empty {@link Polyline} objects making this
     * geometry. This method is used by {@link GeometryCollection#getPathIterator} and
     * {@link PolygonAssembler} only.
     */
    void getPolylines(final Collection polylines) {
    }

    /**
     * Return the number of points in this geometry.
     */
    public abstract int getPointCount();

    /**
     * Returns an estimation of memory usage in bytes. This method is for information
     * purposes only. The memory really used by two geometries may be lower than the sum
     * of their  <code>getMemoryUsage()</code>  return values,  since geometries try to
     * share their data when possible. Furthermore, this method does not take into account
     * the extra bytes generated by Java Virtual Machine for each object.
     *
     * @return An <em>estimation</em> of memory usage in bytes.
     */
    long getMemoryUsage() {
        // To be overriden by subclasses.
        return getPointCount()*8;
    }

    /**
     * Returns the smallest bounding box containing {@link #getBounds2D}.
     *
     * @deprecated This method is required by the {@link Shape} interface,
     *             but it doesn't provide enough precision for most cases.
     *             Use {@link #getBounds2D()} instead.
     */
    public Rectangle getBounds() {
        final Rectangle rect = new Rectangle();
        rect.setRect(getBounds2D()); // Perform the appropriate rounding.
        return rect;
    }

    /**
     * Returns the bounding box of this geometry. The rectangle's coordinates will be expressed
     * in this geometry's coordinate system (as returned by {@link #getCoordinateSystem}).
     *
     * @return The bounding box of this geometry.
     */
    public abstract Rectangle2D getBounds2D();

    /**
     * Tests if the specified coordinates are inside the boundary of this geometry.
     *
     * @param  x the specified <var>x</var> coordinates in this geometry coordinate system.
     * @param  y the specified <var>y</var> coordinates in this geometry coordinate system.
     * @return <code>true</code> if the specified coordinates are inside 
     *         the geometry boundary; <code>false</code> otherwise.
     */
    public boolean contains(double x, double y) {
        // To be overriden by subclasses with a more efficient implementation.
        return contains(new Point2D.Double(x,y));
    }

    /**
     * Tests if a specified {@link Point2D} is inside the boundary of this geometry.
     *
     * @param  point the specified point in this geometry coordinate system.
     * @return <code>true</code> if the specified point is inside 
     *         the geometry boundary; <code>false</code> otherwise.
     */
    public abstract boolean contains(Point2D point);

    /**
     * Test if the interior of this geometry entirely contains the given rectangle.
     * The rectangle's coordinates must expressed in this geometry's coordinate
     * system (as returned by {@link #getCoordinateSystem}).
     */
    public boolean contains(double x, double y, double width, double height) {
        return contains(new Rectangle2D.Double(x, y, width, height));
    }

    /**
     * Tests if the interior of this geometry entirely contains the given rectangle.
     * The rectangle's coordinates must expressed in this geometry's coordinate
     * system (as returned by {@link #getCoordinateSystem}).
     */
    public boolean contains(final Rectangle2D rectangle) {
        // To be overriden by subclasses with a more efficient implementation.
        return contains((Shape)rectangle);
    }

    /**
     * Test if the interior of this geometry entirely contains the given shape.
     * The coordinate system for the specified <code>shape</code> argument
     * must be the same than this <code>Geometry</code> object, as returned
     * by {@link #getCoordinateSystem}.
     */
    public abstract boolean contains(final Shape shape);

    /**
     * Tests if the interior of the geometry intersects the interior of a specified rectangle.
     * The rectangle's coordinates must expressed in this geometry's coordinate
     * system (as returned by {@link #getCoordinateSystem}).
     */
    public boolean intersects(double x, double y, double width, double height) {
        return intersects(new Rectangle2D.Double(x, y, width, height));
    }

    /**
     * Tests if the interior of the geometry intersects the interior of a specified rectangle.
     * The rectangle's coordinates must expressed in this geometry's coordinate
     * system (as returned by {@link #getCoordinateSystem}).
     */
    public boolean intersects(final Rectangle2D rectangle) {
        // To be overriden by subclasses with a more efficient implementation.
        return intersects((Shape)rectangle);
    }

    /**
     * Tests if the interior of the geometry intersects the interior of a specified shape.
     * The coordinate system for the specified <code>shape</code> argument
     * must be the same than this <code>Geometry</code> object, as returned
     * by {@link #getCoordinateSystem}.
     */
    public abstract boolean intersects(final Shape shape);

    /**
     * Returns an geometry approximately equal to this geometry clipped to the specified bounds.
     * The clip is only approximate in that the resulting geometry may extend outside the clip
     * area. However, it is guaranteed that the returned geometry contains at least all the
     * interior of the clip area.
     *
     * If this method can't perform the clip, or if it believes that it isn't worth doing a clip,
     * it returns <code>this</code>. If this geometry doesn't intersect the clip area, then this
     * method returns <code>null</code>. Otherwise, a new geometry is created and returned. The new
     * geometry will try to share as much internal data as possible with <code>this</code> in order
     * to keep memory footprint low.
     *
     * @param  clipper The clipping area.
     * @return <code>null</code> if this geometry doesn't intersect the clip, <code>this</code>
     *         if no clip has been performed, or a new clipped geometry otherwise.
     */
    public Geometry clip(final Clipper clipper) {
        // Subclasses will overrides this method with a more efficient implementation.
        if (equivalents(clipper.mapCS, getCoordinateSystem())) {
            return XRectangle2D.intersectInclusive(clipper.mapClip, getBounds2D()) ? this : null;
        }
        return this;
    }

    /**
     * Compress this geometry. The <code>level</code> argument specify the algorithm,
     * which may be desctructive (i.e. data may loose precision). Compressing geometry
     * may help to reduce memory usage, providing that there is no reference to the
     * (<var>x</var>,<var>y</var>) coordinate points outside this geometry (otherwise
     * the garbage collector will not reclaim the old data).
     *
     * @param  level The compression level (or algorithm) to use. See the {@link CompressionLevel}
     *         javadoc for an explanation of available algorithms.
     * @return A <em>estimation</em> of the compression rate. For example a value of 0.2
     *         means that the new geometry use <em>approximatively</em> 20% less memory.
     *         Warning: this value may be inacurate, for example if the old geometry was
     *         used to shares its data with an other geometry, compressing one geometry
     *         may actually increase memory usage since the two geometries will no longer
     *         share their data.
     * @throws TransformException If an error has come up during a cartographic projection.
     * @throws UnmodifiableGeometryException if modifying this geometry would corrupt a container.
     *         To avoid this exception, {@linkplain #clone clone} this geometry before to modify it.
     */
    public abstract float compress(final CompressionLevel level)
            throws TransformException, UnmodifiableGeometryException;

    /**
     * Returns the geometry's resolution. The mean resolution is the mean distance between
     * every pair of consecutive points in this geometry (ignoring "extra" points used for
     * drawing a border, if there is one). This method try to express the resolution in
     * linear units (usually meters) no matter if the coordinate systems is actually a
     * {@linkplain ProjectedCoordinateSystem projected} or a
     * {@linkplain GeographicCoordinateSystem geographic} one.
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
     * @return Statistics about the resolution, or <code>null</code> if this geometry doesn't
     *         contains any point. If non-null, the statistics object contains
     *         {@linkplain Statistics#minimum minimum},
     *         {@linkplain Statistics#maximum maximum},
     *         {@linkplain Statistics#mean mean},
     *         {@linkplain Statistics#rms root mean square} and
     *         {@linkplain Statistics#standardDeviation standard deviation}
     *         always in linear units.
     */
    public abstract Statistics getResolution();

    /**
     * Set the geometry's resolution. This method interpolates new points in such a way
     * that every point is spaced by exactly <code>resolution</code> units (usually meters)
     * from the previous one. Consequently, the {@linkplain #getResolution resolution} after
     * this call will have a {@linkplain Statistics#standardDeviation standard deviation}
     * close to 0.
     * <br><br>
     * Calling this method with a large resolution may help to reduce memory footprint if
     * a fine resolution is not needed (note that {@link #compress} provides an alternative
     * way to reduce memory footprint).
     *
     * This method is irreversible. Invoking <code>setResolution</code> with a finner
     * resolution will increase memory consumption with no real resolution improvement.
     *
     * @param  resolution Desired resolution, in the same linear units than {@link #getResolution}.
     * @throws TransformException If some coordinate transformations were needed and failed.
     *         There is no guaranteed on geometry's state in case of failure.
     * @throws UnmodifiableGeometryException if modifying this geometry would corrupt a container.
     *         To avoid this exception, {@linkplain #clone clone} this geometry before to modify it.
     */
    public abstract void setResolution(final double resolution)
            throws TransformException, UnmodifiableGeometryException;

    /**
     * Returns the rendering resolution. This is the spatial resolution used by
     * {@link PathIterator} only; it has no effect on the underyling data. Note
     * that at the difference of {@link #getResolution}, the units are not always
     * linear; they may be angular if the underlying coordinate system is {@linkplain
     * GeographicCoordinateSystem geographic}. Resolution in angular units is not very
     * meaningful for computation purpose (since the length of longitude degrees vary
     * with latitude), but is what the user see if the map is unprojected.
     * The <em>rendering</em> resolution is about what the user see.
     *
     * @return The rendering resolution in units of this geometry's {@linkplain #getCoordinateSystem
     *         coordinate system} (linear or angular units), or 0 if the finest available
     *         resolution should be used.
     */
    public float getRenderingResolution() {
        return 0;
    }

    /**
     * Hints this geometry that the specified resolution is sufficient for rendering.
     * Value 0 ask for the best available resolution. If a value greater than 0 is provided,
     * then the {@link PathIterator} will skip as many points as it can while preserving a
     * distance equals or smaller than <code>resolution</code> between two consecutive points.
     * Note that this method affect the <code>PathIterator</code> behavior only; it has no impact
     * on the underlying data. This method is non-destructive; it is possible to set a finer
     * resolution after a large one.
     *
     * @param resolution The resolution to use at rendering time, in units of this geometry's
     *        {@linkplain #getCoordinateSystem coordinate system} (linear or angular units,
     *        see {@link #getRenderingResolution} for a discussion).
     */
    public void setRenderingResolution(float resolution) {
        // The default implementation ignore this call, since this method is just a hint.
        // Subclasses will do the real work.
    }

    /**
     * Returns an iterator object that iterates along the shape boundary and provides access to
     * the geometry of the shape outline. If an optional {@link AffineTransform} is specified,
     * the coordinates returned in the iteration are transformed accordingly. The iterator may
     * not iterate through all internal data. If a {@linkplain #getRenderingResolution rendering
     * resolution} has been specified, then some points may be skipped during the iteration.
     */
    public abstract PathIterator getPathIterator(AffineTransform transform);

    /**
     * Returns a flattened path iterator for this geometry.
     */
    public PathIterator getPathIterator(final AffineTransform transform, final double flatness) {
        PathIterator iterator = getPathIterator(transform);
        if (!isFlattenedShape()) {
            iterator = new FlatteningPathIterator(iterator, flatness);
        }
        return iterator;
    }

    /**
     * Returns <code>true</code> if {@link #getPathIterator} returns a flattened iterator.
     * In this case, there is no need to wrap it into a {@link FlatteningPathIterator}.
     */
    boolean isFlattenedShape() {
        // Will be overriden by subclasses.
        return false;
    }

    /**
     * Deletes all the information that was kept in an internal cache. This method can be
     * called when we know that this geometry will no longer be used before a long time.
     * It does not cause the loss of any information, but will make subsequent uses of this
     * geometry slower (the time the internal caches take to be reconstructed, after which the
     * geometry will resume its normal speed).
     */
    void clearCache() {
    }

    /**
     * Freeze this geometry. A frozen geometry can't change its internal data anymore.
     * Invoking methods like {@link #setCoordinateSystem setCoordinateSystem(...)} or
     * {@link #setResolution setResolution(...)} on a frozen geometry will result in a
     * {@link UnmodifiableGeometryException} to be thrown. However, the following methods
     * still allowed:
     * <br><br>
     * <ul>
     *   <li>{@link #setStyle}</li>
     *   <li>{@link #setRenderingResolution}</li>
     * </ul>
     * <br><br>
     * This is because those methods affect the way the geometry is rendered, but has no
     * negative impact on the container that own this geometry.
     *
     * A frozen geometry can never been unfrozen, because we never know if there is not
     * some {@link GeometryProxy} left which still included in the container. To modify
     * a frozen geometry, {@linkplain #clone clone} it first.
     */
    void freeze() {
        // Will be overriden by subclasses.
    }

    /**
     * Returns <code>true</code> if we are not allowed to change this geometry. Containers
     * like {@link GeometryCollection} will test this flag in order to clone a child only
     * if needed.
     */
    boolean isFrozen() {
        return true;
    }

    /**
     * Return a clone of this geometry. The returned geometry will have
     * a deep copy semantic. However, subclasses should overrides this
     * method in such a way that both shapes will share as much internal
     * arrays as possible, even if they use differents coordinate systems.
     */
    public Object clone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException exception) {
            // Should never happen, since we are cloneable.
            throw new AssertionError(exception);
        }
    }

    /**
     * Clone this geometry. This method should never been invoked directly excepted only once
     * in the final <code>clone()</code> method. Invoke {@link #resolveClone(Map)} instead.
     * The <code>alreadyCloned</code> argument should not be modified inside this method,
     * but should be passed to all invocation of {@link #resolveClone(Map)}.
     *
     * This method needs to be overriden only if the clone operation requires cloning of
     * child geometries. In such case, the {@link #clone()} method should be overriden
     * and declared final as below:
     *
     * <blockquote><pre>
     * public final Object clone() {
     *     return clone(new IdentityHashMap());
     * }
     * </pre></blockquote>
     *
     * This <code>clone()</code> method needs to be final because user's implementation would be
     * ignored, since we override <code>clone(Map)</code> in a way which do not call this method
     * anymore (it usually call <code>super.clone()</code> instead).
     */
    Object clone(final Map alreadyCloned) {
        return clone();
    }

    /**
     * Clone this geometry only if it was not already cloned. This implementation is used in order
     * to avoid duplicate clones when a {@link GeometryCollection} contains {@link GeometryProxy}.
     *
     * @param alreadyCloned Maps the original geometries with their clones.
     *        This map should be an instance of {@link java.util.IdentityHashMap}.
     */
    final Object resolveClone(final Map alreadyCloned) {
        Object copy = alreadyCloned.get(this);
        if (copy != null) {
            return copy;
        }
        copy = clone(alreadyCloned);
        alreadyCloned.put(this, copy);
        return copy;
    }

    /**
     * Compares the specified object with this geometry for equality.
     */
    public boolean equals(final Object object) {
        if (object!=null && object.getClass().equals(getClass())) {
            return Utilities.equals(style, ((Geometry)object).style);
        }
        return false;
    }

    /**
     * Returns a hash value for this geometry.
     */
    public int hashCode() {
        // To be overriden by subclass with a more efficient implementation.
        final String name = getName(null);
        return (name!=null) ? name.hashCode() : 0;
    }

    /**
     * Return a string representation of this geometry for debugging purpose.
     * The returned string will look like
     * "<code>Polygon["polygon name", 44°30'N-51°59'N  70°59'W-54°59'W (56 pts)]</code>".
     */
    public String toString() {
        final Format format;
        final Rectangle2D  bounds = getBounds2D();
        final CoordinateSystem cs = getCoordinateSystem();
        Object minX,minY,maxX,maxY;
        if (cs instanceof GeographicCoordinateSystem) {
            minX   = new Longitude(bounds.getMinX());
            minY   = new Latitude (bounds.getMinY());
            maxX   = new Longitude(bounds.getMaxX());
            maxY   = new Latitude (bounds.getMaxY());
            format = new AngleFormat();
        } else {
            minX   = new Double(bounds.getMinX());
            minY   = new Double(bounds.getMinY());
            maxX   = new Double(bounds.getMaxX());
            maxY   = new Double(bounds.getMaxY());
            format = NumberFormat.getNumberInstance();
        }
        final String         name = getName(Locale.getDefault());
        final FieldPosition dummy = new FieldPosition(0);
        final StringBuffer buffer = new StringBuffer(Utilities.getShortClassName(this));
        buffer.append('[');
        if (name != null) {
            buffer.append('"');
            buffer.append(name);
            buffer.append("\", ");
        }
        if (cs != null) {
            buffer.append("cs=\"");
            buffer.append(cs.getName(null));
            buffer.append("\", ");
        }
        buffer.append("x={");
        format.format(minY, buffer, dummy).append(" \u2026 ");
        format.format(maxY, buffer, dummy).append("}, y={");
        format.format(minX, buffer, dummy).append(" \u2026 ");
        format.format(maxX, buffer, dummy).append("} (");
        buffer.append(getPointCount()); buffer.append(" pts)");
        if (style != null) {
            buffer.append(", style=");
            buffer.append(style);
        }
        buffer.append(']');
        return buffer.toString();
    }
}
