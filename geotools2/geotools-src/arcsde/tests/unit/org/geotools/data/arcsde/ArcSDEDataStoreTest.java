/* Copyright (c) 2001, 2003 TOPP - www.openplans.org.  All rights reserved.
 * This code is licensed under the GPL 2.0 license, availible at the root
 * application directory.
 */
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
package org.geotools.data.arcsde;

import com.vividsolutions.jts.geom.*;
import junit.framework.TestCase;
import org.geotools.data.*;
import org.geotools.feature.*;
import org.geotools.filter.*;
import org.geotools.gml.GMLFilterDocument;
import org.geotools.gml.GMLFilterGeometry;
import org.xml.sax.helpers.*;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.NoSuchElementException;
import java.util.Properties;
import java.util.logging.Logger;
import javax.xml.parsers.*;


/**
 * SdeDatasource's test cases
 *
 * @author Gabriel Rold?n
 * @version $Id: ArcSDEDataStoreTest.java,v 1.1 2004/03/11 00:36:41 groldan Exp $
 */
public class ArcSDEDataStoreTest extends TestCase {
    /** DOCUMENT ME! */
    private static Logger LOGGER = Logger.getLogger(ArcSDEDataStoreTest.class.getPackage()
                                                                             .getName());

    /** folder used to load test filters */
    private String dataFolder = "/testData/";

    /** DOCUMENT ME! */
    private Properties conProps = null;

    /** DOCUMENT ME! */
    private String point_table;

    /** DOCUMENT ME! */
    private String line_table;

    /** DOCUMENT ME! */
    private String polygon_table;

    /** DOCUMENT ME! */
    private DataStore store;

    /** DOCUMENT ME! */
    FilterFactory ff = FilterFactory.createFilterFactory();

    /**
     * Creates a new ArcSDEDataStoreTest object.
     */
    public ArcSDEDataStoreTest() {
        this("ArcSDE DataStore unit tests");
    }

    /**
     * Creates a new SdeDataSourceTest object.
     *
     * @param name DOCUMENT ME!
     */
    public ArcSDEDataStoreTest(String name) {
        super(name);

        URL folderUrl = getClass().getResource("/testData");
        dataFolder = folderUrl.toExternalForm() + "/";
    }

    /**
     * loads /testData/testparams.properties into a Properties object, wich is
     * used to obtain test tables names and is used as parameter to find the
     * DataStore
     *
     * @throws Exception DOCUMENT ME!
     */
    protected void setUp() throws Exception {
        super.setUp();
        conProps = new Properties();

        String propsFile = "/testData/testparams.properties";
        InputStream in = getClass().getResourceAsStream(propsFile);

        if (in == null) {
            fail("cannot find test params: " + propsFile);
        }

        conProps.load(in);
        point_table = conProps.getProperty("point_table");
        line_table = conProps.getProperty("line_table");
        polygon_table = conProps.getProperty("polygon_table");
        assertNotNull("point_table not defined in " + propsFile, point_table);
        assertNotNull("line_table not defined in " + propsFile, line_table);
        assertNotNull("polygon_table not defined in " + propsFile, polygon_table);
        store = getDataStore();
    }

    /**
     * DOCUMENT ME!
     *
     * @throws Exception DOCUMENT ME!
     */
    protected void tearDown() throws Exception {
        conProps = null;
        super.tearDown();
    }

    /**
     * DOCUMENT ME!
     */
    public void testFinder() {
        DataStore sdeDs = null;

        try {
            sdeDs = DataStoreFinder.getDataStore(conProps);

            String failMsg = sdeDs + " is not an ArcSDEDataStore";
            assertTrue(failMsg, (sdeDs instanceof ArcSDEDataStore));
            LOGGER.info("testFinder OK :" + sdeDs.getClass().getName());
        } catch (IOException ex) {
            ex.printStackTrace();
            fail("can't find the SdeDataSource:" + ex.getMessage());
        }
    }

    /**
     * tests that a connection to a live ArcSDE database can be established
     * with the parameters defined int testparams.properties, and a
     * ArcSDEConnectionPool can be properly setted up
     */
    public void testConnect() {
        LOGGER.info("testing connection to the sde database");

        ConnectionPoolFactory pf = ConnectionPoolFactory.getInstance();
        ConnectionConfig congfig = null;

        try {
            congfig = new ConnectionConfig(conProps);
        } catch (Exception ex) {
            pf.clear(); //close and remove all pools
            fail(ex.getMessage());
        }

        try {
            ArcSDEConnectionPool pool = pf.getPoolFor(congfig);
            LOGGER.info("connection succeed " + pool.getPoolSize()
                + " connections ready");
        } catch (DataSourceException ex) {
            fail(ex.getMessage());
        } finally {
            pf.clear(); //close and remove all pools
        }
    }

    /**
     * test that a ArcSDEDataStore that connects to de configured test database
     * contains the tables defined by the parameters "point_table",
     * "line_table" and "polygon_table", wether ot not they're defined as
     * single table names or as full qualified sde table names (i.e.
     * SDE.SDE.TEST_POINT)
     */
    public void testGetTypeNames() {
        String[] featureTypes = store.getTypeNames();
        assertNotNull(featureTypes);
        testTypeExists(featureTypes, point_table);
        testTypeExists(featureTypes, line_table);
        testTypeExists(featureTypes, polygon_table);
    }

    /**
     * tests that the schema for the defined tests tables are returned.
     */
    public void testGetSchema() {
        FeatureType schema;

        try {
            schema = store.getSchema(point_table);
            assertNotNull(schema);
            assertTrue(schema.getAttributeCount() > 0);
            schema = store.getSchema(line_table);
            assertNotNull(schema);
            assertTrue(schema.getAttributeCount() > 0);
            schema = store.getSchema(polygon_table);
            assertNotNull(schema);
            assertTrue(schema.getAttributeCount() > 0);
            LOGGER.info("testGetSchema OK: " + schema);
        } catch (IOException ex) {
            ex.printStackTrace();
            fail(ex.getMessage());
        }
    }

    /**
     * This method tests the feature reader by opening various simultaneous
     * FeatureReaders using the 3 test tables.
     * 
     * <p>
     * I found experimentally that until 24 simultaneous streams can be opened
     * by a single connection. Each featurereader has an ArcSDE stream opened
     * until its <code>close()</code> method is called or hasNext() returns
     * flase, wich automatically closes the stream. If more than 24
     * simultaneous streams are tryied to be opened upon a single
     * SeConnection, an exception is thrown by de Java ArcSDE API saying that
     * a "NETWORK I/O OPERATION FAILED"
     * </p>
     */
    public void testGetFeatureReader() {
        try {
            final int num_readers = 24;
            String[] typeNames = { point_table, line_table, polygon_table };
            FeatureReader[] readers = new FeatureReader[num_readers];
            int[] counts = new int[num_readers];

            for (int i = 0; i < num_readers;) {
                for (int j = 0; j < typeNames.length; j++, i++) {
                    readers[i] = getReader(typeNames[j]);
                }
            }

            Feature f;
            Geometry geom;
            int count1 = 0;
            int count2 = 0;
            int count3 = 0;
            long t = System.currentTimeMillis();
            boolean hasNext = false;

            while (true) {
                for (int i = 0; i < num_readers; i++) {
                    if (readers[i].hasNext()) {
                        hasNext = true;

                        break;
                    }

                    hasNext = false;
                }

                if (!hasNext) {
                    for (int i = 0; i < num_readers; i++)
                        readers[i].close();

                    break;
                }

                for (int i = 0; i < num_readers; i++) {
                    if (testNext(readers[i])) {
                        ++counts[i];
                    }
                }
            }

            t = System.currentTimeMillis() - t;

            String scounts = "";

            for (int i = 0; i < num_readers; i++)
                scounts += (counts[i] + ", ");

            LOGGER.info("testGetFeatureReader: traversed " + scounts
                + " features simultaneously from " + num_readers
                + " different FeatureReaders in " + t + "ms");
        } catch (Exception ex) {
            ex.printStackTrace();
            fail(ex.getMessage());
        }
    }

    private boolean testNext(FeatureReader r) throws Exception {
        if (r.hasNext()) {
            Feature f = r.next();
            assertNotNull(f);
            assertNotNull(f.getFeatureType());
            assertNotNull(f.getBounds());

            Geometry geom = f.getDefaultGeometry();
            assertNotNull(geom);

            return true;
        }

        return false;
    }

    private FeatureReader getReader(String typeName) throws IOException {
        Query q = new DefaultQuery(typeName, Filter.NONE);
        FeatureReader reader = store.getFeatureReader(q, Transaction.AUTO_COMMIT);
        FeatureType retType = reader.getFeatureType();
        assertNotNull(retType.getDefaultGeometry());
        assertTrue(retType.hasAttributeType("SHAPE"));
        assertTrue(reader.hasNext());

        return reader;
    }

    /**
     * test that getFeatureSource over the point_table table works
     */
    public void testGetFeatureSourcePoint() {
        try {
            testGetFeatureSource(store.getFeatureSource(point_table));
        } catch (IOException ex) {
            ex.printStackTrace();
            fail(ex.getMessage());
        }
    }

    /**
     * DOCUMENT ME!
     */
    public void testGetFeatureSourceLine() {
        try {
            testGetFeatureSource(store.getFeatureSource(line_table));
        } catch (IOException ex) {
            ex.printStackTrace();
            fail(ex.getMessage());
        }
    }

    /**
     * DOCUMENT ME!
     */
    public void testGetFeatureSourcePoly() {
        try {
            testGetFeatureSource(store.getFeatureSource(polygon_table));
        } catch (IOException ex) {
            ex.printStackTrace();
            fail(ex.getMessage());
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @throws IOException DOCUMENT ME!
     */
    public void testGetFeaturesPoint() throws IOException {
        testGetFeatures("points", point_table);
    }

    /**
     * DOCUMENT ME!
     *
     * @throws IOException DOCUMENT ME!
     */
    public void testGetFeaturesLine() throws IOException {
        testGetFeatures("lines", line_table);
    }

    /**
     * DOCUMENT ME!
     *
     * @throws IOException DOCUMENT ME!
     */
    public void testGetFeaturesPolygon() throws IOException {
        testGetFeatures("polygons", polygon_table);
    }

    /**
     * DOCUMENT ME!
     */
    public void testSQLFilterPoints() {
        String uri = getFilterUri("filters.sql.points.filter");
        int expected = getExpectedCount("filters.sql.points.expectedCount");
        testFilter(uri, point_table, expected);
    }

    /**
     * DOCUMENT ME!
     */
    public void testSQLFilterLines() {
        String uri = getFilterUri("filters.sql.lines.filter");
        int expected = getExpectedCount("filters.sql.lines.expectedCount");
        testFilter(uri, line_table, expected);
    }

    /**
     * DOCUMENT ME!
     */
    public void testSQLFilterPolygons() {
        String uri = getFilterUri("filters.sql.polygons.filter");
        int expected = getExpectedCount("filters.sql.polygons.expectedCount");
        testFilter(uri, polygon_table, expected);
    }

    /**
     * DOCUMENT ME!
     */
    public void testBBoxFilterPoints() {
        //String uri = getFilterUri("filters.bbox.points.filter");
        //int expected = getExpectedCount("filters.bbox.points.expectedCount");
        int expected = 6;
        testBBox(point_table, expected);
    }

    /**
     * DOCUMENT ME!
     */
    public void testBBoxFilterLines() {
        //String uri = getFilterUri("filters.bbox.lines.filter");
        //int expected = getExpectedCount("filters.bbox.lines.expectedCount");
        int expected = 22;
        testBBox(line_table, expected);
    }

    /**
     * DOCUMENT ME!
     */
    public void testBBoxFilterPolygons() {
        //String uri = getFilterUri("filters.bbox.polygons.filter");
        //int expected = getExpectedCount("filters.bbox.polygons.expectedCount");
        int expected = 8;
        testBBox(polygon_table, expected);
    }

    /**
     * DOCUMENT ME!
     */
    public void testGeometryIntersectsFilters() {
    }

    /////////////////// HELPER FUNCTIONS ////////////////////////

    /**
     * for a given FeatureSource, makes the following assertions:
     * 
     * <ul>
     * <li>
     * it's not null
     * </li>
     * <li>
     * .getDataStore() != null
     * </li>
     * <li>
     * .getDataStore() == the datastore obtained in setUp()
     * </li>
     * <li>
     * .getSchema() != null
     * </li>
     * <li>
     * .getBounds() != null
     * </li>
     * <li>
     * .getBounds().isNull() == false
     * </li>
     * <li>
     * .getFeatures().getCounr() > 0
     * </li>
     * <li>
     * .getFeatures().reader().hasNex() == true
     * </li>
     * <li>
     * .getFeatures().reader().next() != null
     * </li>
     * </ul>
     * 
     *
     * @param fsource DOCUMENT ME!
     *
     * @throws IOException DOCUMENT ME!
     */
    private void testGetFeatureSource(FeatureSource fsource)
        throws IOException {
        assertNotNull(fsource);
        assertNotNull(fsource.getDataStore());
        assertEquals(fsource.getDataStore(), store);
        assertNotNull(fsource.getSchema());

        FeatureResults results = fsource.getFeatures();
        int count = results.getCount();
        assertTrue(count > 0);
        LOGGER.info("feature count: " + count);

        Envelope env1;
        Envelope env2;
        env1 = fsource.getBounds();
        assertNotNull(env1);
        assertFalse(env1.isNull());
        env2 = fsource.getBounds(Query.ALL);
        assertNotNull(env2);
        assertFalse(env2.isNull());
        env1 = results.getBounds();
        assertNotNull(env1);
        assertFalse(env1.isNull());

        FeatureReader reader = results.reader();
        assertTrue(reader.hasNext());

        try {
            assertNotNull(reader.next());
        } catch (NoSuchElementException ex) {
            ex.printStackTrace();
            fail(ex.getMessage());
        } catch (IllegalAttributeException ex) {
            ex.printStackTrace();
            fail(ex.getMessage());
        }

        reader.close();
    }

    /**
     * DOCUMENT ME!
     *
     * @param filterKey DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    private String getFilterUri(String filterKey) {
        String filterFileName = conProps.getProperty(filterKey);

        if (filterFileName == null) {
            super.fail(filterKey
                + " param not found in tests configurarion properties file");
        }

        String uri = dataFolder + filterFileName;

        return uri;
    }

    /**
     * creates an ArcSDEDataStore using /testData/testparams.properties as
     * holder of datastore parameters
     *
     * @return DOCUMENT ME!
     */
    private ArcSDEDataStore getDataStore() {
        try {
            ConnectionPoolFactory pfac = ConnectionPoolFactory.getInstance();
            ConnectionConfig config = new ConnectionConfig(conProps);
            ArcSDEConnectionPool pool = pfac.getPoolFor(config);
            ArcSDEDataStore ds = new ArcSDEDataStore(pool);

            return ds;
        } catch (Exception ex) {
            ex.printStackTrace();
            fail("cannot obtain datastore: " + ex.getMessage());
        }

        return null;
    }

    /**
     * DOCUMENT ME!
     *
     * @param key DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    private int getExpectedCount(String key) {
        try {
            return Integer.parseInt(conProps.getProperty(key));
        } catch (NumberFormatException ex) {
            super.fail(key
                + " parameter not found or not an integer in testParams.properties");
        }

        return -1;
    }

    /**
     * DOCUMENT ME!
     *
     * @param filterUri DOCUMENT ME!
     * @param table DOCUMENT ME!
     * @param expected DOCUMENT ME!
     */
    private void testFilter(String filterUri, String table, int expected) {
        try {
            Filter filter = parseDocument(filterUri);
            FeatureSource fsource = store.getFeatureSource(table);
            testFilter(filter, fsource, expected);
        } catch (Exception ex) {
            ex.printStackTrace();
            fail(ex.getMessage());
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param filter DOCUMENT ME!
     * @param fsource DOCUMENT ME!
     * @param expected DOCUMENT ME!
     */
    private void testFilter(Filter filter, FeatureSource fsource, int expected) {
        try {
            FeatureResults results = fsource.getFeatures(filter);
            FeatureCollection fc = results.collection();
            int resCount = results.getCount();
            int fCount = fc.size();
            LOGGER.info("results count: " + resCount + " collection size: "
                + fCount);

            Feature f = fc.features().next();
            LOGGER.info("first feature is: " + f.getID());

            String failMsg = "Expected and returned result count does not match";
            assertEquals(failMsg, expected, fCount);
            assertEquals(failMsg, expected, resCount);
        } catch (Exception ex) {
            ex.printStackTrace();
            fail(ex.getMessage());
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param table DOCUMENT ME!
     * @param expected DOCUMENT ME!
     */
    private void testBBox(String table, int expected) {
        try {
            FeatureSource fs = store.getFeatureSource(table);
            Filter bboxFilter = getBBoxfilter(fs);
            testFilter(bboxFilter, fs, expected);
        } catch (Exception ex) {
            ex.printStackTrace();
            super.fail(ex.getMessage());
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param fs DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     *
     * @throws Exception DOCUMENT ME!
     */
    private Filter getBBoxfilter(FeatureSource fs) throws Exception {
        Envelope env = new Envelope(-60, -40, -55, -20);
        BBoxExpression bbe = ff.createBBoxExpression(env);
        org.geotools.filter.GeometryFilter gf = ff.createGeometryFilter(AbstractFilter.GEOMETRY_BBOX);
        FeatureType schema = fs.getSchema();
        Expression attExp = ff.createAttributeExpression(schema,
                schema.getDefaultGeometry().getName());
        gf.addLeftGeometry(attExp);
        gf.addRightGeometry(bbe);

        return gf;
    }

    /**
     * checks for the existence of <code>table</code> in
     * <code>featureTypes</code>. <code>table</code> must be a full qualified
     * sde feature type name. (i.e "TEST_POINT" == "SDE.SDE.TEST_POINT")
     *
     * @param featureTypes DOCUMENT ME!
     * @param table DOCUMENT ME!
     */
    private void testTypeExists(String[] featureTypes, String table) {
        for (int i = 0; i < featureTypes.length; i++) {
            if (featureTypes[i].equalsIgnoreCase(table.toUpperCase())) {
                LOGGER.info("testTypeExists OK: " + table);

                return;
            }
        }

        fail("table " + table + " not found in getFeatureTypes results");
    }

    /**
     * DOCUMENT ME!
     *
     * @param wich DOCUMENT ME!
     * @param table DOCUMENT ME!
     *
     * @throws IOException DOCUMENT ME!
     */
    private void testGetFeatures(String wich, String table)
        throws IOException {
        LOGGER.info("getting all features from " + table);

        FeatureSource source = store.getFeatureSource(table);
        int expectedCount = getExpectedCount("getfeatures." + wich
                + ".expectedCount");
        int fCount = source.getCount(Query.ALL);
        String failMsg = "Expected and returned result count does not match";
        assertEquals(failMsg, expectedCount, fCount);

        FeatureResults fresults = source.getFeatures();
        FeatureCollection features = fresults.collection();
        failMsg = "FeatureResults.getCount and .collection().size thoes not match";
        assertEquals(failMsg, fCount, features.size());
        LOGGER.info("fetched " + fCount + " features for " + wich
            + " layer, OK");
    }

    /**
     * stolen from filter module tests
     *
     * @param uri DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     *
     * @throws Exception DOCUMENT ME!
     */
    private Filter parseDocument(String uri) throws Exception {
        LOGGER.finest("about to create parser");

        // chains all the appropriate filters together (in correct order)
        //  and initiates parsing
        TestFilterHandler filterHandler = new TestFilterHandler();
        FilterFilter filterFilter = new FilterFilter(filterHandler, null);
        GMLFilterGeometry geometryFilter = new GMLFilterGeometry(filterFilter);
        GMLFilterDocument documentFilter = new GMLFilterDocument(geometryFilter);
        SAXParserFactory fac = SAXParserFactory.newInstance();
        SAXParser parser = fac.newSAXParser();
        ParserAdapter p = new ParserAdapter(parser.getParser());
        p.setContentHandler(documentFilter);
        LOGGER.fine("just made parser, " + uri);
        p.parse(uri);
        LOGGER.finest("just parsed: " + uri);

        Filter filter = filterHandler.getFilter();

        return filter;
    }
}
