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
package org.geotools.renderer.j2d;

// J2SE dependencies
import java.awt.geom.Point2D;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.NoninvertibleTransformException;

// Geotools dependencies
import org.geotools.pt.CoordinatePoint;
import org.geotools.cs.CoordinateSystem;
import org.geotools.ct.MathTransform;
import org.geotools.ct.MathTransform2D;
import org.geotools.ct.TransformException;
import org.geotools.pt.MismatchedDimensionException;
import org.geotools.renderer.DeformableViewer;
import org.geotools.resources.CTSUtilities;
import org.geotools.resources.Utilities;


/**
 * An event which indicates that a mouse action occurred in a map component. This event can
 * convert mouse position to geographic coordinates.  All {@link MouseListener}s registered
 * in {@link org.geotools.gui.swing.MapPane} will automatically receive events of this class.
 * Listeners implementations can implements their code as below:
 *
 * <blockquote><pre>
 * &nbsp;public void mouseClicked(MouseEvent e) {
 * &nbsp;    GeoMouseEvent event = (GeoMouseEvent) e;
 * &nbsp;    // Process event here...
 * &nbsp;}
 * </pre></blockquote>
 *
 * @version $Id: GeoMouseEvent.java,v 1.11 2004/02/17 21:11:23 desruisseaux Exp $
 * @author Martin Desruisseaux
 */
public final class GeoMouseEvent extends MouseEvent {
    /**
     * Version number for compatability with previous versions.
     */
    private static final long serialVersionUID = 2151488551541106023L;

    /**
     * A snapshot of the coordinate systems in use at the time this event were fired. Since
     * all coordinate system fields are immutables, they are guarantee to be consistent with
     * the mouse coordinates even if some {@link MouseListener} changed the renderer state.
     * This field is read by {@link MouseCoordinateFormat}.
     */
    final RenderingContext context;

    /**
     * "Real world" two-dimensional coordinate of mouse location, in the user
     * {@link #coordinateSystem}. Will be computed only when first requested.
     */
    private transient double px,py;

    /**
     * The coordinate system for ({@link #x},{@link #y}) or <code>null</code> if the
     * coordinate has not yet been computed. This coordinate system must be two-dimensional.
     */
    private transient CoordinateSystem coordinateSystem;

    /**
     * A mouseClick event which also contains methods to transform from 
     * pixels to the Coordinate System of the Renderer.
     * @param event    The original mouse event.
     * @param renderer The renderer used by the viewer that emmited the
     * <code>event</code>.
     */
    public GeoMouseEvent(final MouseEvent event, final Renderer renderer) {
        super(event.getComponent(),    // the Component that originated the event
              event.getID(),           // the integer that identifies the event
              event.getWhen(),         // a long int that gives the time the event occurred
              event.getModifiers(),    // the modifier keys down during event (shift, ctrl, alt, meta)
              event.getX(),            // the horizontal x coordinate for the mouse location
              event.getY(),            // the vertical y coordinate for the mouse location
              event.getClickCount(),   // the number of mouse clicks associated with event
              event.isPopupTrigger(),  // a boolean, true if this event is a trigger for a popup-menu
              event.getButton());      // which of the mouse buttons has changed state (JDK 1.4 only).
        context = renderer.getRenderingContext();
    }

    /**
     * Returns the mouse's position in pixel units. This method is
     * similar to {@link #getPoint} except that the mouse location
     * is corrected for deformations caused by some artifacts like the
     * {@linkplain org.geotools.gui.swing.ZoomPane#setMagnifierVisible magnifying glass}.
     *
     * @param  dest A pre-allocated point that stores the mouse's
     *              location, or <code>null</code> if none.
     * @return The mouse's location in pixel coordinates.
     */
    public Point2D getPixelCoordinate(Point2D dest) {
        if (dest != null) {
            dest.setLocation(getX(), getY());
        } else {
            dest = new Point2D.Double(getX(), getY());
        }
        final Object source = getSource();
        if (source instanceof DeformableViewer) {
            ((DeformableViewer) source).correctApparentPixelPosition(dest);
        }
        return dest;
    }

    /**
     * Returns the "real world" mouse's position. The coordinates are expressed in
     * {@linkplain Renderer#getCoordinateSystem renderer's coordinate system} (a.k.a.
     * {@link RenderingContext#mapCS mapCS}).
     *
     * @param  dest A pre-allocated point that stores the mouse's
     *              location, or <code>null</code> if none.
     * @return The mouse's location in map coordinates.
     */
    public Point2D getMapCoordinate(Point2D dest) {
        dest = getPixelCoordinate(dest);
        try {
            final MathTransform2D transform = (MathTransform2D) context.renderer.getMathTransform(
                            context.textCS, context.mapCS, "GeoMouseEvent", "getMapCoordinate");
            return transform.transform(dest, dest);
        } catch (TransformException exception) {
            // Should not happen, since the transform should be affine in most cases.
            Utilities.unexpectedException("org.geotools.renderer.j2d", "GeoMouseEvent",
                                          "getMapCoordinate", exception);
            dest.setLocation(Double.NaN, Double.NaN);
            return dest;
        }
    }

    /**
     * Returns the "real world" mouse's position in the specified coordinate system.
     *
     * @param  cs   The desired coordinate system.
     * @param  dest A pre-allocated point that stores the mouse's
     *              location, or <code>null</code> if none.
     * @return The mouse's location in map coordinates.
     * @throws TransformException if the mouse's position can't
     *         be expressed in the specified coordinate system.
     */
    public Point2D getCoordinate(CoordinateSystem cs, final Point2D dest)
            throws TransformException
    {
        if (!cs.equals(coordinateSystem, false)) {
            if (cs.equals(context.textCS, false)) {
                return getPixelCoordinate(dest);
            }
            /*
             * If the specified coordinate system is not the same than the one used the last time
             * this method was invoked, compute now the transformed coordinates and cache the value.
             * To keep things simple (and fast), we cache the values for only one coordinate system.
             * It should be enough for most cases, since a map is likely to use the same coordinate
             * system for all layers. If layers have mixed coordinate systems, then we will have to
             * recompute the coordinates each time the CS change.
             */
            Point2D point = getMapCoordinate(null);
            /*
             * Note: the following method call is faster when the target coordinate system is
             * the renderer's CS, since it can reuse pre-computed math transforms from a cache.
             * Inverting the returned transform in this case is both faster and consume less
             * memory than swapping 'sourceCS' and 'targetCS' arguments.
             */
            cs = CTSUtilities.getCoordinateSystem2D(cs);
            final MathTransform2D transform = (MathTransform2D) context.renderer.getMathTransform(
                            cs, context.mapCS, "GeoMouseEvent", "getCoordinate").inverse();
            point = transform.transform(point, point);
            px = point.getX();
            py = point.getY();
            coordinateSystem = cs;
        }
        if (dest != null) {
            dest.setLocation(px, py);
            return dest;
        }
        return new Point2D.Double(px, py);
    }

    /**
     * Returns the "real world" mouse's position in the specified coordinate system.
     * The coordinate system may have an arbitrary number of dimensions (as long as
     * a transform exists from the two-dimensional {@linkplain Renderer#getCoordinateSystem
     * renderer's coordinate system}), but is usually two-dimensional.
     *
     * @param  cs   The desired coordinate system.
     * @param  dest A pre-allocated point that stores the mouse's
     *              location, or <code>null</code> if none.
     * @return The mouse's location in map coordinates.
     * @throws TransformException if the mouse's position can't
     *         be expressed in the specified coordinate system.
     */
    public CoordinatePoint getCoordinate(CoordinateSystem cs, CoordinatePoint dest)
            throws TransformException
    {
        if (cs.equals(coordinateSystem, false)) {
            assert cs.getDimension() == 2;
            if (dest != null) {
                if (dest.ord.length != 2) {
                    throw new MismatchedDimensionException(cs, dest);
                }
                dest.ord[0] = px;
                dest.ord[1] = py;
                return dest;
            } else {
                return new CoordinatePoint(px, py);
            }
        }
        /*
         * If the specified coordinate system (cs) is not the same than the one used the last time
         * this method was invoked, compute now the transformed coordinates and cache the result.
         * Values are cached only for 2 dimensional coordinate systems.
         */
        final Point2D point = getMapCoordinate(null);
        final CoordinatePoint coord;
        if (cs.getDimension() == 2) {
            if (dest != null) {
                dest.setLocation(point);
            } else {
                dest = new CoordinatePoint(point);
            }
            coord = dest;
        } else {
            coord = new CoordinatePoint(point);
        }
        /*
         * Note: the following method call is faster when the target coordinate system is
         * the renderer's CS, since it can reuse pre-computed math transforms from a cache.
         * Inverting the returned transform in this case is both faster and consume less
         * memory than swapping 'sourceCS' and 'targetCS' arguments.
         */
        final MathTransform transform = context.renderer.getMathTransform(
                        cs, context.mapCS, "GeoMouseEvent", "getCoordinate").inverse();
        dest = transform.transform(coord, dest);
        if (dest.ord.length == 2) {
            assert cs.getDimension() == 2;
            coordinateSystem = cs;
            px = dest.ord[0];
            py = dest.ord[1];
        }
        return dest;
    }

    /**
     * Returns the transform from the widget coordinate system to the
     * {@linkplain Renderer#getCoordinateSystem renderer's coordinate system}.
     * More specifically, this is the transform from {@link RenderingContext#textCS textCS}
     * to {@link RenderingContext#mapCS mapCS}. This transform is usually (but not always)
     * {@linkplain java.awt.geom.AffineTransform affine}.
     * <br><br>
     * <strong>IMPLEMENTATION NOTE:</strong> In current implementation, this transform do not
     * apply any correction for {@linkplain DeformableViewer deformable viewer}. For fetching
     * current mouse coordinate, it is better to invokes {@link #getMapCoordinate}. The later
     * corrects for deformable viewers. A future version may includes the correction in the
     * <code>textToMap</code> math transform, in which case this transform may no longer be
     * affine.
     *
     * @return The transform from widget CRS to renderer CRS.
     */
    public MathTransform2D getTextToMap() {
        try {
            return (MathTransform2D) context.renderer.getMathTransform(
                   context.textCS, context.mapCS, "GeoMouseEvent", "getTextToMap");
        } catch (TransformException exception) {
            // Should not happen, since the transform should be affine in most cases.
            IllegalStateException e = new IllegalStateException(exception.getLocalizedMessage());
            e.initCause(exception);
            throw e;
        }
    }
}
