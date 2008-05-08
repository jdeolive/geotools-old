/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2005-2006, GeoTools Project Managment Committee (PMC)
 *    (C) 2005, Institut de Recherche pour le Développement
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
 *    This package contains documentation from OpenGIS specifications.
 *    OpenGIS consortium's work is fully acknowledged here.
 */
package org.geotools.display.canvas;

import java.io.Serializable;

import org.opengis.util.Cloneable;
import org.opengis.go.display.canvas.Canvas;        // For javadoc
import org.opengis.go.display.canvas.CanvasState;
import org.opengis.geometry.Envelope;
import org.opengis.geometry.DirectPosition;

import org.geotools.resources.Classes;
import org.geotools.resources.Utilities;
import org.geotools.geometry.GeneralDirectPosition;


/**
 * Describes the current state of a {@link Canvas}. The information contained by instances
 * of this interface should only describe the viewing area or volume of the canvas and should
 * not contain any state information regarding the data contained within it.
 * <p>
 * When an instance of this class is returned from {@link Canvas} methods, a "snapshot" of the
 * current state of the canvas is taken and the values will never change (even if the canvas
 * changes state).
 *
 * @since 2.3
 * @source $URL$
 * @version $Id$
 * @author Martin Desruisseaux
 */
public class DefaultCanvasState implements CanvasState, Cloneable, Serializable {
    /**
     * Serial number for interoperability with different versions.
     */
    private static final long serialVersionUID = 8473318790311715748L;

    /**
     * The title of the canvas.
     */
    private final String title;

    /**
     * The position of the center pixel of the canvas.
     */
    private final GeneralDirectPosition center;

    /**
     * Creates a canvas state with the specified title and center position.
     *
     * @param title  The title of the canvas.
     * @param center The position of the center pixel of the canvas.
     */
    public DefaultCanvasState(final String title, final DirectPosition center) {
        this.title  = title;
        this.center = (center!=null) ? new GeneralDirectPosition(center) : null;
    }

    /**
     * Creates a canvas state with the specified title and envelope. The center position
     * is infered from the envelope.
     *
     * @param title    The title of the canvas.
     * @param envelope The canvas envelope.
     */
    public DefaultCanvasState(final String title, final Envelope envelope) {
        this.title = title;
        if (envelope != null) {
            final int dimension = envelope.getDimension();
            center = new GeneralDirectPosition(dimension);
            center.setCoordinateReferenceSystem(envelope.getCoordinateReferenceSystem());
            for (int i=0; i<dimension; i++) {
                center.ordinates[i] = envelope.getCenter(i);
            }
        } else {
            center = null;
        }
    }

    /**
     * Returns the title of the canvas.
     */
    public String getTitle() {
        return title;
    }

    /**
     * Returns the position of the center pixel of the canvas.
     */
    public DirectPosition getCenter() {
        return (center!=null) ? new GeneralDirectPosition(center) : center;
    }

    /**
     * Returns a hash code value for this canvas state.
     */
    @Override
    public int hashCode() {
        int code = (int) serialVersionUID;
        if (title  != null) code ^= title .hashCode();
        if (center != null) code ^= center.hashCode();
        return code;
    }

    /**
     * Determines if the given object is the same type of canvas state object and has
     * values equal to this one.
     */
    @Override
    public boolean equals(final Object object) {
        if (object == this) {
            return true;
        }
        if (object!=null && object.getClass().equals(getClass())) {
            final DefaultCanvasState that = (DefaultCanvasState) object;
            return Utilities.equals(this.title,  that.title ) &&
                   Utilities.equals(this.center, that.center);
        }
        return false;
    }

    /**
     * Returns a copy of this canvas state.
     */
    @Override
    public CanvasState clone() {
        try {
            return (CanvasState) super.clone();
        } catch (CloneNotSupportedException e) {
            // Should never happen, since we are cloneable.
            throw new AssertionError(e);
        }
    }

    /**
     * Returns a string representation of this canvas state.
     */
    @Override
    public String toString() {
        final StringBuilder buffer = new StringBuilder(Classes.getShortClassName(this));
        buffer.append('[');
        if (title != null) {
            buffer.append('"').append(title).append('"');
        }
        if (center != null) {
            buffer.append(", (").append(center).append(')');
        }
        return buffer.append(']').toString();
    }
}
