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

import java.util.List;

import org.geotools.gui.tools.event.SelectedToolListener;


/**
 * A list of tools provided by the MapViewer to the operator, including the
 * selectedTool which is the current tool in use.  An event is sent to
 * interested classes when the selectedTool changes.<br>
 * No event is sent if a tool is added or removed from the ToolList as the
 * ToolList is expected to be set up once at startup and not change after
 * that.
 *
 * @author Cameron Shorter
 * @version $Id: ToolList.java,v 1.3 2003/08/20 21:32:13 cholmesny Exp $
 */
public interface ToolList extends List {
    /**
     * Register interest in being called when Tool changes.
     *
     * @param listener The object to notify when tool changes.
     */
    void addSelectedToolListener(SelectedToolListener listener);

    /**
     * Remove interest in bening notified when Tool changes.
     *
     * @param listener The listener.
     */
    void removeSelectedToolListener(SelectedToolListener listener);

    /**
     * Get the SelectedTool.  Null will be returned if there is no
     * selectedTool.
     *
     * @return The SelectedTool.
     */
    Tool getSelectedTool();

    /**
     * Set the SelectedTool.
     *
     * @param tool The new SelectedTtool.
     */
    void setSelectedTool(Tool tool);
}
