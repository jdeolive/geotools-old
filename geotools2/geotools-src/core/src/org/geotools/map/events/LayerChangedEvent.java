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
package org.geotools.map.events;


/**
 * Legacy event.
 *
 * @author iant
 *
 * @deprecated Use {@link org.geotools.map.event.LayerEvent} instead.
 */
public class LayerChangedEvent extends org.geotools.map.event.LayerEvent {
    /**
     * Creates a new instance of LayerChangedEvent
     *
     * @param source the source of the event change.
     */
    public LayerChangedEvent(Object source) {
        super(source, VISIBILITY_CHANGED);
    }

    /**
     * Creates a new instance of LayerChangedEvent
     *
     * @param source the source of the event change.
     * @param reason why the event was fired.
     */
    public LayerChangedEvent(Object source, int reason) {
        super(source, reason);
    }

    /**
     * Setter for property reason.
     *
     * @param reason New value of property reason.
     */
    public void setReason(int reason) {
        // Unsupported method.
    }
}
