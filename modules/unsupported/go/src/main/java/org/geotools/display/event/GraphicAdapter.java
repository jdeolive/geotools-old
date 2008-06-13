/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 *
 *    (C) 2005-2008, Open Source Geospatial Foundation (OSGeo)
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
package org.geotools.display.event;

// OpenGIS dependencies
import org.opengis.go.display.primitive.Graphic;    // For javadoc
import org.opengis.go.display.event.GraphicChangeEvent;
import org.opengis.go.display.event.GraphicListener;
import org.opengis.go.display.event.GraphicMouseEvent;


/**
 * An abstract adapter class for receiving graphic events. The methods in this class are empty.
 * This class exists as convenience for creating listener objects. Extend this class to create
 * a graphic event listener and override the methods for the events of interest. 
 *
 * @since 2.3
 * @version $Id$
 * @source $URL$
 * @author Martin Desruisseaux (IRD)
 */
public abstract class GraphicAdapter implements GraphicListener {
    /**
     * Creates a new instance of {@code GraphicAdapter}.
     */
    protected GraphicAdapter() {
    }

    /**
     * Invoked when the mouse has been clicked on a {@link Graphic}.
     */
    public void mouseClicked(GraphicMouseEvent ge) {
    }

    /**
     * Invoked when a mouse button has been pressed on a {@link Graphic}.
     */
    public void mousePressed(GraphicMouseEvent ge) {
    }

    /**
     * Invoked when a mouse button has been released on a {@link Graphic}.
     */
    public void mouseReleased(GraphicMouseEvent ge) {
    }

    /**
     * Invoked when the mouse dwells on a {@link Graphic}.  Dwelling 
     * occurs after a {@code mouseEntered} event transpires and only
     * after {@code mouseMoved} events have ceased for an arbitrary
     * length of time.  
     */
    public void mouseDwelled(GraphicMouseEvent ge) {
    }

    /**
     * Invoked when a {@link Graphic} is selected, either programmatically
     * or through a mouse gesture.
     */
    public void graphicSelected(GraphicChangeEvent ge) {
    }

    /**
     * Invoked when a {@link Graphic} is deselected, either 
     * programmatically or through a mouse gesture.
     */
    public void graphicDeselected(GraphicChangeEvent ge) {
    }

    /**
     * Invoked when a {@link Graphic} is disposed.
     */
    public void graphicDisposed(GraphicChangeEvent ge) {
    }

    /**
     * Invoked when a {@link Graphic} is put into an editable state.
     */
    public void graphicEditableStart(GraphicChangeEvent ge) {
    }

    /**
     * Invoked when a {@link Graphic} is edited by a GUI user.
     */
    public void graphicEditableChanged(GraphicChangeEvent ge) {
    }

    /**
     * Invoked when a {@link Graphic} is no longer in an editable state.
     */
    public void graphicEditableEnd(GraphicChangeEvent ge) {
    }

    /**
     * Invoked when a {@link Graphic} changes in any way, other than editing.
     */
    public void graphicChanged(GraphicChangeEvent ge) {
    }
}
