/*
 * MockDataSourceFactory.java
 *
 * Created on August 16, 2003, 2:01 PM
 */

package org.geotools.data;

/**
 *
 * @author  jamesm
 */
public class MockDataSourceFactory implements org.geotools.data.DataSourceFactorySpi {
    
    /** Creates a new instance of MockDataSourceFactory */
    public MockDataSourceFactory() {
    }
    
    public boolean canProcess(java.util.Map params) {
        return params.containsKey("foo");
    }
    
    public DataSource createDataSource(java.util.Map params) throws DataSourceException {
        return new MockDataSourceFactory.MockDataSource();
    }
    
    public String getDescription() {
        return "A mock datasource factory for testing only";
    }
    
    class MockDataSource extends AbstractDataSource {
        
        public void getFeatures(org.geotools.feature.FeatureCollection collection, org.geotools.data.Query query) throws org.geotools.data.DataSourceException {
        }        
        
        public org.geotools.feature.FeatureType getSchema() throws org.geotools.data.DataSourceException {
            return null;
        }
        
    }
    
}
