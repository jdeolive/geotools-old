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
/*
 * LayerChangedEvent.java
 *
 * Created on 10 July 2003, 17:10
 */
package org.geotools.map.events;

import java.util.EventObject;


/**
 * Event fired if a layer changes.
 *
 * @author iant
 */
public class LayerChangedEvent extends EventObject {
    private int reason = 0;

    /**
     * Creates a new instance of LayerChangedEvent
     *
     * @param source the source of the event change.
     */
    public LayerChangedEvent(Object source) {
        super(source);
    }

    /**
     * Creates a new instance of LayerChangedEvent
     *
     * @param source the source of the event change.
     * @param reason why the event was fired.
     */
    public LayerChangedEvent(Object source, int reason) {
        this(source);
        this.reason = reason;
    }

    /**
     * Getter for property reason.
     *
     * @return Value of property reason.
     */
    public int getReason() {
        return reason;
    }

    /**
     * Setter for property reason.
     *
     * @param reason New value of property reason.
     */
    public void setReason(int reason) {
        this.reason = reason;
    }

    /**
     * Getter for property source.
     *
     * @return Value of property source.
     */
    public java.lang.Object getSource() {
        return source;
    }

    /**
     * Setter for property source.
     *
     * @param source New value of property source.
     */
    public void setSource(java.lang.Object source) {
        this.source = source;
    }
}
