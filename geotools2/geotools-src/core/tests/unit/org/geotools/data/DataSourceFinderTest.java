/*
 * DataSourceFinderTest.java
 * JUnit based test
 *
 * Created on August 16, 2003, 1:41 PM
 */

package org.geotools.data;

import java.util.HashMap;
import junit.framework.*;
import org.geotools.factory.FactoryFinder;
import java.util.Iterator;
import java.util.Map;

/**
 *
 * @author jamesm
 */
public class DataSourceFinderTest extends TestCase {
    
    public DataSourceFinderTest(java.lang.String testName) {
        super(testName);
    }
    
    public static Test suite() {
        TestSuite suite = new TestSuite(DataSourceFinderTest.class);
        return suite;
    }
    
    /** Test of getDataSource method, of class org.geotools.data.DataSourceFinder. */
    public void testGetDataSource() throws DataSourceException {
        System.out.println("testGetDataSource");
        
        Map params = new HashMap();
        params.put("foo","bar");
        DataSource ds = DataSourceFinder.getDataSource(params);
        assertNotNull(ds);
        assertTrue(ds instanceof MockDataSourceFactory.MockDataSource);
        
    }
    
       public void testGetDataSourceWhenMissing() throws DataSourceException {
        System.out.println("testGetDataSource");
        
        Map params = new HashMap();
        params.put("wibble","bar");
        DataSource ds = DataSourceFinder.getDataSource(params);
        assertNull(ds);
        
    }
    
    /** Test of getAvailableDataSources method, of class org.geotools.data.DataSourceFinder. */
    public void testGetAvailableDataSources() {
        System.out.println("testGetAvailableDataSources");
        Iterator list = DataSourceFinder.getAvailableDataSources();
        assertNotNull(list);
    }
    
    // Add test methods here, they have to start with 'test' name.
    // for example:
    // public void testHello() {}
    
    
}
