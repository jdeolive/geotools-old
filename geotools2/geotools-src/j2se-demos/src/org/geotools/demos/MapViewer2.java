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
package org.geotools.demos;

// J2SE dependencies
import java.net.URL;
import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.awt.BorderLayout;
import java.awt.Container;
import javax.swing.JFrame;

// Geotools dependencies
import org.geotools.map.Layer;
import org.geotools.map.Context;
import org.geotools.map.ContextFactory;
import org.geotools.styling.Style;
import org.geotools.styling.StyleBuilder;
import org.geotools.feature.FeatureCollection;
import org.geotools.data.DataSource;
import org.geotools.data.DataSourceException;
import org.geotools.data.shapefile.ShapefileDataSource;
import org.geotools.renderer.j2d.RenderedMapScale;
import org.geotools.gui.swing.StyledMapPane;
import org.geotools.gui.swing.StatusBar;


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
 * @version $Id: MapViewer2.java,v 1.27 2003/08/20 22:04:06 desruisseaux Exp $
 */
public class MapViewer2 {
    /**
     * Run the test from the command line. If arguments are provided, then the first
     * argument is understood as the filename of the shapefile to load.
     *
     * @throws IOException is a I/O error occured.
     * @throws DataSource if an error occured while reading the data source.
     */
    public static void main(final String[] args) throws IOException, DataSourceException {
        final MapViewer2 viewer = new MapViewer2();
        final Context context;
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
    protected Context loadContext() throws IOException, DataSourceException {
        return loadContext(getClass().getClassLoader().getResource("testData/statepop.shp"));
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
    protected Context loadContext(final URL url) throws IOException, DataSourceException {

        // Load the file
        if (url == null) {
            throw new FileNotFoundException("Resource not found");
        }
        final DataSource      datasource = new ShapefileDataSource(url);
        final FeatureCollection features = datasource.getFeatures();

        // Create the style
        final StyleBuilder builder = new StyleBuilder();
        final Style style = builder.createStyle(builder.createPolygonSymbolizer(
                                                Color.ORANGE, Color.BLACK, 1));

        // Create the context
        final ContextFactory factory = ContextFactory.createFactory();
        final Context        context = factory.createContext();
        final Layer          layer   = factory.createLayer(features, style);
        layer.setTitle("The shapefile");
        context.getLayerList().addLayer(layer);
        context.getBoundingBox().setAreaOfInterest(features.getBounds());
        context.setTitle("Hello World");
        return context;
    }

    /**
     * Create and show the map pane.
     *
     * @param context The context to show.
     */
    protected void showMapPane(final Context context) {
        // Create the map pane and add a map scale layer to it.
        final StyledMapPane mapPane = new StyledMapPane();
        mapPane.setContext(context);
        mapPane.setPaintingWhileAdjusting(true);
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
