/*
 * Created on 20/10/2003
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package org.geotools.data.jdbc;

import java.io.IOException;

import org.geotools.data.AttributeReader;
import org.geotools.data.AttributeWriter;
import org.geotools.data.DataSourceException;
import org.geotools.feature.AttributeType;

/** Provides a Mock JDBC DataStore for testing the abstract DataStore implementation.
 * 
 *  @author Sean Geoghegan, Defence Science and Technology Organisation.
 */
public class MockJDBCDataStore extends JDBCDataStore {

    /**
     * @param connectionPool
     * @throws DataSourceException
     */
    public MockJDBCDataStore(ConnectionPool connectionPool) throws IOException {
        super(connectionPool);    
    }

    

    /* (non-Javadoc)
     * @see org.geotools.data.jdbc.JDBCDataStore#createGeometryReader(org.geotools.feature.AttributeType, org.geotools.data.jdbc.JDBCDataStore.QueryData, int)
     */
    protected AttributeReader createGeometryReader(AttributeType attrType, QueryData queryData, int index)
        throws DataSourceException {
        // TODO Auto-generated method stub
        return null;
    }
    
    public String getFidColumnName(String ft) throws IOException {
        FeatureTypeInfo info = getFeatureTypeInfo(ft);
        return info.getFidColumnName();
    }
    
    /* (non-Javadoc)
     * @see org.geotools.data.jdbc.JDBCDataStore#createGeometryWriter(org.geotools.feature.AttributeType, org.geotools.data.jdbc.JDBCDataStore.QueryData, int)
     */
    protected AttributeWriter createGeometryWriter(AttributeType attrType, QueryData queryData, int index)
        throws DataSourceException {     
        return null;
    }
}
