package org.geotools.data.mysql;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import org.geotools.data.AttributeWriter;
import org.geotools.data.DataSourceException;
import org.geotools.data.jdbc.JDBCDataStore.FeatureTypeInfo;
import org.geotools.data.jdbc.JDBCUtils;
import org.geotools.data.jdbc.QueryData;
import org.geotools.data.jdbc.QueryData.RowData;
import org.geotools.data.jdbc.ResultSetAttributeIO;
import org.geotools.feature.AttributeType;

/**
 * A MySQL-specific AttributeWriter for non-geometric data.
 * @author Gary Sheppard garysheppard@psu.edu
 */
public class MySQLAttributeWriter extends ResultSetAttributeIO implements AttributeWriter {
    
    public MySQLAttributeWriter(AttributeType[] metaData, QueryData queryData, int startColumn, int endColumn) {
        super(metaData, queryData, startColumn, endColumn);
    }

    /**
     * Overrides ResultSetAttributeIO.write(int, Object).  This implementation of
     * the method does not actually use the ResultSet to make the update.  Instead,
     * it constructs an SQL statement and runs it.  This is because ResultSet objects
     * for tables that have any geometry (whether the column being updated is geometric
     * or not) are not updatable.
     * @param position the column to update; zero-based, excluding FID
     * @param attribute the data to write to the database
     * @exception IOException if this writer is already closed, or if there is an SQLException thrown while updating the column
     */
     public void write(int position, Object attribute) throws IOException {
        if (isClosed()) {
            throw new IOException(
                "Close has already been called on this MySQLAttributeWriter.");
        }

        Statement statement = null;

        try {
            Connection conn = queryData.getConnection();
            statement = conn.createStatement();

            FeatureTypeInfo ftInfo = queryData.getFeatureTypeInfo();
            StringBuffer sql = new StringBuffer("UPDATE ");
            sql.append(ftInfo.getFeatureTypeName() + " SET ");

            AttributeType type = metaData[position];
            String name = type.getName();
            String newValue = null;

            newValue = formatValue(attribute);

            sql.append(name + " = " + newValue);

            String fidColName = ftInfo.getFidColumnName();
            RowData rd = queryData.getRowData(this);
            Object fid = rd.read(1);
            sql.append(" WHERE " + fidColName + " = " + fid);

            System.out.println("this sql statement = " + sql.toString());
            statement.executeUpdate(sql.toString());

            //TODO: update with queryData.close(), need to figure it out
            //first.
        } catch (SQLException sqle) {
            String msg = "SQL Exception writing column";
            queryData.close(sqle, this);
            throw new DataSourceException(msg, sqle);
        } finally {
            JDBCUtils.close(statement);
        }
    }
    
    protected String formatValue(Object value) {
        String retString;

        if (value != null) {
            retString = "'" + value.toString() + "'";
        } else {
            retString = "null";
        }

        return retString;
    }    
        
}