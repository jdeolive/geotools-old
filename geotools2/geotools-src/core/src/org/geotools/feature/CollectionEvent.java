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
package org.geotools.feature;

import java.util.EventObject;


/**
 * A simple event object to represent all events triggered by FeatureCollection
 * instances (typically change events).
 *
 * @author Ray Gallagher
 * @version $Id: CollectionEvent.java,v 1.5 2003/05/07 16:53:04 jmacgill Exp $
 */
public class CollectionEvent extends EventObject {
    /**
     * Constructs a new CollectionEvent.
     *
     * @param source the collection which triggered the event
     *
     * @task TODO: potential for reason codes here later
     */
    public CollectionEvent(Object source) {
        super(source);
    }
}
