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
 * SVGSuite.java
 * JUnit based test
 *
 * Created on 08 June 2002, 00:04
 */
package org.geotools.svg;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.geotools.data.DataSource;
import org.geotools.data.Query;
import org.geotools.data.gml.GMLDataSource;
import org.geotools.feature.FeatureCollection;
import org.geotools.map.DefaultMapContext;
import org.geotools.map.MapContext;
import org.geotools.styling.SLDStyle;
import org.geotools.styling.Style;
import org.geotools.styling.StyleFactory;
import java.awt.Dimension;
import java.io.File;
import java.io.FileOutputStream;
import java.net.URL;


/**
 * DOCUMENT ME!
 *
 * @author James
 */
public class SVGTest extends TestCase {
    String dataFolder;

    public SVGTest(java.lang.String testName) {
        super(testName);

        dataFolder = new File(getResourcePath("/testData/bluelake.svg")).getParent();
    }

    private String getResourcePath(String resourceName) {
        URL r = getClass().getResource(resourceName);

        if (r == null) {
            throw new RuntimeException("Could not locate resource : " + resourceName);
        }

        return r.getFile();
    }

    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(suite());
    }

    public static Test suite() {
        TestSuite suite = new TestSuite(SVGTest.class);

        return suite;
    }

    public void testGenerateSVG() {
        String stylefile = "simple.sld";
        String gmlfile = "simple.gml";
        createSVG("simple.sld", "simple.gml", "simple.svg");
    }

    public void testBlueLake() {
        String stylefile = "bluelake.sld";
        String gmlfile = "bluelake.gml";
        createSVG(stylefile, gmlfile, "bluelake.svg");
    }

    public void testNameFilterSVG() {
        createSVG("nameFilter.sld", "simple.gml", "nameFilter.svg");
    }

    /**
     * DOCUMENT ME!
     *
     * @param stylefile
     * @param gmlfile
     * @param outfile DOCUMENT ME!
     */
    private void createSVG(final String stylefile, final String gmlfile, final String outfile) {
        try {
            GenerateSVG gen = new GenerateSVG();
            URL url = new URL("file:///" + dataFolder + "/" + gmlfile);
            DataSource ds = new GMLDataSource(url);
            FeatureCollection fc = ds.getFeatures(Query.ALL);

            File f = new File(dataFolder, stylefile);

            MapContext mapContext = new DefaultMapContext();
            StyleFactory sFac = StyleFactory.createStyleFactory();
            SLDStyle reader = new SLDStyle(sFac, f);
            Style[] style = reader.readXML();
            mapContext.addLayer(fc, style[0]);

            url = new URL("file:///" + dataFolder + "/" + outfile);

            FileOutputStream out = new FileOutputStream(url.getFile());

            gen.setCanvasSize(new Dimension(500, 500));
            gen.go(mapContext, fc.getBounds(), out);
        } catch (Exception e) {
            System.out.println(e);
            e.printStackTrace();
            fail("failed because of exception " + e.toString());
        }
    }
}
