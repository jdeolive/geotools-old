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
package org.geotools.data;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import junit.framework.TestCase;
import org.geotools.feature.AttributeType;
import org.geotools.feature.Feature;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureCollections;
import org.geotools.feature.FeatureIterator;
import org.geotools.feature.FeatureType;
import org.geotools.feature.IllegalAttributeException;
import org.geotools.filter.FidFilter;
import org.geotools.filter.Filter;
import org.geotools.filter.FilterFactory;
import java.util.NoSuchElementException;

/**
 * LockingDataSource test cases.
 * <p>
 * This TestCase is designed to be subclassed to
 * ensure conformace of LockingDataSource implementations.</p>
 * <p>
 * To ensure conformance:</p>
 * <ul>
 * <li>override <code>createDataSource( FeatureCollection )</code></li>
 * <li>override <code>isLocked( String )</code>:<br>
 *     True when provided with FID of locked feature.</li>
 * </ul>
 * To improve execution speed:</p>
 * <ul>
 * <li>override <code>feature( String )</code>:<br>
 *     Feature matching provided FID.</li>
 * <li>override <code>setUp()</code>:<br>
 *     To store generated DataSource <code>ds</code> in a field for
 *     testing.</li>
 * </ul>
 * <p>Example:</p>
 * <code><pre>
 * // optional setUp override
 * <b>public void</b> setUp() throws Exception{
 *     super.setUp(); // sets up ds with thre results of createDataSource
 *     mds = (MemoryLockingDataSource) ds;
 * }
 * // required createDataSource overide
 * <b>public static/<b> DataSource createDataSource( FeatureCollection contents ){
 *     LockingDataSource dataSource = new MemoryLockingDataSource();
 *     dataSource.addFeatures( features );
 *     <b>return</b> dataSource;
 * }
 * // required isLocked(String)
 * <b>protected boolean</b> isLocked( String fid ){
 *     <b>return</b> mds.isLocked( fid );
 * }
 * //example Test using <b>mds</b>
 * <b>public void</b> testDataSourceContents(){
 *     assertNotNull( mds.features );
 * }
 * </pre></code>
 * <p>
 * For an example use of this TestCase please see MemoryLockingDataSourceTest.</p>
 *
 * @see org.geotools.data
 * @author jgarnett, Refractions Reasearch Inc.
 * @version CVS Version
 */
public class LockingDataSourceTestCase extends TestCase {
    static protected double TOLERENCE = 0.0000001;

    /** LockingDataSource being tested. */
    protected LockingDataSource ds;

    /** Feature Collection loaded into DataSource */
    protected FeatureCollection features;

    /** Feature Test Feature IDS */
    protected String fid1;

    /** Feature Test Feature IDS */
    protected String fid2;

    /** Feature Test Feature IDS */
    protected String fid3;

    /** Feature Test Feature IDS */
    protected String fid4;

    /** Features loaded into DataSource */
    protected Feature feature1;

    /** Features loaded into DataSource */
    protected Feature feature2;

    /** Features loaded into DataSource */
    protected Feature feature3;

    /** Features loaded into DataSource */
    protected Feature feature4;

    /** Sample Filter - filters fid1 & fid2 */
    protected Filter filter12;

    /** Sample Filter - filters fid3 & fid4 */
    protected Filter filter34;

    /**
     * Constructor for LockingDataSourceTestCase.
     *
     * @param arg0
     */
    public LockingDataSourceTestCase(String arg0) {
        super(arg0);
    }

    /**
     * Used by setUp to initialize and populate a new LockingDataSource.
     * Initial Implementaion makes uses an InMemoryDataSource
     *
     * @param features TestData required to populate DataSource
     *
     * @return LockingDataSource Initialized and Populated with features
     */

    // Uncomment to test functionality with MemoryLocking DataSource
    protected LockingDataSource createDataSource(FeatureCollection features)
        throws UnsupportedOperationException, DataSourceException {
        LockingDataSource dataSource = new MemoryLockingDataSource();

        dataSource.addFeatures(features);

        return dataSource;
    }

    /**
     * Sets up test fixture.
     * 
     * <p>
     * Subclasses should call super.setUp() which will use createDataSource()
     * to initialize and populate the LockingDataSource being tested.
     * </p>
     */
    protected void setUp() throws Exception {
        super.setUp();
        features = createTestFeatures();

        FeatureIterator i = features.features();

        feature1 = i.next();
        fid1 = feature1.getID();
        feature2 = i.next();
        fid2 = feature2.getID();
        feature3 = i.next();
        fid3 = feature3.getID();
        feature4 = i.next();
        fid4 = feature4.getID();

        filter12 = createFilter(new String[] { fid1, fid2 });
        filter34 = createFilter(new String[] { fid3, fid4 });

        ds = createDataSource(features);
    }

    /**
     * Creates required test data.  Hardcoded right now - should give up and
     * suck in a shapefile.
     *
     * @return A collection of 4 test features.
     */
    protected FeatureCollection createTestFeatures() throws Exception {
        LockingDataSourceFixture fixture = new LockingDataSourceFixture();

        FeatureCollection data = FeatureCollections.newCollection();
        data.add(fixture.createFeature("first", 2, 7));
        data.add(fixture.createFeature("second", 5, 5));
        data.add(fixture.createFeature("third", 6, 2));
        data.add(fixture.createFeature("forth", 8, 8));

        return data;
    }

    /**
     * Creates a filter based on input (null matches all).
     *
     * @param fids
     *
     * @return
     */
    protected Filter createFilter(String[] fids) {
        FidFilter filter = FilterFactory.createFilterFactory().createFidFilter();

        for (int i = 0; i < fids.length; i++) {
            filter.addFid(fids[i]);
        }

        return filter;
    }

    protected Filter createFilter(String fid) {
        return createFilter(new String[] { fid, });
    }

    /**
     * Closes DataSource and returns resources.
     * 
     * <p></p>
     *
     * @throws Exception
     *
     * @see junit.framework.TestCase#tearDown()
     */
    protected void tearDown() throws Exception {
        super.tearDown();
        ds = null;
    }

    public void testSetFeatureLock() {
        ds.setFeatureLock( FeatureLock.TRANSACTION );
        ds.setFeatureLock(FeatureLockFactory.generate("Test", 3600));
        ds.setFeatureLock( FeatureLock.TRANSACTION );
        ds.setFeatureLock(FeatureLockFactory.generate("Forever", 0));
        ds.setFeatureLock(FeatureLockFactory.generate("Invalid", -1));
    }

    //
    // Extensions to Assert
    //
    static public void assertEquals(String message, Geometry expected,
        Geometry actual) {
        assertTrue(message, expected.equalsExact(actual));
    }

    static public void assertEquals(Geometry expected, Geometry actual) {
        assertEquals("assert geometry", expected, actual);
    }

    /**
     * Test for void lockFeatures(Query)
     */
    public void testLockFeaturesQuery() throws DataSourceException {
        ds.setFeatureLock(FeatureLockFactory.generate("Test", 3600));

        FeatureCollection data = ds.getFeatures();
        Feature feature = data.features().next();
        assertFalse("unlocked before", isLocked(feature.getID()));

        ds.lockFeatures(Query.ALL);

        assertTrue("locked after", isLocked(feature.getID()));
    }

    /**
     * Test for void lockFeatures(Filter)
     */
    public void testLockFeaturesFilter() throws DataSourceException {
        ds.setFeatureLock(FeatureLockFactory.generate("Test", 3600));
        assertFalse("unlocked before", isLocked(fid1));

        ds.lockFeatures(Filter.NONE);
        assertTrue("locked after", isLocked(fid1));
    }

    /**
     * more complicated lockFeatures test
     */
    public void testUnLockFeaturesFilter2() throws DataSourceException {
        FeatureLock lockA = FeatureLockFactory.generate("LockA", 3600);
        FeatureLock lockB = FeatureLockFactory.generate("LockB", 3600);
        ds.setFeatureLock(lockA);
        ds.lockFeatures(filter12);
        assertTrue("A  locked1", isLocked(fid1));
        assertTrue("A  locked2", isLocked(fid2));
        assertFalse("A  locked3", isLocked(fid3));
        assertFalse("A  locked4", isLocked(fid4));
        ds.setFeatureLock(lockB);
        ds.lockFeatures(filter34);
        assertTrue("AB locked1", isLocked(fid1));
        assertTrue("AB locked2", isLocked(fid2));
        assertTrue("AB locked3", isLocked(fid3));
        assertTrue("AB locked4", isLocked(fid4));
    }

    /*
     * Test for void lockFeatures()
     */
    public void testLockFeatures() throws DataSourceException {
        ds.setFeatureLock(FeatureLockFactory.generate("Test", 3600));

        FeatureCollection data = ds.getFeatures();
        Feature feature = data.features().next();
        assertFalse("unlocked before", isLocked(feature.getID()));

        ds.lockFeatures();

        assertTrue("locked after", isLocked(feature.getID()));
    }

    public void testSetAuthorization() throws DataSourceException {
        FeatureLock lock = FeatureLockFactory.generate("Lock", 3600);
        ds.setAuthorization(lock.getAuthorization());
    }

    /*
     * Test for void unLockFeatures()
     */
    public void testUnLockFeatures() throws DataSourceException {
        FeatureLock lock = FeatureLockFactory.generate("Lock", 3600);
        ds.setFeatureLock(lock);
        ds.lockFeatures();

        try {
            ds.unLockFeatures();
            fail("unlock should fail without authorization");
        } catch (DataSourceException e) {
        }

        ds.setAuthorization(lock.getAuthorization());
        ds.unLockFeatures();
    }

    /*
     * Test for void unLockFeatures(Filter)
     */
    public void testUnLockFeaturesFilter() throws DataSourceException {
        FeatureLock lock = FeatureLockFactory.generate("Lock", 3600);
        ds.setFeatureLock(lock);
        ds.lockFeatures();
        ds.unLockFeatures(Filter.ALL);

        try {
            ds.unLockFeatures(Filter.NONE);
            fail("unlock should fail without authorization");
        } catch (DataSourceException e) {
        }

        ds.setAuthorization(lock.getAuthorization());
        ds.unLockFeatures(Filter.NONE);
    }

    public void testUnLockFeaturesFilterAB() throws DataSourceException {
        FeatureLock lockA = FeatureLockFactory.generate("LockA", 3600);
        FeatureLock lockB = FeatureLockFactory.generate("LockB", 3600);
        ds.setFeatureLock(lockA);
        ds.lockFeatures(filter12);
        ds.setFeatureLock(lockB);
        ds.lockFeatures(filter34);
        assertTrue("AB locked1", isLocked(fid1));
        assertTrue("AB locked2", isLocked(fid2));
        assertTrue("AB locked3", isLocked(fid3));
        assertTrue("AB locked4", isLocked(fid4));

        try {
            ds.unLockFeatures(filter12);
            fail("need lockA authorization");
        } catch (DataSourceException expected) {
        }

        ds.setAuthorization(lockA.getAuthorization());
        ds.unLockFeatures(filter12);
        assertFalse(" B locked1", isLocked(fid1));
        assertFalse(" B locked2", isLocked(fid2));
        assertTrue(" B locked3", isLocked(fid3));
        assertTrue(" B locked4", isLocked(fid4));
    }

    /*
     * Test for void unLockFeatures(Query)
     */
    public void testUnLockFeaturesQuery() throws DataSourceException {
        FeatureLock lock = FeatureLockFactory.generate("Lock", 3600);
        ds.setFeatureLock(lock);
        ds.lockFeatures();

        try {
            ds.unLockFeatures(Query.ALL);
            fail("unlock should fail without authorization");
        } catch (DataSourceException e) {
        }

        ds.setAuthorization(lock.getAuthorization());
        ds.unLockFeatures(Query.ALL);
    }

    /*
     * Test for void getFeatures(FeatureCollection, Query)
     */
    public void testGetFeaturesFeatureCollectionQuery()
        throws DataSourceException {
        FeatureCollection data = FeatureCollections.newCollection();

        ds.getFeatures(data, Query.ALL);
        assertEquals("ALL", 4, data.size());
    }

    /**
     * Test for void getFeatures(FeatureCollection, Filter)
     * 
     * @task TODO: The last assertion fails on my machine for some
     * reason - cholmes.  The behavior should be 4, I get 8.
     * 
     */
    public void testGetFeaturesFeatureCollectionFilter()
        throws DataSourceException {
        FeatureCollection data = FeatureCollections.newCollection();

        ds.getFeatures(data, Filter.ALL);
        assertEquals("ALL", 0, data.size());

        ds.getFeatures(data, Filter.NONE);
        assertEquals("NONEx1", 4, data.size());

        // ds.getFeatures(data, Filter.NONE);
        // assertEquals("NONEx2", 4, data.size());
    }

    /*
     * Test for FeatureCollection getFeatures(Query)
     */
    public void testGetFeaturesQuery() throws DataSourceException {
        Query query;
        FeatureCollection response;

        query = Query.ALL;
        response = ds.getFeatures(query);
        assertEquals("ALL", 4, response.size());
    }

    /*
     * Test for FeatureCollection getFeatures(Filter)
     */
    public void testGetFeaturesFilter() throws DataSourceException {
        Filter filter;
        FeatureCollection response;

        filter = Filter.ALL;
        response = ds.getFeatures(filter);
        assertEquals("ALL", 0, response.size());

        filter = Filter.NONE;
        response = ds.getFeatures(filter);
        assertEquals("NONE", 4, response.size());
    }

    /*
     * Test for FeatureCollection getFeatures()
     */
    public void testGetFeatures() throws DataSourceException {
        FeatureCollection all = ds.getFeatures();
        assertEquals(all.size(), 4);
    }

    public void testAddFeatures() throws Exception {
        LockingDataSourceFixture fixture = new LockingDataSourceFixture();

        FeatureCollection data = FeatureCollections.newCollection();
        data.add(fixture.createFeature("newOne", 1, 1));
        data.add(fixture.createFeature("newTen", 10, 10));

        ds.addFeatures(data);
        assertEquals("size", 6, numberFeatures());
    }

    public void testRemoveFeatures()
        throws UnsupportedOperationException, DataSourceException {
        ds.removeFeatures(filter12);
        assertEquals(2, ds.getFeatures().size());
        ds.removeFeatures(filter34);
        assertEquals(0, ds.getFeatures().size());
    }

    public void testRemoveFeaturesAuth()
        throws UnsupportedOperationException, DataSourceException {
        FeatureLock lock = FeatureLockFactory.generate("Lock", 3600);
        ds.setFeatureLock(lock);
        ds.lockFeatures(filter12);

        try {
            ds.removeFeatures(Filter.NONE);
            fail("requires authorization");
        } catch (DataSourceException e) {
            // expected result
        }
    }

    /**
     * Returns Feature from ds for testing.
     * 
     * <p>
     * Feature ID is used to reference defined feature.
     * </p>
     * 
     * <p>
     * This represents a generic solution based on iterating through the
     * results of getFeatures(). Subclasses may provide a more efficient
     * implementation.
     * </p>
     */
    protected Feature feature(String fid) {
        if (fid == null) {
            return null;
        }

        Feature feature;

        try {
            for (FeatureIterator i = ds.getFeatures().features(); i.hasNext();) {
                feature = i.next();

                if (fid.equals(feature.getID())) {
                    return feature;
                }
            }
        } catch (NoSuchElementException e) {
            return null;
        } catch (DataSourceException e) {
            return null;
        }

        return null;
    }

    /**
     * Returns lock status for testing.
     * 
     * <p>
     * This represents a generic solution based on iterating through the
     * results of getFeatures(). Subclasses may provide a more efficient
     * implementation.
     * </p>
     *
     */
    // Uncomment to test with MemoryLockingDataSource
    protected boolean isLocked(String fid) {
        // May be able to provide a "generic" solution for
        // this by trying to aquire a lock with a negative
        // duration.
        return ((MemoryLockingDataSource) ds).isLocked(fid);
    }

    /**
     * Returns the number of features in the DataSource for testing.
     * 
     * <p>
     * Returns -1 if something goes wrong
     * </p>
     *
     * @return Number of Features.
     */
    protected int numberFeatures() {
        try {
            return ds.getFeatures().size();
        } catch (DataSourceException e) {
            return -1;
        }
    }

    /**
     * Returns the number of locked features for testing.
     * <p>
     * Default implementation iterates through results of getFeatures() using
     * isLocked to test Feature IDs.</p>
     * <p>
     * Subclasses may be able to provide a more efficient implementation.</p>
     * <p>
     * MemoryLockingDataSource implementation:</p>
     * <code><pre>
     * <b>protected int </b>numberLocked(){
     *     <b>return</b> mds.getLocks().size();
     * }
     * </pre></code>
     * Returns -1 if something goes wrong</p>
     * @return Number of Locked features.
     */
    // Uncomment to test with MemoryLocking DataSource
    protected int numberLocked() {
        // May be able to provide a "generic" solution for
        // this by trying to aquire a lock with a negative
        // duration.
        int count = 0;
        Feature feature;

        try {
            for (FeatureIterator i = ds.getFeatures().features(); i.hasNext();) {
                feature = i.next();

                if (isLocked(feature.getID())) {
                    count++;
                }
            }
        } catch (DataSourceException e) {
            return -1;
        }

        return count;
    }

    /*
     * Test for void modifyFeatures(AttributeType[], Object[], Filter)
     */
    public void testModifyFeaturesAttributeTypeArrayObjectArrayFilter()
        throws IllegalAttributeException, UnsupportedOperationException, 
            DataSourceException {
        Feature feature = features.features().next();

        FeatureType type = feature.getFeatureType();
        AttributeType name = type.getAttributeType("name");
        AttributeType geom = type.getAttributeType("geometry");

        Geometry g1 = feature(fid1).getDefaultGeometry();
        Geometry g3 = feature(fid3).getDefaultGeometry();
        assertNotNull(g1);
        assertNotSame(g1, g3);

        assertEquals("first", feature(fid1).getAttribute("name"));

        // JTS does not support equals
        assertTrue(g1.equals(feature(fid1).getDefaultGeometry()));
        assertEquals("second", feature(fid2).getAttribute("name"));
        assertEquals("third", feature(fid3).getAttribute("name"));

        // JTS does not support equals
        assertTrue(g3.equals(feature(fid3).getDefaultGeometry()));
        assertEquals("forth", feature(fid4).getAttribute("name"));
        ds.modifyFeatures(new AttributeType[] { name, geom },
            new Object[] { "CHANGED", g3 }, filter12);
        assertEquals("CHANGED", feature(fid1).getAttribute("name"));
        assertEquals("CHANGED", feature(fid2).getAttribute("name"));
        assertEquals("third", feature(fid3).getAttribute("name"));
        assertEquals("forth", feature(fid4).getAttribute("name"));
        assertEquals("feature1 geom", g3,
            (Geometry) feature(fid1).getAttribute("geometry"));
        assertEquals("feature2 geom", g3,
            (Geometry) feature(fid2).getAttribute("geometry"));
        assertEquals("feature3 geom", g3,
            (Geometry) feature(fid3).getAttribute("geometry"));
    }

    /*
     * Test for void modifyFeatures(AttributeType, Object, Filter)
     */
    public void testModifyFeaturesAttributeTypeObjectFilter()
        throws UnsupportedOperationException, DataSourceException {
        Feature feature = features.features().next();

        FeatureType type = feature.getFeatureType();
        AttributeType name = type.getAttributeType("name");

        assertEquals("first", feature(fid1).getAttribute("name"));
        assertEquals("second", feature(fid2).getAttribute("name"));
        assertEquals("third", feature(fid3).getAttribute("name"));
        assertEquals("forth", feature(fid4).getAttribute("name"));
        ds.modifyFeatures(name, "CHANGED", filter12);
        assertEquals("CHANGED", feature(fid1).getAttribute("name"));
        assertEquals("CHANGED", feature(fid2).getAttribute("name"));
        assertEquals("third", feature(fid3).getAttribute("name"));
        assertEquals("forth", feature(fid4).getAttribute("name"));
    }

    public void testSetFeatures() throws Exception {
        LockingDataSourceFixture fixture = new LockingDataSourceFixture();

        FeatureCollection data = FeatureCollections.newCollection();
        data.add(fixture.createFeature("newOne", 1, 1));
        data.add(fixture.createFeature("newTen", 10, 10));

        assertEquals("size", 4, numberFeatures());
        ds.setFeatures(data);
        assertEquals("size", 2, numberFeatures());
    }

    public void testSetAutoCommit() throws DataSourceException {
        assertEquals("size", 4, numberFeatures());
        ds.setAutoCommit(true);
        assertEquals("size", 4, numberFeatures());
        ds.setAutoCommit(false);
        assertEquals("size", 4, numberFeatures());
    }

    public void testCommit() throws Exception {
        LockingDataSourceFixture fixture = new LockingDataSourceFixture();
        FeatureCollection data = FeatureCollections.newCollection();
        data.add(fixture.createFeature("newOne", 1, 1));
        data.add(fixture.createFeature("newTen", 10, 10));

        assertEquals("size", 4, numberFeatures());
        ds.setAutoCommit(false);
        assertEquals("size", 4, numberFeatures());
        ds.setFeatures(data);
        assertEquals("size", 2, numberFeatures());
        ds.commit();
        assertEquals("size", 2, numberFeatures());
    }

    public void testRollback() throws Exception {
        LockingDataSourceFixture fixture = new LockingDataSourceFixture();
        FeatureCollection data = FeatureCollections.newCollection();
        data.add(fixture.createFeature("newOne", 1, 1));
        data.add(fixture.createFeature("newTen", 10, 10));

        assertEquals("size", 4, numberFeatures());
        ds.setAutoCommit(false);
        assertEquals("size", 4, numberFeatures());
        ds.setFeatures(data);
        assertEquals("size", 2, numberFeatures());
        ds.rollback();
        assertEquals("size", 4, numberFeatures());
    }

    public void testRollbackTransactionLockRelease()
        throws UnsupportedOperationException, DataSourceException {
        ds.setAutoCommit(false);
        ds.lockFeatures(filter12);

        assertTrue("A  locked1", isLocked(fid1));
        assertTrue("A  locked2", isLocked(fid2));
        assertFalse("A  locked3", isLocked(fid3));
        assertFalse("A  locked4", isLocked(fid4));
        assertEquals("locks", 2, numberLocked());
        ds.rollback();
        assertEquals("locks", 0, numberLocked());
    }

    public void testCommitTransactionalLockRelease()
        throws UnsupportedOperationException, DataSourceException {
        ds.setAutoCommit(false);
        ds.lockFeatures(filter12);

        assertTrue("A  locked1", isLocked(fid1));
        assertTrue("A  locked2", isLocked(fid2));
        assertFalse("A  locked3", isLocked(fid3));
        assertFalse("A  locked4", isLocked(fid4));
        assertEquals("locks", 2, numberLocked());
        ds.commit();
        assertEquals("locks", 0, numberLocked());
    }

    public void testGetAutoCommit() throws DataSourceException {
        assertTrue(ds.getAutoCommit());
    }

    public void testGetMetaData() {
        assertNotNull(ds.getMetaData());
    }

    public void testGetSchema() throws DataSourceException {
        assertNotNull(ds.getSchema());
    }

    public void testAbortLoading() {
        try {
            ds.abortLoading();
            fail("not supported");
        } catch (UnsupportedOperationException e) {
        }
    }

    public void testGetBounds() throws DataSourceException {
        Envelope bounds = ds.getBounds();
        assertEquals("minX", 2.0, bounds.getMinX(), TOLERENCE);
        assertEquals("minY", 2.0, bounds.getMinY(), TOLERENCE);
        assertEquals("maxX", 8.0, bounds.getMaxX(), TOLERENCE);
        assertEquals("maxY", 8.0, bounds.getMaxY(), TOLERENCE);
    }

    public void testBBox()
        throws UnsupportedOperationException, DataSourceException {
        Envelope bounds = ds.getBounds();
        assertEquals("minX", 2.0, bounds.getMinX(), TOLERENCE);
        assertEquals("minY", 2.0, bounds.getMinY(), TOLERENCE);
        assertEquals("maxX", 8.0, bounds.getMaxX(), TOLERENCE);
        assertEquals("maxY", 8.0, bounds.getMaxY(), TOLERENCE);
    }

    public void testLockExpire() throws Exception {
        FeatureLock lock = FeatureLockFactory.generate("Timed", 1);
        ds.setFeatureLock(lock);
        assertFalse("before", isLocked(fid1));
        ds.lockFeatures(filter12);
        assertTrue("after", isLocked(fid1));
        Thread.sleep(50);

        assertFalse("expire", isLocked(fid1));
    }
}
