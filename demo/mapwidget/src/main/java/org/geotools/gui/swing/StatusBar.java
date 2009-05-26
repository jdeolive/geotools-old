/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 *
 *    (C) 2003-2008, Open Source Geospatial Foundation (OSGeo)
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

import com.vividsolutions.jts.geom.Envelope;
import java.awt.Dimension;
import java.awt.FlowLayout;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.BevelBorder;
import net.miginfocom.swing.MigLayout;
import org.geotools.geometry.DirectPosition2D;
import org.geotools.gui.swing.event.MapMouseAdapter;
import org.geotools.gui.swing.event.MapMouseEvent;
import org.geotools.gui.swing.event.MapMouseListener;
import org.geotools.gui.swing.event.MapPaneListener;
import org.geotools.gui.swing.event.MapPaneNewContextEvent;
import org.geotools.gui.swing.event.MapPaneNewRendererEvent;
import org.geotools.map.MapContext;
import org.geotools.map.event.MapBoundsEvent;
import org.geotools.map.event.MapBoundsListener;

/**
 * A status bar that displays the mouse cursor position in
 * world coordinates.
 *
 * @todo Add the facility to display additional information in
 * the status bar. The notion of 'spaces' is in the present code
 * looking ahead to this facility.
 *
 * @author Michael Bedward
 * @since 2.6
 */
public class StatusBar extends JPanel implements MapPaneListener {

    /*
     * TODO: display additional info in the status bar
     */
    public static final int NUM_SPACES = 2;

    public static final int COORDS_SPACE = 0;
    public static final int BOUNDS_SPACE = 1;

    private static final int BORDER_WIDTH = 2;
    private static final int SPACE_GAP = 5;

    private JMapPane pane;
    private MapMouseListener mouseListener;
    private MapBoundsListener mapBoundsListener;

    private JLabel[] spaces;

    /**
     * Default constructor.
     * {@linkplain #setMapPane(org.geotools.gui.swing.JMapPane)} must be
     * called subsequently for the status bar to receive mouse events.
     */
    public StatusBar() {
        this(null);
    }

    /**
     * Constructor. Links the status bar to the specified map pane.
     *
     * @param pane the map pane that will send mouse events to this
     * status bar
     */
    public StatusBar(JMapPane pane) {
        createListeners();
        init();

        if (pane != null) {
            setMapPane(pane);
        }
    }

    private void createListeners() {
        mouseListener = new MapMouseAdapter() {

            @Override
            public void onMouseMoved(MapMouseEvent ev) {
                displayCoords(ev.getMapPosition());
            }

            @Override
            public void onMouseExited(MapMouseEvent ev) {
                clearCoords();
            }
        };

        mapBoundsListener = new MapBoundsListener() {

            public void mapBoundsChanged(MapBoundsEvent event) {
                displayBounds(event.getNewAreaOfInterest());
            }
        };
    }

    /**
     * Register this status bar to receive mouse events from
     * the given map pane
     *
     * @param pane the map pane
     * @throws IllegalArgumentException if pane is null
     */
    public void setMapPane(final JMapPane pane) {
        if (pane == null) {
            throw new IllegalArgumentException(java.util.ResourceBundle.getBundle("org/geotools/gui/swing/MapWidget").getString("arg_null_error"));
        }

        if (this.pane != pane) {
            if (this.pane != null) {
                this.pane.removeMouseListener(mouseListener);

                MapContext context = this.pane.getContext();
                if (context != null) {
                    context.removeMapBoundsListener(mapBoundsListener);
                }
            }

            pane.addMouseListener(mouseListener);

            pane.addMapPaneListener(this);

            if (pane.getContext() != null) {
                pane.getContext().addMapBoundsListener(mapBoundsListener);
            }

            this.pane = pane;
        }
    }

    /**
     * Format and display the world coordinates of the mouse cursor
     * position in the first 'space'
     *
     * @param mapPos mouse cursor position (world coords)
     */
    private void displayCoords(DirectPosition2D mapPos) {
        if (spaces != null) {
            spaces[COORDS_SPACE].setText(String.format("%.4f %.4f", mapPos.x, mapPos.y));
        }
    }

    /**
     * Clear the map coordinate display
     */
    private void clearCoords() {
        spaces[COORDS_SPACE].setText("");
    }

    /**
     * Display the bounding coordinates, width and height of the current
     * map area
     */
    private void displayBounds(Envelope env) {
        if (spaces != null) {
            spaces[BOUNDS_SPACE].setText(String.format("%.4f-%.4f (%.4f) %.4f-%.4f (%.4f)",
                    env.getMinX(),
                    env.getMaxX(),
                    env.getWidth(),
                    env.getMinY(),
                    env.getMaxY(),
                    env.getHeight()));
        }
    }

    /**
     * Clear the map bounds display
     */
    private void clearBounds() {
        spaces[BOUNDS_SPACE].setText("");
    }

    /**
     * Helper for constructors. Sets basic layout and creates
     * the first space for map coordinates.
     */
    private void init() {
        MigLayout layout = new MigLayout("insets 0", "[][grow]");
        setLayout(layout);

        spaces = new JLabel[NUM_SPACES];
        int fontH = getFontMetrics(this.getFont()).getHeight();

        for (int i = 0; i < NUM_SPACES; i++) {
            spaces[i] = new JLabel();
            spaces[i].setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));

            // @todo improve this arbitrary sizing
            spaces[i].setMinimumSize(new Dimension(200, (int)(1.5 * fontH)));
            add(spaces[i], "grow");
        }
    }

    public void onNewContext(MapPaneNewContextEvent ev) {
        if (ev.getOldContext() != null) {
            ev.getOldContext().removeMapBoundsListener(mapBoundsListener);
        }

        if (ev.getNewContext() != null) {
            ev.getNewContext().addMapBoundsListener(mapBoundsListener);
        }
    }

    public void onNewRenderer(MapPaneNewRendererEvent ev) {
    }
    
}
