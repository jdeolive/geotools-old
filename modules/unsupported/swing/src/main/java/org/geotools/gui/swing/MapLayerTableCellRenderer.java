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

import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.ResourceBundle;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;
import org.geotools.map.MapLayer;

/**
 * A custom list cell renderer for items in the JList used by {@linkplain MapLayerTable}
 * to show map layer names and states.
 * <p>
 * Note: this class is package-private
 *
 * @author Michael Bedward
 */
class MapLayerTableCellRenderer extends JPanel implements ListCellRenderer {

    /**
     * Constants for icons used to display layer states. Each constant has
     * an associated tool-tip string, and icons for 'on' and 'off' states.
     */
    public static enum LayerState {
        /**
         * Layer visibility - whether the layer will be shown or hidden
         * when the map display is drawn
         */
        VISIBLE(
            ResourceBundle.getBundle("org/geotools/gui/swing/MapWidget").getString("show_layer"),
            new ImageIcon(MapLayerTableCellRenderer.class.getResource(
                "/org/geotools/gui/swing/images/eye_open.png")),
            new ImageIcon(MapLayerTableCellRenderer.class.getResource(
                "/org/geotools/gui/swing/images/eye_closed.png"))
        ),

        /**
         * Layer selection - the selected status of layers can be used
         * to include or exclude them in map queries etc.
         */
        SELECTED(
            ResourceBundle.getBundle("org/geotools/gui/swing/MapWidget").getString("select_layer"),
            new ImageIcon(MapLayerTableCellRenderer.class.getResource(
                "/org/geotools/gui/swing/images/tick.png")),
            new ImageIcon(MapLayerTableCellRenderer.class.getResource(
                "/org/geotools/gui/swing/images/cross.png"))
        );
    
        private String desc;
        private ImageIcon onIcon;
        private ImageIcon offIcon;

        /**
         * Private constructor
         * @param desc a brief description for a tool-tip
         * @param onIcon icon for the 'on' state
         * @param offIcon icon for the 'off' state
         */
        private LayerState(String desc, ImageIcon onIcon, ImageIcon offIcon) {
            this.desc = desc;
            this.onIcon = onIcon;
            this.offIcon = offIcon;
        }

        /**
         * Get the tool-tip string
         */
        @Override
        public String toString() {
            return desc;
        }

        /**
         * Get the icon used to signify the 'on' state
         */
        public Icon getOnIcon() {
            return onIcon;
        }

        /**
         * Get the icon used to signify the 'off' state
         */
        public Icon getOffIcon() {
            return offIcon;
        }
    }

    private static Icon REMOVE_LAYER_ICON = new ImageIcon(
            MapLayerTableCellRenderer.class.getResource("/org/geotools/gui/swing/images/remove_layer.png"));

    private final static int CELL_PADDING = 5;
    private final static int CELL_HEIGHT;
    private final static Rectangle SEL_RECT;
    private final static Rectangle VIS_RECT;
    private final static Rectangle REM_RECT;

    static {
        int x = CELL_PADDING;
        int h = LayerState.SELECTED.getOnIcon().getIconHeight();
        int w = LayerState.SELECTED.getOnIcon().getIconWidth();
        CELL_HEIGHT = h + 2*CELL_PADDING;

        VIS_RECT = new Rectangle(x, CELL_PADDING, w, h);
        x += w + CELL_PADDING;

        h = LayerState.VISIBLE.getOnIcon().getIconHeight();
        w = LayerState.VISIBLE.getOnIcon().getIconWidth();
        SEL_RECT = new Rectangle(x, CELL_PADDING, w, h);
        x += w + CELL_PADDING;

        h = REMOVE_LAYER_ICON.getIconHeight();
        w = REMOVE_LAYER_ICON.getIconWidth();
        REM_RECT = new Rectangle(x, CELL_PADDING, w, h);
    }

    private JLabel visibleLabel;
    private JLabel selectedLabel;
    private JLabel removeLayerLabel;
    private JLabel nameLabel;


    /**
     * Get the constant height that will be used for list cells
     */
    public static int getCellHeight() {
        return CELL_HEIGHT;
    }

    /**
     * Check if a point representing a mouse click location lies within
     * the bounds of the layer visibility label
     * @param p coords of the mouse click; relative to this cell's origin
     * @return true if the point is within the label bounds; false otherwise
     */
    public static boolean hitVisibilityLabel(Point p) {
        return VIS_RECT.contains(p);
    }

    /**
     * Check if a point representing a mouse click location lies within
     * the bounds of the layer selection label
     * @param p coords of the mouse click; relative to this cell's origin
     * @return true if the point is within the label bounds; false otherwise
     */
    public static boolean hitSelectionLabel(Point p) {
        return SEL_RECT.contains(p);
    }

    /**
     * Check if a point representing a mouse click location lies within
     * the bounds of the remove layer label
     * @param p coords of the mouse click; relative to this cell's origin
     * @return true if the point is within the label bounds; false otherwise
     */
    public static boolean hitRemoveLabel(Point p) {
        return REM_RECT.contains(p);
    }


    /**
     * Constructor
     */
    public MapLayerTableCellRenderer() {
        super(new FlowLayout(FlowLayout.LEFT, CELL_PADDING, CELL_PADDING));

        visibleLabel = new JLabel();
        add(visibleLabel);

        selectedLabel = new JLabel();
        add(selectedLabel);

        removeLayerLabel = new JLabel();
        removeLayerLabel.setIcon(REMOVE_LAYER_ICON);
        add(removeLayerLabel);

        this.nameLabel = new JLabel();
        add(nameLabel);
    }

    public Component getListCellRendererComponent(
            JList list,
            Object value, // value to display
            int index, // cell index
            boolean isSelected, // is the cell selected
            boolean cellHasFocus) // the list and the cell have the focus
    {
        MapLayer layer = (MapLayer)value;
        String s = layer.getFeatureSource().getName().getLocalPart();
        nameLabel.setText(s);

        visibleLabel.setIcon(
                layer.isVisible() ? 
                    LayerState.VISIBLE.getOnIcon() : LayerState.VISIBLE.getOffIcon());

        selectedLabel.setIcon(
                layer.isSelected() ?
                    LayerState.SELECTED.getOnIcon() : LayerState.SELECTED.getOffIcon());

        if (isSelected) {
            setBackground(list.getSelectionBackground());
            setForeground(list.getSelectionForeground());
        } else {
            setBackground(list.getBackground());
            setForeground(list.getForeground());
        }

        setEnabled(list.isEnabled());
        setFont(list.getFont());
        setOpaque(true);
        return this;
    }
}
