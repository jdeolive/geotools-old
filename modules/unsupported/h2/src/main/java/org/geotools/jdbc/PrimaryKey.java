/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 *
 *    (C) 2002-2008, Open Source Geospatial Foundation (OSGeo)
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
 */
package org.geotools.jdbc;

import java.net.URLDecoder;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;


/**
 * Primary key of a table.
 *
 * @author Justin Deoliveira, The Open Planning Project, jdeolive@openplans.org
 *
 */
public abstract class PrimaryKey {
    /**
     * Table name
     */
    String tableName;

    /**
     * the column name
     */
    String columnName;

    /**
     * the column type.
     */
    Class type;

    protected PrimaryKey(String tableName, String columnName, Class type) {
        this.tableName = tableName;
        this.columnName = columnName;
        this.type = type;
    }

    public String getTableName() {
        return tableName;
    }

    public String getColumnName() {
        return columnName;
    }

    public Class getType() {
        return type;
    }

    /**
     * Decodes a featureId into an array of objects which map to the columns
     * of the primary key.
     *
     * @param fid The featureId.
     *
     * @return An array of values which map the primary key columns making up
     * the featureId.
     *
     * @throws Exception
     */
    public Object decode(String fid) throws Exception {
        return URLDecoder.decode(fid, "UTF-8");
    }

    /**
     * Encodes a table row into a featureId by obtaining the primary key values
     * from the row.
     *
     * @param rs A result set pointing to a paritcular table row.
     *
     * @return A featureid for the row.
     *
     * @throws Exception
     */
    public String encode(ResultSet rs) throws SQLException {
        Object value = rs.getObject(columnName);

        //TODO: run column[i].type through converter to string
        //return tableName + "." + value.toString();
        return value.toString();
    }

    /**
     * Generates a new value for the primary key.
     *
     */
    public abstract String generate(Connection cx, SQLDialect dialect)
        throws Exception;
}
