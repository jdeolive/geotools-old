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
package org.geotools.data.postgis;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;
import junit.framework.*;

/*
 * GmlSuite.java
 * JUnit based test
 *
 * Created on 04 March 2002, 16:09
 */
import org.geotools.data.*;
import org.geotools.data.DataSource;
import org.geotools.data.DataSourceFinder;
import org.geotools.feature.*;
import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.net.URL;
import java.util.*;
import java.util.HashMap;
import java.util.Iterator;


/**
 * Tests the PostgisDataSource factory and the DataSourceFinder.
 *
 * @author ian<br>
 * @author Chris Holmes, TOPP
 */
public class ServiceTest extends TestCase {
    public ServiceTest(java.lang.String testName) {
        super(testName);
    }

    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(suite());
    }

    public static Test suite() {
        TestSuite suite = new TestSuite(ServiceTest.class);

        return suite;
    }

    public void testIsAvailable() {
        Iterator list = DataSourceFinder.getAvailableDataSources();
        boolean found = false;

        while (list.hasNext()) {
            DataSourceFactorySpi fac = (DataSourceFactorySpi) list.next();

            if (fac instanceof PostgisDataSourceFactory) {
                found = true;

                break;
            }
        }

        assertTrue("PostgisDataSourceFactory not registered", found);
    }

    public void testPGDataSourceFactory() {
        PostgisDataSourceFactory pgdsFactory = new PostgisDataSourceFactory();
        String desc = pgdsFactory.getDescription();
        assertEquals("PostGIS spatial database", desc);

        HashMap params = new HashMap();
        assertTrue(!pgdsFactory.canProcess(params));
        params.put("dbtype", "postgs");
        assertTrue(!pgdsFactory.canProcess(params));
        params.put("dbtype", "postgis");
        assertTrue(!pgdsFactory.canProcess(params));
        params.put("host", "www.openplans.org");
        assertTrue(!pgdsFactory.canProcess(params));
        params.put("user", "postgis");
        assertTrue(!pgdsFactory.canProcess(params));
        //params.put("port", "5432");
        //assertTrue(!pgdsFactory.canProcess(params));
        params.put("table", "blorg");
        assertTrue(!pgdsFactory.canProcess(params));

        //assertNull(pgdsFactory.createDataSource(params));
        try {
            params.put("database", "postgis_test");
            assertNull(pgdsFactory.createDataSource(params));
        } catch (DataSourceException dse) {
            assertTrue(dse.getMessage().startsWith("Unable to connect"));
        }
    }

    public void testPostgisDataSource() throws Exception {
        HashMap params = new HashMap();
        params.put("dbtype", "postgis");
        params.put("host", "feathers.leeds.ac.uk");
        params.put("port", "5432");
        params.put("database", "postgis_test");

        DataSource nullDS = DataSourceFinder.getDataSource(params);
        assertNull(nullDS);
        params.put("user", "postgis_ro");
        params.put("passwd", "postgis_ro");
        params.put("table", "testset");
        params.put("charset", "iso-8859-1");

        DataSource ds = DataSourceFinder.getDataSource(params);
        assertNotNull(ds);
    }
}
