/*
 *    Geotools2 - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2003, Geotools Project Managment Committee (PMC)
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

import org.geotools.feature.Feature;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureCollections;
import org.geotools.feature.AttributeType;
import org.geotools.feature.FeatureType;
import org.geotools.feature.IllegalAttributeException;

/**
 * MemoryLockingDataSource test cases.
 * 
 * @see org.geotools.data
 * @author jgarnett, Refractions Reasearch Inc.
 * @version CVS Version
 */
public class MemoryLockingDataSourceTest extends LockingDataSourceTestCase {
    
    /** LockingDataSource being tested. */
    protected MemoryLockingDataSource mds;
    
    /**
     * Constructor for LockingDataSourceTestCase.
     * @param arg0
     */
    public MemoryLockingDataSourceTest(String arg0) {
        super(arg0);
    }
    
    /**
     * Used by setUp to initialize and populate a new LockingDataSource.
     * 
     * Initial Implementaion makes uses an InMemoryDataSource
     * @param features TestData required to populate DataSource
     * @return LockingDataSource Initialized and Populated with features
     */
    protected LockingDataSource createDataSource( FeatureCollection features ) throws UnsupportedOperationException, DataSourceException{
        LockingDataSource dataSource = new MemoryLockingDataSource();
        
        dataSource.addFeatures( features );
        return dataSource;        
    }
    
    /**
     * Sets up test fixture.
     * <p>
     * Subclasses should call super.setUp() which will use createDataSource()
     * to initialize and populate the LockingDataSource being tested.
     * <p>
     * </p>
     */
    protected void setUp() throws Exception {
        super.setUp();
        mds = (MemoryLockingDataSource) ds;         
    }
    /**
     * Closes DataSource and returns resources.
     * <p>
     * @throws java.lang.Exception
     * 
     * @see junit.framework.TestCase#tearDown()
     */
    protected void tearDown() throws Exception {        
        super.tearDown();
        mds.features = null;
        mds.locks = null;
        mds.transactionFeatures = null;
        mds.transactionLocks = null;
        mds = null;        
    }
    /**
     * Returns lock status for testing.
     * <p>
     * Overriden to check mds directly.</p>
     */
    protected boolean isLocked( String fid ){        
        return mds.isLocked( fid ); 
    }
    /**
     * Returns the number of features in the DataSource for testing.
     * </p>
     * @return Number of Features.
     */
    protected int numberFeatures(){
        return mds.getFeaturesMap().size();
    }
    /**
     * Returns the number of locked features for testing.
     * <p>
     * Offers more efficient implementation for MemoryLockingDataSource.</p>
     * @return Number of Locked features.
     */
    protected int numberLocked(){
        // May be able to provide a "generic" solution for
        // this by trying to aquire a lock with a negative
        // duration.
        mds.releaseExpiredLocks();
        return mds.getLocks().size();
    }
    //
    // LockingDataSource tests;
    //
    public void testModifyFeature() throws IllegalAttributeException {
        Feature feature = feature1;
        FeatureType type = feature.getFeatureType();
        AttributeType name = type.getAttributeType("name");
        
        assertEquals( "first", feature.getAttribute("name") );        
        mds.modifyFeature(
            feature, new AttributeType[]{ name, },
            new Object[]{ "MODIFIED",} );
        assertEquals( "MODIFIED", feature.getAttribute("name") ); 
    }
    public void testSetAutoCommitImplementation() throws DataSourceException {
        assertEquals( "size", 4, mds.features.size() );
        assertNull( "size", mds.transactionFeatures );
        mds.setAutoCommit( false );
        assertEquals( "size", 4, mds.features.size() );
        assertEquals( "size", 4, mds.transactionFeatures.size() );
        mds.setAutoCommit( true );
        assertEquals( "size", 4, mds.features.size() );
        assertNull( "size", mds.transactionFeatures );
    }
    public void testCommitImplementation() throws Exception {
        LockingDataSourceFixture fixture = new LockingDataSourceFixture();
        FeatureCollection data  = FeatureCollections.newCollection();
        data.add( fixture.createFeature( "newOne", 1, 1) ); 
        data.add( fixture.createFeature( "newTen", 10, 10) );
        
        assertEquals( "size", 4, mds.features.size() );
        assertNull( "size", mds.transactionFeatures );
        mds.setAutoCommit( false );
        mds.setFeatures( data );
        assertEquals( "size", 4, mds.features.size() );
        assertEquals( "size", 2, mds.transactionFeatures.size() );
        mds.commit();        
        assertEquals( "size", 2, mds.features.size() );
        assertEquals( "size", 2, mds.transactionFeatures.size() );
    }
    public void testRollbackImplementation() throws Exception {
        LockingDataSourceFixture fixture = new LockingDataSourceFixture();
        FeatureCollection data  = FeatureCollections.newCollection();
        data.add( fixture.createFeature( "newOne", 1, 1) ); 
        data.add( fixture.createFeature( "newTen", 10, 10) );
        
        assertEquals( "size", 4, mds.features.size() );
        assertNull( "size", mds.transactionFeatures );
        
        mds.setAutoCommit( false );
        assertEquals( "size", 4, mds.features.size() );
        assertEquals( "size", 4, mds.transactionFeatures.size() );                
        mds.setFeatures( data );
        assertEquals( "size", 4, mds.features.size() );
        assertEquals( "size", 2, mds.transactionFeatures.size() );
        mds.rollback();
        assertEquals( "size", 4, mds.features.size() );
        assertEquals( "size", 4, mds.transactionFeatures.size() );        
    }
    public void testRollbackTransactionLockReleaseImplementation() throws UnsupportedOperationException, DataSourceException{
        mds.setAutoCommit( false );
        mds.lockFeatures( filter12 );
        
        assertTrue( "A  locked1", isLocked( fid1 ) );
        assertTrue( "A  locked2", isLocked( fid2 ) );
        assertFalse( "A  locked3", isLocked( fid3 ) );
        assertFalse( "A  locked4", isLocked( fid4 ) );
        assertEquals( "locks", 0, mds.locks.size() );
        assertEquals( "locks", 2, mds.transactionLocks.size() );
        mds.rollback();
        assertEquals( "locks", 0, mds.locks.size() );
        assertEquals( "locks", 0, mds.transactionLocks.size() );
    }
    public void testCommitTransactionalLockReleaseImplmentation() throws UnsupportedOperationException, DataSourceException{
        mds.setAutoCommit( false );
        mds.lockFeatures( filter12 );
        
        assertTrue( "A  locked1", isLocked( fid1 ) );
        assertTrue( "A  locked2", isLocked( fid2 ) );
        assertFalse( "A  locked3", isLocked( fid3 ) );
        assertFalse( "A  locked4", isLocked( fid4 ) );
        assertEquals( "locks", 0, mds.locks.size() );
        assertEquals( "locks", 2, mds.transactionLocks.size() );
        mds.commit();
        assertEquals( "locks", 0, mds.locks.size() );
        assertEquals( "locks", 0, mds.transactionLocks.size() );
    }    
}
