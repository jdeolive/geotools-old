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
package org.geotools.demos;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Polygon;
import org.geotools.cs.CoordinateSystemFactory;
import org.geotools.cs.HorizontalDatum;
import org.geotools.ct.Adapters;
import org.geotools.data.DataSource;
import org.geotools.data.MemoryDataSource;
import org.geotools.feature.AttributeType;
import org.geotools.feature.AttributeTypeDefault;
import org.geotools.feature.Feature;
import org.geotools.feature.FeatureType;
import org.geotools.feature.FeatureTypeFlat;
import org.geotools.feature.FlatFeatureFactory;
import org.geotools.feature.IllegalFeatureException;
import org.geotools.gui.swing.MapPaneImpl;
import org.geotools.gui.swing.ToolMenu;
import org.geotools.gui.tools.PanTool;
import org.geotools.gui.tools.Tool;
import org.geotools.gui.tools.ToolFactory;
import org.geotools.gui.tools.ToolList;
import org.geotools.gui.tools.ZoomTool;
import org.geotools.map.BoundingBox;
import org.geotools.map.Context;
import org.geotools.map.ContextFactory;
import org.geotools.map.Layer;
import org.geotools.map.LayerList;
import org.geotools.styling.SLDStyle;
import org.geotools.styling.Style;
import org.geotools.styling.StyleFactory;
import org.opengis.cs.CS_CoordinateSystem;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.util.logging.Logger;
import javax.swing.*;


/**
 * A demonstration of a Map Viewer which uses geotools2.
 *
 * @author Cameron Shorter
 * @version $Id: MapViewer.java,v 1.19 2003/05/30 12:31:29 camerons Exp $
 */
public class MapViewer {
    /** The class used for identifying for logging. */
    private static final Logger LOGGER = Logger.getLogger(
            "org.geotools.demos.MapViewer");

    /** Translates between coordinate systems */
    private Adapters adapters = Adapters.getDefault();

    /** The context which contains this maps data */
    private Context context;

    /**
     * Creates new form MapViewer
     */
    public MapViewer() {
        initComponents(createMapPane());
    }

    /**
     * This method is called from within the constructor to initialize the
     * form.
     *
     * @return A demo mapPane
     *
     * @throws RuntimeException When there is a FactoryException or Problem
     *         reading the SLD style sheets.
     *
     * @task TODO remove references to RemoteException when we can.
     */
    private MapPaneImpl createMapPane() {
        BoundingBox bbox;
        Envelope envelope;
        MapPaneImpl mapPane;
        LayerList layerList;
        Layer layer;
        Tool tool;

        try {
            ContextFactory contextFactory = ContextFactory.createFactory();

            // Create a Style
            StyleFactory styleFactory = StyleFactory.createStyleFactory();
            SLDStyle sldStyle = new SLDStyle(styleFactory,
                    ClassLoader.getSystemResource(
                        "org/geotools/demos/simple.sld"));
            Style[] style = sldStyle.readXML();

            // Create a DataSource
            MemoryDataSource datasource1 = new MemoryDataSource();
            populateDataSource(datasource1, 46, 46, "river");

            MemoryDataSource datasource2 = new MemoryDataSource();
            populateDataSource(datasource2, 50, 50, "road");

            // Create a Context
            context = contextFactory.createContext();
            layer = contextFactory.createLayer(datasource1, style[0]);
            layer.setTitle("river layer");
            context.getLayerList().addLayer(layer);

            // Create Layers
            layer = contextFactory.createLayer(datasource2, style[0]);
            layer.setTitle("road layer");
            context.getLayerList().addLayer(layer);

            // Create MapPane
            mapPane = new MapPaneImpl(context);
            mapPane.setBorder(new javax.swing.border.TitledBorder("MapPane Map"));
            mapPane.setBackground(Color.BLACK);
            mapPane.setPreferredSize(new Dimension(300, 300));
        } catch (IllegalFeatureException e) {
            LOGGER.warning("Error styling features.  Cause is: " +
                e.getCause());
            throw new RuntimeException();
        }

        return mapPane;
    }

    private void initComponents(MapPaneImpl mapPane) {
        // Create Menu for tools
        JMenuBar menuBar = new javax.swing.JMenuBar();

        // Create frame
        JFrame frame = new JFrame();
        frame.addWindowListener(new java.awt.event.WindowAdapter() {
                public void windowClosing(java.awt.event.WindowEvent evt) {
                    exitForm(evt);
                }
            });

        ToolMenu toolMenu = new ToolMenu(mapPane.getToolList());
        menuBar.add(toolMenu);
        frame.setJMenuBar(menuBar);
        frame.getContentPane().setLayout(new BorderLayout());
        frame.getContentPane().add(mapPane, "North");
        frame.setTitle("Map Viewer");
        frame.pack();
        frame.show();
    }

    /**
     * Create a test LineString.
     *
     * @param geomFac The geometry factory to use
     * @param xoff The starting x postion
     * @param yoff The starting y position
     *
     * @return a line
     */
    private LineString makeSampleLineString(final GeometryFactory geomFac,
        double xoff, double yoff) {
        Coordinate[] linestringCoordinates = new Coordinate[8];
        linestringCoordinates[0] = new Coordinate(5.0d + xoff, 5.0d + yoff);
        linestringCoordinates[1] = new Coordinate(6.0d + xoff, 5.0d + yoff);
        linestringCoordinates[2] = new Coordinate(6.0d + xoff, 6.0d + yoff);
        linestringCoordinates[3] = new Coordinate(7.0d + xoff, 6.0d + yoff);
        linestringCoordinates[4] = new Coordinate(7.0d + xoff, 7.0d + yoff);
        linestringCoordinates[5] = new Coordinate(8.0d + xoff, 7.0d + yoff);
        linestringCoordinates[6] = new Coordinate(8.0d + xoff, 8.0d + yoff);
        linestringCoordinates[7] = new Coordinate(8.0d + xoff, 9.0d + yoff);

        LineString line = geomFac.createLineString(linestringCoordinates);

        return line;
    }

    /**
     * Create a test LineString.
     *
     * @param geomFac The geometry factory to use
     *
     * @return a line
     */
    private LineString makeSampleLineString2(final GeometryFactory geomFac) {
        Coordinate[] linestringCoordinates = new Coordinate[4];
        linestringCoordinates[0] = new Coordinate(-170.0d, -80.0d);
        linestringCoordinates[1] = new Coordinate(-170.0d, 80.0d);
        linestringCoordinates[2] = new Coordinate(170.0d, 80.0d);
        linestringCoordinates[3] = new Coordinate(170.0d, -80.0d);

        LineString line = geomFac.createLineString(linestringCoordinates);

        return line;
    }

    /**
     * Add some features into a dataSouce
     *
     * @param dataSource The dataSource to populate
     * @param xoff The starting x postion
     * @param yoff The starting y position
     * @param featureTypeName The type of feature
     *
     * @throws IllegalFeatureException for IllegalFeatures
     */
    private void populateDataSource(MemoryDataSource dataSource, double xoff,
        double yoff, String featureTypeName) throws IllegalFeatureException {
        GeometryFactory geomFac = new GeometryFactory();
        LineString line = makeSampleLineString(geomFac, xoff, yoff);
        AttributeType lineAttribute = new AttributeTypeDefault("centerline",
                line.getClass());
        FeatureType lineType = new FeatureTypeFlat(lineAttribute).setTypeName(featureTypeName);
        FlatFeatureFactory lineFac = new FlatFeatureFactory(lineType);
        Feature lineFeature = lineFac.create(new Object[] { line });

        LineString line2 = makeSampleLineString(geomFac, xoff + 2, yoff);
        lineType = new FeatureTypeFlat(lineAttribute).setTypeName(featureTypeName);
        lineFac = new FlatFeatureFactory(lineType);

        Feature lineFeature2 = lineFac.create(new Object[] { line2 });

        LineString line3 = makeSampleLineString(geomFac, xoff + 4, yoff);
        lineType = new FeatureTypeFlat(lineAttribute).setTypeName(featureTypeName);
        lineFac = new FlatFeatureFactory(lineType);

        Feature lineFeature3 = lineFac.create(new Object[] { line3 });

        LineString line4 = makeSampleLineString2(geomFac);
        lineType = new FeatureTypeFlat(lineAttribute).setTypeName(featureTypeName);
        lineFac = new FlatFeatureFactory(lineType);

        Feature lineFeature4 = lineFac.create(new Object[] { line4 });

        dataSource.addFeature(lineFeature);
        dataSource.addFeature(lineFeature2);
        dataSource.addFeature(lineFeature3);
        dataSource.addFeature(lineFeature4);
    }

    /**
     * Exit the Application
     *
     * @param evt the event
     */
    private void exitForm(java.awt.event.WindowEvent evt) {
        System.exit(0);
    }

    /**
     * The MapViewer main program.
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        new MapViewer(); //.show();
    }
}
