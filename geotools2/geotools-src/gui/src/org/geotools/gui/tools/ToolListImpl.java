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
package org.geotools.gui.tools;

import org.geotools.gui.tools.event.SelectedToolListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EventObject;
import javax.swing.event.EventListenerList;


/**
 * A list of tools provided by the MapViewer to the operator, including the
 * selectedTool which is the current tool in use.  An event is sent to
 * interested classes when the selectedTool changes.<br>
 * No event is sent if a tool is added or removed from the ToolList as the
 * ToolList is expected to be set up once at startup and not change after
 * that.
 *
 * @author Cameron Shorter
 * @version $Id: ToolListImpl.java,v 1.1 2003/05/30 12:31:28 camerons Exp $
 */
public class ToolListImpl extends ArrayList implements ToolList {
    /** The selected tool for this context. */
    private Tool selectedTool;

    /** Classes to notify if the LayerList changes */
    private EventListenerList listenerList = new EventListenerList();

    /**
     * Creates a new instance of ToolList.
     */
    public ToolListImpl() {
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
     * If the selectedTool is not in the ToolList any more, then set the
     * SelectedTool to null and trigger a SelectedTool Event.  This method is
     * called when Tools are removed from the ToolList.
     */
    private void checkSelectedToolIsAvailable() {
        if (this.contains(this.selectedTool)) {
            setSelectedTool(null);
        }
    }

    /**
     * Get the tool.
     *
     * @return The selected tool.
     */
    public Tool getSelectedTool() {
        return this.selectedTool;
    }

    /**
     * Set the tool.
     *
     * @param selectedTool The new tool.
     */
    public void setSelectedTool(Tool selectedTool) {
        if (selectedTool != this.selectedTool) {
            // Stop the old selectedTool from recieving MouseEvents
            if (this.selectedTool != null) {
                this.selectedTool.removeMouseListeners();
            }

            this.selectedTool = selectedTool;

            // Notify all listeners that the selected tool has changed.  They
            // will then ask to be listeners of the new selectedTool.
            fireSelectedToolListener();
        }
    }

    /**
     * Removes all of the elements from this collection (optional operation).
     * This collection will be empty after this method returns unless it
     * throws an exception.<br>
     * The selectedTool is set to <code>null</code>.
     */
    public void clear() {
        super.clear();
        setSelectedTool(null);
    }

    /**
     * Removes the element at the specified position in this list (optional
     * operation).  Shifts any subsequent elements to the left (subtracts one
     * from their indices).  Returns the element that was removed from the
     * list.
     *
     * @param index the index of the element to removed.
     *
     * @return the element previously at the specified position.
     */
    public Object remove(int index) {
        Object o = super.remove(index);
        checkSelectedToolIsAvailable();

        return o;
    }

    /**
     * Removes a single instance of the specified element from this collection,
     * if it is present (optional operation).  More formally, removes an
     * element <tt>e</tt> such that <tt>(o==null ?  e==null :
     * o.equals(e))</tt>, if this collection contains one or more such
     * elements.  Returns true if this collection contained the specified
     * element (or equivalently, if this collection changed as a result of the
     * call).
     *
     * @param o element to be removed from this collection, if present.
     *
     * @return <tt>true</tt> if this collection changed as a result of the call
     */
    public boolean remove(Object o) {
        boolean b = super.remove(o);
        checkSelectedToolIsAvailable();

        return b;
    }

    /**
     * Removes all this collection's elements that are also contained in the
     * specified collection (optional operation).  After this call returns,
     * this collection will contain no elements in common with the specified
     * collection.
     *
     * @param c elements to be removed from this collection.
     *
     * @return <tt>true</tt> if this collection changed as a result of the call
     *
     * @see #remove(Object)
     * @see #contains(Object)
     */
    public boolean removeAll(Collection c) {
        boolean b = super.removeAll(c);
        checkSelectedToolIsAvailable();

        return b;
    }

    /**
     * Retains only the elements in this collection that are contained in the
     * specified collection (optional operation).  In other words, removes
     * from this collection all of its elements that are not contained in the
     * specified collection.
     *
     * @param c elements to be retained in this collection.
     *
     * @return <tt>true</tt> if this collection changed as a result of the call
     *
     * @see #remove(Object)
     * @see #contains(Object)
     */
    public boolean retainAll(Collection c) {
        boolean b = super.retainAll(c);
        checkSelectedToolIsAvailable();

        return b;
    }

    /**
     * Replaces the element at the specified position in this list with the
     * specified element (optional operation).
     *
     * @param index index of element to replace.
     * @param element element to be stored at the specified position.
     *
     * @return the element previously at the specified position.
     */
    public Object set(int index, Object element) {
        Object o = super.set(index, element);
        checkSelectedToolIsAvailable();

        return o;
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
