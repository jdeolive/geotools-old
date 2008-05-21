/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002-2007, GeoTools Project Managment Committee (PMC)
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
package org.geotools.gui.swing.map.map2d.control;

import java.awt.Dimension;

import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JToolBar;
import javax.swing.border.EmptyBorder;

import org.geotools.gui.swing.icon.IconBundle;
import org.geotools.gui.swing.map.map2d.Map2D;
import org.geotools.gui.swing.map.map2d.StreamingMap2D;

/**
 * JMap2DEditBar is a JPanel to handle edition state for an EditableMap2D
 * Layer selection, edition, line, polygon, point ...
 * 
 * @author johann sorel
 */
public class JMap2DEditBar extends JToolBar {

    private static final ImageIcon ICON_EDIT = IconBundle.getResource().getIcon("16_edit");
    
    private final EditAction ACTION_EDIT = new EditAction();
    
    private StreamingMap2D map = null;
    private final JButton gui_edit = buildButton(ICON_EDIT, ACTION_EDIT);
    private final EditedLayerChooser gui_chooser = new EditedLayerChooser();
    private final EditHandlerChooser gui_handler = new EditHandlerChooser();
    private final int largeur = 2;
    
    

    /**
     * Creates a new instance of JMap2DEditBar
     */
    public JMap2DEditBar() {
        this(null);
    }

    /**
     * Creates a new instance of JMap2DEditBar
     * @param pane : related Map2D or null
     */
    public JMap2DEditBar(StreamingMap2D pane) {
        setMap(pane);
        init();
    }

    private void init() {
        add(gui_edit);
        add(gui_chooser);
        add(gui_handler);
    }


    private JButton buildButton(ImageIcon img,Action action) {
        JButton but = new JButton(action);
        but.setIcon(img);
        but.setBorder(new EmptyBorder(largeur, largeur, largeur, largeur));
        but.setBorderPainted(false);
        but.setContentAreaFilled(false);
        but.setPreferredSize(new Dimension(25, 25));
        but.setOpaque(false);
        return but;
    }
    
    
    /**
     * set the related Map2D
     * @param map2d : related Map2D
     */
    public void setMap(StreamingMap2D map2d) {

        map = map2d;
        ACTION_EDIT.setMap(map);
        gui_chooser.setMap(map2d);
        gui_handler.setMap(map2d);
    }

}
