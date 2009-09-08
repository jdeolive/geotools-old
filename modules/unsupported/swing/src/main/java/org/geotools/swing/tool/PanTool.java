/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 *
 *    (C) 2002-2008, Open Source Geospatial Foundation (OSGeo)
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
import java.awt.Point;
import java.awt.Toolkit;
import java.util.ResourceBundle;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import org.geotools.swing.JMapPane;
import org.geotools.swing.event.MapMouseEvent;

/**
 * A map panning tool for JMapPane.  Allows the user to drag the map
 * with the mouse.
 * 
 * @author Michael Bedward
 * @since 2.6
 * @source $URL$
 * @version $Id$
 */
public class PanTool extends CursorTool {
    
    private static final ResourceBundle stringRes = ResourceBundle.getBundle("org/geotools/swing/widget");

    public static final String TOOL_NAME = stringRes.getString("tool_name_pan");
    public static final String TOOL_TIP = stringRes.getString("tool_tip_pan");
    public static final String CURSOR_IMAGE = "/org/geotools/swing/icons/pan_cursor_32.gif";
    public static final Point CURSOR_HOTSPOT = new Point(15, 15);

    public static final String ICON_IMAGE = "/org/geotools/swing/icons/mActionPan.png";
    
    private Cursor cursor;
    private Icon icon;

    private Point panePos;
    boolean panning;
    
    /**
     * Constructor
     *
     * @param pane the map pane that this tool is to work with
     */
    public PanTool(JMapPane pane) {
        setMapPane(pane);
        icon = new ImageIcon(getClass().getResource(ICON_IMAGE));

        Toolkit tk = Toolkit.getDefaultToolkit();
        ImageIcon imgIcon = new ImageIcon(getClass().getResource(CURSOR_IMAGE));
        cursor = tk.createCustomCursor(imgIcon.getImage(), CURSOR_HOTSPOT, TOOL_NAME);

        panning = false;
    }

    /**
     * Respond to a mouse button press event from the map pane. This may
     * signal the start of a mouse drag. Records the event's window position.
     */
    @Override
    public void onMousePressed(MapMouseEvent pme) {
        panePos = pme.getPoint();
        panning = true;
    }

    /**
     * Respond to a mouse dragged event. Calls {@link org.geotools.swing.JMapPane#moveImage()}
     */
    @Override
    public void onMouseDragged(MapMouseEvent pme) {
        if (panning) {
            Point pos = pme.getPoint();
            if (!pos.equals(panePos)) {
                pane.moveImage(pos.x - panePos.x, pos.y - panePos.y);
                panePos = pos;
            }
        }
    }

    /**
     * If this button release is the end of a mouse dragged event, requests the
     * map pane to repaint the display
     */
    @Override
    public void onMouseReleased(MapMouseEvent pme) {
        panning = false;
        pane.repaint();
    }

    /**
     * Get the name assigned to this tool
     * @return "Pan"
     */
    @Override
    public String getName() {
        return TOOL_NAME;
    }

    /**
     * Get the mouse cursor for this tool
     */
    public Cursor getCursor() {
        return cursor;
    }
    
    /**
     * Get the 24x24 pixel icon for this tool
     */
    public Icon getIcon() {
        return icon;
    }

    /**
     * Returns false to indicate that this tool does not draw a box
     * on the map display when the mouse is being dragged
     */
    @Override
    public boolean drawDragBox() {
        return false;
    }
}
