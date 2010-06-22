/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 * 
 *    (C) 2002-2010, Open Source Geospatial Foundation (OSGeo)
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
package org.geotools.renderer.chart;

import java.awt.Dimension;
import java.awt.HeadlessException;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;

import junit.framework.TestCase;

import org.geotools.test.TestData;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.XYPlot;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.io.WKTReader;

public class GeometryDatasetTest extends TestCase {

    WKTReader wkt;
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        
        wkt = new WKTReader();
    }
    
    public void testSimple() throws Exception {
        LineString line = (LineString) wkt.read("LINESTRING(0 0, 1 1)");
        render(new GeometryDataset(line));
    }
    
    public void testPolygon() throws Exception {
        Polygon poly = (Polygon) wkt.read("POLYGON((0 0, 10 0, 10 10, 0 10, 0 0))");
        render(new GeometryDataset(poly));
        render(new GeometryDataset(poly.buffer(0.5)));
        render(new GeometryDataset(wkt.read("POINT(0 0)").buffer(2)));
    }

    public void testPolygonWithHoles() throws Exception {
        Polygon poly = (Polygon) wkt.read("POLYGON((0 0, 10 0, 10 10, 0 10, 0 0),(2 2, 5 2, 5 5, 2 5, 2 2))");
        render(new GeometryDataset(poly));
    }
    
    public void testPoint() throws Exception {
        render(new GeometryDataset(wkt.read("POINT(0 0)")));
    }
    
    public void testMultiPolygon() throws Exception {
        Geometry g = wkt.read("MULTIPOLYGON(((1 1,5 1,5 5,1 5,1 1),(2 2,2 3,3 3,3 2,2 2)),((6 3,9 2,9 4,6 3))))");
        render(new GeometryDataset(g));
    }
    
    public void testGeometryCollection() throws Exception {
        Geometry g = wkt.read("GEOMETRYCOLLECTION(POINT(4 2),LINESTRING(4 6,7 10))");
        render(new GeometryDataset(g));
    }
    
    public void testGeometryCollections() throws Exception {
        Geometry g1 = wkt.read("MULTIPOINT((3.5 5.6),(4.8 10.5))");
        Geometry g2 = wkt.read("MULTILINESTRING((3 4,10 50,20 25),(-5 -8,-10 -8,-15 -4))");
        Geometry g3 = wkt.read("MULTIPOLYGON(((1 1,5 1,5 5,1 5,1 1),(2 2,2 3,3 3,3 2,2 2)),((6 3,9 2,9 4,6 3)))");
        render(new GeometryDataset(g1,g2,g3));
    }
    
    void render(GeometryDataset dataset) throws Exception {
        XYPlot plot = dataset.createPlot();
        showChart(plot);
    }
    
    void showChart(XYPlot plot) throws Exception {
        JFreeChart chart = new JFreeChart(plot);
        ChartPanel panel = new ChartPanel(chart);
        
        final String headless = System.getProperty("java.awt.headless", "false");
        if (!headless.equalsIgnoreCase("true") && TestData.isInteractiveTest()) {
            try {
                JFrame frame = new JFrame(getName());
                frame.addWindowListener(new WindowAdapter() {
                    public void windowClosing(WindowEvent e) {
                        e.getWindow().dispose();
                    }
                });
                frame.setContentPane(panel);
                frame.setSize(new Dimension(500,500));
                frame.setVisible(true);

                Thread.sleep(5000);
                frame.dispose();
            } catch (HeadlessException exception) {
                // The test is running on a machine without X11 display. Ignore.
                return;
            }
        }
    }
}
