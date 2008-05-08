/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2003-2006, Geotools Project Managment Committee (PMC)
 *    (C) 2001, Institut de Recherche pour le Développement
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
package org.geotools.gui.swing.event;

// Dependencies
import java.util.EventObject;
import java.awt.geom.AffineTransform;


/**
 * An event which indicates that a zoom occurred in a component.
 * This event is usually fired by {@link org.geotools.gui.swing.ZoomPane}.
 *
 * @since 2.0
 * @source $URL$
 * @version $Id$
 * @author Martin Desruisseaux
 */
public class ZoomChangeEvent extends EventObject {
    /**
     * An affine transform indicating the zoom change. If {@code oldZoom} and {@code newZoom}
     * are the affine transforms before and after the change respectively, then the following
     * relation must hold (within the limits of rounding error):
     *
     * <code>newZoom = oldZoom.{@link AffineTransform#concatenate concatenate}(change)</code>
     */
    private final AffineTransform change;

    /**
     * Constructs a new event. If {@code oldZoom} and {@code newZoom} are the affine transforms
     * before and after the change respectively, then the following relation must hold (within
     * the limits of rounding error):
     *
     * <code>newZoom = oldZoom.{@link AffineTransform#concatenate concatenate}(change)</code>
     *
     * @param source The event source (usually a {@link org.geotools.gui.swing.ZoomPane}).
     * @param change An affine transform indicating the zoom change.
     */
    public ZoomChangeEvent(final Object source, final AffineTransform change) {
        super(source);
        this.change = change;
    }

    /**
     * Returns the affine transform indicating the zoom change.
     * <strong>Note:</strong> for performance reasons, this method does not clone
     * the returned transform. Do not change!
     */
    public AffineTransform getChange() {
        return change;
    }
}
