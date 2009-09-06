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

import java.awt.Color;
import java.awt.Dimension;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;
import net.miginfocom.swing.MigLayout;

import org.geotools.gui.swing.action.PanAction;
import org.geotools.gui.swing.action.ResetAction;
import org.geotools.gui.swing.action.ZoomInAction;
import org.geotools.gui.swing.action.ZoomOutAction;
import org.geotools.map.MapContext;
import org.geotools.renderer.lite.StreamingRenderer;

/**
 * A simple Swing widget with a menu bar, tool bar, map display pane
 * ({@linkplain org.geotools.gui.swing.JMapPane}), table of displayed
 * layers ({@linkplain org.geotools.gui.swing.MapLayerTable}) and
 * and status bar ({@linkplain org.geotools.gui.swing.StatusBar}).
 * <p>
 * This widget is capable enough for simple applications, and has
 * been designed to allow its methods to be easily overridden to customize
 * it further. Alternatively, use this code as a starting point for your
 * own display classes.
 * <p>
 * The UI is constructed using the MiGLayout layout manager
 * to avoid clutterring the sources with GUI-builder generated code.
 *
 * @todo simple code example(s) here
 *
 * @see MapLayerTable
 * @see StatusBar
 *
 * @author Michael Bedward
 * @since 2.6
 */
public class JMapFrame extends JFrame {

    /**
     * UI elements
     */
    private JMapPane mapPane;
    private MapLayerTable mapLayerTable;
    private JToolBar toolBar;
    private StatusBar statusBar;

    private boolean showToolBar;
    private boolean showStatusBar;
    private boolean showLayerTable;
    private boolean showMenuBar;
    private boolean uiSet;

    /**
     * Creates a new {@code JMapFrame} object with a toolbar, map pane and status
     * bar; sets the supplied {@code MapContext}; and displays the frame on the
     * AWT event dispatching thread.
     */
    public static void showMap(MapContext context) {
        final JMapFrame frame = new JMapFrame(context);
        frame.enableStatusBar(true);
        frame.enableToolBar(true);
        frame.setupUI();

        frame.setMapContext(context);

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                frame.setVisible(true);
            }
        });
    }

    /**
     * Default constructor. Creates a {@code JMapFrame} with
     * no context set
     */
    public JMapFrame() {
        this(null);
    }

    /**
     * Constructor
     * @param context the map context with layers to be displayed
     */
    public JMapFrame(MapContext context) {
        super(context == null ? "" : context.getTitle());
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        showMenuBar = false;
        showToolBar = false;
        showLayerTable = false;
        showStatusBar = false;

        // the map pane is the one element that is always displayed
        mapPane = new JMapPane();
        mapPane.setBackground(Color.WHITE);
        mapPane.setRenderer(new StreamingRenderer());
    }

    /**
     * Set whether a toolbar, with a basic set of map tools, will be displayed
     * (default is false);
     * 
     * @param state
     */
    public void enableToolBar(boolean state) {
        showToolBar = state;
    }

    /**
     * Set whether a menu bar will be displayed. If true, an empty menu bar
     * is created either when {@link #setupUI()} or {@link #setVisible(true)}
     * is called.
     */
    public void enableMenuBar(boolean state) {
        showMenuBar = state;
    }

    /**
     * Set whether a status bar will be displayed.
     */
    public void enableStatusBar(boolean state) {
        showStatusBar = state;
    }

    /**
     * Set whether a map layer table will be displayed.
     */
    public void enableLayerTable(boolean state) {
        showLayerTable = state;
    }

    @Override
    public void setVisible(boolean state) {
        if (state && !uiSet) {
            setupUI();
        }

        super.setVisible(state);
    }

    /**
     * Creates and lays out the UI elements
     *
     * @todo Needs a better name !
     */
    public void setupUI() {
        if (uiSet) {
            // @todo log a warning ?
            return;
        }
        
        if (showLayerTable) {
            mapLayerTable = new MapLayerTable(mapPane);
        }

        /*
         * A toolbar with buttons for zooming in, zooming out,
         * panning, and resetting the map to its full extent.
         * The cursor tool buttons (zooming and panning) are put
         * in a ButtonGroup.
         *
         * Note the use of the XXXAction objects which makes constructing
         * the tool bar buttons very simple.
         */
        if (showToolBar) {
            toolBar = new JToolBar();
            toolBar.setOrientation(JToolBar.HORIZONTAL);
            toolBar.setFloatable(false);

            ButtonGroup cursorToolGrp = new ButtonGroup();

            JButton zoomInBtn = new JButton(new ZoomInAction(mapPane));
            toolBar.add(zoomInBtn);
            cursorToolGrp.add(zoomInBtn);

            JButton zoomOutBtn = new JButton(new ZoomOutAction(mapPane));
            toolBar.add(zoomOutBtn);
            cursorToolGrp.add(zoomOutBtn);

            toolBar.addSeparator();

            JButton panBtn = new JButton(new PanAction(mapPane));
            toolBar.add(panBtn);
            cursorToolGrp.add(panBtn);

            toolBar.addSeparator();

            JButton resetBtn = new JButton(new ResetAction(mapPane));
            toolBar.add(resetBtn);
        }

        /*
         * We use the MigLayout manager to make it easy to manually code
         * our UI design
         */
        StringBuilder sb = new StringBuilder();
        if (showMenuBar) {
            sb.append("[]"); // fixed size
        }
        if (showToolBar) {
            sb.append("[]"); // fixed size
        }
        sb.append("[grow]"); // map pane and optionally layer table fill space
        if (showStatusBar) {
            sb.append("[30px::]"); // status bar height
        }

        JPanel panel = new JPanel(new MigLayout(
                "wrap 1, insets 0", // layout constrains: 1 component per row, no insets

                "[grow]", // column constraints: col grows when frame is resized

                sb.toString() ));

        panel.add(toolBar, "grow");

        if (showLayerTable) {
            /*
             * We put the map layer panel and the map pane into a JSplitPane
             * so that the user can adjust their relative sizes as needed
             * during a session. The call to setPreferredSize for the layer
             * panel has the effect of setting the initial position of the
             * JSplitPane divider
             */
            mapLayerTable.setPreferredSize(new Dimension(200, -1));
            JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, false, mapLayerTable, mapPane);
            panel.add(splitPane, "grow");

        } else {
            /*
             * No layer table, just the map pane
             */
            panel.add(mapPane, "grow");
        }

        if (showStatusBar) {
            statusBar = new StatusBar(mapPane);
            panel.add(statusBar, "grow");
        }

        this.getContentPane().add(panel);
        uiSet = true;
    }

    /**
     * Get the MapContext object associated with this JMapFrame.
     * If no map context has been set explicitly with
     * {@linkplain #setMapContext} or implicitly via
     * {@linkplain #addLayer} then null will be returned.
     *
     * @return the current MapContext object
     */
    public MapContext getMapContext() {
        MapContext context = null;

        if (mapPane != null) {
            context = mapPane.getContext();
        }

        return context;
    }

    /**
     * Set the MapContext object used by this JMapFrame.
     *
     * @param context a MapContext instance
     * @throws IllegalArgumentException if context is null
     */
    public void setMapContext(MapContext context) {
        if (context == null) {
            throw new IllegalArgumentException("context must not be null");
        }

        mapPane.setContext(context);
    }
    
    public JMapPane getMapPane() {
        return mapPane;
    }
}
