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
import org.geotools.map.events.BoundingBoxListener;
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
 * @version $Id: BoundingBox.java,v 1.9 2003/08/07 22:11:22 cholmesny Exp $
 */
public interface BoundingBox extends Cloneable {
    /**
     * Register interest in receiving an AreaOfInterestChangedEvent.
     *
     * @param ecl The object to notify when AreaOfInterest has changed.
     * @param sendEvent After registering this listener, send a changeEvent to
     *        all listeners.
     */
    void addAreaOfInterestChangedListener(BoundingBoxListener ecl,
        boolean sendEvent);

    /**
     * Register interest in receiving an AreaOfInterestChangedEvent.
     *
     * @param ecl The object to notify when AreaOfInterest has changed.
     */
    void addAreaOfInterestChangedListener(BoundingBoxListener ecl);

    /**
     * Remove interest in receiving an AreaOfInterestChangedEvent.
     *
     * @param ecl The object to stop sending AreaOfInterestChanged Events.
     */
    void removeAreaOfInterestChangedListener(BoundingBoxListener ecl);

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
    void setAreaOfInterest(Envelope bbox, CS_CoordinateSystem coordinateSystem)
        throws IllegalArgumentException;

    /**
     * Set a new AreaOfInterest and trigger an AreaOfInterestEvent.
     *
     * @param areaOfInterest The new areaOfInterest.
     *
     * @throws IllegalArgumentException if an argument is <code>null</code>.
     */
    void setAreaOfInterest(Envelope areaOfInterest);

    /**
     * Gets the current AreaOfInterest.
     *
     * @return Current AreaOfInterest
     */
    Envelope getAreaOfInterest();

    /**
     * Get the coordinateSystem.
     *
     * @return the coordinate system of this box.
     */
    CS_CoordinateSystem getCoordinateSystem();

    /**
     * Transform the coordinates according to the provided transform.  Useful
     * for zooming and panning processes.
     *
     * @param transform The transform to change AreaOfInterest.
     */
    void transform(CT_MathTransform transform);

    /**
     * Create a copy of this class
     *
     * @return a copy of this object.
     */
    Object clone();
}
