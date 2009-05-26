/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 *
 *    (C) 2002-2008, Open Source Geospatial Foundation (OSGeo)
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

package org.geotools.gui.swing;

import java.awt.BorderLayout;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import javax.swing.Icon;
import javax.swing.event.ListDataEvent;
import org.geotools.gui.swing.dndlist.DnDList;
import java.awt.Point;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.util.ResourceBundle;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.ListDataListener;
import org.geotools.gui.swing.dndlist.DnDListModel;
import org.geotools.map.MapLayer;

/**
 * Displays a list of the map layers in an associated {@linkplain JMapPane} and
 * provides controls to set the visibility and selection of each layer.
 * <p>
 * Implementation note: DefaultMapContext stores its list of MapLayer objects
 * in rendering order, ie. the layer at index 0 is rendererd first, followed by
 * index 1 etc. MapLayerTable stores its layers in the reverse order since it
 * is more intuitive for the user to think of layers being 'on top' of other
 * layers.
 *
 * @author Michael Bedward
 */
public class MapLayerTable extends JPanel {

    private static final ResourceBundle stringRes = ResourceBundle.getBundle("org/geotools/gui/swing/MapWidget");

    private static final int BORDER = 10;

    private JMapPane pane;
    private DnDListModel<MapLayer> listModel;
    private DnDList list;
    private JScrollPane scrollPane;

    /**
     * Default constructor. A subsequent call to {@linkplain #setMapPane}
     * will be required.
     */
    public MapLayerTable() {
        init();
    }

    /**
     * Constructor.
     * @param pane the map pane this MapLayerTable will service.
     */
    public MapLayerTable(JMapPane pane) {
        init();
        setMapPane(pane);
    }

    /**
     * Set the map pane that this MapLayerTable will service.
     */
    public void setMapPane(JMapPane pane) {
        this.pane = pane;
        pane.setMapLayerTable(this);
    }

    /**
     * Add a new layer to those listed in the table. This method will be called
     * by the associated map pane automatically as part of the event sequence
     * when a new MapLayer is added to the pane's MapContext.
     */
    public void addLayer(MapLayer layer) {
        listModel.insertItem(0, layer);
    }

    /**
     * Remove a layer from those listed in the table. This method will be called
     * by the associated map pane automatically as part of the event sequence
     * when a new MapLayer is removed from the pane's MapContext.
     */
    void removeLayer(MapLayer layer) {
        listModel.removeItem(layer);
    }

    /**
     * Repaint the list item associated with the specified MapLayer object
     */
    public void repaint(MapLayer layer) {
        int index = listModel.indexOf(layer);
        list.repaint(list.getCellBounds(index, index));
    }

    /**
     * Called by the constructor. This method lays out the components that
     * make up the MapLayerTable and registers a mouse listener.
     */
    private void init() {
        listModel = new DnDListModel<MapLayer>();
        list = new DnDList(listModel) {
            /*
             * We override setToolTipText to provide tool tips
             * for the control labels displayed for each list item
             */
            @Override
            public String getToolTipText(MouseEvent e) {
                int item = list.locationToIndex(e.getPoint());

                if (item >= 0) {
                    Rectangle r = list.getCellBounds(item, item);
                    if (r.contains(e.getPoint())) {
                        Point p = new Point(e.getPoint().x, e.getPoint().y - r.y);

                        if (MapLayerTableCellRenderer.hitSelectionLabel(p)) {
                            return stringRes.getString("select_layer");

                        } else if (MapLayerTableCellRenderer.hitVisibilityLabel(p)) {
                            return stringRes.getString("show_layer");

                        } else if (MapLayerTableCellRenderer.hitRemoveLabel(p)) {
                            return stringRes.getString("remove_layer");
                        }
                    }
                }
                
                return null;
            }
        };

        // Listen for drag-reordering of the list contents which
        // will be received via the contentsChanged method
        listModel.addListDataListener(new ListDataListener() {

            public void intervalAdded(ListDataEvent e) {
            }

            public void intervalRemoved(ListDataEvent e) {
            }

            public void contentsChanged(ListDataEvent e) {
                onReorderLayers(e);
            }
        });

        list.setCellRenderer(new MapLayerTableCellRenderer());
        list.setFixedCellHeight(MapLayerTableCellRenderer.getCellHeight());

        list.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent e) {
                onLayerItemClicked(e);
            }
        });

        scrollPane = new JScrollPane(list,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);

        scrollPane.setBorder(BorderFactory.createTitledBorder(stringRes.getString("layers_list_title")));

        JPanel btnPanel = new JPanel();
        Icon showIcon = MapLayerTableCellRenderer.LayerState.VISIBLE.getOnIcon();
        JButton showAllBtn = new JButton(showIcon);
        showAllBtn.setToolTipText(stringRes.getString("show_all_layers"));
        showAllBtn.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                onShowAllLayers();
            }
        });
        btnPanel.add(showAllBtn);

        Icon hideIcon = MapLayerTableCellRenderer.LayerState.VISIBLE.getOffIcon();
        JButton hideAllBtn = new JButton(hideIcon);
        hideAllBtn.setToolTipText(stringRes.getString("hide_all_layers"));
        hideAllBtn.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                onHideAllLayers();
            }
        });
        btnPanel.add(hideAllBtn);

        Icon onIcon = MapLayerTableCellRenderer.LayerState.SELECTED.getOnIcon();
        JButton selAllBtn = new JButton(onIcon);
        selAllBtn.setToolTipText(stringRes.getString("select_all_layers"));
        selAllBtn.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                onSelectAllLayers();
            }
        });
        btnPanel.add(selAllBtn);

        Icon offIcon = MapLayerTableCellRenderer.LayerState.SELECTED.getOffIcon();
        JButton unselAllBtn = new JButton(offIcon);
        unselAllBtn.setToolTipText(stringRes.getString("unselect_all_layers"));
        unselAllBtn.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                onUnselectAllLayers();
            }
        });
        btnPanel.add(unselAllBtn);

        setLayout(new BorderLayout());
        add(scrollPane, BorderLayout.CENTER);
        add(btnPanel, BorderLayout.SOUTH);
    }

    /**
     * Handle a mouse click on a cell in the JList that displays
     * layer names and states.
     */
    private void onLayerItemClicked(MouseEvent e) {
        int item = list.locationToIndex(e.getPoint());

        if (item >= 0) {
            Rectangle r = list.getCellBounds(item, item);
            if (r.contains(e.getPoint())) {
                MapLayer layer = listModel.getElementAt(item);
                Point p = new Point(e.getPoint().x, e.getPoint().y - r.y);

                if (MapLayerTableCellRenderer.hitSelectionLabel(p)) {
                    layer.setSelected(!layer.isSelected());

                } else if (MapLayerTableCellRenderer.hitVisibilityLabel(p)) {
                    layer.setVisible(!layer.isVisible());
                
                } else if (MapLayerTableCellRenderer.hitRemoveLabel(p)) {
                    pane.getContext().removeLayer(layer);
                }
            }
        }
    }

    /**
     * Handle a ListDataEvent signallying a drag-reordering of the map layers.
     * The event is published by the list model after the layers have been
     * reordered there.
     */
    private void onReorderLayers(ListDataEvent ev) {
        pane.setRepaint(false);
        for (int pos = ev.getIndex0(); pos <= ev.getIndex1(); pos++) {
            MapLayer layer = listModel.getElementAt(pos);

            /*
             * MapLayerTable stores layers in the reverse order to
             * DefaultMapContext (see comment in javadocs for this class)
             */
            int newContextPos = listModel.getSize() - pos - 1;

            int curContextPos = pane.getContext().indexOf(layer);

            if (curContextPos != newContextPos) {
                pane.getContext().moveLayer(curContextPos, newContextPos);
            }
        }
        pane.setRepaint(true);
        pane.repaint();
    }

    private void onShowAllLayers() {
        if (pane != null && pane.getContext() != null) {
            for (MapLayer layer : pane.getContext().getLayers()) {
                if (!layer.isVisible()) {
                    layer.setVisible(true);
                }
            }
        }
    }

    private void onHideAllLayers() {
        if (pane != null && pane.getContext() != null) {
            for (MapLayer layer : pane.getContext().getLayers()) {
                if (layer.isVisible()) {
                    layer.setVisible(false);
                }
            }
        }
    }

    private void onSelectAllLayers() {
        if (pane != null && pane.getContext() != null) {
            for (MapLayer layer : pane.getContext().getLayers()) {
                if (!layer.isSelected()) {
                    layer.setSelected(true);
                }
            }
        }
    }

    private void onUnselectAllLayers() {
        if (pane != null && pane.getContext() != null) {
            for (MapLayer layer : pane.getContext().getLayers()) {
                if (layer.isSelected()) {
                    layer.setSelected(false);
                }
            }
        }
    }
}
