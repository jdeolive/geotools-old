/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2006, GeoTools Project Managment Committee (PMC)
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

package org.geotools.gui.swing.map.map2d.decoration;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import org.geotools.gui.swing.icon.IconBundle;
import org.geotools.gui.swing.map.map2d.Map2D;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;

/**
 *
 * @author Johann Sorel
 */
public class NavigationDecoration extends JPanel implements MapDecoration{

    private Map2D map = null;
    private int ratio = 10;
    
    public JButton gui_east = new JButton(IconBundle.getResource().getIcon("CP32_actions_1rightarrow"));
    public JButton gui_north = new JButton(IconBundle.getResource().getIcon("CP32_actions_1uparrow"));
    public JButton gui_south = new JButton(IconBundle.getResource().getIcon("CP32_actions_1downarrow"));
    public JButton gui_west = new JButton(IconBundle.getResource().getIcon("CP32_actions_1leftarrow"));
    
//    public JButton gui_ne = new JButton(IconBundle.getResource().getIcon("CP32_actions_1nearrow"));
//    public JButton gui_nw = new JButton(IconBundle.getResource().getIcon("CP32_actions_1nwarrow"));
//    public JButton gui_se = new JButton(IconBundle.getResource().getIcon("CP32_actions_1searrow"));
//    public JButton gui_sw = new JButton(IconBundle.getResource().getIcon("CP32_actions_1swarrow"));
    
    public NavigationDecoration(){
        setLayout(new BorderLayout());
        setOpaque(false);
        
        JPanel pan_north = new JPanel(new BorderLayout());        
        JPanel pan_south = new JPanel(new BorderLayout());
        JPanel pan_east = new JPanel();
        JPanel pan_west = new JPanel();
        JPanel flow_north = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JPanel flow_south = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JPanel flow_east = new JPanel();
        JPanel flow_west = new JPanel();
        flow_east.setLayout( new BoxLayout(flow_east, BoxLayout.X_AXIS));
        flow_west.setLayout( new BoxLayout(flow_west, BoxLayout.X_AXIS));
        
        pan_north.setOpaque(false);
        pan_south.setOpaque(false);
        pan_east.setOpaque(false);
        pan_west.setOpaque(false);
        flow_north.setOpaque(false);
        flow_south.setOpaque(false);
        flow_east.setOpaque(false);
        flow_west.setOpaque(false);
        
        
//        pan_north.add(BorderLayout.WEST,gui_nw);
//        pan_north.add(BorderLayout.EAST,gui_ne);
        pan_north.add(BorderLayout.CENTER,flow_north);
        flow_north.add(gui_north);
                
//        pan_south.add(BorderLayout.WEST,gui_sw);
//        pan_south.add(BorderLayout.EAST,gui_se);
        pan_south.add(BorderLayout.CENTER,flow_south);
        flow_south.add(gui_south);
        
        gui_east.setAlignmentY(Component.CENTER_ALIGNMENT);
        gui_west.setAlignmentY(Component.CENTER_ALIGNMENT);
        flow_east.add(gui_east);
        flow_west.add(gui_west);
        
        initButton(gui_east);
        initButton(gui_west);
        initButton(gui_south);
        initButton(gui_north);        
//        initButton(gui_ne);
//        initButton(gui_nw);
//        initButton(gui_se);
//        initButton(gui_sw);
        
        
        add(BorderLayout.NORTH,pan_north);
        add(BorderLayout.SOUTH,pan_south);
        add(BorderLayout.EAST,flow_east);
        add(BorderLayout.WEST,flow_west);
        
        gui_east.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                if(map != null && map.getRenderingStrategy().getMapArea() != null){
                    Envelope oldEnv = map.getRenderingStrategy().getMapArea();
                    double deplacement = oldEnv.getWidth()/ratio;                    
                    Coordinate coord1 = new Coordinate(oldEnv.getMinX() + deplacement, oldEnv.getMinY());
                    Coordinate coord2 = new Coordinate(oldEnv.getMaxX() + deplacement, oldEnv.getMaxY());                    
                    Envelope newEnv = new Envelope( coord1,coord2);
                    map.getRenderingStrategy().setMapArea(newEnv);                    
                }
            }
        });
        
        gui_north.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                if(map != null && map.getRenderingStrategy().getMapArea() != null){
                    Envelope oldEnv = map.getRenderingStrategy().getMapArea();
                    double deplacement = oldEnv.getWidth()/ratio;                    
                    Coordinate coord1 = new Coordinate(oldEnv.getMinX() , oldEnv.getMinY() + deplacement);
                    Coordinate coord2 = new Coordinate(oldEnv.getMaxX() , oldEnv.getMaxY() + deplacement);                    
                    Envelope newEnv = new Envelope( coord1,coord2);
                    map.getRenderingStrategy().setMapArea(newEnv);                    
                }
            }
        });
        
        gui_south.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                if(map != null && map.getRenderingStrategy().getMapArea() != null){
                    Envelope oldEnv = map.getRenderingStrategy().getMapArea();
                    double deplacement = oldEnv.getWidth()/ratio;                    
                    Coordinate coord1 = new Coordinate(oldEnv.getMinX(), oldEnv.getMinY() - deplacement);
                    Coordinate coord2 = new Coordinate(oldEnv.getMaxX(), oldEnv.getMaxY() - deplacement);                    
                    Envelope newEnv = new Envelope( coord1,coord2);
                    map.getRenderingStrategy().setMapArea(newEnv);                    
                }
            }
        });
        
        gui_west.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                if(map != null && map.getRenderingStrategy().getMapArea() != null){
                    Envelope oldEnv = map.getRenderingStrategy().getMapArea();
                    double deplacement = oldEnv.getWidth()/ratio;                    
                    Coordinate coord1 = new Coordinate(oldEnv.getMinX() - deplacement, oldEnv.getMinY());
                    Coordinate coord2 = new Coordinate(oldEnv.getMaxX() - deplacement, oldEnv.getMaxY());                    
                    Envelope newEnv = new Envelope( coord1,coord2);
                    map.getRenderingStrategy().setMapArea(newEnv);                    
                }
            }
        });
        
        
    }
    
    private void initButton(JButton b){
        b.setBorder(null);
        b.setBorderPainted(false);
        b.setOpaque(false);
        b.setContentAreaFilled(false);
        b.setFocusable(false);
        b.setRolloverEnabled(false);
        b.setHorizontalAlignment(SwingConstants.CENTER);
    }

    public void refresh() {
        repaint();
    }

    public JComponent geComponent() {
        return this;
    }

    public void setMap2D(Map2D map) {
        this.map = map;
    }

    public Map2D getMap2D() {
        return map;
    }

    public void dispose() {
    }
}
