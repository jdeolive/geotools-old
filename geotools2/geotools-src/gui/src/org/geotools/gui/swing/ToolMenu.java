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
package org.geotools.gui.swing;

import org.geotools.gui.tools.Tool;
import org.geotools.gui.tools.ToolList;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Iterator;
import java.util.logging.Logger;
import javax.swing.JMenu;
import javax.swing.JMenuItem;


/**
 * Creates a JMenu for all the tools in the ToolList.  The ToolList should be
 * initialised before constructing the ToolMenu as the ToolMenu constructs
 * itself from the ToolList parameters.
 *
 * @author Cameron Shorter
 * @version $Id: ToolMenu.java,v 1.3 2003/05/30 12:31:28 camerons Exp $
 */
public class ToolMenu extends JMenu {
    /** The class used for identifying for logging. */
    private static final Logger LOGGER = Logger.getLogger(
            "org.geotools.gui.swing.ToolMenu");

    /** The toolList which this menu is displaying */
    ToolList toolList;

    /**
     * Build a JMenu using all the name of all the Tools in the ToolList.
     *
     * @param toolList The toolList to build the menu from.
     */
    public ToolMenu(ToolList toolList) {
        this.toolList = toolList;

        setText("Tool");

        for (Iterator it = toolList.iterator(); it.hasNext();) {
            Tool tool = (Tool) it.next();
            JMenuItem menuItem = new JMenuItem();
            menuItem.setText(tool.getName());
            menuItem.addActionListener(new ToolMenuActionListener(tool));
            this.add(menuItem);
        }
    }

    /**
     * Process a tool action event by setting the selected tool in the
     * ToolList.
     *
     * @param tool The new tool that has been selected.
     */
    private void setSelectedTool(Tool tool) {
        toolList.setSelectedTool(tool);
    }

    /**
     * Process key presses on the ToolMenu items.
     */
    private class ToolMenuActionListener implements ActionListener {
        private Tool tool;

        public ToolMenuActionListener(Tool tool) {
            this.tool = tool;
        }

        public void actionPerformed(ActionEvent evt) {
            setSelectedTool(tool);
        }
    }
}
