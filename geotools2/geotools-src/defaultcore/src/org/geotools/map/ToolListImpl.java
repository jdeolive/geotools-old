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
package org.geotools.map;

import org.geotools.gui.tools.Tool;
import java.util.EventObject;
import javax.swing.event.EventListenerList;
import org.geotools.map.events.SelectedToolListener;


/**
 * The tool which will process mouse events on a MapPane.
 *
 * @author Cameron Shorter
 * @version $Id: ToolListImpl.java,v 1.4 2003/05/16 21:10:20 jmacgill Exp $
 */
public class ToolListImpl implements ToolList {
    /** The selected tool for this context. */
    private Tool tool;

    /** Classes to notify if the LayerList changes */
    private EventListenerList listenerList = new EventListenerList();

    /**
     * Creates a new instance of SelectedTool.
     *
     * @param tool The selected tool.
     */
    protected ToolListImpl(Tool tool) {
        this.tool = tool;
    }

    /**
     * Register interest in being called when Tool changes.
     *
     * @param listener The object to notify when tool changes.
     */
    public void addSelectedToolListener(SelectedToolListener listener) {
        if (listener != null) {
            listenerList.add(SelectedToolListener.class, listener);
        }
    }

    /**
     * Remove interest in bening notified when Tool changes.
     *
     * @param listener The listener.
     */
    public void removeSelectedToolListener(SelectedToolListener listener) {
        listenerList.remove(SelectedToolListener.class, listener);
    }

    /**
     * Send events to all SelectedToolListeners.
     */
    protected void fireSelectedToolListener() {
        // Guaranteed to return a non-null array
        Object[] listeners = listenerList.getListenerList();

        // Process the listeners last to first, notifying
        // those that are interested in this event
        EventObject ece = new EventObject(this);

        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] == SelectedToolListener.class) {
                ((SelectedToolListener) listeners[i + 1]).selectedToolChanged(ece);
            }
        }
    }

    /**
     * Get the tool.
     *
     * @return The selected tool.
     */
    public Tool getTool() {
        return this.tool;
    }

    /**
     * Set the tool.
     *
     * @param tool The new tool.
     *
     * @throws IllegalArgumentException if an argument is <code>null</code>.
     */
    public void setTool(Tool tool) throws IllegalArgumentException {
        if (tool != this.tool) {
            // Stop the old tool from recieving MouseEvents
            if (this.tool != null) {
                this.tool.removeMouseListeners();
            }

            this.tool = tool;

            // Notify all listeners that the selected tool has changed.  They
            // will then ask to be listeners of the new tool.
            fireSelectedToolListener();
        }
    }

    //    /** Get the title of this layer.  If title has not been defined then an
    //     * empty string is returned.
    //     * @return The title of this layer.
    //     */ 
    //    public String getTitle() {
    //        if (title==null){
    //            return new String("");
    //        }else{
    //            return title;
    //        }
    //    }
    //
    //    /** Set the title of this layer.
    //     * @title The title of this layer.
    //     */ 
    //    public void setTitle(String title) {
    //        this.title = title;
    //    }
    //
    //    /** Return the title of this layer.  If no title has been defined, then
    //     * the class name is returned.
    //     */
    //    public String toString() {
    //        if (title==null){
    //            return super.toString();
    //        }else{
    //            return title;
    //        }
    //    }
}
