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
package org.geotools.gui.swing.legend;

import java.awt.Color;
import java.util.logging.Logger;

import javax.swing.Icon;

import org.geotools.map.MapContext;


/**
 * stores Context Object reference and others
 *
 * @author jianhuij
 */
public class LegendRootNodeInfo extends LegendNodeInfo {
    /** The logger for the Legend module. */
    private static final Logger LOGGER = Logger.getLogger(
            "org.geotools.gui.swing.legend.LegendRootNodeInfo");

    /** is this note selected */
    private boolean selected = true;

    /** if the note is root node, then this object will get a context */
    private MapContext context = null;

    /**
     * Creates a new instance of LegendRootNoteInfo
     *
     * @param context DOCUMENT ME!
     * @param name DOCUMENT ME!
     * @param selected DOCUMENT ME!
     */
    public LegendRootNodeInfo(MapContext context, String name, boolean selected) {
        setMapContext(context);
        setName(name);
        setSelected(selected);
    }

    public Color getBackground(boolean selected) {
        Color c = null;

        if (selected) {
            c = new Color(204, 204, 255);
        } else {
            c = new Color(255, 255, 255);
        }

        return c;
    }

    public MapContext getMapContext() {
        return context;
    }

    public void setMapContext(MapContext context) {
        this.context = context;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public boolean isSelected() {
        return selected;
    }

    public String toString() {
        return getName();
    }

    /**
     * @see org.geotools.gui.swing.legend.LegendNodeInfo#getIcon(boolean)
     */
    public Icon getIcon(boolean selected) {
        return null;
    }
}
