package org.geotools.jdbc;

import java.util.Properties;

import javax.sql.DataSource;

import org.apache.commons.dbcp.BasicDataSource;

public class JDBCDelegatingTestSetup extends JDBCTestSetup {

    JDBCTestSetup delegate;
    
    protected JDBCDelegatingTestSetup( JDBCTestSetup delegate ) {
        this.delegate = delegate;
    }

    public void setUp() throws Exception {
        super.setUp();
        
        delegate.setUp();
    }
    
    protected final void initializeDatabase() throws Exception {
        delegate.initializeDatabase();
    }

    protected void initializeDataSource(BasicDataSource ds, Properties db) {
        delegate.initializeDataSource(ds, db);
    }

    protected final SQLDialect createSQLDialect(JDBCDataStore dataStore) {
        return delegate.createSQLDialect(dataStore);
    }
    
    @Override
    protected void setUpDataStore(JDBCDataStore dataStore) {
        delegate.setUpDataStore(dataStore);
    }

    @Override
    protected String typeName(String raw) {
        return delegate.typeName(raw);
    }
    
    @Override
    protected String attributeName(String raw) {
        return delegate.attributeName(raw);
    }
}
