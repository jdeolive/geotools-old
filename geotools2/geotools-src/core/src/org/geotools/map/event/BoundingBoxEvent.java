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
import java.util.EventObject;

// OpenGIS dependencies
import org.opengis.ct.CT_MathTransform;

// Geotools dependencies
import org.geotools.map.BoundingBox; // For JavaDoc


/**
 * Fired when a map's {@linkplain BoundingBox bounding box} changes.
 *
 * @author Cameron Shorter
 * @version $Id: BoundingBoxEvent.java,v 1.1 2003/08/18 16:32:31 desruisseaux Exp $
 *
 * @see BoundingBox
 * @see BoundingBoxListener
 */
public class BoundingBoxEvent extends EventObject {
    /*
     * If the BoundingBox was changed by some transform (usually an affine one),
     * then this variable represents the transform, otherwise it will be null.
     */
    private final CT_MathTransform transform;

    /**
     * Constructs a new event with no transform.
     *
     * @param source The event source (usually a {@link BoundingBox}).
     */
    public BoundingBoxEvent(final Object source) {
        this(source, null);
    }

    /**
     * Constructs a new event with the specified transform.
     *
     * @param source The event source (usually a {@link BoundingBox}).
     */
    public BoundingBoxEvent(final Object source, final CT_MathTransform transform) {
        super(source);
        this.transform = transform;
    }

    /**
     * Returns the bounding box change as a transform. This transform is usually affine.
     * This method may returns <code>null</code> if the transform is not known.  In this
     * case, call {@link BoundingBox#getAreaOfInterest()}.
     *
     * @return the transform for the last change in BoundingBox size.
     */
    public CT_MathTransform getTransform() {
        return transform;
    }
}
