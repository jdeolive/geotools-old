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

import com.vividsolutions.jts.geom.Envelope;
import org.geotools.cs.CoordinateSystem;
import org.geotools.ct.Adapters;
import org.geotools.ct.MathTransform;
import org.geotools.ct.MathTransformFactory;
import org.geotools.ct.TransformException;
import org.geotools.map.events.BoundingBoxEvent;
import org.geotools.map.events.BoundingBoxListener;
import org.geotools.pt.CoordinatePoint;
import org.opengis.cs.CS_CoordinateSystem;
import org.opengis.ct.CT_MathTransform;
/**
 * Stores Extent and CoordinateSystem associated with a Map Context. Note that
 * there is no setCoordinateSystem, this is to ensure that this object doesn't
 * depend on CoordinateTransform classes.  If you want to change
 * CoordinateSystem, use the setExtent(extent,coordinateSystem) method and
 * transform the coordinates in the calling application.<br>
 * Extent and CoordinateSystem are cloned during construction and when
 * returned. This is to ensure only this class can change their values.
 *
 * @task REVISIT Probably should use CoordinatePoint or Point2D to store points
 *       instead of using Envelope.  Also worth waiting to see what interface
 *       the GeoAPI project creates and use that.
 */
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.lang.Cloneable;
import java.lang.IllegalArgumentException;
import java.rmi.RemoteException;
import java.util.EventObject;
import java.util.Vector;
import java.util.logging.Logger;
import javax.swing.event.EventListenerList;


/**
 * Stores the a Bounding Box (Area Of Interest) for a Context and sends an
 * event to interested classes when parameters change.
 *
 * @author Cameron Shorter
 * @version $Id: BoundingBoxImpl.java,v 1.12 2003/05/02 10:55:18 desruisseaux Exp $
 */
public class BoundingBoxImpl implements BoundingBox {
    private static final Logger LOGGER =
        Logger.getLogger("org.geotools.map.BoundingBoxImpl");
    private Envelope bBox;
    private CoordinateSystem coordinateSystem;
    private EventListenerList listenerList = new EventListenerList();
    private Adapters adapters = Adapters.getDefault();

    /**
     * Initialise the model.
     *
     * @param bbox The extent associated with this class.
     * @param coordinateSystem The coordinate system associated with this
     *        class.
     *
     * @throws IllegalArgumentException if an argument is <code>null</code>.
     */
    protected BoundingBoxImpl(
        Envelope bbox,
        CS_CoordinateSystem coordinateSystem
    ) throws IllegalArgumentException {
        this.setAreaOfInterest(bbox, coordinateSystem);
    }

    /**
     * Initialise the model.
     *
     * @param bbox The extent associated with this class.
     * @param coordinateSystem The coordinate system associated with this
     *        class.
     *
     * @throws IllegalArgumentException if an argument is <code>null</code>.
     */
    protected BoundingBoxImpl(
        Envelope bbox,
        CoordinateSystem coordinateSystem
    ) throws IllegalArgumentException {
        this.setAreaOfInterest(bbox, coordinateSystem);
    }

    /**
     * Register interest in receiving an AreaOfInterestChangedEvent.
     *
     * @param ecl The object to notify when AreaOfInterest has changed.
     * @param sendEvent After registering this listener, send a changeEvent to
     *        all listeners.
     */
    public void addAreaOfInterestChangedListener(
        BoundingBoxListener ecl,
        boolean sendEvent
    ) {
        listenerList.add(BoundingBoxListener.class, ecl);

        if (sendEvent) {
            fireAreaOfInterestChangedListener(null);
        }
    }

    /**
     * Register interest in receiving an AreaOfInterestChangedEvent.
     *
     * @param ecl The object to notify when AreaOfInterest has changed.
     */
    public void addAreaOfInterestChangedListener(BoundingBoxListener ecl) {
        addAreaOfInterestChangedListener(ecl, false);
    }

    /**
     * Remove interest in receiving an AreaOfInterestChangedEvent.
     *
     * @param ecl The object to stop sending AreaOfInterestChanged Events.
     */
    public void removeAreaOfInterestChangedListener(BoundingBoxListener ecl) {
        listenerList.remove(BoundingBoxListener.class, ecl);
    }

    /**
     * Notify all listeners that have registered interest for notification an
     * AreaOfInterestChangedEvent.
     *
     * @param transform The transform that has been applied to this class.
     */
    protected void fireAreaOfInterestChangedListener(MathTransform transform) {
        // Guaranteed to return a non-null array
        Object[] listeners = listenerList.getListenerList();

        // Process the listeners last to first, notifying
        // those that are interested in this event
        BoundingBoxEvent ece = new BoundingBoxEvent(this //, transform
            );

        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] == BoundingBoxListener.class) {
                ((BoundingBoxListener) listeners[i + 1]).areaOfInterestChanged(
                    ece
                );
            }
        }
    }

    /**
     * Set a new AreaOfInterest and trigger a BoundingBoxEvent. A
     * <code>setCoordinateSystem</code> method is not provided to ensure this
     * class is not dependant on classes to transform between coordinate
     * systems.
     *
     * @param bbox The new areaOfInterest.
     * @param coordinateSystem The coordinate system being using by this model.
     *
     * @throws IllegalArgumentException if an argument is <code>null</code>.
     */
    private void setAreaOfInterest(
        Envelope bbox,
        CoordinateSystem coordinateSystem
    ) throws IllegalArgumentException {
        MathTransform transform;

        if ((bbox == null) || (coordinateSystem == null) || bbox.isNull()) {
            throw new IllegalArgumentException();
        }

        // Calculate the transform from the old to new bbox, or set transform
        // to null if it is not available
        if ((this.bBox == null) || (this.coordinateSystem != coordinateSystem)) {
            transform = null;
        } else {
            AffineTransform at = new AffineTransform();
            at.setToIdentity();
            at.translate(
                bbox.getMinX(),
                bbox.getMinY()
            );

            // scaleFactor=newWidth/oldWidth
            at.scale(
                (bBox.getMaxX() - bBox.getMinX()) / (this.bBox.getMaxX()
                - this.bBox.getMinX()),
                (bBox.getMaxY() - bBox.getMinY()) / (this.bBox.getMaxY()
                - this.bBox.getMinY())
            );
            at.translate(-this.bBox.getMinX(), -this.bBox.getMinY());
            transform =
                MathTransformFactory.getDefault().createAffineTransform(at);
        }

        this.bBox = new Envelope(bbox);
        this.coordinateSystem = coordinateSystem;
        fireAreaOfInterestChangedListener(transform);
    }

    /**
     * Set a new AreaOfInterest and trigger an AreaOfInterestEvent. Note that
     * this is the only method to change coordinateSystem.  A
     * <code>setCoordinateSystem</code> method is not provided to ensure this
     * class is not dependant on transform classes.
     *
     * @param bbox The new areaOfInterest.
     * @param coordinateSystem The coordinate system being using by this model.
     *
     * @throws IllegalArgumentException if an argument is <code>null</code>.
     */
    public void setAreaOfInterest(
        Envelope bbox,
        CS_CoordinateSystem coordinateSystem
    ) throws IllegalArgumentException {
        try {
            setAreaOfInterest(
                bbox,
                adapters.wrap(coordinateSystem)
            );
        } catch (RemoteException e) {
            // TODO: We should not hide a checked exception that way.
            throw new java.lang.reflect.UndeclaredThrowableException(e, "Remote call failed");
        }
    }

    /**
     * Set a new AreaOfInterest and trigger an AreaOfInterestEvent.
     *
     * @param bbox The new areaOfInterest.
     */
    public void setAreaOfInterest(Envelope bbox) {
        setAreaOfInterest(bbox, this.coordinateSystem);
    }

    /**
     * Transform the coordinates according to the provided transform.  Useful
     * for zooming and panning processes.
     *
     * @param transform The transform to change AreaOfInterest.
     */
    public void transform(MathTransform transform) {
        // The real world coordinates of the AreaOfInterest
        CoordinatePoint minP =
            new CoordinatePoint(
                bBox.getMinX(),
                bBox.getMinY()
            );
        CoordinatePoint maxP =
            new CoordinatePoint(
                bBox.getMaxX(),
                bBox.getMaxY()
            );

        try {
            transform.transform(minP, minP);
            transform.transform(maxP, maxP);
            bBox =
                new Envelope(
                    minP.getOrdinate(0),
                    maxP.getOrdinate(0),
                    minP.getOrdinate(1),
                    maxP.getOrdinate(1)
                );

            //LOGGER.info("bBox="+bBox);
            fireAreaOfInterestChangedListener(transform);
        } catch (TransformException e) {
            // TODO: We should not hide a checked exception that way.
            throw new java.lang.reflect.UndeclaredThrowableException(e, "Transformation failed");
        }
    }

    /**
     * Transform the coordinates according to the provided transform.  Useful
     * for zooming and panning processes.
     *
     * @param transform The transform to change AreaOfInterest.
     */
    public void transform(CT_MathTransform transform) {
        try {
            transform(adapters.wrap(transform));
        } catch (RemoteException e) {
            // TODO: We should not hide a checked exception that way.
            throw new java.lang.reflect.UndeclaredThrowableException(e, "Remote call failed");
        }
    }

    /**
     * Gets the current AreaOfInterest.
     *
     * @return Current AreaOfInterest
     */
    public Envelope getAreaOfInterest() {
        return new Envelope(bBox);
    }

    /**
     * Get the coordinateSystem.
     *
     * @return the coordinateSystem.
     */
    public CS_CoordinateSystem getCoordinateSystem() {
        try {
            return adapters.export(this.coordinateSystem);
        } catch (RemoteException e) {
            // TODO: We should not hide a checked exception that way.
            throw new java.lang.reflect.UndeclaredThrowableException(e, "Remote call failed");
        }
    }

    /*
     * Create a copy of this class
     * @HACK Probably need to add all the eventListeners to the cloned class.
     */
    public Object clone() {
        return new BoundingBoxImpl(
            this.bBox,
            this.coordinateSystem
        );
    }

    /**
     * Show the Envelope extent.
     *
     * @return The BoundingBox extent in the form (minX,minY,maxX,maxY).
     */
    public String toString() {
        return "(" + bBox.getMinX() + "," + bBox.getMinY() + ")," + "("
        + bBox.getMaxX() + "," + bBox.getMaxY() + ")";
    }
}
