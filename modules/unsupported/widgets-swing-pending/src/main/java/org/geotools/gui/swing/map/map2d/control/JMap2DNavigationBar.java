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

/**
 * JMap2DControlBar is a JPanel to handle Navigation state for a NavigableMap2D
 * ZoomIn/Out, pan, selection, refresh ...
 * 
 * @author johann sorel
 */
public class JMap2DNavigationBar extends JToolBar {

    
    
    private static final ImageIcon ICON_ZOOM_ALL = IconBundle.getResource().getIcon("16_zoom_all");
    private static final ImageIcon ICON_NEXT = IconBundle.getResource().getIcon("16_next_maparea");
    private static final ImageIcon ICON_PREVIOUS = IconBundle.getResource().getIcon("16_previous_maparea");
    private static final ImageIcon ICON_ZOOM_IN = IconBundle.getResource().getIcon("16_zoom_in");
    private static final ImageIcon ICON_ZOOM_OUT = IconBundle.getResource().getIcon("16_zoom_out");
    private static final ImageIcon ICON_ZOOM_PAN = IconBundle.getResource().getIcon("16_zoom_pan");
    private static final ImageIcon ICON_REFRESH = IconBundle.getResource().getIcon("16_data_reload");
    
    private final ZoomAllAction ACTION_ZOOM_ALL = new ZoomAllAction();
    private final NextAreaAction ACTION_NEXT = new NextAreaAction();
    private final PreviousAreaAction ACTION_PREVIOUS = new PreviousAreaAction();
    private final ZoomInAction ACTION_ZOOM_IN = new ZoomInAction();
    private final ZoomOutAction ACTION_ZOOM_OUT = new ZoomOutAction();
    private final PanAction ACTION_ZOOM_PAN = new PanAction();
    private final RefreshAction ACTION_REFRESH = new RefreshAction();
    
    
    private Map2D map = null;
    private final JButton gui_zoomAll = buildButton(ICON_ZOOM_ALL, ACTION_ZOOM_ALL);
    private final JButton gui_nextArea = buildButton(ICON_NEXT, ACTION_NEXT);
    private final JButton gui_previousArea = buildButton(ICON_PREVIOUS, ACTION_PREVIOUS);
    private final JButton gui_zoomIn = buildButton(ICON_ZOOM_IN, ACTION_ZOOM_IN);
    private final JButton gui_zoomOut = buildButton(ICON_ZOOM_OUT, ACTION_ZOOM_OUT);
    private final JButton gui_zoomPan = buildButton(ICON_ZOOM_PAN, ACTION_ZOOM_PAN);
    private final JButton gui_refresh = buildButton(ICON_REFRESH, ACTION_REFRESH);
    private final int largeur = 2;

    /**
     * Creates a new instance of JMap2DControlBar
     */
    public JMap2DNavigationBar() {
        this(null);
    }

    /**
     * Creates a new instance of JMap2DControlBar
     * @param pane : related Map2D or null
     */
    public JMap2DNavigationBar(Map2D pane) {
        setMap(pane);
        init();
    }

    private void init() {
        add(gui_zoomAll);
        add(gui_refresh);
        add(gui_previousArea);
        add(gui_nextArea);
        add(gui_zoomIn);
        add(gui_zoomOut);
        add(gui_zoomPan);
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
    public void setMap(Map2D map2d) {
        map = map2d;        
        ACTION_NEXT.setMap(map);
        ACTION_PREVIOUS.setMap(map);
        ACTION_REFRESH.setMap(map);
        ACTION_ZOOM_ALL.setMap(map);
        ACTION_ZOOM_IN.setMap(map);
        ACTION_ZOOM_OUT.setMap(map);
        ACTION_ZOOM_PAN.setMap(map);        
    }
}
