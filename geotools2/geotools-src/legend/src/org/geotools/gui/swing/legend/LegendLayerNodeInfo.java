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

import org.geotools.map.MapLayer;


/**
 * DOCUMENT ME!
 *
 * @author jianhuij
 */
/**
 * it is used to keep the information about legend tree root and layer node
 * such as name, expanded icon, collapsed icon, and isSelected when mean
 * isLayerVisible or does user select the checkbox for this layer node
 */
public class LegendLayerNodeInfo extends LegendNodeInfo {
    /** The logger for the Legend module. */
    private static final Logger LOGGER = Logger.getLogger(
            "org.geotools.gui.swing.legend");

    /** is this note selected */
    private boolean selected = true;

    /** if the note is a layer, if it is null, this is a root node */
    private MapLayer layer = null;

    /**
     * a new instance of LegendLayerNoteObject if layer is null, then this is a
     * root node by name for this note and a boolean parameter to indicate if
     * the layer is selected or visible.
     *
     * @param name DOCUMENT ME!
     * @param isLayerSelected DOCUMENT ME!
     * @param layer DOCUMENT ME!
     */
    public LegendLayerNodeInfo(String name, boolean isLayerSelected, MapLayer layer) {
        setSelected(isLayerSelected);
        setName(name);
        setMapLayer(layer);
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public boolean isSelected() {
        return selected;
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

    public Icon getIcon(boolean expanded) {
        Icon i = null;
//        if (expanded) {
//            i = UIManager.getIcon("Tree.openIcon");
//        } else {
//            i = UIManager.getIcon("Tree.closedIcon");
//        }
        return null;
    }

    public void setMapLayer(MapLayer layer) {
        this.layer = layer;
    }

    /**
     * if the note is a layer, if returns null, this is a root node
     *
     * @return DOCUMENT ME!
     */
    public MapLayer getMapLayer() {
        return this.layer;
    }

    public String toString() {
        return getName();
    }
}
