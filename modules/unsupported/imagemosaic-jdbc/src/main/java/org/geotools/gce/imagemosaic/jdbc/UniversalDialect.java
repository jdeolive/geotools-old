/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 *
 *    (C) 2008, Open Source Geospatial Foundation (OSGeo)
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
package org.geotools.gce.imagemosaic.jdbc;

public class UniversalDialect extends DBDialect {
    public UniversalDialect(Config config) {
        super(config);
    }

    protected String getDriverClassName() {
        return "org.h2.Driver";
    }

    protected String getJDBCUrl(String host, Integer port, String dbName) {
        return "jdbc:h2:target/h2/testdata";
    }

    @Override
    protected String getBLOBSQLType() {
        return "BLOB";
    }

    @Override
    protected String getMultiPolygonSQLType() {
        // return "blob";
        return null;
    }

    @Override
    protected String getCreateSpatialTableStatement(String tableName)
        throws Exception {
        String statement = " CREATE TABLE " + tableName;
        statement += (" ( " + getConfig().getKeyAttributeNameInSpatialTable() +
        " CHAR(64) NOT NULL,");
        statement += (getConfig().getTileMinXAttribute() + " DOUBLE NOT NULL,");
        statement += (getConfig().getTileMinYAttribute() + " DOUBLE NOT NULL,");
        statement += (getConfig().getTileMaxXAttribute() + " DOUBLE NOT NULL,");
        statement += (getConfig().getTileMaxYAttribute() + " DOUBLE NOT NULL,");
        statement += ("CONSTRAINT " + tableName + "_PK PRIMARY KEY(" +
        getConfig().getKeyAttributeNameInSpatialTable());
        statement += "))";

        return statement;
    }

    @Override
    protected String getCreateSpatialTableStatementJoined(String tableName)
        throws Exception {
        String statement = " CREATE TABLE " + tableName;
        statement += (" ( " + getConfig().getKeyAttributeNameInSpatialTable() +
        " CHAR(64) NOT NULL,");
        statement += (getConfig().getTileMinXAttribute() + " DOUBLE NOT NULL,");
        statement += (getConfig().getTileMinYAttribute() + " DOUBLE NOT NULL,");
        statement += (getConfig().getTileMaxXAttribute() + " DOUBLE NOT NULL,");
        statement += (getConfig().getTileMaxYAttribute() + " DOUBLE NOT NULL,");
        statement += (getConfig().getBlobAttributeNameInTileTable() + " " +
        getBLOBSQLType() + ",");
        statement += ("CONSTRAINT " + tableName + "_PK PRIMARY KEY(" +
        getConfig().getKeyAttributeNameInSpatialTable());
        statement += "))";

        return statement;
    }

    @Override
    protected String getCreateIndexStatement(String tn)
        throws Exception {
        return "CREATE  INDEX IX_" + tn + " ON " + tn + "(" +
        getConfig().getTileMinXAttribute() + "," +
        getConfig().getTileMinYAttribute() + ")";
    }

    protected String getXMLConnectFragmentName() {
        return "connect.h2.xml.inc";
    }
}
