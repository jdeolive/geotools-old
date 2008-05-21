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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ResourceBundle;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JToolBar;

import org.geotools.gui.swing.icon.IconBundle;
import org.geotools.gui.swing.map.map2d.JDefaultMap2D;
import org.geotools.gui.swing.map.map2d.StreamingMap2D;

/**
 *
 * @author johann sorel
 */
public class JMap2DInfoBar extends JToolBar {

    private final ResourceBundle bundle = ResourceBundle.getBundle("org/geotools/gui/swing/map/map2d/control/Bundle");
    private static final ImageIcon ICON_CONFIG = IconBundle.getResource().getIcon("16_map2d_optimize");
    private StreamingMap2D map = null;
    private JMap2DMouseCoordPanel m_coord = new JMap2DMouseCoordPanel();
    private JCheckBox gui_chk_refresh = new JCheckBox();
    private JCheckBox gui_chk_message = new JCheckBox();
    private JButton gui_config = new JButton(ICON_CONFIG);

    public JMap2DInfoBar() {

        gui_config.setEnabled(false);
        gui_config.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent arg0) {
                if (map != null && map instanceof JDefaultMap2D) {

                    JMap2DConfigPanel opt = new JMap2DConfigPanel();
                    opt.setMap((JDefaultMap2D) map);

                    JDialog dia = new JDialog();
                    dia.setContentPane(opt);

                    dia.pack();
                    dia.setLocationRelativeTo(null);
                    dia.setVisible(true);
                    dia.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
                }
            }
        });

        gui_chk_refresh.setText(bundle.getString("autorefresh"));
        gui_chk_message.setText(bundle.getString("messages"));
        gui_chk_refresh.setOpaque(false);
        gui_chk_message.setOpaque(false);
        gui_chk_refresh.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent arg0) {
                if (map != null) {
                    map.getRenderingStrategy().setAutoRefreshEnabled(gui_chk_refresh.isSelected());
                }
            }
        });
        gui_chk_message.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent arg0) {
                if (map != null) {
                    map.getInformationDecoration().displayLowLevelMessages(gui_chk_message.isSelected());
                }
            }
        });


        add(gui_config);
        add(m_coord);
        add(gui_chk_message);
        add(gui_chk_refresh);
    }

    /**
     * set the related Map2D
     * @param map : related Map2D
     */
    public void setMap(StreamingMap2D map) {

        if (map instanceof StreamingMap2D) {
            this.map = map;
            m_coord.setMap(map);
            gui_chk_refresh.setSelected(map.getRenderingStrategy().isAutoRefresh());
            gui_chk_message.setSelected(map.getInformationDecoration().isDisplayingLowLevelMessages());
        }

        if (map instanceof JDefaultMap2D) {
            gui_config.setEnabled(true);
        } else {
            gui_config.setEnabled(false);
        }

    }
}
