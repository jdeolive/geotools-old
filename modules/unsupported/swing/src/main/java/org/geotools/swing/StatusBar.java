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

import java.awt.Font;
import java.awt.LayoutManager;
import java.awt.geom.Rectangle2D;
import java.util.ResourceBundle;
import javax.swing.JLabel;
import javax.swing.JPanel;
import net.miginfocom.swing.MigLayout;
import org.geotools.geometry.DirectPosition2D;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.swing.event.MapMouseAdapter;
import org.geotools.swing.event.MapMouseEvent;
import org.geotools.swing.event.MapMouseListener;
import org.geotools.swing.event.MapPaneListener;
import org.geotools.map.MapContext;
import org.geotools.swing.event.MapPaneAdapter;
import org.geotools.swing.event.MapPaneEvent;
import org.opengis.geometry.Envelope;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

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
public class StatusBar extends JPanel {
    private static final ResourceBundle stringRes = ResourceBundle.getBundle("org/geotools/swing/widget");

    public static final int NUM_SPACES = 3;

    public static final int COORDS_SPACE = 0;
    public static final int BOUNDS_SPACE = 1;
    public static final int CRS_SPACE = 2;

    private JMapPane mapPane;
    private MapContext context;
    private MapMouseListener mouseListener;
    private MapPaneAdapter mapPaneListener;

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
    public void setMapPane(final JMapPane newPane) {
        if (newPane == null) {
            throw new IllegalArgumentException(stringRes.getString("arg_null_error"));
        }

        if (mapPane != newPane) {
            if (mapPane != null) {
                mapPane.removeMouseListener(mouseListener);
            }

            newPane.addMouseListener(mouseListener);
            newPane.addMapPaneListener(mapPaneListener);
            context = newPane.getMapContext();
            mapPane = newPane;
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
        if (mapPos != null) {
            spaces[COORDS_SPACE].setText(String.format("  %.2f %.2f", mapPos.x, mapPos.y));
        }
    }

    /**
     * Display the bounding coordinates of the given envelope
     */
    public void displayBounds(Envelope bounds) {
        if (bounds != null) {
            spaces[BOUNDS_SPACE].setText(String.format("Min:%.2f %.2f Span:%.2f %.2f",
                    bounds.getMinimum(0),
                    bounds.getMinimum(1),
                    bounds.getSpan(0),
                    bounds.getSpan(1)));
        }
    }

    public void displayCRS(CoordinateReferenceSystem crs) {
        if (crs != null) {
            spaces[CRS_SPACE].setText(crs.getName().toString());
        }
    }

    /**
     * Helper for constructors. Sets basic layout and creates
     * the first space for map coordinates.
     */
    private void init() {
        LayoutManager lm = new MigLayout("insets 0");
        this.setLayout(lm);

        spaces = new JLabel[NUM_SPACES];
        Font font = Font.decode("Courier-12");

        int fontH = getFontMetrics(font).getHeight();

        Rectangle2D rect;
        String constraint;

        spaces[COORDS_SPACE] = new JLabel();
        spaces[COORDS_SPACE].setFont(font);
        rect = getFontMetrics(font).getStringBounds(
                "  00000000.000 00000000.000", spaces[0].getGraphics());
        constraint = String.format("width %d!, height %d!",
                (int)rect.getWidth() + 10, (int)rect.getHeight() + 6);
        add(spaces[COORDS_SPACE], constraint);

        spaces[BOUNDS_SPACE] = new JLabel();
        spaces[BOUNDS_SPACE].setFont(font);
        rect = getFontMetrics(font).getStringBounds(
                "Min: 00000000.000 00000000.000 Span: 00000000.000 00000000.000", spaces[0].getGraphics());
        constraint = String.format("width %d!, height %d!",
                (int)rect.getWidth() + 10, (int)rect.getHeight() + 6);
        add(spaces[BOUNDS_SPACE], constraint);

        spaces[CRS_SPACE] = new JLabel();
        spaces[CRS_SPACE].setFont(font);
        rect = getFontMetrics(font).getStringBounds(
                "The name of a CRS might be this long", spaces[0].getGraphics());
        constraint = String.format("width %d!, height %d!",
                (int)rect.getWidth() + 20, (int)rect.getHeight() + 6);
        add(spaces[CRS_SPACE], constraint);
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

        mapPaneListener = new MapPaneAdapter() {

            @Override
            public void onDisplayAreaChanged(MapPaneEvent ev) {
                ReferencedEnvelope env = mapPane.getDisplayArea();
                if (env != null) {
                    displayBounds(env);
                    displayCRS(env.getCoordinateReferenceSystem());
                }
            }

            @Override
            public void onResized(MapPaneEvent ev) {
                displayBounds(mapPane.getDisplayArea());
            }

        };
    }

}
