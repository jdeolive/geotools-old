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
package org.geotools.gui.widget;

/**
 * A widget is a component which can be added to a display.  Typical widgets
 * include MapPane, Legend, PanButton, etc.
 *
 * @author Cameron Shorter
 * @version $Id: Widget.java,v 1.3 2003/08/20 21:37:58 cholmesny Exp $
 *
 * @deprecated
 */
public interface Widget {
    /**
     * Adds the specified mouse listener to receive mouse events from this
     * component. If listener <code>l</code> is <code>null</code>, no
     * exception is thrown and no action is performed.
     *
     * @param l the mouse listener
     *
     * @task REVIST Can classes in the core module depend on awt events?
     *
     * @see java.awt.event.MouseEvent
     * @see java.awt.event.MouseListener
     * @see #removeMouseListener
     * @see #getMouseListeners
     * @since JDK1.1
     */
    void addMouseListener(Object l);

    /**
     * Removes the specified mouse listener so that it no longer receives mouse
     * events from this component. This method performs  no function, nor does
     * it throw an exception, if the listener  specified by the argument was
     * not previously added to this component. If listener <code>l</code> is
     * <code>null</code>, no exception is thrown and no action is performed.
     *
     * @param l the mouse listener
     *
     * @task REVIST Can classes in the core module depend on awt events?
     *
     * @see java.awt.event.MouseEvent
     * @see java.awt.event.MouseListener
     * @see #addMouseListener
     * @see #getMouseListeners
     * @since JDK1.1
     */
    void removeMouseListener(Object l);

    /**
     * Returns the current width of this component.
     *
     * @return the current width of this component
     */
    int getWidth();
}
