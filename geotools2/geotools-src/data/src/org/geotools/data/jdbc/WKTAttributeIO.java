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
package org.geotools.data.jdbc;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;
import com.vividsolutions.jts.io.WKTWriter;
import org.geotools.data.DataSourceException;
import org.geotools.data.jdbc.JDBCDataStore.FeatureTypeInfo;
import org.geotools.data.jdbc.QueryData.RowData;
import org.geotools.feature.AttributeType;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * An attribute reader/writer for well known text.   For now this just reads
 * well known text in result sets, but if there are other cases of  wkt that
 * are not in databases we can rethink or adapt this class to them.  Result
 * sets should call AsText() when requesting the results, to put it into wkt.
 * 
 * <p>
 * Inserts will not work, should be overriden by the FeatureWriter using this,
 * to make the sql call directly, since right now we can not get
 * resultSet.updateString/Object to work on a geometry.
 * </p>
 *
 * @author Chris Holmes
 * @version $Id: WKTAttributeIO.java,v 1.8 2004/01/09 20:24:15 shepshep Exp $
 */
public class WKTAttributeIO extends ResultSetAttributeIO {
    /** The logger for the filter module. */
    private static final Logger LOGGER = Logger.getLogger(
            "org.geotools.data.jdbc");

    /** Factory for producing geometries (from JTS). */
    private static GeometryFactory geometryFactory = new GeometryFactory();

    /** Well Known Text reader (from JTS). */
    private static WKTReader geometryReader = new WKTReader(geometryFactory);

    /** Well Known Text writer (from JTS). */
    private static WKTWriter geometryWriter = new WKTWriter();
    private int columnIndex;

    public WKTAttributeIO(QueryData queryData, AttributeType metadata,
        int columnIndex) {
        super(new AttributeType[] { metadata }, queryData, columnIndex,
            columnIndex + 1);

        AttributeType[] attTypes = new AttributeType[] { metadata };

        if (!metadata.isGeometry()) {
            String mesg = "AttributeTypes of a WKTAttributeIO must be "
                + "geometries, " + metadata + " is not";
            throw new IllegalArgumentException(mesg);
        }

        this.queryData = queryData;
        this.columnIndex = columnIndex;
    }

    public Object read(int i)
        throws IOException, ArrayIndexOutOfBoundsException {
        if (isClosed()) {
            throw new IOException(
                "Close has already been called on this AttributeReader.");
        }

        // this Reader only reads one attribute so i should always be 0
        if (i != 0) {
            throw new ArrayIndexOutOfBoundsException(
                "This Reader only reads one attribute so i should always be 0");
        }

        Object retObject = null;

        try {
            RowData rd = queryData.getRowData(this);

            Object wkt = rd.read(columnIndex);

            if (wkt == null) {
                retObject = null;
            } else {
                retObject = geometryReader.read(wkt.toString());
            }

            //LOGGER.fine("returning " + retObject);
            return retObject;
        } catch (SQLException sqle) {
            queryData.close(sqle, this);
            throw new DataSourceException("Problem with sql", sqle);
        } catch (ParseException pe) {
            throw new DataSourceException("could not parse wkt" + pe);
        }
    }

    public void write(int position, Object attribute) throws IOException {
        if (isClosed()) {
            throw new IOException(
                "Close has already been called on this AttributeReader.");
        }

        // this Reader only reads one attribute so i should always be 0
        if (position != 0) {
            throw new ArrayIndexOutOfBoundsException(
                "This Reader only reads one attribute so i should always be 0");
        }

        // Ive commented this out - let the SQLException be thrown 
        //        try {
        //            //can we check if on insert row?  I'm pretty sure this method
        //            //won't work on an insert row, since it's not in the database yet.
        //            //The regular update ones we can just use update sql.
        //            if (resultSet.isAfterLast()) {
        //                throw new IOException(
        //                    "cannot insert WKT on insert row, must use"
        //                    + " a full sql insert statement");
        //            }
        //        } catch (SQLException e) {
        //            String msg = "SQL Error calling isLast on result set";
        //            LOGGER.log(Level.SEVERE, msg, e);
        //            throw new IOException(msg + ":" + e.getMessage());
        //        }
        Statement statement = null;

        try {
            //TODO: make this generic, working with JDBCFeatureStore.
            //first attempt at this failed miserably, but it was also
            //trying to do generic before specific.
            Connection conn = queryData.getConnection();
            statement = conn.createStatement();

            //SQLBuilder sqlBuilder = getSqlBuilder();
            FeatureTypeInfo ftInfo = queryData.getFeatureTypeInfo();
            StringBuffer sql = new StringBuffer("UPDATE ");
            sql.append(formatFieldName(ftInfo.getFeatureTypeName()) + " SET ");

            AttributeType type = metaData[0];
            String name = type.getName();
            String newValue = null;

            if (attribute instanceof Geometry) {
                int srid = ftInfo.getSRID(name);

                String geoText = geometryWriter.write((Geometry) attribute);
                newValue = "GeometryFromText('" + geoText + "', " + srid + ")";
            } else {
                newValue = formatValue(attribute);
            }

            sql.append(formatFieldName(name) + " = " + newValue);

            //sqlBuilder.buildSQLUpdate(queryData.getFeatureTypeInfo(), attribute, position, );
            String fidColName = ftInfo.getFidColumnName();
            RowData rd = queryData.getRowData(this);
            Object fid = rd.read(1);
            sql.append("WHERE " + fidColName + "=" + fid);

            LOGGER.fine("this sql statement = " + sql);
            statement.executeUpdate(sql.toString());

            //TODO: update with queryData.close(), need to figure it out
            //first.
        } catch (SQLException sqle) {
            String msg = "SQL Exception writing geometry column";
            LOGGER.log(Level.SEVERE, msg, sqle);
            queryData.close(sqle, this);
            throw new DataSourceException(msg, sqle);
        } finally {
            JDBCUtils.close(statement);
        }
    }

    //TODO
    //protected SQLBuilder getSqlBuilder() {
    //new WktSqlBuilder();
    //}
    protected String formatValue(Object value) {
        String retString;

        if (value != null) {
            retString = "'" + value.toString() + "'";
        } else {
            retString = "null";
        }

        return retString;
    }
    
    /**
     * Formats a field name or a table name for use in an SQL statement.  This default
     * WKTAttributeIO version of the method puts double quotes around the name and
     * returns it.  Specific implementations should override this method if field
     * and table names are to be formatted differently; for example, MySQL field
     * and table names do not get quotes of any kind, so MySQL's version of formatFieldName(String)
     * simply returns the field or table name unaltered.
     * @param fieldName the name of a field or a table to be used in an SQL statement
     * @return fieldName properly formatted for an SQL statement.  In this case, the method returns <code>"\"" + fieldName + "\""</code>.
     */
    protected String formatFieldName(String fieldName) {
        return "\"" + fieldName + "\"";
    }
    
}
