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

// J2SE dependencies
import java.util.EventListener;

// Geotools dependencies
import org.geotools.map.BoundingBox; // For JavaDoc


/**
 * The listener that's notified when a {@link BoundingBox} changes its area of interest.
 *
 * @author Cameron Shorter
 * @version $Id: BoundingBoxListener.java,v 1.1 2003/08/18 16:32:31 desruisseaux Exp $
 *
 * @see BoundingBox
 * @see BoundingBoxEvent
 */
public interface BoundingBoxListener extends EventListener {
    /**
     * Invoked when the bounding box's {@linkplain BoundingBox#getAreaOfInterest area of
     * interest} changed. The implementation for this method will typically triggers a
     * redraw.
     *
     * @param event The change event.
     */
    void areaOfInterestChanged(BoundingBoxEvent event);
}
