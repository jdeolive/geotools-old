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
/*
 * SLDStyleSuite.java
 * JUnit based test
 *
 * $Id: LegendTest.java,v 1.3 2004/05/01 21:18:37 jmacgill Exp $
 */
package org.geotools.gui.swing;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.geom.Point2D;
import java.io.File;
import java.net.URL;
import java.net.URLDecoder;
import java.util.HashMap;

import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;

import junit.framework.Test;
import junit.framework.TestCase;

import org.geotools.cs.CoordinateSystem;
import org.geotools.cs.CoordinateSystemFactory;
import org.geotools.cs.Ellipsoid;
import org.geotools.cs.GeographicCoordinateSystem;
import org.geotools.cs.HorizontalDatum;
import org.geotools.cs.Projection;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.gui.swing.sldeditor.SLDEditor;
import org.geotools.map.DefaultMapContext;
import org.geotools.map.DefaultMapLayer;
import org.geotools.map.MapContext;
import org.geotools.map.MapLayer;
import org.geotools.styling.BasicLineStyle;
import org.geotools.styling.BasicPolygonStyle;
import org.geotools.styling.SLDStyle;
import org.geotools.styling.Style;
import org.geotools.styling.StyleFactory;
import org.geotools.units.Unit;


/**
 * DOCUMENT ME!
 *
 * @author iant
 */
public class LegendTest extends TestCase {
    /**
     * The context which contains this maps data
     *
     * @param testName DOCUMENT ME!
     */
    public LegendTest(java.lang.String testName) {
        super(testName);
    }

    public void testLegend() throws java.io.UnsupportedEncodingException{
        MapLayer[] layers;
        MapContext context;

        URL shpData0 = null;
        URL shpData1 = null;
        URL shpData2 = null;

        URL base = getClass().getResource("testdata/");

        System.out.println("base: " + base.toString());

        try {
	        shpData2 = new File(base.getPath() + "/testPoint.shp").toURL();
            shpData1 = new File(base.getPath() + "/testLine.shp").toURL();
            shpData0 = new File(base.getPath() + "/testPolygon.shp").toURL();
        } catch (Exception e) {
            e.printStackTrace();
        }

        URL[] data = new URL[] { shpData0, shpData1, shpData2 };

        File sldFile = new File(URLDecoder.decode(base.getPath(),"UTF-8") + "/color.sld");
        SLDStyle sld = null;
        
        SLDEditor.propertyEditorFactory.setInExpertMode(true);

        try {
            sld = new SLDStyle(StyleFactory.createStyleFactory(), sldFile);
        } catch (java.io.FileNotFoundException e) {
            e.printStackTrace();
        }

        Style[] styles = sld.readXML();
        System.out.println("user style num: " + styles.length);

        try {
            context = new DefaultMapContext();

            ShapefileDataStore[] dataStores = new ShapefileDataStore[data.length];
            layers = new MapLayer[data.length];

            for (int i = 0; i < data.length; i++) {
                HashMap params = new HashMap();
                params.put("url", data[i].toString());
                dataStores[i] = new ShapefileDataStore(data[i]);
            }

            StyleFactory styleFactory = StyleFactory.createStyleFactory();
            BasicPolygonStyle style = new BasicPolygonStyle(styleFactory.getDefaultFill(),
                    styleFactory.getDefaultStroke());
            style.setTitle("Leeds ED Poly");

            BasicLineStyle lineStyle = new BasicLineStyle(styleFactory.getDefaultStroke());
            lineStyle.setTitle("UK Motorway Basic");
            lineStyle.getFeatureTypeStyles()[0].getRules()[0].setTitle("Motorway");

            System.out.println("Loading Data");

            layers[0] = new DefaultMapLayer(dataStores[0].getFeatureSource(dataStores[0].getTypeNames()[0]), styles[0], "Leeds Ward Layer");
            layers[1] = new DefaultMapLayer(dataStores[1].getFeatureSource(dataStores[1].getTypeNames()[0]), lineStyle, "Leeds ED Layer");
            layers[2] = new DefaultMapLayer(dataStores[2].getFeatureSource(dataStores[2].getTypeNames()[0]), styles[1], "UK MotorWays Basic");

            System.out.println("create Coodinate System....1");

            Ellipsoid airy1830 = Ellipsoid.createEllipsoid("Airy1830", 6377563.396, 6356256.910,
                    Unit.METRE);
            System.out.println("create Coodinate System....2" + airy1830.toString());

            GeographicCoordinateSystem geogCS = CoordinateSystemFactory.getDefault()
                                                                       .createGeographicCoordinateSystem("Airy1830",
                    new HorizontalDatum("Airy1830", airy1830));
            System.out.println("create Coodinate System....3" + geogCS.toString());

            Projection p = new Projection("Great_Britian_National_Grid", "Transverse_Mercator",
                    airy1830, new Point2D.Double(49, -2), new Point2D.Double(400000, -100000));

            System.out.println("create Coodinate System....4" + p.toString());

            CoordinateSystem projectCS = CoordinateSystemFactory.getDefault()
                                                                .createProjectedCoordinateSystem("Great_Britian_National_Grid",
                    geogCS, p);

            System.out.println("create Context");

            context.addLayers(layers);

            System.out.println("creating Map Pane");

            // Create MapPane
            StyledMapPane mapPane = new StyledMapPane();
            mapPane.setMapContext(context);
            System.out.println("Creating Map Pane Done");
            mapPane.setBackground(Color.WHITE);
            mapPane.setPreferredSize(new Dimension(800, 800));

            // Create Menu for tools
            JMenuBar menuBar = new javax.swing.JMenuBar();
            System.out.println("creating MenuBar for Map Pane...");

            // Create frame
            JFrame frame = new JFrame();
            frame.addWindowListener(new java.awt.event.WindowAdapter() {
                    public void windowClosing(java.awt.event.WindowEvent evt) {
                        System.exit(0);
                    }
                });

            frame.getContentPane().setLayout(new BorderLayout());
            System.out.println("Add Map Pane into Frame");
            
            JSplitPane splitPane = new JSplitPane();
            splitPane.add(new Legend(context, "LegendTest"), JSplitPane.LEFT);
            splitPane.add(mapPane, JSplitPane.RIGHT);
            splitPane.setDividerLocation(200);
            frame.setContentPane(splitPane);            

            frame.setTitle("Map Viewer");
            frame.setSize(600, 400);
            frame.show();

            frame.dispose();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(suite());
    }

    public static Test suite() {
        return new junit.framework.TestSuite(LegendTest.class);
    }
   
    
    
}
