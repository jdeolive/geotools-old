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
import org.geotools.filter.IllegalFilterException;
import org.geotools.map.Context;
import org.geotools.map.ContextFactory;
import org.geotools.map.DefaultMapContext;
import org.geotools.map.Layer;
import org.geotools.map.MapContext;
import org.geotools.renderer.Renderer2D;
import org.geotools.renderer.j2d.StyledRenderer;
import org.geotools.renderer.lite.LiteRenderer;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;


/**
 * Test class for marks and text
 *
 * @author jamesm
 * @author Andrea Aime
 */
public class TextStyleTest extends junit.framework.TestCase {
    private static boolean INTERACTIVE = false;
    
    /**
     * Creates a new DefaultMarkTest object.
     */
    public TextStyleTest(java.lang.String testName) {
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
        junit.framework.TestSuite suite = new junit.framework.TestSuite(TextStyleTest.class);

        return suite;
    }

    /**
     * Builds and returns the features used to perform this test
     */
    private FeatureCollection buildFeatureCollection()
        throws Exception {
            com.vividsolutions.jts.geom.Envelope ex = new com.vividsolutions.jts.geom.Envelope(0, 50, 0, 100);
        GeometryFactory geomFac = new GeometryFactory();
        java.util.ArrayList features = new java.util.ArrayList();
        int points = 4;
        int rows = 3;
        org.geotools.feature.AttributeType[] pointAttribute = new org.geotools.feature.AttributeType[3];
        pointAttribute[0] = AttributeTypeFactory.newAttributeType("centre", com.vividsolutions.jts.geom.Point.class);
        pointAttribute[1] = AttributeTypeFactory.newAttributeType("size",Double.class);
        pointAttribute[2] = AttributeTypeFactory.newAttributeType("rotation",Double.class);
        org.geotools.feature.FeatureType pointType = org.geotools.feature.FeatureTypeFactory.newFeatureType(pointAttribute,"testPoint");
        for(int j=0;j<rows;j++){
            double angle =0.0;
            for(int i=0; i<points; i++){
                
                com.vividsolutions.jts.geom.Point point = makeSamplePoint(geomFac,
                2.0+(double)i*((ex.getWidth()-4)/points),
                (double)j*((50)/rows));
                
                Double size = new Double(5.0+j*5);
                Double rotation = new Double(angle);
                angle+=90.0;
                org.geotools.feature.Feature pointFeature = pointType.create(new Object[]{point,size,rotation});
                //                System.out.println(""+pointFeature);
                features.add(pointFeature);
            }
        }
        
        org.geotools.feature.AttributeType[] lineAttribute = new org.geotools.feature.AttributeType[3];
        lineAttribute[0] = AttributeTypeFactory.newAttributeType("edge", LineString.class);
        lineAttribute[1] = AttributeTypeFactory.newAttributeType("size",Double.class);
        lineAttribute[2] = AttributeTypeFactory.newAttributeType("perpendicularoffset",Double.class);
        org.geotools.feature.FeatureType lineType = org.geotools.feature.FeatureTypeFactory.newFeatureType(lineAttribute,"testLine");
        rows = 3;
        points = 3;
        
        int off = 6;
        for(int j=0;j<rows;j++){
            double angle =0.0;
            int sign = 1;
            for(int i=0; i<points; i++){
                LineString line = makeSampleLineString(geomFac,50 + i*ex.getWidth()/points,j*20,sign*20,20);
                Double size = new Double(12);
                Double poffset = new Double(off);
                sign--;
                org.geotools.feature.Feature lineFeature = lineType.create(new Object[]{line,size,poffset});
                features.add(lineFeature);
            }
            off-=6;
        }
        
        org.geotools.feature.FeatureCollection ft = org.geotools.feature.FeatureCollections.newCollection();
        ft.addAll(features);
        
        return ft;
    }

    private Style loadStyleFromXml() throws Exception {
        java.net.URL base = getClass().getResource("rs-testData/");

        StyleFactory factory = StyleFactory.createStyleFactory();
        java.net.URL surl = new java.net.URL(base + "/textTest.sld");
        SLDStyle stylereader = new SLDStyle(factory, surl);
        Style[] style = stylereader.readXML();

        return style[0];
    }
    
    public Style buildStyle() throws IllegalFilterException {
        StyleBuilder sb = new StyleBuilder();
        Style style = sb.createStyle();
        
        // point style
        Mark triangle = sb.createMark(StyleBuilder.MARK_TRIANGLE, sb.createFill(Color.MAGENTA, 0.5), null);
        PointSymbolizer ps = sb.createPointSymbolizer(sb.createGraphic(null, triangle, null));
        Font font = sb.createFont("Lucida Sans", 10);
        font.setFontSize(sb.attributeExpression("size"));
        PointPlacement pp = sb.createPointPlacement();
        pp.setDisplacement(sb.createDisplacement(5, 0));
        pp.setRotation(sb.attributeExpression("rotation"));
        Fill fill = sb.createFill(Color.decode("#AAAA00"));
        TextSymbolizer ts = sb.createTextSymbolizer(fill, new Font[] {font}, sb.createHalo(), sb.literalExpression("Point Label"), pp, null);
        style.addFeatureTypeStyle(sb.createFeatureTypeStyle("testPoint", 
          new Symbolizer[] {ps, ts}));
        
        // line style
        Font font2 = sb.createFont("Lucida Sans", 10);
        font2.setFontSize(sb.attributeExpression("size"));
        TextSymbolizer ts2 = sb.createStaticTextSymbolizer(Color.decode("#FFAA00"), font2, "Line Label");
        ts2.setHalo(sb.createHalo(Color.RED, 3));
        ts2.setLabelPlacement(sb.createLinePlacement(sb.attributeExpression("perpendicularoffset")));
        style.addFeatureTypeStyle(sb.createFeatureTypeStyle("testLine", 
          new Symbolizer[] {sb.createLineSymbolizer(Color.BLUE, 1), ts2})); 
         
        
        return style;
    }

    /**
     * Test lite renderer and style loaded from xml file
     */
    public void testLiteRendererXml() throws Exception {
        MapContext ctx = new DefaultMapContext();
        ctx.addLayer(buildFeatureCollection(), loadStyleFromXml());
        
        performTestOnRenderer(new LiteRenderer(ctx), "xml");
    }
    
    /**
     * Test lite renderer and style created with the style builder
     */
    public void testLiteRendererBuilder() throws Exception {
    	MapContext ctx = new DefaultMapContext();
    	ctx.addLayer(buildFeatureCollection(), buildStyle());
        
        performTestOnRenderer(new LiteRenderer(ctx), "builder");
    }

    /**
     * Test j2d renderer
     */
    public void testJ2DRendererXml() throws Exception {
        ContextFactory cf = ContextFactory.createFactory();
        Context ctx = cf.createContext();
        Layer layer = cf.createLayer(buildFeatureCollection(), loadStyleFromXml());
        ctx.getLayerList().addLayer(layer);
        
        StyledRenderer sr = new StyledRenderer(null);
        sr.setContext(ctx);
        performTestOnRenderer(sr, "xml");
    }
    
     /**
     * Test j2d renderer
     */
    public void testJ2DRendererBuilder() throws Exception {
        ContextFactory cf = ContextFactory.createFactory();
        Context ctx = cf.createContext();
        Layer layer = cf.createLayer(buildFeatureCollection(), buildStyle());
        ctx.getLayerList().addLayer(layer);
        
        StyledRenderer sr = new StyledRenderer(null);
        sr.setContext(ctx);
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
        at.scale(6, -6);

        if(INTERACTIVE) {
            java.awt.Frame frame = new java.awt.Frame("Text style test (" + renderer.getClass().getName());
            frame.addWindowListener(new java.awt.event.WindowAdapter() {
                    public void windowClosing(java.awt.event.WindowEvent e) {
                        e.getWindow().dispose();
                    }
                });

            java.awt.Panel p = new java.awt.Panel();
            frame.add(p);
            frame.setSize(550, 480);
            frame.setLocation(0, 0);
            frame.setVisible(true);

            renderer.paint((Graphics2D) p.getGraphics(), p.getBounds(), at);
            
            Thread.sleep(10000);
            frame.dispose();
        }

        int w = 550;
        int h = 480;
        java.awt.image.BufferedImage image = new java.awt.image.BufferedImage(w, h,
                java.awt.image.BufferedImage.TYPE_INT_RGB);
        java.awt.Graphics2D g = (Graphics2D) image.getGraphics();
        g.setColor(java.awt.Color.white);
        g.fillRect(0, 0, w, h);
        renderer.paint(g, new java.awt.Rectangle(0, 0, w, h), at);


        java.io.File file = new java.io.File(base.getPath(),
                "TextStyleTest_" + renderer.getClass().getName().replace('.', '_') + "_" + fileSuffix + ".png");
        java.io.FileOutputStream out = new java.io.FileOutputStream(file);
        boolean fred = javax.imageio.ImageIO.write(image, "PNG", out);
        RenderedImage image1 = (RenderedImage) JAI.create("fileload", file.toString());

        if (!fred) {
            System.out.println("Failed to write image to " + file.toString());
        }

        java.io.File file2 = new java.io.File(base.getPath() + "/exemplars/",
                "TextStyleTest_" + renderer.getClass().getName().replace('.', '_') + ".png");

        RenderedImage image2 = (RenderedImage) JAI.create("fileload", file2.toString());

        assertNotNull("Failed to load exemplar image", image2);

        Raster data = image1.getData();
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
    
    private LineString makeSampleLineString(final GeometryFactory geomFac, double xstart, double ystart, double width, double height) {
        Coordinate[] linestringCoordinates = new Coordinate[2];
        linestringCoordinates[0] = new Coordinate(xstart,ystart);
        
        linestringCoordinates[1] = new Coordinate(xstart+width,ystart+height);
        
        LineString line = geomFac.createLineString(linestringCoordinates);
        
        return line;
    }
}
