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
package org.geotools.swing;

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

import org.geotools.renderer.GTRenderer;
import org.geotools.swing.action.PanAction;
import org.geotools.swing.action.ResetAction;
import org.geotools.swing.action.ZoomInAction;
import org.geotools.swing.action.ZoomOutAction;
import org.geotools.map.MapContext;
import org.geotools.renderer.lite.StreamingRenderer;

/**
 * A simple Swing widget with a menu bar, tool bar, map display pane
 * ({@linkplain org.geotools.swing.JMapPane}), table of displayed
 * layers ({@linkplain org.geotools.swing.MapLayerTable}) and
 * and status bar ({@linkplain org.geotools.swing.StatusBar}).
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

        frame.setSize(500, 500);

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                frame.setVisible(true);
            }
        });
    }

    /**
     * Default constructor. Creates a {@code JMapFrame} with
     * no context or renderer set
     */
    public JMapFrame() {
        this(null);
    }

    /**
     * Constructs a new {@code JMapFrame} object with specified context
     * and a default renderer (an instance of {@link StreamingRenderer}).
     *
     * @param context the map context with layers to be displayed
     */
    public JMapFrame(MapContext context) {
        this(context, new StreamingRenderer());
    }

    /**
     * Constructs a new {@code JMapFrame} object with specified context and renderer
     *
     * @param context the map context with layers to be displayed
     */
    public JMapFrame(MapContext context, GTRenderer renderer) {
        super(context == null ? "" : context.getTitle());
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        showToolBar = false;
        showLayerTable = false;
        showStatusBar = false;

        // the map pane is the one element that is always displayed
        mapPane = new JMapPane();
        mapPane.setBackground(Color.WHITE);
        mapPane.setRenderer(renderer);
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

        /*
         * We use the MigLayout manager to make it easy to manually code
         * our UI design
         */
        StringBuilder sb = new StringBuilder();
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

            panel.add(toolBar, "grow");
        }

        if (showLayerTable) {
            mapLayerTable = new MapLayerTable(mapPane);

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
     * Get the map context associated with this frame.
     * Returns {@code null} if no map context has been set explicitly with the
     * constructor or {@linkplain #setMapContext}.
     *
     * @return the current {@code MapContext} object
     */
    public MapContext getMapContext() {
        return mapPane.getMapContext();
    }

    /**
     * Set the MapContext object used by this frame.
     *
     * @param context a MapContext instance
     * @throws IllegalArgumentException if context is null
     */
    public void setMapContext(MapContext context) {
        if (context == null) {
            throw new IllegalArgumentException("context must not be null");
        }

        mapPane.setMapContext(context);
    }

    /**
     * Get the renderer being used by this frame.
     * Returns {@code null} if no renderer was set via the constructor
     * or {@linkplain #setRenderer}.
     *
     * @return the current {@code GTRenderer} object
     */
    public GTRenderer getRenderer() {
        return mapPane.getRenderer();
    }

    /**
     * Set the renderer to be used by this frame.
     *
     * @param renderer a GTRenderer instance
     * @throws IllegalArgumentException if renderer is null
     */
    public void setRenderer(GTRenderer renderer) {
        if (renderer == null) {
            throw new IllegalArgumentException("renderer must not be null");
        }

        mapPane.setRenderer(renderer);
    }

    /**
     * Provides access to the instance of {@code JMapPane} being used
     * by this frame.
     *
     * @return the {@code JMapPane} object
     */
    public JMapPane getMapPane() {
        return mapPane;
    }
}
