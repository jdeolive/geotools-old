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
/*
 * LegendTreeStyleElementCellRenderer.java
 *
 * Created on 07 July 2003, 21:29
 */
package org.geotools.gui.swing.legend;

import java.util.logging.Logger;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;


/**
 * Rule Cell Renderer, the userObject got from a rule node will generate an icon
 * for being set in the renderer as the rule icon
 *
 * @author jianhuij
 */
public class LegendTreeRuleCellRenderer
    extends javax.swing.tree.DefaultTreeCellRenderer {
    /** The logger for the Legend module. */
    private static final Logger LOGGER = Logger.getLogger(
            "org.geotools.gui.swing.legend");

    public java.awt.Component getTreeCellRendererComponent(
        javax.swing.JTree tree, Object value, boolean selected,
        boolean expanded, boolean leaf, int row, boolean hasFocus) {
        DefaultTreeCellRenderer renderer = (DefaultTreeCellRenderer) super.getTreeCellRendererComponent(tree,
                value, selected, expanded, leaf, row, hasFocus);
        LegendRuleNodeInfo userObject = (LegendRuleNodeInfo) ((DefaultMutableTreeNode) value).getUserObject();
        renderer.setName(userObject.toString());
        renderer.setIcon(userObject.getIcon());

        return renderer;
    }
}
