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
 * RenderStyleTest.java
 *
 * Created on 27 May 2002, 15:40
 */
package org.geotools.styling;

import com.vividsolutions.jts.geom.*;
import junit.framework.*;
import org.geotools.data.*;
import org.geotools.feature.*;
import org.geotools.feature.Feature;
import org.geotools.map.*;
import org.geotools.map.BoundingBox;
import org.geotools.map.BoundingBoxImpl;
import org.geotools.renderer.Renderer;
import org.geotools.renderer.lite.LiteRenderer;

import org.geotools.styling.ExternalGraphic;
import org.geotools.styling.Fill;
import org.geotools.styling.Font;
import org.geotools.styling.Graphic;
import org.geotools.styling.Mark;
import org.geotools.styling.Stroke;
import org.geotools.styling.Style;
import org.geotools.styling.Symbolizer;
import java.awt.Color;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Panel;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.AffineTransform;
import java.awt.image.*;
import java.io.*;
import java.util.*;
import javax.imageio.*;
import javax.swing.*;


/**
 * Test for styled rendering
 *
 * @author jamesm,iant,aaime
 */
public class RenderStyleBuilderTest extends TestCase {
    /**
     * path for test data
     *
     * @param testName 
     */

    // private java.net.URL base = getClass().getResource("testData/");

    /**
     * Creates a new RenderStyleTest object.
     *
     * @param testName
     */
    public RenderStyleBuilderTest(java.lang.String testName) {
        super(testName);
    }

    /**
     * 
     *
     * @param args
     */
    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(suite());
    }

    /**
     * 
     *
     * @return
     */
    public static Test suite() {
        TestSuite suite = new TestSuite(RenderStyleTest.class);

        return suite;
    }

    /**
     * 
     *
     * @throws Exception
     */
    public void testSimpleRender() throws Exception {
        // build the features
        FeatureCollection fc = buildFeatures();

        // Build up style
        Style style = buildStyle();

        // test the context architecture
        contextArchitectureTest(fc, style, fc.getBounds());

        // test non interactive direct rendering
        directRenderingTest(fc, style, fc.getBounds(), false, 400, 400);
    }

    public void testDirectRendering() throws Exception {
        // build the features
        FeatureCollection fc = buildFeatures();

        // Build up style
        Style style = buildStyle();

        // test direct rendering with various frame sizes to check if the image
        // gets deformed accordingly (needed for wms)
        int[] widths = new int[] {200, 300, 400, 500, 300, 300, 300, 300};
        int[] heights = new int[] {300, 300, 300, 300, 200, 300, 400, 500};

        for (int i = 0; i < widths.length; i++) {
            directRenderingTest(fc, style, fc.getBounds(), true, widths[i], heights[i]);
        }
    }

    private Style buildStyle() {
        StyleBuilder sb = new StyleBuilder();
        Style style = sb.createStyle();
        style.setName("MyStyle");

        // create line style with two widths
        style.addFeatureTypeStyle( //
            sb.createFeatureTypeStyle("linefeature",
                new Symbolizer[] {
                    sb.createLineSymbolizer(Color.RED, 10), sb.createLineSymbolizer(Color.BLUE, 3)
                }));

        // create line style with graphic fill
        Mark triangle = sb.createMark(sb.MARK_TRIANGLE, Color.GREEN);
        Stroke gstroke = sb.createStroke(Color.RED, 10);
        gstroke.setGraphicFill(sb.createGraphic(null, triangle, null, 1, 10, 0));
        style.addFeatureTypeStyle(sb.createFeatureTypeStyle("linefeature2",
                new Symbolizer[] {sb.createLineSymbolizer(gstroke), sb.createLineSymbolizer(1)}));

        // create line style referring to external graphic
        ExternalGraphic ext = sb.createExternalGraphic("http://www.ccg.leeds.ac.uk/ian/geotools/icons/rail.gif",
                "image/gif");
        Mark arrow = sb.createMark(sb.MARK_ARROW, Color.BLUE);
        Stroke gstroke2 = sb.createStroke(Color.RED, 10);
        gstroke2.setGraphicStroke(sb.createGraphic(ext, arrow, null, 1, 10, 0));
        style.addFeatureTypeStyle(sb.createFeatureTypeStyle("linefeature3",
                new Symbolizer[] {
                    sb.createLineSymbolizer(gstroke2), sb.createLineSymbolizer(Color.RED, 1)
                }));

        // create complex fill style for polygons
        Mark circle = sb.createMark(sb.MARK_CIRCLE, Color.YELLOW, Color.CYAN, 1);
        Fill gfill = sb.createFill(Color.BLUE, 0.5);
        gfill.setGraphicFill(sb.createGraphic(null, circle, null, 1, 10, 0));

        Stroke dashed = sb.createStroke(Color.YELLOW, 3, new float[] {1f, 2f});
        style.addFeatureTypeStyle( //
            sb.createFeatureTypeStyle("polygon",
                new Symbolizer[] {
                    sb.createPolygonSymbolizer(Color.BLUE, 3),
                    sb.createPolygonSymbolizer(dashed, gfill)
                }));

        // create another one that refers to an external graphic
        Mark triangle2 = sb.createMark(sb.MARK_TRIANGLE, sb.createFill(Color.MAGENTA, 0.5), null);
        ExternalGraphic brick = sb.createExternalGraphic("http://www.ccg.leeds.ac.uk/ian/geotools/icons/brick1.gif",
                "image/gif");
        ExternalGraphic localBrick = sb.createExternalGraphic("http://www.ccg.leeds.ac.uk/ian/geotools/icons/brick1.gif",
                "image/gif");
        Fill gfill2 = sb.createFill(Color.MAGENTA, 0.5);
        gfill2.setGraphicFill(sb.createGraphic(brick, triangle2, null, 1, 20, 0));
        style.addFeatureTypeStyle(sb.createFeatureTypeStyle("polygontest2",
                sb.createPolygonSymbolizer(sb.createStroke(), gfill2)));

        // create one with graphic stroke
        Mark triangle3 = sb.createMark(sb.MARK_TRIANGLE, sb.createFill(Color.MAGENTA, 0.5), null);
        Fill gfill3 = sb.createFill(Color.MAGENTA, 0.5);
        gfill3.setGraphicFill(sb.createGraphic(null, triangle3, null, 1, 10, 0));

        Stroke gstroke3 = sb.createStroke();
        Mark arrow2 = sb.createMark(sb.MARK_ARROW, new Color(32, 32, 255));
        gstroke3.setGraphicStroke(sb.createGraphic(null, arrow2, null, 1, 8, 0));
        style.addFeatureTypeStyle(sb.createFeatureTypeStyle("polygontest3",
                sb.createPolygonSymbolizer(gstroke3, gfill3)));

        // and finally a point style using both graphics and text
        ExternalGraphic blob = sb.createExternalGraphic("http://www.ccg.leeds.ac.uk/ian/geotools/icons/blob.gif",
                "image/gif");
        Mark cross = sb.createMark(sb.MARK_CROSS, Color.MAGENTA, 0.5);
        Mark square = sb.createMark(sb.MARK_SQUARE, Color.GREEN, 0.5);
        Graphic gr = sb.createGraphic(new ExternalGraphic[] {blob}, new Mark[] {cross, square},
                null, 0.5, 10, 45);
        Font font = sb.createFont("Times", 10);
        style.addFeatureTypeStyle(sb.createFeatureTypeStyle("pointfeature",
                new Symbolizer[] {
                    sb.createPointSymbolizer(gr),
                    sb.createStaticTextSymbolizer(Color.GREEN, font, "Point Label")
                }));

        return style;
    }

    /**
     * 
     *
     * @return
     *
     * @throws SchemaException
     * @throws IllegalAttributeException
     */
    private FeatureCollection buildFeatures() throws SchemaException, IllegalAttributeException {
        AttributeType[] types = new AttributeType[1];

        GeometryFactory geomFac = new GeometryFactory();
        LineString line = makeSampleLineString(geomFac, 0, 0);
        types[0] = AttributeTypeFactory.newAttributeType("centerline", line.getClass());

        FeatureType lineType = FeatureTypeFactory.newFeatureType(types, "linefeature");
        Feature lineFeature = lineType.create(new Object[] {line});

        LineString line2 = makeSampleLineString(geomFac, 100, 0);
        lineType = FeatureTypeFactory.newFeatureType(types, "linefeature2");

        Feature lineFeature2 = lineType.create(new Object[] {line2});

        LineString line3 = makeSampleLineString(geomFac, 200, 0);
        lineType = FeatureTypeFactory.newFeatureType(types, "linefeature3");

        Feature lineFeature3 = lineType.create(new Object[] {line3});

        Polygon polygon = makeSamplePolygon(geomFac, 0, 0);

        types[0] = AttributeTypeFactory.newAttributeType("edge", polygon.getClass());

        FeatureType polygonType = FeatureTypeFactory.newFeatureType(types, "polygon");
        Feature polygonFeature = polygonType.create(new Object[] {polygon});

        Polygon polygon2 = makeSamplePolygon(geomFac, 0, 150);
        polygonType = FeatureTypeFactory.newFeatureType(types, "polygontest2");

        Feature polygonFeature2 = polygonType.create(new Object[] {polygon2});

        Polygon polygon3 = makeSamplePolygon(geomFac, 220, 100);
        polygonType = FeatureTypeFactory.newFeatureType(types, "polygontest3");

        Feature polygonFeature3 = polygonType.create(new Object[] {polygon3});

        Point point = makeSamplePoint(geomFac, 140.0, 140.0);
        types[0] = AttributeTypeFactory.newAttributeType("centre", point.getClass());

        FeatureType pointType = FeatureTypeFactory.newFeatureType(types, "pointfeature");

        Feature pointFeature = pointType.create(new Object[] {point});

        FeatureCollection fc = FeatureCollections.newCollection();
        fc.add(lineFeature);
        fc.add(lineFeature2);
        fc.add(lineFeature3);
        fc.add(polygonFeature);
        fc.add(polygonFeature2);
        fc.add(polygonFeature3);
        fc.add(pointFeature);

        return fc;
    }

    /**
     * 
     *
     * @param ft
     * @param style
     * @param ex
     * @param interactive 
     * @param width 
     * @param height 
     *
     * @throws Exception
     */
    private void directRenderingTest(FeatureCollection ft, Style style, Envelope ex,
        boolean interactive, int width, int height) throws Exception {
        LiteRenderer renderer = new LiteRenderer();
        Frame frame = new Frame("rendering test");
        frame.addWindowListener(new WindowAdapter() {
                public void windowClosing(WindowEvent e) {
                    e.getWindow().dispose();
                }
            });

        Panel p = new Panel();

        frame.add(p);

        frame.setSize(width, height);
        frame.setVisible(true);
        renderer.setOutput(p.getGraphics(), p.getBounds());
        renderer.setInteractive(interactive);

        Date start = new Date();
        Feature[] features = new Feature[ft.size()];
        features = (Feature[]) ft.toArray(features);
        renderer.render(features, ex, style);

        Date end = new Date();
        System.out.println("Time to render to screen: " + (end.getTime() - start.getTime()));

        // Thread.sleep(2000);
        frame.dispose();
    }

    /**
     * 
     *
     * @param fc
     * @param style
     * @param ex
     *
     * @throws Exception
     */
    private void contextArchitectureTest(FeatureCollection fc, Style style, Envelope ex)
        throws Exception {
        ContextFactory cfac = ContextFactory.createFactory();
        Context ctx = cfac.createContext();
        ctx.getLayerList().addLayer(cfac.createLayer(fc, style));

        LiteRenderer renderer = new LiteRenderer(ctx);
        renderer.setConcatTransforms(true);

        Frame frame = new Frame("rendering test");
        frame.addWindowListener(new WindowAdapter() {
                public void windowClosing(WindowEvent e) {
                    e.getWindow().dispose();
                }
            });

        Panel p = new Panel();

        frame.add(p);

        frame.setSize(400, 400);
        p.setSize(400, 400); // make the panel square ?
        frame.setLocation(400, 0);
        frame.setVisible(true);

        Date start = new Date();
        AffineTransform at = new AffineTransform(1, 0.0d, 0.0d, -1, 0, 400);
        renderer.paint((Graphics2D) p.getGraphics(), p.getBounds(), at);

        Date end = new Date();
        System.out.println("Time to render to screen using context: "
            + (end.getTime() - start.getTime()));

        frame.dispose();
    }

    /**
     * 
     *
     * @param geomFac
     * @param xoff
     * @param yoff
     *
     * @return
     */
    private LineString makeSampleLineString(final GeometryFactory geomFac, double xoff, double yoff) {
        Coordinate[] linestringCoordinates = new Coordinate[8];
        linestringCoordinates[0] = new Coordinate(50.0d + xoff, 50.0d + yoff);
        linestringCoordinates[1] = new Coordinate(60.0d + xoff, 50.0d + yoff);
        linestringCoordinates[2] = new Coordinate(60.0d + xoff, 60.0d + yoff);
        linestringCoordinates[3] = new Coordinate(70.0d + xoff, 60.0d + yoff);
        linestringCoordinates[4] = new Coordinate(70.0d + xoff, 70.0d + yoff);
        linestringCoordinates[5] = new Coordinate(80.0d + xoff, 70.0d + yoff);
        linestringCoordinates[6] = new Coordinate(80.0d + xoff, 80.0d + yoff);
        linestringCoordinates[7] = new Coordinate(130.0d + xoff, 300.0d + yoff);

        LineString line = geomFac.createLineString(linestringCoordinates);

        return line;
    }

    /**
     * 
     *
     * @param geomFac
     * @param xoff
     * @param yoff
     *
     * @return
     */
    private com.vividsolutions.jts.geom.Polygon makeSamplePolygon(final GeometryFactory geomFac,
        double xoff, double yoff) {
        Coordinate[] polygonCoordinates = new Coordinate[10];
        polygonCoordinates[0] = new Coordinate(70 + xoff, 70 + yoff);
        polygonCoordinates[1] = new Coordinate(60 + xoff, 90 + yoff);
        polygonCoordinates[2] = new Coordinate(60 + xoff, 110 + yoff);
        polygonCoordinates[3] = new Coordinate(70 + xoff, 120 + yoff);
        polygonCoordinates[4] = new Coordinate(90 + xoff, 110 + yoff);
        polygonCoordinates[5] = new Coordinate(110 + xoff, 120 + yoff);
        polygonCoordinates[6] = new Coordinate(130 + xoff, 110 + yoff);
        polygonCoordinates[7] = new Coordinate(130 + xoff, 90 + yoff);
        polygonCoordinates[8] = new Coordinate(110 + xoff, 70 + yoff);
        polygonCoordinates[9] = new Coordinate(70 + xoff, 70 + yoff);

        try {
            LinearRing ring = geomFac.createLinearRing(polygonCoordinates);
            com.vividsolutions.jts.geom.Polygon polyg = geomFac.createPolygon(ring, null);

            return polyg;
        } catch (TopologyException te) {
            fail("Error creating sample polygon for testing " + te);
        }

        return null;
    }

    /**
     * 
     *
     * @param geomFac
     * @param x
     * @param y
     *
     * @return
     */
    private Point makeSamplePoint(final GeometryFactory geomFac, double x, double y) {
        Coordinate c = new Coordinate(x, y);
        Point point = geomFac.createPoint(c);

        return point;
    }
}
