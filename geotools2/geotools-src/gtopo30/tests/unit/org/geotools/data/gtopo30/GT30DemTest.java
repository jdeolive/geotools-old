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
 * ShapefileTest.java
 * JUnit based test
 *
 * Created on 12 February 2002, 21:27
 */
package org.geotools.data.gtopo30;

import com.vividsolutions.jts.geom.Envelope;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.FileOutputStream;
import junit.framework.*;
import org.geotools.data.gtopo30.GTopo30DataSource;
import org.geotools.feature.FeatureCollection;
import org.geotools.filter.Filter;
import org.geotools.map.Context;
import org.geotools.styling.FeatureTypeStyle;
import org.geotools.styling.RasterSymbolizer;
import org.geotools.styling.Rule;
import org.geotools.styling.Style;
import org.geotools.styling.StyleFactory;
import org.geotools.styling.Symbolizer;
import java.net.URL;
import java.net.URLDecoder;
import javax.imageio.ImageIO;
import org.geotools.feature.Feature;
import org.geotools.gc.GridCoverage;


/**
 * DOCUMENT ME!
 *
 * @author Ian Schneider
 * @author James Macgill
 */
public class GT30DemTest extends TestCaseSupport {
    public GT30DemTest(String testName) {
        super(testName);
    }

    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(suite(GT30DemTest.class));
    }

    public void testDem() throws Exception {
        // read dem      
        URL demURL = getTestResource("test.dem");
        GTopo30DataSource ds = new GTopo30DataSource(demURL);
        FeatureCollection fc = ds.getFeatures((Filter) null);
        Envelope ex = ds.getBounds();

        // get the image out of the grid coverage
        Feature f = fc.features().next();
        GridCoverage gc = (GridCoverage) f.getAttribute("grid");
        RenderedImage image = gc.geophysics(false).getRenderedImage();
        
        // write to disk
        FileOutputStream out = new FileOutputStream(getFile("demImage.png"));
        ImageIO.write(image, "PNG", out);
    }
    
    public void testCrop() throws Exception {
        // read dem      
        URL demURL = getTestResource("test.dem");
        GTopo30DataSource ds = new GTopo30DataSource(demURL);
        ds.setCropEnvelope(new Envelope(0, 40, 70, 90));
        FeatureCollection fc = ds.getFeatures((Filter) null);
        System.out.println(ds.getBounds());

        // get the image out of the grid coverage
        Feature f = fc.features().next();
        GridCoverage gc = (GridCoverage) f.getAttribute("grid");
        RenderedImage image = gc.geophysics(false).getRenderedImage();

        // write to disk
        FileOutputStream out = new FileOutputStream(getFile("emptyImage.png"));
    }
}
