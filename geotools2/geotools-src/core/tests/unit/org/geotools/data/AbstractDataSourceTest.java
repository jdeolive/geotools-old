/*
 * AbstractDataSourceTest.java
 * JUnit based test
 *
 * Created on August 11, 2003, 9:13 PM
 */

package org.geotools.data;

import java.util.Set;
import junit.framework.*;
import org.geotools.feature.AttributeType;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureCollections;
import org.geotools.feature.FeatureType;
import org.geotools.filter.Filter;
import com.vividsolutions.jts.geom.Envelope;

/**
 *
 * @author jamesm
 */
public class AbstractDataSourceTest extends TestCase {
    
    DataSource ds, ds2;
    
    public AbstractDataSourceTest(java.lang.String testName) {
        super(testName);
    }
    
    public static Test suite() {
        TestSuite suite = new TestSuite(AbstractDataSourceTest.class);
        return suite;
    }
    
    public void setUp(){
        System.setProperty("org.geotools.feature.FeatureCollections", "org.geotools.feature.MockFeatureCollections");
        ds = new AbstractDataSourceTest.AbstractDataSourceImpl();
        ds2 = new AbstractDataSourceTest.AbstractDataSourceImpl2();
    }
    
    /** Test of getFeatures method, of class org.geotools.data.AbstractDataSource. */
    public void testGetFeatures() throws Exception{
        
        assertNotNull(ds.getFeatures());
        assertNotNull(ds.getFeatures(Filter.ALL));
        
    }
   
    
    
    /** Test of addFeatures method, of class org.geotools.data.AbstractDataSource. */
    public void testAddFeatures() throws Exception {
        try{
            System.out.println("calling addFeatures");
            ds.addFeatures(null);
            fail("Add features should not be supported");
        }
        catch(UnsupportedOperationException uoe){
        }
        try{
            ds2.addFeatures(null);
        }
        catch(UnsupportedOperationException uoe){
            fail("operation should be enabled");
        }
    }
    
    /** Test of removeFeatures method, of class org.geotools.data.AbstractDataSource. */
    public void testRemoveFeatures()throws Exception {
        try{
            ds.removeFeatures(Filter.NONE);
            fail("remove features should not be supported");
        }
        catch(UnsupportedOperationException uoe){
        }
        try{
            ds2.removeFeatures(Filter.NONE);
        }
        catch(UnsupportedOperationException uoe){
            fail("operation should be enabled");
        }
    }
    
    /** Test of modifyFeatures method, of class org.geotools.data.AbstractDataSource. */
    public void testModifyFeatures() throws Exception{
        System.out.println("testModifyFeatures");
        try{
            ds.modifyFeatures((AttributeType)null, null, Filter.NONE);
            fail("remove features should not be supported.");
        }
        catch(UnsupportedOperationException uoe){
        }
        try{
            ds2.modifyFeatures((AttributeType)null, null, Filter.NONE);
        }
        catch(UnsupportedOperationException uoe){
            fail("operation should be enabled");
        }
    }
    
    /** Test of setFeatures method, of class org.geotools.data.AbstractDataSource. */
    public void testSetFeatures() throws Exception{
        System.out.println("testSetFeatures");
        try{
            ds.setFeatures(null);
            fail("set features should not be supported.");
        }
        catch(UnsupportedOperationException uoe){
        }
        try{
            ds2.setFeatures(null);
        }
        catch(UnsupportedOperationException uoe){
            fail("operation should be enabled");
        }
    }
    
    /** Test of commit method, of class org.geotools.data.AbstractDataSource. */
    public void testCommit() throws DataSourceException {
        System.out.println("testCommit");
        try{
            ds.commit();
            
        }
        catch(UnsupportedOperationException uoe){
            fail("commit should be supported.");
        }
    }
    
    /** Test of rollback method, of class org.geotools.data.AbstractDataSource. */
    public void testRollback() throws DataSourceException {
        System.out.println("testRollback");
        try{
            ds.rollback();
            fail("rollback should not be supported.");
        }
        catch(UnsupportedOperationException uoe){
        }
        try{
            ds2.rollback();
        }
        catch(UnsupportedOperationException uoe){
            fail("operation should be enabled");
        }
    }
    
    /** Test of setAutoCommit method, of class org.geotools.data.AbstractDataSource. */
    public void testSetAutoCommit() throws DataSourceException {
        System.out.println("testSetAutoCommit");
        try{
            ds.setAutoCommit(false);
            fail("auto commit must not be disabled if rollback not be supported.");
        }
        catch(UnsupportedOperationException uoe){
        }
        try{
            ds2.setAutoCommit(true);
        }
        catch(UnsupportedOperationException uoe){
            fail("operation should be enabled");
        }
    }
    
    /** Test of getAutoCommit method, of class org.geotools.data.AbstractDataSource. */
    public void testGetAutoCommit() throws DataSourceException {
        System.out.println("testGetAutoCommit");
        assertTrue("Auto Comit should be true",  ds.getAutoCommit());
    }
    
    /** Test of getMetaData method, of class org.geotools.data.AbstractDataSource. */
    public void testGetMetaData() {
        
        System.out.println("testGetMetaData");
        ds.getMetaData();
        ds2.getMetaData();
        
    }
    
    /** Test of abortLoading method, of class org.geotools.data.AbstractDataSource. */
    public void testAbortLoading() throws DataSourceException {
        System.out.println("testAbortLoading");
        try{
            ds.abortLoading();
            fail("abort loading should not be disabled if rollback not be supported.");
        }
        catch(UnsupportedOperationException uoe){
        }
        try{
            ds2.abortLoading();
        }
        catch(UnsupportedOperationException uoe){
            fail("operation should be enabled");
        }        
    }
    
    /** Test of getBounds method, of class org.geotools.data.AbstractDataSource. */
    public void testGetBounds() throws DataSourceException {
        System.out.println("testGetBounds");
        try{
            ds.getBounds();
            fail("operation should not be supported.");
        }
        catch(UnsupportedOperationException uoe){
        }
        try{
            ds2.getBounds();
        }
        catch(UnsupportedOperationException uoe){
            fail("operation should be enabled");
        }     
    }
   
    public void testFastBBox(){
        assertFalse(ds.getMetaData().hasFastBbox());
        assertTrue(ds2.getMetaData().hasFastBbox());
    }
    
    public void testMetaDataToString(){
        System.out.println(ds.getMetaData());
        System.out.println(ds2.getMetaData());
    }
    
    /** Generated implementation of abstract class org.geotools.data.AbstractDataSource. Please fill dummy bodies of generated methods. */
    private class AbstractDataSourceImpl extends AbstractDataSource {
        
        
        public void getFeatures(FeatureCollection collection, Query query) throws DataSourceException {
            
        }
        
        
        public FeatureType getSchema() throws DataSourceException {
            return null;
        }
        
    }
    
    private class AbstractDataSourceImpl2 extends AbstractDataSource {
        protected DataSourceMetaData createMetaData() {
            MetaDataSupport all = new MetaDataSupport();
            all.setSupportsAdd(true);
            all.setSupportsAbort(true);
            all.setSupportsGetBbox(true);
            all.setSupportsRemove(true);
            all.setSupportsModify(true);
            all.setSupportsAbort(true);
            all.setSupportsRollbacks(true);
            all.setSupportsSetFeatures(true);
            all.setFastBbox(true);
            return all;
        }
        
        public void getFeatures(FeatureCollection collection, Query query) throws DataSourceException {
            
        }
        
        
        public FeatureType getSchema() throws DataSourceException {
            return null;
        }
        
    }
    
    
    
    // Add test methods here, they have to start with 'test' name.
    // for example:
    // public void testHello() {}
    
    
}
