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

import java.awt.Component;
import java.awt.Cursor;

import org.geotools.map.MapContext;


/**
 * Base class for all the geotools Tools, like PanTool, ZoomTool, etc. Tools
 * process mouse events on behalf of widgets like MapPane and change data in
 * the Context (like the AreaOfInterest).
 *
 * @author Cameron Shorter
 * @version $Id: Tool.java,v 1.6 2003/12/23 17:21:02 aaime Exp $
 */
public abstract interface Tool {
    /**
     * Register this tool to receive Mouse Events from <code>component</code>.
     * The events may be MouseEvents or MouseMotionEvents or both depending on
     * the Tool which implements this interface.
     *
     * @param component The tool will process mouseEvents from this component.
     * @param context The Context that will be changed by this Tool.
     *
     * @throws IllegalArgumentException if an argument is <code>null</code> or
     *         the tool is being assigned a different context to before.
     */
    void addMouseListener(Component component, MapContext context)
        throws IllegalArgumentException;

    /**
     * Remove all Mouse Listeners from this tool.  This method should be called
     * when this tool is deselected from a MapPane.
     */
    void removeMouseListeners();

    /**
     * Get the context.  If context has not been set yet, then null is
     * returned.
     *
     * @return The context of this tool.
     */
    MapContext getContext();

    /**
     * Set the name for the tool, eg "Zoom In", "Zoom Out", "Pan".
     *
     * @param name what to call this tool.
     */
    void setName(String name);

    /**
     * Get the name of the tool.
     *
     * @return The name of this tool.
     */
    String getName();

    /**
     * Return the prefered cursor for this tool.
     *
     * @return cursor The prefered cursor for this tool.
     */
    Cursor getCursor();

    /**
     * Set the cursor for this Tool.
     *
     * @param cursor The cursor to associate with this tool.
     */
    void setCursor(Cursor cursor);

    /**
     * Clean up this class.
     */
    void destroy();
}
