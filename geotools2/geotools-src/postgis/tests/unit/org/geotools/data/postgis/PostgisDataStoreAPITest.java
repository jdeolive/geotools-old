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

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.MultiLineString;
import org.geotools.data.DataStore;
import org.geotools.data.DataTestCase;
import org.geotools.data.DataUtilities;
import org.geotools.data.DefaultQuery;
import org.geotools.data.DefaultTransaction;
import org.geotools.data.DiffFeatureReader;
import org.geotools.data.EmptyFeatureReader;
import org.geotools.data.EmptyFeatureWriter;
import org.geotools.data.FeatureLock;
import org.geotools.data.FeatureLockFactory;
import org.geotools.data.FeatureLocking;
import org.geotools.data.FeatureReader;
import org.geotools.data.FeatureResults;
import org.geotools.data.FeatureSource;
import org.geotools.data.FeatureStore;
import org.geotools.data.FeatureWriter;
import org.geotools.data.FilteringFeatureReader;
import org.geotools.data.FilteringFeatureWriter;
import org.geotools.data.InProcessLockingManager;
import org.geotools.data.Query;
import org.geotools.data.Transaction;
import org.geotools.data.TransactionStateDiff;
import org.geotools.data.jdbc.ConnectionPool;
import org.geotools.feature.AttributeType;
import org.geotools.feature.Feature;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.geotools.feature.FeatureType;
import org.geotools.feature.IllegalAttributeException;
import org.geotools.filter.FidFilter;
import org.geotools.filter.Filter;
import org.geotools.filter.FilterFactory;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.NoSuchElementException;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;
import java.util.logging.Logger;


/**
 * This class tests the PostgisDataStoreAPI, against the same tests as
 * MemoryDataStore.
 * 
 * <p>
 * The test fixture is available in the shared DataTestCase, really the common
 * elements should move to a shared DataStoreAPITestCase.
 * </p>
 * 
 * <p>
 * This class does require your own DataStore, it will create a table populated
 * with the Features from the test fixture, and run a test, and then remove
 * the table.
 * </p>
 * 
 * <p>
 * Because of the nature of this testing process you cannot run these tests in
 * conjunction with another user, so they cannot be implemented against the
 * public server.
 * </p>
 * 
 * <p>
 * A simple properties file has been constructed,
 * <code>fixture.properties</code>, which you may direct to your own potgis
 * database installation.
 * </p>
 *
 * @author Jody Garnett, Refractions Research
 */
public class PostgisDataStoreAPITest extends DataTestCase {
    /** The logger for the filter module. */
    private static final Logger LOGGER = Logger.getLogger(
            "org.geotools.data.postgis");
                
    static boolean CHECK_TYPE = false;
    PostgisDataStore data;
    ConnectionPool pool;
    String database;

    String victim = null;//"testGetFeatureStoreModifyFeatures1";
    /**
     * Constructor for MemoryDataStoreTest.
     *
     * @param arg0
     */
    public PostgisDataStoreAPITest(String test ) {
        super(test);
        if( victim != null && !test.equals( victim ) ) {
            throw new AssertionError("test supressed "+test );        
        }
    }

    /**
     * @see TestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();

        PropertyResourceBundle resource;
        resource = new PropertyResourceBundle(this.getClass()
                                                  .getResourceAsStream("fixture.properties"));

        String namespace = resource.getString("namespace");
        String host = resource.getString("host");
        int port = Integer.parseInt(resource.getString("port"));
        String database = resource.getString("database");
	this.database = database;
        String user = resource.getString("user");
        String password = resource.getString("password");

        if (namespace.equals("http://www.geotools.org/data/postgis")) {
            //
            throw new IllegalStateException(
                "The fixture.properties file needs to be configured for your own database");
        }

        PostgisConnectionFactory factory1 = new PostgisConnectionFactory(host,
                port, database);
        pool = factory1.getConnectionPool(user, password);

        setUpRoadTable();
        setUpRiverTable();

        if (CHECK_TYPE) {
            checkTypesInDataBase();
            CHECK_TYPE = false; // just once
        }

        data = new PostgisDataStore(pool, "public", getName(),
                PostgisDataStore.OPTIMIZE_SAFE);

        //
        // Update Fixture to reflect the actual data in the database
        // I am doing this because it
        updateRoadFeaturesFixture();
        updateRiverFeaturesFixture();
        
    }

    /**
     * This is a quick hack to have our fixture reflect the FIDs in the
     * database.
     * 
     * <p>
     * When the dataStore learns how to preserve our FeatureIds this won't be
     * required.
     * </p>
     *
     * @throws Exception
     */
    protected void updateRoadFeaturesFixture() throws Exception {
        Connection conn = pool.getConnection();
        FeatureReader reader = data.getFeatureReader(new DefaultQuery("road",
                    Filter.NONE), Transaction.AUTO_COMMIT);

        Envelope bounds = new Envelope();        
        try {
            Feature f;

            while (reader.hasNext()) {
                f = reader.next();
                int index = ((Integer) f.getAttribute("id")).intValue() - 1;
                roadFeatures[index] = f;
                bounds.expandToInclude( f.getBounds() );
            }                                        
        } finally {
            reader.close();
            conn.close();
        }
        if( !roadBounds.equals( bounds ) ){
            System.out.println( "warning! Database changed bounds()");
            System.out.println( "was:"+roadBounds );
            System.out.println( "now:"+bounds );            
            roadBounds = bounds;
        }
        Envelope bounds12 = new Envelope();
        bounds12.expandToInclude( roadFeatures[0].getBounds() );
        bounds12.expandToInclude( roadFeatures[1].getBounds() );
        if( !rd12Bounds.equals( bounds12 ) ){
            System.out.println( "warning! Database changed bounds of rd1 & rd2");
            System.out.println( "was:"+rd12Bounds );
            System.out.println( "now:"+bounds12 );            
            rd12Bounds = bounds12;
        }         
                
        FilterFactory factory = FilterFactory.createFilterFactory();
        rd1Filter = factory.createFidFilter(roadFeatures[0].getID());
        rd2Filter = factory.createFidFilter(roadFeatures[1].getID());

        FidFilter create = factory.createFidFilter();
        create.addFid(roadFeatures[0].getID());
        create.addFid(roadFeatures[1].getID());

        rd12Filter = create;
    }

    /**
     * This is a quick hack to have our fixture reflect the FIDs in the
     * database.
     * 
     * <p>
     * When the dataStore learns how to preserve our FeatureIds this won't be
     * required.
     * </p>
     *
     * @throws Exception DOCUMENT ME!
     */
    protected void updateRiverFeaturesFixture() throws Exception {
        Connection conn = pool.getConnection();
        FeatureReader reader = data.getFeatureReader(new DefaultQuery("river",
                    Filter.NONE), Transaction.AUTO_COMMIT);

        Envelope bounds = new Envelope();                
        try {
            Feature f;

            while (reader.hasNext()) {
                f = reader.next();

                int index = ((Integer) f.getAttribute("id")).intValue() - 1;
                riverFeatures[index] = f;
                bounds.expandToInclude( f.getBounds() );                
            }
        } finally {
            reader.close();
            conn.close();
        }
        if( !riverBounds.equals( bounds ) ){
            System.out.println( "warning! Database changed bounds of river");
            System.out.println( "was:"+riverBounds );
            System.out.println( "now:"+bounds );            
            riverBounds = bounds;
        }        
        
        FilterFactory factory = FilterFactory.createFilterFactory();
        rv1Filter = FilterFactory.createFilterFactory().createFidFilter(riverFeatures[0]
                .getID());
    }

    protected void checkTypesInDataBase() throws SQLException {
        Connection conn = pool.getConnection();

        try {
            DatabaseMetaData md = conn.getMetaData();
            ResultSet rs = 
                //md.getTables( catalog, null, null, null );
                md.getTables(null, "public", "%", new String[] { "TABLE", });
            ResultSetMetaData rsmd = rs.getMetaData();
            int NUM = rsmd.getColumnCount();
            System.out.print(" ");

            for (int i = 1; i <= NUM; i++) {
                System.out.print(rsmd.getColumnName(i));
                System.out.flush();
                System.out.print(":");
                System.out.flush();
                System.out.print(rsmd.getColumnClassName(i));
                System.out.flush();

                if (i < NUM) {
                    System.out.print(",");
                    System.out.flush();
                }
            }

            System.out.println();

            while (rs.next()) {
                System.out.print(rs.getRow());
                System.out.print(":");
                System.out.flush();

                for (int i = 1; i <= NUM; i++) {
                    System.out.print(rsmd.getColumnName(i));
                    System.out.flush();
                    System.out.print("=");
                    System.out.flush();
                    System.out.print(rs.getString(i));
                    System.out.flush();

                    if (i < NUM) {
                        System.out.print(",");
                        System.out.flush();
                    }
                }

                System.out.println();
            }
        } finally {
            conn.close();
        }
    }

    protected void setUpRoadTable() throws Exception {
        Connection conn = pool.getConnection();

        try {
            Statement s = conn.createStatement();
            s.execute("SELECT dropgeometrycolumn( '" + database + "','road','geom')");
        } catch (Exception ignore) {
        }

        try {
            Statement s = conn.createStatement();
            s.execute("DROP TABLE road");
        } catch (Exception ignore) {
        }
        
        try {
            Statement s = conn.createStatement();

            //postgis = new PostgisDataSource(connection, FEATURE_TABLE);
            s.execute("CREATE TABLE road (fid varchar PRIMARY KEY, id int )");
            s.execute(
                "SELECT AddGeometryColumn('" + database + "', 'road', 'geom', 0, 'LINESTRING', 2);");
            s.execute("ALTER TABLE road add name varchar;");

            for (int i = 0; i < roadFeatures.length; i++) {
                Feature f = roadFeatures[i];

                String ql = "INSERT INTO road (fid,id,geom,name) VALUES ("
                    + "'"+f.getID() + "',"                
                    + f.getAttribute("id") + "," + "GeometryFromText('"
                    + ((Geometry) f.getAttribute("geom")).toText() + "', 0 ),"
                    + "'" + f.getAttribute("name") + "')";

                s.execute(ql);
            }
        } finally {
            conn.close();
        }
    }

    protected void killTestTables() throws Exception {
        Connection conn = pool.getConnection();

        try {
            Statement s = conn.createStatement();
            s.execute("SELECT dropgeometrycolumn( '" + database + "','road','geom')");
            s.execute("SELECT dropgeometrycolumn( '" + database + "','river','geom')");
        } catch (Exception ignore) {
        }

        try {
            Statement s = conn.createStatement();
            s.execute("DROP TABLE road");
            s.execute("DROP TABLE river");
        } catch (Exception ignore) {
        } finally {
            conn.close();
        }
    }

    protected void setUpRiverTable() throws Exception {
        Connection conn = pool.getConnection();

        try {
            Statement s = conn.createStatement();
            s.execute("SELECT dropgeometrycolumn( '" + database + "','river','geom')");
        } catch (Exception ignore) {
        }

        try {
            Statement s = conn.createStatement();
            s.execute("DROP TABLE river");
        } catch (Exception ignore) {
        }
        try {
            Statement s = conn.createStatement();

            //postgis = new PostgisDataSource(connection, FEATURE_TABLE);
            s.execute("CREATE TABLE river(fid varchar PRIMARY KEY, id int)");
            s.execute(
                "SELECT AddGeometryColumn('" + database + "', 'river', 'geom', 0, 'MULTILINESTRING', 2);");
            s.execute("ALTER TABLE river add river varchar");
            s.execute("ALTER TABLE river add flow float8");

            for (int i = 0; i < riverFeatures.length; i++) {
                Feature f = riverFeatures[i];
                s.execute(
                    "INSERT INTO river (fid, id, geom, river, flow) VALUES ("
                    + "'"+f.getID()+"',"
                    + f.getAttribute("id") + "," + "GeometryFromText('"
                    + f.getAttribute("geom").toString() + "', 0 )," + "'"
                    + f.getAttribute("river") + "',"
                    + f.getAttribute("flow") +
                    ")");
            }
        } finally {
            conn.close();
        }
    }

    /*
     * @see TestCase#tearDown()
     */
    protected void tearDown() throws Exception {
        data = null;

        PostgisConnectionFactory factory1 = new PostgisConnectionFactory("hydra",
                "5432", "jody");
        factory1.free(pool);

        //pool.close();
        super.tearDown();
    }

    public void testGetFeatureTypes() {
        String[] names = data.getTypeNames();        
        assertTrue(contains(names, "road"));
        assertTrue(contains(names, "river"));
    }

    boolean contains(Object[] array, Object expected) {
        if ((array == null) || (array.length == 0)) {
            return false;
        }

        for (int i = 0; i < array.length; i++) {
            if (array[i].equals(expected)) {
                return true;
            }
        }

        return false;
    }

    void assertContains(Object[] array, Object expected) {
        assertFalse(array == null);
        assertFalse(array.length == 0);
        assertNotNull(expected);

        for (int i = 0; i < array.length; i++) {
            if (array[i].equals(expected)) {
                return;
            }
        }

        fail("Contains " + expected);
    }

    /**
     * Like contain but based on match rather than equals
     *
     * @param array DOCUMENT ME!
     * @param expected DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    boolean containsLax(Feature[] array, Feature expected) {
        if ((array == null) || (array.length == 0)) {
            return false;
        }

        FeatureType type = expected.getFeatureType();

        for (int i = 0; i < array.length; i++) {
            if (match(array[i], expected)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Compare based on attributes not getID allows comparison of Diff contents
     *
     * @param expected DOCUMENT ME!
     * @param actual DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    boolean match(Feature expected, Feature actual) {
        FeatureType type = expected.getFeatureType();        
        
        for (int i = 0; i < type.getAttributeCount(); i++) {
            Object av = actual.getAttribute(i);
            Object ev = expected.getAttribute( i );

            if ((av == null) && (ev != null)) {
                return false;
            } else if ((ev == null) && (av != null)) {
                return false;
            } else if (av instanceof Geometry && ev instanceof Geometry) {
                Geometry ag = (Geometry) av;
                Geometry eg = (Geometry) ev;

                if (!ag.equals(eg)) {
                    return false;
                }
            } else if (!av.equals(ev)) {
                return false;
            }
        }
        return true;
    }

    public void testGetSchemaRoad() throws IOException {
        FeatureType expected = roadType;
        FeatureType actual = data.getSchema("road");
        assertEquals("namespace", expected.getNamespace(), actual.getNamespace());
        assertEquals("typeName", expected.getTypeName(), actual.getTypeName());

        //assertEquals( "compare", 0, DataUtilities.compare( expected, actual ));
        assertEquals("attributeCount", expected.getAttributeCount(),
            actual.getAttributeCount());

        for (int i = 0; i < expected.getAttributeCount(); i++) {
            AttributeType expectedAttribute = expected.getAttributeType(i);
            AttributeType actualAttribute = actual.getAttributeType(i);
            assertEquals("attribute " + expectedAttribute.getName(),
                expectedAttribute, actualAttribute);
        }

        assertEquals(expected, actual);
    }

    public void testGetSchemaRiver() throws IOException {
        FeatureType expected = riverType;
        FeatureType actual = data.getSchema("river");
        assertEquals("namespace", expected.getNamespace(), actual.getNamespace());
        assertEquals("typeName", expected.getTypeName(), actual.getTypeName());

        //assertEquals( "compare", 0, DataUtilities.compare( expected, actual ));
        assertEquals("attributeCount", expected.getAttributeCount(),
            actual.getAttributeCount());

        for (int i = 0; i < expected.getAttributeCount(); i++) {
            AttributeType expectedAttribute = expected.getAttributeType(i);
            AttributeType actualAttribute = actual.getAttributeType(i);
            assertEquals("attribute " + expectedAttribute.getName(),
                expectedAttribute, actualAttribute);
        }

        assertEquals(expected, actual);
    }

    static public void assertEquals(String message, String expected,
        String actual) {
        if (expected == actual) {
            return;
        }

        assertNotNull(message, expected);
        assertNotNull(message, actual);

        if (!expected.equals(actual)) {
            fail(message + " expected:<" + expected + "> but was <" + actual
                + ">");
        }
    }

    void assertCovers(String msg, FeatureCollection c1, FeatureCollection c2) {
        if (c1 == c2) {
            return;
        }

        assertNotNull(msg, c1);
        assertNotNull(msg, c2);
        assertEquals(msg + " size", c1.size(), c2.size());

        Feature f;

        for (FeatureIterator i = c1.features(); i.hasNext();) {
            f = i.next();
            assertTrue(msg + " " + f.getID(), c2.contains(f));
        }
    }

    public FeatureReader reader(String typeName) throws IOException {
        FeatureType type = data.getSchema(typeName);

        return data.getFeatureReader(type, Filter.NONE, Transaction.AUTO_COMMIT);
    }

    public FeatureWriter writer(String typeName) throws IOException {
        return data.getFeatureWriter(typeName, Transaction.AUTO_COMMIT);
    }

    public void testGetFeatureReader()
        throws IOException, IllegalAttributeException {
        assertCovered(roadFeatures, reader("road"));
        assertEquals(3, count(reader("road")));
    }

    public void testGetFeatureReaderMutability()
        throws IOException, IllegalAttributeException {
        FeatureReader reader = reader("road");
        Feature feature;

        while (reader.hasNext()) {
            feature = (Feature) reader.next();
            feature.setAttribute("name", null);
        }

        reader.close();

        reader = reader("road");

        while (reader.hasNext()) {
            feature = (Feature) reader.next();
            assertNotNull(feature.getAttribute("name"));
        }

        reader.close();

        try {
            reader.next();
            fail("next should fail with an IOException");
        } catch (IOException expected) {
        }
    }

    public void testGetFeatureReaderConcurancy()
        throws NoSuchElementException, IOException, IllegalAttributeException {
        FeatureReader reader1 = reader("road");
        FeatureReader reader2 = reader("road");
        FeatureReader reader3 = reader("river");

        Feature feature1;
        Feature feature2;
        Feature feature3;

        while (reader1.hasNext() || reader2.hasNext() || reader3.hasNext()) {
            assertTrue(contains(roadFeatures, reader1.next()));
            assertTrue(contains(roadFeatures, reader2.next()));

            if (reader3.hasNext()) {
                assertTrue(contains(riverFeatures, reader3.next()));
            }
        }

        try {
            reader1.next();
            fail("next should fail with an NoSuchElementException");
        } catch (NoSuchElementException expectedNoElement) {
            // this is new to me, I had expected an IOException
        }

        try {
            reader2.next();
            fail("next should fail with an NoSuchElementException");
        } catch (NoSuchElementException expectedNoElement) {
        }

        try {
            reader3.next();
            fail("next should fail with an NoSuchElementException");
        } catch (NoSuchElementException expectedNoElement) {
        }

        reader1.close();
        reader2.close();
        reader3.close();

        try {
            reader1.next();
            fail("next should fail with an IOException");
        } catch (IOException expectedClosed) {
        }

        try {
            reader2.next();
            fail("next should fail with an IOException");
        } catch (IOException expectedClosed) {
        }

        try {
            reader3.next();
            fail("next should fail with an IOException");
        } catch (IOException expectedClosed) {
        }
    }

    public void testGetFeatureReaderFilterAutoCommit()
        throws NoSuchElementException, IOException, IllegalAttributeException {
        FeatureType type = data.getSchema("road");
        FeatureReader reader;

        reader = data.getFeatureReader(type, Filter.NONE,
                Transaction.AUTO_COMMIT);
        assertFalse(reader instanceof FilteringFeatureReader);
        assertEquals(type, reader.getFeatureType());
        assertEquals(roadFeatures.length, count(reader));

        reader = data.getFeatureReader(type, Filter.ALL, Transaction.AUTO_COMMIT);
        assertTrue(reader instanceof EmptyFeatureReader);

        assertEquals(type, reader.getFeatureType());
        assertEquals(0, count(reader));

        reader = data.getFeatureReader(type, rd1Filter, Transaction.AUTO_COMMIT);
        //assertTrue(reader instanceof FilteringFeatureReader);
        assertEquals(type, reader.getFeatureType());
        assertEquals(1, count(reader));
    }

    public void testGetFeatureReaderFilterTransaction()
        throws NoSuchElementException, IOException, IllegalAttributeException {
        Transaction t = new DefaultTransaction();
        FeatureType type = data.getSchema("road");
        FeatureReader reader;

        reader = data.getFeatureReader(type, Filter.ALL, t);
        assertTrue(reader instanceof EmptyFeatureReader);
        assertEquals(type, reader.getFeatureType());
        assertEquals(0, count(reader));

        reader = data.getFeatureReader(type, Filter.NONE, t);
        assertEquals(type, reader.getFeatureType());
        assertEquals(roadFeatures.length, count(reader));

        reader = data.getFeatureReader(type, rd1Filter, t);
        assertEquals(type, reader.getFeatureType());
        assertEquals(1, count(reader));

        FeatureWriter writer = data.getFeatureWriter("road", Filter.NONE, t);
        Feature feature;

        while (writer.hasNext()) {
            feature = writer.next();

            if (feature.getID().equals(roadFeatures[0].getID())) {
                writer.remove();
            }
        }

        reader = data.getFeatureReader(type, Filter.ALL, t);
        assertEquals(0, count(reader));

        reader = data.getFeatureReader(type, Filter.NONE, t);
        assertEquals(roadFeatures.length - 1, count(reader));

        reader = data.getFeatureReader(type, rd1Filter, t);
        assertEquals(0, count(reader));

        t.rollback();
        reader = data.getFeatureReader(type, Filter.ALL, t);
        assertEquals(0, count(reader));

        reader = data.getFeatureReader(type, Filter.NONE, t);
        assertEquals(roadFeatures.length, count(reader));

        reader = data.getFeatureReader(type, rd1Filter, t);
        assertEquals(1, count(reader));
    }

    /**
     * Ensure readers contents equal those in the feature array
     *
     * @param features DOCUMENT ME!
     * @param reader DOCUMENT ME!
     *
     * @throws NoSuchElementException DOCUMENT ME!
     * @throws IOException DOCUMENT ME!
     * @throws IllegalAttributeException DOCUMENT ME!
     */
    void assertCovered(Feature[] features, FeatureReader reader)
        throws NoSuchElementException, IOException, IllegalAttributeException {
        int count = 0;

        try {
            while (reader.hasNext()) {
                assertContains(features, reader.next());
                count++;
            }
        } finally {
            reader.close();
        }

        assertEquals(features.length, count);
    }

    /**
     * Ensure readers contents match those in the feature array
     * 
     * <p>
     * Implemented using match on attribute types, not feature id
     * </p>
     *
     * @param array DOCUMENT ME!
     * @param reader DOCUMENT ME!
     *
     * @throws Exception DOCUMENT ME!
     */
    void assertMatched(Feature[] array, FeatureReader reader)
        throws Exception {
        Feature feature;
        int count = 0;

        try {
            while (reader.hasNext()) {
                feature = reader.next();
                assertMatch( array, feature );
                count++;
            }
        } finally {
            reader.close();
        }

        assertEquals("array not matched by reader", array.length, count);
    }
    void assertMatch( Feature[] array, Feature feature ){
        assertTrue( array != null );
        assertTrue( array.length != 0 );            
       
        FeatureType schema = feature.getFeatureType();
                
        for (int i = 0; i < array.length; i++) {
            if (match(array[i], feature)) {
                return;
            }
        }
        System.out.println( "not found:" + feature );
        for (int i = 0; i < array.length; i++) {
            System.out.println( i+":"+array[i] );
        }        
        fail( "array has no match for "+feature );          
    }
    /**
     * Ensure that FeatureReader reader contains extactly the contents of
     * array.
     *
     * @param reader DOCUMENT ME!
     * @param array DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     *
     * @throws NoSuchElementException DOCUMENT ME!
     * @throws IOException DOCUMENT ME!
     * @throws IllegalAttributeException DOCUMENT ME!
     */
    boolean covers(FeatureReader reader, Feature[] array)
        throws NoSuchElementException, IOException, IllegalAttributeException {
        Feature feature;
        int count = 0;

        try {
            while (reader.hasNext()) {
                feature = reader.next();

                if (!contains(array, feature)) {
                    return false;
                }

                count++;
            }
        } finally {
            reader.close();
        }

        return count == array.length;
    }

    boolean coversLax(FeatureReader reader, Feature[] array)
        throws NoSuchElementException, IOException, IllegalAttributeException {
        Feature feature;
        int count = 0;

        try {
            while (reader.hasNext()) {
                feature = reader.next();

                if (!containsLax(array, feature)) {
                    return false;
                }

                count++;
            }
        } finally {
            reader.close();
        }

        return count == array.length;
    }

    void dump(String message, FeatureReader reader)
        throws NoSuchElementException, IOException, IllegalAttributeException {
        Feature feature;
        int count = 0;

        try {
            while (reader.hasNext()) {
                feature = reader.next();
                String msg = message+": feture "+count + "=" + feature;
                //LOGGER.info( msg );
                System.out.println( msg );
                count++;
            }
        } finally {
            reader.close();
        }
    }

    void dump(String message, Object[] array) {
        for (int i = 0; i < array.length; i++) {
            String msg = message+": "+i + "=" + array[i];
            //LOGGER.info( msg );
            System.out.println( msg );            
        }
    }

    /*
     * Test for FeatureWriter getFeatureWriter(String, Filter, Transaction)
     */
    public void testGetFeatureWriter() throws Exception {
        FeatureWriter writer = data.getFeatureWriter("road", Filter.NONE,
                Transaction.AUTO_COMMIT);
        assertEquals(roadFeatures.length, count(writer));
    }

    public void testGetFeatureWriterClose() throws Exception {
        FeatureWriter writer = data.getFeatureWriter("road", Filter.NONE,
                Transaction.AUTO_COMMIT);
                
        writer.close();        
        try {
            assertFalse(writer.hasNext());
            fail("Should not be able to use a closed writer");
        } catch (IOException expected) {
        }

        try {
            assertNull(writer.next());
            fail("Should not be able to use a closed writer");
        } catch (IOException expected) {
        }

        try {
            writer.close();
            fail("Should be able to close a closed writer?");
        } catch (IOException expected) {
        }
    }

    public void testGetFeatureWriterRemove()
        throws IOException, IllegalAttributeException {
        FeatureWriter writer = writer("road");
        Feature feature;

        while (writer.hasNext()) {
            feature = writer.next();

            if (feature.getID().equals(roadFeatures[0].getID())) {
                writer.remove();
            }
        }

        assertEquals(roadFeatures.length - 1, count("road"));
    }

    public int count(String typeName) throws IOException {
        return count(reader(typeName));
    }

    public void testGetFeaturesWriterAdd()
        throws IOException, IllegalAttributeException {
        FeatureWriter writer =
            data.getFeatureWriter("road", Transaction.AUTO_COMMIT);
        Feature feature;

        while (writer.hasNext()) {
            feature = writer.next();
        }
        assertFalse(writer.hasNext());
        
        feature = writer.next();
        feature.setAttributes(newRoad.getAttributes(null));
        writer.write();
        
        assertFalse(writer.hasNext());
        assertEquals(roadFeatures.length + 1, count("road"));
    }
    /**
     * Seach for feature based on AttributeType.
     * <p>
     * If attributeName is null, we will search by feature.getID()
     * </p>
     * <p>
     * The provided reader will be closed by this opperations.
     * </p>
     * 
     * @param reader reader to search through
     * @param attributeName attributeName, or null for featureID
     * @param value value to match
     * @return Feature
     * @throws IOException We could not use reader
     * @throws NoSuchElementException if a match could not be found
     * @throws IllegalAttributeException if attributeName did not match schema
     */
    public Feature findFeature( FeatureReader reader, String attributeName, Object value )
        throws NoSuchElementException, IOException, IllegalAttributeException
    {
        Feature f;
        try {
            while (reader.hasNext()) {
                f = reader.next();
                
                if( attributeName == null ){
                    if (value.equals( f.getID()) ) {
                        return f;
                    }
                }
                else {
                    if( value.equals( f.getAttribute( attributeName ))){
                        return f;
                    }
                }
            }
        } finally {
            reader.close();
        }
        if( attributeName == null ){
            throw new NoSuchElementException("No match for FID="+value );
        }
        else {
            throw new NoSuchElementException("No match for "+attributeName+"="+value );
        }
    }
    public Feature feature(String typeName, String fid)
        throws NoSuchElementException, IOException, IllegalAttributeException {
        FeatureReader reader = reader(typeName);
        Feature f;

        try {
            while (reader.hasNext()) {
                f = reader.next();

                if (fid.equals(f.getID())) {
                    return f;
                }
            }
        } finally {
            reader.close();
        }

        return null;
    }

    public void testGetFeaturesWriterModify()
        throws IOException, IllegalAttributeException {
        FeatureWriter writer = writer("road");
        Feature feature;

        while (writer.hasNext()) {
            feature = writer.next();

            if (feature.getID().equals(roadFeatures[0].getID())) {
                feature.setAttribute("name", "changed");
                writer.write();
            }
        }

        feature = (Feature) feature("road", roadFeatures[0].getID());
        assertNotNull(feature);
        assertEquals("changed", feature.getAttribute("name"));
    }

    public void testGetFeatureWriterTypeNameTransaction()
        throws NoSuchElementException, IOException, IllegalAttributeException {
        FeatureWriter writer;

        writer = data.getFeatureWriter("road", Transaction.AUTO_COMMIT);
        assertEquals(roadFeatures.length, count(writer));
        writer.close();
    }

    public void testGetFeatureWriterAppendTypeNameTransaction()
        throws Exception {
        FeatureWriter writer;

        writer = data.getFeatureWriterAppend("road", Transaction.AUTO_COMMIT);
        assertEquals(0, count(writer));
        writer.close();
    }

    /*
     * Test for FeatureWriter getFeatureWriter(String, boolean, Transaction)
     */
    public void testGetFeatureWriterFilter()
        throws NoSuchElementException, IOException, IllegalAttributeException {
        FeatureWriter writer;

        writer = data.getFeatureWriter("road", Filter.ALL,
                Transaction.AUTO_COMMIT);
        assertTrue(writer instanceof EmptyFeatureWriter);
        assertEquals(0, count(writer));

        writer = data.getFeatureWriter("road", Filter.NONE,
                Transaction.AUTO_COMMIT);
        assertFalse(writer instanceof FilteringFeatureWriter);
        assertEquals(roadFeatures.length, count(writer));

        writer = data.getFeatureWriter("road", rd1Filter,
                Transaction.AUTO_COMMIT);
        assertTrue(writer instanceof FilteringFeatureWriter);
        assertEquals(1, count(writer));
    }

    /**
     * Test two transactions one removing feature, and one adding a feature.
     *
     * @throws Exception DOCUMENT ME!
     */
    public void testGetFeatureWriterTransaction() throws Exception {
        Transaction t1 = new DefaultTransaction();
        Transaction t2 = new DefaultTransaction();
        FeatureWriter writer1 = data.getFeatureWriter("road", rd1Filter, t1);
        FeatureWriter writer2 = data.getFeatureWriterAppend("road", t2);

        FeatureType road = data.getSchema("road");
        FeatureReader reader;
        Feature feature;
        Feature[] ORIGIONAL = roadFeatures;
        Feature[] REMOVE = new Feature[ORIGIONAL.length - 1];
        Feature[] ADD = new Feature[ORIGIONAL.length + 1];
        Feature[] FINAL = new Feature[ORIGIONAL.length];
        int i;
        int index;
        index = 0;

        for (i = 0; i < ORIGIONAL.length; i++) {
            feature = ORIGIONAL[i];

            if (!feature.getID().equals(roadFeatures[0].getID())) {
                REMOVE[index++] = feature;
            }
        }

        for (i = 0; i < ORIGIONAL.length; i++) {
            ADD[i] = ORIGIONAL[i];
        }

        ADD[i] = newRoad; // will need to update with Fid from database

        for (i = 0; i < REMOVE.length; i++) {
            FINAL[i] = REMOVE[i];
        }
        FINAL[i] = newRoad; // will need to update with Fid from database

        // start of with ORIGINAL                        
        reader = data.getFeatureReader(road, Filter.NONE,
                Transaction.AUTO_COMMIT);
        assertTrue(covers(reader, ORIGIONAL));

        // writer 1 removes road.rd1 on t1
        // -------------------------------
        // - tests transaction independence from DataStore
        while (writer1.hasNext()) {
            feature = writer1.next();
            assertEquals(roadFeatures[0].getID(), feature.getID());
            writer1.remove();
        }

        // still have ORIGIONAL and t1 has REMOVE
        reader = data.getFeatureReader(road, Filter.NONE, Transaction.AUTO_COMMIT);
        assertTrue(covers(reader, ORIGIONAL));
                
        reader = data.getFeatureReader(road, Filter.NONE, t1);
        assertTrue(covers(reader, REMOVE));
        
        // close writer1
        // --------------
        // ensure that modification is left up to transaction commmit
        writer1.close();

        // We still have ORIGIONAL and t1 has REMOVE
        reader = data.getFeatureReader(road, Filter.NONE,
                Transaction.AUTO_COMMIT);
        assertTrue(covers(reader, ORIGIONAL));
        
        reader = data.getFeatureReader(road, Filter.NONE, t1);
        assertTrue(covers(reader, REMOVE));

        // writer 2 adds road.rd4 on t2
        // ----------------------------
        // - tests transaction independence from each other
        feature = writer2.next();
        feature.setAttributes(newRoad.getAttributes(null));
        writer2.write();
        
        // HACK: ?!? update ADD and FINAL with new FID from database
        //
        reader = data.getFeatureReader( road, Filter.NONE, t2 );
        newRoad = findFeature( reader, "id", new Integer(4) );
        System.out.println("newRoad:"+newRoad );
        ADD[ADD.length-1] = newRoad;
        FINAL[FINAL.length-1] = newRoad;
        
        // We still have ORIGIONAL and t2 has ADD
        reader = data.getFeatureReader(road, Filter.NONE,
                Transaction.AUTO_COMMIT);
        assertTrue(covers(reader, ORIGIONAL));
        
        reader = data.getFeatureReader(road, Filter.NONE, t2);
        assertMatched(ADD, reader); // broken due to FID problem
        
        // close writer2
        // -------------
        // ensure that modification is left up to transaction commmit
        writer2.close();

        // Still have ORIGIONAL and t2 has ADD
        reader = data.getFeatureReader(road, Filter.NONE,
                Transaction.AUTO_COMMIT);
        assertTrue(covers(reader, ORIGIONAL));
        reader = data.getFeatureReader(road, Filter.NONE, t2);
        assertTrue(coversLax(reader, ADD));
        

        // commit t1
        // ---------
        // -ensure that delayed writing of transactions takes place
        //
        t1.commit();

        // We now have REMOVE, as does t1 (which has not additional diffs)
        // t2 will have FINAL
        reader = data.getFeatureReader(road, Filter.NONE,
                Transaction.AUTO_COMMIT);
        assertTrue(covers(reader, REMOVE));
        reader = data.getFeatureReader(road, Filter.NONE, t1);
        assertTrue(covers(reader, REMOVE));
        reader = data.getFeatureReader(road, Filter.NONE, t2);
        assertTrue(coversLax(reader, FINAL));                

        // commit t2
        // ---------
        // -ensure that everyone is FINAL at the end of the day
        t2.commit();

        // We now have Number( remove one and add one)
        reader = data.getFeatureReader(road, Filter.NONE,
                Transaction.AUTO_COMMIT);
        reader = data.getFeatureReader(road, Filter.NONE,
                Transaction.AUTO_COMMIT);
        assertTrue(coversLax(reader, FINAL));
                
        reader = data.getFeatureReader(road, Filter.NONE, t1);
        assertTrue(coversLax(reader, FINAL));
                
        reader = data.getFeatureReader(road, Filter.NONE, t2);
        assertTrue(coversLax(reader, FINAL));                
    }

    // Feature Source Testing
    public void testGetFeatureSourceRoad() throws IOException {
        FeatureSource road = data.getFeatureSource("road");

        assertEquals(roadType, road.getSchema());        
        assertSame(data, road.getDataStore());
        int count = road.getCount(Query.ALL);        
        assertTrue( count == 3 || count == -1);
        Envelope bounds = road.getBounds(Query.ALL);
        assertTrue( bounds == null || bounds.equals( roadBounds ) );

        FeatureResults all = road.getFeatures();
        assertEquals(3, all.getCount());
        assertEquals(roadBounds, all.getBounds());

        FeatureCollection expected = DataUtilities.collection(roadFeatures);

        assertCovers("all", expected, all.collection());
        assertEquals(roadBounds, all.collection().getBounds());

        FeatureResults some = road.getFeatures(rd12Filter);
        assertEquals(2, some.getCount());
        Envelope e = new Envelope();
        e.expandToInclude( roadFeatures[0].getBounds());
        e.expandToInclude( roadFeatures[1].getBounds());        
        assertEquals( e, some.getBounds() );
        assertEquals(some.getSchema(), road.getSchema());

        DefaultQuery query = new DefaultQuery(rd12Filter,
                new String[] { "name", });

        FeatureResults half = road.getFeatures(query);
        assertEquals(2, half.getCount());
        assertEquals(1, half.getSchema().getAttributeCount());

        FeatureReader reader = half.reader();
        FeatureType type = reader.getFeatureType();
        reader.close();

        FeatureType actual = half.getSchema();

        assertEquals(type.getTypeName(), actual.getTypeName());
        assertEquals(type.getNamespace(), actual.getNamespace());
        assertEquals(type.getAttributeCount(), actual.getAttributeCount());

        for (int i = 0; i < type.getAttributeCount(); i++) {
            assertEquals(type.getAttributeType(i), actual.getAttributeType(i));
        }

        assertNull(type.getDefaultGeometry());
        assertEquals(type.getDefaultGeometry(), actual.getDefaultGeometry());
        assertEquals(type, actual);

        try {
            assertNull(half.getBounds());
            fail("half does not specify a default geometry");
        } catch (IOException io) {
        }
    }

    public void testGetFeatureSourceRiver()
        throws NoSuchElementException, IOException, IllegalAttributeException {
        FeatureSource river = data.getFeatureSource("river");

        assertEquals(riverType, river.getSchema());
        assertSame(data, river.getDataStore());

        FeatureResults all = river.getFeatures();
        assertEquals(2, all.getCount());
        assertEquals(riverBounds, all.getBounds());
        assertTrue("rivers", covers(all.reader(), riverFeatures));

        FeatureCollection expected = DataUtilities.collection(riverFeatures);
        assertCovers("all", expected, all.collection());
        assertEquals(riverBounds, all.collection().getBounds());
    }

    //
    // Feature Store Testing
    //
    public void testGetFeatureStoreModifyFeatures1() throws IOException {
        FeatureStore road = (FeatureStore) data.getFeatureSource("road");
        
        FilterFactory factory = FilterFactory.createFilterFactory();
        rd1Filter = factory.createFidFilter( roadFeatures[0].getID() );
        
        AttributeType name = roadType.getAttributeType("name");
        road.modifyFeatures(name, "changed", rd1Filter);

        FeatureCollection results = road.getFeatures(rd1Filter).collection();                
        assertEquals("changed", results.features().next().getAttribute("name"));
    }

    public void testGetFeatureStoreModifyFeatures2() throws IOException {
        FeatureStore road = (FeatureStore) data.getFeatureSource("road");
        
        FilterFactory factory = FilterFactory.createFilterFactory();
        rd1Filter = factory.createFidFilter( roadFeatures[0].getID() );
                
        AttributeType name = roadType.getAttributeType("name");
        road.modifyFeatures(new AttributeType[] { name, },
            new Object[] { "changed", }, rd1Filter);

        FeatureCollection results = road.getFeatures(rd1Filter).collection();
        assertEquals("changed", results.features().next().getAttribute("name"));
    }

    public void testGetFeatureStoreRemoveFeatures() throws IOException {
        FeatureStore road = (FeatureStore) data.getFeatureSource("road");

        road.removeFeatures(rd1Filter);
        assertEquals(0, road.getFeatures(rd1Filter).getCount());
        assertEquals(roadFeatures.length - 1, road.getFeatures().getCount());
    }

    public void testGetFeatureStoreAddFeatures() throws IOException {
        FeatureReader reader = DataUtilities.reader(new Feature[] { newRoad, });
        FeatureStore road = (FeatureStore) data.getFeatureSource("road");

        road.addFeatures(reader);
        assertEquals(roadFeatures.length + 1, road.getFeatures().getCount());
    }

    public void testGetFeatureStoreSetFeatures() throws IOException {
        FeatureReader reader = DataUtilities.reader(new Feature[] { newRoad, });
        FeatureStore road = (FeatureStore) data.getFeatureSource("road");

        road.setFeatures(reader);
        assertEquals(1, road.getFeatures().getCount());
    }

    public void testGetFeatureStoreTransactionSupport()
        throws Exception {
        Transaction t1 = new DefaultTransaction();
        Transaction t2 = new DefaultTransaction();

        FeatureStore road = (FeatureStore) data.getFeatureSource("road");
        FeatureStore road1 = (FeatureStore) data.getFeatureSource("road");
        FeatureStore road2 = (FeatureStore) data.getFeatureSource("road");

        road1.setTransaction(t1);
        road2.setTransaction(t2);

        Feature feature;
        Feature[] ORIGIONAL = roadFeatures;
        Feature[] REMOVE = new Feature[ORIGIONAL.length - 1];
        Feature[] ADD = new Feature[ORIGIONAL.length + 1];
        Feature[] FINAL = new Feature[ORIGIONAL.length];
        int i;
        int index;
        index = 0;

        for (i = 0; i < ORIGIONAL.length; i++) {
            feature = ORIGIONAL[i];

            if (!feature.getID().equals("road.rd1")) {
                REMOVE[index++] = feature;
            }
        }

        for (i = 0; i < ORIGIONAL.length; i++) {
            ADD[i] = ORIGIONAL[i];
        }

        ADD[i] = newRoad;

        for (i = 0; i < REMOVE.length; i++) {
            FINAL[i] = REMOVE[i];
        }

        FINAL[i] = newRoad;

        // start of with ORIGINAL
        assertTrue(covers(road.getFeatures().reader(), ORIGIONAL));

        // road1 removes road.rd1 on t1
        // -------------------------------
        // - tests transaction independence from DataStore
        road1.removeFeatures(rd1Filter);

        // still have ORIGIONAL and t1 has REMOVE
        assertTrue(covers(road.getFeatures().reader(), ORIGIONAL));
        assertTrue(covers(road1.getFeatures().reader(), REMOVE));

        // road2 adds road.rd4 on t2
        // ----------------------------
        // - tests transaction independence from each other
        FeatureReader reader = DataUtilities.reader(new Feature[] { newRoad, });
        road2.addFeatures(reader);

        // We still have ORIGIONAL, t1 has REMOVE, and t2 has ADD
        assertTrue(covers(road.getFeatures().reader(), ORIGIONAL));
        assertTrue(covers(road1.getFeatures().reader(), REMOVE));
        assertTrue(coversLax(road2.getFeatures().reader(), ADD));

        // commit t1
        // ---------
        // -ensure that delayed writing of transactions takes place
        //
        t1.commit();

        // We now have REMOVE, as does t1 (which has not additional diffs)
        // t2 will have FINAL
        assertTrue(covers(road.getFeatures().reader(), REMOVE));
        assertTrue(covers(road1.getFeatures().reader(), REMOVE));
        assertTrue(coversLax(road2.getFeatures().reader(), FINAL));

        // commit t2
        // ---------
        // -ensure that everyone is FINAL at the end of the day
        t2.commit();

        // We now have Number( remove one and add one)
        assertTrue(coversLax(road.getFeatures().reader(), FINAL));
        assertTrue(coversLax(road1.getFeatures().reader(), FINAL));
        assertTrue(coversLax(road2.getFeatures().reader(), FINAL));
    }

    boolean isLocked(String typeName, String fid) {
        InProcessLockingManager lockingManager = (InProcessLockingManager) data
            .getLockingManager();

        return lockingManager.isLocked(typeName, fid);
    }

    //
    // FeatureLocking Testing    
    //

    /*
     * Test for void lockFeatures()
     */
    public void testLockFeatures() throws IOException {
        FeatureLock lock = FeatureLockFactory.generate("test", 3600);
        FeatureLocking road = (FeatureLocking) data.getFeatureSource("road");
        road.setFeatureLock(lock);

        assertFalse(isLocked("road", "road.rd1"));
        road.lockFeatures();
        assertTrue(isLocked("road", "road.rd1"));
    }

    public void testUnLockFeatures() throws IOException {
        FeatureLock lock = FeatureLockFactory.generate("test", 3600);
        FeatureLocking road = (FeatureLocking) data.getFeatureSource("road");
        road.setFeatureLock(lock);
        road.lockFeatures();

        try {
            road.unLockFeatures();
            fail("unlock should fail due on AUTO_COMMIT");
        } catch (IOException expected) {
        }

        Transaction t = new DefaultTransaction();
        road.setTransaction(t);

        try {
            road.unLockFeatures();
            fail("unlock should fail due lack of authorization");
        } catch (IOException expected) {
        }

        t.addAuthorization(lock.getAuthorization());
        road.unLockFeatures();
    }

    public void testLockFeatureInteraction() throws IOException {
        FeatureLock lockA = FeatureLockFactory.generate("LockA", 3600);
        FeatureLock lockB = FeatureLockFactory.generate("LockB", 3600);
        Transaction t1 = new DefaultTransaction();
        Transaction t2 = new DefaultTransaction();
        FeatureLocking road1 = (FeatureLocking) data.getFeatureSource("road");
        FeatureLocking road2 = (FeatureLocking) data.getFeatureSource("road");
        road1.setTransaction(t1);
        road2.setTransaction(t2);
        road1.setFeatureLock(lockA);
        road2.setFeatureLock(lockB);

        assertFalse(isLocked("road", "road.rd1"));
        assertFalse(isLocked("road", "road.rd2"));
        assertFalse(isLocked("road", "road.rd3"));

        road1.lockFeatures(rd1Filter);
        assertTrue(isLocked("road", "road.rd1"));
        assertFalse(isLocked("road", "road.rd2"));
        assertFalse(isLocked("road", "road.rd3"));

        road2.lockFeatures(rd2Filter);
        assertTrue(isLocked("road", "road.rd1"));
        assertTrue(isLocked("road", "road.rd2"));
        assertFalse(isLocked("road", "road.rd3"));

        try {
            road1.unLockFeatures(rd1Filter);
            fail("need authorization");
        } catch (IOException expected) {
        }

        t1.addAuthorization(lockA.getAuthorization());

        try {
            road1.unLockFeatures(rd2Filter);
            fail("need correct authorization");
        } catch (IOException expected) {
        }

        road1.unLockFeatures(rd1Filter);
        assertFalse(isLocked("road", "road.rd1"));
        assertTrue(isLocked("road", "road.rd2"));
        assertFalse(isLocked("road", "road.rd3"));

        t2.addAuthorization(lockB.getAuthorization());
        road2.unLockFeatures(rd2Filter);
        assertFalse(isLocked("road", "road.rd1"));
        assertFalse(isLocked("road", "road.rd2"));
        assertFalse(isLocked("road", "road.rd3"));
    }

    public void testGetFeatureLockingExpire() throws Exception {
        FeatureLock lock = FeatureLockFactory.generate("Timed", 1);
        
        FeatureLocking road = (FeatureLocking) data.getFeatureSource("road");
        road.setFeatureLock(lock);
        assertFalse(isLocked("road", "road.rd1"));
                
        road.lockFeatures(rd1Filter);
        assertTrue(isLocked("road", "road.rd1"));
        Thread.sleep(100);
        assertFalse(isLocked("road", "road.rd1"));
    }
}
