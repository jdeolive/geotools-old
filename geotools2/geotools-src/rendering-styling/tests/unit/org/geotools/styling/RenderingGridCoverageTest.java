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
 * StylingTest.java
 * JUnit based test
 *
 * Created on April 12, 2002, 1:18 PM
 */
package org.geotools.styling;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.geom.PrecisionModel;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.textui.TestRunner;
import org.geotools.cs.AxisInfo;
import org.geotools.cs.CoordinateSystemFactory;
import org.geotools.cs.GeographicCoordinateSystem;
import org.geotools.cs.HorizontalDatum;
import org.geotools.cs.PrimeMeridian;
import org.geotools.ct.MathTransform;
import org.geotools.ct.MathTransformFactory;
import org.geotools.cv.Category;
import org.geotools.cv.SampleDimension;
import org.geotools.feature.AttributeType;
import org.geotools.feature.AttributeTypeFactory;
import org.geotools.feature.Feature;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureCollections;
import org.geotools.feature.FeatureType;
import org.geotools.feature.FeatureTypeFactory;
import org.geotools.filter.FilterFactory;
import org.geotools.gc.GridCoverage;
import org.geotools.map.Context;
import org.geotools.map.ContextFactory;
import org.geotools.renderer.lite.LiteRenderer;
import org.geotools.styling.FeatureTypeStyle;
import org.geotools.styling.RasterSymbolizer;
import org.geotools.styling.Rule;
import org.geotools.styling.Style;
import org.geotools.styling.StyleFactory;
import org.geotools.styling.Symbolizer;
import org.geotools.units.Unit;
import java.awt.Color;
import java.awt.Frame;
import java.awt.Graphics2D;
import java.awt.Panel;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.Raster;
import java.awt.image.RenderedImage;
import java.awt.image.WritableRaster;
import java.util.Date;
import java.util.logging.Logger;
import javax.media.jai.JAI;

//Logging system
import javax.media.jai.RasterFactory;
import org.geotools.renderer.Renderer2D;
import org.geotools.renderer.j2d.StyledRenderer;


/**
 * Simple testing for grid coverage rendering
 *
 * @author aaime
 */
public class RenderingGridCoverageTest extends TestCase {
    /** Grid coverage width in cells */
    private static final int WIDTH = 1000;

    /** Grid coverage height in cells */
    private static final int HEIGHT = 1000;
    
    private static final boolean INTERACTIVE = false;


    /**
     * Creates a new RenderingGridCoverageTest object.
     *
     * @param testName DOCUMENT ME!
     */
    public RenderingGridCoverageTest(java.lang.String testName) {
        super(testName);
    }

    /**
     * DOCUMENT ME!
     *
     * @param args DOCUMENT ME!
     */
    public static void main(java.lang.String[] args) {
        TestRunner.run(suite());
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public static Test suite() {
        TestSuite suite = new TestSuite(RenderingGridCoverageTest.class);

        return suite;
    }

    /**
     * DOCUMENT ME!
     *
     * @throws Exception DOCUMENT ME!
     */
    public void testEmtpyStyleLiteRenderer() throws Exception {
        FeatureCollection fc = wrapGcInFeatureCollection(createGrid());
        Style style = createEmtpyRasterStyle();
        
        ContextFactory cfac = ContextFactory.createFactory();
        Context ctx = cfac.createContext();
        ctx.getLayerList().addLayer(cfac.createLayer(fc, style));
        
        LiteRenderer renderer = new LiteRenderer(ctx);
        performTestOnRenderer(renderer, "", 300, 300, 100, 100);
    }
    
    /**
     * DOCUMENT ME!
     *
     * @throws Exception DOCUMENT ME!
     */
    public void testEmtpyStyleStyledRenderer() throws Exception {
        FeatureCollection fc = wrapGcInFeatureCollection(createGrid());
        Style style = createEmtpyRasterStyle();
        
        ContextFactory cfac = ContextFactory.createFactory();
        Context ctx = cfac.createContext();
        ctx.getLayerList().addLayer(cfac.createLayer(fc, style));
        
        StyledRenderer renderer = new StyledRenderer(null);
        renderer.setContext(ctx);
        performTestOnRenderer(renderer, "", 300, 300, 100, 100);
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    private static Style createEmtpyRasterStyle() {
        StyleFactory sFac = StyleFactory.createStyleFactory();
        RasterSymbolizer rsymb = sFac.createRasterSymbolizer(
                "grid", null, null, null, null, null, null, null);
        Rule rule = sFac.createRule();
        rule.setSymbolizers(new Symbolizer[] {rsymb});

        FeatureTypeStyle fts = sFac.createFeatureTypeStyle(new Rule[] {rule});
        Style style = sFac.createStyle();
        style.setFeatureTypeStyles(new FeatureTypeStyle[] {fts});

        return style;
    }

    /**
     * DOCUMENT ME!
     *
     * @param fc DOCUMENT ME!
     * @param style DOCUMENT ME!
     *
     * @throws Exception DOCUMENT ME!
     */
    private void contextArchitectureTest(FeatureCollection fc, Style style)
        throws Exception {
        ContextFactory cfac = ContextFactory.createFactory();
        Context ctx = cfac.createContext();
        ctx.getLayerList().addLayer(cfac.createLayer(fc, style));

        LiteRenderer renderer = new LiteRenderer(ctx);

        Frame frame = new Frame("rendering test");
        frame.addWindowListener(
            new WindowAdapter() {
                public void windowClosing(WindowEvent e) {
                    e.getWindow().dispose();
                }
            });

        Panel p = new Panel();

        frame.add(p);

        frame.setSize(300, 300);
        p.setSize(300, 300); // make the panel square ?
        frame.setLocation(300, 0);
        frame.setVisible(true);

        Date start = new Date();
        renderer.paint(
            (Graphics2D) p.getGraphics(), p.getBounds(), new AffineTransform());

        Date end = new Date();
        System.out.println(
            "Time to render to screen using context: "
            + (end.getTime() - start.getTime()));
        
        // Thread.sleep(300);

        frame.dispose();
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     *
     * @throws Exception DOCUMENT ME!
     */
    private GridCoverage createGrid() throws Exception {
        // Banded raster creation works for floating point types, and for integers
        // up to USHORT, if you want to create an Integer raster, it has to be
        // single banded
        WritableRaster raster = RasterFactory.createBandedRaster(DataBuffer.TYPE_FLOAT, WIDTH,
                HEIGHT, 1, null
            );

        // fill in with silly data :-)
        for (int i = 0; i < HEIGHT; i++) {
            for (int j = 0; j < WIDTH; j++) {
                raster.setSample(i, j, 0, i + j);
            }
        }

        // Create categories: they are needed at the low level for a couple of reasons:
        // * allow use to associate a scaled color map to the data;
        // * allow to change on the fly from a geophisics view of the world, based on
        //   floating point data, to an integer view, more efficient for display
        //   (or the other way round, use integer data and derive floating point for processing
        //   using a linear tranformation).
        // cat1: something we dont' want to show on the screen
        int max = 255; // may choose 65535 if you want more colors
        Category cat1 = new Category("empty", new Color[] {Color.BLACK}, 0, 2, 200.0, 0.0);
        Category cat2 = new Category("sum",
                new Color[] {
                    Color.RED, Color.BLUE, Color.YELLOW, Color.BLUE, Color.YELLOW, Color.BLUE,
                    Color.YELLOW, Color.BLUE
                }, 2, max, (2000.0 - 200) / max, 200.0
            );

        // A sample dimension can be composed of more than one category, each with different
        // color and transformation between integer and float version
        SampleDimension band1 = (new SampleDimension(new Category[] { cat1, cat2 },
                Unit.DIMENSIONLESS
            )).geophysics(true);
        SampleDimension[] bands = new SampleDimension[] { band1 };

        // not get the colormodel from the first band and build a bufferedImage, which
        // can be displayed... only the first band is visible, and it's grayscale since
        // we're working with floating point data (color representation is still not
        // available for floating point data, sorry)
        ColorModel colorModel = band1.getColorModel(0, raster.getNumBands());
        BufferedImage image = new BufferedImage(colorModel, raster, false, null);

        // Build the rest of the info needed to create a grid coverage: an affine transform
        // that maps from matrix indexes to geographic coordinates, and a geographic
        // coordinate system to georeference the grid coverage
        AffineTransform at = AffineTransform.getScaleInstance(0.1, 0.1);
        MathTransformFactory mtFactory = MathTransformFactory.getDefault();
        MathTransform gridToWorld = mtFactory.createAffineTransform(at);
        CoordinateSystemFactory csFactory = CoordinateSystemFactory.getDefault();
        HorizontalDatum datum = HorizontalDatum.WGS84;
        PrimeMeridian meridian = PrimeMeridian.GREENWICH;
        GeographicCoordinateSystem sourceCS = csFactory.createGeographicCoordinateSystem("My source CS",
                Unit.DEGREE, datum, meridian, AxisInfo.LATITUDE, AxisInfo.LONGITUDE 
            );

        // now we do have everything to build a georefenced image
        return new GridCoverage("Simple image", image, sourceCS, gridToWorld, bands,
                null, null
            );

    }

    /**
     * Wraps a grid into a feature collection
     *
     * @param gc
     *
     * @return
     *
     * @throws Exception
     */
    private FeatureCollection wrapGcInFeatureCollection(GridCoverage gc)
        throws Exception {
        // create surrounding polygon
        PrecisionModel pm = new PrecisionModel();
        Rectangle2D rect = gc.getEnvelope().toRectangle2D();
        Coordinate[] coord = new Coordinate[5];
        coord[0] = new Coordinate(rect.getMinX(), rect.getMinY());
        coord[1] = new Coordinate(rect.getMaxX(), rect.getMinY());
        coord[2] = new Coordinate(rect.getMaxX(), rect.getMaxY());
        coord[3] = new Coordinate(rect.getMinX(), rect.getMaxY());
        coord[4] = new Coordinate(rect.getMinX(), rect.getMinY());

        Feature feature = null;

        LinearRing ring = new LinearRing(coord, pm, 0);
        Polygon bounds = new Polygon(ring, pm, 0);

        // create the feature type
        AttributeType geom = AttributeTypeFactory.newAttributeType(
                "geom", Polygon.class);
        AttributeType grid = AttributeTypeFactory.newAttributeType(
                "grid", GridCoverage.class);
        FeatureType schema = null;
        AttributeType[] attTypes = {geom, grid};

        schema = FeatureTypeFactory.newFeatureType(attTypes, "grid");

        // create the feature
        feature = schema.create(new Object[] {bounds, gc});

        // create the feature collection 
        FeatureCollection fc = FeatureCollections.newCollection();
        fc.add(feature);

        return fc;
    }
    
    /**
     * Perform test on the passed renderer, which must be already configured with its context
     */
    private void performTestOnRenderer(Renderer2D renderer, String fileSuffix, int width, int height, double dataWidth, double dataHeigth)
        throws Exception {
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
                "RenderingGridCoverageTest_" + renderer.getClass().getName().replace('.', '_') + "_"
                + fileSuffix + width + "x" + height + ".png");
        java.io.FileOutputStream out = new java.io.FileOutputStream(file);
        boolean fred = javax.imageio.ImageIO.write(image, "PNG", out);

        if (!fred) {
            System.out.println("Failed to write image to " + file.toString());
        }

        java.io.File file2 = new java.io.File(base.getPath() + "/exemplars/",
                "RenderingGridCoverageTest_" + renderer.getClass().getName().replace('.', '_') + "_" + width + "x" + height + ".png");

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
}
