/*
 *    Geotools - OpenSource mapping toolkit
 *    (C) 2002, Centre for Computational Geography
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
 *    You should have received a copy of the GNU Lesser General Public
 *    License along with this library; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 */

package org.geotools.demos;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Polygon;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.BorderLayout;
import java.util.logging.Logger;
import javax.swing.*;
import org.geotools.cs.CoordinateSystemFactory;
import org.geotools.cs.HorizontalDatum;
import org.geotools.ct.Adapters;
import org.geotools.data.DataSource;
import org.geotools.data.MemoryDataSource;
import org.geotools.feature.AttributeType;
import org.geotools.feature.AttributeTypeDefault;
import org.geotools.feature.Feature;
import org.geotools.feature.FeatureFactory;
import org.geotools.feature.FeatureType;
import org.geotools.feature.FeatureTypeFlat;
import org.geotools.gui.swing.MapPaneImpl;
import org.geotools.gui.tools.Tool;
import org.geotools.gui.tools.PanTool;
import org.geotools.gui.tools.ToolFactory;
import org.geotools.gui.tools.ZoomTool;
import org.geotools.map.BoundingBox;
import org.geotools.map.Context;
import org.geotools.map.ContextFactory;
import org.geotools.map.Layer;
import org.geotools.map.LayerList;
import org.geotools.map.ToolList;
import org.geotools.styling.SLDStyle;
import org.geotools.styling.Style;
import org.geotools.styling.StyleFactory;
import org.opengis.cs.CS_CoordinateSystem;

/**
 * A demonstration of a Map Viewer which uses geotools2.
 *
 * @author Cameron Shorter
 * @version $Id: MapViewer.java,v 1.9 2003/03/30 20:06:42 camerons Exp $
 *
 */

public class MapViewer {

    /** Translates between coordinate systems */
    private Adapters adapters = Adapters.getDefault();

    /**
     * The class used for identifying for logging.
     */
    private static final Logger LOGGER = Logger.getLogger("org.geotools.demos.MapViewer");
    
    /** The context which contains this maps data */
    private Context context;

    /** Creates new form MapViewer */
    public MapViewer() {
        initComponents(createMapPane());
    }

    /** This method is called from within the constructor to
     * initialize the form.
     */
    private MapPaneImpl createMapPane() {

        BoundingBox bbox;
        Envelope envelope;
        MapPaneImpl mapPane;
        LayerList layerList;
        Layer layer;
        Tool tool;


        try {
            ContextFactory contextFactory=ContextFactory.createFactory();
            
            // Create a BoundingBox
            envelope=new Envelope(50,60,50,60);
            CS_CoordinateSystem cs = adapters.export(
                CoordinateSystemFactory.getDefault(
                    ).createGeographicCoordinateSystem(
                        "WGS84",HorizontalDatum.WGS84));
            bbox=contextFactory.createBoundingBox(envelope,cs);

            // Create a Style
            StyleFactory styleFactory=StyleFactory.createStyleFactory();
            SLDStyle sldStyle = new SLDStyle(
                styleFactory,
                ClassLoader.getSystemResource("org/geotools/demos/simple.sld"));
            Style[] style=sldStyle.readXML();

            // Create a DataSource
            MemoryDataSource datasource1 = new MemoryDataSource();
            populateDataSource(datasource1,46,46,"river");
            MemoryDataSource datasource2 = new MemoryDataSource();
            populateDataSource(datasource2,50,50,"road");

            // Create a LayerList and Layer
            layerList=contextFactory.createLayerList();
            layer=contextFactory.createLayer(datasource1,style[0]);
            layer.setTitle("river layer");
            layerList.addLayer(layer);

            layer=contextFactory.createLayer(datasource2,style[0]);
            layer.setTitle("road layer");
            layerList.addLayer(layer);

            // Create Tool
            ToolFactory toolFactory=ToolFactory.createFactory();
            tool=toolFactory.createPanTool();
            
            // Create SelectedTool
            ToolList selectedTool=contextFactory.createToolList(
                tool);

            // Create a Context
            context=contextFactory.createContext(
                bbox,             // BoundingBox
                layerList,        // LayerList
                selectedTool,     // SelectedTool
                "defaultContext", // Title
                null,             // Abstract
                null,             // Keywords
                null);            // ContactInformation

            // Create MapPane
            mapPane=new MapPaneImpl(context);
            mapPane.setBorder(
                new javax.swing.border.TitledBorder("MapPane Map"));
            mapPane.setBackground(Color.BLACK);
            mapPane.setPreferredSize(new Dimension(300,300));

         } catch (Exception e){
            LOGGER.warning("Exception: "+e+" initialising MapView.");
            return null;
        }
        return mapPane;
   }

    private void initComponents(MapPaneImpl mapPane){
        // Create Menu for tools
        JMenuBar menuBar = new javax.swing.JMenuBar();
        JMenu toolMenu = new javax.swing.JMenu();
        JMenuItem panMenuItem = new javax.swing.JMenuItem();
        JMenuItem zoomInMenuItem = new javax.swing.JMenuItem();
        JMenuItem zoomOutMenuItem = new javax.swing.JMenuItem();
        JMenuItem zoomPanMenuItem = new javax.swing.JMenuItem();
        JMenuItem noToolMenuItem = new javax.swing.JMenuItem();

        toolMenu.setText("Tool");

        panMenuItem.setText("Pan");
        panMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                panActionPerformed(evt);
            }
        });
        toolMenu.add(panMenuItem);
        
        zoomInMenuItem.setText("Zoom In");
        zoomInMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                zoomInActionPerformed(evt);
            }
        });
        toolMenu.add(zoomInMenuItem);
        
        zoomOutMenuItem.setText("Zoom Out");
        zoomOutMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                zoomOutActionPerformed(evt);
            }
        });
        toolMenu.add(zoomOutMenuItem);
        
        zoomPanMenuItem.setText("Zoom Pan");
        zoomPanMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                zoomPanActionPerformed(evt);
            }
        });
        toolMenu.add(zoomPanMenuItem);
        
        noToolMenuItem.setText("No Tool");
        noToolMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                noToolActionPerformed(evt);
            }
        });
        toolMenu.add(noToolMenuItem);


        // Create frame
        JFrame frame=new JFrame();
        frame.addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                exitForm(evt);
            }
        });

        menuBar.add(toolMenu);
        frame.setJMenuBar(menuBar);
        frame.getContentPane().setLayout(new BorderLayout());
        frame.getContentPane().add(
            mapPane,"North");
        frame.setTitle("Map Viewer");
        frame.pack();
        frame.show();
    }

    private void panActionPerformed(java.awt.event.ActionEvent evt){
        ToolFactory toolFactory=ToolFactory.createFactory();
        context.getSelectedTool().setTool(toolFactory.createPanTool());
    }

    private void zoomInActionPerformed(java.awt.event.ActionEvent evt){
        ToolFactory toolFactory=ToolFactory.createFactory();
        context.getSelectedTool().setTool(toolFactory.createZoomTool(2));
    }

    private void zoomOutActionPerformed(java.awt.event.ActionEvent evt){
        ToolFactory toolFactory=ToolFactory.createFactory();
        context.getSelectedTool().setTool(toolFactory.createZoomTool(0.5));
    }

    private void zoomPanActionPerformed(java.awt.event.ActionEvent evt){
        ToolFactory toolFactory=ToolFactory.createFactory();
        context.getSelectedTool().setTool(toolFactory.createZoomTool(1));
    }

    private void noToolActionPerformed(java.awt.event.ActionEvent evt){
        ToolFactory toolFactory=ToolFactory.createFactory();
        context.getSelectedTool().setTool(null);
    }

    /**
     * Create a test LineString.
     * @param geomFac The geometry factory to use
     * @param xoff The starting x postion
     * @param yoff The starting y position
     */
    private LineString makeSampleLineString(
        final GeometryFactory geomFac, 
        double xoff, 
        double yoff)
    {
        Coordinate[] linestringCoordinates = new Coordinate[8];
        linestringCoordinates[0] = new Coordinate(5.0d+xoff,5.0d+yoff);
        linestringCoordinates[1] = new Coordinate(6.0d+xoff,5.0d+yoff);
        linestringCoordinates[2] = new Coordinate(6.0d+xoff,6.0d+yoff);
        linestringCoordinates[3] = new Coordinate(7.0d+xoff,6.0d+yoff);
        linestringCoordinates[4] = new Coordinate(7.0d+xoff,7.0d+yoff);
        linestringCoordinates[5] = new Coordinate(8.0d+xoff,7.0d+yoff);
        linestringCoordinates[6] = new Coordinate(8.0d+xoff,8.0d+yoff);
        linestringCoordinates[7] = new Coordinate(8.0d+xoff,9.0d+yoff);
        LineString line = geomFac.createLineString(linestringCoordinates);
        
        return line;
    }

    /**
     * Add some features into a dataSouce
     * @param dataSource The dataSource to populate
     * @param xoff The starting x postion
     * @param yoff The starting y position
     */
    private void populateDataSource(
        MemoryDataSource dataSource,
        double xoff, 
        double yoff,
        String featureTypeName)throws Exception {
        
        GeometryFactory geomFac = new GeometryFactory();
        LineString line = makeSampleLineString(geomFac,xoff,yoff);
        AttributeType lineAttribute = new AttributeTypeDefault("centerline", line.getClass());
        FeatureType lineType = new FeatureTypeFlat(lineAttribute).setTypeName(featureTypeName);
        FeatureFactory lineFac = new FeatureFactory(lineType);
        Feature lineFeature = lineFac.create(new Object[]{line});
        
        LineString line2 = makeSampleLineString(geomFac,xoff+2,yoff);
        lineType = new FeatureTypeFlat(lineAttribute).setTypeName(featureTypeName);
        lineFac = new FeatureFactory(lineType);
        Feature lineFeature2 = lineFac.create(new Object[]{line2});
        
        LineString line3 = makeSampleLineString(geomFac,xoff+4,yoff);
        lineType = new FeatureTypeFlat(lineAttribute).setTypeName(featureTypeName);
        lineFac = new FeatureFactory(lineType);
        Feature lineFeature3 = lineFac.create(new Object[]{line3});
        
//        Polygon polygon = makeSamplePolygon(geomFac,0,0);
//        
//        AttributeType polygonAttribute = new AttributeTypeDefault("edge", polygon.getClass());
//        FeatureType polygonType = new FeatureTypeFlat(polygonAttribute).setTypeName("polygon");
//        FeatureFactory polygonFac = new FeatureFactory(polygonType);
//        
//        Feature polygonFeature = polygonFac.create(new Object[]{polygon});
//        
//        Polygon polygon2 = makeSamplePolygon(geomFac,0,150);
//        polygonType = new FeatureTypeFlat(polygonAttribute).setTypeName("polygontest2");
//        polygonFac = new FeatureFactory(polygonType);
//        Feature polygonFeature2 = polygonFac.create(new Object[]{polygon2});
//        
//        Polygon polygon3 = makeSamplePolygon(geomFac,220,100);
//        polygonType = new FeatureTypeFlat(polygonAttribute).setTypeName("polygontest3");
//        polygonFac = new FeatureFactory(polygonType);
//        Feature polygonFeature3 = polygonFac.create(new Object[]{polygon3});
//        
//        
//        Point point = makeSamplePoint(geomFac,140.0,140.0);
//        AttributeType pointAttribute = new AttributeTypeDefault("centre", point.getClass());
//        FeatureType pointType = new FeatureTypeFlat(pointAttribute).setTypeName("pointfeature");
//        FeatureFactory pointFac = new FeatureFactory(pointType);
//        
//        Feature pointFeature = pointFac.create(new Object[]{point});
//        MemoryDataSource datasource = new MemoryDataSource();
        dataSource.addFeature(lineFeature);
        dataSource.addFeature(lineFeature2);
        dataSource.addFeature(lineFeature3);
//        datasource.addFeature(polygonFeature);
//        datasource.addFeature(polygonFeature2);
//        datasource.addFeature(polygonFeature3);
//        datasource.addFeature(pointFeature);
    }

    /** Exit the Application */
    private void exitForm(java.awt.event.WindowEvent evt) {
        System.exit(0);
    }

    /**
    * @param args the command line arguments
    */
    public static void main(String args[]) {
        new MapViewer();//.show();
    }
}
