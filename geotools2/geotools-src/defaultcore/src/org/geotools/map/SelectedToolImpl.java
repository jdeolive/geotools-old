/*
 *    Geotools - OpenSource mapping toolkit
 *    (C) 2002, Centre for Computational Geography
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
 *    You should have received a copy of the GNU Lesser General Public
 *    License along with this library; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 */

package org.geotools.map;

import java.util.EventListener;
import java.util.EventObject;
import javax.swing.event.EventListenerList;
import org.geotools.gui.tools.AbstractTool;
import org.geotools.map.events.SelectedToolListener;

/**
 * The tool which will process mouse events on a MapPane.
 * @version $Id: SelectedToolImpl.java,v 1.6 2003/03/28 19:08:43 camerons Exp $
 * @author  Cameron Shorter
 */

public class SelectedToolImpl implements SelectedTool
{

    /**
     * The selected tool for this context.
     */
    private AbstractTool tool;
    
    /** Classes to notify if the LayerList changes */
    private EventListenerList listenerList = new EventListenerList();

    /**
     * Creates a new instance of SelectedTool.
     * @param tool The selected tool.
     */
    protected SelectedToolImpl(AbstractTool tool)
        throws IllegalArgumentException
    {
        this.tool=tool;
    }

    /**
     * Register interest in being called when Tool changes.
     * @param listener The object to notify when tool changes.
     * @param sendEvent After registering this listener, send a changeEvent
     * to all listeners.
     */
    public void addSelectedToolListener(
            SelectedToolListener listener)
    {
        if (listener!=null){
            listenerList.add(SelectedToolListener.class, listener);
        }
    }

    /**
     * Remove interest in bening notified when Tool changes.
     * @param listener The listener.
     */
    public void removeSelectedToolListener(SelectedToolListener listener)
    {
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
                ((SelectedToolListener)
                    listeners[i + 1]).selectedToolChanged(ece);
            }
        }
    }
    
    /**
     * Get the tool.
     * @return The selected tool.
     */
    public AbstractTool getTool(){
        return this.tool;
    }

    /**
     * Set the tool.
     * @param tool The new tool.
     * @throws IllegalArgumentException if an argument is <code>null</code>.
     */
    public void setTool(AbstractTool tool) throws IllegalArgumentException
    {
        if (tool!=this.tool){
            this.tool=tool;
            // Stop the old tool from recieving MouseEvents
            if (this.tool!=null){
                this.tool.removeMouseListeners();
            }
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
