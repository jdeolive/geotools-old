package org.geotools.jdbc;

import javax.sql.DataSource;

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

    protected final DataSource createDataSource() {
        return delegate.createDataSource();
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
