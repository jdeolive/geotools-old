/*
 *    Geotools2 - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002, Geotools Project Managment Committee (PMC)
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    version 2.1 of the License.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *
 */
package org.geotools.data.postgis;

import org.geotools.data.jdbc.DefaultSQLBuilder;
import org.geotools.data.jdbc.SQLBuilder;
import org.geotools.feature.AttributeType;
import org.geotools.filter.Filter;
import org.geotools.filter.SQLEncoder;
import org.geotools.filter.SQLEncoderException;
import org.geotools.filter.SQLEncoderPostgis;
import org.geotools.filter.SQLUnpacker;


/**
 * Builds sql for postgis.
 *
 * @author Chris Holmes
 */
public class PostgisSQLBuilder extends DefaultSQLBuilder {
    /**
     *
     */
    public PostgisSQLBuilder(int srid) {
        this((SQLEncoder) new SQLEncoderPostgis(srid));
    }

    /**
     * Constructor with encoder.
     *
     * @param encoder
     */
    public PostgisSQLBuilder(SQLEncoder encoder) {
        super(encoder);
    }

    /* (non-Javadoc)
     * @see org.geotools.data.jdbc.SQLStatementBuilder#buildSQLQuery(java.lang.String, org.geotools.feature.AttributeType[], org.geotools.filter.Filter)
     */
    public String buildSQLQuery(String typeName, String fidColumnName,
        AttributeType[] attrTypes, Filter filter) throws SQLEncoderException {
        StringBuffer sqlBuffer = new StringBuffer();

        sqlBuffer.append("SELECT ");
        sqlColumns(sqlBuffer, fidColumnName, attrTypes);
        sqlFrom(sqlBuffer, typeName);
        sqlWhere(sqlBuffer, filter);

        String sqlStmt = sqlBuffer.toString();

        return sqlStmt;
    }

    /**
     * Produces the select information required.
     * 
     * <p>
     * The featureType, if known, is always requested.
     * </p>
     * 
     * <p>
     * sql: <code>featureID (,attributeColumn)</code>
     * </p>
     * 
     * <p>
     * We may need to provide AttributeReaders with a hook so they can request
     * a wrapper function.
     * </p>
     *
     * @param sql
     * @param fidColumnName
     * @param attributes
     */
    public void sqlColumns(StringBuffer sql, String fidColumnName,
        AttributeType[] attributes) {
        if (fidColumnName != null) {
            sql.append(fidColumnName);
            sql.append(", ");
        }

        for (int i = 0; i < attributes.length; i++) {
            String colName = attributes[i].getName();

            if (attributes[i].isGeometry()) {
                sql.append("AsText(force_2d(\"" + colName + "\"))");
            } else {
                sql.append("\"" + colName + "\"");
            }

            if (i < (attributes.length - 1)) {
                sql.append(", ");
            }
        }
    }

    /**
     * Consutrcts FROM clause for featureType
     * 
     * <p>
     * sql: <code>FROM typeName</code>
     * </p>
     *
     * @param sql
     * @param typeName
     */
    public void sqlFrom(StringBuffer sql, String typeName) {
        sql.append(" FROM ");
        sql.append("\"" + typeName + "\"");
    }

    /**
     * Constructs WHERE clause, if needed, for FILTER.
     * 
     * <p>
     * sql: <code>WHERE filter encoding</code>
     * </p>
     *
     * @param sql DOCUMENT ME!
     * @param preFilter DOCUMENT ME!
     *
     * @throws SQLEncoderException DOCUMENT ME!
     */
    public void sqlWhere(StringBuffer sql, Filter preFilter)
        throws SQLEncoderException {
        if ((preFilter != null) || (preFilter == Filter.NONE)) {
            String where = encoder.encode(preFilter);
            sql.append(" ");
            sql.append(where);
        }
    }
}
