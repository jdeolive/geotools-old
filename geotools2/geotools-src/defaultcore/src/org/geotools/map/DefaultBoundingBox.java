/*
 *    Geotools2 - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002, Geotools Project Managment Committee (PMC)
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    version 2.1 of the License.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *
 */
package org.geotools.map;


// JTS dependencies
import com.vividsolutions.jts.geom.Envelope;

// Geotools dependencies
import org.geotools.cs.CoordinateSystem;
import org.geotools.ct.Adapters;
import org.geotools.ct.MathTransform2D;
import org.geotools.ct.MathTransformFactory;
import org.geotools.ct.TransformException;
import org.geotools.map.event.BoundingBoxEvent;
import org.geotools.map.event.BoundingBoxListener;
import org.geotools.resources.CTSUtilities;

// OpenGIS dependencies
import org.opengis.cs.CS_CoordinateSystem;
import org.opengis.ct.CT_MathTransform;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.lang.reflect.UndeclaredThrowableException;

// J2SE dependencies
import java.rmi.RemoteException;
import javax.swing.event.EventListenerList;


/**
 * A default implementation of {@link BoundingBox}.
 *
 * @author Cameron Shorter
 * @author Martin Desruisseaux
 * @version $Id: DefaultBoundingBox.java,v 1.3 2003/08/20 21:04:11 cholmesny Exp $
 *
 * @task REVISIT Probably should use CoordinatePoint or Point2D to store points
 *       instead of using Envelope.  Also worth waiting to see what interface
 *       the GeoAPI project creates and use that.
 */
public class DefaultBoundingBox implements BoundingBox {
    /** The area of interest. */
    private Envelope areaOfInterest;

    /** The coordinate system for the area of interest. */
    private CoordinateSystem coordinateSystem;

    /** The listener list. Will be constructed only when first needed. */
    private EventListenerList listenerList;

    /**
     * Construct a bounding box with the given area of interest and coordinate
     * system.
     *
     * @param areaOfInterest The extent associated with this class.
     * @param coordinateSystem The coordinate system associated with this
     *        class.
     *
     * @throws IllegalArgumentException if an argument is <code>null</code>.
     */
    protected DefaultBoundingBox(final Envelope areaOfInterest,
        final CoordinateSystem coordinateSystem)
        throws IllegalArgumentException {
        setAreaOfInterest(areaOfInterest, coordinateSystem);
    }

    /**
     * Construct a bounding box with the given area of interest and coordinate
     * system.
     *
     * @param areaOfInterest The extent associated with this class.
     * @param coordinateSystem The coordinate system associated with this
     *        class.
     *
     * @throws IllegalArgumentException if an argument is <code>null</code>.
     */
    protected DefaultBoundingBox(final Envelope areaOfInterest,
        final CS_CoordinateSystem coordinateSystem)
        throws IllegalArgumentException {
        setAreaOfInterest(areaOfInterest, coordinateSystem);
    }

    /**
     * Set a new area of interest and trigger a {@link BoundingBoxEvent}. Note
     * that this is the only method to change coordinate system.  A
     * <code>setCoordinateSystem</code> method is not provided to ensure this
     * class is not dependant on transform classes.
     *
     * @param areaOfInterest The new areaOfInterest.
     * @param coordinateSystem The coordinate system being using by this model.
     *
     * @throws IllegalArgumentException if an argument is <code>null</code>.
     */
    private void setAreaOfInterest(final Envelope areaOfInterest,
        final CoordinateSystem coordinateSystem)
        throws IllegalArgumentException {
        if ((areaOfInterest == null) || (coordinateSystem == null)
                || areaOfInterest.isNull()) {
            throw new IllegalArgumentException();
        }

        // Calculate the transform from the old to new area of interest, or set transform
        // to null if it is not available. Note: current implementation do not take coordinate
        // system changes in account; we may provides that in a future version.
        MathTransform2D transform;

        if ((this.areaOfInterest == null) || (this.coordinateSystem == null)
                || (!this.coordinateSystem.equals(coordinateSystem, false))) {
            transform = null;
        } else {
            final AffineTransform at = AffineTransform.getTranslateInstance(areaOfInterest
                    .getMinX(), areaOfInterest.getMinY());

            // scaleFactor = newWidth/oldWidth
            at.scale(areaOfInterest.getWidth() / this.areaOfInterest.getWidth(),
                areaOfInterest.getHeight() / this.areaOfInterest.getHeight());
            at.translate(-this.areaOfInterest.getMinX(),
                -this.areaOfInterest.getMinY());
            transform = MathTransformFactory.getDefault().createAffineTransform(at);
        }

        this.areaOfInterest = new Envelope(areaOfInterest);
        this.coordinateSystem = coordinateSystem;
        fireAreaOfInterestChanged(transform);
    }

    /**
     * {@inheritDoc}
     */
    public void setAreaOfInterest(final Envelope areaOfInterest,
        final CS_CoordinateSystem coordinateSystem)
        throws IllegalArgumentException {
        try {
            setAreaOfInterest(areaOfInterest,
                Adapters.getDefault().wrap(coordinateSystem));
        } catch (RemoteException e) {
            // TODO: We should not hide a checked exception that way.
            throw new UndeclaredThrowableException(e, "Remote call failed");
        }
    }

    /**
     * {@inheritDoc}
     */
    public void setAreaOfInterest(Envelope areaOfInterest) {
        setAreaOfInterest(areaOfInterest, this.coordinateSystem);
    }

    /**
     * {@inheritDoc}
     */
    public Envelope getAreaOfInterest() {
        return new Envelope(areaOfInterest);
    }

    /**
     * {@inheritDoc}
     */
    public CS_CoordinateSystem getCoordinateSystem() {
        try {
            return Adapters.getDefault().export(this.coordinateSystem);
        } catch (RemoteException e) {
            // TODO: We should not hide a checked exception that way.
            throw new UndeclaredThrowableException(e, "Remote call failed");
        }
    }

    /**
     * Transform the area of interest according to the provided transform.
     * Useful for zooming and panning processes.
     *
     * @param transform The transform to apply on the area of interest.
     *
     * @throws TransformException if the transformation failed.
     */
    public void transform(final MathTransform2D transform)
        throws TransformException {
        Rectangle2D area = new Rectangle2D.Double(areaOfInterest.getMinX(),
                areaOfInterest.getMinY(), areaOfInterest.getWidth(),
                areaOfInterest.getHeight());
        area = CTSUtilities.transform(transform, area, area);
        areaOfInterest = new Envelope(area.getMinX(), area.getMinY(),
                area.getMaxX(), area.getMaxY());
        fireAreaOfInterestChanged(transform);
    }

    /**
     * {@inheritDoc}
     */
    public void transform(CT_MathTransform transform) {
        try {
            transform((MathTransform2D) Adapters.getDefault().wrap(transform));
        } catch (RemoteException e) {
            // TODO: We should not hide a checked exception that way.
            throw new UndeclaredThrowableException(e, "Remote call failed");
        } catch (TransformException e) {
            // TODO: We should not hide a checked exception that way.
            throw new UndeclaredThrowableException(e, "Transformation failed");
        }
    }

    /**
     * {@inheritDoc}
     */
    public void addBoundingBoxListener(final BoundingBoxListener listener) {
        if (listenerList == null) {
            listenerList = new EventListenerList();
        }

        listenerList.add(BoundingBoxListener.class, listener);
    }

    /**
     * {@inheritDoc}
     */
    public void removeBoundingBoxListener(final BoundingBoxListener listener) {
        if (listenerList != null) {
            listenerList.remove(BoundingBoxListener.class, listener);

            if (listenerList.getListenerCount() == 0) {
                listenerList = null;
            }
        }
    }

    /**
     * {@inheritDoc}
     *
     * @deprecated Use {@link #addBoundingBoxListener} instead.
     */
    public void addAreaOfInterestChangedListener(
        final BoundingBoxListener ecl, final boolean sendEvent) {
        addAreaOfInterestChangedListener(ecl);

        if (sendEvent) {
            fireAreaOfInterestChanged(null);
        }
    }

    /**
     * {@inheritDoc}
     *
     * @deprecated Use {@link #addBoundingBoxListener} instead.
     */
    public void addAreaOfInterestChangedListener(BoundingBoxListener ecl) {
        addBoundingBoxListener(ecl);
    }

    /**
     * {@inheritDoc}
     *
     * @deprecated Use {@link #removeBoundingBoxListener} instead.
     */
    public void removeAreaOfInterestChangedListener(BoundingBoxListener ecl) {
        removeBoundingBoxListener(ecl);
    }

    /**
     * Notify all listeners that have registered interest for changes in the
     * area of tnterest.
     *
     * @param transform The transform that has been applied to the area of
     *        interest.
     */
    protected void fireAreaOfInterestChanged(final MathTransform2D transform) {
        if (listenerList != null) {
            // Guaranteed to return a non-null array
            final Object[] listeners = listenerList.getListenerList();

            // Process the listeners last to first, notifying
            // those that are interested in this event
            BoundingBoxEvent event = null;

            for (int i = listeners.length; (i -= 2) >= 0;) {
                if (listeners[i] == BoundingBoxListener.class) {
                    if (event == null) {
                        try {
                            event = new BoundingBoxEvent(this,
                                    Adapters.getDefault().export(transform));
                        } catch (RemoteException exception) {
                            // TODO: We should not hide a checked exception that way.
                            throw new UndeclaredThrowableException(exception,
                                "Remote call failed");
                        }
                    }

                    ((BoundingBoxListener) listeners[i + 1])
                    .areaOfInterestChanged(event);
                }
            }
        }
    }

    /**
     * Create a copy of this class
     *
     * @return the copy of this class.
     *
     * @task HACK: Probably need to add all the eventListeners to the cloned
     *       class.
     */
    public Object clone() {
        return new DefaultBoundingBox(this.areaOfInterest, this.coordinateSystem);
    }

    /**
     * Show the Envelope extent.
     *
     * @return The BoundingBox extent in the form (minX,minY,maxX,maxY).
     */
    public String toString() {
        final StringBuffer buffer = new StringBuffer();
        buffer.append('(');
        buffer.append(areaOfInterest.getMinX());
        buffer.append(',');
        buffer.append(areaOfInterest.getMinY());
        buffer.append("),(");
        buffer.append(areaOfInterest.getMaxX());
        buffer.append(',');
        buffer.append(areaOfInterest.getMaxY());
        buffer.append(')');

        return buffer.toString();
    }
}
