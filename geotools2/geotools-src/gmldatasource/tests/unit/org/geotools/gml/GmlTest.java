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
package org.geotools.gml;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import junit.framework.*;

/*
 * GmlSuite.java
 * JUnit based test
 *
 * Created on 04 March 2002, 16:09
 */
import org.geotools.data.*;
import org.geotools.data.gml.GMLDataSource;
import org.geotools.feature.*;
import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.net.URL;
import java.util.*;
import java.util.logging.Logger;


/**
 * DOCUMENT ME!
 *
 * @author ian
 */
public class GmlTest extends TestCase {
    /** The logger for the GML module. */
    private static final Logger LOGGER = Logger.getLogger(
            "org.geotools.data.gml");
    static int NTests = 7;
    FeatureCollection table = null;

    public GmlTest(java.lang.String testName) {
        super(testName);
    }

    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(suite());
    }

    public static Test suite() {
        TestSuite suite = new TestSuite(GmlTest.class);

        return suite;
    }

    public void testParsingHoles() throws Exception {
         String dataFolder = System.getProperty("dataFolder");

        if (dataFolder == null) {
            //then we are being run by maven
            dataFolder = System.getProperty("basedir");
            dataFolder += "/tests/unit/testData";
        }

        URL url = new URL("file:///" + dataFolder + "/testGML11Hole.gml");
        System.out.println("Testing ability to load " + url
            + " as Feature datasource");

        DataSource ds = new GMLDataSource(url);

        table = ds.getFeatures(Query.ALL);

        LOGGER.fine("first feature is " + table.features().next());
        assertEquals(1, table.size());

        // TODO: add more tests here
        Iterator i = table.iterator();
        LOGGER.fine("Got " + table.size() + " features");

        while (i.hasNext()) {
            Feature f = (Feature)i.next();
            Polygon geom = (Polygon)f.getDefaultGeometry();
            assertEquals(2,geom.getNumInteriorRing());
            //LOGGER.fine("feature is " + i.next());
        }
    }
    
    public void testGMLDataSource() throws Exception {
        // no try block, a thrown exception will cause it a fail and should
        //print the trace to the output.
        String dataFolder = System.getProperty("dataFolder");

        if (dataFolder == null) {
            //then we are being run by maven
            dataFolder = System.getProperty("basedir");
            dataFolder += "/tests/unit/testData";
        }

        URL url = new URL("file:///" + dataFolder + "/testGML7Features.gml");
        System.out.println("Testing ability to load " + url
            + " as Feature datasource");

        DataSource ds = new GMLDataSource(url);

        table = ds.getFeatures(Query.ALL);

        //}catch(DataSourceException exp) {
        //   System.out.println("Exception requesting Extent : "+exp.getClass().getName()+" : "+exp.getMessage());
        //   exp.printStackTrace();
        //}
        LOGGER.fine("first feature is " + table.features().next());
        assertEquals(7, table.size());

        // TODO: add more tests here
        Iterator i = table.iterator();
        LOGGER.fine("Got " + table.size() + " features");

        while (i.hasNext()) {
            LOGGER.fine("feature is " + i.next());
        }
    }
}
