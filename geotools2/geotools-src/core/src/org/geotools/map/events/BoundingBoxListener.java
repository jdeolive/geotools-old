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

import java.util.EventListener;


/**
 * Methods to handle a change in AreaOfInterest
 *
 * @author Cameron Shorter
 * @version $Id: BoundingBoxListener.java,v 1.3 2003/05/16 15:51:14 jmacgill Exp $
 */
public interface BoundingBoxListener extends EventListener {
    /**
     * Process an BoundingBoxEvent, probably triggers a redraw.
     *
     * @param boundingBoxEvent The new extent.
     */
    void areaOfInterestChanged(BoundingBoxEvent boundingBoxEvent);
}
