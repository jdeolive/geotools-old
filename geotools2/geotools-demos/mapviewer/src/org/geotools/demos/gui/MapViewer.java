/*
 *    Geotools2 - OpenSource mapping toolkit
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
 */
package org.geotools.demos.gui;

// J2SE dependencies
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;

import javax.swing.JFrame;

import org.geotools.data.DataSourceException;
import org.geotools.data.DataStore;
import org.geotools.data.FeatureSource;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.gui.swing.StatusBar;
import org.geotools.gui.swing.StyledMapPane;
import org.geotools.map.DefaultMapContext;
import org.geotools.map.DefaultMapLayer;
import org.geotools.map.MapContext;
import org.geotools.map.MapLayer;
import org.geotools.renderer.j2d.RenderedMapScale;
import org.geotools.styling.Style;
import org.geotools.styling.StyleBuilder;

import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.Point;


/**
 * Load and display a shape file. At the difference of the {@link MapViewer} demo, this demo
 * use {@link MapPane} with the {@linkplain Renderer J2D renderer}. This renderer has the
 * following advantages:
 * <ul>
 *   <li>faster;</li>
 *   <li>progressive rendering of tiled image;</li>
 *   <li>supports arbitrary map projections (different geometries to be rendered on the same
 *       map can have different coordinate systems);</li>
 *   <li>supports zooms, translations and rotations through mouse drag, mouse wheel, keyboard
 *       and contextual menu localized in English, French, Portuguese and partially in Spanish
 *       and Greek;</li>
 *   <li>provides a magnifier (accessible from the contextual menu, right button click);</li>
 *   <li>arbitrary amount of offscreen buffering;</li>
 *   <li>can display scroll bars;</li>
 *   <li>can display a status bar with mouse coordinates in an arbitrary coordinate system
 *       (it doesn't have to be the same coordinate system than the renderer one);</li>
 *   <li>has a more precise scale factor taking in account the physical size of the output
 *       device (when this information is available);</li>
 * </ul>
 *
 * The inconvenient is a more complex renderer, which is more difficult to modify for new users.
 * <br><br>
 * NOTE: While not essential, it is recommanded to run this demos in server mode, with:
 * <blockquote><pre>
 * java -server org.geotools.demos.MapViewer2 <I>thefile.shp</I>
 * </pre></blockquote>
 *
 * @author Martin Desruisseaux
 * @version $Id: MapViewer.java,v 1.3 2004/03/26 19:06:51 aaime Exp $
 */
public class MapViewer {
    /**
     * Run the test from the command line. If arguments are provided, then the first
     * argument is understood as the filename of the shapefile to load.
     *
     * @throws IOException is a I/O error occured.
     * @throws DataSource if an error occured while reading the data source.
     */
    public static void main(final String[] args) throws Exception {
        final MapViewer viewer = new MapViewer();
        final MapContext context;
        switch (args.length) {
            default: // Fall through
            case  1: context=viewer.loadContext(new File(args[0]).toURL()); break;
            case  0: context=viewer.loadContext();                          break;
        }
        viewer.showMapPane(context);
    }

    /**
     * Load the data from the shapefile <code>&quot;testData/statepop.shp&quot;</code>.
     * This file must be on the class path.
     *
     * @return Context The data from the shape file.
     * @throws FileNotFoundException if the shape file was not found.
     * @throws IOException is some other kind of I/O error occured.
     * @throws DataSource if an error occured while reading the data source.
     */
    protected MapContext loadContext() throws IOException, DataSourceException {
        return loadContext(getClass().getClassLoader().getResource("org/geotools/sampleData/statepop.shp"));
    }

    /**
     * Load the data from the specified shapefile and construct a {@linkplain Context context}
     * with a default style.
     *
     * @param  url The url of the shapefile to load.
     * @return Context The data from the shape file.
     * @throws IOException is a I/O error occured.
     * @throws DataSource if an error occured while reading the data source.
     */
    protected MapContext loadContext(final URL url) throws IOException, DataSourceException {

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
        if(LineString.class.isAssignableFrom(geometryClass) || MultiLineString.class.isAssignableFrom(geometryClass)) {
            style = builder.createStyle(builder.createLineSymbolizer());
        } else if(Point.class.isAssignableFrom(geometryClass) || MultiPoint.class.isAssignableFrom(geometryClass)) {
            style = builder.createStyle(builder.createPointSymbolizer());
        } else {
            style = builder.createStyle(builder.createPolygonSymbolizer(
                     Color.ORANGE, Color.BLACK, 1));
        }
         

        // Create the context
        MapContext context = new DefaultMapContext();
        MapLayer layer = new DefaultMapLayer(features, style);
        layer.setTitle("The shapefile");
        context.addLayer(layer);
        context.setTitle("Hello World");
        return context;
    }

    /** 
     * Create and show the map pane.
     *
     * @param context The context to show.
     */
    protected void showMapPane(final MapContext context) throws Exception {
        // Create the map pane and add a map scale layer to it.
        final StyledMapPane mapPane = new StyledMapPane();
        mapPane.setMapContext(context);
        mapPane.setPaintingWhileAdjusting(false);
        mapPane.getRenderer().addLayer(new RenderedMapScale());

        // Create the frame, add the map pane and a status bar.
        final JFrame frame = new JFrame(context.getTitle());
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        final Container container = frame.getContentPane();
        container.setLayout(new BorderLayout());
        container.add(mapPane.createScrollPane(), BorderLayout.CENTER);
        container.add(new StatusBar(mapPane),     BorderLayout.SOUTH);
        frame.pack();
        frame.show();
    }
}
