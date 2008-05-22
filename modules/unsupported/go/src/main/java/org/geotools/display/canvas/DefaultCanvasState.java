/*
 *    GeoTools - An Open Source Java GIS Tookit
 *    http://geotools.org
 *    (C) 2004-2008, Open Source Geospatial Foundation (OSGeo)
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
 */
package org.geotools.display.canvas;

import java.io.Serializable;

import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.util.Cloneable;
import org.opengis.util.InternationalString;
import org.opengis.display.canvas.CanvasState;
import org.opengis.geometry.DirectPosition;

import org.geotools.resources.Classes;
import org.geotools.resources.Utilities;

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
 * @author Martin Desruisseaux (IRD)
 * @author Johann Sorel (Geomatys)
 */
public class DefaultCanvasState implements CanvasState, Cloneable, Serializable {
    /**
     * Serial number for interoperability with different versions.
     */
    private static final long serialVersionUID = 8473318790311715748L;

    /**
     * The title of the canvas.
     */
    private final InternationalString title;
    /**
     * The position of the center pixel of the canvas.
     */
    private final DirectPosition center;
    
    private final CoordinateReferenceSystem objectiveCRS;
    
    private final CoordinateReferenceSystem displayCRS;
    
    private final MathTransform dispToObj;
    
    private final MathTransform objToDisp;
    

    /**
     * Creates a canvas state with the specified title and center position.
     *
     * @param title  The title of the canvas.
     * @param center The position of the center pixel of the canvas.
     * @param obj Objective CRS
     * @param disp Display CRS
     * @param toObj MathTransform to Objective CRS
     * @param toDisp MathTransform to Display CRS
     */
    public DefaultCanvasState(
            final InternationalString title, 
            final DirectPosition center,
            final CoordinateReferenceSystem obj,
            final CoordinateReferenceSystem disp,
            final MathTransform toObj,
            final MathTransform toDisp) {
        this.title  = title;
        this.center = center;
        objectiveCRS = obj;
        displayCRS = disp;
        dispToObj = toObj;
        objToDisp = toDisp;
    }

   


    public DirectPosition getCenter() {
        return center;
    }

    public InternationalString getTitle() {
        return title;
    }

    public CoordinateReferenceSystem getDisplayCRS() {
        return displayCRS;
    }

    public CoordinateReferenceSystem getObjectiveCRS() {
        return objectiveCRS;
    }

    public MathTransform getObjectiveToDisplayTransform() {
        return objToDisp;
    }

    public MathTransform getDisplayToObjectiveTransform() {
        return dispToObj;
    }
    
    
    /**
     * Returns a hash code value for this canvas state.
     * @return hashcode
     */
    @Override
    public int hashCode() {
        int code = (int) serialVersionUID;
        if (title  != null) code ^= title .hashCode();
        if (center != null) code ^= center.hashCode();
        if (objectiveCRS != null) code ^= objectiveCRS.hashCode();
        if (displayCRS != null) code ^= displayCRS.hashCode();
        if (objToDisp != null) code ^= objToDisp.hashCode();
        if (dispToObj != null) code ^= dispToObj.hashCode();
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
                   Utilities.equals(this.center, that.center) &&
                   Utilities.equals(this.displayCRS, that.displayCRS) &&
                   Utilities.equals(this.objectiveCRS, that.objectiveCRS) &&
                   Utilities.equals(this.objToDisp, that.objToDisp) &&
                   Utilities.equals(this.dispToObj, that.dispToObj);
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
     * String is formed : ["TITLE", (CENTER), OBJECTIVE_CRS_NAME , DISPLAY_CRS_NAME ]
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
        if (objectiveCRS != null) {
            buffer.append(", ").append(objectiveCRS.getName()).append(' ');
        }
        if (displayCRS != null) {
            buffer.append(", ").append(displayCRS.getName()).append(' ');
        }
        return buffer.append(']').toString();
    }
    
    
}
