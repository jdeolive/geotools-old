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
package org.geotools.map.event;


// Geotools dependencies
import org.geotools.map.BoundingBox; // For JavaDoc

// J2SE dependencies
import java.util.EventListener;


/**
 * The listener that's notified when a {@link BoundingBox} changes its area of interest.
 *
 * @author Cameron Shorter
 * @version $Id: MapBoundsListener.java,v 1.1 2003/12/04 23:20:33 aaime Exp $
 *
 * @see AreaOfInterestEvent
 */
public interface MapBoundsListener extends EventListener {
    /**
     * Invoked when the area of interest or the coordinate system changes
     *
     * @param event The change event.
     */
    void mapBoundsChanged(MapBoundsEvent event);
}
