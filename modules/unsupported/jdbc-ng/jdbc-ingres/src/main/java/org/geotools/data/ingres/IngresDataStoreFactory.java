package org.geotools.data.ingres;

import java.io.IOException;
import java.io.Serializable;
import java.util.Map;

import org.geotools.data.DataStore;
import org.geotools.jdbc.JDBCDataStore;
import org.geotools.jdbc.JDBCDataStoreFactory;
import org.geotools.jdbc.SQLDialect;

public class IngresDataStoreFactory extends JDBCDataStoreFactory {

    @Override
    protected SQLDialect createSQLDialect(JDBCDataStore dataStore) {
        return null;
    }

    @Override
    protected String getDatabaseID() {
        return null;
    }

    @Override
    protected String getDriverClassName() {
        return null;
    }

    @Override
    protected String getValidationQuery() {
        return null;
    }

    public DataStore createDataStore(Map<String, Serializable> params) throws IOException {
        return null;
    }

    public String getDescription() {
        return null;
    }

}
