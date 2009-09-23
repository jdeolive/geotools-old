/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 *
 *    (C) 2003-2008, Open Source Geospatial Foundation (OSGeo)
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

package org.geotools.swing.tool;

import java.awt.Cursor;
import java.awt.Toolkit;
import java.util.ResourceBundle;
import javax.swing.Icon;
import org.geotools.swing.JMapPane;
import org.geotools.swing.event.MapMouseAdapter;

/**
 * The base class for map mapPane cursor tools. Simply adds a getCursor
 * method to the MapToolAdapter
 * 
 * @author Michael Bedward
 * @since 2.6
 * @source $URL$
 * @version $Id$
 */
public abstract class CursorTool extends MapMouseAdapter {

    private static final ResourceBundle stringRes = ResourceBundle.getBundle("org/geotools/swing/widget");

    /**
     * Used with tool constructors to specify that the GUI control
     * (e.g. JButton) should not display an icon for this tool
     */
    public static final int NO_ICON = 0;

    /**
     * Used with tool constructors to specify that the GUI control
     * (e.g. JButton) display an icon for this tool
     */
    public static final int HAS_ICON = 1;

    protected JMapPane mapPane;

    /**
     * Set the map mapPane that this cursor tool is associated with
     * @param mapPane the map mapPane
     * @throws IllegalArgumentException if mapPane is null
     */
    public void setMapPane(JMapPane pane) {
        if (pane == null) {
            throw new IllegalArgumentException(stringRes.getString("arg_null_error"));
        }

        this.mapPane = pane;
    }

    /**
     * Get the name assigned to this tool (e.g. "Zoom in")
     */
    public abstract String getName();

    /**
     * Get the icon for this tool to be used with JButtons
     */
    public Icon getIcon() {
        return null;
    }

    /**
     * Get the cursor for this tool.
     */
    public Cursor getCursor() {
        return Cursor.getDefaultCursor();
    }

    /**
     * Query if the tool is one that draws a box on the map display
     * when the mouse is being dragged (eg. to indicate a zoom area).
     */
    public boolean drawDragBox() {
        return false;
    }
}
