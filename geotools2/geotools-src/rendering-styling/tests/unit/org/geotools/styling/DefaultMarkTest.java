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

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.Raster;
import java.awt.image.RenderedImage;

import javax.media.jai.JAI;

import org.geotools.feature.AttributeTypeFactory;
import org.geotools.feature.FeatureCollection;
import org.geotools.filter.FilterFactory;
import org.geotools.map.DefaultMapContext;
import org.geotools.map.MapContext;
import org.geotools.renderer.Renderer2D;
import org.geotools.renderer.j2d.StyledMapRenderer;
import org.geotools.renderer.lite.LiteRenderer;

import com.vividsolutions.jts.geom.Point;


/**
 * Test class for marks and text
 *
 * @author jamesm
 * @author Andrea Aime
 */
public class DefaultMarkTest extends junit.framework.TestCase {
    private static boolean INTERACTIVE = false;
    
    /**
     * Creates a new DefaultMarkTest object.
     */
    public DefaultMarkTest(java.lang.String testName) {
        super(testName);
    }

    /**
     * Main
     */
    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(suite());
    }

    /**
     * Suite
     */
    public static junit.framework.Test suite() {
        junit.framework.TestSuite suite = new junit.framework.TestSuite(DefaultMarkTest.class);

        return suite;
    }

    /**
     * Builds and returns the features used to perform this test
     */
    private FeatureCollection buildFeatureCollection()
        throws Exception {
        // Request extent
        com.vividsolutions.jts.geom.Envelope ex = new com.vividsolutions.jts.geom.Envelope(0, 45,
                0, 45);

        com.vividsolutions.jts.geom.GeometryFactory geomFac = new com.vividsolutions.jts.geom.GeometryFactory();
        java.util.ArrayList features = new java.util.ArrayList();

        org.geotools.feature.AttributeType[] pointAttribute = new org.geotools.feature.AttributeType[4];
        pointAttribute[0] = AttributeTypeFactory.newAttributeType("centre", Point.class);
        pointAttribute[1] = AttributeTypeFactory.newAttributeType("size", Double.class);
        pointAttribute[2] = AttributeTypeFactory.newAttributeType("rotation", Double.class);
        pointAttribute[3] = AttributeTypeFactory.newAttributeType("name", String.class);

        org.geotools.feature.FeatureType pointType = org.geotools.feature.FeatureTypeFactory
            .newFeatureType(pointAttribute, "testPoint");

        org.geotools.feature.AttributeType[] labelAttribute = new org.geotools.feature.AttributeType[4];
        labelAttribute[0] = AttributeTypeFactory.newAttributeType("centre", Point.class);
        labelAttribute[1] = AttributeTypeFactory.newAttributeType("name", String.class);
        labelAttribute[2] = AttributeTypeFactory.newAttributeType("X", Double.class);
        labelAttribute[3] = AttributeTypeFactory.newAttributeType("Y", Double.class);

        org.geotools.feature.FeatureType labelType = org.geotools.feature.FeatureTypeFactory
            .newFeatureType(labelAttribute, "labelPoint");
        String[] marks = { "Circle", "Triangle", "Cross", "Star", "X", "Square", "Arrow" };
        double size = 6;
        double rotation = 0;
        int rows = 7;

        for (int j = 0; j < rows; j++) {
            Point point = makeSamplePoint(geomFac, 2, 5.0 + (j * 5));
            org.geotools.feature.Feature pointFeature = labelType.create(new Object[] {
                        point, "" + size + "/" + rotation, new Double(0.3), new Double(.5)
                    });
            features.add(pointFeature);

            for (int i = 0; i < marks.length; i++) {
                point = makeSamplePoint(geomFac, ((double) i * 5.0) + 10.0, 5.0 + (j * 5));
                pointFeature = pointType.create(new Object[] {
                            point, new Double(size), new Double(rotation), marks[i]
                        });
                features.add(pointFeature);
            }

            size += 2;
            rotation += 45;
        }

        for (int i = 0; i < marks.length; i++) {
            Point point = makeSamplePoint(geomFac, ((double) i * 5.0) + 10.0, 5.0 + (rows * 5));
            org.geotools.feature.Feature pointFeature = labelType.create(new Object[] {
                        point, marks[i], new Double(.5), new Double(0)
                    });
            features.add(pointFeature);
        }

        org.geotools.feature.FeatureCollection ft = org.geotools.feature.FeatureCollections
            .newCollection();
        ft.addAll(features);

        return ft;
    }

    private Style loadStyleFromXml() throws Exception {
        java.net.URL base = getClass().getResource("rs-testData");

        StyleFactory factory = StyleFactory.createStyleFactory();
        java.net.URL surl = new java.net.URL(base + "/markTest.sld");
        SLDStyle stylereader = new SLDStyle(factory, surl);
        Style[] style = stylereader.readXML();

        return style[0];
    }
    
    private Style buildStyle() throws Exception {
        StyleBuilder sb = new StyleBuilder();
        FilterFactory ff = sb.getFilterFactory();
        Style style = sb.createStyle();
        style.setName("MyStyle");

        // "testPoint" feature type style
        Mark testMark = sb.createMark(sb.attributeExpression("name"),
                sb.createFill(Color.RED, 0.5), null);
        Graphic graph = sb.createGraphic(null, new Mark[] { testMark }, null,
                sb.literalExpression(1), sb.attributeExpression("size"),
                sb.attributeExpression("rotation"));
        style.addFeatureTypeStyle(sb.createFeatureTypeStyle("testPoint",
                new Symbolizer[] { sb.createPointSymbolizer(graph) }));

        // "labelPoint" feature type style
        AnchorPoint anchorPoint = sb.createAnchorPoint(sb.attributeExpression("X"),
                sb.attributeExpression("Y"));
        PointPlacement pointPlacement = sb.createPointPlacement(anchorPoint, null,
                sb.literalExpression(0));
        TextSymbolizer textSymbolizer = sb.createTextSymbolizer(sb.createFill(Color.BLACK),
                new Font[] { sb.createFont("Lucida Sans", 10), sb.createFont("Arial", 10) },
                sb.createHalo(), sb.attributeExpression("name"), pointPlacement, null);
        Mark circle = sb.createMark(StyleBuilder.MARK_CIRCLE, Color.RED);
        Graphic graph2 = sb.createGraphic(null, circle, null, 1, 4, 0);
        PointSymbolizer pointSymbolizer = sb.createPointSymbolizer(graph2);
        style.addFeatureTypeStyle(sb.createFeatureTypeStyle("labelPoint",
                new Symbolizer[] { textSymbolizer, pointSymbolizer }));

        return style;
    }

    /**
     * Test lite renderer and style loaded from xml file
     */
    public void testLiteRendererXml() throws Exception {
        MapContext context = new DefaultMapContext();
        context.addLayer(buildFeatureCollection(), loadStyleFromXml());
        
        performTestOnRenderer(new LiteRenderer(context), "xml");
    }
    
    /**
     * Test lite renderer and style created with the style builder
     */
    public void testLiteRendererBuilder() throws Exception {
        MapContext context = new DefaultMapContext();
        context.addLayer(buildFeatureCollection(), buildStyle());
        
        performTestOnRenderer(new LiteRenderer(context), "builder");
    }

    /**
     * Test j2d renderer
     */
    public void testJ2DRendererXml() throws Exception {
        MapContext map = new DefaultMapContext();
        map.addLayer(buildFeatureCollection(), loadStyleFromXml());
        
        StyledMapRenderer sr = new StyledMapRenderer(null);
        sr.setMapContext(map);
        performTestOnRenderer(sr, "xml");
    }
    
     /**
     * Test j2d renderer
     */
    public void testJ2DRendererBuilder() throws Exception {
		MapContext map = new DefaultMapContext();
	   map.addLayer(buildFeatureCollection(), loadStyleFromXml());
        
	   StyledMapRenderer sr = new StyledMapRenderer(null);
	   sr.setMapContext(map);
        performTestOnRenderer(sr, "builder");
    }


    /**
     * Perform test on the passed renderer, which must be already configured with its context
     */
    private void performTestOnRenderer(Renderer2D renderer, String fileSuffix)
        throws Exception {
        java.net.URL base = getClass().getResource("rs-testData");
        
        AffineTransform at = new AffineTransform();
        at.translate(0, 400);
        at.scale(9, -9);

        if(INTERACTIVE) {
            java.awt.Frame frame = new java.awt.Frame("Mark test (" + renderer.getClass().getName());
            frame.addWindowListener(new java.awt.event.WindowAdapter() {
                    public void windowClosing(java.awt.event.WindowEvent e) {
                        e.getWindow().dispose();
                    }
                });

            java.awt.Panel p = new java.awt.Panel();
            frame.add(p);
            frame.setSize(400, 400);
            frame.setLocation(0, 0);
            frame.setVisible(true);
            
            renderer.paint((Graphics2D) p.getGraphics(), p.getBounds(), at);
            
            // Thread.sleep(5000);
            frame.dispose();
        }

        int w = 400;
        int h = 400;
        java.awt.image.BufferedImage image = new java.awt.image.BufferedImage(w, h,
                java.awt.image.BufferedImage.TYPE_INT_RGB);
        java.awt.Graphics2D g = (Graphics2D) image.getGraphics();
        g.setColor(java.awt.Color.white);
        g.fillRect(0, 0, w, h);
        renderer.paint(g, new java.awt.Rectangle(0, 0, w, h), at);

            
        

        java.io.File file = new java.io.File(base.getPath(),
                "DefaultMarkTest_" + renderer.getClass().getName().replace('.', '_') + "_" + fileSuffix + ".png");
        java.io.FileOutputStream out = new java.io.FileOutputStream(file);
        boolean fred = javax.imageio.ImageIO.write(image, "png", out);

        if (!fred) {
            System.out.println("Failed to write image to " + file.toString());
        }

        java.io.File file2 = new java.io.File(base.getPath() + "/exemplars/",
                "DefaultMarkTest_" + renderer.getClass().getName().replace('.', '_') + ".png");

        RenderedImage image2 = (RenderedImage) JAI.create("fileload", file2.toString());

        assertNotNull("Failed to load exemplar image", image2);

        Raster data = image.getData();
        Raster data2 = image2.getData();
        int[] pixel1 = null;
        int[] pixel2 = null;
        boolean isBlack = false;

        for (int x = 0; x < data.getWidth(); x++) {
            for (int y = 0; y < data.getHeight(); y++) {
                pixel1 = data.getPixel(x, y, pixel1);
                pixel2 = data2.getPixel(x, y, pixel2);

                if ((notBlack(pixel1)) && (notBlack(pixel2))) { //Since text is black and fonts are not stable across platforms, ignore pixels where at least one is black.

                    for (int band = 0; band < data2.getNumBands(); band++) {
                        assertEquals("mismatch in image comparison at (x: " + x + " y: " + y
                            + " band: " + band + ")", pixel1[band], pixel2[band]);
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

    private Point makeSamplePoint(final com.vividsolutions.jts.geom.GeometryFactory geomFac,
        double x, double y) {
        com.vividsolutions.jts.geom.Coordinate c = new com.vividsolutions.jts.geom.Coordinate(x, y);
        Point point = geomFac.createPoint(c);

        return point;
    }
}
