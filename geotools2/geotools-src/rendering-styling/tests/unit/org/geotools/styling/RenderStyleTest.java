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

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Polygon;
import org.geotools.feature.Feature;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureCollections;
import org.geotools.feature.FeatureTypeFactory;
import org.geotools.map.DefaultMapContext;
import org.geotools.map.MapContext;
import org.geotools.renderer.Renderer2D;
import org.geotools.renderer.j2d.StyledMapRenderer;
import org.geotools.renderer.lite.LiteRenderer;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.Raster;
import java.awt.image.RenderedImage;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.logging.Logger;

import javax.media.jai.JAI;


/**
 * Test class for marks and text
 *
 * @author jamesm
 * @author Andrea Aime
 */
public class RenderStyleTest extends junit.framework.TestCase {
    /** The logger for the rendering module. */
    private static final Logger LOGGER = Logger.getLogger("org.geotools.rendering");
    
    private static boolean INTERACTIVE = false;
    private static int MAX_PIXEL_ERRORS = 18; //that is, 6 pixels since every band is counted

    /**
     * Creates a new DefaultMarkTest object.
     *
     * @param testName
     */
    public RenderStyleTest(java.lang.String testName) {
        super(testName);
    }

    /**
     * Main
     *
     * @param args
     */
    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(suite());
    }

    /**
     * Suite
     *
     * @return
     */
    public static junit.framework.Test suite() {
        junit.framework.TestSuite suite = new junit.framework.TestSuite(RenderStyleTest.class);

        return suite;
    }

    /**
     * Builds and returns the features used to perform this test. Line, polygon and point feature
     * collections, in this order
     *
     * @return
     *
     * @throws Exception
     */
    private FeatureCollection[] buildFeatureCollections()
        throws Exception {
        // Request extent
        com.vividsolutions.jts.geom.Envelope ex = new com.vividsolutions.jts.geom.Envelope(30, 350,
                30, 350);

        org.geotools.feature.AttributeType[] types = new org.geotools.feature.AttributeType[1];

        GeometryFactory geomFac = new GeometryFactory();
        LineString line = makeSampleLineString(geomFac, 0, 0);
        types[0] = org.geotools.feature.AttributeTypeFactory.newAttributeType("centerline",
                line.getClass());

        org.geotools.feature.FeatureType lineType = FeatureTypeFactory.newFeatureType(types,
                "linefeature");
        Feature lineFeature = lineType.create(new Object[] { line });

        LineString line2 = makeSampleLineString(geomFac, 100, 0);
        lineType = FeatureTypeFactory.newFeatureType(types, "linefeature2");

        Feature lineFeature2 = lineType.create(new Object[] { line2 });

        LineString line3 = makeSampleLineString(geomFac, 150, 0);
        lineType = FeatureTypeFactory.newFeatureType(types, "linefeature3");

        Feature lineFeature3 = lineType.create(new Object[] { line3 });

        Polygon polygon = makeSamplePolygon(geomFac, 0, 0);

        types[0] = org.geotools.feature.AttributeTypeFactory.newAttributeType("edge",
                polygon.getClass());

        org.geotools.feature.FeatureType polygonType = FeatureTypeFactory.newFeatureType(types,
                "polygon");
        Feature polygonFeature = polygonType.create(new Object[] { polygon });

        Polygon polygon2 = makeSamplePolygon(geomFac, 0, 150);
        polygonType = FeatureTypeFactory.newFeatureType(types, "polygontest2");

        Feature polygonFeature2 = polygonType.create(new Object[] { polygon2 });

        Polygon polygon3 = makeSamplePolygon(geomFac, 220, 100);
        polygonType = FeatureTypeFactory.newFeatureType(types, "polygontest3");

        Feature polygonFeature3 = polygonType.create(new Object[] { polygon3 });

        com.vividsolutions.jts.geom.Point point = makeSamplePoint(geomFac, 140.0, 140.0);
        types[0] = org.geotools.feature.AttributeTypeFactory.newAttributeType("centre",
                point.getClass());

        org.geotools.feature.FeatureType pointType = FeatureTypeFactory.newFeatureType(types,
                "pointfeature");

        Feature pointFeature = pointType.create(new Object[] { point });

        FeatureCollection fcLine = FeatureCollections.newCollection();
        FeatureCollection fcPolygon = FeatureCollections.newCollection();
        FeatureCollection fcPoint = FeatureCollections.newCollection();

        fcLine.add(lineFeature);
        fcLine.add(lineFeature2);
        fcLine.add(lineFeature3);
        fcPolygon.add(polygonFeature);
        fcPolygon.add(polygonFeature2);
        fcPolygon.add(polygonFeature3);
        fcPoint.add(pointFeature);

        return new FeatureCollection[] { fcLine, fcPolygon, fcPoint };
    }

    private Style loadStyleFromXml() throws Exception {
        java.net.URL base = getClass().getResource("rs-testData/");

        StyleFactory factory = StyleFactory.createStyleFactory();
        java.net.URL surl = new java.net.URL(base + "/sample.sld");
        SLDParser stylereader = new SLDParser(factory, surl);
        Style[] style = stylereader.readXML();

        return style[0];
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
        Mark triangle = sb.createMark(StyleBuilder.MARK_TRIANGLE, Color.GREEN);
        Stroke gstroke = sb.createStroke(Color.RED, 10);
        gstroke.setGraphicFill(sb.createGraphic(null, triangle, null, 1, 10, 0));
        style.addFeatureTypeStyle(sb.createFeatureTypeStyle("linefeature2",
                new Symbolizer[] { sb.createLineSymbolizer(gstroke), sb.createLineSymbolizer(1) }));

        // create line style referring to external graphic
        ExternalGraphic ext = sb.createExternalGraphic("http://www.ccg.leeds.ac.uk/ian/geotools/icons/rail.gif",
                "image/gif");
        Mark arrow = sb.createMark(StyleBuilder.MARK_ARROW, Color.BLUE);
        Stroke gstroke2 = sb.createStroke(Color.RED, 10);
        gstroke2.setGraphicStroke(sb.createGraphic(ext, arrow, null, 1, 10, 0));
        style.addFeatureTypeStyle(sb.createFeatureTypeStyle("linefeature3",
                new Symbolizer[] {
                    sb.createLineSymbolizer(gstroke2), sb.createLineSymbolizer(Color.RED, 1)
                }));

        // create complex fill style for polygons
        Mark circle = sb.createMark(StyleBuilder.MARK_CIRCLE, Color.YELLOW);
        Fill gfill = sb.createFill(Color.YELLOW, 0.5);
        gfill.setGraphicFill(sb.createGraphic(null, circle, null, 1, 10, 0));

        Stroke dashed = sb.createStroke(Color.YELLOW, 3, new float[] { 1f, 2f });
        style.addFeatureTypeStyle( //
            sb.createFeatureTypeStyle("polygon",
                new Symbolizer[] {
                    sb.createPolygonSymbolizer(Color.BLACK, 3),
                    sb.createPolygonSymbolizer(dashed, gfill)
                }));

        // create another one that refers to an external graphic
        Mark triangle2 = sb.createMark(StyleBuilder.MARK_TRIANGLE,
                sb.createFill(Color.MAGENTA, 0.5), null);
        ExternalGraphic brick = sb.createExternalGraphic("http://www.ccg.leeds.ac.uk/ian/geotools/icons/brick1.gif",
                "image/gif");
        Fill gfill2 = sb.createFill(Color.MAGENTA, 0.5);
        gfill2.setGraphicFill(sb.createGraphic(brick, triangle2, null, 1, 20, 0));
        style.addFeatureTypeStyle(sb.createFeatureTypeStyle("polygontest2",
                sb.createPolygonSymbolizer(sb.createStroke(), gfill2)));

        // create one with graphic stroke
        Mark triangle3 = sb.createMark(StyleBuilder.MARK_TRIANGLE,
                sb.createFill(Color.MAGENTA, 0.5), null);
        Fill gfill3 = sb.createFill(Color.MAGENTA, 0.5);
        gfill3.setGraphicFill(sb.createGraphic(null, triangle3, null, 1, 10, 0));

        Stroke gstroke3 = sb.createStroke();
        Mark arrow2 = sb.createMark(StyleBuilder.MARK_ARROW, new Color(32, 32, 255));
        gstroke3.setGraphicStroke(sb.createGraphic(null, arrow2, null, 1, 8, 0));
        style.addFeatureTypeStyle(sb.createFeatureTypeStyle("polygontest3",
                sb.createPolygonSymbolizer(gstroke3, gfill3)));

        // and finally a point style using both graphics and text
        ExternalGraphic blob = sb.createExternalGraphic("http://www.ccg.leeds.ac.uk/ian/geotools/icons/blob.gif",
                "image/gif");
        Mark cross = sb.createMark(StyleBuilder.MARK_TRIANGLE, new Color(255, 0, 255), 0.5);
        Mark square = sb.createMark(StyleBuilder.MARK_SQUARE, Color.GREEN, 0.5);
        Graphic gr = sb.createGraphic(new ExternalGraphic[] { blob }, new Mark[] { cross, square },
                null, 1.0, 10, 45);
        Font font = sb.createFont("Lucida Sans", 10);
        TextSymbolizer ts = sb.createStaticTextSymbolizer(Color.GREEN, font, "Point Label");
        ts.setHalo(sb.createHalo());
        style.addFeatureTypeStyle(sb.createFeatureTypeStyle("pointfeature",
                new Symbolizer[] { sb.createPointSymbolizer(gr), ts }));

        return style;
    }

    private LiteRenderer createLiteRenderedXmlStyle() throws Exception {
        MapContext ctx = new DefaultMapContext();
        FeatureCollection[] collections = buildFeatureCollections();
        ctx.addLayer(collections[0], loadStyleFromXml());
        ctx.addLayer(collections[1], loadStyleFromXml());
        ctx.addLayer(collections[2], loadStyleFromXml());

        LiteRenderer renderer = new LiteRenderer(ctx);
        renderer.setInteractive(false);
        renderer.setOptimizedDataLoadingEnabled(true);

        return renderer;
    }

    /**
     * Test lite renderer and style loaded from xml file
     *
     * @throws Exception
     */
    public void testLiteRendererXmlh250() throws Exception {
        performTestOnRenderer(createLiteRenderedXmlStyle(), "xml", 400, 250, 400, 350);
    }

    /**
     * Test lite renderer and style loaded from xml file
     *
     * @throws Exception
     */
    public void testLiteRendererXmlh450() throws Exception {
        performTestOnRenderer(createLiteRenderedXmlStyle(), "xml", 400, 450, 400, 350);
    }

    /**
     * Test lite renderer and style loaded from xml file
     *
     * @throws Exception
     */
    public void testLiteRendererXmlw200() throws Exception {
        performTestOnRenderer(createLiteRenderedXmlStyle(), "xml", 200, 350, 400, 350);
    }

    /**
     * Test lite renderer and style loaded from xml file
     *
     * @throws Exception
     */
    public void testLiteRendererXmlw400() throws Exception {
        performTestOnRenderer(createLiteRenderedXmlStyle(), "xml", 400, 350, 400, 350);
    }

    /**
     * Test lite renderer and style loaded from xml file
     *
     * @throws Exception
     */
    public void testLiteRendererXmlw600() throws Exception {
        performTestOnRenderer(createLiteRenderedXmlStyle(), "xml", 600, 350, 400, 350);
    }

    private StyledMapRenderer createStyledRendererXmlStyle()
        throws Exception {
        MapContext map = new DefaultMapContext();
        FeatureCollection[] collections = buildFeatureCollections();
        map.addLayer(collections[0], loadStyleFromXml());
        map.addLayer(collections[1], loadStyleFromXml());
        map.addLayer(collections[2], loadStyleFromXml());

        StyledMapRenderer sr = new StyledMapRenderer(null);
        sr.setMapContext(map);

        return sr;
    }

    /**
     * Test lite renderer and style loaded from xml file
     *
     * @throws Exception
     */
    public void testStyledRendererXmlh250() throws Exception {
        performTestOnRenderer(createStyledRendererXmlStyle(), "xml", 400, 250, 400, 350);
    }

    /**
     * Test lite renderer and style loaded from xml file
     *
     * @throws Exception
     */
    public void testStyledRendererXmlh350() throws Exception {
        performTestOnRenderer(createStyledRendererXmlStyle(), "xml", 400, 350, 400, 350);
    }

    /**
     * Test lite renderer and style loaded from xml file
     *
     * @throws Exception
     */
    public void testStyledRendererXmlw200() throws Exception {
        performTestOnRenderer(createStyledRendererXmlStyle(), "xml", 200, 350, 400, 350);
    }

    /**
     * Test lite renderer and style loaded from xml file
     *
     * @throws Exception
     */
    public void testStyledRendererXmlw400() throws Exception {
        performTestOnRenderer(createStyledRendererXmlStyle(), "xml", 400, 350, 400, 350);
    }

    /**
     * Test lite renderer and style loaded from xml file
     *
     * @throws Exception
     */
    public void testStyledRendererXmlw600() throws Exception {
        performTestOnRenderer(createStyledRendererXmlStyle(), "xml", 600, 350, 400, 350);
    }

    /**
     * Test j2d renderer
     *
     * @throws Exception
     */
    public void testJ2DRendererBuilder() throws Exception {
        MapContext map = new DefaultMapContext();
        FeatureCollection[] collections = buildFeatureCollections();
        map.addLayer(collections[0], loadStyleFromXml());
        map.addLayer(collections[1], loadStyleFromXml());
        map.addLayer(collections[2], loadStyleFromXml());

        StyledMapRenderer sr = new StyledMapRenderer(null);
        sr.setMapContext(map);
        performTestOnRenderer(sr, "builder", 400, 350, 400, 350);
    }

    /**
     * Test lite renderer and style created with the style builder
     *
     * @throws Exception
     */
    public void testLiteRendererBuilder() throws Exception {
        MapContext map = new DefaultMapContext();
        FeatureCollection[] collections = buildFeatureCollections();
        map.addLayer(collections[0], buildStyle());
        map.addLayer(collections[1], buildStyle());
        map.addLayer(collections[2], buildStyle());

        LiteRenderer renderer = new LiteRenderer(map);
        renderer.setInteractive(false);
        performTestOnRenderer(renderer, "builder", 400, 350, 400, 350);
    }

    /**
     * Perform test on the passed renderer, which must be already configured with its context
     *
     * @param renderer
     * @param fileSuffix
     * @param width
     * @param height
     * @param dataWidth
     * @param dataHeigth
     *
     * @throws Exception
     */
    private void performTestOnRenderer(Renderer2D renderer, String fileSuffix, int width,
        int height, double dataWidth, double dataHeigth)
        throws Exception {
        if (!online()) {
            System.out.println("WARNING: CANNOT REACH LEEDS HOST, TEST WON'T BE PERFORMED");

            return;
        }

        final double scalex = width / dataWidth;
        final double scaley = height / dataHeigth;

        java.net.URL base = getClass().getResource("rs-testData/");

        AffineTransform at = new AffineTransform();
        at.translate(0, height);
        at.scale(scalex, -scaley);

        if (INTERACTIVE) {
            java.awt.Frame frame = new java.awt.Frame("Mark test (" + renderer.getClass().getName());
            frame.addWindowListener(new java.awt.event.WindowAdapter() {
                    public void windowClosing(java.awt.event.WindowEvent e) {
                        e.getWindow().dispose();
                    }
                });

            java.awt.Panel p = new java.awt.Panel();
            frame.add(p);
            frame.setSize(width, height);
            frame.setLocation(0, 0);
            frame.setVisible(true);
            renderer.paint((Graphics2D) p.getGraphics(), p.getBounds(), at);

            Thread.sleep(5000);
            frame.dispose();
        }

        java.awt.image.BufferedImage image = new java.awt.image.BufferedImage(width, height,
                java.awt.image.BufferedImage.TYPE_INT_RGB);
        java.awt.Graphics2D g = (Graphics2D) image.getGraphics();
        g.setColor(java.awt.Color.white);
        g.fillRect(0, 0, width, height);
        renderer.paint(g, new java.awt.Rectangle(0, 0, width, height), at);

        java.io.File file = new java.io.File(base.getPath(),
                "RenderStyleTest_" + renderer.getClass().getName().replace('.', '_') + "_"
                + fileSuffix + width + "x" + height + ".png");
        java.io.FileOutputStream out = new java.io.FileOutputStream(file);
        boolean fred = javax.imageio.ImageIO.write(image, "PNG", out);

        if (!fred) {
            System.out.println("Failed to write image to " + file.toString());
        }

        java.io.File file2 = new java.io.File(base.getPath() + "/exemplars/",
                "RenderStyleTest_" + renderer.getClass().getName().replace('.', '_') + "_" + width
                + "x" + height + ".png");

        RenderedImage image2 = (RenderedImage) JAI.create("fileload", file2.toString());

        assertNotNull("Failed to load exemplar image", image2);

        Raster data = image.getData();
        Raster data2 = image2.getData();
        int[] pixel1 = null;
        int[] pixel2 = null;
        boolean isBlack = false;

        int pixelErrors = 0;
        LOGGER.info("Comparing pixels between:");
        LOGGER.info(file.getName());
        LOGGER.info(file2.getName());
        LOGGER.info("Comparing pixels between:");
        for (int x = 0; x < data.getWidth(); x++) {
            for (int y = 0; y < data.getHeight(); y++) {
                pixel1 = data.getPixel(x, y, pixel1);
                pixel2 = data2.getPixel(x, y, pixel2);

                if ((notBlack(pixel1)) && (notBlack(pixel2))) {
                    //Since text is black and fonts are not stable across platforms, ignore pixels where at least one is black.
                    for (int band = 0; band < data2.getNumBands(); band++) {
                        if(pixel1[band] != pixel2[band]) {
                            pixelErrors++;
                            LOGGER.info("mismatch in image comparison at (x: " + x + " y: " + y
                                                        + " band: " + band + ") , expected " + pixel1[band] + " but was " + pixel2[band]);
                                                        
                            if(pixelErrors > MAX_PIXEL_ERRORS)
                                fail("Too many pixel differences between examplar and generated image");
                        }
                        
                    }
                }
            }
        }
    }

    private boolean notBlack(int[] pixel) {
        boolean isBlack = true;
        int x = 0;

        while (isBlack && (x < pixel.length)) {
            isBlack = (pixel[x] == 0);
            x++;
        }

        return !(isBlack);
    }

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

    private Polygon makeSamplePolygon(final GeometryFactory geomFac, double xoff, double yoff) {
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
            com.vividsolutions.jts.geom.LinearRing ring = geomFac.createLinearRing(polygonCoordinates);
            Polygon polyg = geomFac.createPolygon(ring, null);

            return polyg;
        } catch (com.vividsolutions.jts.geom.TopologyException te) {
            fail("Error creating sample polygon for testing " + te);
        }

        return null;
    }

    private com.vividsolutions.jts.geom.Point makeSamplePoint(final GeometryFactory geomFac,
        double x, double y) {
        Coordinate c = new Coordinate(x, y);
        com.vividsolutions.jts.geom.Point point = geomFac.createPoint(c);

        return point;
    }

    // returns true if can reach leeds
    public boolean online() {
        boolean online = true;

        try {
            InetAddress address = InetAddress.getByName("www.ccg.leeds.ac.uk");
        } catch (UnknownHostException e) {
            online = false;
        }

        return online;
    }
}
