package org.geotools.data.mysql;

import org.geotools.data.jdbc.DefaultSQLBuilder;
import org.geotools.feature.AttributeType;

/**
 * A MySQL-specific instance of DefaultSQLBuilder, which supports MySQL 4.1's geometric
 * datatypes.
 * @author Gary Sheppard garysheppard@psu.edu
 */
public class MySQLSQLBuilder extends DefaultSQLBuilder {
    
    /**
     * Appends to the given StringBuffer a comma-separated list of the column names
     * given by the AttributeType array.  This takes geometric datatypes into account,
     * appending "AsText(colName) AS colName" instead of just "colName" for a geometric
     * datatype.  This yields WKT in the ResultSet, which MySQLAttributeReader knows
     * how to handle.
     * @param sql  A StringBuffer onto which the comma-separated list of column names
     * will be appended.
     * @param fidColumnName The name of the feature ID column (the primary key).
     * @param attributes The AttributeType objects describing the columns.
     */
    public void sqlColumns(StringBuffer sql, String fidColumnName, AttributeType[] attributes) {
        String sep = "";
        if (fidColumnName != null) {
            sql.append(fidColumnName);
            sep = ", ";
        }

        for (int i = 0; i < attributes.length; i++) {
            sql.append(sep);
            if (attributes[i].isGeometry()) {
                sql.append("AsText(" + attributes[i].getName() + ") AS " + attributes[i].getName());
            } else {
                sql.append(attributes[i].getName());
            }
            sep = ", ";
        }
    }    
    
}
