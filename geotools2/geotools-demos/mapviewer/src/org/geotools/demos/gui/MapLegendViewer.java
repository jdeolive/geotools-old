/*
 *    Geotools2 - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002, Geotools Project Managment Committee (PMC)
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
 *
 */
package org.geotools.demos.gui;

import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.Point;
import org.geotools.data.DataSourceException;
import org.geotools.data.DataStore;
import org.geotools.data.FeatureSource;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.gui.swing.Legend;
import org.geotools.gui.swing.StatusBar;
import org.geotools.gui.swing.StyledMapPane2;
import org.geotools.map.DefaultMapContext;
import org.geotools.map.DefaultMapLayer;
import org.geotools.map.MapContext;
import org.geotools.map.MapLayer;
import org.geotools.renderer.j2d.RenderedMapScale;
import org.geotools.styling.Style;
import org.geotools.styling.StyleBuilder;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JToolBar;


/**
 * Load and display a shape file. At the difference of the {@link MapViewer} demo, this demo use
 * {@link MapPane} with the {@linkPlain Renderer J2D renderer}. This renderer has the following
 * advantages:
 * 
 * <ul>
 * <li>
 * faster;
 * </li>
 * <li>
 * progressive rendering of tiled image;
 * </li>
 * <li>
 * supports arbitrary map projections (different geometries to be rendered on the same map can have
 * different coordinate systems);
 * </li>
 * <li>
 * supports zooms, translations and rotations through mouse drag, mouse wheel, keyboard and
 * contextual menu localized in English, French, Portuguese and partially in Spanish and Greek;
 * </li>
 * <li>
 * provides a magnifier (accessible from the contextual menu, right button click);
 * </li>
 * <li>
 * arbitrary amount of offscreen buffering;
 * </li>
 * <li>
 * can display scroll bars;
 * </li>
 * <li>
 * can display a status bar with mouse coordinates in an arbitrary coordinate system (it doesn't
 * have to be the same coordinate system than the renderer one);
 * </li>
 * <li>
 * has a more precise scale factor taking in account the physical size of the output device (when
 * this information is available);
 * </li>
 * </ul>
 * 
 * The inconvenient is a more complex renderer, which is more difficult to modify for new users. <br>
 * <br>
 * NOTE: While not essential, it is recommanded to run this demos in server mode, with:
 * <blockquote>
 * <pre>
 * java -server org.geotools.demos.MapViewer2 <I>thefile.shp</I>
 * </pre>
 * </blockquote>
 *
 * @author Martin Desruisseaux
 * @version $Id: MapLegendViewer.java,v 1.1 2004/02/23 17:56:25 aaime Exp $
 */
public class MapLegendViewer extends JFrame {
    private MapContext context;
    private JFrame frame;
    private Legend legend;

    /**
     * Create and show the map pane.
     *
     * @throws Exception DOCUMENT ME!
     */
    public MapLegendViewer() throws Exception {
        context = new DefaultMapContext();

        // Create the map pane and add a map scale layer to it.
        final StyledMapPane2 mapPane = new StyledMapPane2();
        mapPane.setMapContext(context);
        mapPane.setPaintingWhileAdjusting(false);
        mapPane.getRenderer().addLayer(new RenderedMapScale());

        // create the legend
        legend = new Legend(context, "My legend");

        // create the status bar
        final StatusBar statusBar = new StatusBar(mapPane);

        //      manipulation layer buttons
        JButton btnAddLayer = new JButton("Add layer");
        JButton btnRemoveLayer = new JButton("Remove layer");
        JButton btnMoveUp = new JButton("Move up");
        JButton btnMoveDown = new JButton("Move down");
        JButton btnHideShow = new JButton("Hide/Show");
        JToolBar toolbar = new JToolBar();
        toolbar.setFloatable(false);
        toolbar.add(btnAddLayer);
        toolbar.add(btnRemoveLayer);
        toolbar.add(btnMoveDown);
        toolbar.add(btnMoveUp);
        toolbar.add(btnHideShow);

        // layout the form
        JSplitPane splitPane = new JSplitPane();
        splitPane.setLeftComponent(legend);
        splitPane.setRightComponent(mapPane.createScrollPane());

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());
        mainPanel.add(toolbar, BorderLayout.NORTH);
        mainPanel.add(splitPane, BorderLayout.CENTER);
        mainPanel.add(statusBar, BorderLayout.SOUTH);

        btnAddLayer.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    addLayer();
                }
            });
        btnRemoveLayer.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    removeLayer();
                }
            });
        btnHideShow.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    hideShowLayer();
                }
            });
        btnMoveUp.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    moveUpLayer();
                }
            });
        btnMoveDown.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    moveDownLayer();
                }
            });

        setTitle(context.getTitle());
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setContentPane(mainPanel);
        pack();
    }

    /**
     * Run the test from the command line. If arguments are provided, then the first argument is
     * understood as the filename of the shapefile to load.
     *
     * @param args DOCUMENT ME!
     *
     * @throws Exception is a I/O error occured.
     */
    public static void main(final String[] args) throws Exception {
        final MapLegendViewer viewer = new MapLegendViewer();
        viewer.show();
    }

    /**
     * Load the data from the specified shapefile and construct a {@linkPlain Context context} with
     * a default style.
     *
     * @param url The url of the shapefile to load.
     * @param name DOCUMENT ME!
     *
     * @throws IOException is a I/O error occured.
     * @throws DataSourceException if an error occured while reading the data source.
     * @throws FileNotFoundException DOCUMENT ME!
     */
    protected void addLayer(final URL url, String name)
        throws IOException, DataSourceException {
        // Load the file
        if (url == null) {
            throw new FileNotFoundException("Resource not found");
        }

        final DataStore store = new ShapefileDataStore(url);
        final FeatureSource features = store.getFeatureSource(store.getTypeNames()[0]);

        // Create the style
        final StyleBuilder builder = new StyleBuilder();
        final Style style;
        Class geometryClass = features.getSchema().getDefaultGeometry().getType();

        if (LineString.class.isAssignableFrom(geometryClass)
                || MultiLineString.class.isAssignableFrom(geometryClass)) {
            style = builder.createStyle(builder.createLineSymbolizer());
        } else if (Point.class.isAssignableFrom(geometryClass)
                || MultiPoint.class.isAssignableFrom(geometryClass)) {
            style = builder.createStyle(builder.createPointSymbolizer());
        } else {
            style = builder.createStyle(builder.createPolygonSymbolizer(Color.ORANGE, Color.BLACK, 1));
        }

        MapLayer layer = new DefaultMapLayer(features, style);
        layer.setTitle(name);
        context.addLayer(layer);
    }

    /**
     *
     */
    protected void removeLayer() {
        MapLayer currentLayer = legend.getSelectedLayer();

        if (currentLayer != null) {
            context.removeLayer(currentLayer);
        }
    }

    /**
     *
     */
    protected void addLayer() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.addChoosableFileFilter(new SimpleFileFilter("shp", "Shapefile (*.shp)"));

        int result = fileChooser.showOpenDialog(this);

        if (result == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();

            try {
                addLayer(file.toURL(), file.getName());
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "An error occurred while loading the file",
                    "Map viewer", JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
            }
        }
    }

    protected void hideShowLayer() {
        MapLayer currentLayer = legend.getSelectedLayer();

        if (currentLayer != null) {
            currentLayer.setVisible(!currentLayer.isVisible());
        }
    }

    protected void moveUpLayer() {
        MapLayer currentLayer = legend.getSelectedLayer();

        if (currentLayer != null) {
            int position = context.indexOf(currentLayer);

            if (position > 0) {
                context.moveLayer(position, position - 1);
            }
        }
    }

    protected void moveDownLayer() {
        MapLayer currentLayer = legend.getSelectedLayer();

        if (currentLayer != null) {
            int position = context.indexOf(currentLayer);

            if (position < (context.getLayerCount() - 1)) {
                context.moveLayer(position, position + 1);
            }
        }
    }
}
