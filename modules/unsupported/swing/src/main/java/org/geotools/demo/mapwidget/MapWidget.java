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

package org.geotools.demo.mapwidget;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ResourceBundle;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JToolBar;
import javax.swing.filechooser.FileFilter;
import net.miginfocom.swing.MigLayout;

import org.geotools.data.DataStore;
import org.geotools.data.DefaultRepository;
import org.geotools.data.Repository;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.gui.swing.JMapPane;
import org.geotools.gui.swing.MapLayerTable;
import org.geotools.gui.swing.StatusBar;
import org.geotools.gui.swing.action.PanAction;
import org.geotools.gui.swing.action.ResetAction;
import org.geotools.gui.swing.action.ZoomInAction;
import org.geotools.gui.swing.action.ZoomOutAction;
import org.geotools.map.DefaultMapContext;
import org.geotools.map.DefaultMapLayer;
import org.geotools.map.MapContext;
import org.geotools.map.MapLayer;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.geotools.renderer.lite.StreamingRenderer;
import org.geotools.styling.SLDParser;
import org.geotools.styling.Style;
import org.geotools.styling.StyleFactory;
import org.opengis.referencing.crs.CoordinateReferenceSystem;


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
public class MapWidget extends JFrame {

    private static final ResourceBundle stringRes = ResourceBundle.getBundle("org/geotools/gui/swing/MapWidget");

    private JMapPane mapPane;
    private MapLayerTable mapLayerTable;
    private StatusBar statusBar;
    private DefaultRepository repository = new DefaultRepository();    
    private MapContext context;

    private File cwd;

    /**
     * Main function. Creates and displays a MapWidget object.
     *
     * @param args ignored presently
     */
    public static void main(String[] args) {
        final MapWidget widget = new MapWidget(stringRes.getString("MapWidget_demo_title"));

        File dataDir = new File(MapWidget.class.getResource("/data").getPath());
        widget.setWorkingDir(dataDir);

        widget.setSize(500, 500);
        widget.setVisible(true);
    }

    /**
     * Default constructor
     */
    public MapWidget() {
        this(null);
    }

    /**
     * Constructor
     * @param title text to be displayed in the frame's title bar
     */
    public MapWidget(String title) {
        super(title);
        setupUI();
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    /**
     * Set the current working directory
     *
     * @param cwd a File object representing the directory; if NULL the user's default
     * directory will be set
     */
    public void setWorkingDir(File cwd) {
        if (!cwd.isDirectory()) {
            throw new IllegalArgumentException(stringRes.getString("arg_not_dir_error"));
        }

        this.cwd = cwd;
    }

    /**
     * Create a basic interface with a menu bar, tool bar, map pane,
     * map layer table and status bar
     */
    private void setupUI() {
        mapPane = new JMapPane();
        mapPane.setBackground(Color.WHITE);

        mapLayerTable = new MapLayerTable(mapPane);

        /*
         * Create menus and menu items
         */
        JMenuBar menuBar = new JMenuBar();
        JMenuItem item;

        JMenu fileMenu = new JMenu(stringRes.getString("file_menu"));

        item = new JMenuItem(stringRes.getString("file_open_local_shapefile"));
        item.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                try {
                    loadShapefile();
                } catch (Exception ex) {
                    throw new RuntimeException(ex);
                }
            }
        });
        fileMenu.add(item);

        menuBar.add(fileMenu);

        /*
         * A toolbar with buttons for zooming in, zooming out,
         * panning, and resetting the map to its full extent.
         * The cursor tool buttons (zooming and panning) are put
         * in a ButtonGroup.
         *
         * Note the use of the XXXAction objects which makes constructing
         * the tool bar buttons very simple.
         */
        JToolBar toolBar = new JToolBar();
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

        /*
         * We use the MigLayout manager to make it easy to manually code
         * our UI design
         */
        JPanel panel = new JPanel(new MigLayout(
                "wrap 1, insets 0", // layout constrains: 1 component per row, no insets
                
                "[grow]", // column constraints: col grows when frame is resized

                "[][][grow][30px::]" // row constraints: first two rows are fixed size, third row
                                     // (map pane and layer table) grows, last row (status bar)
                                     // fixed with min size of 30 pixels
        ));

        panel.add(menuBar, "grow");
        panel.add(toolBar, "grow");

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

        statusBar = new StatusBar(mapPane);
        panel.add(statusBar, "grow");

        this.getContentPane().add(panel);
    }

    /**
     * Add the contents of a shapefile as a new layer. This method
     * simply calls {@linkplain #showOpenShapefileDialog} followed by
     * {@linkplain #addShapefile} with the {@code defaultStyle}
     * argument of the latter method set to {@code true}.
     */
    public void loadShapefile() throws IOException {
        File file = showOpenShapefileDialog();
        if (file != null) {
        	addShapefile(file.toURL(), true);
            setWorkingDir(file.getParentFile());
        }
    }

    /**
     * Display an open file dialog for shapefiles.
     *
     * @return the selected file; or null if none was selected
     */
    public File showOpenShapefileDialog() {
        // Note: cwd == NULL is safe here
        JFileChooser chooser = new JFileChooser(cwd);
        
        chooser.setFileFilter(new FileFilter() {

            @Override
            public boolean accept(File f) {
                if (f.isDirectory()) {
                    return true;
                }

                return (f.getName().endsWith(".shp"));
            }

            @Override
            public String getDescription() {
                return stringRes.getString("shape_files");
            }
        });

        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            return chooser.getSelectedFile();
        } else {
            return null;
        }
    }

    /**
     * Open a shapefile and add it to the map layers displayed
     * by the widget, styling the layer as specified by the
     * associated SLD file (same root name as shapefile with .sld
     * extension) if it exists.
     * <p>
     * To add a shapefile with a Style from elsewhere, e.g. one
     * constructed programmitcally, use the
     * {@linkplain #addShapefile(java.net.URL, org.geotools.styling.Style)} method.
     * Alternatively, create a MapLayer object and use the
     * {@linkplain #addLayer} method.
     *
     * @param shapefile URL for the shapefile to add
     * @param defaultStyle specifies what action to take if there is no
     * associated SLD file: fallback to a minimal style (if true) or
     * abort adding the layer (if false)
     *
     * @return true if the layer was added successfully; false otherwise
     *
     * @throws IllegalArgumentException if shapefileURL is null
     */
    public boolean addShapefile(URL shapefileURL, boolean defaultStyle) throws IOException {
        if (shapefileURL == null) {
            throw new IllegalArgumentException(stringRes.getString("null_arg_error"));
        }
        ShapefileDataStore dstore = null;
        
        DataStore found = repository.dataStore( shapefileURL.toString());
        if( found != null && found instanceof ShapefileDataStore){
        	dstore = (ShapefileDataStore) found;
        }
        else {
	        try {
	            dstore = new ShapefileDataStore(shapefileURL);
	        } catch (MalformedURLException urlEx) {
	            throw new RuntimeException(urlEx);
	        }
	        repository.register( shapefileURL.toString(), dstore );
        }
        /*
         * Before doing anything else we attempt to connect to the 
         * shapefile to check that it exists and is reachable. An
         * IOException will be thrown if this fails.
         */
        dstore.getSchema();

        /*
         * We assume from this point that the shapefile exists and
         * is accessible
         */
        String typeName = dstore.getTypeNames()[0];
        Style style = null;
        URL sldURL = getShapefileSLD(shapefileURL);

        if (sldURL != null) {
            /*
             * The shapefile has an associated SLD file. We read this and
             * use the (first) style for the new layer
             */
            StyleFactory factory = CommonFactoryFinder.getStyleFactory(null);
            SLDParser stylereader = new SLDParser(factory, sldURL);
            style = stylereader.readXML()[0];

        } else if (defaultStyle) {
            /*
             * There was no associated SLD file so we attempt to create
             * a minimal style to display the layer
             */
            style = ReallyBasicStyleMaker.createBasicStyle(dstore, typeName);

        } else {
            /*
             * We are not having a good day...
             */
            return false;
        }

        MapLayer layer = new DefaultMapLayer(dstore.getFeatureSource(typeName), style);
        addLayer(layer);
        return true;
    }

    /**
     * Open a shapefile and add it to the map layers displayed
     * by the widget, rendering the layer with the provided style.
     * <p>
     *
     * @param shapefile URL for the shapefile to add
     * @param style the Syle object to use in rendering this layer
     *
     * @return true if the layer was added successfully; false otherwise
     *
     * @throws IOException if the shapefile could not be accessed
     * @throws IllegalArgumentException if either of the arguments is null
     */
    public boolean addShapefile(URL shapefileURL, Style style) throws IOException {
        if (shapefileURL == null || style == null) {
            throw new IllegalArgumentException(stringRes.getString("null_arg_error"));
        }
        ShapefileDataStore dstore = null;
        
        DataStore found = repository.dataStore( shapefileURL.toString());
        if( found != null && found instanceof ShapefileDataStore){
        	dstore = (ShapefileDataStore) found;
        }
        else {
	        try {
	            dstore = new ShapefileDataStore(shapefileURL);
	        } catch (MalformedURLException urlEx) {
	            throw new RuntimeException(urlEx);
	        }
	        repository.register( shapefileURL.toString(), dstore );
        }        
        /*
         * Before doing anything else we attempt to connect to the
         * shapefile to check that it exists and is reachable. An
         * IOException will be thrown if this fails.
         */
        dstore.getSchema();

        /*
         * We assume from this point that the shapefile exists and
         * is accessible
         */
        String typeName = dstore.getTypeNames()[0];
        MapLayer layer = new DefaultMapLayer(dstore.getFeatureSource(typeName), style);
        addLayer(layer);
        return true;
    }

    /**
     * Search for a Styled Layer Descriptor (SLD) file associated with the
     * specified shapefile. If the SLD file exists it will be in the same
     * directory as the shapefile, have the same root name, and have
     * .sld as its extension.
     *
     * @param shapefileURL the shapefile for which an SLD file is being sought
     *
     * @return the URL of the SLD file; or null if not found or not accessible
     */
    public URL getShapefileSLD(URL shapefileURL) {
        URL sldURL = null;
        
        File shapefile;
		try {			
			shapefile = new File( shapefileURL.toURI() );
		} catch (URISyntaxException e) {
			shapefile = new File( shapefileURL.getPath() );
		}
        String fileName = shapefile.getName();

        int lastDot = fileName.lastIndexOf('.');
        if (lastDot > 0) {
            File directory = shapefile.getParentFile();
            String sldname1 = fileName.substring(0, lastDot) + ".sld";
            String sldname2 = fileName.substring(0, lastDot) + ".SLD";
            InputStream input=null;
            try {
            	File sldFile1 = new File( directory, sldname1 );
            	File sldFile2 = new File( directory, sldname2 );
            	if( sldFile1.exists() && sldFile1.canRead() ){
            		sldURL = sldFile1.toURL();	
            	}
            	else if( sldFile1.exists() && sldFile1.canRead() ){
            		sldURL = sldFile2.toURL();	
            	}
            	else {
            		/*
                     * The SLD file can't be opened so we return null
                     */
                    return null;
            	}
                /*
                 * Now we check to see if the url that we have created
                 * corresponds to an existing and accessible SLD file.
                 * If it doesn't, this call to openStream() will provoke
                 * an IOException
                 */
                input = sldURL.openStream();
            } catch (MalformedURLException urlEx) {
                throw new RuntimeException(urlEx);
            } catch (IOException ioEx) {
                /*
                 * The SLD file can't be opened so we return null
                 */
                return null;
            }
            finally {
            	if( input != null) {
            		try {
						input.close();
					} catch (IOException e) {
					}
            	}
            }
        }

        return sldURL;
    }

    /**
     * Get the MapContext object associated with this MapWidget.
     * If no map context has been set explicitly with
     * {@linkplain #setMapContext} or implicitly via
     * {@linkplain #addLayer} then null will be returned.
     *
     * @return the current MapContext object
     */
    public MapContext getMapContext() {
        return context;
    }

    /**
     * Set the MapContext object used by this MapWidget.
     *
     * @param context a MapContext instance
     * @throws IllegalArgumentException if context is null
     */
    public void setMapContext(MapContext context) {
        if (context == null) {
            throw new IllegalArgumentException(stringRes.getString("arg_null_error"));
        }

        this.context = context;
    }

    /**
     * Add a map layer to those displayed. If no {@linkplain org.geotools.map.MapContext}
     * has been set explicitly, a new instance of {@linkplain org.geotools.map.DefaultMapContext}
     * will be created.
     */
    public void addLayer(MapLayer layer) {
        if (context == null) {
            CoordinateReferenceSystem crs = layer.getBounds().getCoordinateReferenceSystem();
            if (crs == null) {
                crs = DefaultGeographicCRS.WGS84;
            }
            context = new DefaultMapContext(crs);
            mapPane.setContext(context);
            mapPane.setRenderer(new StreamingRenderer());
        }

        context.addLayer(layer);
    }

}
