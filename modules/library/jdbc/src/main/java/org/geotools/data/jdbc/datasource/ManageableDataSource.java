package org.geotools.data.jdbc.datasource;

import java.sql.SQLException;

import javax.sql.DataSource;

public interface ManageableDataSource extends DataSource {
    /**
     * Closes up the datasource, frees all of its resources. No other connection
     * can be gathered from this DataSource once close() has been called
     * 
     */
    public void close() throws SQLException;
}
