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

//import org.geotools.ct.MathTransform;
import java.util.EventObject;


/**
 * Sent when a map's Bounding Box changes.  This is usually fired by {@link
 * org.geotools.map.BoundingBoxImpl}
 *
 * @author Cameron Shorter
 * @version $Id: BoundingBoxEvent.java,v 1.2 2003/04/23 05:02:21 camerons Exp $
 */
public class BoundingBoxEvent extends EventObject {
    /**
     * If the BoundingBox was changed by an Affine Transform, then this
     * variable represents the transform, otherwise it will be null.
     */
    //private final MathTransform transform;

    /**
     * Constructs a new event.
     *
     * @param source The event source (usually a {@link
     *        org.geotools.map.MapPaneImpl}).
     * @param transform An affine transform indicating latest BoundingBox
     *        transform.  This may be set to null if the transform is unknown.
     */
    public BoundingBoxEvent(
        final Object source//,
        //final MathTransform transform
    ) {
        super(source);
//        this.transform = transform;
    }

//    /**
//     * Returns the affine transform for the last change in BoundingBox size.
//     * 
//     * <p>
//     * The transform may be null if the transform is not known or is being
//     * called for the first time.  In this case, call
//     * BoundingBox.getAreaOfInterest();
//     * </p>
//     * 
//     * <p>
//     * Note: for performance reasons, this method does not clone the returned
//     * transform. Do not transform!
//     * </p>
//     *
//     * @return the AffineTransform for the last change in BoundingBox size.
//     */
//    public MathTransform getTransform() {
//        return transform;
//    }
}
