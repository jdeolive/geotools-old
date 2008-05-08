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
package org.geotools.gui.swing.map.map2d.control;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.border.Border;

import org.geotools.gui.swing.map.map2d.EditableMap2D;
import org.geotools.gui.swing.map.map2d.Map2D;
import org.geotools.gui.swing.map.map2d.event.Map2DEditionEvent;
import org.geotools.gui.swing.map.map2d.handler.DefaultEditionHandler;
import org.geotools.gui.swing.map.map2d.handler.EditionHandler;
import org.geotools.gui.swing.map.map2d.handler.LineCreationHandler;
import org.geotools.gui.swing.map.map2d.handler.MultiLineCreationHandler;
import org.geotools.gui.swing.map.map2d.handler.MultiPointCreationHandler;
import org.geotools.gui.swing.map.map2d.handler.MultiPolygonCreationHandler;
import org.geotools.gui.swing.map.map2d.handler.PointCreationHandler;
import org.geotools.gui.swing.map.map2d.handler.PolygonCreationHandler;
import org.geotools.gui.swing.map.map2d.listener.Map2DEditionListener;

/**
 *
 * @author johann sorel
 */
public class EditHandlerChooser extends JComboBox {

    private Map2D map = null;
    private ItemListener listListener = new ItemListener() {

        public void itemStateChanged(ItemEvent e) {

            if (map != null && map instanceof EditableMap2D) {
                EditableMap2D editmap = (EditableMap2D) map;
                editmap.setEditionHandler((EditionHandler) getSelectedItem());
            }
        }
    };
    private Map2DEditionListener selectionListener = new Map2DEditionListener() {


        public void editionHandlerChanged(Map2DEditionEvent event) {
            removeItemListener(listListener);
            EditionHandler handler = event.getEditionHandler();
            
            if (handler != getSelectedItem()) {
                if (handler instanceof DefaultEditionHandler) {
                    setSelectedItem(defaultHandler);
                } else if (handler instanceof PointCreationHandler) {
                    setSelectedItem(pointHandler);
                } else if (handler instanceof MultiPointCreationHandler) {
                    setSelectedItem(multipointHandler);
                } else if (handler instanceof LineCreationHandler) {
                    setSelectedItem(lineHandler);
                } else if (handler instanceof MultiLineCreationHandler) {
                    setSelectedItem(multilineHandler);
                } else if (handler instanceof PolygonCreationHandler) {
                    setSelectedItem(polygonHandler);
                } else if (handler instanceof MultiPolygonCreationHandler) {
                    setSelectedItem(multipolygonHandler);
                } else {

                }
            }
            addItemListener(listListener);
        }

        public void editedLayerChanged(Map2DEditionEvent event) {
        }

    };
    private final EditionHandler defaultHandler = new DefaultEditionHandler();
    private final EditionHandler pointHandler = new PointCreationHandler();
    private final EditionHandler multipointHandler = new MultiPointCreationHandler();
    private final EditionHandler lineHandler = new LineCreationHandler();
    private final EditionHandler multilineHandler = new MultiLineCreationHandler();
    private final EditionHandler polygonHandler = new PolygonCreationHandler();
    private final EditionHandler multipolygonHandler = new MultiPolygonCreationHandler();

    public EditHandlerChooser() {
        setRenderer(new listRenderer());

        addItem(defaultHandler);
        addItem(pointHandler);
        addItem(multipointHandler);
        addItem(lineHandler);
        addItem(multilineHandler);
        addItem(polygonHandler);
        addItem(multipolygonHandler);

        addItemListener(listListener);

        initComboBox();
        
        setOpaque(false);
        setBorder(null);
    }

    private void initComboBox() {

        removeItemListener(listListener);

        if (map != null && map instanceof EditableMap2D) {
            setEnabled(true);
            EditableMap2D select = (EditableMap2D) map;

            if (select.getEditionHandler() instanceof DefaultEditionHandler) {
                setSelectedItem(defaultHandler);
            } else if (select.getEditionHandler() instanceof PointCreationHandler) {
                setSelectedItem(pointHandler);
            } else if (select.getEditionHandler() instanceof MultiPointCreationHandler) {
                setSelectedItem(multipointHandler);
            } else if (select.getEditionHandler() instanceof LineCreationHandler) {
                setSelectedItem(lineHandler);
            } else if (select.getEditionHandler() instanceof MultiLineCreationHandler) {
                setSelectedItem(multilineHandler);
            } else if (select.getEditionHandler() instanceof PolygonCreationHandler) {
                setSelectedItem(polygonHandler);
            } else if (select.getEditionHandler() instanceof MultiPolygonCreationHandler) {
                setSelectedItem(multipolygonHandler);
            } else {

            }

        } else {
            setEnabled(false);
        }

        addItemListener(listListener);
    }

    public Map2D getMap() {
        return map;
    }

    public void setMap(Map2D map2d) {

        if (map != null) {
            if (map instanceof EditableMap2D) {
                ((EditableMap2D) map).removeEditableMap2DListener(selectionListener);
            }
        }

        map = map2d;

        if (map != null) {
            if (map instanceof EditableMap2D) {
                ((EditableMap2D) map).addEditableMap2DListener(selectionListener);
            }
        }

        initComboBox();
    }

    //----------------private classes-------------------------------------------
    private class listRenderer extends DefaultListCellRenderer {

        private JLabel lbl = new JLabel();
        private final Border border = BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1);
        private final Border nullborder = BorderFactory.createEmptyBorder(1, 1, 1, 1);

        @Override
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            if (value instanceof EditionHandler) {
                lbl.setIcon(((EditionHandler) value).getIcon());
                lbl.setText(((EditionHandler) value).getTitle());
            } else {
                lbl.setIcon(null);
                lbl.setText(value.toString());
            }

            if (isSelected) {
                lbl.setBorder(border);
            } else {
                lbl.setBorder(nullborder);
            }

            return lbl;
        }
    }
}
