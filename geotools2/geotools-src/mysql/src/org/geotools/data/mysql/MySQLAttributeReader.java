package org.geotools.data.mysql;

import org.geotools.data.jdbc.QueryData;
import org.geotools.data.jdbc.WKTAttributeIO;
import org.geotools.feature.AttributeType;

/**
 * A MySQL-specific AttributeReader, which handles MySQL 4.1's geometric datatypes.
 * @author Gary Sheppard garysheppard@psu.edu
 */
public class MySQLAttributeReader extends WKTAttributeIO {
    
    public MySQLAttributeReader(AttributeType metaData, QueryData queryData, int columnIndex) {
        super(queryData, metaData, columnIndex);
    }

    /**
     * Formats a field name or a table name for use in an SQL statement.  WKTAttributeIO's
     * version of the method puts double quotes around the name and returns it.
     * Since MySQL does not need (or accept) quotes around field and table names,
     * this overriding method returns the name unaltered.
     * @param fieldName the name of a field or a table to be used in an SQL statement
     * @return fieldName properly formatted for an SQL statement.  In this case, the method returns <code>fieldName</code> unaltered.
     */
    protected String formatFieldName(String fieldName) {
        return fieldName;
    }
        
}