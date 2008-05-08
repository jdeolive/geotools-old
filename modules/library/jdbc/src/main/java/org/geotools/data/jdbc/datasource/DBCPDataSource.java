package org.geotools.data.jdbc.datasource;

import java.sql.SQLException;

import org.apache.commons.dbcp.BasicDataSource;

/**
 * A closeable wrapper around {@link BasicDataSource}
 * 
 * @author Administrator
 * 
 */
public class DBCPDataSource extends AbstractManageableDataSource {

    public DBCPDataSource(BasicDataSource wrapped) {
        super(wrapped);

    }

    public void close() throws SQLException {
        ((BasicDataSource) wrapped).close();
    }

	public boolean isWrapperFor(Class type) throws SQLException {
		return false;
		//return this.wrapped.isWrapperFor(type);
	}

	public Object unwrap(Class type) throws SQLException {
		return null;
		//return this.wrapped.unwrap(type);
	}

}
