/*
 * Geotools - OpenSource mapping toolkit
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
package org.geotools.renderer;

// Java2D Geometry
import java.awt.Shape;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

// Formatting and logging
import java.text.Format;
import java.text.NumberFormat;
import java.text.FieldPosition;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.LogRecord;

// Miscellaneous
import java.util.Locale;
import java.io.Serializable;

// Geotools dependencies
import org.geotools.pt.Latitude;
import org.geotools.pt.Longitude;
import org.geotools.pt.AngleFormat;
import org.geotools.cs.Ellipsoid;
import org.geotools.cs.CoordinateSystem;
import org.geotools.cs.ProjectedCoordinateSystem;
import org.geotools.cs.GeographicCoordinateSystem;
import org.geotools.ct.CoordinateTransformationFactory;
import org.geotools.ct.CannotCreateTransformException;
import org.geotools.ct.CoordinateTransformation;
import org.geotools.ct.TransformException;
import org.geotools.resources.Utilities;


/**
 * A {@linkplain Shape geometric shape} with a {@link CoordinateSystem coordinate system}. Those
 * <cite>geographic</cite> shapes may be a single polygon ({@link Polygon}) or a set of polygons at
 * the same altitude value ({@link Isoline}). This class implements the {@link Shape} interface for
 * interoperability with <A HREF="http://java.sun.com/products/java-media/2D/">Java2D</A>. But it
 * provides also some more capabilities. For example, <code>contains</code> and
 * <code>intersects</code> methods accepts arbitrary shapes instead of rectangles only.
 * <code>GeoShape</code> objects can have arbitrary two-dimensional coordinate systems,
 * which can be changed dynamically (i.e. the shapes can be reprojected). Futhermore,
 * <code>GeoShape</code>s can compress and share their data in order to reduce memory footprint.
 *
 * @version $Id: GeoShape.java,v 1.3 2003/01/30 23:34:39 desruisseaux Exp $
 * @author Martin Desruisseaux
 */
public abstract class GeoShape implements Shape, Cloneable, Serializable {
    /**
     * Numéro de version pour compatibilité avec des
     * bathymétries enregistrées sous d'anciennes versions.
     */
    private static final long serialVersionUID = 1447796611034908887L;

    /**
     * The logger for the renderer module.
     */
    static final Logger LOGGER = Logger.getLogger("org.geotools.renderer");

    /**
     * Nom de cette forme. Il s'agit en général d'un nom géographique, par exemple
     * "Île d'Anticosti" ou "Lac Supérieur". Ce champs peut être nul si cet objet
     * ne porte pas de nom.
     */
    private String name;

    /**
     * Construct an empty geographic shape.
     */
    public GeoShape() {
    }

    /**
     * Construct a geographic shape with the same data than the specified shape.
     *
     * @param shape The shape to copy data.
     */
    public GeoShape(final GeoShape shape) {
        this.name = shape.name;
    }

    /**
     * Set a default name for this shape. For example, a polygon
     * may have the name of a lake or an island. This name may be
     * <code>null</code> if this shape is unnamed.
     */
    public void setName(final String name) {
        this.name = name;
    }

    /**
     * Returns the localized name for this shape. The default
     * implementation ignore the locale and returns the last name
     * set by {@link #setName}.
     *
     * @param  locale The desired locale. If no name is available
     *         for this locale, a default locale will be used.
     * @return The shape's name, localized if possible.
     */
    public String getName(final Locale locale) {
        return name;
    }

    /**
     * Returns the shape's coordinate system, or <code>null</code> if unknow.
     */
    public abstract CoordinateSystem getCoordinateSystem();

    /**
     * Set the shape's coordinate system. Calling this method is equivalents
     * to reproject all shape's points from the old coordinate system to the
     * new one.
     *
     * @param  The new coordinate system. A <code>null</code> value reset the default
     *         coordinate system (usually the one that best fits internal data).
     * @throws TransformException If a transformation failed. In case of failure,
     *         the state of this object will stay unchanged, as if this method has
     *         never been invoked.
     */
    public abstract void setCoordinateSystem(final CoordinateSystem coordinateSystem)
            throws TransformException;

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
            Utilities.unexpectedException("org.geotools.renderer", "GeoShape",
                                          "getIdentityTransform", exception);
        }
        return null;
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
     * Determines whetever this shape is empty.
     */
    public abstract boolean isEmpty();

    /**
     * Return the bounding box of this shape. This methode returns
     * a direct reference to the internally cached bounding box.
     * DO NOT MODIFY!
     */
    Rectangle2D getCachedBounds() {
        return getBounds2D(); // To be overriden by subclasses.
    }

    /**
     * Test if the interior of this shape entirely contains the given rectangle.
     * The rectangle's coordinates must expressed in this shape's coordinate
     * system (as returned by {@link #getCoordinateSystem}).
     */
    public boolean contains(double x, double y, double width, double height) {
        return contains(new Rectangle2D.Double(x, y, width, height));
    }

    /**
     * Test if the interior of this shape entirely contains the given shape.
     * The coordinate system for the specified <code>shape</code> argument
     * must be the same than this <code>GeoShape</code> object, as returned
     * by {@link #getCoordinateSystem}.
     */
    public abstract boolean contains(final Shape shape);

    /**
     * Tests if the interior of the shape intersects the interior of a specified rectangle.
     * The rectangle's coordinates must expressed in this shape's coordinate
     * system (as returned by {@link #getCoordinateSystem}).
     */
    public boolean intersects(double x, double y, double width, double height) {
        return intersects(new Rectangle2D.Double(x, y, width, height));
    }

    /**
     * Tests if the interior of the shape intersects the interior of a specified shape.
     * The coordinate system for the specified <code>shape</code> argument
     * must be the same than this <code>GeoShape</code> object, as returned
     * by {@link #getCoordinateSystem}.
     */
    public abstract boolean intersects(final Shape shape);

    /**
     * Returns the string to be used as the tooltip for the given location.
     * If there is no such tooltip, returns <code>null</code>. This method
     * is usually invoked as result of mouse events. Default implementation
     * returns {@link #getName} if the specified coordinates is contained
     * inside this shape, or <code>null</code> otherwise.
     *
     * @param  point Coordinates (usually mouse coordinates). Must be
     *         specified in this shape's coordinate system (as returned
     *         by {@link #getCoordinateSystem}).
     * @param  locale The desired locale for the tool tips.
     * @return The tooltip text for the given location, or <code>null</code>
     *         if there is none.
     */
    public String getToolTipText(final Point2D point, final Locale locale) {
        final String name = getName(locale);
        return (name!=null && contains(point)) ? name : null;
    }

    /**
     * Return the number of points in this shape.
     */
    public abstract int getPointCount();

    /**
     * Returns the shape's mean resolution. This resolution is the mean distance between
     * every pair of consecutive points in this shape  (ignoring "extra" points used for
     * drawing a border, if there is one). This method try to returns linear units (usually
     * meters) no matter if the coordinate systems is actually a {@link ProjectedCoordinateSystem}
     * or a {@link GeographicCoordinateSystem}. More specifically:
     * <ul>
     *   <li>If the coordinate system is a {@linkplain GeographicCoordinateSystem geographic}
     *       one, then the resolution is expressed in units of the underlying
     *       {@linkplain Ellipsoid#getAxisUnit ellipsoid's axis length}.</li>
     *   <li>Otherwise (especially if the coordinate system is a {@linkplain
     *       ProjectedCoordinateSystem projected} one), the resolution is expressed in
     *       {@linkplain ProjectedCoordinateSystem#getUnits units of the coordinate system}.</li>
     * </ul>
     *
     * @return The mean resolution, or {@link Float#NaN} if this shape doesn't have any point.
     */
    public abstract float getResolution();

    /**
     * Set the shape's resolution. This method try to interpolate new points in such a way
     * that every point is spaced by exactly <code>resolution</code> units (usually meters)
     * from the previous one. Calling this method with a lower resolution may help to reduce
     * memory footprint if a high resolution is not needed (note that {@link Isoline#compress}
     * provides an alternative way to reduce memory footprint).
     * <br><br>
     * This method is irreversible. Invoking <code>setResolution</code> with a finner
     * resolution will increase memory consumption with no real resolution improvement.
     *
     * @param  resolution Desired resolution, in the same units than {@link #getResolution}.
     * @throws TransformException If some coordinate transformations were needed and failed.
     *         There is no guaranteed on shape's state in case of failure.
     */
    public abstract void setResolution(final double resolution) throws TransformException;

    /**
     * Returns a shape approximatively equals to this shape clipped to the specified bounds.
     * The clip is only approximative in that the resulting shape may extends outside the clip
     * area. However, it is garanted that the resulting shape contains at least all the interior
     * of the clip area.
     *
     * If this method can't performs the clip, or if it believe that it doesn't worth to do a clip,
     * it returns <code>this</code>. If this shape doesn't intersect the clip area, then this method
     * returns <code>null</code>. Otherwise, a new shape is created and returned. The new shape
     * will try to share as much internal data as possible with <code>this</code> in order to keep
     * memory footprint low.
     *
     * @param  clipper An object containing the clip area.
     * @return <code>null</code> if this shape doesn't intersect the clip, <code>this</code>
     *         if no clip has been performed, or a new clipped shape otherwise.
     */
//    GeoShape getClipped(final Clipper clipper) {
//        return this;
//    }

    /**
     * Return a string representation of this shape for debugging purpose.
     * The returned string will look like
     * "<code>Polygon["Île Quelconque", 44°30'N-51°59'N  70°59'W-54°59'W (56 pts)]</code>".
     */
    public String toString() {
        final Format format;
        final Rectangle2D bounds = getBounds2D();
        Object minX,minY,maxX,maxY;
        if (getCoordinateSystem() instanceof GeographicCoordinateSystem) {
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
        format.format(minY, buffer, dummy).append('-' );
        format.format(maxY, buffer, dummy).append("  ");
        format.format(minX, buffer, dummy).append('-' );
        format.format(maxX, buffer, dummy).append(" (");
        buffer.append(getPointCount()); buffer.append(" pts)]");
        return buffer.toString();
    }

    /**
     * Returns an hash code for this shape. Subclasses should
     * overrides this method to provide a more appropriate value.
     */
    public int hashCode() {
        return (name!=null) ? name.hashCode() : 0;
    }

    /**
     * Compare the specified object with this shape for equality.
     * Default implementation tests if the two objects are instances
     * of the same class and compare their name. Subclasses should
     * overrides this method for checking shape's points.
     */
    public boolean equals(final Object object) {
        if (object!=null && object.getClass().equals(getClass())) {
            return Utilities.equals(name, ((GeoShape) object).name);
        }
        return false;
    }

    /**
     * Return a clone of this shape. The returned shape will have
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
}
