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
 * LegendTreeCellRendererSwitcher.java
 *
 * Created on 07 July 2003, 21:06
 */
package org.geotools.gui.swing.legend;

import java.util.logging.Logger;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeCellRenderer;


/**
 * Three kinds of nodes, but only two kinds of rendering require in a legendTree layer and root
 * node using LegendTreeLayerCellRenderer,  Rule use LegendTreeRuleCellRenderer
 *
 * @author jianhuij
 */
public class LegendTreeCellRendererSwitcher implements javax.swing.tree.TreeCellRenderer {
    /** The logger for the Legend module. */
    private static final Logger LOGGER = Logger.getLogger("org.geotools.gui.swing.legend");
    LegendTreeRuleCellRenderer ruleRenderer = new LegendTreeRuleCellRenderer();
    LegendTreeLayerCellRenderer layerRenderer = new LegendTreeLayerCellRenderer();
    DefaultTreeCellRenderer rootRenderer = new DefaultTreeCellRenderer();

    public java.awt.Component getTreeCellRendererComponent(javax.swing.JTree tree, Object value,
        boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
        rootRenderer.setLeafIcon(null);
        rootRenderer.setClosedIcon(null);
        rootRenderer.setOpenIcon(null);
        
        TreeCellRenderer tcr = rootRenderer;
        if (((DefaultMutableTreeNode) value).getUserObject() instanceof LegendRuleNodeInfo) {
            tcr = ruleRenderer;
        } else if (((DefaultMutableTreeNode) value).getUserObject() instanceof LegendLayerNodeInfo) {
            tcr = layerRenderer;
        }

        return tcr.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);
    }
}
