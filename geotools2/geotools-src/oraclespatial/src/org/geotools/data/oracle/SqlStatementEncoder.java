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
package org.geotools.data.oracle;

import org.geotools.feature.AttributeType;
import org.geotools.feature.FeatureType;
import org.geotools.filter.Filter;
import org.geotools.filter.SQLEncoder;
import org.geotools.filter.SQLEncoderException;
import java.util.logging.Logger;


/**
 * Provides SQL encoding functions for the Oracle Datasource
 *
 * @author Sean Geoghegan, Defence Science and Technology Organisation
 * @author $Author: seangeo $
 * @version $Id: SqlStatementEncoder.java,v 1.2 2003/11/05 00:53:37 seangeo Exp $ Last Modified: $Date: 2003/11/05 00:53:37 $
 */
final class SqlStatementEncoder {
    /** A logger for logging */
    private static final Logger LOGGER = Logger.getLogger("org.geotools.data.oracle");
    /** SQL Where clause encoder */
    private SQLEncoder whereEncoder;
    /** FID column of the table */
    private String fidColumn;
    /** Name of the table */
    private String tableName;

    /**
     * Creates a new SQL Statement encoder.
     *
     * @param whereEncoder This in the encoder used for where clauses.
     * @param tablename This the table name to use in SQL statements.
     * @param fidColumn The fid column for the table.
     */
    SqlStatementEncoder(SQLEncoder whereEncoder, String tablename, String fidColumn) {
        this.whereEncoder = whereEncoder;
        this.tableName = tablename;
        this.fidColumn = fidColumn;
    }

    /**
     * Constructs an Insert SQL statement template for this feature type.
     *
     * @param featureType The feature type to construct the statement for.
     *
     * @return The SQL insert template.  The FID column will always be first, followed by each
     *         feature attribute.  The VALUES section will contain ?'s for each attribute of the
     *         feature type.
     */
    String makeInsertSQL(FeatureType featureType) {
        StringBuffer sql = new StringBuffer("INSERT INTO ");

        sql.append(tableName);
        sql.append("(");
        sql.append(fidColumn);
        sql.append(",");

        AttributeType[] attributeTypes = featureType.getAttributeTypes();

        for (int i = 0; i < attributeTypes.length; i++) {
            sql.append(attributeTypes[i].getName());
            if (i < (attributeTypes.length - 1)) {
                sql.append(",");
            } else { 
                sql.append(")");
            }
        }

        sql.append(" VALUES (?,"); // fid column        

        for (int i = 0; i < attributeTypes.length; i++) {
            sql.append("?");
            if (i < (attributeTypes.length - 1)) {
                sql.append(",");
            } else { 
                sql.append(")");
            }
        }

        return sql.toString();
    }

    /**
     * Makes an SQL statement for getFeatures.  Constructs an SQL statement that will select the
     * features from the table based on the filter.
     *
     * @param attrTypes The Attribute types for the select statement
     * @param filter The filter to convert to a where statement.
     * @param maxFeatures The max amount of features to return.
     * @param useMax True if we are to use the maxFeature as the max.
     *
     * @return An SQL statement.
     *
     * @throws SQLEncoderException If an error occurs encoding the SQL
     */
    String makeSelectSQL(AttributeType[] attrTypes, Filter filter, int maxFeatures, boolean useMax)
        throws SQLEncoderException {
        LOGGER.finer("Creating sql for Query: mf=" + maxFeatures + " filter=" + filter 
            +  " useMax=" + useMax);

        StringBuffer sqlBuffer = new StringBuffer();

        sqlBuffer.append("SELECT ");
        sqlBuffer.append(fidColumn);

        for (int i = 0; i < attrTypes.length; i++) {
            sqlBuffer.append(", ");
            sqlBuffer.append(attrTypes[i].getName());
        }

        sqlBuffer.append(" FROM ");
        sqlBuffer.append(tableName);

        if (filter != null && filter != Filter.NONE) {
            String where = whereEncoder.encode(filter);

            sqlBuffer.append(" ");
            sqlBuffer.append(where);

            if (useMax && (maxFeatures > 0)) {
                sqlBuffer.append(" and ROWNUM <= ");
                sqlBuffer.append(maxFeatures);
            }
        } else if (useMax && (maxFeatures > 0)) {
            sqlBuffer.append(" WHERE ROWNUM <= ");
            sqlBuffer.append(maxFeatures);
        }

        String sqlStmt = sqlBuffer.toString();

        LOGGER.finer("sqlString = " + sqlStmt);

        return sqlStmt;
    }

    /**
     * Makes a template SQL statement for use in an update prepared statement. The template will
     * have the form:  <code>UPDATE &lt;tablename&gt; SET &lt;type&gt = ?</code>
     *
     * @param attributeTypes The feature attributes that are being updated.
     *
     * @return An SQL template.
     */
    String makeModifyTemplate(AttributeType[] attributeTypes) {
        StringBuffer buffer = new StringBuffer("UPDATE ");

        buffer.append(tableName);
        buffer.append(" SET ");

        for (int i = 0; i < attributeTypes.length; i++) {
            buffer.append(attributeTypes[i].getName());
            buffer.append(" = ? ");
            if (i < (attributeTypes.length - 1)) {
                buffer.append(", ");
            } else {
                buffer.append(" ");
            }
        }

        return buffer.toString();
    }

    String makeModifyTemplate(AttributeType[] attributeTypes, Filter filter)
        throws SQLEncoderException {
        String whereClause = whereEncoder.encode(filter);

        return makeModifyTemplate(attributeTypes) + " " + whereClause;
    }

    String makeDeleteSQL(Filter filter) throws SQLEncoderException {
        return "DELETE FROM " + tableName + " " + whereEncoder.encode(filter);
    }
}
