package org.geotools.gui.tools;

import java.awt.Component;
import java.awt.Cursor;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.geotools.map.MapContext;

/**
 * Base class for all the geotools Tools, like PanTool, ZoomTool, etc.
 * Tools process mouse events on behalf of widgets like MapPane and change
 * data in the Context (like the AreaOfInterest).
 * @version $Id: AbstractTool.java,v 1.4 2003/12/23 17:21:02 aaime Exp $
 * @author Cameron Shorter
 */
public abstract class AbstractTool
    implements Tool,MouseListener,MouseMotionListener
{
     /** The widgets that use this Tool for MouseEvents */
    private List mouseListenerList=
        Collections.synchronizedList(new ArrayList());
    
    /** The widgets that use this Tool for MouseMotionEvents */
    private List mouseMotionListenerList=
        Collections.synchronizedList(new ArrayList());
    
    /** The name of the tool, eg "Zoom In" */
    private String name="Tool";

    /**
     * A tool is associated with only one context.  The context stores all data
     * about a mapping model.  This tool will change data in context classes.
     */
    protected MapContext context;
    
    /** The cursor associated with this tool. */
    private Cursor cursor = new Cursor(Cursor.DEFAULT_CURSOR);

    /**
     * Register this tool to receive MouseEvents from <code>component<code>.
     * @param component The tool will process mouseEvents from this component.
     * @param context The Context that will be changed by this Tool.
     * @param listener The tool to send mouseEvents to, usually the child of
     * this class.
     * @throws IllegalArgumentException if an argument is <code>null</code>
     * or the tool is being assigned a different context to before.
     */
    protected void addMouseListener(
        Component component,
        MapContext context,
        MouseListener listener)
    {
        if ((component==null) || (context==null)
            || ((this.context!=null)&&(this.context!=context)))
        {
            throw new IllegalArgumentException();
        } else {
            this.context=context;
            mouseListenerList.add(component);
            component.addMouseListener(listener);
        }
    }
    
    /**
     * Register this tool to receive MouseMotionEvents from
     * <code>component<code>.
     * @param component The tool will process mouseMotionEvents from this
     * component.
     * @param listener The tool to send mouseMotionEvents to, usually the child
     * of his class.
     * @throws IllegalArgumentException if an argument is <code>null</code>
     * or the tool is being assigned a different context to before.
     */
    protected void addMouseMotionListener(
        Component component,
        MapContext context,
        MouseMotionListener listener) throws IllegalArgumentException
    {
        if ((component==null) || (context==null)
            || ((this.context!=null)&&(this.context!=context)))
        {
            throw new IllegalArgumentException();
        } else {
            this.context=context;
            mouseMotionListenerList.add(component);
            component.addMouseMotionListener(listener);
        }
    }
    
    /**
     * Remove all Mouse Listeners from this tool.  This method should be called
     * when this tool is deselected from a MapPane.
     */
    public void removeMouseListeners(){
        
        // remove mouseListeners
        for (Iterator i=mouseListenerList.iterator();i.hasNext();)
        {
            Component component=(Component)i.next();
            MouseListener[] listeners=component.getMouseListeners();
            for (int j=0;j<listeners.length;j++){
                component.removeMouseListener(listeners[j]);
            }
        }
        mouseListenerList.clear();

        // remove mouseMotionListeners
        for (Iterator i=mouseMotionListenerList.iterator();i.hasNext();)
        {
            Component component=(Component)i.next();
            MouseMotionListener[] listeners=component.getMouseMotionListeners();
            for (int j=0;j<listeners.length;j++){
                component.removeMouseMotionListener(listeners[j]);
            }
        }
        mouseMotionListenerList.clear();
    }

    /**
     * Get the context.  If context has not been set yet, then null is returned.
     * @param The context which stores the state data.
     */
    public MapContext getContext(){
        return context;
    }
    
    /**
     * Return the prefered cursor for this tool.
     * @return cursor The prefered cursor for this tool.
     */
    public Cursor getCursor(){
        return this.cursor;
    }
    
    /**
     * Set the cursor for this Tool.
     * @param cursor The cursor to associate with this tool.
     */
    public void setCursor(Cursor cursor) {
        this.cursor=cursor;
    }
    
    /** Invoked when the mouse button has been clicked (pressed
     * and released) on a component.
     *
     */
    public void mouseClicked(MouseEvent e) {
    }
    
    /** Invoked when the mouse enters a component.
     *
     */
    public void mouseEntered(MouseEvent e) {
    }
    
    /** Invoked when the mouse exits a component.
     *
     */
    public void mouseExited(MouseEvent e) {
    }
    
    /** Invoked when a mouse button has been pressed on a component.
     *
     */
    public void mousePressed(MouseEvent e) {
    }
    
    /** Invoked when a mouse button has been released on a component.
     *
     */
    public void mouseReleased(MouseEvent e) {
    }

    /** Clean up this class.
     *
     */
    public void destroy() {
        removeMouseListeners();
    }
    
    /** Invoked when a mouse button is pressed on a component and then
     * dragged.  <code>MOUSE_DRAGGED</code> events will continue to be
     * delivered to the component where the drag originated until the
     * mouse button is released (regardless of whether the mouse position
     * is within the bounds of the component).
     * <p>
     * Due to platform-dependent Drag&Drop implementations,
     * <code>MOUSE_DRAGGED</code> events may not be delivered during a native
     * Drag&Drop operation.
     *
     */
    public void mouseDragged(MouseEvent e) {
    }
    
    /** Invoked when the mouse button has been moved on a component
     * (with no buttons down).
     *
     */
    public void mouseMoved(MouseEvent e) {
    }
    
    /**
     * Set the name for the tool, eg "Zoom In", "Zoom Out", "Pan".
     */
    public void setName(String name) {
        this.name=name;
    }
    
    /**
     * Get the name of the tool.
     */
    public String getName() {
        return name;
    }
    
    /**
     * Return the name of the tool.
     */
    public String toString() {
        return getName();
    }
    
}
