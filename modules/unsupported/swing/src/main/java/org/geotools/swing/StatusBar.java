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

package org.geotools.swing;

import com.vividsolutions.jts.geom.Envelope;
import java.awt.Dimension;
import java.awt.LayoutManager;
import java.util.ResourceBundle;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.BevelBorder;
import net.miginfocom.swing.MigLayout;
import org.geotools.geometry.DirectPosition2D;
import org.geotools.swing.event.MapMouseAdapter;
import org.geotools.swing.event.MapMouseEvent;
import org.geotools.swing.event.MapMouseListener;
import org.geotools.swing.event.MapPaneListener;
import org.geotools.swing.event.MapPaneNewContextEvent;
import org.geotools.swing.event.MapPaneNewRendererEvent;
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
 * @source $URL$
 * @version $Id$
 */
public class StatusBar extends JPanel implements MapPaneListener {
    private static final long serialVersionUID = 3871466161939637993L;

    private static final ResourceBundle stringRes = ResourceBundle.getBundle("org/geotools/swing/widget");

    /*
     * TODO: display additional info in the status bar
     */
    public static final int NUM_SPACES = 2;

    public static final int COORDS_SPACE = 0;
    public static final int BOUNDS_SPACE = 1;

    private JMapPane pane;
    private MapMouseListener mouseListener;
    private MapBoundsListener mapBoundsListener;

    private JLabel[] spaces;

    /**
     * Default constructor.
     * {@linkplain #setMapPane} must be
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

    /**
     * Register this status bar to receive mouse events from
     * the given map pane
     *
     * @param pane the map pane
     * @throws IllegalArgumentException if pane is null
     */
    public void setMapPane(final JMapPane pane) {
        if (pane == null) {
            throw new IllegalArgumentException(stringRes.getString("arg_null_error"));
        }

        if (this.pane != pane) {
            if (this.pane != null) {
                this.pane.removeMouseListener(mouseListener);

                MapContext context = this.pane.getMapContext();
                if (context != null) {
                    context.removeMapBoundsListener(mapBoundsListener);
                }
            }

            pane.addMouseListener(mouseListener);

            pane.addMapPaneListener(this);

            if (pane.getMapContext() != null) {
                pane.getMapContext().addMapBoundsListener(mapBoundsListener);
            }

            this.pane = pane;
        }
    }

    /**
     * Clear the map coordinate display
     */
    public void clearCoords() {
        spaces[COORDS_SPACE].setText("");
    }

    /**
     * Clear the map bounds display
     */
    public void clearBounds() {
        spaces[BOUNDS_SPACE].setText("");
    }

    /**
     * Format and display the coordinates of the given position
     *
     * @param mapPos mouse cursor position (world coords)
     */
    public void displayCoords(DirectPosition2D mapPos) {
        if (spaces != null) {
            spaces[COORDS_SPACE].setText(String.format("%.4f %.4f", mapPos.x, mapPos.y));
        }
    }

    /**
     * Display the bounding coordinates of the given envelope
     */
    public void displayBounds(Envelope env) {
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
    
    /**
     * Helper for constructors. Sets basic layout and creates
     * the first space for map coordinates.
     */
    private void init() {
        LayoutManager lm = new MigLayout("insets 0", "[][grow]");
        this.setLayout(lm);

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

    /**
     * Initialize the mouse and map bounds listeners
     */
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

}
