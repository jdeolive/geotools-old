package org.geotools.gui.widget;

/**
 * A widget is a component which can be added to a display.  Typical widgets
 * include MapPane, Legend, PanButton, etc.
 * @version $Id: AbstractWidget.java,v 1.2 2003/02/04 19:12:54 camerons Exp $
 * @author Cameron Shorter
 */
public abstract interface AbstractWidget {
    /**
     * Adds the specified mouse listener to receive mouse events from
     * this component.
     * If listener <code>l</code> is <code>null</code>,
     * no exception is thrown and no action is performed.
     *
     * @param    l   the mouse listener
     * @see      java.awt.event.MouseEvent
     * @see      java.awt.event.MouseListener
     * @see      #removeMouseListener
     * @see      #getMouseListeners
     * @since    JDK1.1
     * @task REVIST Can classes in the core module depend on awt events?
     */
    public void addMouseListener(Object l);

    /**
     * Removes the specified mouse listener so that it no longer
     * receives mouse events from this component. This method performs 
     * no function, nor does it throw an exception, if the listener 
     * specified by the argument was not previously added to this component.
     * If listener <code>l</code> is <code>null</code>,
     * no exception is thrown and no action is performed.
     *
     * @param    l   the mouse listener
     * @see      java.awt.event.MouseEvent
     * @see      java.awt.event.MouseListener
     * @see      #addMouseListener
     * @see      #getMouseListeners
     * @since    JDK1.1
     * @task REVIST Can classes in the core module depend on awt events?
     */
    public void removeMouseListener(Object l);

    /**
     * Returns the current width of this component.
     * @return the current width of this component
     */
    public int getWidth();
}