/*
 * Geotools 2 - OpenSource mapping toolkit
 * (C) 2003, Geotools Project Managment Committee (PMC)
 * (C) 2003, Institut de Recherche pour le Développement
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

// Miscellaneous
import java.util.Map;
import java.util.Locale;
import java.util.Collection;
import java.util.IdentityHashMap;

// Geotools dependencies
import org.geotools.cs.CoordinateSystem;
import org.geotools.ct.TransformException;
import org.geotools.renderer.style.Style;
import org.geotools.math.Statistics;


/**
 * A geometry wrapping an existing {@linkplain Geometry geometry} object with a different
 * {@linkplain Style style}. Every calls except <code>get/setStyle</code> are forwarded
 * to the wrapped geometry. Consequently, <strong>changes in this geometry will impact
 * on the wrapped geometry</strong>, and conversely.
 *
 * @version $Id: GeometryProxy.java,v 1.4 2003/05/30 18:20:52 desruisseaux Exp $
 * @author Martin Desruisseaux
 */
public class GeometryProxy extends Geometry {
    /**
     * Serial number for compatibility with previous versions.
     */
    private static final long serialVersionUID = 7024656664286763717L;

    /**
     * The wrapped geometry object.
     */
    private Geometry geometry;

    /**
     * Construct a geographic shape wrapping the given geometry. The new geometry will
     * initially shares the same style than the given geometry.
     *
     * @param geometry The geometry to wrap.
     */
    public GeometryProxy(Geometry geometry) {
        super(geometry);
        while (geometry instanceof GeometryProxy) {
            geometry = ((GeometryProxy) geometry).geometry;
        }
        this.geometry = geometry;
    }

    /**
     * Returns the localized name for this geometry, or <code>null</code> if none.
     * This method forwards the call to the wrapped geometry.
     *
     * @param  locale The desired locale. If no name is available
     *         for this locale, a default locale will be used.
     * @return The geometry's name, localized if possible.
     */
    public String getName(final Locale locale) {
        return geometry.getName(locale);
    }

    /**
     * Returns the geometry's coordinate system, or <code>null</code> if unknow.
     * This method forwards the call to the wrapped geometry.
     */
    public CoordinateSystem getCoordinateSystem() {
        return geometry.getCoordinateSystem();
    }

    /**
     * Set the geometry's coordinate system.
     * It will changes the coordinate system of the wrapped geometry.
     */
    public void setCoordinateSystem(final CoordinateSystem coordinateSystem)
            throws TransformException
    {
        geometry.setCoordinateSystem(coordinateSystem);
    }

    /**
     * Determines whetever this geometry is empty.
     * This method forwards the call to the wrapped geometry.
     */
    public boolean isEmpty() {
        return geometry.isEmpty();
    }

    /**
     * Add to the specified collection all {@link Polyline} objects making the wrapped geometry.
     */
    void getPolylines(final Collection polylines) {
        geometry.getPolylines(polylines);
    }

    /**
     * Return the number of points in this geometry.
     * This method forwards the call to the wrapped geometry.
     */
    public int getPointCount() {
        return geometry.getPointCount();
    }

    /**
     * Returns an estimation of memory usage in bytes.
     * This method forwards the call to the wrapped geometry.
     */
    long getMemoryUsage() {
        return geometry.getMemoryUsage() + 4;
    }

    /**
     * Returns the smallest bounding box containing {@link #getBounds2D}.
     * This method forwards the call to the wrapped geometry.
     *
     * @deprecated This method is required by the {@link Shape} interface,
     *             but it doesn't provide enough precision for most cases.
     *             Use {@link #getBounds2D()} instead.
     */
    public Rectangle getBounds() {
        return geometry.getBounds();
    }

    /**
     * Returns the bounding box of this geometry.
     * This method forwards the call to the wrapped geometry.
     */
    public Rectangle2D getBounds2D() {
        return geometry.getBounds2D();
    }

    /**
     * Tests if the specified coordinates are inside the boundary of this geometry.
     * This method forwards the call to the wrapped geometry.
     */
    public boolean contains(final double x, final double y) {
        return geometry.contains(x, y);
    }

    /**
     * Tests if a specified {@link Point2D} is inside the boundary of this geometry.
     * This method forwards the call to the wrapped geometry.
     */
    public boolean contains(final Point2D point) {
        return geometry.contains(point);
    }

    /**
     * Test if the interior of this geometry entirely contains the given rectangle.
     * This method forwards the call to the wrapped geometry.
     */
    public boolean contains(double x, double y, double width, double height) {
        return geometry.contains(x, y, width, height);
    }

    /**
     * Tests if the interior of this geometry entirely contains the given rectangle.
     * This method forwards the call to the wrapped geometry.
     */
    public boolean contains(final Rectangle2D rectangle) {
        return geometry.contains(rectangle);
    }

    /**
     * Test if the interior of this geometry entirely contains the given shape.
     * This method forwards the call to the wrapped geometry.
     */
    public boolean contains(final Shape shape) {
        return geometry.contains(shape);
    }

    /**
     * Tests if the interior of the geometry intersects the interior of a specified rectangle.
     * This method forwards the call to the wrapped geometry.
     */
    public boolean intersects(double x, double y, double width, double height) {
        return geometry.intersects(x, y, width, height);
    }

    /**
     * Tests if the interior of the geometry intersects the interior of a specified rectangle.
     * This method forwards the call to the wrapped geometry.
     */
    public boolean intersects(final Rectangle2D rectangle) {
        return geometry.intersects(rectangle);
    }

    /**
     * Tests if the interior of the geometry intersects the interior of a specified shape.
     * This method forwards the call to the wrapped geometry.
     */
    public boolean intersects(final Shape shape) {
        return geometry.intersects(shape);
    }

    /**
     * Returns an geometry approximately equal to this geometry clipped to the specified
     * bounds. This method clip the wrapped geometry, and wrap the result in a new
     * <code>GeometryProxy</code> instance with the same {@linkplain Style style} than
     * the current one.
     */
    public Geometry clip(final Clipper clipper) {
        Geometry clipped = geometry.clip(clipper);
        if (clipped == geometry) {
            return this;
        }
        if (clipped != null) {
            clipped = new GeometryProxy(clipped);
            clipped.setStyle(getStyle());
        }
        return clipped;
    }

    /**
     * Compress the wrapped geometry.
     *
     * @param  level The compression level (or algorithm) to use.
     * @return A <em>estimation</em> of the compression rate.
     * @throws TransformException If an error has come up during a cartographic projection.
     */
    public float compress(final CompressionLevel level) throws TransformException {
        return geometry.compress(level);
    }

    /**
     * Returns the geometry's resolution.
     * This method forwards the call to the wrapped geometry.
     *
     * @return Statistics about the resolution, or <code>null</code>
     *         if this geometry doesn't contains any point.
     */
    public Statistics getResolution() {
        return geometry.getResolution();
    }

    /**
     * Set the geometry's resolution. It will changes the resolution of the wrapped geometry.
     *
     * @param  resolution Desired resolution, in the same linear units than {@link #getResolution}.
     * @throws TransformException If some coordinate transformations were needed and failed.
     *         There is no guaranteed on geometry's state in case of failure.
     */
    public void setResolution(final double resolution) throws TransformException {
        geometry.setResolution(resolution);
    }

    /**
     * Returns the rendering resolution.
     * This method forwards the call to the wrapped geometry.
     *
     * @return The rendering resolution in units of this geometry's {@linkplain #getCoordinateSystem
     *         coordinate system} (linear or angular units), or 0 if the finest available
     *         resolution should be used.
     */
    public float getRenderingResolution() {
        return geometry.getRenderingResolution();
    }

    /**
     * Hints this geometry that the specified resolution is sufficient for rendering.
     * It will changes the rendering resolution of the wrapped geometry.
     *
     * @param resolution The resolution to use at rendering time, in units of this geometry's
     *        {@linkplain #getCoordinateSystem coordinate system} (linear or angular units).
     */
    public void setRenderingResolution(float resolution) {
        geometry.setRenderingResolution(resolution);
    }

    /**
     * Returns an iterator object that iterates along the shape boundary and provides access to
     * the geometry of the shape outline. This method forwards the call to the wrapped geometry.
     */
    public PathIterator getPathIterator(final AffineTransform transform) {
        return geometry.getPathIterator(transform);
    }

    /**
     * Returns a flattened path iterator for this geometry.
     * This method forwards the call to the wrapped geometry.
     */
    public PathIterator getPathIterator(final AffineTransform transform, final double flatness) {
        return geometry.getPathIterator(transform, flatness);
    }

    /**
     * Returns <code>true</code> if {@link #getPathIterator} returns a flattened iterator.
     * This method forwards the call to the wrapped geometry.
     */
    boolean isFlattenedShape() {
        return geometry.isFlattenedShape();
    }

    /**
     * Deletes all the information that was kept in an internal cache.
     * This method forwards the call to the wrapped geometry.
     */
    void clearCache() {
        geometry.clearCache();
    }

    /**
     * Freeze the wrapped geometry.
     */
    final void freeze() {
        geometry.freeze();
    }

    /**
     * Returns <code>true</code> if we are not allowed to change this geometry.
     * This method forwards the call to the wrapped geometry.
     */
    final boolean isFrozen() {
        return geometry.isFrozen();
    }

    /**
     * Return a clone of this geometry. The returned geometry will have a deep copy semantic.
     * This method is <code>final</code> for implementation reason.
     */
    public final Object clone() {
        /*
         * This <code>clone()</code> method needs to be final because user's implementation would be
         * ignored, since we override <code>clone(Map)</code> in a way which do not call this method
         * anymore. It have to call <code>super.clone()</code> instead.
         */
        return clone(new IdentityHashMap());
    }

    /**
     * Clone this geometry, trying to avoid cloning twice the wrapped geometry.
     */
    Object clone(final Map alreadyCloned) {
        final GeometryProxy copy = (GeometryProxy) super.clone();
        copy.geometry = (Geometry) geometry.resolveClone(alreadyCloned);
        return copy;
    }

    /**
     * Compares the specified object with this geometry for equality.
     */
    public boolean equals(final Object object) {
        if (object==this) {
            // Slight optimization
            return true;
        }
        if (super.equals(object)) {
            return geometry.equals(((GeometryProxy)object).geometry);
        }
        return false;
    }

    /**
     * Returns a hash value for this geometry.
     */
    public int hashCode() {
        return geometry.hashCode() ^ (int)serialVersionUID;
    }
}
