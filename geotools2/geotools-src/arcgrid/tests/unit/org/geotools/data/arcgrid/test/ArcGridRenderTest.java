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
 * ArcGridStatTest.java
 * JUnit based test
 *
 * Created on 12 February 2002, 21:27
 */
package org.geotools.data.arcgrid.test;

import com.vividsolutions.jts.geom.Envelope;
import org.geotools.cs.AxisInfo;
import org.geotools.cs.CoordinateSystem;
import org.geotools.cs.CoordinateSystemFactory;
import org.geotools.cs.DatumType;
import org.geotools.cs.LocalDatum;
import org.geotools.data.DataSourceException;
import org.geotools.data.arcgrid.ArcGridDataSource;
import org.geotools.data.arcgrid.ArcGridRaster;
import org.geotools.factory.FactoryConfigurationError;
import org.geotools.feature.FeatureCollection;
import org.geotools.filter.Filter;
import org.geotools.map.DefaultMapContext;
import org.geotools.map.MapContext;
import org.geotools.renderer.lite.LiteRenderer;
import org.geotools.styling.FeatureTypeStyle;
import org.geotools.styling.RasterSymbolizer;
import org.geotools.styling.Rule;
import org.geotools.styling.Style;
import org.geotools.styling.StyleFactory;
import org.geotools.styling.Symbolizer;
import org.geotools.units.Unit;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import javax.imageio.ImageIO;


/**
 * DOCUMENT ME!
 *
 * @author Christiaan ten Klooster
 */
public class ArcGridRenderTest extends TestCaseSupport {
    private static boolean setup = false;
    private static ArcGridDataSource ds;
    private static String dataFolder;

    public ArcGridRenderTest(String testName) {
        super(testName);
    }

    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(suite(ArcGridRenderTest.class));
    }

    public void setUp() throws Exception {
        if (setup) {
            return;
        }

        setup = true;

        URL url = getTestResource("ArcGrid.asc");
        ds = new ArcGridDataSource(url);

        // Build the coordinate system
        DatumType.Local type = (DatumType.Local) DatumType.getEnum(DatumType.Local.MINIMUM);
        LocalDatum ld = CoordinateSystemFactory.getDefault().createLocalDatum("", type);
        AxisInfo[] ai = { AxisInfo.X, AxisInfo.Y };

        CoordinateSystem cs = CoordinateSystemFactory.getDefault().createLocalCoordinateSystem("RD",
                ld, Unit.METRE, ai);

        ds.setCoordinateSystem(cs);

        if (ds == null) {
            fail("unable to build datasource " + url);
        }

        System.out.println("get a datasource " + ds);
    }

    public void testRenderImage() throws Exception {
        renderImage("renderedArcGrid.jpg");
    }

    private void renderImage(String filename)
        throws DataSourceException, FactoryConfigurationError, FileNotFoundException, IOException {
        Filter filter = null;
        FeatureCollection ft = ds.getFeatures(filter);
        MapContext mapContext = new DefaultMapContext();
        StyleFactory sFac = StyleFactory.createStyleFactory();
        Envelope ex = ds.getBounds();

        //The following is complex, and should be built from
        //an SLD document and not by hand
        RasterSymbolizer rs = sFac.getDefaultRasterSymbolizer();
        Rule rule = sFac.createRule();
        rule.setSymbolizers(new Symbolizer[] { rs });

        FeatureTypeStyle fts = sFac.createFeatureTypeStyle(new Rule[] { rule });
        Style style = sFac.createStyle();
        style.setFeatureTypeStyles(new FeatureTypeStyle[] { fts });
        mapContext.addLayer(ft, style);

        ArcGridRaster arcGridRaster = ds.openArcGridRaster();
        arcGridRaster.parseHeader();

        int w = arcGridRaster.getNCols();
        int h = arcGridRaster.getNRows();

        LiteRenderer renderer = new LiteRenderer(mapContext);
        BufferedImage image = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = (Graphics2D) image.getGraphics();
        g.setColor(Color.white);
        g.fillRect(0, 0, w, h);

        Rectangle paintArea = new Rectangle(w, h);
        Envelope dataArea = mapContext.getLayerBounds();
        AffineTransform at = renderer.worldToScreenTransform(dataArea, paintArea);
        renderer.paint(g, paintArea, at);

        java.net.URL base = getClass().getResource("testData/");
        File file = new File(java.net.URLDecoder.decode(base.getPath(), "UTF-8"), filename);
        System.out.println("Writing to " + file.getAbsolutePath());

        FileOutputStream out = new FileOutputStream(file);
        ImageIO.write(image, "JPEG", out);
    }
}
