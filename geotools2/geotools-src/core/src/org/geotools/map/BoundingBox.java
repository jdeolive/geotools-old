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

// OpenGIS dependencies
import org.opengis.cs.CS_CoordinateSystem;
import org.opengis.ct.CT_MathTransform;

// Geotools dependencies
import org.geotools.map.event.BoundingBoxEvent;
import org.geotools.map.event.BoundingBoxListener;


/**
 * Stores extent and {@linkplain CS_CoordinateSystem coordinate system} associated with a map
 * {@linkplain Context context}. Note that there is no <code>setCoordinateSystem(...)</code>
 * method, this is to ensure that this object doesn't depend on <code>CoordinateTransform</code>
 * classes.  If you want to change <code>CoordinateSystem</code>, use the
 * <code>setAreaOfInterest(extent,coordinateSystem)</code> method and transform
 * the coordinates in the calling application.
 * <br><br>
 * Area of interest is cloned during construction and when returned.
 * This is to ensure only this class can change their values.
 *
 * @version $Id: BoundingBox.java,v 1.11 2003/08/29 11:02:34 desruisseaux Exp $
 * @author Cameron Shorter
 * @author Martin Desruisseaux
 */
public interface BoundingBox {
    /**
     * Set a new area of interest and trigger a {@link BoundingBoxEvent}.
     * Note that this is the only method to change coordinate system.  A
     * <code>setCoordinateSystem</code> method is not provided to ensure
     * this class is not dependant on transform classes.
     *
     * @param areaOfInterest The new areaOfInterest.
     * @param coordinateSystem The coordinate system being using by this model.
     * @throws IllegalArgumentException if an argument is <code>null</code>.
     */
    void setAreaOfInterest(Envelope areaOfInterest, CS_CoordinateSystem coordinateSystem)
        throws IllegalArgumentException;

    /**
     * Set a new area of interest and trigger an {@link BoundingBoxEvent}.
     *
     * @param areaOfInterest The new area of interest.
     * @throws IllegalArgumentException if an argument is <code>null</code>.
     */
    void setAreaOfInterest(Envelope areaOfInterest);

    /**
     * Gets the current area of interest.
     *
     * @return Current area of interest
     */
    Envelope getAreaOfInterest();

    /**
     * Get the current coordinate system.
     *
     * @return the coordinate system of this box.
     */
    CS_CoordinateSystem getCoordinateSystem();

    /**
     * Transform the coordinates according to the provided transform.
     * Useful for zooming and panning processes.
     *
     * @param transform The transform to change area of interest.
     */
    void transform(CT_MathTransform transform);

    /**
     * Create a copy of this class
     *
     * @return a copy of this object.
     *
     * @task REVISIT: clone should not be defined in those interfaces.
     */
    Object clone();

    /**
     * Register interest in receiving {@link BoundingBoxEvent}s.
     *
     * @param listener The object to notify when the area of interest has changed.
     */
    void addBoundingBoxListener(BoundingBoxListener listener);

    /**
     * Remove interest in receiving a {@link BoundingBoxEvent}s.
     *
     * @param listener The object to stop sending change events.
     */
    void removeBoundingBoxListener(BoundingBoxListener listener);

    /**
     * Register interest in receiving an {@link BoundingBoxEvent}.
     *
     * @param ecl The object to notify when AreaOfInterest has changed.
     * @param sendEvent After registering this listener, send a changeEvent to
     *        all listeners.
     *
     * @deprecated Use {@link #addBoundingBoxListener} instead.
     */
    void addAreaOfInterestChangedListener(BoundingBoxListener ecl, boolean sendEvent);

    /**
     * Register interest in receiving an BoundingBoxEvent.
     *
     * @param ecl The object to notify when AreaOfInterest has changed.
     *
     * @deprecated Use {@link #addBoundingBoxListener} instead.
     */
    void addAreaOfInterestChangedListener(BoundingBoxListener ecl);

    /**
     * Remove interest in receiving an BoundingBoxEvent.
     *
     * @param ecl The object to stop sending AreaOfInterestChanged Events.
     *
     * @deprecated Use {@link #removeBoundingBoxListener} instead.
     */
    void removeAreaOfInterestChangedListener(BoundingBoxListener ecl);
}
